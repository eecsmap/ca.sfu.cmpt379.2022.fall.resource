package asmCodeGenerator.runtime;

import static asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType.*;
import static asmCodeGenerator.codeStorage.ASMOpcode.*;
import asmCodeGenerator.Macros;
import asmCodeGenerator.codeStorage.ASMCodeFragment;

public class RunTime {
    public static final String EAT_LOCATION_ZERO = "$eat-location-zero"; // helps us distinguish null pointers from real
                                                                         // ones.
    public static final String INTEGER_PRINT_FORMAT = "$print-format-integer";
    public static final String CHAR_PRINT_FORMAT = "$print-format-char";
    public static final String FLOAT_PRINT_FORMAT = "$print-format-float";
    public static final String STRING_PRINT_FORMAT = "$print-format-string";
    public static final String BOOLEAN_PRINT_FORMAT = "$print-format-boolean";
    public static final String NEWLINE_PRINT_FORMAT = "$print-format-newline";
    public static final String SPACE_PRINT_FORMAT = "$print-format-space";
    public static final String TAB_PRINT_FORMAT = "$print-format-tab";
    public static final String BOOLEAN_TRUE_STRING = "$boolean-true-string";
    public static final String BOOLEAN_FALSE_STRING = "$boolean-false-string";
    public static final String GLOBAL_MEMORY_BLOCK = "$global-memory-block";
    public static final String USABLE_MEMORY_START = "$usable-memory-start";
    public static final String MAIN_PROGRAM_LABEL = "$$main";
    public static final String FRAME_POINTER = "$$frame-pointer";
    public static final String STACK_POINTER = "$$stack-pointer";

    public static final String GENERAL_RUNTIME_ERROR = "$$general-runtime-error";
    public static final String INTEGER_DIVIDE_BY_ZERO_RUNTIME_ERROR = "$$i-divide-by-zero";
    public static final String FLOAT_DIVIDE_BY_ZERO_RUNTIME_ERROR = "$$f-divide-by-zero";

    private ASMCodeFragment environmentASM() {
        ASMCodeFragment result = new ASMCodeFragment(GENERATES_VOID);
        result.append(registers());
        result.append(jumpToMain());
        result.append(stringsForPrintf());
        result.append(runtimeErrors());
        result.add(DLabel, USABLE_MEMORY_START);
        return result;
    }

    private ASMCodeFragment registers() {
        ASMCodeFragment frag = new ASMCodeFragment(GENERATES_VOID);
        Macros.declareI(frag, FRAME_POINTER);
        Macros.declareI(frag, STACK_POINTER);
        frag.add(Memtop);
        frag.add(Duplicate);
        Macros.storeITo(frag, FRAME_POINTER);
        Macros.storeITo(frag, STACK_POINTER);
        return frag;
    }

    private ASMCodeFragment jumpToMain() {
        ASMCodeFragment frag = new ASMCodeFragment(GENERATES_VOID);
        frag.add(Jump, MAIN_PROGRAM_LABEL);
        return frag;
    }

    private ASMCodeFragment stringsForPrintf() {
        ASMCodeFragment frag = new ASMCodeFragment(GENERATES_VOID);
        frag.add(DLabel, EAT_LOCATION_ZERO);
        frag.add(DataZ, 8);
        frag.add(DLabel, CHAR_PRINT_FORMAT);
        frag.add(DataS, "%c");
        frag.add(DLabel, INTEGER_PRINT_FORMAT);
        frag.add(DataS, "%d");
        frag.add(DLabel, FLOAT_PRINT_FORMAT);
        frag.add(DataS, "%f");
        frag.add(DLabel, STRING_PRINT_FORMAT);
        frag.add(DataS, "%s");
        frag.add(DLabel, BOOLEAN_PRINT_FORMAT);
        frag.add(DataS, "%s");
        frag.add(DLabel, NEWLINE_PRINT_FORMAT);
        frag.add(DataS, "\n");
        frag.add(DLabel, SPACE_PRINT_FORMAT);
        frag.add(DataS, " ");
        frag.add(DLabel, TAB_PRINT_FORMAT);
        frag.add(DataS, "\t");
        frag.add(DLabel, BOOLEAN_TRUE_STRING);
        frag.add(DataS, "true");
        frag.add(DLabel, BOOLEAN_FALSE_STRING);
        frag.add(DataS, "false");

        return frag;
    }

    private ASMCodeFragment runtimeErrors() {
        ASMCodeFragment frag = new ASMCodeFragment(GENERATES_VOID);

        generalRuntimeError(frag);
        integerDivideByZeroError(frag);
        floatDivideByZeroError(frag);

        return frag;
    }

    private void floatDivideByZeroError(ASMCodeFragment frag) {
        String floatDivideByZeroMessage = "$errors-float-divide-by-zero";

        frag.add(DLabel, floatDivideByZeroMessage);
        frag.add(DataS, "float divide by zero");

        frag.add(Label, FLOAT_DIVIDE_BY_ZERO_RUNTIME_ERROR);
        frag.add(PushD, floatDivideByZeroMessage);
        frag.add(Jump, GENERAL_RUNTIME_ERROR);
    }

    private ASMCodeFragment generalRuntimeError(ASMCodeFragment frag) {
        String generalErrorMessage = "$errors-general-message";

        frag.add(DLabel, generalErrorMessage);
        frag.add(DataS, "Runtime error: %s\n");

        frag.add(Label, GENERAL_RUNTIME_ERROR);
        frag.add(PushD, generalErrorMessage);
        frag.add(Printf);
        frag.add(Halt);
        return frag;
    }

    private void integerDivideByZeroError(ASMCodeFragment frag) {
        String intDivideByZeroMessage = "$errors-int-divide-by-zero";

        frag.add(DLabel, intDivideByZeroMessage);
        frag.add(DataS, "integer divide by zero");

        frag.add(Label, INTEGER_DIVIDE_BY_ZERO_RUNTIME_ERROR);
        frag.add(PushD, intDivideByZeroMessage);
        frag.add(Jump, GENERAL_RUNTIME_ERROR);
    }

    public static ASMCodeFragment getEnvironment() {
        RunTime rt = new RunTime();
        return rt.environmentASM();
    }
}
