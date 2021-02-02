package uk.co.demon.mcdowella.filesync;

import java.util.ArrayList;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.io.IOException;
import java.io.PrintWriter;

/** TransformBuilder that stores its instructions in a file. I believe
 *  this format to be independent of the endian-ness of the machine.
*/

public class FileTransformBuilder implements TransformBuilder,
  Closer.Closeable
{
  private BufferedOutputStream os;
  private IOException savedException;
  public FileTransformBuilder(File filename) throws IOException
  {
    os = new BufferedOutputStream(new FileOutputStream(filename));
  }
  public void close() throws IOException
  {
    if (os != null)
    {
      os.close();
      os = null;
    }
    IOException e = savedException;
    savedException = null;
    if (e != null)
    {
      throw e;
    }
  }
  /**
   * Give number of bytes needed to hold l, allowing sign
   * extension when going from shorter l to full 64-bit l.
   */
  private static int encodeLen(long l)
  {
    if (l == 0)
    {
      return 0;
    }
    int result = 8;
    for (int x = 7; x >= 0; x--)
    {
      int shift = x * 8;
      // System.out.println((l << shift) >> shift);
      if (((l << shift) >> shift) == l)
      {
	result = 8 - x;
        break;
      }
    }
    // System.out.println("Length for " + l + " is " + result);
    return result;
  }
  private void writeEncodedLong(long l) throws IOException
  {
    int x = encodeLen(l);
    os.write(x);
    for (int shift = x * 8 - 8; shift >= 0; shift -= 8)
    {
      os.write((byte)(l >> shift));
    }
  }
  private static long readEncodedLong(InputStream is) throws IOException
  {
    int x = is.read();
    if (x < 0)
    {
      throw new IOException("EOF reading encoded long");
    }
    if (x > 8)
    {
      throw new IOException("Bad length code in encoded long");
    }
    // System.err.println("Length of long is " + x);
    long l = 0;
    for (int i = 0; i < x; i++)
    {
      long xd = is.read();
      if (xd == -1)
      {
        throw new IOException("EOF reading encoded length");
      }
      l += ((xd & 0xff) << (56 - 8 * i));
    }
    long result = l >> ((8 - x) * 8);
    return result;
  }
  private static final int COPY_CODE = 1;
  private static final int SEEK_CODE = 2;
  private static final int INSERT_CODE = 3;
  public void addCopy(long numBytes)
  {
    try
    {
      os.write(COPY_CODE);
      writeEncodedLong(numBytes);
    }
    catch (IOException e)
    {
      savedException = e;
    }
  }
  public double copyCost(long numBytes)
  {
    return 2 + encodeLen(numBytes);
  }
  public void addSeek(long relativeOffset)
  {
    try
    {
      os.write(SEEK_CODE);
      writeEncodedLong(relativeOffset);
    }
    catch (IOException e)
    {
      savedException = e;
    }
  }
  public double seekCost(long relativeOffset)
  {
    return 2 + encodeLen(relativeOffset);
  }
  public void addInsert(byte[] data)
  {
    try
    {
      os.write(INSERT_CODE);
      writeEncodedLong(data.length);
      os.write(data);
    }
    catch (IOException e)
    {
      savedException = e;
    }
  }
  public double insertCost(int numBytes)
  {
    return 2 + encodeLen(numBytes) + numBytes;
  }
  /** Read an encoded file and apply the transforms requested to t */
  public static void readControl(File control, Transform t)
    throws IOException
  {
    Closer closer = new Closer();
    try
    {
      BufferedInputStream cs = new BufferedInputStream(
        new FileInputStream(control));
      closer.addInputStream(cs, control.getPath());
      for (;;)
      {
        int x = cs.read();
	switch (x)
	{
	  case -1: // EOF
	  return;
	  case COPY_CODE:
	  {
	    long len = readEncodedLong(cs);
	    if (len < 0)
	    {
	      throw new IOException("-ve length in copy code");
	    }
	    t.addCopy(len);
	  }
	  break;
	  case SEEK_CODE:
	  {
	    long relativeOffset = readEncodedLong(cs);
	    t.addSeek(relativeOffset);
	  }
	  break;
	  case INSERT_CODE:
	  {
	    long len = readEncodedLong(cs);
	    if (len < 0)
	    {
	      throw new IOException("-ve length in insert code");
	    }
	    byte[] info = new byte[(int)len];
	    for (int offset = 0; offset < info.length; )
	    {
	      int got = cs.read(info, offset, info.length - offset);
	      if (got <= 0)
	      {
	        throw new IOException("EOF reading insert");
	      }
	      offset += got;
	    }
	    t.addInsert(info);
	  }
	  break;
	  default:
	    throw new IOException("bad code in readControl");
	}
      }
    }
    finally
    {
      closer.close();
    }
  }
  private static class ApplyTransformToFile implements Transform
  {
    private RAFileWrapper sw;
    private BufferedOutputStream ts;
    private long filePos = 0;
    private IOException error;
    private Closer closer = new Closer();
    ApplyTransformToFile(File source, File target)
      throws IOException
    {
      sw = new RAFileWrapper(source);
      closer.addCloseable(sw, source.getPath());
      /*
      // Allow this so we can create a temporary file and use it as
      // a transform target
      if (target.exists())
      {
        throw new IOException("Target file exists");
      }
      */
      ts = new BufferedOutputStream(
        new FileOutputStream(target));
      closer.addOutputStream(ts, target.getPath());
    }
    public void addCopy(long numBytes) throws IOException
    {
      for (long i = 0; i < numBytes; i++)
      {
	ts.write(sw.readByte());
      }
      filePos += numBytes;
    }
    public void addSeek(long relativeOffset) throws IOException
    {
      filePos += relativeOffset;
      sw.seek(filePos);
    }
    public void addInsert(byte[] data) throws IOException
    {
      for (int i = 0; i < data.length; i++)
      {
	ts.write(data[i]);
      }
    }
    public void close() throws IOException
    {
      IOException ioe = (IOException) closer.close();
      if (ioe != null)
      {
        throw ioe;
      }
    }
  }
  public static void applyTransform(File control, File source,
    File target) throws IOException
  {
    ApplyTransformToFile atf = 
      new ApplyTransformToFile(source, target);
    try
    {
      readControl(control, atf);
    }
    finally
    {
      atf.close();
    }
  }
  private static class ShowTransform implements Transform
  {
    private PrintWriter pw;
    ShowTransform(PrintWriter p)
    {
      pw = p;
    }
    public void addCopy(long numBytes)
    {
      pw.println("Copy " +  numBytes + " bytes");
    }
    public void addSeek(long relativeOffset)
    {
      pw.println("Relative Seek of " + relativeOffset + " bytes");
    }
    public void addInsert(byte[] data)
    {
      pw.println("Insert " + data.length + " bytes as follows");
      for (int i = 0; i < data.length; i++)
      {
	pw.write((char)data[i]);
      }
      pw.println();
    }
  }
  /** Show commands in transform */
  public static void showTransform(File control,
    PrintWriter pw) throws IOException
  {
    ShowTransform st = new ShowTransform(pw);
    readControl(control, st);
  }
  private static class CheckTransformer implements Transform
  {
    private RAFileWrapper source;
    private RAFileWrapper target;
    private File sf;
    private File tf;
    private Closer closer = new Closer();
    private long sourceFilePos = 0;
    private long checked = 0;
    private boolean notCopyTransforms = false;
    private boolean lengthsMatched;
    private long numCopyTransforms = 0;
    CheckTransformer(File sourceFile, File targetFile)
      throws IOException
    {
      sf = sourceFile;
      tf = targetFile;
      source = new RAFileWrapper(sourceFile);
      closer.addCloseable(source, sourceFile.getPath());
      target = new RAFileWrapper(targetFile);
      closer.addCloseable(target, targetFile.getPath());
      lengthsMatched = (sourceFile.length() == targetFile.length());
    }
    public void addCopy(long numBytes) throws IOException
    {
      for (long i = 0; i < numBytes; i++)
      {
	if (source.readByte() != target.readByte())
	{
	  throw new IOException("Mismatch during copy");
	}
      }
      if (sourceFilePos == 0)
      {
        numCopyTransforms++;
      }
      sourceFilePos += numBytes;
      checked += numBytes;
    }
    public void addSeek(long relativeOffset) throws IOException
    {
      notCopyTransforms = true;
      sourceFilePos += relativeOffset;
      source.seek(sourceFilePos);
    }
    public void addInsert(byte[] data) throws IOException
    {
      notCopyTransforms = true;
      for (int i = 0; i < data.length; i++)
      {
	if (target.readByte() != data[i])
	{
	  throw new IOException("Mismatch during insert");
	}
      }
      checked += data.length;
    }
    /** return true if files are copies of each other */
    public boolean close() throws IOException
    {
      if (checked != target.getLength())
      {
        throw new IOException("Not all bytes checked between " +
	  sf.getPath() + " and " + tf.getPath() + " checked " +
	  checked);
      }
      IOException ioe = (IOException)closer.close();
      if (ioe != null)
      {
        throw ioe;
      }
      return ((!notCopyTransforms) && (numCopyTransforms <= 1) &&
               lengthsMatched);
    }
  }
  /** return true if transform is just copy */
  public static boolean checkTransform(File control,
    File source, File target) throws IOException
  {
    CheckTransformer ct = new CheckTransformer(source, target);
    readControl(control, ct);
    return ct.close();
  }
  public static void main(String[] args) throws Exception
  {
    boolean trouble = false;
    PrintWriter pw = new PrintWriter(System.out, true);
    for (int i = 0; i < args.length; i++)
    {
      if ((i < (args.length - 1)) && "-show".equals(args[i]))
      {
	i++;
        showTransform(new File(args[i]), pw);
      }
      else if ((i < (args.length -3)) && "-diff".equals(args[i]))
      {
	Closer closer = new Closer();
	FileTransformBuilder builder = null;
	File sFile = new File(args[++i]);
	File tFile = new File(args[++i]);
	File cFile = new File(args[++i]);
	try
	{
	  RAFileWrapper source = new RAFileWrapper(sFile);
	  closer.addCloseable(source, sFile.getPath());
	  RAFileWrapper target = new RAFileWrapper(tFile);
	  closer.addCloseable(target, tFile.getPath());
	  if (cFile.exists())
	  {
	    throw new IOException("Control File " + cFile +
	      " already exists");
	  }
	  builder = new FileTransformBuilder(cFile);
	  closer.addCloseable(builder, cFile.getPath());
	  FileSync.createFrom(source, target, builder, 1.5, 10000);
	}
	finally
	{
	  Exception ioe = closer.close();
	  if (ioe != null)
	  {
	    throw ioe;
	  }
	}
	checkTransform(cFile, sFile, tFile);
      }
      else if ((i < (args.length -3)) && "-apply".equals(args[i]))
      {
	File cFile = new File(args[++i]);
        File sFile = new File(args[++i]);
	File tFile = new File(args[++i]);
        applyTransform(cFile, sFile, tFile);
      }
      else
      {
        System.err.println("Cannot handle flag " + args[i]);
	trouble = true;
	break;
      }
    }
    if (trouble)
    {
      System.err.println("Args are [-show <controlFile>]* " +
        "[-diff <sourceFile> <targetFile> <controlFile>]* " +
	"[-apply <controlFile> <sourceFile> <targetFile>]*");
    }
    pw.close();
    if (pw.checkError())
    {
      System.err.println("Error writing output info");
    }
  }
}
