package tsdb.iterator;

import java.util.NoSuchElementException;

import tsdb.util.TsEntry;
import tsdb.util.iterator.TsIterator;

public class BufferIterator extends TsIterator {
	
	private final TsIterator input_iterator;
	private final int windowSize;
	protected final int halfWindow;
	protected final TsEntry[] window;
	
	private int windowReadPos = 0;
	private int windowCurrPos = 0;
	private int elementsRead = 0;
	private int elementsReturned = 0;
	private boolean initialized = false;
	private boolean finished = false;

	public BufferIterator(TsIterator input_iterator, int windowSize) {
		super(input_iterator.getSchema());
		this.input_iterator = input_iterator;
		this.windowSize = windowSize;
		this.halfWindow = windowSize / 2;
		this.window = new TsEntry[windowSize];
	}
	
    private void initialize() {
        for (int i = 0; i < halfWindow && input_iterator.hasNext(); i++) {
            window[windowReadPos] = input_iterator.next();
            windowReadPos = (windowReadPos + 1) % windowSize;
            elementsRead++;
        }
        initialized = true;
        if (elementsReturned >= elementsRead) {
            finished = true;
        }
    }
	
	@Override
	public boolean hasNext() {
		if (!initialized) {
            initialize();
        }
        return !finished;
	}

	@Override
	public TsEntry next() {
		if (!initialized) {
            initialize();
        }
        if (finished) {
            throw new NoSuchElementException();
        }

        TsEntry result = calc(windowCurrPos);
        
        elementsReturned++;
        
        if (input_iterator.hasNext()) {
            window[windowReadPos] = input_iterator.next();
            windowReadPos = (windowReadPos + 1) % windowSize;
            windowCurrPos = (windowCurrPos + 1) % windowSize;
            elementsRead++;
        } else {
        	window[windowReadPos] = null;
            windowReadPos = (windowReadPos + 1) % windowSize;
            windowCurrPos = (windowCurrPos + 1) % windowSize;
        }

        if (elementsReturned >= elementsRead) {
            finished = true;
        }       

        return result;
	}

	protected TsEntry calc(int currentPos) {
		return window[currentPos];
	}
}
