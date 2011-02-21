package uk.co.demon.mcdowella.filesync;

/** This interface allows you to write out the commands used to
 *  create one file from another, and to inquire about their
 *  cost
 */
public interface TransformBuilder extends Transform
{
  /** cost of the equivalent addCopy command */
  double copyCost(long numBytes);
  /** cost of the equivalent addSeek command */
  double seekCost(long relativeOffset);
  /** cost of the equivalent addInsert command */
  double insertCost(int numBytes);
}
