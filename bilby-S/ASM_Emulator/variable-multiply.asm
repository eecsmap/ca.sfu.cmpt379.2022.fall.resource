PushD	argument1				// [] -> [&arg1]
LoadI							// [&arg1] -> [arg1]
PushD	argument2				// [arg1] -> [arg1 &arg2]
LoadI							// [arg1 &arg2] -> [arg1 arg2]
Multiply						// [a1 a2] -> [a1*a2]
PushD	integer-format-string	// [val] -> [val fmtStr]  (where val = a1*a2)
Printf							// [val fmtStr] -> []
PushD	newline-string			// [] -> [nlStr]
Printf							// [nlStr] -> []
Halt
DLabel integer-format-string
DataC	37						// %
DataC	100						// d
DataC	0						// <null>
DLabel newline-string
DataC	10						// \n
DataC	0						// <null>
DLabel argument1
DataI	13
DLabel argument2
DataI	14
