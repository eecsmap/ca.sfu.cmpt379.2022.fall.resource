package asmCodeGenerator.codeStorage;

// ABSTRACT STACK MACHINE
// the machine described here consists of a stack (called the accumulator), a
// separate memory array, and a separate instruction store. There is also a PC
// (program counter) that holds the address (in the instruction store)
// of the next instruction to execute.

// The accumulator (having, say, 1024 locations) is used for most operations.
// Each accumulator location holds either an integer or a floating-point value.
// The content of memory location m in the memory array is denoted MEM(l);
// the memory is of some large size and m must be a non-negative int.
// Each memory location is one byte long. Floats and integers are stored in 8
// and 4 memory
// units, respectively. IMEM(m...m+3) means the four memory locations m, m+1,
// m+2, m+3 treated
// as an integer. FMEM(m...m+7) is similar for a floating-point number.

public enum ASMOpcode {
    // For the following arithmetic instructions, the one or two operands involved
    // (top element(s) of accumulator stack) must be integer.
    // If not, the machine halts. The result is an int.

    Add, // adds top two elements of accumulator, leaving result on accumulator. [... a
         // b] -> [... a+b]
    Subtract, // subtracts top of accumulator from 2nd element. [... a b] -> [... a-b]
    Negate, // [... a] -> [... -a]
    Multiply, // [... a b] -> [... a*b]
    Divide, // [... a b] -> [... a/b]
    Remainder, // [... a b] -> [... a%b] (% defined as in C/C++/Java, *not* a true modulo)

    // the following are for floating-point; they generate an error if an operand
    // is integer. The result is floating-point.

    FAdd, // [... a b] -> [... a+b]
    FSubtract, // [... a b] -> [... a-b]
    FNegate, // [... a] -> [... -a]
    FMultiply, // [... a b] -> [... a*b]
    FDivide, // [... a b] -> [... a/b]
    // There is no FRemainder.

    // the following are boolean operations; the top two (or one for BNegate)
    // elements of
    // the accumulator must be integers.
    // Each integer is treated as boolean TRUE if it is nonzero, and FALSE if it is
    // zero.
    // The result is an integer: 0 if FALSE, something nonzero if TRUE

    And, // [... a b] -> [... (a AND b)]
    Or, // [... a b] -> [... (a OR b)]
    Nand, // [... a b] -> [... (a NAND b)]
    Nor, // [... a b] -> [... (a NOR b)]
    Xor, // [... a b] -> [... (a XOR b)]
    BEqual, // [... a b] -> [... (a NXOR b)] (not (a xor b))
    BNegate, // [... a] -> [... (NOT a)]

    // the following are bitwise operations; the top two (or one for BTNegate)
    // elements of
    // the accumulator must be integers.

    BTAnd, // [... a b] -> [... (a AND b)]
    BTOr, // [... a b] -> [... (a OR b)]
    BTNand, // [... a b] -> [... (a NAND b)]
    BTNor, // [... a b] -> [... (a NOR b)]
    BTXor, // [... a b] -> [... (a XOR b)]
    BTEqual, // [... a b] -> [... (a NXOR b)] (not (a xor b))
    BTNegate, // [... a] -> [... (NOT a)]

    // Type conversions.
    ConvertF, // Convert the top of the accumulator from int to floating. Halt if top isn't
              // int.
    ConvertI, // Convert the top of the accumulator from floating to int. Halt if top isn't
              // floating.
              // The conversion is a truncation towards zero (floor for positives, ceiling for
              // negatives).

    // Accumulator stack manipulation
    Duplicate, // [... a] -> [... a a] (duplicate the top element of the accumulator)
    Exchange, // [... a b] -> [... b a]

    Pop, // [... a b] -> [... a]
    PushI, // takes an integer operand i: [... a] -> [... a i]
    PushD, // takes a string operand, pushes the data location labelled with this string.
    PushF, // takes a floating operand f: [... a] -> [... a f]
    PushPC, // [... a] -> [... a v] (where v is the (already incremented to next
            // instruction) value of the PC)
    PopPC, // [... a b] -> [... a] and the PC is set to b

    LoadC, // top of accumulator is treated as a location, and replaced with the contents
           // of
           // that memory location: [... a] -> [... MEM(a)] (MEM(a) is of type integer)
    LoadI, // [... a] -> [... IMEM(a..a+3)]
    LoadF, // [... a] -> [... FMEM(a..a+7)]

    StoreC, // top of accumulator, which must be an integer, is ANDed with 0xff
            // and stored in MEM(second stack element)
            // [... a b] -> [...] MEM(a) <- (b & 0xff) (stores 8 bit int)
    StoreI, // [... a b] -> [...] IMEM(a..a+3) <- b (stores 32 bit int)
    StoreF, // [... a b] -> [...] FMEM(a..a+7) <- b (stores 64-bit float)

    Memtop, // pushes the size s of the memory onto the accumulator stack. This is an
            // invalid address,
            // being the number after the last memory location. [... a] -> [... a s]

    // Control flow
    Label, // takes a string operand, and labels this place in the instruction store
           // with that string. (this is an assembler directive, not an instruction opcode)

    Jump, // takes a string operand, branches to statement with that label.
    JumpFalse, // takes a string operand. Pops top value (integer) from stack, does Jump if
               // value=0
    JumpTrue, // takes a string operand. Pops the top (integer) and Jumps if it is not 0.
    JumpNeg, // takes a string operand. Pops the top (integer) and Jumps if it is negative.
    JumpPos, // takes a string operand. Pops the top (integer) and Jumps if it is positive.
    JumpFNeg, // takes a string operand. Pops the top (floating) and Jumps if it is negative.
    JumpFPos, // takes a string operand. Pops the top (floating) and Jumps if it is positive.
    JumpFZero, // takes a string operand. Pops the top (floating) and Jumps if it is zero.
    Call, // takes a string operand. Jumps to that location, and pushes return instruction
          // location.

    JumpV, // [... addr] -> [...] Branches to addr.
    CallV, // [... addr] -> [...] Jumps to addr, and pushes return instruction location.

    Return, // another name for PopPC
    Halt, // stops the machine.

    // Data initialization directives (low memory; done once before program starts)
    DLabel, // takes a string operand, and labels the location of the next encountered data
    DataC, // takes an integer operand, and stores the low 8 bits in the next available
           // location.
    DataI, // takes an integer operand, and stores it in the next 4 available memory
           // locations.
    DataF, // takes a floating operand, and stores it in the next 8 available memory
           // locations.
    DataS, // stores a string in the next available memory locations. Not valid in a file;
           // used only in compilers. When written to a file, this should be broken into
           // DataC directives.
    DataZ, // takes an integer operand n, and stores zero in the next n available memory
           // locations.
    DataD, // takes a string (label) operand, and stores its value in the next 4 available
           // memory locations.

    PStack, // Nondestructively prints a copy of the current ASM accumulator stack. For
            // debugging purposes.

    // the absolutely amazing opcode
    Printf, // Does a C-style printf, with args taken from the top of the accumulator stack
            // (Top of accumulator = first arg, etc.)
            // Does not support 'I64' (wide integer) or 'n' or 'p' (pointer) specifiers

    Comment, // pseudo-opcode. puts a comment ("#" at start of line) followed by string
             // argument/comment in code.
             // used only in compilers. This is not a valid opcode on ASM, just in java.
    // Okay, I lied. There's one more opcode.
    Nop; // No operation; guaranteed to be the last opcode in this list.

    // Note: all Labels and DLabels must be unique (they're all in the same
    // namespace), and not equal to an opcode,
    // but their definition does not need to precede their usage. The emulator does
    // not warn you when you have
    // identical Labels and/or DLabels; it silently deletes one of the definitions.
    // Valid Labels and DLabels consist of letters, digits, _, -, and $, and they
    // don't start with a digit.
    //
    // Warning: the emulator does not ignore blank lines.
    // the maximum number of characters in a line for the emulator is 512. Don't go
    // near that.
    //
    // any line whose first character is "#" is treated as a comment (and ignored).
    // any string after the opcode and (optional) argument on a line is considered a
    // comment.

    public static final int ASMIntSize = 4;
    public static final int ASMFloatSize = 8;

    public boolean takesFloat() {
        return this == PushF || this == DataF;
    }

    public boolean takesInteger() {
        return this == PushI || this == DataC || this == DataI || this == DataZ;
    }

    public boolean takesString() {
        return this == PushD || this == DLabel || this == DataD || this == DataS || labelJumpOrCall();
    }

    private boolean labelJumpOrCall() {
        return this == Label || this == Call || (this.name().indexOf("Jump") == 0 && this != JumpV);
    }
}
