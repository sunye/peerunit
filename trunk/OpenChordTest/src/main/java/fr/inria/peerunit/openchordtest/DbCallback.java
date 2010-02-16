package fr.inria.peerunit.openchordtest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import de.uniba.wiai.lspi.chord.service.ChordCallback;
import de.uniba.wiai.lspi.chord.service.Key;

public class DbCallback  implements ChordCallback{
	private List<String> resultSet=new ArrayList<String>();
	public int retr=0;
	public int newRetr=-1;	
	private Collection<Key> insertedKeys= new ArrayList<Key>();
	private Collection<Key> failedKeys= new ArrayList<Key>();
	private Logger log;
	
	private int OBJECTS;
	public void setCallback(int OBJECTS){
		this.OBJECTS=OBJECTS;
	}
	
	public void setCallback(int OBJECTS,Logger log){		
		this.log=log;
		this.OBJECTS=OBJECTS;
	}
	
	public void retrieved(Key key, Set<Serializable> entries, Throwable t){
		
		if (t == null) {
			for(Serializable entry: entries){
				if(! resultSet.contains(entry.toString())){
					resultSet.add(entry.toString());
				}					
				if (retr!=newRetr){
					//actual=0;
					newRetr=retr;
				}
				//actual++;
				log.info("[Test] "+retr+" Asynch Retrieve :"+entry.toString());
			}			
		} else {
			log.info("[Test] Retrieval with key " 
					+ key + " failed!");
			t.printStackTrace();  			
		}			
	}
	public void inserted(Key key, Serializable entry, Throwable t){			
		//insertedKeys.remove(key);			
		if (t == null) {			
			insertedKeys.add(key);
			log.info("[Test] Asynch Successfully inserted " 
					+ entry + " with key " 
					+ key); 
		} else {			
			failedKeys.add(key);
			log.info("[Test] Insert of " 
					+ entry + " with key " 
					+ key + " failed!");
			t.printStackTrace();  			
		}
	}
	public void removed(Key key, Serializable entry, Throwable t){
		if (t == null) {
			log.info("[Test] Asynch Successfully removed " 
					+ entry + " with key " 
					+ key); 
		} else {
			log.info("[Test] Removal of " 
					+ entry + " with key " 
					+ key + " failed!");
			t.printStackTrace();  			
		}
	}		
	public int getSizeExpected(){
		return resultSet.size();
	}	

	public List<String> getResultSet(){
		return resultSet;
	}
	
	/*public void waitInserts(Collection keys) throws InterruptedException {			
		insertedKeys=keys;
		while(!insertedKeys.isEmpty()) {			
			log.info("[Test] Inserted Keys "+insertedKeys.size());
			log.info("[Test] Failed Keys "+failedKeys.size());
			insertedKeys.wait(200);
		}			
	}*/		
	
	public void clearResultSet(){
		resultSet.clear();
	}
	
	public boolean isInserted() {	
		if((failedKeys.size()+ insertedKeys.size())>=OBJECTS){
			return true;
		}else		
			return false;
	}
	
	public int insertSize(){
		return insertedKeys.size();
	}
	
	public Collection<Key> getInsertedKeys(){
		return insertedKeys;
	}
	
	
}	
