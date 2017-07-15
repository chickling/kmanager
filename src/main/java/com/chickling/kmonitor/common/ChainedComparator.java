package com.chickling.kmonitor.common;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author Hulva Luva.H
 *
 */
public class ChainedComparator<T> implements Comparator<T> {
	private List<Comparator<T>> listComparators;

	@SafeVarargs
	public ChainedComparator(Comparator<T>... comparators) {
		this.listComparators = Arrays.asList(comparators);
	}

	@Override
	public int compare(T o1, T o2) {
		for (Comparator<T> comparator : listComparators) {
			int result = comparator.compare(o1, o2);
			if (result != 0) {
				return result;
			}
		}
		return 0;
	}

}
