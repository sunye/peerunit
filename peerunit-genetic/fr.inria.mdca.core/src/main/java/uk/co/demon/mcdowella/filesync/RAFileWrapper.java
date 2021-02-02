package uk.co.demon.mcdowella.filesync;

import java.io.IOException;
import java.io.File;
import java.io.RandomAccessFile;

/** FileWrapper round a RandomAccessFile
 */
public class RAFileWrapper implements FileWrapper, Closer.Closeable
{
  private RandomAccessFile f;
  private File file;
  private byte[] buffer;
  /** position of 0th byte in buffer in file */
  private long bufferPosition;
  /** number of valid bytes in the buffer */
  private int bufferValid;
  /** position of read pointer as a file offset */
  private long readPtr;
  /** length of file */
  private long length;
  public void close() throws IOException
  {
    if (f != null)
    {
      f.close();
      f = null;
    }
  }
  public RAFileWrapper(File fileName) throws IOException
  {
    file = fileName;
    f = new RandomAccessFile(fileName, "r");
    buffer = new byte[1000];
    length = f.length();
  }
  public long getLength()
  {
    return length;
  }
  public void seek(long absoluteOffset) throws IOException
  {
    if ((absoluteOffset < 0) || (absoluteOffset >= length))
    {
      throw new IOException("Invalid seek offset");
    }
    readPtr = absoluteOffset;
  }
  public byte readByte() throws IOException
  {
    if ((readPtr < bufferPosition) ||
        (readPtr >= (bufferPosition + bufferValid)))
    {
      if ((readPtr < 0) || (readPtr >= length))
      {
	/*
	System.err.println("readPtr " + readPtr + " length " +
	  length + " file " + file);
	*/
	throw new IOException("Invalid readByte offset");
      }
      f.seek(readPtr);
      bufferValid = f.read(buffer);
      bufferPosition = readPtr;
    }
    byte result = buffer[(int)(readPtr - bufferPosition)];
    readPtr++;
    return result;
  }
  public byte readByteBackwards() throws IOException
  {
    int readSize = buffer.length;
    while ((readPtr < bufferPosition) ||
        (readPtr >= (bufferPosition + bufferValid)))
    {
      if ((readPtr < 0) || (readPtr >= length))
      {
	throw new IOException("Invalid readByteBackwards offset");
      }
      long ourPtr = readPtr - readSize + 1;
      if (ourPtr < 0)
      {
        ourPtr = 0;
      }
      f.seek(ourPtr);
      bufferValid = f.read(buffer);
      bufferPosition = ourPtr;
      readSize = bufferValid;
    }
    byte result = buffer[(int)(readPtr - bufferPosition)];
    readPtr--;
    return result;
  }
}
