package Table;

public class Key {

	private String type;
	private String lexem;
	
	public Key(String type, String lexem) {
		this.type = type; 
		this.lexem = lexem;
	}

	public String getType() {
		return type;
	}

	public String getLexem() {
		return lexem;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((lexem == null) ? 0 : lexem.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Key other = (Key) obj;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		if (lexem == null) {
			if (other.lexem != null)
				return false;
		} else if (!lexem.equals(other.lexem))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getName() + "[type=" + type + ", lexem=" + lexem + "]";
	}
}