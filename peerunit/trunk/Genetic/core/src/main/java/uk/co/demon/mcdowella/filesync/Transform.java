package uk.co.demon.mcdowella.filesync;

import java.io.IOException;

/** This interface consists of the commands used to create one file
 *  from another
 */
public interface Transform
{
  /** Add a command to copy out the next numBytes bytes from the
   *  source file
   */
  void addCopy(long numBytes) throws IOException;
  /** Add a command to seek for the relative distance given in the
   *  source file
   */
  void addSeek(long relativeOffset) throws IOException;
  /** add a command to insert the given sequence of bytes at the
   *  end of the target file
   */
  void addInsert(byte[] data) throws IOException;
  /** cost of the equivalent addInsert command */
}
