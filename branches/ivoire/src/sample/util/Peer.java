package util;

public interface Peer {
	 public void join();
	 public void leave();
	 public void put(int key, String value);
	 public String get(int key);	
}
