package fr.inria.peerunit.tree.oldbtree;

public class SearchResult {
	private BTNode btnode;
	private int keyIndex;

	SearchResult(BTNode btnode, int keyIndex) {
		this.btnode = btnode;
		this.keyIndex = keyIndex;
	}
	BTNode getBTNode() {
		return btnode;
	}
	int getKeyIndex() {
		return keyIndex;
	}
}
