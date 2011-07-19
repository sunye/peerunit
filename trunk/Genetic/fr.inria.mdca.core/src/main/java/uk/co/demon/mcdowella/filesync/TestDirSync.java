package uk.co.demon.mcdowella.filesync;

import java.util.Arrays;
import java.io.BufferedReader;
import java.util.Comparator;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.io.PrintWriter;

/** This class exists to test the DirSync versioning stuff
 */
public class TestDirSync
{
  /** Create a directory with the given name of maximum depth
   *  maxDepth, filling it with random directories and files
   */
  private static void createRandomDirectory(int maxDepth,
    double probFileStop, double probDirStop, 
    double probChunkStop, Random r, File target) throws IOException
  {
    if (maxDepth <= 0)
    {
      return;
    }
    if (!target.mkdir())
    {
      throw new IOException("Could not create directory " +
        target.getPath());
    }
    int numFiles = 1;
    for (;;)
    {
      if (r.nextDouble() < probFileStop)
      {
        break;
      }
      File f = new File(target, "ex_" + numFiles++);
      createRandomFile(f, r, probChunkStop);
    }
    if (maxDepth <= 1)
    {
      return;
    }
    for (;;)
    {
      if (r.nextDouble() < probDirStop)
      {
        break;
      }
      File f = new File(target, "ex_" + numFiles++);
      createRandomDirectory(maxDepth - 1, probFileStop, probDirStop,
        probChunkStop, r, f);
    }
  }
  // Want very structured files so differences between files are 
  // interesting
  private static final String[] FILE_CHUNKS =
  {
    "123456789",
    "123456",
    "78912345",
    "11111111111111111111",
    "22222222222222222222222222222222222"
  };
  private static void createRandomFile(File f, Random r,
    double probChunkStop) throws IOException
  {
    PrintWriter pw = new PrintWriter(new FileWriter(f));
    for (;;)
    {
      if (r.nextDouble() < probChunkStop)
      {
        break;
      }
      pw.println(FILE_CHUNKS[r.nextInt(FILE_CHUNKS.length)]);
    }
    pw.close();
    if (pw.checkError())
    {
      throw new IOException("Could not write to " + f.getPath());
    }
  }
  public static boolean compareItems(File a, File b) throws IOException
  {
    boolean isFilea = a.isFile();
    boolean isFileb = b.isFile();
    if (isFilea != isFileb)
    {
      throw new IOException("File type mismatch in " + a.getPath() +
        " and " + b.getPath());
      // return false;
    }
    if (isFilea)
    {
      if (a.length() != b.length())
      {
	throw new IOException("File length mismatch in " +
	  a.getPath() + " and " + b.getPath());
        // return false;
      }
      Closer c = new Closer();
      try
      {
        BufferedReader fra = new BufferedReader(new FileReader(a));
	c.addReader(fra, a.getPath());
	BufferedReader frb = new BufferedReader(new FileReader(b));
	c.addReader(frb, b.getPath());
	for (;;)
	{
	  int x = fra.read();
	  int y = frb.read();
	  if (x != y)
	  {
	    throw new IOException("File data mismatch in " +
	      a.getPath() + " and " + b.getPath());
	    // return false;
	  }
	  if (x < 0)
	  {
	    return true;
	  }
	}
      }
      finally
      {
        IOException ioe = (IOException)c.close();
	if (ioe != null)
	{
	  throw ioe;
	}
      }
    }
    // Here => neither is a file
    boolean isDira = a.isDirectory();
    boolean isDirb = b.isDirectory();
    if (isDira != isDirb)
    {
      throw new IOException("Directory type mismatch in " +
        a.getPath() + " and " + b.getPath());
      // return false;
    }
    File[] af = a.listFiles();
    File[] bf = b.listFiles();
    if (af.length != bf.length)
    {
      throw new IOException("Directory size mismatch in " +
        a.getPath() + " and " + b.getPath());
      // return false;
    }
    Comparator fc = new Comparator()
    {
      public int compare(Object a, Object b)
      {
        File af = (File)a;
	File bf = (File)b;
	return af.getName().compareTo(bf.getName());
      }
    };
    Arrays.sort(af, fc);
    Arrays.sort(bf, fc);
    for (int i = 0; i < af.length; i++)
    {
      File aa = af[i];
      File bb = bf[i];
      if (!aa.getName().equals(bb.getName()))
      {
	throw new IOException("File name mismatch in " +
	  aa.getPath() + " and " + bb.getPath());
        // return false;
      }
      return compareItems(aa, bb);
    }
    return true;
  }
  /** return the number of components in the canonical path, or 0
   *  if trouble
   */
  private static int numComponents(File f)
  {
    try
    {
      String canon = f.getCanonicalPath();
      int comps = 1;
      int len = canon.length();
      for (int i = 0; i < len; i++)
      {
	if (canon.charAt(i) == File.separatorChar)
	{
	  comps++;
	}
      }
      return comps;
    }
    catch (IOException ioe)
    {
      return 0;
    }
  }
  /** Deletes a file and all its sub-files, if a directory. Actually
   *  what it does is to delete all files found from the given file
   *  with more path components, so links may fool it. Hopefully we
   *  won't run in to those here. Return true on success.
   */
  public static boolean deleteAll(File f)
  {
    if (!f.isDirectory())
    {
      System.err.println("Not a directory " + f.getPath());
      boolean result = f.delete();
      if (!result)
      {
        System.err.println("Could not delete " + f.getPath());
      }
      return result;
    }
    int inHere = numComponents(f);
    if (inHere < 1)
    {
      System.err.println("No components found in " + f.getPath());
      return false;
    }
    File[] files = f.listFiles();
    if (files == null)
    {
      files = new File[0];
    }
    for (int i = 0; i < files.length; i++)
    {
      File target = files[i];
      if (numComponents(target) <= inHere)
      {
	System.err.println("File has fewer components " +
	  target.getPath() + " here is " + f.getPath());
        continue;
      }
      if (target.isDirectory())
      {
        if (!deleteAll(target))
	{
	  return false;
	}
      }
      else
      {
	boolean success = target.delete();
	if (!success)
	{
	  System.err.println("Could not delete " + target);
	  return false;
	}
      }
    }
    boolean result = f.delete();
    if (!result)
    {
      System.err.println("Could not delete " + f.getPath());
    }
    return result;
  }
  private static void showCommand(String[] s)
  {
    System.out.print("About to run");
    for (int i = 0; i < s.length; i++)
    {
      System.out.print(" ");
      System.out.print(s[i]);
    }
    System.out.println();
  }
  private static void runTest(int maxDepth,
    double probFileStop, double probDirStop, 
    double probChunkStop, Random r, File target, boolean verbose)
      throws IOException, DirSyncInternalException,
      DirSyncUserException
  {
    // Create random directory that we will suck in
    createRandomDirectory(maxDepth + 1, probFileStop, probDirStop,
      probChunkStop, r, target);
    File syncer = new File(target, "dSource");
    String syncFile = syncer.getPath();
    File[] dirs = target.listFiles();
    String[] command;
    if (verbose)
    {
      command = new String[] {"create", syncFile};
    }
    else
    {
      command = new String[] {"quiet", "create", syncFile};
    }
    if (verbose)
    {
      showCommand(command);
    }
    DirSync.main(command);
    // suck in files
    for (int i = 0; i < dirs.length; i++)
    {
      File dir = dirs[i];
      if (!dir.isDirectory())
      {
        continue;
      }
      if (verbose)
      {
	command = new String[] {"syncDir", dir.getPath(), syncFile};
      }
      else
      {
	command = new String[] {"quiet", "syncDir", dir.getPath(), syncFile};
      }
      if (verbose)
      {
	showCommand(command);
      }
      DirSync.main(command);
    }
    // Now extract and check
    int version = 2;
    File backFile = new File(target, "back");
    String back = backFile.getPath();
    for (int i = 0; i < dirs.length; i++)
    {
      File dir = dirs[i];
      if (!dir.isDirectory())
      {
        continue;
      }
      if (verbose)
      {
	command = new String[] {"xd", syncFile,
	  Integer.toString(version), back};
      }
      else
      {
	command = new String[] {"quiet", "xd", syncFile,
	  Integer.toString(version), back};
      }
      if (verbose)
      {
	showCommand(command);
	System.out.println("and check with " + dir.getPath());
      }
      DirSync.main(command);
      version++;
      if (!compareItems(backFile, dir))
      {
        throw new IOException("Mismatch checking version " + version);
      }
      if (verbose)
      {
	System.err.println("Before deleting backFile " +
	  backFile.exists());
      }
      if (!deleteAll(backFile))
      {
        throw new IOException("Could not delete back file");
      }
      if (verbose)
      {
	System.err.println("Deleted backFile " + backFile.exists());
      }
    }
    if (!deleteAll(target))
    {
      throw new IOException("Could not delete target");
    }
  }
  public static void main(String[] s) throws IOException,
    DirSyncInternalException, DirSyncUserException
  {
    int passes = 100;
    double probFileStop = 0.1;
    double probDirStop = 0.1;
    double probChunkStop = 0.1;
    boolean verbose = true;
    int maxDepth = 2;
    long seed = 42;
    String target = "f:\\temp\\TestDirSyncDataDir";
    int sm1 = s.length - 1;
    int argp = 0;
    boolean trouble = false;

    try
    {
      for (argp = 0; argp < s.length; argp++)
      {
	if ((argp < sm1) && ("-depth".equals(s[argp])))
	{
	  argp++;
	  maxDepth = (new Integer(s[argp].trim())).intValue();
	}
	else if ((argp < sm1) && ("-goes".equals(s[argp])))
	{
	  argp++;
	  passes = (new Integer(s[argp].trim())).intValue();
	}
	else if ((argp < sm1) && ("-pChunk".equals(s[argp])))
	{
	  argp++;
	  probChunkStop = (new Double(s[argp].trim())).doubleValue();
	}
	else if ((argp < sm1) && ("-pDir".equals(s[argp])))
	{
	  argp++;
	  probDirStop = (new Double(s[argp].trim())).doubleValue();
	}
	else if ((argp < sm1) && ("-pFile".equals(s[argp])))
	{
	  argp++;
	  probFileStop = (new Double(s[argp].trim())).doubleValue();
	}
	else if ("-quiet".equals(s[argp]))
	{
	  verbose = false;
	}
	else if ((argp < sm1) && ("-seed".equals(s[argp])))
	{
	  argp++;
	  seed = (new Long(s[argp].trim())).longValue();
	}
	else if ((argp < sm1) && ("-target".equals(s[argp])))
	{
	  argp++;
	  target = s[argp];
	}
	else
	{
	  System.err.println("Could not handle flag " + s[argp]);
	  trouble = true;
	}
      }
    }
    catch (NumberFormatException nfe)
    {
      System.err.println("Could not read number in " + s[argp]);
      trouble = true;
    }

    if (trouble)
    {
      System.err.println("Args are [-depth #] [-goes #] [-pChunk #] " +
        "[-pDir #] [-pFile #] [-quiet] [-seed #] [-target <directory>]");
      System.exit(1);
    }

    System.out.println("Max depth is " + maxDepth);
    System.out.println("Number of passes is " + passes);
    System.out.println("Chunk stop prob is " + probChunkStop);
    System.out.println("Dir stop prob is " + probDirStop);
    System.out.println("File stop prob is " + probFileStop);
    System.out.println("Random seed is " + seed);
    System.out.println("Target directory is " + target);
    System.out.println("Verbose is " + verbose);

    for (int i = 0; i < passes; i++, seed++)
    {
      Random r = new Random(seed);
      System.err.println("Seed is " + seed);
      runTest(maxDepth, probFileStop, probDirStop, probChunkStop,
        r, new File(target), verbose);
    }
  }
}
