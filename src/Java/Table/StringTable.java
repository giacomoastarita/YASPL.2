package Table;

import java.util.ArrayList;

public class StringTable extends ArrayList<String>{
	private static final long serialVersionUID = 1L;
	private static StringTable instance = null;
	
	private StringTable(){
		super();
	}
	
	public static final StringTable getInstance(){
		if(instance == null)
			instance = new StringTable();
		return instance;
	}

}