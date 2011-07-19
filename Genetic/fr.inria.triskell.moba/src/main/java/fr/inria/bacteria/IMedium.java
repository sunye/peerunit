package fr.inria.bacteria;

import java.util.ArrayList;

public interface IMedium {
	public boolean isEmpty();

	public IBacterium getNextBacterium();

	public ArrayList<IBacterium> getBacteria();
}
