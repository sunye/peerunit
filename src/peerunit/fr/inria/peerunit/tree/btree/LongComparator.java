package fr.inria.peerunit.tree.btree;

public class LongComparator implements Comparator{
	protected Long number;
	public LongComparator(Long val) {
		number=val;
	}
	public int compareTo(Object obj) {
		long result = number.longValue()-((LongComparator)obj).number.longValue();
		if (result < 0) return -1;
		if (result == 0) return 0;
		return 1;
	}
	public Object getKey() {
		// TODO Auto-generated method stub
		return null;
	}
}
