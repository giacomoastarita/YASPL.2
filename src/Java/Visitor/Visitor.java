package Visitor;

import Visitable.*;

public interface Visitor {
	
	public Object visit(ProgramOp node); 
	public Object visit(Decls node); 
	public Object visit(Statements node); 
	public Object visit(VarDeclOp node); 
	public Object visit(VarOp node); 
	public Object visit(IntegerType node); 
	public Object visit(BooleanType node); 
	public Object visit(DoubleType node); 
	public Object visit(Name node); 
	public Object visit(ProDeclOp node); 
	public Object visit(VarDecl node); 
	public Object visit(ParDecl node); 
	public Object visit(BodyOp node); 
	public Object visit(CompStatOp node); 
	public Object visit(ReadOp node); 
	public Object visit(Types node); 
	public Object visit(WriteOp node); 
	public Object visit(AssignOp node); 
	public Object visit(Exprs node); 
	public Object visit(CallOp node); 
	public Object visit(IfThenElseOp node); 
	public Object visit(IfThenOp node); 
	public Object visit(WhileOp node);
	public Object visit(StringConstant node); 
	public Object visit(IntegerConstant node); 
	public Object visit(DoubleConstant node);
	public Object visit(ExprArithOp node); 
	public Object visit(UminuOp node); 
	public Object visit(BoolConstant node);  
	public Object visit(BoolOp node); 
	public Object visit(NotOp node); 
	public Object visit(ExpBoolOp node); 
	public Object visit(WriteOut node);
	public Object visit(RelOp node);
	public Object visit(BoolOperator node);
	public Object visit(ArithmeticOperator node);
	public void fileOut(String namefile);
}
