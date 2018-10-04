package Visitable;

import java.util.ArrayList;
import Visitor.Visitor;
import Tree.TreeNode;

public class ArithmeticOperator extends TreeNode{

	public ArithmeticOperator(String n, Object v) {
		super(n, v);
		// TODO Auto-generated constructor stub
	}
	
	public Object accept(Visitor visitor){
		return visitor.visit(this);
	}

}