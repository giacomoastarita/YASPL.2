package Visitor;

import java.util.*;
import Table.*;
import Tree.*;
import Visitable.*;

public class SemanticVisitor implements Visitor {

	private Stack<SymbolTable> stack;
	private StringTable stringTable;

	public SemanticVisitor() {
		stack = new Stack<SymbolTable>();
		stringTable = StringTable.getInstance();
	}

	/* 
	 *	Scoping (parte A)
	 *  Se il nodo è legato ad un costrutto di creazione di nuovo scope allora il nodo è visitato per la prima volta
	 *  crea una nuova tabella, la lega al nodo corrente e la inserisce al top dello stack (push).
	 *  Se il nodo è visitato per l’ultima volta allora la elimina dal top dello stack (pop)
	 */
	@Override
	public Object visit(ProgramOp node) {

		//Creo la tabella, la lego al nodo e la inserisco nello stack
		SymbolTable programTable = new SymbolTable();
		node.setSymbolTable(programTable);
		stack.push(programTable);

		//visito i miei figli e li accetto (pattern visitor)
		ArrayList<TreeNode> list = node.getChildren();
		for(TreeNode child : list)
			child.accept(this);

		//è l'ultima chiamata quindi tolgo la tabella dallo stack e restituisco la stringa
		stack.pop();
		return "Il programma semanticamente corretto";
	}	

	@Override
	public Object visit(ProDeclOp node) {

		//riferimento della tabella che si trova al top dello stack
		SymbolTable currentTable = stack.peek();

		//in list ci sono i figli del nodo
		ArrayList<TreeNode> list = node.getChildren();

		// Mi prendo il nome della funzione e la cerco all'interno della tabella dei simboli
		String name_func = stringTable.get((int) list.get(0).getValue());

		//setto la chiave
		Key key = new Key("name", name_func);

		//se la funzione è presente nella tabella che è al top dello stack restituisco errore, altrimenti l'aggiungo
		if(currentTable.containsKey(key))
			throw new Error("Errore semantico: la funzione " + name_func + " è stata già dichiarata");
		else{
			Value value = new Value("void");
			currentTable.put(key, value);
			node.setType("void");
		}

		//Creo la tabella, la lego al nodo e la inserisco nello stack
		SymbolTable functionTable = new SymbolTable();
		node.setSymbolTable(functionTable);
		stack.push(functionTable);

		//visito i miei figli e li accetto (pattern visitor)
		for(TreeNode child : list)
			child.accept(this);

		//è l'ultima chiamata quindi tolgo la tabella dallo stack
		stack.pop();
		return null;
	}

	/* 
	 *	Scoping (parte B)
	 *  Se il nodo è legato ad un costrutto di dichiarazione variabile o funzione allora se la tabella al top dello stack contiene già la dichiarazione 
	 *  dell’identificatore coinvolto si restituisce “errore di dichiarazione multipla” altrimenti si aggiunge la dichiarazione alla tabella al top dello stack
	 */

	@Override
	public Object visit(VarDeclOp node) {
		//in list ci sono i figli del nodo
		ArrayList<TreeNode> list = node.getChildren();

		//mi prendo il tipo della variabile
		String type = (String) list.get(0).getValue();

		//visito i miei figli e li accetto (pattern visitor)
		for(TreeNode child : list){
			child.setType(type);
			child.accept(this);
		}
		return null;
	}

	@Override
	public Object visit(ParDecl node) { 
		//visito i miei figli e li accetto (pattern visitor)
		ArrayList<TreeNode> list = node.getChildren();
		for(TreeNode child : list)
			child.accept(this);

		return null;
	}

	@Override
	public Object visit(VarOp node) {

		//riferimento della tabella che si trova al top dello stack
		SymbolTable currentTable = stack.peek();

		//in list ci sono i figli del nodo
		ArrayList<TreeNode> list = node.getChildren();

		// Mi prendo il valore del nome della variabile e la cerco all'interno della tabella dei simboli
		String name_var = stringTable.get((int) list.get(0).getValue());

		//setto la chiave
		Key key = new Key("name", name_var);

		//setto il valore
		Value value = new Value(node.getType());

		//se la variabile è presente nella tabella che è al top dello stack restituisco errore, altrimenti lo aggiungo
		if(currentTable.containsKey(key))
			throw new Error("Errore semantico, dichiarazione multipla: la variabile " + name_var + " è stata già dichiarata");
		else
			currentTable.put(key, value);

		//visito i miei figli e li accetto (pattern visitor)
		for(TreeNode child : list){
			child.setType(node.getType());
			child.accept(this);
		}

		return null;
	}

	/* 
	 *	Type-check 
	 * 	Aggiungere un type a tutti i nodi dell'albero e verificare che le specifiche di tipo del linguaggio siano rispettate
	 */

	/* Se il nodo è legato ad un uso di un identificatore allora si mette in cur_tab il riferimento al top corrente dello stack */
	@Override
	public Object visit(Name node) {

		int size_stack = stack.size() - 1; //il n° di tabelle nello stack
		SymbolTable cur_tab; //rifetimento alla tabella
		String identifier = stringTable.get((int) node.getValue()); //nome dell'identificatore
		Key key = new Key("name", identifier);

		while(size_stack >= 0){

			cur_tab = stack.get(size_stack);

			if(cur_tab.containsKey(key)){
				node.setType(cur_tab.get(key).getType());
				return null;
			}

			size_stack --;
		}

		throw new Error("Errore semantico: l'identificatore " + identifier + " non è stato dichiarato");
	}

	/* Se il nodo è legato ad una costante allora node.type = tipo dato dalla costante */
	@Override
	public Object visit(BoolConstant node) {
		node.setType("boolean");
		return null;
	}

	@Override
	public Object visit(IntegerConstant node) {
		node.setType("int");
		return null;
	}

	@Override
	public Object visit(DoubleConstant node) {
		node.setType("double");
		return null;
	}

	@Override
	public Object visit(StringConstant node) {
		node.setType("String");
		return null;
	}

	/* Se il nodo è legato ad un costrutto riguardante operatori di espressioni o istruzioni allora controlla se i tipi dei nodi figli 
	 * rispettano le specifiche del type system. Se il controllo ha avuto successo allora si assegna al nodo il tipo indicato nel type system
	 * altrimenti restituisci “errore di tipo”  
	 */

	/* Costrutto while */
	@Override
	public Object visit(WhileOp node) {

		//in list ci sono i figli del nodo
		ArrayList<TreeNode> list = node.getChildren();

		//visito i miei figli e li accetto (pattern visitor)
		for(TreeNode child : list)
			child.accept(this);

		//controllo che il tipo del primo figlio sia boolean
		if(list.get(0).getType().equals("boolean"))
			node.setType("void");
		else
			throw new Error("Type mismatch nello statement WhileOp");

		return null;
	}

	/* Costrutto istruzione composta */
	@Override
	public Object visit(CompStatOp node) {

		//in list ci sono i figli del nodo
		ArrayList<TreeNode> list = node.getChildren();

		//visito i miei figli e li accetto (pattern visitor)
		for(TreeNode child : list)
			child.accept(this);

		node.setType("void");

		return null;
	}

	/* Costrutto assegnazione */
	@Override
	public Object visit(AssignOp node) {

		//in list ci sono i figli del nodo
		ArrayList<TreeNode> list = node.getChildren();

		//visito i miei figli e li accetto (pattern visitor)
		for(TreeNode child : list)
			child.accept(this);

		//controllo che il tipo dei due figli sia integer o double
		if(list.get(0).getType().equals(list.get(1).getType()))
			node.setType("void");
		else if(list.get(0).getType().equals("double") && list.get(1).getType().equals("int"))
			node.setType("void");
		else if(list.get(0).getType().equals("int") && list.get(1).getType().equals("double"))
			node.setType("void");
		else
			throw new Error("Type mismatch nello statement AssignOp");

		return null;
	}

	/* costrutti condizionali */
	@Override
	public Object visit(IfThenElseOp node) {

		//in list ci sono i figli del nodo
		ArrayList<TreeNode> list = node.getChildren();

		//visito i miei figli e li accetto (pattern visitor)
		for(TreeNode child : list)
			child.accept(this);

		//controllo che il tipo del primo figlio sia boolean
		if(list.get(0).getType().equals("boolean"))
			node.setType("void");
		else
			throw new Error("Type mismatch nello statement IfThenElseOp");

		return null; 
	}

	@Override
	public Object visit(IfThenOp node) {
		//in list ci sono i figli del nodo
		ArrayList<TreeNode> list = node.getChildren();

		//visito i miei figli e li accetto (pattern visitor)
		for(TreeNode child : list)
			child.accept(this);

		//controllo che il tipo del primo figlio sia boolean
		if(list.get(0).getType().equals("boolean"))
			node.setType("void");
		else
			throw new Error("Type mismatch nello statement IfThenOp");

		return null; 
	}

	/* Costrutto operatore relazionale binario */
	@Override
	public Object visit(ExpBoolOp node) { 

		//in list ci sono i figli del nodo
		ArrayList<TreeNode> list = node.getChildren();

		//visito i miei figli e li accetto (pattern visitor)
		for(TreeNode child : list)
			child.accept(this);

		//controllo che il tipo dei due figli sia integer o double
		if(list.get(0).getType().equals(list.get(1).getType()))
			node.setType("boolean");
		else if(list.get(0).getType().equals("double") && list.get(1).getType().equals("int"))
			node.setType("boolean");
		else if(list.get(0).getType().equals("int") && list.get(1).getType().equals("double"))
			node.setType("boolean");
		else
			throw new Error("Type mismatch nello statement ExpBoolOp");

		return null; 
	}

	/*Costrutto operatore relazionale negato*/
	@Override
	public Object visit(NotOp node) {

		//in list ci sono i figli del nodo
		ArrayList<TreeNode> list = node.getChildren();

		//visito i miei figli e li accetto (pattern visitor)
		for(TreeNode child : list)
			child.accept(this);

		//controllo che il tipo del figlio sia boolean
		if(list.get(0).getType().equals("boolean"))
			node.setType("int");
		else
			throw new Error("Type mismatch nello statement NotOp");

		return null;
	}

	/* costrutti operatori aritmetici */
	@Override
	public Object visit(ExprArithOp node) {

		//in list ci sono i figli del nodo
		ArrayList<TreeNode> list = node.getChildren();

		//visito i miei figli e li accetto (pattern visitor)
		for(TreeNode child : list)
			child.accept(this);

		//controllo che il tipo dei due figli 
		if(list.get(0).getType().equals("int") && list.get(1).getType().equals("int"))
			node.setType("int");
		else if(list.get(0).getType().equals("double") && list.get(1).getType().equals("double"))
			node.setType("double");
		else if(list.get(0).getType().equals("int") && list.get(1).getType().equals("double"))
			node.setType("double");
		else if(list.get(0).getType().equals("double") && list.get(1).getType().equals("double"))
			node.setType("double");
		else
			throw new Error("Type mismatch nello statement ExprArithOp");

		return null;
	}

	/* costrutti operatori booleani */
	@Override
	public Object visit(BoolOp node) {

		//in list ci sono i figli del nodo
		ArrayList<TreeNode> list = node.getChildren();

		//visito i miei figli e li accetto (pattern visitor)
		for(TreeNode child : list)
			child.accept(this);

		//controllo che il tipo dei due figli sia boolean
		if(list.get(0).getType().equals("boolean") && list.get(1).getType().equals("boolen"))
			node.setType("boolean");
		else
			throw new Error("Type mismatch nello statement BoolOp");

		return null;
	}

	/* Costrutto operatore unario minus */
	@Override
	public Object visit(UminuOp node) {

		//in list ci sono i figli del nodo
		ArrayList<TreeNode> list = node.getChildren();

		//visito i miei figli e li accetto (pattern visitor)
		for(TreeNode child : list)
			child.accept(this);

		//controllo che il tipo del figlio sia int o double
		if(list.get(0).getType().equals("int"))
			node.setType("int");
		else if(list.get(0).getType().equals("double"))
			node.setType("double");
		else
			throw new Error("Type mismatch nello statement UminuOp");

		return null;
	}

	/* Costrutto ReadOp*/
	@Override
	public Object visit(ReadOp node) {

		//riferimento della tabella che si trova al top dello stack
		SymbolTable currentTable = stack.peek();

		//In list ci sono i figli del nodo ReadOp
		ArrayList<TreeNode> list = node.getChildren(); 
		list.get(1).accept(this);

		//In variabili ci sono le variabili
		ArrayList<TreeNode> variabili = list.get(0).getChildren();

		//In variabili ci sono le variabili
		ArrayList<TreeNode> tipi = list.get(1).getChildren();

		//se il n° di variabili è diverso dal n° di tipi bisogna lanciare eccezione
		if(variabili.size() != tipi.size())
			throw new Error("Statement ReadOp: il numero di variabili non coincide con il numero di tipi");

		//per ogni variabile nello statement ReadOp controllo se il tipo combacia 
		for(int i = 0; i < variabili.size(); i++){
			// Mi prendo il valore del nome della variabile e la cerco all'interno della tabella dei simboli
			String name_var = stringTable.get((int) variabili.get(0).getValue());		

			//Setto la chiave
			Key key = new Key("name", name_var);

			//Mi prendo il tipo della variabile dalla tabella che si trova al top dello stack
			String type = currentTable.get(key).getType();

			if(type.equals(tipi.get(0).getType()))
				node.setType("void");
			else
				throw new Error("Type mismatch nello statement ReadOp");
		}

		return null;
	}

	/* Costrutto WriteOp*/
	@Override
	public Object visit(WriteOp node) {

		//visito i miei figli e li accetto (pattern visitor)
		ArrayList<TreeNode> list = node.getChildren();
		for(TreeNode child : list)
			child.accept(this);
		
		return null;
	}

	/* Costrutto CallOp */
	@Override
	public Object visit(CallOp node) {

		//riferimento della tabella che si trova al top dello stack
		SymbolTable currentTable = stack.peek();

		//In list ci sono i figli del nodo CallOp
		ArrayList<TreeNode> list = node.getChildren(); 
		
		//In espressioni ci sono le espressioni/variabili
		ArrayList<TreeNode> espressioni = list.get(1).getChildren();

		//per ogni variabile in espressioni controllo se è stata dichiarata
		for(int i = 0; i < espressioni.size(); i++){

			// Mi prendo il valore del nome della variabile e la cerco all'interno della tabella dei simboli
			String name_var = stringTable.get((int) espressioni.get(0).getValue());		

			//Setto la chiave
			Key key = new Key("name", name_var);

			if(!(currentTable.containsKey(key)))
				throw new Error("Errore semantico: la variabile " + name_var + " non è stata già dichiarata");
		}

		//In variabili ci sono le variabili
		ArrayList<TreeNode> variabili = list.get(2).getChildren();
		
		//per ogni variabile in variabili controllo se è stata dichiarata
		for(int i = 0; i < variabili.size(); i++)
			if(variabili.get(i).getValue() == null)
				throw new Error("Errore semantico statement CallOp: variabile non dichiarata");

		return null;
	}

	/* Tipi: Integer, Double, Boolean */
	@Override
	public Object visit(IntegerType node) {
		node.setType((String) node.getValue());
		return null;
	}

	@Override
	public Object visit(BooleanType node) {
		node.setType((String) node.getValue());
		return null;
	}

	@Override
	public Object visit(DoubleType node) {
		node.setType((String) node.getValue());
		return null;
	}

	/* Altri */
	@Override
	public Object visit(Types node) {

		//visito i miei figli e li accetto (pattern visitor)
		ArrayList<TreeNode> list = node.getChildren();
		for(TreeNode child : list){
			child.accept(this);
			node.setType(child.getType());
		}

		return null;
	}

	@Override
	public Object visit(BodyOp node) {

		//visito i miei figli e li accetto (pattern visitor)
		ArrayList<TreeNode> list = node.getChildren();
		for(TreeNode child : list)
			child.accept(this);

		return null;
	}

	@Override
	public Object visit(Decls node) { 

		//visito i miei figli e li accetto (pattern visitor)
		ArrayList<TreeNode> list = node.getChildren();
		for(TreeNode child : list)
			child.accept(this);

		return null;
	}

	@Override
	public Object visit(Statements node) {

		//visito i miei figli e li accetto (pattern visitor)
		ArrayList<TreeNode> list = node.getChildren();
		for(TreeNode child : list)
			child.accept(this);

		return null;
	}

	@Override
	public Object visit(Exprs node) { 

		//visito i miei figli e li accetto (pattern visitor)
		ArrayList<TreeNode> list = node.getChildren();
		for(TreeNode child : list)
			child.accept(this);

		return null;
	}

	@Override
	public Object visit(WriteOut node) {

		//visito i miei figli e li accetto (pattern visitor)
		ArrayList<TreeNode> list = node.getChildren();
		for(TreeNode child : list)
			child.accept(this);
		
		node.setType(list.get(0).getType());
		return null;
	}

	@Override
	public Object visit(VarDecl node) {

		//visito i miei figli e li accetto (pattern visitor)
		ArrayList<TreeNode> list = node.getChildren();
		for(TreeNode child : list)
			child.accept(this);

		return null;
	}

	@Override
	public Object visit(RelOp node) {
		node.setType("void");
		return null;
	}

	@Override
	public Object visit(BoolOperator node) {
		node.setType("void");
		return null;
	}

	@Override
	public Object visit(ArithmeticOperator node) {
		node.setType("void");
		return null;
	}
	
	//non deve fare nulla
	@Override
	public void fileOut(String namefile) {
	}
}