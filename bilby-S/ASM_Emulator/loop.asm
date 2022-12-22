Label	the-loop
PushD	loop-counter			// [&ctr]
LoadI							// [ctr]
JumpFalse	the-end				// []
PushD	loop-counter			// [&ctr]
LoadI							// [ctr]
PushI	1						// [ctr 1]
Subtract						// [ctr-1]
PushD	loop-counter			// [ctr-1 &ctr]
Exchange						// [&ctr ctr-1]
StoreI							// []
PushD	loop-counter			// [&ctr]
LoadI							// [ctr]	(now the new value of the counter)
PushD	integer-format-string	// [ctr fmtStr]
Printf							// []
PushD	newline-string			// [nlStr]
Printf							// []
Jump	the-loop				// []
Label	the-end					// arrive with accumulator: []
Halt
DLabel	integer-format-string
DataC	37						// %
DataC	100						// d
DataC	0						// <null>
DLabel	newline-string
DataC	10						// \n
DataC	0						// <null>
DLabel	loop-counter
DataI	14
