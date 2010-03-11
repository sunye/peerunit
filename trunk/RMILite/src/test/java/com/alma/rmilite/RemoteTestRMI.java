package com.alma.rmilite;

import org.junit.Before;

public class RemoteTestRMI extends AbstractRemoteTest {
	
	@Before
	public void setUp() {
		setConfigManagerStrategy(new ConfigManagerRMIStrategy());
	}
	
}
