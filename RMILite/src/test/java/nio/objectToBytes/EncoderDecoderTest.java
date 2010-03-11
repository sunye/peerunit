package nio.objectToBytes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class EncoderDecoderTest {

	public Encoder encoder;
	public Decoder decoder;

	@BeforeMethod
	public void setUp() {
		decoder = new Decoder();
		encoder = new Encoder();
	}

	@Test(dataProvider = "ObjectsToSend")
	public void decodEncodeBasic(Serializable toSend) {
		byte[] encoded = encoder.encode(toSend);
		List<Object> received = decoder.decode(encoded, encoded.length);
		// printByteArray( encoded );
		// System.out.println();
		assert (received.get(0).equals(toSend));
	}

	@Test(dataProvider = "ObjectsToSend")
	public void decodeWithSmallBuffer(Serializable toSend) {
		byte[] encoded = encoder.encode(toSend);
		List<Object> received = new ArrayList<Object>();
		int chunk_size = 5;
		for(byte[] buff : explode(encoded, chunk_size)) {
			received.addAll(decoder.decode(buff, buff.length));
		}
		assert (received.get(0).equals(toSend));
	}
	
	@Test
	public void decodeWithcloseArrays() {
		Serializable[] datas  = new Serializable[]{"ok", "I", "got", "it"};
		int receivedObjs = 0;
		for(Serializable s : datas) {
			byte[] encoded = encoder.encode( s );
			List<Object> decoded = decoder.decode( encoded, encoded.length);
			receivedObjs+=decoded.size();
		}
		assert(receivedObjs == datas.length);
	}

	protected List<byte[]> explode(byte[] source, int maxSize) {
		List<byte[]> ret = new ArrayList<byte[]>();
		for (int startPos = 0; startPos < source.length; startPos += maxSize) {
			int endPos = startPos + maxSize -1;
			if (endPos > source.length-1) {
				endPos = source.length-1;
			}
			byte[] buff = new byte[endPos-startPos+1];
			for (int i = startPos; i <= endPos; i++) {
				buff[i-startPos] = source[i];
			}
			ret.add(buff);
		}
//		System.out.println("array exploded : ");
//		System.out.print("  ");printByteArray(source);System.out.println();
//		System.out.println("explosion : ");
//		System.out.print("  ");
//		for(byte[] buff : ret) {
//			printByteArray(buff); System.out.print("|");
//		}
//		System.out.println();
		return ret;
	}

	@DataProvider(name = "ObjectsToSend")
	public Object[][] objectsToSend() {
		return new Object[][] { { new Integer(3) }, { new Integer(-250) },
				{ new String("little String") },
				{ Arrays.asList("a", "cat", "is", "sleeping", ".") } };
	}

	@Test(dataProvider = "multipleObjectsToSend")
	public void testMultipleSends(List<Serializable> list) {
		for (Serializable toSend : list) {
			byte[] encoded = encoder.encode(toSend);
			List<Object> received = decoder.decode(encoded, encoded.length);
			// printByteArray( encoded );
			// System.out.println();
			// System.out.println("sent : "+toSend+" ; received : "+received);
			assert (received.get(0).equals(toSend));
		}
	}

	@DataProvider(name = "multipleObjectsToSend")
	public Object[][] multipleObjectsToSend() {
		return new Object[][] {
				{ Arrays.asList(new Integer(1), new Integer(2), new Integer(3)) },
				{ Arrays.asList("a", "cat", "is", "sleeping", ".") } };
	}

	public static void printByteArray(byte[] array, int size) {
		for (int i = 0; i < array.length && i<size; i++) {
			int val = (int) (array[i] & 0xff);// removing thesign
			System.out.print(":" + Integer.toHexString(val / 16)
					+ Integer.toHexString(val % 16));
		}
	}
	
	public static void printByteArray(byte[] array) {
		printByteArray( array, array.length );
	}

}
