package tsdb.util.iterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import tsdb.util.TsEntry;
import tsdb.util.TsSchema;

public class TsIteratorIterator extends MoveIterator {

	private Iterator<TsIterator> input_iterator;
	private SchemaConverterIterator current_iterator;
	
	public static <T> TsIteratorIterator create(Iterable<T> input, Function<T,TsIterator> function) {
		return create(input.iterator(),function);
	}

	public static <T> TsIteratorIterator create(Iterator<T> input_iterator, Function<T,TsIterator> function) {
		Set<String> schemaSet = new HashSet<String>();
		List<TsIterator> list = new ArrayList<TsIterator>();
		while(input_iterator.hasNext()) {
			TsIterator timeSeriesIterator = function.apply(input_iterator.next());
			List<String> schemalist = Arrays.asList(timeSeriesIterator.getNames());
			System.out.println("schemalist: "+schemalist);
			schemaSet.addAll(schemalist);
			System.out.println("schemaSet: "+schemaSet);
			list.add(timeSeriesIterator);
		}			
		return new TsIteratorIterator(list,schemaSet.toArray(new String[0]));
	}
	
	public TsIteratorIterator(Iterable<TsIterator> input, String[] outputSchema) {
		this(input.iterator(),outputSchema);
	}

	public TsIteratorIterator(Iterator<TsIterator> input_iterator, String[] outputSchema) {
		super(new TsSchema(outputSchema));
		this.input_iterator = input_iterator;
		current_iterator = null;
	}

	@Override
	protected TsEntry getNext() {			
		if(current_iterator==null) {
			if(input_iterator.hasNext()) {
				TsIterator next = input_iterator.next();
				current_iterator = new SchemaConverterIterator(next, schema.names, true);
				//System.out.println("get next iterator");
				return getNext();
			} else {
				return null;
			}
		} else if(current_iterator.hasNext()) {
			return current_iterator.next();
		} else {
			current_iterator = null;
			return getNext();
		}
	}	
}