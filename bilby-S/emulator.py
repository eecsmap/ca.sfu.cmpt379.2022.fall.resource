import logging
import operator
import struct


# ABSTRACT STACK MACHINE
# the machine described here consists of a stack (called the accumulator), a
# separate memory array, and a separate instruction store.  There is also a PC
# (program counter) that holds the address (in the instruction store)
# of the next instruction to execute.

# The accumulator (having, say, 1024 locations) is used for most operations.
# Each accumulator location holds either an integer or a floating-point value.
# The content of memory location m in the memory array is denoted MEM(l);
# the memory is of some large size and m must be a non-negative int.
# Each memory location is one byte long.  Floats and integers are stored in 8 and 4 memory
# units, respectively.   IMEM(m...m+3) means the four memory locations m, m+1, m+2, m+3 treated
# as an integer.  FMEM(m...m+7) is similar for a floating-point number.


class EmulationError(Exception):
    '''
    Base class for all emulation errors
    '''

# mimic C's integer division
def c_div(a, b):
    result = abs(a) // abs(b)
    if a < 0 and b > 0 or a > 0 and b < 0:
        result = -result
    return result

def c_mod(a, b):
    return a - c_div(a, b) * b
 
class Emulator:

    def __init__(self, mem_size = 500000):
        # Labels on instructions and labels on data are put in the same dictionary.
        # The only instruction deal both label types is *DataD*.
        # It puts the position of either labeled instruction or data in the stack.
        self.labels = {}
        self.pc = 0
        self.instructions = []
        self.data_index = 0
        self.mem = bytearray(mem_size)
        self.stack = [] 

    def _assure_type(self, expected_type, value):
        # type is either int or float
        if type(value) != expected_type:
            raise EmulationError(f'Value {value} is not of type {expected_type}')

    def _assure_stack(self, n = 1):
        if len(self.stack) < n:
            raise EmulationError('Not enough operands on stack')

    def _assure_mem(self, address, size):
        if len(self.mem) < address + size or address < 0:
            raise EmulationError(f'Memory address {address} out of range')

    def _unary_op(self, op, expected_type=int, result_type=None):
        '[... a] -> [... op(a)]'
        self._assure_stack(1)
        a = self.stack[-1]
        if expected_type:
            self._assure_type(expected_type, a)
        result = op(a)
        if result_type:
            result = result_type(result)
        self.stack[-1] = result

    def _binary_op(self, op, expected_type=int):
        '[... a b] -> [... op(a, b)]'
        self._assure_stack(2)
        a = self.stack[-2]
        b = self.stack[-1]
        if expected_type:
            self._assure_type(expected_type, a)
            self._assure_type(expected_type, b)
        result = op(a, b)
        if expected_type:
            result = expected_type(result)
        # apply the changes to the stack
        self.stack[-2] = result
        self.stack.pop()

    def _newpc(self, label, predicate = None):
        if label not in self.labels:
            raise ValueError(f'Label "{label}" not found')
        to_jump = True
        if predicate:
            self._assure_stack(1)
            value = self.stack.pop()
            if not predicate(value):
                to_jump = False
        if to_jump:
            return self.labels[label]

    def _get_string(self, string_address):
        string_length = 0
        while self.mem[string_address+string_length] != 0:
            string_length += 1
        return self.mem[string_address:string_address+string_length].decode()

    def load(self, instructions):
        for line_number, instruction in instructions:
            self.preprocess(line_number, instruction)

    def preprocess(self, line_number, instruction):
        'handle initialization directives'
        match instruction[0].lower():
            case 'label':
                label = instruction[1]
                if label in self.labels:
                    raise EmulationError(f'symbol "{label}" defined multiple times.')
                self.labels[label] = len(self.instructions)
            case 'dlabel':
                label = instruction[1]
                if label in self.labels:
                    raise EmulationError(f'symbol "{label}" defined multiple times.')
                self.labels[label] = self.data_index
            case 'datac':
                value = int(instruction[1])
                assert len(self.mem) >= self.data_index + 1
                self.mem[self.data_index] = value & 0xFF
                self.data_index += 1
            case 'datai':
                value = int(instruction[1])
                assert len(self.mem) >= self.data_index + 4
                self.mem[self.data_index:self.data_index+4] = struct.pack('<i', value)
                self.data_index += 4
            case 'dataf':
                value = float(instruction[1])
                assert len(self.mem) >= self.data_index + 8
                self.mem[self.data_index:self.data_index+8] = struct.pack('<d', value)
                self.data_index += 8
            case 'dataz':
                size = int(instruction[1])
                assert len(self.mem) >= self.data_index + size
                self.data_index += size
            case 'datad':
                label = instruction[1]
                if label not in self.labels:
                    raise ValueError(f'label "{label}" not found')
                value = self.labels[label]
                assert len(self.mem) >= self.data_index + 4
                self.mem[self.data_index:self.data_index+4] = struct.pack('<i', value)
                self.data_index += 4
            case _: # default
                self.instructions.append((line_number, instruction))

    def execute(self):
        if self.pc >= len(self.instructions):
            return

        self.halted = False
        while not self.halted and 0 <= self.pc < len(self.instructions):
            line_number, instruction = self.instructions[self.pc]
            try:
                self.execute_instruction(instruction)
            except EmulationError as e:
                print(f'Simulation error on Instruction:\n\t#{self.pc}: {instruction} at line {line_number}\n* {e}')
                print(self.stack)
                return

    def execute_instruction(self, instruction):
        new_pc = None
        match instruction[0].lower():
            # ------------------------------
            # int arithmetic(int)->int
            # ------------------------------
            case 'add':
                self._binary_op(operator.add)
            case 'subtract':
                self._binary_op(operator.sub)
            case 'negate':
                self._unary_op(operator.neg)
            case 'multiply':
                self._binary_op(operator.mul)
            case 'divide':
                self._binary_op(c_div)
            case 'remainder':
                self._binary_op(c_mod)
            # ------------------------------
            # float arithmetic(float)->float
            # ------------------------------
            case 'fadd':
                self._binary_op(operator.add, float)
            case 'fsubtract':
                self._binary_op(operator.sub, float)
            case 'fnegate':
                self._unary_op(operator.neg, float)
            case 'fmultiply':
                self._binary_op(operator.mul, float)
            case 'fdivide':
                self._binary_op(operator.truediv, float)
            # ------------------------------
            # logical(int)->int
            # ------------------------------
            case 'and':
                self._binary_op(lambda a, b: a and b)
            case 'or':
                self._binary_op(lambda a, b: a or b)
            case 'xor':
                self._binary_op(lambda a, b: not b if a else b)
            case 'nand':
                self._binary_op(lambda a, b: not (a and b))
            case 'nor':
                self._binary_op(lambda a, b: not (a or b))
            case 'bequal':
                self._binary_op(lambda a, b: b if a else not b) # bool(a) == bool(b)
            case 'bnegate':
                self._unary_op(lambda a: not a, expected_type=int, result_type=int)
            # ------------------------------
            # bitwise(int)->int
            # ------------------------------
            case 'btand':
                self._binary_op(operator.and_)
            case 'btor':
                self._binary_op(operator.or_)
            case 'btxor':
                self._binary_op(operator.xor)
            case 'btnand':
                self._binary_op(lambda a, b: ~(a & b))
            case 'btnor':
                self._binary_op(lambda a, b: ~(a | b))
            case 'btequal':
                self._binary_op(lambda a, b: ~a ^ b)
            case 'btnegtate':
                self._unary_op(lambda a: ~a)
            # ------------------------------
            # type conversion
            # ------------------------------
            case 'convertf':
                self._unary_op(float, expected_type=int)
            case 'converti':
                self._unary_op(int, expected_type=float)
            # ------------------------------
            # stack
            # ------------------------------
            case 'duplicate':
                self._assure_stack()
                self.stack.append(self.stack[-1])
            case 'exchange':
                self._assure_stack(2)
                self.stack[-1], self.stack[-2] = self.stack[-2], self.stack[-1]
            case 'pop':
                self._assure_stack()
                self.stack.pop()
            case 'pushi':
                value = int(instruction[1])
                self.stack.append(value)
            case 'pushf':
                value = float(instruction[1])
                self.stack.append(value)
            case 'pushd':
                label = instruction[1]
                if not label in self.labels:
                    raise EmulationError(f'Label "{label}" not found')
                self.stack.append(self.labels[label])
            case 'pushpc':
                self.stack.append(self.pc + 1)
            case 'poppc' | 'return':
                self._assure_stack()
                new_pc = self.stack.pop()
            case 'loadc':
                self._assure_stack()
                address = self.stack[-1]
                self._assure_mem(address, 1)
                self.stack[-1] = self.mem[address]
            case 'loadi':
                self._assure_stack()
                address = self.stack[-1]
                self._assure_mem(address, 4)
                value = struct.unpack('<i', self.mem[address:address + 4])[0]
                self.stack[-1] = value
            case 'loadf':
                self._assure_stack()
                address = self.stack[-1]
                self._assure_mem(address, 8)
                value = struct.unpack('<d', self.mem[address:address + 8])[0]
                self.stack[-1] = value
            case 'storec':
                self._assure_stack(2)
                value = self.stack[-1]
                address = self.stack[-2]
                self._assure_mem(address, 1)
                self.mem[address] = value & 0xFF
                self.stack.pop()
                self.stack.pop()
            case 'storei':
                self._assure_stack(2)
                assert type(self.stack[-1]) is int, self.stack[-1]
                value = struct.pack('<i', self.stack[-1])
                address = self.stack[-2]
                self._assure_mem(address, 4)
                self.mem[address:address+4] = value
                self.stack.pop()
                self.stack.pop()
            case 'storef':
                self._assure_stack(2)
                value = struct.pack('<d', self.stack[-1])
                address = self.stack[-2]
                self._assure_mem(address, 8)
                self.mem[address:address+8] = value
                self.stack.pop()
                self.stack.pop()
            case 'memtop':
                self.stack.append(len(self.mem))
            # ------------------------------
            # control flow
            # ------------------------------
            case 'jump':
                label = instruction[1]
                new_pc = self._newpc(label)
            case 'jumpfalse' | 'jumpfzero':
                label = instruction[1]
                new_pc = self._newpc(label, lambda x: x == 0)
            case 'jumptrue':
                label = instruction[1]
                new_pc = self._newpc(label, lambda x: x != 0)
            case 'jumpneg' | 'jumpfneg':
                label = instruction[1]
                new_pc = self._newpc(label, lambda x: x < 0)
            case 'jumppos' | 'jumpfpos':
                label = instruction[1]
                new_pc = self._newpc(label, lambda x: x > 0)
            case 'call':
                label = instruction[1]
                new_pc = self._newpc(label)
                self.stack.append(self.pc + 1)
            case 'jumpv':
                self._assure_stack()
                new_pc = self.stack.pop()
            case 'callv':
                self._assure_stack()
                new_pc = self.stack.pop()
                self.stack.append(self.pc + 1)
            case 'halt':
                self.halted = True
            # ------------------------------
            # fancy opcodes
            # ------------------------------
            case 'printf':
                self._assure_stack()
                format_string = self._get_string(self.stack.pop())
                if '%s' in format_string:
                    self._assure_stack()
                    format_string = format_string.replace('%s', self._get_string(self.stack.pop()))
                if '%f' in format_string:
                    self._assure_stack()
                    value = self.stack.pop()
                    format_string = format_string.replace('%f', f'{value:.6f}')
                if '%d' in format_string:
                    self._assure_stack()
                    value = self.stack.pop()
                    format_string = format_string.replace('%d', str(value))
                if '%c' in format_string:
                    self._assure_stack()
                    value = self.stack.pop()
                    format_string = format_string.replace('%c', chr(value))
                print(format_string, end='')
            case 'pstack':
                print(self.stack)
            case 'nop':
                pass
            case _:
                raise NotImplementedError(instruction[0])

        self.pc = new_pc if new_pc is not None else self.pc + 1

def main(asm_file_path):
    emulator = Emulator()
    instructions = []
    for line_number, line in enumerate(open(asm_file_path, 'r').readlines(), start=1):
        instruction = [i.strip() for i in line.split()]
        if instruction and not instruction[0].startswith('#'):
            instructions.append((line_number, instruction))
    try:
        emulator.load(instructions)
    except EmulationError as e:
        logging.error(e)
        return 1
    emulator.execute()


if __name__ == '__main__':
    logging.basicConfig(level=logging.INFO, format='%(levelname)s - %(message)s')
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument('file')
    args = parser.parse_args()
    exit(main(args.file))
