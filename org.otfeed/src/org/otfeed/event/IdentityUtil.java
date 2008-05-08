package org.otfeed.event;

import java.util.Set;

/**
 * Utilities for identity computations.
 */
abstract class IdentityUtil {
	
	private IdentityUtil() { }

	/**
	 * Universal comparison routine.
	 * @param <T> type.
	 * @param o1  object1.
	 * @param o2  object2.
	 * @return comparison result.
	 */
	public static <T extends Comparable<T>> int safeCompare(T o1, T o2) {
// System.out.println("comparing [" + o1 + "] and [" + o2 + "]");
    	if(o1 != null) {
    		return o2 == null ? 1 : o1.compareTo(o2);
    	} else {
    		return o2 == null ? 0 : -1;
    	}
    }

	/**
	 * Deep comparison routine for enum sets.
	 * @param <E> type.
	 * @param cls class.
	 * @param set1 set1.
	 * @param set2 set2.
	 * @return comparison result.
	 */
	public static <E extends Enum<E>> int compareEnumSet(
			Class<E> cls, 
			Set<E> set1, Set<E> set2) {
		
		if(set1 != null) {
			if(set2 == null) return 1;
			for(E elt : cls.getEnumConstants()) {
				boolean c1 = set1.contains(elt);
				boolean c2 = set2.contains(elt);
			
				if(c1 != c2) {
					return c1 ? 1 : -1;
				}
			}

			return 0;
		} else {
			return set2 == null ? 0 : -1;
		}
	}

	/**
	 * Safe hash computation routine.
	 * @param <T> type.
	 * @param o object.
	 * @return hash code.
	 */
	public static <T> int safeHashCode(T o) {
		return o == null ? 0 : o.hashCode();
	}
	
	/**
	 * Computes deep hash of an enum set.
	 * @param <E> type.
	 * @param set set.
	 * @return hash code.
	 */
	public static <E extends Enum<E>> int enumSetHashCode(Set<E> set) {
		int code = 0;
		
		if(set != null) {
			for(E elt : set) {
				code += elt.hashCode();
			}
		}
		return code;
	}
	
	/**
	 * Universal equality routine.
	 * @param <T> type.
	 * @param o1 object1.
	 * @param o2 object2.
	 * @return true, if equal.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Comparable<T>> boolean equalsTo(T o1, Object o2) {
		assert(o1 != null);
		if(o2 != null && o2.getClass() == o1.getClass()) {
			return o1.compareTo((T) o2) == 0;
		} else {
			return false;
		}
	}
}
