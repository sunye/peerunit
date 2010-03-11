package com.alma.rmilite;

import org.junit.Before;

public class RemoteTestSocket extends AbstractRemoteTest {
	
	@Before
	public void setUp() {
		setConfigManagerStrategy(new ConfigManagerSocketStrategy());
	}
	
}
