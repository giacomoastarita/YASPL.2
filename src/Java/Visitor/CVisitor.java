package Visitor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import Table.*;
import Tree.*;
import Visitable.*;


public class CVisitor implements Visitor{

	private StringTable string_table;
	private String file_content = "";
	private FileWriter file_writer;
	private File file;
	
	public CVisitor() {
		string_table = StringTable.getInstance();	
	}
	
	public void fileOut(String namefile){
		file = new File(namefile);
	}
	
	/* I seguenti metodi scrivono all'interno del file */
	@Override
	public Object visit(ProgramOp node) {
		//In list ci sono i figli del nodo
		ArrayList<TreeNode> list = node.getChildren();

		try{
			file_writer = new FileWriter(file);
		}catch(IOException e){
			System.err.println("Errore nella creazione del file");
			System.exit(-1);
		}

		file_content += "#include <stdio.h>\n\n";

		//visito il figlio "Decls" in cui ci sono le variabili globali e le funzioni e le scrivo nel file
		list.get(0).accept(this);

		file_content += "int main (int argc, char* argv[]){\n";

		//visito il figlio "Statement" in cui c'è il corpo del programma C
		list.get(1).accept(this);

		file_content += "\n\treturn 0; \n}\n";

		try{
			file_writer.write(file_content);
			file_writer.flush();
			file_writer.close();
		}catch(IOException e){
			System.err.println("Errore nella scrittura del file");
			System.exit(-1);
		}

		return file_content;
	}

	@Override
	public Object visit(ProDeclOp node) {
		
		//In list ci sono i figli del nodo
		ArrayList<TreeNode> list = node.getChildren();

		//Visito il nodo "Name", lo accetto e mi prendo il nome della funzione
		String name_function = list.get(0).accept(this).toString();

		//Visito il nodo "VarDecl", lo accetto e mi prendo i parametri in input
		String[] var_input = list.get(1).accept(this).toString().split(" ");

		String in = "";
		if(var_input.length == 2)
			in += var_input[0] + " " + var_input[1].substring(0, var_input.length - 1 );
		else
			for(int i = 1; i < var_input.length; i++){
				if(i == var_input.length - 1)
					in += var_input[0] + " " + var_input[i].substring(0, var_input[i].length() - 2);
				else
					in += var_input[0] + " " + var_input[i] + " ";
			}

		//Visito il nodo "ParDecl", lo accetto e mi prendo i parametri in output
		String[] var_out = list.get(2).accept(this).toString().split(" ");

		//Inizio a scrivere nel file	
		file_content += var_out[0] + " " + name_function + " (" + in +"){\n";

		//Visito il nodo "Body" e lo accetto
		list.get(3).accept(this);

		file_content += "\n \treturn " + var_out[1] + "}\n\n";

		return "";
	}

	@Override
	public Object visit(BodyOp node) {
		String write = "";

		//In list ci sono i figli del nodo
		ArrayList<TreeNode> list = node.getChildren();

		//Visito il nodo "VarDecl", lo accetto e mi prendo le variabili
		write += "\t" + list.get(0).accept(this).toString() +"\n";

		//scrivo le variabili nel file
		file_content += write;

		//Visito il nodo "Statements" e lo accetto
		list.get(1).accept(this);

		return "";
	}

	@Override
	public Object visit(ReadOp node) {
		file_content += "\tscanf(\"";

		//In list ci sono i figli del nodo
		ArrayList<TreeNode> list = node.getChildren();

		//Visito il nodo "Types", lo accetto e mi prendo il tipo delle variabili
		String[] type = list.get(1).accept(this).toString().split(", ");

		if(type.length > 1){
			for(int i = 0; i < type.length; i++){
				if(type[i].startsWith("i"))
					file_content += "%d";
				else if(type[i].startsWith("d"))
					file_content += "%lf";
			}
		}else{
			if(type[0].startsWith("i"))
				file_content += "%d";
			else if(type[0].startsWith("d"))
				file_content += "%lf";
		}

		file_content += "\", ";

		//Visito il nodo "VarOp", lo accetto e mi prendo il nome delle variabile
		String[] var = list.get(0).accept(this).toString().split(",");

		if(var.length > 1){
			for(int i = 0; i < var.length; i++){
				if(i == var.length-1)
					file_content += "&" + var[i].trim();
				else
					file_content += "&" + var[i].trim() + ",";
			}
		}else
			file_content += "&" + var[0].trim();

		file_content += ");\n";

		return "";
	}

	@Override
	public Object visit(WriteOp node) { 

		file_content += "\tprintf(";

		//In list ci sono i figli del nodo
		ArrayList<TreeNode> list = node.getChildren();

		//Visito il nodo "WriteOut", lo accetto e mi prendo la stringa e/o la variabile da stamapre
		String[] values = list.get(0).accept(this).toString().split(",");
		
		//se la lunghezza dell'array values è uguale a 2 allora o c'è solo la stringa o solo la variabile da stampare
		if(values.length == 2){
			if(values[1].startsWith("\""))
				file_content += values[1];
			else if(list.get(0).getType().equals("int"))
				file_content += "\"%d\\n\", " + values[1];
			else if(list.get(0).getType().equals("double"))
				file_content += "\"%lf\\n\", " + values[1];
		}else{
			file_content += "\"";
			for(int i = values.length-1; i > 0; i--){
				if(values[i].startsWith("\"")){
					String str = values[i].substring(1, values[i].length()- 1);
					file_content += str;
				}else if(list.get(0).getType().equals("int"))
					file_content += "%d";
				else if(list.get(0).getType().equals("double"))
					file_content += "%lf";
			}
			file_content += "\\n\", ";
			for(int i = values.length-1; i > 0; i--){
				if(!values[i].startsWith("\"")){
					if(values.length > 3)
						file_content += values[i] + ", ";
					else
						file_content += values[i];
				}
			}
		}
		
		file_content += ");\n";
		return "";
	}

	@Override
	public Object visit(CallOp node) {

		//In list ci sono i figli del nodo
		ArrayList<TreeNode> list = node.getChildren();

		//Visito il nodo "Name", lo accetto e mi prendo il nome della funzione
		String name_function = list.get(0).accept(this).toString();
		
		//Visito il nodo "Exprs", lo accetto e mi prendo i parametri in input alla funzione
		String[] input = list.get(1).accept(this).toString().split(",");
		
		String in = "";
		if(input.length == 1)
			in += input[0];
		else{
			for(int i = input.length - 1; i >= 0 ; i--){
				if(i == 0)
					in += input[i];
				else
					in += input[i] + ", ";
			}
		}
		
		//Visito il nodo "VarOp", lo accetto e mi prendo i paramtri in output della funzione
		String output = list.get(2).accept(this).toString();

		//creo la stringa da scrivere nel file
		file_content += "\t" + output + " = " + name_function +"("+ in +");\n";
		
		return "";
	}

	@Override
	public Object visit(AssignOp node) {

		String write = "";

		//In list ci sono i figli del nodo
		ArrayList<TreeNode> list = node.getChildren();

		//Visito il nodo "Name", lo accetto e mi prendo il nome della variabile
		write += "\t" + list.get(0).accept(this).toString();

		//Visito il nodo "Expr", lo accetto e mi prendo il valore (costante, variabile, risultato di un operazione aritmetica, ...)
		write += " = " + list.get(1).accept(this).toString() + ";\n";

		//scrivo nel file
		file_content += write;

		return "";
	}

	@Override
	public Object visit(IfThenElseOp node) {
		file_content += "\tif(";

		//In list ci sono i figli del nodo
		ArrayList<TreeNode> list = node.getChildren();

		//Visito il nodo "bool_expr", lo accetto e mi prendo l'espressione booleana
		file_content += list.get(0).accept(this).toString();

		file_content += "){\n\n";

		//Dato che la visita è bottom-up visito i figli partendo dall'ultimo, dopodiché li accetto e scrivo nel file gli statements
		ArrayList<TreeNode> stat1= list.get(1).getChildren();
		for(int i = stat1.size() - 1; i >=0; i--)
			stat1.get(i).accept(this);

		file_content += "\t}else{\n";

		//Dato che la visita è bottom-up visito i figli partendo dall'ultimo, dopodiché li accetto e scrivo nel file gli statements
		ArrayList<TreeNode> stat2= list.get(2).getChildren();
		for(int i = stat2.size() - 1; i >=0; i--)
			stat2.get(i).accept(this);

		file_content += "\t}\n";

		return "";
	}

	@Override
	public Object visit(IfThenOp node) {

		file_content += "\n\tif(";

		//In list ci sono i figli del nodo
		ArrayList<TreeNode> list = node.getChildren();

		//Visito il nodo "bool_expr", lo accetto e mi prendo l'espressione booleana
		file_content += list.get(0).accept(this).toString();

		file_content += "){\n\n";

		//Dato che la visita è bottom-up visito i figli partendo dall'ultimo, dopodiché li accetto e scrivo nel file gli statements
		ArrayList<TreeNode> statement= list.get(1).getChildren();
		for(int i = statement.size() - 1; i >=0; i--)
			statement.get(i).accept(this);

		file_content += "\t}\n";

		return "";
	}

	@Override
	public Object visit(WhileOp node) {

		file_content += "\n\twhile(";

		//In list ci sono i figli del nodo
		ArrayList<TreeNode> list = node.getChildren();

		//Visito il nodo "bool_expr", lo accetto e mi prendo l'espressione booleana
		file_content += list.get(0).accept(this).toString();

		file_content += "){\n\n";

		//Dato che la visita è bottom-up visito i figli partendo dall'ultimo, dopodiché li accetto e scrivo nel file gli statements
		ArrayList<TreeNode> statement= list.get(1).getChildren();
		for(int i = statement.size() - 1; i >=0; i--)
			statement.get(i).accept(this);

		file_content += "\t}\n";

		return "";
	}

	/* I seguenti metodi non scrivono all'interno del file. Costruiscono e restituiscono la stringa in base a ciò che visitano*/
	@Override
	public Object visit(Decls node) {
		String toReturn = "";

		//In list ci sono i figli del nodo
		ArrayList<TreeNode> list = node.getChildren();

		//visito i miei figli, li accetto e mi prendo le variabili globali e le funzioni
		for(int i = list.size() - 1; i >=0; i--){
			if(list.get(i).getName().equals("VarDeclOp"))
				file_content += list.get(i).accept(this).toString();

			toReturn = list.get(i).accept(this).toString() + toReturn;
		}

		return toReturn;
	}

	@Override
	public Object visit(Statements node) {

		//In list ci sono i figli del nodo
		ArrayList<TreeNode> list = node.getChildren();

		//Dato che la visita è bottom-up visito i figli partendo dall'ultimo, dopodiché li accetto e scrivo nel file gli statements
		for(int i = list.size() - 1; i >= 0; i--)
			list.get(i).accept(this);

		return "";
	}

	@Override
	public Object visit(VarDeclOp node) {
		String toReturn = "";

		//In list ci sono i figli del nodo
		ArrayList<TreeNode> list = node.getChildren();

		//visito il figlio "type", lo accetto e mi prendo il tipo della variabile
		String type = list.get(0).accept(this).toString();

		//visito il figlio "VarOp", lo accetto e mi prendo il nome della variabile
		String name_var = list.get(1).accept(this).toString();

		//Creo la stringa e la restituisco al padre 
		return toReturn += type + " " + name_var + ";\n";
	}

	@Override
	public Object visit(VarOp node) {
		String toReturn = "";

		//In list ci sono i figli del nodo
		ArrayList<TreeNode> list = node.getChildren();

		//Visito il nodo "Name", lo accetto e mi prendo il nome della variabile
		toReturn = list.get(0).accept(this).toString();

		//Il secondo figlio è "Vars". Se esso non è null vuol dire che ci sono altre variabili quindi lo visito, lo accetto e mi prendo i nomi
		if(list.size() > 1 && list.get(1) != null)
			toReturn += ", " + list.get(1).accept(this).toString();

		return toReturn;
	}

	@Override
	public Object visit(VarDecl node) { 
		String toReturn= "";

		//In list ci sono i figli del nodo
		ArrayList<TreeNode> list = node.getChildren();

		//Visito il nodo "VarDeclOp", lo accetto, mi prendo il tipo e il/i nome/i della/e variabile/i e restituisco la stringa
		if(list.size() != 0)
			toReturn += list.get(0).accept(this).toString();

		return toReturn;
	}

	@Override
	public Object visit(ParDecl node) {
		//In list ci sono i figli del nodo
		ArrayList<TreeNode> list = node.getChildren();

		//Visito il nodo "VarDeclOp", lo accetto, mi prendo il tipo e il/i nome/i della/e variabile/i e restituisco la stringa
		return list.get(0).accept(this).toString();
	}

	@Override
	public Object visit(CompStatOp node) {

		//In list ci sono i figli del nodo
		ArrayList<TreeNode> list = node.getChildren();

		//Visito il nodo "Statements" e lo accetto
		list.get(0).accept(this);

		return "";
	}

	@Override
	public Object visit(ExprArithOp node) {
		String toReturn = "";

		//In list ci sono i figli del nodo
		ArrayList<TreeNode> list = node.getChildren();

		//Visito il nodo "Expr", lo accetto e mi prendo l'espressione
		toReturn += list.get(0).accept(this).toString() + " ";

		//Visito il nodo "ArithmeticOperator", lo accetto e mi prendo l'operatore
		toReturn += list.get(2).accept(this).toString() + " ";

		//Visito il nodo "Expr", lo accetto e mi prendo l'espressione
		toReturn += list.get(1).accept(this).toString();

		return toReturn;
	}

	@Override
	public Object visit(UminuOp node) {
		String toReturn = "-";

		//In list ci sono i figli del nodo
		ArrayList<TreeNode> list = node.getChildren();

		//Visito il nodo "Expr", lo accetto e mi prendo l'espressione
		toReturn += list.get(0).accept(this).toString();

		return toReturn;
	}

	@Override
	public Object visit(BoolOp node) {
		String toReturn = "";

		//In list ci sono i figli del nodo
		ArrayList<TreeNode> list = node.getChildren();

		//Visito il nodo "Bool_expr", lo accetto e mi prendo l'espressione booleana
		toReturn += list.get(0).accept(this).toString() + " ";

		//Visito il nodo "RelOp", lo accetto e mi prendo l'operatore booleano
		toReturn += list.get(2).accept(this).toString() + " ";

		//Visito il nodo "Bool_expr", lo accetto e mi prendo l'espressione booleana
		toReturn += list.get(1).accept(this).toString();

		return toReturn;
	}

	@Override
	public Object visit(NotOp node) {
		String toReturn = "!";

		//In list ci sono i figli del nodo
		ArrayList<TreeNode> list = node.getChildren();

		//Visito il nodo "Bool_expr", lo accetto e mi prendo l'espressione booleana
		toReturn += list.get(0).accept(this).toString();

		return toReturn;
	}

	@Override
	public Object visit(ExpBoolOp node) {
		String toReturn = "";

		//In list ci sono i figli del nodo
		ArrayList<TreeNode> list = node.getChildren();

		//Visito il nodo "Expr", lo accetto e mi prendo l'espressione
		toReturn += list.get(0).accept(this).toString() + " ";

		//Visito il nodo "RelOp", lo accetto e mi prendo l'operatore booleano
		toReturn += list.get(2).accept(this).toString() + " ";

		//Visito il nodo "Expr", lo accetto e mi prendo l'espressione
		toReturn += list.get(1).accept(this).toString();

		return toReturn;
	}

	@Override
	public Object visit(WriteOut node) {
		
		String toReturn = "";
		
		//visito i miei figli e li accetto (pattern visitor)
		ArrayList<TreeNode> list = node.getChildren();
		
		for(TreeNode child : list)
			toReturn += "," + child.accept(this).toString();
		
		return toReturn;
	}

	@Override
	public Object visit(Types node) {

		String toReturn = "";

		//visito i miei figli e li accetto (pattern visitor)
		ArrayList<TreeNode> list = node.getChildren();
		for(TreeNode child : list){
			if(list.size() > 1)
				toReturn += child.accept(this).toString() + ", ";
			else
				toReturn += child.accept(this).toString();
		}

		return toReturn;
	}

	@Override
	public Object visit(Exprs node) {

		String toReturn = "";

		//visito i miei figli e li accetto (pattern visitor)
		ArrayList<TreeNode> list = node.getChildren();
		for(TreeNode child : list)
			toReturn += child.accept(this).toString() + "," ;

		return toReturn;
	}

	@Override
	public Object visit(Name node) {
		// Mi prendo il valore del nome della variabile, la cerco all'interno della tabella dei simboli
		return string_table.get((int) node.getValue());
	}

	@Override
	public Object visit(IntegerType node) {
		//restituisco, come stringa, il tipo della variabile
		return (String) node.getValue();
	}

	@Override
	public Object visit(BooleanType node) {
		//in C il tipo booleano viene visto come int
		return "int";
	}

	@Override
	public Object visit(DoubleType node) {
		//restituisco, come stringa, il tipo della variabile
		return (String) node.getValue();
	}

	@Override
	public Object visit(RelOp node) {
		//restituisco, come stringa, l'operatore relazionale
		return (String) node.getValue();
	}

	@Override
	public Object visit(BoolOperator node) {
		//restituisco, come stringa, l'operatore booleano
		return (String) node.getValue();
	}

	@Override
	public Object visit(ArithmeticOperator node) {
		//restituisco, come stringa, l'operatore aritmetico
		return (String) node.getValue();
	}

	@Override
	public Object visit(StringConstant node) {
		//restituisco la stringa
		return "\"" + node.getValue().toString() + "\"";
	}

	@Override
	public Object visit(IntegerConstant node) {
		//restituisco la costante intera
		return node.getValue().toString();
	}

	@Override
	public Object visit(DoubleConstant node) {
		//restituisco la costante double
		return node.getValue().toString();
	}

	@Override
	public Object visit(BoolConstant node) {
		//restituisco la costante booleana
		return node.getValue().toString();
	}
}
