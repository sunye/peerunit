package uk.co.demon.mcdowella.misc;

import java.io.IOException;

public interface ButtonBearerFactory
{
  /** Create a button bearer to show info to the user */
  ButtonBearer createButtonBearer();
  /** close log files and releases any other resources */
  void close() throws IOException;
  /** set font size */
  void setFontSize(float size);
}
