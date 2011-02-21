package uk.co.demon.mcdowella.filesync;

public class DirSyncInternalException extends Exception
{
  private Throwable cause;
  public Throwable getCause()
  {
    return cause;
  }
  DirSyncInternalException(String message, Throwable because)
  {
    super(message);
    cause = because;
  }
  DirSyncInternalException(String message)
  {
    this(message, null);
  }
}
