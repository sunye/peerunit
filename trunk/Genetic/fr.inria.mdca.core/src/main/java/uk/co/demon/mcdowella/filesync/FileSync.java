package uk.co.demon.mcdowella.filesync;

import java.util.ArrayList;
import java.util.HashMap;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/** This class contains a method to produce a list of commands that
 *  can be used to produce one file from another.
 */
public class FileSync
{
  /** Transform builder that keeps track of source file offset */
  private static class TellTaleTransformBuilder implements 
    TransformBuilder
  {
    private TransformBuilder tb;
    private long offset = 0;
    TellTaleTransformBuilder(TransformBuilder t)
    {
      tb = t;
    }
    public void addCopy(long numBytes) throws IOException
    {
      tb.addCopy(numBytes);
      offset += numBytes;
    }
    public double copyCost(long numBytes)
    {
      return tb.copyCost(numBytes);
    }
    public void addSeek(long relativeOffset) throws IOException
    {
      tb.addSeek(relativeOffset);
      offset += relativeOffset;
    }
    public double seekCost(long relativeOffset)
    {
      return tb.seekCost(relativeOffset);
    }
    public void addInsert(byte[] data) throws IOException
    {
      tb.addInsert(data);
    }
    public double insertCost(int numBytes)
    {
      return tb.insertCost(numBytes);
    }
    public long tell()
    {
      return offset;
    }
  }
  /** No constructor - only static methods are used */
  private FileSync()
  {
  }
  /** Number of bytes used to resync. Change createChunk if you
   *  change this. */
  private static int CHUNK_SIZE = 4;
  /** create an immutable object representing a chunk of bytes which
   *  has equals() and hashCode() conforming to byte comparison.
   *  Change CHUNK_SIZE if you change this.
   */
  private static Object createChunk(byte[] b)
  {
    int x = 0;
    for (int i = 0; i < CHUNK_SIZE; i++)
    {
      x = (x << 8) + (b[i] & 0xff);
    }
    return new Integer(x);
  }
  /** Add a chunk of bytes to the Map between chunks and offsets.
   *  We later use this map to search for matches between new and
   *  old data, creating it from the old data and searching along
   *  the new checking it for matches.
   */
  private static void addBytes(FileWrapper source, long offset,
    byte[] chunk, Map map) throws IOException
  {
    // System.err.println("Offset is " + offset);
    source.seek(offset);
    for (int i = 0; i < chunk.length; i++)
    {
      chunk[i] = source.readByte();
    }
    Object oc = createChunk(chunk);
    List l = (List)map.get(oc);
    if (l == null)
    {
      l = new ArrayList();
      map.put(oc, l);
    }
    l.add(new Long(offset));
  }
  /** Create transform creating target given source.
   *  @param source The FileWrapper object used as source by the
   *         transform.
   *  @param target The created transform should create something
   *         equivalent to this when applied to source.
   *  @param cg This TransformBuilder receives the individual operations
   *         of the created transform, and can also cost putative
   *         operations.
   *  @param skipFactor. Must be > 1.0. This is the size multiplier
   *         used to produce an exponentially increasing sequence of
   *         skips used to try and resync the source and target. We
   *         try skips of 1, skipFactor, skipFactor^2,... bytes.
   *  @param maxInsertLen. Maximum size of sequence of inserted bytes
   *         created in the transform. This also sets a limit on the
   *         amount we are prepared to move forward in the target file
   *         while trying to resync the two files. Must be at least
   *         the size of the chunk used to find resyncs - currently 4.
   *         should normally be at least a few thousand bytes, because
   *         of its impact on resyncs.
   * <br>
   * This method does not use the dynamic programming algorithm that
   * provides the smallest possible list of commands to transform one
   * file into another, or any descendant of it. These methods cost
   * n^2, or n times the edit distance, or something that grows quite
   * quickly with n. Note that most source code systems - and diff -
   * look for operations on lines, not individual characters, to
   * decrease the effective n. This doesn't work too well with binary
   * files. Instead we look for matches between the source and target
   * files. When we need to find a match we build a table of chunks
   * of bytes at distances 1, x, x^2, x^3... from the current position
   * in the source file, then scan through a chunk of target file
   * looking for matching chunks. The cost of the table building stage
   * depends only logarithmically on the size of the source file. If
   * x = 2 then we store bytes at offset 1, 2, 4, 8, 16, ... so if
   * the current target region starts a chunk of length 2^k that matches
   * with a chunk of length 2^k in the source file, we should notice
   * that match within a scan forward of length about 2^k as long as the
   * chunk in the source file is not more than about 2^(k+1) away from
   * wherever we are scanning from there.
   */
  public static void createFrom(FileWrapper source, FileWrapper target,
    TransformBuilder tb, double skipFactor, int maxInsertLen)
    throws FileSyncException
  {
    if (skipFactor <= 1.0)
    {
      throw new IllegalArgumentException("skipFactor <= 1.0");
    }
    if (maxInsertLen < CHUNK_SIZE)
    {
      throw new IllegalArgumentException("maxInsertLen < " + CHUNK_SIZE);
    }
    TellTaleTransformBuilder cg = new TellTaleTransformBuilder(tb);
    // Points at position we should look at first for a match
    long sourceSearchPtr = 0;
    // Points at the first byte in the target file we have yet
    // to generate.
    long targetPtr = 0;
    // length of source file
    long sourceLength;
    // length of target file
    long targetLength;
    try
    {
      sourceLength = source.getLength();
      targetLength = target.getLength();
    }
    catch (IOException ioe)
    {
      throw new FileSyncException("Trouble getting file lengths", ioe);
    }
    try
    {
      // Holds bytes to be checked for a resync match
      byte[] chunk = new byte[CHUNK_SIZE];
      while (targetPtr < targetLength)
      { // here with bytes to generate
        // Terminates because each time through we either find a
	// match and shove in a copy or give up and shove in a
	// raw insert

	// System.out.println("Source ptr " + sourcePtr + " len " +
	//   sourceLength);
	// System.out.println("Target ptr " + targetPtr + " len " +
	//   targetLength);

	// First try to copy bytes over
	if (sourceSearchPtr >= sourceLength)
	{
	  sourceSearchPtr = sourceLength + targetPtr - targetLength;
	}
	if (sourceSearchPtr < 0)
	{
	  sourceSearchPtr = 0;
	}
	long todo = sourceLength - sourceSearchPtr;
	if (todo > (targetLength - targetPtr))
	{
	  todo = targetLength - targetPtr;
	}
	long copied = 0;
	boolean misMatch = false;
	if (todo > 0)
	{ // test here to avoid trying to seek in 0-length file
	  // System.out.println("SourceSearchPtr " + sourceSearchPtr);
	  source.seek(sourceSearchPtr);
	  target.seek(targetPtr);
	  for (long i = 0; i < todo; i++)
	  {
	    if (source.readByte() != target.readByte())
	    {
	      misMatch = true;
	      break;
	    }
	    copied++;
	  }
	}
	if (copied > 0)
	{
	  long cgp = cg.tell();
	  if (sourceSearchPtr != cgp)
	  {
	    cg.addSeek(sourceSearchPtr - cgp);
	  }
	  cg.addCopy(copied);
	  sourceSearchPtr += copied;
	  targetPtr += copied;
	}
        // here when next source byte does not match next target byte.
	// Will use next CHUNK_SIZE bytes to try to resync.
	if ((targetPtr + CHUNK_SIZE) > targetLength)
	{
	  break;
	}
	// First build a table from the target file
	Map sourcePositionListByChunk = new HashMap();
	if ((sourceSearchPtr + CHUNK_SIZE) <= sourceLength)
	{
	  addBytes(source, sourceSearchPtr, chunk,
		   sourcePositionListByChunk);
	}
	long move = 1;
	for (;;)
	{
	  boolean gotInfo = false;
	  if ((sourceSearchPtr + move + CHUNK_SIZE) <= sourceLength)
	  {
	    gotInfo = true;
	    addBytes(source, sourceSearchPtr + move, chunk,
	             sourcePositionListByChunk);
	  }
	  long movedBack = sourceSearchPtr - move;
	  if ((movedBack >= 0) && 
	      ((movedBack + CHUNK_SIZE) <= sourceLength))
	  {
	    gotInfo = true;
	    addBytes(source, movedBack, chunk,
	             sourcePositionListByChunk);
	  }
	  if (!gotInfo)
	  {
	    break;
	  }
	  long lastMove = move;
	  move = (long)Math.floor(move * skipFactor);
	  if (move == lastMove)
	  {
	    move++;
	  }
	}
	// Now start looking for a resync
	target.seek(targetPtr);
	for (int i = 0; i < (CHUNK_SIZE - 1); i++)
	{
	  chunk[i] = target.readByte();
	}
	boolean gotChunk = false;
	if (maxInsertLen > (targetLength - targetPtr))
	{
	  maxInsertLen = (int)(targetLength - targetPtr);
	}
	for (int i = (CHUNK_SIZE - 1); i < maxInsertLen; i++)
	{
	  chunk[CHUNK_SIZE - 1] = target.readByte();
	  // chunk contains bytes targetPtr + i - CHUNK_SIZE + 1
	  // to targetPtr + i
	  List matches = (List)sourcePositionListByChunk.get(
	    createChunk(chunk));
	  if (matches != null)
	  { // Found at least one match. If more than one, go for
	    // the least cost way of accounting for
	    // all the bytes up to the current position. If still more
	    // than one candidate we will take the first such match,
	    // since it starts closest
	    double bestCost = 0.0;
	    boolean gotBestCost = false;
	    long bestOffset = -1;
	    long bestMatchedBefore = -1;
	    // number of bytes to the left of the match
	    int gapLeft = i - CHUNK_SIZE + 1;
	    for (Iterator matchIt = matches.iterator();
	      matchIt.hasNext();)
	    {
	      // position of putative match in source file
	      long matchPosition = ((Long)matchIt.next()).longValue();
	      // gap between current position and start of match
	      // in target file
	      int toCheckLeft = gapLeft;
	      if (matchPosition < toCheckLeft)
	      { // here if no possible way we can fill in all the gap
	        // from the source file, which can happen if the
		// match is nearer the start of the source file than
		// the amount we have looked ahead in the target file
	        toCheckLeft = (int)matchPosition;
	      }
	      // Check bytes to left of match
	      int matchedBefore = 0;
	      if (toCheckLeft > 0)
	      {
		source.seek(matchPosition - 1);
		target.seek(targetPtr + i - CHUNK_SIZE);
		for (int j = 0; j < toCheckLeft; j++)
		{
		  if (target.readByteBackwards() !=
		      source.readByteBackwards())
		  {
		    break;
		  }
		  matchedBefore++;
		}
	      }
	      int gap = gapLeft - matchedBefore;
	      double cost = 0.0;
	      if (gap > 0)
	      {
	        cost += cg.insertCost(gap);
	      }
	      long relativeSeek = matchPosition - matchedBefore -
	                          cg.tell();
	      // relative seek must be != 0 because we know
	      // we don't have a match if we just follow on
	      cost += cg.seekCost(relativeSeek);
	      if ((!gotBestCost) || (cost < bestCost))
	      {
	        gotBestCost = true;
		bestCost = cost;
		bestOffset = matchPosition;
		bestMatchedBefore = matchedBefore;
	      }
	    }
	    // Now we know the best match. Use it
	    if (bestMatchedBefore < gapLeft)
	    {
	      byte[] toInsert = new byte[
	        (int)(gapLeft - bestMatchedBefore)];
	      target.seek(targetPtr);
	      for (int j = 0; j < toInsert.length; j++)
	      {
	        toInsert[j] = target.readByte();
	      }
	      cg.addInsert(toInsert);
	    }
	    long relativeSeek = bestOffset - bestMatchedBefore -
				cg.tell();
	    if (relativeSeek != 0)
	    {
	      cg.addSeek(relativeSeek);
	    }
	    // Look to see how far past the matched chunk we can copy
	    long matchedAfter = 0;
	    long pastSourceMatch = bestOffset + CHUNK_SIZE;
	    long toCheck = sourceLength - pastSourceMatch;
	    long pastTargetMatch = targetPtr + i + 1;
	    if (toCheck > (targetLength - pastTargetMatch))
	    {
	      toCheck = targetLength - pastTargetMatch;
	    }
	    if (toCheck > 0)
	    {
	      source.seek(pastSourceMatch);
	      target.seek(pastTargetMatch);
	      for (long j = 0; j < toCheck; j++)
	      {
	        if (target.readByte() != source.readByte())
		{
		  break;
		}
		matchedAfter++;
	      }
	    }
	    sourceSearchPtr = bestOffset - bestMatchedBefore;
	    long cgp = cg.tell();
	    if (cgp != sourceSearchPtr)
	    {
	      cg.addSeek(sourceSearchPtr - cgp);
	    }
	    long added = bestMatchedBefore + matchedAfter + CHUNK_SIZE;
	    cg.addCopy(added);
	    targetPtr = pastTargetMatch + matchedAfter;
	    sourceSearchPtr = pastSourceMatch + matchedAfter;
	    gotChunk = true;
	    break;
	  }
	  System.arraycopy(chunk, 1, chunk, 0, CHUNK_SIZE - 1);
	}
	if (!gotChunk)
	{ // Here if no matches whatsoever. Insert the whole chunk
	  byte[] forInsert = new byte[maxInsertLen];
	  target.seek(targetPtr);
	  for (int i = 0; i < forInsert.length; i++)
	  {
	    forInsert[i] = target.readByte();
	  }
	  cg.addInsert(forInsert);
	  targetPtr += forInsert.length;
	  sourceSearchPtr += (long)Math.round(forInsert.length *
	    (sourceLength / (double)targetLength));
	}
      }
      if (targetPtr < targetLength)
      {
        byte[] info = new byte[(int)(targetLength - targetPtr)];
	target.seek(targetPtr);
	for (int i = 0; i < info.length; i++)
	{
	  info[i] = target.readByte();
	}
	cg.addInsert(info);
      }
    }
    catch (IOException ioe)
    {
      System.err.println(ioe);
      ioe.printStackTrace();
      throw new FileSyncException("Trouble reading from files", ioe);
    }
  }
}
