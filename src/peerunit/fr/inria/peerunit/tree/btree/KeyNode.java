package fr.inria.peerunit.tree.btree;



public class KeyNode {
	
		private Comparator key;
		private Object data;

		KeyNode(Comparator key, Object data) {
			this.key=key;
			this.data=data;
		}
		Comparator getKey() {
			return key;
		}
		Object getObj() {
			return data;
		}
	
}
