package lexicalAnalyzer;

import static lexicalAnalyzer.PunctuatorScanningAids.punctuatorSetForPrefix;
import inputHandler.LocatedChar;
import inputHandler.PushbackCharStream;

import java.util.Set;

import tokens.Token;

/** Algorithm object to scan to find a punctuator.  Invoke only on a character that can start a punctuator lexeme.
 * <p>
 *  PunctuatorScanner will grab input characters as long as it as seen a punctuator lexeme prefix.
 *  If it discovers that it has taken input that is not such a prefix, it backtracks (possibly multiple
 *  characters) until it matches a punctuator.  
 * <p>
 *  If it cannot find a punctuator starting with the given starting character, it returns a NullToken at the
 *  startingCharacter's location. 
 * <p>
 *  PunctuatorScanner uses information derived from the lexeme fields of the enum constants of Punctuator,
 *  and should work regardless of what the set of constants in Punctuator is.
 *  Part of derived information is in PunctuatorScanningAids.java, part is in Punctuator itself.
 */
public class PunctuatorScanner {
	private PushbackCharStream input;
	private PartiallyScannedPunctuator scanned;
	
	public static Token scan(LocatedChar startingCharacter, PushbackCharStream input) {
		PunctuatorScanner scanner = new PunctuatorScanner(startingCharacter, input);
		return scanner.scanPunctuator();
	}
	
	private PunctuatorScanner(LocatedChar startingCharacter, PushbackCharStream input) {
		this.input = input;
		
		scanned = new PartiallyScannedPunctuator(startingCharacter);
	}
	
	private Token scanPunctuator() {
		Set<Punctuator> punctuators = punctuatorSetForPrefix(scanned.asString());
		
		if(punctuators.size() == 1 && scanned.isPunctuator()) {
			return scanned.asToken();
		}
		
		if(punctuators.isEmpty() || !input.hasNext()) {
			backupToLastPunctuatorPrefix();
			return scanned.asToken();
		}
		
		scanned.add(input.next());
		return scanPunctuator();
	}

	private void backupToLastPunctuatorPrefix() {
		while(!scanned.isPunctuator() && !scanned.isEmpty()) {
			LocatedChar lc = scanned.chopTail();
			input.pushback(lc);
		}
	}
}
