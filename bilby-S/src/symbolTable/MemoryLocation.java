package symbolTable;

import asmCodeGenerator.codeStorage.ASMCodeFragment;
import asmCodeGenerator.runtime.RunTime;

public class MemoryLocation {
	public static final String GLOBAL_VARIABLE_BLOCK = RunTime.GLOBAL_MEMORY_BLOCK;
	public static final String FRAME_POINTER = "Frame pointer not yet implemented in RunTime.";
	
	private MemoryAccessMethod accessor;
	private String baseAddress;
	private int offset;
	
	public MemoryLocation(MemoryAccessMethod accessor, String baseAddress, int offset) {
		super();
		this.accessor = accessor;
		this.baseAddress = baseAddress;
		this.offset = offset;
	}

	public MemoryAccessMethod getAccessor() {
		return accessor;
	}
	public String getBaseAddress() {
		return baseAddress;
	}
	public int getOffset() {
		return offset;
	}
	public String toString() {
		return "M-" + accessor + "(" + baseAddress + ") +" + offset + "  ";
	}
	
	public void generateAddress(ASMCodeFragment code, String comment) {
		accessor.generateAddress(code, baseAddress, offset, comment);
	}
	
	
////////////////////////////////////////////////////////////////////////////////////
// Null MemoryLocation object
////////////////////////////////////////////////////////////////////////////////////
	
	public static MemoryLocation nullInstance() {
		return NullMemoryLocation.getInstance();
	}
	private static class NullMemoryLocation extends MemoryLocation {
		private static final int NULL_OFFSET = 0;
		private static NullMemoryLocation instance=null;
		
		private NullMemoryLocation() {
			super(MemoryAccessMethod.NULL_ACCESS, "", NULL_OFFSET);
		}
		public static NullMemoryLocation getInstance() {
			if(instance==null)
				instance = new NullMemoryLocation();
			return instance;
		}
	}
}
