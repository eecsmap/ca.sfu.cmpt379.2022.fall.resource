package symbolTable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import logging.BilbyLogger;

import tokens.Token;

public class SymbolTable {
    private Map<String, Binding> table;

    public SymbolTable() {
        table = new HashMap<String, Binding>();
    }

    ////////////////////////////////////////////////////////////////
    // installation and lookup of identifiers

    public Binding install(String identifier, Binding binding) {
        table.put(identifier, binding);
        return binding;
    }

    public Binding lookup(String identifier) {
        return table.getOrDefault(identifier, Binding.nullInstance());
    }

    ///////////////////////////////////////////////////////////////////////
    // Map delegates

    public boolean containsKey(String identifier) {
        return table.containsKey(identifier);
    }

    public Set<String> keySet() {
        return table.keySet();
    }

    public Collection<Binding> values() {
        return table.values();
    }

    ///////////////////////////////////////////////////////////////////////
    // error reporting

    public void errorIfAlreadyDefined(Token token) {
        if (containsKey(token.getLexeme())) {
            multipleDefinitionError(token);
        }
    }

    protected static void multipleDefinitionError(Token token) {
        BilbyLogger log = BilbyLogger.getLogger("compiler.symbolTable");
        log.severe("variable \"" + token.getLexeme() + "\" multiply defined at " + token.getLocation());
    }

    ///////////////////////////////////////////////////////////////////////
    // toString

    public String toString() {
        StringBuffer result = new StringBuffer("    symbol table: \n");
        table.entrySet().forEach((entry) -> {
            result.append("        " + entry + "\n");
        });
        return result.toString();
    }
}
