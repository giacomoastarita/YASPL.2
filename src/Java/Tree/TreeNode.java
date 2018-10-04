package Tree;

import java.util.ArrayList;
import Table.*;
import Visitor.*;
import Visitable.*;

public abstract class TreeNode<T> implements Visitable{
	
	private T value;
	private String name;
	private ArrayList<TreeNode<T>> children;
	private String type;
	private SymbolTable symbolTable;
	
	/* Costruttore per la foglia */
	public TreeNode(String name, T value) {
		this.name = name;
		this.value = value;		
		this.children = null;
	}

	/* Costruttore per un nodo interno */
	public TreeNode(String name, ArrayList<TreeNode<T>> children) {
		this.name = name;
		this.value = null;
		this.children = children;
	}
	
	public String toString() {
		if(children!=null){
			String children_string = "{";
			for(TreeNode<T> n : children){
				if(n!=null)
					children_string +=n.toString()+",";
				else
					children_string +=" ,";
			}
			children_string += "}";
			return getClass().getName() + "[name=" + name + ", children=" + children_string + "]";
		}else
			return getClass().getName() + "[name=" + name + ", value=" + value +"]";
		
	}
	
	public T getValue() {
		return value;
	}
	
	public void setValue(T value) {
		this.value = value;
	}
	
	public String getName() {
		return name;
	}
	
	public ArrayList<TreeNode<T>> getChildren() {
		return children;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public SymbolTable getSymbolTable() {
		return symbolTable;
	}

	public void setSymbolTable(SymbolTable symbolTable) {
		this.symbolTable = symbolTable;
	}
	
	@Override
	public abstract Object accept(Visitor visitor);
}