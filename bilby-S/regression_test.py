import os
import pytest
import subprocess

PROJECT_ROOT_PATH = os.path.dirname(os.path.abspath(__file__))
TEST_DATA_ROOT_PATH = os.path.join(PROJECT_ROOT_PATH, 'input')

def get_tests(*test_group_names):
    tests = []
    if not test_group_names:
        test_groups = [entry for entry in os.scandir(TEST_DATA_ROOT_PATH) if entry.is_dir()]
    else:
        test_groups = [entry for entry in os.scandir(TEST_DATA_ROOT_PATH) if entry.name in test_group_names]
    for test_group in test_groups:
        for root, dirs, files in os.walk(test_group.path):
            for file in files:
                if file.endswith('.bilby'):
                    tests.append(
                        os.path.relpath(os.path.join(root, file), TEST_DATA_ROOT_PATH)
                        )
    tests.sort()
    return tests

def run_compiler(src_file_path):
    relative_path = os.path.relpath(src_file_path, TEST_DATA_ROOT_PATH)
    p = subprocess.run(
        [
            'java', '-ea',
            '-cp', os.path.join(PROJECT_ROOT_PATH, 'bin'),
            'applications.BilbyCompiler',
            src_file_path,
            os.path.join('output', os.path.dirname(relative_path)),
        ],
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE)
    return p.stdout.decode('utf-8'), p.stderr.decode('utf-8'), p.returncode

def run_emulator(asm_file_path):
    p = subprocess.run(
        [
            #os.path.join(PROJECT_ROOT_PATH, 'ASM_Emulator', 'ASMEmu.exe'),
            'python3', os.path.join(PROJECT_ROOT_PATH, 'emulator.py'),
            asm_file_path
        ],
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE)
    return p.stdout.decode('utf-8'), p.stderr.decode('utf-8'), p.returncode

@pytest.mark.parametrize('test_relative_path', get_tests())
def test_compile_and_emulate(test_relative_path):
    full_test_path = os.path.join(TEST_DATA_ROOT_PATH, test_relative_path)
    print('Running test: {}'.format(test_relative_path))
    test_file_name = os.path.basename(test_relative_path)
    test_dir_path = os.path.dirname(full_test_path)
    # compiler
    compiler_out, compiler_err, retcode = run_compiler(full_test_path)
    if test_file_name.startswith('err_'):
        assert retcode != 0
        assert 'Exception in thread' not in compiler_err
    else:
        # emulator
        assert retcode == 0, 'Compiler error:\n{}'.format(compiler_err)
        assert compiler_out == ''
        compiler_output_file_path = os.path.join('output', test_relative_path.replace('.bilby', '.asm'))
        compiler_output = open(compiler_output_file_path, 'r').read()
        assert 'Runtime error' not in compiler_err
        # asm output varies frequently whenever we have runtime changes. So we don't check it.
        #expected_asm_file_path = os.path.join(test_dir_path, 'output', test_file_name.replace('.bilby', '.asm'))
        #expected_asm = open(expected_asm_file_path).read()
        #assert compiler_output == expected_asm

        emulator_out, emulator_err, emulator_retcode = run_emulator(compiler_output_file_path)
        assert emulator_retcode == 0, emulator_err
        open(os.path.join('output', test_relative_path.replace('.bilby', '.txt')), 'wb').write(emulator_out.encode('utf-8'))
        expected_output_file_path = os.path.join(test_dir_path, test_file_name.replace('.bilby', '.txt'))
        expected_output = open(expected_output_file_path).read()
        assert emulator_out.splitlines() == expected_output.splitlines()
        if test_file_name.startswith('rte_'):
            assert 'Runtime error' in emulator_out
