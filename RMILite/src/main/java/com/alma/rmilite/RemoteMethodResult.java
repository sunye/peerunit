package com.alma.rmilite;

import java.io.Serializable;

/**
 * Contains a serializable result of a remote method.
 */
public interface RemoteMethodResult extends Serializable {

	/**
	 * Returns the result of a remote method invocation.
	 * @return the result
	 */
	public Object getObject();
}