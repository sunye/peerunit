package nio;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


public class ByteSocketSenderTest {

	protected ByteSocketSender toTest;
	protected SocketChannel sock;


	@BeforeMethod
	public void setUp() {
		sock = mock(SocketChannel.class);
		toTest = new ByteSocketSender(sock);
	}

	@Test(dataProvider="encodingvalues")
	public void encodeInt(Integer toEncode, ByteBuffer expected) {
		ByteBuffer buff = toTest.sizeEncoder;
		toTest.encodeInt( toEncode, buff );
		while(buff.remaining()>0) {
			byte b = buff.get();
			assert(b == expected.get());
		}
	}

	@DataProvider(name="encodingvalues")
	public Object[][] encodingValues() {
		return new Object[][]{
				{new Integer(25), ByteBuffer.wrap( new byte[]{0,0,0,25} )},
				{new Integer(1024),ByteBuffer.wrap( new byte[]{0,0,4,0} )}
		};
	}

	@Test(dataProvider="byteBuffersToSend")
	public void send(final ByteBuffer toSend) throws IOException {
		final ByteBuffer encoder = toTest.sizeEncoder;
		Answer<Integer> mockWrite = new Answer<Integer>() {

			@Override
			public Integer answer( InvocationOnMock invocation ) throws Throwable {
				ByteBuffer buff = (ByteBuffer) invocation.getArguments()[0];
				if(buff == encoder) {
					assert(encoder.get(3) == toSend.remaining());
				} else {
					assert(buff == toSend);
				}
				return new Integer(buff.remaining());
			}

		};
		when(sock.write((ByteBuffer) any())).thenAnswer( mockWrite);

		toTest.sendByteBuffer( toSend );

		verify( sock, times( 1 ) ).write(encoder);
		verify( sock, times( 1 ) ).write(toSend);
	}

	@DataProvider(name="byteBuffersToSend")
	public Object[][] byteBuffersToSend() {
		return new Object[][]{
				{ByteBuffer.wrap(new byte[]{0,1,2})},
				{ByteBuffer.wrap(new byte[]{0})},
				{ByteBuffer.wrap(new byte[]{0, 1, 2, 3, 4, 5})}
		};
	}

}
