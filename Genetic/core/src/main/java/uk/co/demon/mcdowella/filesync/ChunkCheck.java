package uk.co.demon.mcdowella.filesync;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

/** Class to check code by generating two random files and trying
 *  to convert one to another
 */
public class ChunkCheck
{
  public static void main(String[] args) throws Exception
  {
    int len = 10000;
    int bufSize = 10000;
    File oldFile = new File("oldFile.check");
    File newFile = new File("newFile.check");
    File controlFile = new File("control.check");
    long seed = 42;
    boolean trouble = false;
    int as1 = args.length - 1;
    int argp = 0;
    int passes = 1;
    try
    {
      for (; argp < args.length; argp++)
      {
	if ((argp < as1) && "-bufSize".equals(args[argp]))
	{
	  argp++;
	  bufSize = Integer.parseInt(args[argp].trim());
	}
	else if ((argp < as1) && "-len".equals(args[argp]))
	{
	  argp++;
	  len = Integer.parseInt(args[argp].trim());
	}
	else if ((argp < as1) && "-passes".equals(args[argp]))
	{
	  argp++;
	  passes = Integer.parseInt(args[argp].trim());
	}
	else if ((argp < as1) && "-seed".equals(args[argp]))
	{
	  argp++;
	  seed = (new Long(args[argp].trim())).longValue();
	}
	else if ((argp < as1) && "-oldFile".equals(args[argp]))
	{
	  argp++;
	  oldFile = new File(args[argp]);
	}
	else if ((argp < as1) && "-newFile".equals(args[argp]))
	{
	  argp++;
	  newFile = new File(args[argp]);
	}
	else if ((argp < as1) && "-controlFile".equals(args[argp]))
	{
	  argp++;
	  controlFile = new File(args[argp]);
	}
	else
	{
	  System.err.println("Cannot handle arg " + args[argp]);
	  trouble = true;
	}
      }
    }
    catch (NumberFormatException nfe)
    {
      System.err.println("Cannot read number in " + args[argp]);
      trouble  = true;
    }

    if (trouble)
    {
      System.err.println("Args are [-bufSize #] [-len #] [-seed #] " +
        "[-newFile <file>] [-oldFile <file>] [-controlFile <file>]" +
	" [-passes #]");
      return;
    }

    System.out.println("Len is " + len);
    System.out.println("BufSize is " + bufSize);
    System.out.println("Seed is " + seed);
    System.out.println("Old file is " + oldFile);
    System.out.println("New file is " + newFile);
    System.out.println("Control file is " + controlFile);
    System.out.println("Passes " + passes);

    byte[] data = new byte[len];
    for (int pass = 0; pass < passes; pass++)
    {
      Random r = new Random(seed + pass);
      for (int i = 0; i < data.length; i++)
      {
	data[i] = (byte)r.nextInt();
      }
      if (oldFile.exists())
      {
	throw new IOException("File " + oldFile + " already exists");
      }
      OutputStream os = new BufferedOutputStream(
	new FileOutputStream(oldFile));
      os.write(data);
      int firstTarget = r.nextInt(data.length);
      int firstLen = r.nextInt(data.length - firstTarget);
      int firstSource = r.nextInt(data.length);
      if ((firstLen + firstSource) > data.length)
      {
	firstLen = data.length - firstSource;
      }
      System.arraycopy(data, firstSource, data, firstTarget, firstLen);
      if (newFile.exists())
      {
	throw new IOException("File " + newFile + " already exists");
      }
      os.close();
      os = new BufferedOutputStream(new FileOutputStream(newFile));
      os.write(data);
      os.close();
      RAFileWrapper source = new RAFileWrapper(oldFile);
      RAFileWrapper target = new RAFileWrapper(newFile);
      if (controlFile.exists())
      {
	throw new IOException("Control File " + controlFile +
	  " already exists");
      }
      FileTransformBuilder builder =
	new FileTransformBuilder(controlFile);
      FileSync.createFrom(source, target, builder, 1.5, bufSize);
      source.close();
      target.close();
      builder.close();
      FileTransformBuilder.checkTransform(controlFile, oldFile, newFile);
      if (pass < (passes - 1))
      {
	boolean delTrouble = false;
        if (!oldFile.delete())
	{
	  System.err.println("Could not delete old file " + oldFile);
	  delTrouble = true;
	}
        if (!newFile.delete())
	{
	  System.err.println("Could not delete new file " + newFile);
	  delTrouble = true;
	}
	System.out.println("Control file length is " +
	  controlFile.length());
        if (!controlFile.delete())
	{
	  System.err.println("Could not delete " + controlFile);
	  delTrouble = true;
	}
      }
    }
  }
}
