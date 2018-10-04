package Table;

public class Value {

	private String type = null;
	
	public Value(String type){
		this.type = type;
	}
	
	public String getType(){
		return type;
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "[type=" + type + "]";
	}
}