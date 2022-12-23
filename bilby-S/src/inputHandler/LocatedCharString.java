package inputHandler;


import java.util.ArrayList;
import java.util.List;



public class LocatedCharString {
	private static final int STARTING_SIZE = 6;
	
	// string and locatedChars are kept in lockstep: they both carry the same characters.
	// string is interned.
	private List<LocatedChar> locatedChars;
	private String string;
	protected TextLocation startingLocation;
	
	public LocatedCharString(LocatedChar c) {
		locatedChars = new ArrayList<LocatedChar>(STARTING_SIZE);
		string = "";
		startingLocation = c.getLocation();
		add(c);
	}
	
	// mutators
	public void add(LocatedChar c) {
		locatedChars.add(c);
		string += c.getCharacter();
		string = string.intern();
	}		
	public LocatedChar chopTail() {
		LocatedChar result = locatedChars.remove(locatedChars.size()-1);
		string = string.substring(0, string.length()-1);
		string = string.intern();
		return result;
	}
	
	// boolean queries
	public boolean isEmpty() {
		return locatedChars.isEmpty();
	}
	
	// conversions
	public String asString() {
		return string;
	}
}