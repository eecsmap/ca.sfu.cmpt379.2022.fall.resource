package asmCodeGenerator.runtime;

import static asmCodeGenerator.Macros.*;
import static asmCodeGenerator.codeStorage.ASMCodeFragment.CodeType.*;
import static asmCodeGenerator.codeStorage.ASMOpcode.*;
import asmCodeGenerator.Labeller;
import asmCodeGenerator.codeStorage.ASMCodeFragment;

public class MemoryManager {
	// Debug Mode. DEBUGGING Adds debug code and executes insertDebugMain when the program is initialized.
	// Debug Mode. DEBUGGING2 does not insertDebugMain, but prints allocation diagnostics.
	private static final boolean DEBUGGING = false;
	private static final boolean DEBUGGING2 = false;		// does not insertDebugMain
	
	// ASM Subroutines.  User/Compiler-writer needs only ALLOCATE and DEALLOCATE
	private static final String MEM_MANAGER_INITIALIZE =   "-mem-manager-initialize";
	private static final String MEM_MANAGER_MAKE_TAGS =    "-mem-manager-make-tags";
	private static final String MEM_MANAGER_MAKE_ONE_TAG = "-mem-manager-one-tag";
	public  static final String MEM_MANAGER_ALLOCATE =     "-mem-manager-allocate";
	public  static final String MEM_MANAGER_DEALLOCATE =   "-mem-manager-deallocate";
	private static final String MEM_MANAGER_REMOVE_BLOCK = "-mem-manager-remove-block";
	
	// Main memory manager variables.
	private static final String MEM_MANAGER_HEAP_START_PTR =   "$heap-start-ptr";
	private static final String MEM_MANAGER_HEAP_END_PTR =     "$heap-after-ptr";
	private static final String MEM_MANAGER_FIRST_FREE_BLOCK = "$heap-first-free";
	private static final String MEM_MANAGER_HEAP =             "$heap-memory";
	
	// locals for MAKE_TAGS
	private static final String MMGR_BLOCK_RETURN_ADDRESS = "$mmgr-tags-return";
	private static final String MMGR_BLOCK_START =     		"$mmgr-tags-start";
	private static final String MMGR_BLOCK_SIZE =      		"$mmgr-tags-size";
	private static final String MMGR_BLOCK_PREVPTR =   		"$mmgr-tags-prevptr";
	private static final String MMGR_BLOCK_NEXTPTR =   		"$mmgr-tags-nextptr";
	private static final String MMGR_BLOCK_AVAILABLE = 		"$mmgr-tags-available";
	
	// locals for ONE_TAG
	private static final String MMGR_ONETAG_RETURN_ADDRESS = "$mmgr-onetag-return";
	private static final String MMGR_ONETAG_LOCATION =  	 "$mmgr-onetag-location";
	private static final String MMGR_ONETAG_AVAILABLE = 	 "$mmgr-onetag-available";
	private static final String MMGR_ONETAG_SIZE =      	 "$mmgr-onetag-size";
	private static final String MMGR_ONETAG_POINTER =   	 "$mmgr-onetag-pointer";
	
	// locals and branch targets for ALLOCATE
	private static final String MMGR_ALLOC_RETURN_ADDRESS = 	"$mmgr-alloc-return";
	private static final String MMGR_ALLOC_SIZE = 				"$mmgr-alloc-size";
	private static final String MMGR_ALLOC_CURRENT_BLOCK =  	"$mmgr-alloc-current-block";
	private static final String MMGR_ALLOC_REMAINDER_BLOCK =	"$mmgr-alloc-remainder-block";
	private static final String MMGR_ALLOC_REMAINDER_SIZE = 	"$mmgr-alloc-remainder-size";
	private static final String MMGR_ALLOC_FOUND_BLOCK =		"-mmgr-alloc-found-block";
	private static final String MMGR_ALLOC_PROCESS_CURRENT = 	"-mmgr-alloc-process-current";
	private static final String MMGR_ALLOC_TEST_BLOCK =  		"-mmgr-alloc-test-block";
	private static final String MMGR_ALLOC_NO_BLOCK_WORKS = 	"-mmgr-alloc-no-block-works";
	private static final String MMGR_ALLOC_RETURN_USERBLOCK =	"-mmgr-alloc-return-userblock";
	
	// locals and branch targets for DEALLOCATE	
	private static final String MMGR_DEALLOC_RETURN_ADDRESS = 	"$mmgr-dealloc-return";
	private static final String MMGR_DEALLOC_BLOCK = 			"$mmgr-dealloc-block";

	// locals and branch targets for REMOVE_BLOCK
	private static final String MMGR_REMOVE_RETURN_ADDRESS = 	"$mmgr-remove-return";
	private static final String MMGR_REMOVE_BLOCK = 			"$mmgr-remove-block";
	private static final String MMGR_REMOVE_PREV = 				"$mmgr-remove-prev";
	private static final String MMGR_REMOVE_NEXT = 				"$mmgr-remove-next";
	private static final String MMGR_REMOVE_PROCESS_PREV = 		"-mmgr-remove-process-prev";
	private static final String MMGR_REMOVE_NO_PREV =			"-mmgr-remove-no-prev";
	private static final String MMGR_REMOVE_PROCESS_NEXT =		"-mmgr-remove-process-next";
	private static final String MMGR_REMOVE_DONE = 				"-mmgr-remove-done";
	
	// variables used by a macro (method newBlock) but could be shared by all instances of the macro
	// (although currently there is only one instance.)  allocated in initialization.
	private static final String MMGR_NEWBLOCK_BLOCK = "$mmgr-newblock-block";
	private static final String MMGR_NEWBLOCK_SIZE =  "$mmgr-newblock-size";

	// a tag is:
	//		prev/next ptr:	4 bytes
	//		size:			4 bytes
	//		isAvailable:	1 byte	
	private static final int MMGR_TAG_SIZE_IN_BYTES = 9;
	private static final int MMGR_TWICE_TAG_SIZE = 2 * MMGR_TAG_SIZE_IN_BYTES;
	private static final int TAG_POINTER_OFFSET = 0;
	private static final int TAG_SIZE_OFFSET = 4;
	private static final int TAG_AVAIL_OFFSET = 8;
	
	// the only tunable parameter.
	private static final int MEM_MANAGER_WASTE_TOLERANCE = MMGR_TWICE_TAG_SIZE + 8;

	

	// this code should reside on the executable pathway before the application.
	public static ASMCodeFragment codeForInitialization() {
		ASMCodeFragment frag = new ASMCodeFragment(GENERATES_VOID);
		frag.add(Label, MEM_MANAGER_INITIALIZE);
		
		declareI(frag, MEM_MANAGER_HEAP_START_PTR);	// declare variables
		declareI(frag, MEM_MANAGER_HEAP_END_PTR);	
		declareI(frag, MEM_MANAGER_FIRST_FREE_BLOCK);
		
		declareI(frag, MMGR_NEWBLOCK_BLOCK);
		declareI(frag, MMGR_NEWBLOCK_SIZE);
		
		frag.add(PushD, MEM_MANAGER_HEAP);				// set heapStart and heapEnd
		frag.add(Duplicate);
		storeITo(frag, MEM_MANAGER_HEAP_START_PTR);
		storeITo(frag, MEM_MANAGER_HEAP_END_PTR);
		
		frag.add(PushI, 0);								// no blocks allocated.
		storeITo(frag, MEM_MANAGER_FIRST_FREE_BLOCK);

		if(DEBUGGING) {
			insertDebugMain(frag);
		}
		
		return frag;
	}


	// this goes after the main program, so that MEM_MANAGER_HEAP is after all other variable declarations.
	public static ASMCodeFragment codeForAfterApplication() {
		ASMCodeFragment frag = new ASMCodeFragment(GENERATES_VOID);	
		
		frag.append(subroutineMakeTags());
		frag.append(subroutineMakeOneTag());
		frag.append(subroutineAllocate());
		frag.append(subroutineDeallocate());
		frag.append(subroutineRemoveBlock());
		if(DEBUGGING) {
			frag.append(subroutineDebugPrintBlock());
			frag.append(subroutineDebugPrintFreeList());
		}
		
		frag.add(DLabel, MEM_MANAGER_HEAP);	
		
		return frag;
	}
	
	
	
	private static ASMCodeFragment subroutineMakeTags() {
		ASMCodeFragment frag = new ASMCodeFragment(GENERATES_VOID);
		frag.add(Label, MEM_MANAGER_MAKE_TAGS);		// [...prevPtr nextPtr isAvail start size (return)]
													// size must include the tags.

		declareI(frag, MMGR_BLOCK_SIZE);
		declareI(frag, MMGR_BLOCK_START);
		declareI(frag, MMGR_BLOCK_AVAILABLE);
		declareI(frag, MMGR_BLOCK_NEXTPTR);
		declareI(frag, MMGR_BLOCK_PREVPTR);
		declareI(frag, MMGR_BLOCK_RETURN_ADDRESS);
		
		
		// store the params and return address
		storeITo(frag, MMGR_BLOCK_RETURN_ADDRESS); // [... prevPtr nextPtr isAvail start size]
		storeITo(frag, MMGR_BLOCK_SIZE);		// [... prevPtr nextPtr isAvail start]
		storeITo(frag, MMGR_BLOCK_START);		// [... prevPtr nextPtr isAvail]
		storeITo(frag, MMGR_BLOCK_AVAILABLE);	// [... prevPtr nextPtr]
		storeITo(frag, MMGR_BLOCK_NEXTPTR);		// [... prevPtr]
		storeITo(frag, MMGR_BLOCK_PREVPTR);		// [... ]
		
		// make the start tag
		loadIFrom(frag, MMGR_BLOCK_PREVPTR);		// [... prevPtr]
		loadIFrom(frag, MMGR_BLOCK_SIZE);		// [... prevPtr size]
		loadIFrom(frag, MMGR_BLOCK_AVAILABLE);	// [... prevPtr size isAvail]
		loadIFrom(frag, MMGR_BLOCK_START);		// [... prevPtr size isAvail tagLocation]
		frag.add(Call,  MEM_MANAGER_MAKE_ONE_TAG);			
		
		// make the end tag
		loadIFrom(frag, MMGR_BLOCK_NEXTPTR);		// [... nextPtr]
		loadIFrom(frag, MMGR_BLOCK_SIZE);		// [... nextPtr size]
		loadIFrom(frag, MMGR_BLOCK_AVAILABLE);	// [... nextPtr size isAvail]
		loadIFrom(frag, MMGR_BLOCK_START);		// [... nextPtr size isAvail start]
		tailTag(frag);							// [... nextPtr size isAvail tailTagLocation]
		frag.add(Call,  MEM_MANAGER_MAKE_ONE_TAG);	
		
		loadIFrom(frag, MMGR_BLOCK_RETURN_ADDRESS);
		frag.add(Return);
		
		return frag;
	}
	
	private static ASMCodeFragment subroutineMakeOneTag() {
		ASMCodeFragment frag = new ASMCodeFragment(GENERATES_VOID);
		frag.add(Label, MEM_MANAGER_MAKE_ONE_TAG);		// [... ptr size isAvail location (return)]

		declareI(frag, MMGR_ONETAG_RETURN_ADDRESS);
		declareI(frag, MMGR_ONETAG_LOCATION);
		declareI(frag, MMGR_ONETAG_AVAILABLE);
		declareI(frag, MMGR_ONETAG_SIZE);
		declareI(frag, MMGR_ONETAG_POINTER);

		// store the params (except ptr) and return address
		storeITo(frag, MMGR_ONETAG_RETURN_ADDRESS); // [... ptr size isAvail location]
		storeITo(frag, MMGR_ONETAG_LOCATION);		// [... ptr size isAvail]
		storeITo(frag, MMGR_ONETAG_AVAILABLE); 		// [... ptr size]
		storeITo(frag, MMGR_ONETAG_SIZE); 			// [... ptr]

		loadIFrom(frag, MMGR_ONETAG_LOCATION);		// [.. ptr location]
		writeTagPointer(frag);

		loadIFrom(frag, MMGR_ONETAG_SIZE);			// [.. size]
		loadIFrom(frag, MMGR_ONETAG_LOCATION);		// [.. size location]
		writeTagSize(frag);

		loadIFrom(frag, MMGR_ONETAG_AVAILABLE);		// [.. isAvail]
		loadIFrom(frag, MMGR_ONETAG_LOCATION);		// [.. isAvail location]
		writeTagAvailable(frag);
		
		
		loadIFrom(frag, MMGR_ONETAG_RETURN_ADDRESS);
		frag.add(Return);
		
		return frag;
	}	

	private static ASMCodeFragment subroutineAllocate() {
		ASMCodeFragment frag = new ASMCodeFragment(GENERATES_VOID);
		frag.add(Label, MEM_MANAGER_ALLOCATE);		// [... usableSize (return)]

		declareI(frag, MMGR_ALLOC_RETURN_ADDRESS);
		declareI(frag, MMGR_ALLOC_SIZE);
		declareI(frag, MMGR_ALLOC_CURRENT_BLOCK);
		declareI(frag, MMGR_ALLOC_REMAINDER_BLOCK);
		declareI(frag, MMGR_ALLOC_REMAINDER_SIZE);
		

		//store return addr
		storeITo(frag, MMGR_ALLOC_RETURN_ADDRESS);	// [... usableSize]
		
		if(DEBUGGING2) {
			printAccumulatorTop(frag, "--allocate %d bytes\n");
		}

		//convert user size to mmgr size and store
		frag.add(PushI, MMGR_TWICE_TAG_SIZE);			// [... usableSize 2*tagsize]
		frag.add(Add);									// [... size]
		storeITo(frag, MMGR_ALLOC_SIZE);				// [...]

	
		//initialize current block
			loadIFrom(frag, MEM_MANAGER_FIRST_FREE_BLOCK);
			storeITo(frag, MMGR_ALLOC_CURRENT_BLOCK);

		// if (curblock == null) goto NO_BLOCK_WORKS
		frag.add(Label, MMGR_ALLOC_PROCESS_CURRENT);
			loadIFrom(frag, MMGR_ALLOC_CURRENT_BLOCK);
			frag.add(JumpFalse, MMGR_ALLOC_NO_BLOCK_WORKS);

		// if (curblock.size >= allocsize) goto FOUND_BLOCK		
		frag.add(Label, MMGR_ALLOC_TEST_BLOCK);
			loadIFrom(frag, MMGR_ALLOC_CURRENT_BLOCK);		// [... block]
			if(DEBUGGING2) {
				printAccumulatorTop(frag, "--testing block %d\n");
			}
			readTagSize(frag);								// [... block.size]
			loadIFrom(frag, MMGR_ALLOC_SIZE);				// [... block.size allocSize]
			frag.add(Subtract);								// [... block.size-allocSize]
			frag.add(PushI, 1);								// [... block.size-allocSize 1]
			frag.add(Add);									// [... block.size-allocSize+1]
			frag.add(JumpPos, MMGR_ALLOC_FOUND_BLOCK);


			// curblock = curblock.nextptr
			loadIFrom(frag, MMGR_ALLOC_CURRENT_BLOCK);
			tailTag(frag);
			readTagPointer(frag);
			storeITo(frag, MMGR_ALLOC_CURRENT_BLOCK);


		// loop to NEXT_BLOCK
		frag.add(Jump, MMGR_ALLOC_PROCESS_CURRENT);
		
		
		frag.add(Label, MMGR_ALLOC_FOUND_BLOCK);
			// remove block from free list
			loadIFrom(frag, MMGR_ALLOC_CURRENT_BLOCK);
			frag.add(Call, MEM_MANAGER_REMOVE_BLOCK);
			
			// if (not wasting much memory) use this block as is
			loadIFrom(frag, MMGR_ALLOC_CURRENT_BLOCK);		// [... block]
			readTagSize(frag);								// [... block.size]
			loadIFrom(frag, MMGR_ALLOC_SIZE);				// [... block.size allocSize]
			frag.add(Subtract);								// [... waste]
			frag.add(PushI, MEM_MANAGER_WASTE_TOLERANCE);	// [... waste tolerance]
			frag.add(Subtract);								// [... over-tolerance-amt]
			frag.add(JumpNeg, MMGR_ALLOC_RETURN_USERBLOCK);
			

			// make two blocks from current block
			loadIFrom(frag, MMGR_ALLOC_CURRENT_BLOCK);		// [... block]
			loadIFrom(frag, MMGR_ALLOC_SIZE);				// [... block size]
			frag.add(Add);									// [... remainderBlock]
			storeITo(frag, MMGR_ALLOC_REMAINDER_BLOCK);		// [...]

			loadIFrom(frag, MMGR_ALLOC_SIZE);				// [... size]
			loadIFrom(frag, MMGR_ALLOC_CURRENT_BLOCK);		// [... size block]
			readTagSize(frag);								// [... size block.size]
			frag.add(Exchange);								// [... block.size size]
			frag.add(Subtract);								// [... leftoverbytes]
			storeITo(frag, MMGR_ALLOC_REMAINDER_SIZE);		// [...]
			
//			debugPrintI(frag, "alloc-current-block:   ", MMGR_ALLOC_CURRENT_BLOCK);
//			debugPrintI(frag, "alloc-current-size:    ", MMGR_ALLOC_SIZE);
//			debugPrintI(frag, "alloc-remainder-block: ", MMGR_ALLOC_REMAINDER_BLOCK);
//			debugPrintI(frag, "alloc-remainder-size:  ", MMGR_ALLOC_REMAINDER_SIZE);
			
			// make the tags for first new block.
			frag.add(PushI, 0);								// prevPtr
			frag.add(PushI, 0);								// nextPtr
			frag.add(PushI, 0);								// isAvailable
			loadIFrom(frag, MMGR_ALLOC_CURRENT_BLOCK);		// start_addr
			loadIFrom(frag, MMGR_ALLOC_SIZE);				// size of block
			frag.add(Call, MEM_MANAGER_MAKE_TAGS);	
			
			// make the tags for remainder block.
			frag.add(PushI, 0);								// prevPtr
			frag.add(PushI, 0);								// nextPtr
			frag.add(PushI, 1);								// isAvailable
			loadIFrom(frag, MMGR_ALLOC_REMAINDER_BLOCK);	// start_addr
			loadIFrom(frag, MMGR_ALLOC_REMAINDER_SIZE);		// size of block
			frag.add(Call, MEM_MANAGER_MAKE_TAGS);	
			
			// insert remainder block into free block list
			loadIFrom(frag, MMGR_ALLOC_REMAINDER_BLOCK);
			frag.add(PushI, MMGR_TAG_SIZE_IN_BYTES);
			frag.add(Add);
			frag.add(Call, MEM_MANAGER_DEALLOCATE);
			
			// currentBlock is now usable.
			frag.add(Jump, MMGR_ALLOC_RETURN_USERBLOCK);
		
		
		
		frag.add(Label, MMGR_ALLOC_NO_BLOCK_WORKS);
			if(DEBUGGING2) {
				printString(frag, "--NO BLOCK WORKS\n");
			}
			loadIFrom(frag, MMGR_ALLOC_SIZE);			// [... size]
//			debugPrintI(frag, "alloc ", MEM_MANAGER_HEAP_END_PTR);
			newBlock(frag);								// [... block]
//			debugPrintI(frag, "alloc ", MEM_MANAGER_HEAP_END_PTR);
			storeITo(frag, MMGR_ALLOC_CURRENT_BLOCK);
		
		// [... ] -> [... userBlock] & return
		frag.add(Label, MMGR_ALLOC_RETURN_USERBLOCK);
			loadIFrom(frag, MMGR_ALLOC_CURRENT_BLOCK);	// [... block]
			frag.add(PushI, MMGR_TAG_SIZE_IN_BYTES);	// [... block tagsize]
			frag.add(Add);								// [... userBlock]

			loadIFrom(frag, MMGR_ALLOC_RETURN_ADDRESS);
			frag.add(Return);
			
		return frag;
	}



	// [... block] -> [...]
	// pre: block is in Free Block List.
	private static ASMCodeFragment subroutineRemoveBlock() {
		ASMCodeFragment frag = new ASMCodeFragment(GENERATES_VOID);
		frag.add(Label, MEM_MANAGER_REMOVE_BLOCK);		// [... block (return)]

		declareI(frag, MMGR_REMOVE_RETURN_ADDRESS);
		declareI(frag, MMGR_REMOVE_BLOCK);
		declareI(frag, MMGR_REMOVE_PREV);
		declareI(frag, MMGR_REMOVE_NEXT);
		
		storeITo(frag, MMGR_REMOVE_RETURN_ADDRESS);		// [... block]
		storeITo(frag, MMGR_REMOVE_BLOCK);				// [... ]
		
		// get prev and next
		loadIFrom(frag, MMGR_REMOVE_BLOCK);		// [... block]
		readTagPointer(frag);					// [... prev]
		storeITo(frag, MMGR_REMOVE_PREV);
		
		loadIFrom(frag, MMGR_REMOVE_BLOCK);		// [... block]
		tailTag(frag);							// [... blockTail]
		readTagPointer(frag);					// [... next]
		storeITo(frag, MMGR_REMOVE_NEXT);
		
		
		//set prev block's ptr
		frag.add(Label, MMGR_REMOVE_PROCESS_PREV);
			loadIFrom(frag, MMGR_REMOVE_PREV);
			frag.add(JumpFalse, MMGR_REMOVE_NO_PREV);
			
			// prev.nextPtr = next
			loadIFrom(frag, MMGR_REMOVE_NEXT);			// [... next]
			loadIFrom(frag, MMGR_REMOVE_PREV);			// [... next prev]
			tailTag(frag);								// [... next prevTail]
			writeTagPointer(frag);						// [...]
			frag.add(Jump, MMGR_REMOVE_PROCESS_NEXT);

		frag.add(Label, MMGR_REMOVE_NO_PREV);
			loadIFrom(frag, MMGR_REMOVE_NEXT);
			storeITo(frag, MEM_MANAGER_FIRST_FREE_BLOCK);

		//set next block's ptr
		frag.add(Label, MMGR_REMOVE_PROCESS_NEXT);
			loadIFrom(frag, MMGR_REMOVE_NEXT);
			frag.add(JumpFalse, MMGR_REMOVE_DONE);
			
			// next.prevPtr = prev
			loadIFrom(frag, MMGR_REMOVE_PREV);			// [... prev]
			loadIFrom(frag, MMGR_REMOVE_NEXT);			// [... prev next]
			writeTagPointer(frag);						// [...]

		frag.add(Label, MMGR_REMOVE_DONE);		
			loadIFrom(frag, MMGR_REMOVE_RETURN_ADDRESS);
			frag.add(Return);
		
		return frag;
	}

	// [... usableBlockPtr (return)]
	private static ASMCodeFragment subroutineDeallocate() {
		ASMCodeFragment frag = new ASMCodeFragment(GENERATES_VOID);
		frag.add(Label, MEM_MANAGER_DEALLOCATE);		// [... blockptr (return)]
		
		declareI(frag, MMGR_DEALLOC_RETURN_ADDRESS);
		declareI(frag, MMGR_DEALLOC_BLOCK);
		
		//store return addr
		storeITo(frag, MMGR_DEALLOC_RETURN_ADDRESS);	// [... usableBlock]
		
		// convert user block to mmgr block
		frag.add(PushI, MMGR_TAG_SIZE_IN_BYTES);		// [... usableBlock tagsize]
		frag.add(Subtract);								// [... block]
		storeITo(frag, MMGR_DEALLOC_BLOCK);				// [...]
		
		// if(firstFree != 0) { firstFree.prev = block }
		String bypassLabel = "-mmgr-bypass-firstFree";
		loadIFrom(frag, MEM_MANAGER_FIRST_FREE_BLOCK);
		frag.add(JumpFalse, bypassLabel);
		loadIFrom(frag, MMGR_DEALLOC_BLOCK);
		loadIFrom(frag, MEM_MANAGER_FIRST_FREE_BLOCK);	// [... block firstFree]
		writeTagPointer(frag);		
		frag.add(Label, bypassLabel);

		// block.prev = 0
		frag.add(PushI, 0);
		loadIFrom(frag, MMGR_DEALLOC_BLOCK);	// [... 0 block]
		writeTagPointer(frag);
		
		// block.next = firstFree
		loadIFrom(frag, MEM_MANAGER_FIRST_FREE_BLOCK);	// [... firstFree]
		loadIFrom(frag, MMGR_DEALLOC_BLOCK);			// [... firstFree block]
		tailTag(frag);								// [... firstFree blockTail]
		writeTagPointer(frag);
				
		// block.avail1 = 1;
		frag.add(PushI, 1);						// [... 1]
		loadIFrom(frag, MMGR_DEALLOC_BLOCK);	// [... 1 block]
		writeTagAvailable(frag);

		// block.avail2 = 1;
		frag.add(PushI, 1);						// [... 1]
		loadIFrom(frag, MMGR_DEALLOC_BLOCK);	// [... 1 block]
		tailTag(frag);						    // [... 1 blockTail]
		writeTagAvailable(frag);

		// firstFree = block
		loadIFrom(frag, MMGR_DEALLOC_BLOCK);
		storeITo(frag, MEM_MANAGER_FIRST_FREE_BLOCK);
		
		// return
		loadIFrom(frag, MMGR_DEALLOC_RETURN_ADDRESS);
		frag.add(Return);
		return frag;
	}
	
////////////////////////////////////////////////////////////////////////////////////
//Macros: these get inlined into the subroutines defined above.
////////////////////////////////////////////////////////////////////////////////////

	// [... size] -> [... block]
	// eats new heap space.  allocates a block of given size.
	// does not insert into available list or set availability.
	private static void newBlock(ASMCodeFragment frag) {
		storeITo(frag, MMGR_NEWBLOCK_SIZE);
		// block = heapEnd
		loadIFrom(frag, MEM_MANAGER_HEAP_END_PTR);
		storeITo(frag, MMGR_NEWBLOCK_BLOCK);

		// heapEnd += size
		loadIFrom(frag, MMGR_NEWBLOCK_SIZE);
		addITo(frag, MEM_MANAGER_HEAP_END_PTR);

		// make the tags for our new block.
		frag.add(PushI, 0);								// prevPtr
		frag.add(PushI, 0);								// nextPtr
		frag.add(PushI, 0);								// isAvailable
		loadIFrom(frag, MMGR_NEWBLOCK_BLOCK);			// start_addr
		loadIFrom(frag, MMGR_NEWBLOCK_SIZE);			// size of block
		frag.add(Call, MEM_MANAGER_MAKE_TAGS);			

		// return with block on the stack
		loadIFrom(frag, MMGR_NEWBLOCK_BLOCK);
	}

	// [... blockBaseLocation] -> [... blockTailTagLocation]
	private static void tailTag(ASMCodeFragment frag) {
		frag.add(Duplicate);						// [... block block]
		readTagSize(frag);							// [... block size]
		frag.add(Add);								// [... block+size]
		frag.add(PushI, MMGR_TAG_SIZE_IN_BYTES);	// [... block+size tagsize]
		frag.add(Subtract);							// [... tailTaglocation]
	}

	// [... tagBaseLocation] -> [... tagPointer]		(i.e. nextPtr or prevPtr) 
	private static void readTagPointer(ASMCodeFragment frag) {
		readIOffset(frag, TAG_POINTER_OFFSET);
	}
	// [... tagBaseLocation] -> [... blockSize] 
	private static void readTagSize(ASMCodeFragment frag) {
		readIOffset(frag, TAG_SIZE_OFFSET);
	}
	// [... tagBaseLocation] -> [... blockSize]
	private static void readTagAvailable(ASMCodeFragment frag) {
		readCOffset(frag, TAG_AVAIL_OFFSET);
	}
	// [... ptrToWrite tagBaseLocation] -> [...]
	private static void writeTagPointer(ASMCodeFragment frag) {
		writeIOffset(frag, TAG_POINTER_OFFSET);
	}
	// [... size tagBaseLocation] -> [...] 
	private static void writeTagSize(ASMCodeFragment frag) {
		writeIOffset(frag, TAG_SIZE_OFFSET);
	}
	// [... isAvailable tagBaseLocation] -> [...] 
	private static void writeTagAvailable(ASMCodeFragment frag) {
		writeCOffset(frag, TAG_AVAIL_OFFSET);
	}

	
////////////////////////////////////////////////////////////////////////////////////
//Testing/Debug code
////////////////////////////////////////////////////////////////////////////////////

	private static final String MMGRD_FORMAT = "$$debug-format";
	private static final String MMGRD_FORMAT_FOR_STRING = "$$debug-format-for-string";
	private static final String MMGRD_MAIN_BLOCK1 = "$$mmgrd-main-block1";
	private static final String MMGRD_MAIN_BLOCK2 = "$$mmgrd-main-block2";
	private static final String MMGRD_MAIN_BLOCK3 = "$$mmgrd-main-block3";
	private static final String MMGRD_MAIN_BLOCK4 = "$$mmgrd-main-block4";
	
	private static void insertDebugMain(ASMCodeFragment frag) {
		frag.add(DLabel, MMGRD_FORMAT);
		frag.add(DataS, "%s %d\n");	
		frag.add(DLabel, MMGRD_FORMAT_FOR_STRING);
		frag.add(DataS, "%s");
		declareI(frag, MMGRD_MAIN_BLOCK1);
		declareI(frag, MMGRD_MAIN_BLOCK2);
		declareI(frag, MMGRD_MAIN_BLOCK3);
		declareI(frag, MMGRD_MAIN_BLOCK4);
		
		frag.add(PushI, 30);					// request block of size 30 => 30+18=48
		debugSystemBlockAllocate(frag);
		storeITo(frag, MMGRD_MAIN_BLOCK1);
		debugPrintBlockFromPointer(frag, MMGRD_MAIN_BLOCK1);
		
		frag.add(Call, MMGRD_PRINT_FREE_LIST);
		
		loadIFrom(frag, MMGRD_MAIN_BLOCK1);			// [... block1]
		debugSystemBlockDeallocate(frag);
		debugPrint(frag, "deallocation done\n");
		
		frag.add(Call, MMGRD_PRINT_FREE_LIST);
		
		frag.add(PushI, 40);
		debugSystemBlockAllocate(frag);
		storeITo(frag, MMGRD_MAIN_BLOCK2);
		debugPrintBlockFromPointer(frag, MMGRD_MAIN_BLOCK2);
		
		loadIFrom(frag, MMGRD_MAIN_BLOCK2);			// [... block2]
		debugSystemBlockDeallocate(frag);
		debugPrint(frag, "deallocation 2 done\n");

		frag.add(Call, MMGRD_PRINT_FREE_LIST);
		
		frag.add(PushI, 150);
		debugSystemBlockAllocate(frag);
		storeITo(frag, MMGRD_MAIN_BLOCK3);
		debugPrintBlockFromPointer(frag, MMGRD_MAIN_BLOCK3);

		
		loadIFrom(frag, MMGRD_MAIN_BLOCK3);			// [... block3]
		debugSystemBlockDeallocate(frag);
		debugPrint(frag, "deallocation 3 done\n");

		frag.add(Call, MMGRD_PRINT_FREE_LIST);
		
		frag.add(PushI, 30);
		debugSystemBlockAllocate(frag);
		storeITo(frag, MMGRD_MAIN_BLOCK4);
		debugPrintBlockFromPointer(frag, MMGRD_MAIN_BLOCK4);
		
		frag.add(Call, MMGRD_PRINT_FREE_LIST);
		
		loadIFrom(frag, MMGRD_MAIN_BLOCK4);			// [... block4]
		debugSystemBlockDeallocate(frag);
		debugPrint(frag, "deallocation 4 done\n");

		frag.add(Call, MMGRD_PRINT_FREE_LIST);
		
		// reusing block1
		frag.add(PushI, 25);
		debugSystemBlockAllocate(frag);
		storeITo(frag, MMGRD_MAIN_BLOCK1);
		debugPrintBlockFromPointer(frag, MMGRD_MAIN_BLOCK1);
		
		frag.add(Call, MMGRD_PRINT_FREE_LIST);
		
		loadIFrom(frag, MMGRD_MAIN_BLOCK1);			// [... block4]
		debugSystemBlockDeallocate(frag);
		debugPrint(frag, "deallocation 5 done\n");

		frag.add(Call, MMGRD_PRINT_FREE_LIST);
		
		
		// reusing block1
		frag.add(PushI, 40);
		debugSystemBlockAllocate(frag);
		storeITo(frag, MMGRD_MAIN_BLOCK1);
		debugPrintBlockFromPointer(frag, MMGRD_MAIN_BLOCK1);
		
		frag.add(Call, MMGRD_PRINT_FREE_LIST);
		
		loadIFrom(frag, MMGRD_MAIN_BLOCK1);			// [... block4]
		debugSystemBlockDeallocate(frag);
		debugPrint(frag, "deallocation 6 done\n");

		frag.add(Call, MMGRD_PRINT_FREE_LIST);
	}
	private static void debugPrintBlockFromPointer(ASMCodeFragment frag, String pointerName) {
		loadIFrom(frag, pointerName);
		frag.add(Call, MMGRD_PRINT_BLOCK);	
		debugPrint(frag, "\n");
	}

	// [... size] -> [... block]
	private static void debugSystemBlockAllocate(ASMCodeFragment frag) {
		frag.add(Call, MEM_MANAGER_ALLOCATE);	// [... userblock]
		frag.add(PushI, MMGR_TAG_SIZE_IN_BYTES);
		frag.add(Subtract);						// [... block]
	}
	private static void debugSystemBlockDeallocate(ASMCodeFragment frag) {
		frag.add(PushI, MMGR_TAG_SIZE_IN_BYTES);	// [... block1 tagsize]
		frag.add(Add);								// [... userBlock1]
		frag.add(Call, MEM_MANAGER_DEALLOCATE);		// [...]
	}
	

	// prints top of stack 
	// [... t] -> [... t]
	@SuppressWarnings("unused")
	private static void debugPeekI(ASMCodeFragment frag, String printString) {
		String label = new Labeller("$$debug-peekI").newLabel("");
		frag.add(DLabel, label);
		frag.add(DataS, printString);
		
		frag.add(Duplicate);			// [... t t]
		frag.add(PushD, label);			// [... t t printString]
		frag.add(PushD, MMGRD_FORMAT);  
		frag.add(Printf);
	}	
	private static void debugPrint(ASMCodeFragment frag, String printString) {
		String label = new Labeller("$$debug-print").newLabel("");
		frag.add(DLabel, label);
		frag.add(DataS, printString);
		frag.add(PushD, label);
		frag.add(PushD, MMGRD_FORMAT_FOR_STRING);
		frag.add(Printf);
	}	
	@SuppressWarnings("unused")
	private static void debugPrintI(ASMCodeFragment frag, String printString, String name) {
		String label = new Labeller("$$debug-printI").newLabel("");
		loadIFrom(frag, name);
		frag.add(DLabel, label);
		frag.add(DataS, printString);
		frag.add(PushD, label);
		frag.add(PushD, MMGRD_FORMAT);
		frag.add(Printf);
	}
	

	private static final String MMGRD_PRINT_BLOCK =			  "--mmgrd-print-block";
	private static final String MMGRD_PRINT_FREE_LIST =		  "--mmgrd-print-free-list";
	private static final String MMGRD_PBLOCK_RETURN_ADDRESS = "$$mmgrd-pblock-return";
	private static final String MMGRD_PBLOCK_BLOCK =		  "$$mmgrd-pblock-block";
	private static final String MMGRD_PBLOCK_FORMAT =		  "$$mmgrd-pblock-format";
	private static final String MMGRD_PFREE_RETURN_ADDRESS =  "$$mmgrd-pfree-return";
	private static final String MMGRD_PFREE_CURRENT_BLOCK  =  "$$mmgrd-pfree-current-block";
	private static final String MMGRD_PFREE_LOOP_TEST  	   =  "--mmgrd-pfree-loop-test";
	private static final String MMGRD_PFREE_LOOP_DONE  	   =  "--mmgrd-pfree-loop-done";
	
	// [... block] -> [...]
	private static ASMCodeFragment subroutineDebugPrintBlock() {
		ASMCodeFragment frag = new ASMCodeFragment(GENERATES_VOID);
		frag.add(Label, MMGRD_PRINT_BLOCK);				// [... block (return)]
		
		declareI(frag, MMGRD_PBLOCK_RETURN_ADDRESS);
		declareI(frag, MMGRD_PBLOCK_BLOCK);

		storeITo(frag, MMGRD_PBLOCK_RETURN_ADDRESS);	// [... block]
		storeITo(frag, MMGRD_PBLOCK_BLOCK);				// [...]

		loadIFrom(frag, MMGRD_PBLOCK_BLOCK);			// [... block]

		
		frag.add(DLabel, MMGRD_PBLOCK_FORMAT);
		frag.add(DataS, "%8X[size %d %d, avail %1d%1d, %8X %8X]");


		loadIFrom(frag, MMGRD_PBLOCK_BLOCK);
		tailTag(frag);
		readTagPointer(frag);							// [... next]
		
		loadIFrom(frag, MMGRD_PBLOCK_BLOCK);
		readTagPointer(frag);							// [... next prev]
		
		loadIFrom(frag, MMGRD_PBLOCK_BLOCK);
		tailTag(frag);
		readTagAvailable(frag);							// [... next prev avail2]
		loadIFrom(frag, MMGRD_PBLOCK_BLOCK);
		readTagAvailable(frag);							// [... next prev avail2 avail1]

		loadIFrom(frag, MMGRD_PBLOCK_BLOCK);
		tailTag(frag);
		readTagSize(frag);								// [... next prev avail2 avail1 size2]
		loadIFrom(frag, MMGRD_PBLOCK_BLOCK);
		readTagSize(frag);								// [... next prev avail2 avail1 size2 size1]
		
		loadIFrom(frag, MMGRD_PBLOCK_BLOCK);			// [... next prev avail2 avail1 size2 size1 block]
		
		frag.add(PushD, MMGRD_PBLOCK_FORMAT);
		frag.add(Printf);

		loadIFrom(frag, MMGRD_PBLOCK_RETURN_ADDRESS);
		frag.add(Return);
		
		return frag;
	}
	private static ASMCodeFragment subroutineDebugPrintFreeList() {
		ASMCodeFragment frag = new ASMCodeFragment(GENERATES_VOID);
		frag.add(Label, MMGRD_PRINT_FREE_LIST);				// [... block (return)]

		declareI(frag, MMGRD_PFREE_RETURN_ADDRESS);
		declareI(frag, MMGRD_PFREE_CURRENT_BLOCK);
		
		storeITo(frag, MMGRD_PFREE_RETURN_ADDRESS);
		
		debugPrint(frag, "Free list:\n");
		
		
		loadIFrom(frag, MEM_MANAGER_FIRST_FREE_BLOCK);
		storeITo(frag, MMGRD_PFREE_CURRENT_BLOCK);
		
		frag.add(Label, MMGRD_PFREE_LOOP_TEST);
		    // if(currentBlock == 0) break;
			loadIFrom(frag, MMGRD_PFREE_CURRENT_BLOCK);		
			frag.add(JumpFalse, MMGRD_PFREE_LOOP_DONE);
			
			// print "    "+currentBlock;
			debugPrint(frag, "    ");						
			loadIFrom(frag, MMGRD_PFREE_CURRENT_BLOCK);
			frag.add(Call, MMGRD_PRINT_BLOCK);
			debugPrint(frag, "\n");
			
			// currentBlock = currentBlock.next
			loadIFrom(frag, MMGRD_PFREE_CURRENT_BLOCK);		// [... block]
			tailTag(frag);									// [... tailTag]
			readTagPointer(frag);							// [... next]
			storeITo(frag, MMGRD_PFREE_CURRENT_BLOCK);
			
			frag.add(Jump, MMGRD_PFREE_LOOP_TEST);

		frag.add(Label, MMGRD_PFREE_LOOP_DONE);
			debugPrint(frag, "\n");
			loadIFrom(frag, MMGRD_PFREE_RETURN_ADDRESS);
			frag.add(Return);
		return frag;
	}	

}
