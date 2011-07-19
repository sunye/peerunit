package uk.co.demon.mcdowella.filesync;

import java.io.IOException;

/** This interface defines the operations we need to read from our
 *  input files.
 */
public interface FileWrapper
{
  /** Return the length of the file in bytes */
  long getLength() throws IOException;
  /** Move the read pointer to the given absolute offset */
  void seek(long absoluteOffset) throws IOException;
  /** return the byte at the read pointer and move it forwards */
  byte readByte() throws IOException;
  /** return the byte at the read pointer and move it backwards */
  byte readByteBackwards() throws IOException;
}
