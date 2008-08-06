package fr.inria.peerunit.tree.oldbtree;

public class StringComparator implements Comparator{
	protected String text;

	public StringComparator(String val) {
		text=val;
	}
	public int compareTo(Object obj) {
		return text.compareTo(((StringComparator)obj).text);
	}
	public Object getKey() {
		return (Object)text;
	}
}
