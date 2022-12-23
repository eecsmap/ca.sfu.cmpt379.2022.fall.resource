package symbolTable;

import java.util.ArrayList;
import java.util.List;

public class PositiveMemoryAllocator implements MemoryAllocator {
	MemoryAccessMethod accessor;
	String baseAddress;
	final int startingOffset;	
	int currentOffset;
	int maxOffset;
	List<Integer> bookmarks;
	
	public PositiveMemoryAllocator(MemoryAccessMethod accessor, String baseAddress, int startingOffset) {
		this.accessor = accessor;
		this.baseAddress = baseAddress;
		this.startingOffset = startingOffset;
		this.currentOffset = startingOffset;
		this.maxOffset = startingOffset;
		this.bookmarks = new ArrayList<Integer>();
	}
	public PositiveMemoryAllocator(MemoryAccessMethod accessor, String baseAddress) {
		this(accessor, baseAddress, 0);
	}

	@Override
	public MemoryLocation allocate(int sizeInBytes) {
		int offset = currentOffset;
		currentOffset += sizeInBytes;
		updateMax();
		return new MemoryLocation(accessor, baseAddress, offset);
	}
	private void updateMax() {
		if(maxOffset < currentOffset) {
			maxOffset = currentOffset;
		}
	}
	@Override
	public String getBaseAddress() {
		return baseAddress;
	}
	@Override
	public int getMaxAllocatedSize() {
		return maxOffset - startingOffset;
	}
	
	@Override
	public void saveState() {
		bookmarks.add(currentOffset);
	}
	@Override
	public void restoreState() {
		assert bookmarks.size() > 0;
		int bookmarkIndex = bookmarks.size()-1;
		currentOffset = (int) bookmarks.remove(bookmarkIndex);
	}
}
