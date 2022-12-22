package semanticAnalyzer.types;

public interface Type {
	/** returns the size of an instance of this type, in bytes.
	 * 
	 * @return number of bytes per instance
	 */
	public int getSize(); 
	
	/** Yields a printable string for information about this type.
	 * use this rather than toString() if you want an abbreviated string.
	 * In particular, this yields an empty string for PrimitiveType.NO_TYPE.
	 * 
	 * @return string representation of type.
	 */
	public String infoString();
}
