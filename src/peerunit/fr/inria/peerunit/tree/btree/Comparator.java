package fr.inria.peerunit.tree.btree;

public interface Comparator {
	
		/**
		 * Compare two Objects with respect to ordering. Typical
		 * implementations first cast their arguments to particular
		 * types in order to perform comparison.
		 * @param obj object to be compared with this
		 * @return a negative number if this is less than obj; a
		 * positive number if this is greater than obj; else 0
		**/
		public int compareTo(Object obj);
		public Object getKey() ;
}
