package fr.univnantes.alma.nio.objectToBytes;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Decode a byte of array encoded via {@link Encoder}
 * 
 * @author Guillaume Le Louët
 */
public class Decoder {

    protected void requireHeader() {
	state = receivingState.HEADER;
	receivedSize = 0;
	expectedSize = Encoder.headerBytesSize;
    }

    {
	requireHeader();
    }

    /**
     * handle an array of bytes and try to decode as many bytes as it can. Uses
     * the bytes handled before, if required ; and memorizes unused bytes for
     * further use
     * 
     * @param buffer
     *            the array of bytes to handle. Will be copied if some data
     *            needed are missing, waiting for other arrays to be handled
     * @param maxPos
     *            last position in the index where bytes are meaningful. the
     *            array is considered as being of this size.
     * @return a list of objects decoded
     */
    public List<Serializable> decode(byte[] buffer, int maxPos) {
	List<Serializable> ret = new ArrayList<Serializable>();
	decode(buffer, 0, maxPos, ret);
	return ret;
    }

    protected static enum receivingState {
	HEADER, DATA
    }

    /** what this is awaiting */
    protected receivingState state;

    /** number of bytes of what this is awaiting that have already been received */
    protected int receivedSize;

    /** number of bytes this expect to receive before being able to process it */
    protected int expectedSize;

    /** bytes received for the header */
    protected final byte[] header = new byte[Encoder.headerBytesSize];

    /** the bytes that have been received since last header */
    protected byte[] receivedBytes;

    /**
     * handle a sub array by putting each objects decoded in a list
     * 
     * @param buffer
     *            the buffer representing the encoded data.
     * @param startPos
     *            the starting position of the sub array. Unused if <0. Method
     *            has no effect if >
     * @param buffer
     *            .length
     * @param endPos
     *            the last position of the sub array.
     * @param receivingList
     *            the list to put objects decoded into.
     */
    public void decode(byte[] buffer, int startPos, int endPos,
	    List<Serializable> receivingList) {
	int bufferOffset = 0;

	switch (state) {
	case HEADER:
	    for (; receivedSize + bufferOffset < header.length
		    && bufferOffset + startPos <= endPos; bufferOffset++) {
		header[receivedSize + bufferOffset] = buffer[bufferOffset
			+ startPos];
	    }
	    receivedSize += bufferOffset;
	    if (receivedSize == header.length) {
		headerReceived();
		if (expectedSize > 0) {
		    decode(buffer, startPos + bufferOffset, endPos,
			    receivingList);
		} else {
		    // if no data is expected from the header, then it means we
		    // need
		    // another header : data is already received as an empty
		    // array
		    requireHeader();
		}
	    }
	    break;
	case DATA:
	    assert (expectedSize != 0);// should never require a data of 0
	    // length.
	    for (; receivedSize + bufferOffset < receivedBytes.length
		    && bufferOffset + startPos <= endPos; bufferOffset++) {
		receivedBytes[receivedSize + bufferOffset] = buffer[bufferOffset
			+ startPos];
	    }
	    receivedSize += bufferOffset;
	    if (receivedSize == expectedSize) {
		dataReceived(receivingList);
		decode(buffer, startPos + bufferOffset, endPos, receivingList);
	    }
	    break;
	}
    }

    /** the header ha been successfully put in {@link #header} */
    protected void headerReceived() {
	state = receivingState.DATA;
	receivedSize = 0;
	expectedSize = decodeSize(header);
	receivedBytes = new byte[expectedSize];
    }

    /** decode a array of bytes as a int */
    protected int decodeSize(byte[] header) {
	int ret = 0;
	for (int i = 0; i < header.length; i++) {
	    ret = ret * 256 + (header[i] & 0xff);
	}
	return ret;
    }

    /**
     * {@link #receivedBytes} received as many bytes as expected by the header :
     * try to decode it
     * 
     * @param list
     *            the list of object in which to add objects unserialized
     */
    protected void dataReceived(List<Serializable> list) {
	try {
	    ByteArrayInputStream bais = new ByteArrayInputStream(receivedBytes);
	    ObjectInputStream ois = new ObjectInputStream(bais);
	    Serializable decoded = (Serializable) ois.readObject();
	    list.add(decoded);
	    receivedBytes = null;
	} catch (IOException e) {
	    e.printStackTrace();
	} catch (ClassNotFoundException e) {
	    e.printStackTrace();
	}
	requireHeader();
    }

}
