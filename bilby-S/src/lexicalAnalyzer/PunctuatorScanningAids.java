package lexicalAnalyzer;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


public class PunctuatorScanningAids {
	private static Set<Character> punctuatorStartingCharacters = new HashSet<Character>();
	private static Map<String, Set<Punctuator>> punctuatorsHavingPrefix = new HashMap<String, Set<Punctuator>>();
	private static final Set<Punctuator> emptyPunctuatorSet = Collections.unmodifiableSet(EnumSet.noneOf(Punctuator.class));
	static {
		makeStartingCharacters();
		makePunctuatorsHavingPrefix();
	}

	//////////////////////////////////////////////////////////////////////////////
	// public static interface
	
	public static boolean isPunctuatorStartingCharacter(Character c) {
		return punctuatorStartingCharacters.contains(c);
	}

	public static Set<Punctuator> punctuatorSetForPrefix(String prefix) {
		if(punctuatorsHavingPrefix.containsKey(prefix)) {
			return punctuatorsHavingPrefix.get(prefix);
		}
		else {
			return emptyPunctuatorSet;
		}
	}

	//////////////////////////////////////////////////////////////////////////////
	// creation of startingCharacters

	static private void makeStartingCharacters() {
		for(Punctuator p: Punctuator.values()) {
			String lexeme = p.getLexeme();
			if(!lexeme.isEmpty()) {
				punctuatorStartingCharacters.add(lexeme.charAt(0));
			}
		}
		punctuatorStartingCharacters = Collections.unmodifiableSet(punctuatorStartingCharacters);
	}
	
	
	//////////////////////////////////////////////////////////////////////////////
	// creation of prefix map

	private static void makePunctuatorsHavingPrefix() {
		for(Punctuator p: Punctuator.values()) {
			addAllPrefixesToMap(p);
		}
		makeAllMapEntriesconstutable();
	}
	private static void addAllPrefixesToMap(Punctuator punctuator) {
		String lexeme = punctuator.getLexeme();
		
		for(String prefix: allNonemptyPrefixes(lexeme)) {
			addPrefixToMap(prefix.intern(), punctuator);
		}
	}

	private static void addPrefixToMap(String prefix, Punctuator punctuator) {
		Set<Punctuator> setForThisPrefix = mutablePunctuatorSetForPrefix(prefix);
		setForThisPrefix.add(punctuator);
	}
	private static Set<Punctuator> mutablePunctuatorSetForPrefix(String prefix) {
		if(punctuatorsHavingPrefix.containsKey(prefix)) {
			return punctuatorsHavingPrefix.get(prefix);
		}
		else {
			EnumSet<Punctuator> emptySet = EnumSet.noneOf(Punctuator.class);
			punctuatorsHavingPrefix.put(prefix, emptySet);
			return emptySet;
		}
	}

	
	private static void makeAllMapEntriesconstutable() {
		for(Entry<String, Set<Punctuator>> entry: punctuatorsHavingPrefix.entrySet()) {
			replaceValueWithconstutableValue(entry);
		}
	}
	private static void replaceValueWithconstutableValue(Entry<String, Set<Punctuator>> entry) {
		Set<Punctuator> value = entry.getValue();
		Set<Punctuator> unmodifiableSet = Collections.unmodifiableSet(value);
		entry.setValue(unmodifiableSet);
	}
	
	
	//////////////////////////////////////////////////////////////////////////////
	// string utility

	private static String[] allNonemptyPrefixes(String string) {
		String[] result = new String[string.length()];
		
		for(int length = 1; length <= string.length(); length++) {
			result[length-1] = string.substring(0, length);
		}
		return result;
	}
	
}
