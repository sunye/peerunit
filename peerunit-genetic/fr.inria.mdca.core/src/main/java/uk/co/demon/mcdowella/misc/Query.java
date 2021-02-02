package uk.co.demon.mcdowella.misc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

/** This is a test/utility class to submit a JDBC query and
 *  print out the result. Example use:
 <code>
E:\\web\\uk\\co\\demon\\mcdowella\\misc>java uk.co.demon.mcdowella.misc.Query -db jdbc:odbc:test -query "insert into Test(Test1) values('abc')"
Update count is 1

E:\\web\\uk\\co\\demon\\mcdowella\\misc>java uk.co.demon.mcdowella.misc.Query -db jdbc:odbc:test -query "select * from test"
2 columns as follows
Test1, testKey
Data follows
one, 1
two, 2
three, 3
</code>
 */
public class Query
{
  public static void main(String[] s) throws Exception
  {
    String query = null;
    String db = null;
    String user = null;
    String password = null;
    String driver = "sun.jdbc.odbc.JdbcOdbcDriver";
    int s1 = s.length - 1;
    boolean trouble = false;
    for (int argc = 0; argc < s.length; argc++)
    {
      if ((argc < s1) && "-db".equals(s[argc]))
      {
        db = s[++argc];
      }
      else if ((argc < s1) && "-driver".equals(s[argc]))
      {
        driver = s[++argc];
      }
      else if ((argc < s1) && "-password".equals(s[argc]))
      {
        password = s[++argc];
      }
      else if ((argc < s1) && "-query".equals(s[argc]))
      {
        query = s[++argc];
      }
      else if ((argc < s1) && "-user".equals(s[argc]))
      {
        user = s[++argc];
      }
      else
      {
        System.err.println("Could not handle flag " + s[argc]);
	trouble = true;
      }
    }
    if (db == null)
    {
      System.err.println("No database argument given");
      trouble = true;
    }
    if (query == null)
    {
      System.err.println("no query given");
      trouble = true;
    }
    if (trouble)
    {
      System.err.println("Args are -db <string> [-driver <string>] " +
        "[-password <string>] [-query <string>] [-user <string>]");
      return;
    }
    Class driverClass = Class.forName(driver);
    Connection con = null;
    Statement st = null;
    Exception exFirst = null;
    try
    {
      con = DriverManager.getConnection(db, user, password);
      st = con.createStatement();
      boolean resultSet = st.execute(query);
      for (;;)
      {
	if (resultSet)
	{
	  ResultSet rs = st.getResultSet();
	  ResultSetMetaData rsmd = rs.getMetaData();
	  int columns = rsmd.getColumnCount();
	  System.out.println(columns + " columns as follows");
	  for (int i = 1; i <= columns; i++)
	  {
	    System.out.print(rsmd.getColumnName(i));
	    if (i == columns)
	    {
	      System.out.println();
	    }
	    else
	    {
	      System.out.print(", ");
	    }
	  }
	  System.out.println("Data follows");
	  while (rs.next())
	  {
	    for (int i = 1; i <= columns; i++)
	    {
	      System.out.print(rs.getObject(i));
	      if (i == columns)
	      {
	        System.out.println();
	      }
	      else
	      {
	        System.out.print(", ");
	      }
	    }
	  }
	}
	else
	{
	  int count = st.getUpdateCount();
	  if (count == -1)
	  {
	    break;
	  }
	  System.out.println("Update count is " + count);
	}
	resultSet = st.getMoreResults();
      }
    }
    catch (Exception ex)
    {
      exFirst = ex;
    }
    // With at least access, need to close down statement to
    // get any update to persist - and good practice anyway
    try
    {
      if (st != null)
      {
	st.close();
      }
    }
    catch (Exception ex2)
    {
      if (exFirst == null)
      {
	exFirst = ex2;
      }
    }
    try
    {
      if (con != null)
      {
	con.close();
      }
    }
    catch (Exception ex3)
    {
      if (exFirst == null)
      {
	exFirst = ex3;
      }
    }
    if (exFirst != null)
    {
      throw(exFirst);
    }
  }
}
