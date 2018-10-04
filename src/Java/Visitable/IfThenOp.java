package Visitable;

import java.util.ArrayList;
import Visitor.Visitor;
import Tree.TreeNode;
public class IfThenOp  extends TreeNode{

	public IfThenOp(String n, Object v) {
		super(n, v);
		// TODO Auto-generated constructor stub
	}

	public IfThenOp(String n, ArrayList c) {
		super(n, c);
		// TODO Auto-generated constructor stub
	}
	
	public Object accept(Visitor visitor){
		return visitor.visit(this);
	}

}
