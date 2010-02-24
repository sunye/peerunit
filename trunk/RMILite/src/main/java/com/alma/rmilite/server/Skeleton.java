package com.alma.rmilite.server;

import java.io.IOException;

public interface Skeleton {

	public boolean close() throws IOException;
	
	public int getPort();
}
