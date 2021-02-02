package uk.co.demon.mcdowella.filesync;

import java.util.ArrayList;
import java.io.File;
import java.util.HashSet;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/** This class holds a set of file moves to be made to complete
 *  the transfer of version information. Until this is done, the
 *  old state of the backup/source code control system will be
 *  preserved.
 *  <br>
 *  In an ideal world, this set of operations would be atomic, but
 *  I don't know how to do that in an efficient, simple, and portable
 *  way. The best I can do here is to use a sequence of ordinary
 *  moves, aborting as soon as any exception is detected. We have only
 *  the minimal (but perhaps still useful) guarantee that this
 *  process is unlikely to be held up by common problems such as
 *  running out of disk space.
 *  <br>
 *  Moves are actioned in the order presented
 */
public class FileMover
{
  private List requests = new ArrayList();
  private static class MoveRequest
  {
    private final File from;
    private final File to;
    private final String debugInfo;
    MoveRequest(File fromFile, File toFile, String info)
    {
      from = fromFile;
      to = toFile;
      debugInfo = info;
    }
    boolean run()
    {
      // Ignore failed deletes. Perhaps the file to be moved to
      // doesn't exist. Since we are renaming to this file anyway,
      // the rename should either fail if the delete has failed or
      // succeed and render the failure of the delete irrelevant.
      to.delete();
      boolean result = from.renameTo(to);
      if (!result)
      {
        System.err.println("Could not move " + from + " to " + to +
	  " info was " + debugInfo);
      }
      return result;
    }
  }
  /** set of all files seen, for checking */
  private Set allFiles = new HashSet();
  /** Add a request for a move. IllegalArgumentException if
   *  from file does not exist, or if either file has been
   *  mentioned already
   */
  public void addMoveRequest(File from, File to, String info)
  {
    if (!from.exists())
    {
      throw new IllegalArgumentException(
        "File " + from + " to move from does not exist");
    }
    if (!allFiles.add(from))
    {
      throw new IllegalArgumentException(
        "From file " + from + " already seen in move request");
    }
    if (!allFiles.add(to))
    {
      throw new IllegalStateException(
        "To file " + to + " already seen in move request");
    }
    requests.add(new MoveRequest(from, to, info));
  }
  /** Action all requests until the first failure, clearing all
   *  saved requests regardless. Return true iff all requests
   *  successful
   */
  public boolean run()
  {
    boolean ok = true;
    for (Iterator i = requests.iterator(); i.hasNext();)
    {
      if (!((MoveRequest)i.next()).run())
      {
	ok = false;
	break;
      }
    }
    requests.clear();
    allFiles.clear();
    return ok;
  }
}
