package tokens;

import inputHandler.Locator;

public class CharacterToken extends TokenImp {

    private char value;

    protected CharacterToken(Locator locator, String lexeme) {
        super(locator, lexeme);
    }

    @Override
    protected String rawString() {
        return "character, " + value;
    }

    public static Token make(Locator locator, String lexeme) {
        CharacterToken result = new CharacterToken(locator, lexeme);
        result.setValue(parseValue(lexeme));
        return result;
    }

    private static char parseValue(String lexeme) {
        // speical case: "# " is a space char
        assert lexeme.length() > 1 && lexeme.charAt(0) == '#';
        char ch1 = lexeme.charAt(1);
        if (ch1 >= '0' && ch1 < '8') {
            int index = Integer.parseInt(lexeme.substring(1), 8);
            if (index < 32 || index > 126) {
                throw new IllegalArgumentException("invalid character lexeme: " + lexeme);
            }
            return (char) index;
        }
        // ##
        if (ch1 == '#') {
            if (lexeme.length() != 3) {
                throw new IllegalArgumentException("invalid character lexeme: " + lexeme);
            }
            char ch2 = lexeme.charAt(2);
            if (ch2 >= '0' && ch2 <= '9' || ch2 == '#') {
                return ch2;
            }
            throw new IllegalArgumentException("invalid character lexeme: " + lexeme);
        }
        // #c
        return ch1;
    }

    private void setValue(char ch) {
        value = ch;
    }

    public char getValue() {
        return value;
    }

}
