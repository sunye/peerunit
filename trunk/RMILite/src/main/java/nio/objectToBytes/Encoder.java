package nio.objectToBytes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Encoder {

	public final static int headerBytesSize = 4;

	public byte[] encode(Serializable s) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(s);
			int size = baos.size();
			byte[] ret = new byte[size + headerBytesSize];
			encodeInt(size, ret);
			byte[] buff = baos.toByteArray();
			for (int i = 0; i < size; i++) {
				ret[headerBytesSize + i] = buff[i];
			}
			return ret;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected void encodeInt(int toEncode, byte[] buffer) {
		for (int i = headerBytesSize - 1; i >= 0; i--) {
			buffer[headerBytesSize - i -1] = (new Integer(toEncode
					/ (int) Math.pow(256, i)).byteValue());
		}
	}

}
