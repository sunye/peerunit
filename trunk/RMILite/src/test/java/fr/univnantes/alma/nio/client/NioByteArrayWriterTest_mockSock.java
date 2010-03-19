package fr.univnantes.alma.nio.client;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class NioByteArrayWriterTest_mockSock {

    protected NioByteArrayWriter toTest;
    protected SocketChannel sock;

    @BeforeMethod
    public void setUp() throws IOException {
	sock = mock(SocketChannel.class);
	toTest = new NioByteArrayWriter(sock);
    }

    @DataProvider(name = "encodingvalues")
    public Object[][] encodingValues() {
	return new Object[][] {
		{ new Integer(25), ByteBuffer.wrap(new byte[] { 0, 0, 0, 25 }) },
		{ new Integer(1024), ByteBuffer.wrap(new byte[] { 0, 0, 4, 0 }) } };
    }

    @Test(dataProvider = "byteBuffersToSend")
    public void send(final ByteBuffer toSend) throws IOException {
	Answer<Integer> mockWrite = new Answer<Integer>() {

	    @Override
	    public Integer answer(InvocationOnMock invocation) throws Throwable {
		ByteBuffer buff = (ByteBuffer) invocation.getArguments()[0];
		return new Integer(buff.remaining());
	    }

	};
	when(sock.write((ByteBuffer) any())).thenAnswer(mockWrite);

	toTest.send(new Integer(25));

	verify(sock, times(1)).write((ByteBuffer) any());
    }

    @DataProvider(name = "byteBuffersToSend")
    public Object[][] byteBuffersToSend() {
	return new Object[][] { { ByteBuffer.wrap(new byte[] { 0, 1, 2 }) },
		{ ByteBuffer.wrap(new byte[] { 0 }) },
		{ ByteBuffer.wrap(new byte[] { 0, 1, 2, 3, 4, 5 }) } };
    }

}
