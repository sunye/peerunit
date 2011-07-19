package uk.co.demon.mcdowella.filesync;

public class FileSyncException extends Exception
{
  private Throwable cause;
  public Throwable getCause()
  {
    return cause;
  }
  FileSyncException(String message, Throwable because)
  {
    super(message);
    cause = because;
  }
}
