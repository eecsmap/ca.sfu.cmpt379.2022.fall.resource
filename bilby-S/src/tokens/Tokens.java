package tokens;

public class Tokens {
	public enum Level {
		FULL,
		TYPE_VALUE_NBINDINGS, 
		TYPE_VALUE_SEQ,
		TYPE_AND_VALUE,
	}
	static private Level printLevel = Level.FULL;
	
	/** Set the print level for Tokens.
	 * @param level
	 * 
	 */
	static public void setPrintLevel(Level level) {
		printLevel = level;
	}
	static public Level getPrintLevel() {
		return printLevel;
	}

	public static boolean isType(Token token, Class<?> ...classes) {
		for(Class<?> aClass: classes) {
			if( aClass.isInstance(token) ) {
				return true;
			}
		}
		return false;
	}
}
