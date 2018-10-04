package Visitable;
import Visitor.*;

public interface Visitable {
	
	public Object accept(Visitor visitor);
	
}