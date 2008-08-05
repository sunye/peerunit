package fr.inria.peerunit.tree.btree;

import java.util.Enumeration;

public class BTreeNewTest {
	/**
	 * BTreeNewTest constructor comment.
	 */
	public BTreeNewTest() {
		super();
	}
	/**
	 * Starts the application.
	 * @param args an array of command-line arguments
	 */
	public static void main(java.lang.String[] args) {
		// Insert code to start the application here.
		BTree btree = new BTree(3);
		/*btree.put("one","no1");
		btree.put("two","no2");
		btree.put("three","no3");
		btree.put("four","no4");
		btree.put("five","no5");*/
		for (int i=1;i<10;i++) {
			btree.put("key" + i,"value" + i);
		}

		Enumeration btenum = btree.keys();
		while (btenum.hasMoreElements()) {
			//System.out.println("Key: " + (String)((StringComparator)btenum.nextElement()).getKey());
			/*((Comparator)btenum.nextElement()).getKey();
			System.out.println("Key: " + (String)((StringComparator)btenum.nextElement()).getKey());
			String key=(String)((StringComparator)btenum.nextElement()).getKey();*/
					
			StringComparator key=(StringComparator)btenum.nextElement();
			System.out.println("Key: " + (String)key.getKey());
			if(btree.getRoot(key).parent==null){
				System.out.println("Root Node"+btree.getRoot(key).nKey);
				
				for(int i =0; i < btree.getRoot(key).kArray.length;i++){
					System.out.println((String)btree.getRoot(key).kArray[i].getObj());					
				}
				
				//System.out.println("Keys in Root Node"+btree.getRoot(key).kArray.toString());
			}else{
				System.out.println("Node "+btree.getRoot(key).nKey+" Parent "+btree.getRoot(key).parent.nKey);
				//System.out.println(" Parent "+btree.getRoot(key).parent);
			}
						
		}
		
		
	}
}


