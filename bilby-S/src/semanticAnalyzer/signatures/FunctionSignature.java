package semanticAnalyzer.signatures;

import java.util.List;

import semanticAnalyzer.types.PrimitiveType;
import semanticAnalyzer.types.Type;
import lexicalAnalyzer.Lextant;
import lexicalAnalyzer.Punctuator;

// immutable
public class FunctionSignature {
    private static final boolean ALL_TYPES_ACCEPT_ERROR_TYPES = true;
    private Type resultType;
    private Type[] paramTypes;
    Object whichVariant;

    ///////////////////////////////////////////////////////////////
    // construction

    public FunctionSignature(Object whichVariant, Type... types) {
        assert (types.length >= 1);
        storeParamTypes(types);
        resultType = types[types.length - 1];
        this.whichVariant = whichVariant;
    }

    private void storeParamTypes(Type[] types) {
        paramTypes = new Type[types.length - 1];
        for (int i = 0; i < types.length - 1; i++) {
            paramTypes[i] = types[i];
        }
    }

    ///////////////////////////////////////////////////////////////
    // accessors

    public Object getVariant() {
        return whichVariant;
    }

    public Type resultType() {
        return resultType;
    }

    public boolean isNull() {
        return false;
    }

    ///////////////////////////////////////////////////////////////
    // main query

    public boolean accepts(List<Type> types) {
        if (types.size() != paramTypes.length) {
            return false;
        }

        for (int i = 0; i < paramTypes.length; i++) {
            if (!assignableTo(paramTypes[i], types.get(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean assignableTo(Type variableType, Type valueType) {
        if (valueType == PrimitiveType.ERROR && ALL_TYPES_ACCEPT_ERROR_TYPES) {
            return true;
        }
        return variableType.equals(valueType);
    }

    // Null object pattern
    private static FunctionSignature neverMatchedSignature = new FunctionSignature(1, PrimitiveType.ERROR) {
        public boolean accepts(List<Type> types) {
            return false;
        }

        public boolean isNull() {
            return true;
        }
    };

    public static FunctionSignature nullInstance() {
        return neverMatchedSignature;
    }

    ///////////////////////////////////////////////////////////////////
    // Signatures for bilby-0 operators
    // this section will probably disappear in bilby-1 (in favor of
    /////////////////////////////////////////////////////////////////// FunctionSignatures)

    private static FunctionSignature addSignature = new FunctionSignature(1, PrimitiveType.INTEGER,
            PrimitiveType.INTEGER, PrimitiveType.INTEGER);
    private static FunctionSignature subtractSignature = new FunctionSignature(1, PrimitiveType.INTEGER,
            PrimitiveType.INTEGER);
    private static FunctionSignature multiplySignature = new FunctionSignature(1, PrimitiveType.INTEGER,
            PrimitiveType.INTEGER, PrimitiveType.INTEGER);
    private static FunctionSignature greaterSignature = new FunctionSignature(1, PrimitiveType.INTEGER,
            PrimitiveType.INTEGER, PrimitiveType.BOOLEAN);

    // the switch here is ugly compared to polymorphism. This should perhaps be a
    // method on Lextant.
    public static FunctionSignature signatureOf(Lextant lextant) {
        assert (lextant instanceof Punctuator);
        Punctuator punctuator = (Punctuator) lextant;

        switch (punctuator) {
        case ADD:
            return addSignature;
        case SUBTRACT:
            return subtractSignature;
        case MULTIPLY:
            return multiplySignature;
        case GREATER:
            return greaterSignature;

        default:
            return neverMatchedSignature;
        }
    }

}