# this program demonstrates a loop. It is equivalent to:
# 	bound = 22;
# 	for(int i=0; i<bound; i+=2) {
#	   	printf("%d", i);
#	   	printf("\n");
# 	}
#
PushD	bound					// [&bound]
PushI	22						// [&bound 22]
StoreI							// []
#
# loop header
PushD	index					// [&i]
PushI	0						// [&i 0]
PStack
StoreI							// []
Label	the-loop
#
# loop test
PushD	index					// [&i]
LoadI							// [i]
PushD	bound					// [i &bound]
LoadI							// [i bound]
Subtract						// [i-bound]
JumpNeg	stay-in-loop			// []
Jump	the-end					// []
Label	stay-in-loop			// []
#
# loop body
PushD	index					// [&i]
LoadI							// [i]
PushD	integer-format-string	// [ctr fmtStr]
Printf							// []
PushD	newline-string			// [nlStr]
Printf							// []
#
# loop end--increment
PushD	index					// [&i]
Duplicate						// [&i &i]
LoadI							// [&i i]
PushI	2						// [&i i 2]
Add								// [&i i+2]
StoreI							// []
Jump	the-loop				// []
#
# loop exit
Label	the-end					// []
Halt
DLabel	integer-format-string
DataC	37						// %
DataC	100						// d
DataC	0						// <null>
DLabel	newline-string
DataC	10						// \n
DataC	0						// <null>
DLabel	index
DataI	14
DLabel	bound
DataI	0
