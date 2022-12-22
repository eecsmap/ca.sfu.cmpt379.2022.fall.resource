use strict;
use warnings;
use Cwd;
use File::Find;
use File::Basename;

my $project_location = "C:/Users/Tom/workspaceMars";
my $language_name = "bilby";
my $project_name = "bilbyStudents2021";
my $asm_test_extension = "asmt";

my $cap_language_name = ucfirst($language_name);
my $emulator = "$project_location/$project_name/ASM_Emulator/ASMEmu.exe";
my $javaArgs = "-ea -cp $project_location/$project_name/bin";
my $compiler_class = "applications.$cap_language_name"."Compiler";
#my $verifier_class = "applications.ASMTestVerifier";
my $suffixLength = length($language_name);

#print "suffixLength = $suffixLength \n";
#print "emulator = $emulator \n";
#print "cap_language_name = $cap_language_name \n";
#print "main_class = $main_class \n";

#die "die: this script is being tested.  \n";

my $in_file = $ARGV[0];				# 1st arg is file to check
my $forcing = 0;

if($ARGV[0] eq "-f") {
	$in_file = $ARGV[1];
	$forcing = 1;
}


my $failuresHeader = "failures:\n";
my $failures = $failuresHeader;

find(\&processFile, $in_file);

if($failures eq $failuresHeader) {
	$failures = "all tests passed\n";
}
print $failures;


	
sub processFile {
#	print "processing file  $File::Find::name \n";
	
	if (/good_.*.$language_name/) {
#		print "case 1: good_ \n";
		compileAndExpectResult(0);				# compiler returns 0 if successful
		runExpectingOutput();
	}			
	elsif (/rte_.*.$language_name/) {
#		print "case 2: rte_ \n";
		compileAndExpectResult(0);				# compiler returns 0 if successful
		runExpectingRuntimeError();	
	}
	elsif (/err_.*.$language_name/) {
#		print "case 3: err_ \n";
		compileAndExpectResult(1 * 256);		# compiler returns 1 on failure, perl gets it as 256.
	}
	elsif (/.*.$language_name/) {				# any (language) file not following naming conventions
#		print "case 4: default \n";
		compileAndExpectResult(0);				# (same as good_.*.$language_name)
		runExpectingOutput();
	}

#	print "done \n";
}

sub compileAndExpectResult {
	my ($expectedResult) = @_;			# either 0 or 256, for good or error result.
	my $fullPath = $File::Find::name;
	my $result = chdirAndJavaRun($compiler_class, $fullPath);
#	print "compileAndExpectResult result: "."$result\n";
	if($result != $expectedResult) {
		print "compilation yielded $result but expected $expectedResult\n";
		$failures = $failures.$fullPath."\n";	
	}
}
sub runExpectingOutput {
	my $asmFile = compiledASMFilename();
	my $emulatorOutput = `$emulator $asmFile`;
	my $outFile = emulatorOutputFilename();

	if(-e $outFile  && ($forcing == 0)) {
#		print "outfile exists \n";
  		local $/=undef;
  		my $reference = readFromFile($outFile);
 		
 		if($reference ne $emulatorOutput) {
 			$failures = $failures.$File::Find::name." (doesn't match regression output)\n";
 		}
	}
	else {
#		print "outfile doesn't exist (or we're forcing a rewrite) \n";
 		printToFile($outFile, $emulatorOutput);
		print $emulatorOutput;
	}
} 
sub runExpectingRuntimeError {
	my $asmFile = compiledASMFilename();
	my $emulatorOutput = `$emulator $asmFile`;
	
	print $emulatorOutput;		#reporting
	if($emulatorOutput !~ /.*Runtime error.*/) {
#		print "RTE not issued: $asmFile\n";
		print $emulatorOutput;
		$failures = $failures.$File::Find::name." (no runtime error)\n";	
	}
}




###############################################
# filesystem interaction
###############################################

sub readFromFile {
	my ($outFile) = @_;
	open FILE, $outFile or die "Couldn't open file: $!";
  	binmode FILE;
  	my $contents = <FILE>;
 	close FILE;
 	return $contents;
}
sub printToFile {
	my ($outFile, $testOutput) = @_;
	ensureContainingDirectoryExists($outFile);
	
	open FILE, ">", $outFile or die "Couldn't open file: $!";
	binmode FILE;
	print FILE $testOutput;
	close FILE; 
}
sub ensureContainingDirectoryExists {
	my ($outFile) = @_;
	my $outDir = dirname($outFile);
	print "outDir: $outDir \n";
	mkdir $outDir unless -d $outDir;
}


###############################################
# java execution
###############################################

sub chdirAndJavaRun {
	my ($jmain_class, $fullPath) = @_;
	my $directory = dirname($fullPath);
	chdir($directory);
	my $file = basename($fullPath);
	my $result = javaRun($jmain_class, $file);
#	print "chdirAndJavaRun result: "."$result\n";
	return $result;
}
sub javaRun {
	my ($jmain_class, $in_filename) = @_;
	my $java = "java";
	my $javaCmd = "$java $javaArgs $jmain_class $in_filename" ;
#	print $javaCmd."\n";
	my $result = system("$javaCmd");
#	print "javaRun result: "."$result\n";
	return $result;
}


###############################################
# file and directory names
###############################################

sub emulatorOutputDir {
	my $fullPath = $File::Find::name;
	my $directory = dirname($fullPath);
	return "$directory/expected";	
}
sub emulatorOutputFilename {
	my $fullPath = $File::Find::name;
	my $fileA = basename($fullPath);
	my $fileBase = substr( $fileA, 0, -($suffixLength+1));
	return emulatorOutputDir() . "/$fileBase.txt";	
}
sub compiledASMFilename {
	my $fullPath = $File::Find::name;
	my $directory = dirname($fullPath);
	my $fileA = basename($fullPath);
	my $fileBase = substr( $fileA, 0,  -($suffixLength+1));
#	print "fileBase = $fileBase \n";
	return "$directory/output/$fileBase.asm";	
}
sub optimizedASMFilename {
	my $fullPath = $File::Find::name;
	return"$fullPath"."o";
}
