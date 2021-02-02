package uk.co.demon.mcdowella.filesync;

import java.util.ArrayList;
import java.io.InputStream;
import java.io.IOException;
import java.util.List;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

public class Closer
{
  private List toClose = new ArrayList();
  private interface CloserCommand
  {
    void close() throws Exception;
    String getMessage();
  }
  public interface Closeable
  {
    void close() throws Exception;
  }
  public Exception close()
  {
    Exception saved = null;
    for (int i = toClose.size() - 1; i >= 0; i--)
    {
      CloserCommand cmd = (CloserCommand)toClose.get(i);
      try
      {
        cmd.close();
      }
      catch (Exception e)
      {
	if (saved == null)
	{
	  saved = e;
	}
      }
    }
    toClose.clear();
    return saved;
  }
  public void addInputStream(final InputStream is, final String message)
  {
    toClose.add(new CloserCommand()
    {
      public void close() throws IOException
      {
        is.close();
      }
      public String getMessage()
      {
        return message;
      }
    });
  }
  public void addOutputStream(final OutputStream is,
    final String message)
  {
    toClose.add(new CloserCommand()
    {
      public void close() throws IOException
      {
        is.close();
      }
      public String getMessage()
      {
        return message;
      }
    });
  }
  public void addWriter(final Writer w,
    final String message)
  {
    toClose.add(new CloserCommand()
    {
      public void close() throws IOException
      {
        w.close();
      }
      public String getMessage()
      {
        return message;
      }
    });
  }
  public void addReader(final Reader r,
    final String message)
  {
    toClose.add(new CloserCommand()
    {
      public void close() throws IOException
      {
        r.close();
      }
      public String getMessage()
      {
        return message;
      }
    });
  }
  public void addCloseable(final Closeable raf,
    final String message)
  {
    toClose.add(new CloserCommand()
    {
      public void close() throws Exception
      {
        raf.close();
      }
      public String getMessage()
      {
        return message;
      }
    });
  }
}
