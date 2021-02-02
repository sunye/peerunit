package uk.co.demon.mcdowella.misc;

import org.xml.sax.helpers.DefaultHandler;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import org.xml.sax.ErrorHandler;
import javax.xml.transform.ErrorListener;
import java.io.File;
import java.io.FileReader;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import javax.xml.transform.OutputKeys;
import org.xml.sax.SAXParseException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.XMLConstants;
import org.xml.sax.XMLReader;

/**
 * Test/utility class to parse an XML document from its input
 * stream and optionally apply an XSL transform to it
 * This is the DOM-based version. I would prefer to use SAX
 * but the DOM implementation/interface seems to lead to fewer
 * problems.
 */
public class XslC
{
  public static void main(String[] s) throws Exception
  {
    boolean validating = false;
    String xslFile = null;
    String schema = null;
    boolean trouble = false;
    int s1 = s.length - 1;
    for (int argc = 0; argc < s.length; argc++)
    {
      if ((argc < s1) && "-schema".equals(s[argc]))
      {
        schema = s[++argc];
      }
      else if ("-validate".equals(s[argc]))
      {
        validating = true;
      }
      else if ((argc < s1) && "-xsl".equals(s[argc]))
      {
        xslFile = s[++argc];
      }
      else
      {
        System.err.println("Cannot handle flag " + s[argc]);
	trouble = true;
      }
    }
    if (trouble)
    {
      System.err.println(
        "Usage is [-schema <filename>] [-validate] [-xsl <filename>]");
      return;
    }
    System.out.println("Schema " + schema + " Validating: " +
      validating + " xsl " + xslFile);
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setValidating(validating);
    DocumentBuilder db = dbf.newDocumentBuilder();
    db.setErrorHandler(new EHD());
    Document dom = db.parse(System.in);
    System.err.println("Dom = " + dom);
    if (schema != null)
    {
      SchemaFactory scf = SchemaFactory.newInstance(
        XMLConstants.W3C_XML_SCHEMA_NS_URI);
      Schema sc = scf.newSchema(new File(schema));
      sc.newValidator().validate(new DOMSource(dom));
    }
    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer trans;
    if (xslFile == null)
    {
      trans = tf.newTransformer();
    }
    else
    {
      InputSource ts = new InputSource(new FileReader(xslFile));
      trans = tf.newTransformer(new SAXSource(ts));
    }
    trans.setOutputProperty(OutputKeys.METHOD, "xml");
    trans.setOutputProperty(OutputKeys.INDENT, "yes");
    EH eh = new EH();
    tf.setErrorListener(eh);
    trans.setErrorListener(eh);
    StreamResult out = new StreamResult(System.out);
    EHD ehd = new EHD();
    trans.transform(new DOMSource(dom), out);
  }
  private static class EH implements ErrorHandler,
    ErrorListener
  {
    void showLocation()
    {
    }
    void showLocation(TransformerException te)
    {
      SourceLocator ourLoc = te.getLocator();
      if (ourLoc == null)
      {
	System.err.println("Null source location info");
        return;
      }
      System.out.println("Trouble near Line " + ourLoc.getLineNumber() +
        " column " + ourLoc.getColumnNumber() + " public id " +
	ourLoc.getPublicId() + " system id " + ourLoc.getSystemId());
    }
    public void error(SAXParseException spe) throws SAXParseException
    {
      System.err.println("SAX error " + spe.getMessage());
      showLocation();
      throw spe;
    }
    public void fatalError(SAXParseException spe) throws
      SAXParseException
    {
      System.err.println("SAX fatal error " + spe.getMessage());
      showLocation();
      throw spe;
    }
    public void warning(SAXParseException spe) throws
      SAXParseException
    {
      System.err.println("SAX warning " + spe.getMessage());
      showLocation();
      throw spe;
    }
    public void error(TransformerException te) throws TransformerException
    {
      System.err.println("Transformer error " + te.getMessage());
      showLocation(te);
      throw te;
    }
    public void fatalError(TransformerException te) throws TransformerException
    {
      System.err.println("Transformer fatal error " + te.getMessage());
      showLocation(te);
      throw te;
    }
    public void warning(TransformerException te) throws TransformerException
    {
      System.err.println("Transformer warning " + te.getMessage());
      showLocation(te);
      throw te;
    }
  }
  private static class EHD extends DefaultHandler implements ErrorHandler
  {
    private Locator loc;
    public void setDocumentLocator(Locator l)
    {
      loc = l;
      super.setDocumentLocator(l);
    }
    void showLocation()
    {
      if (loc == null)
      {
	System.err.println("Null location info");
        return;
      }
      System.out.println("Trouble near Line " + loc.getLineNumber() +
        " column " + loc.getColumnNumber() + " public id " +
	loc.getPublicId() + " system id " + loc.getSystemId());
    }
    public void error(SAXParseException spe) throws SAXParseException
    {
      System.err.println("SAX error " + spe.getMessage());
      showLocation();
      throw spe;
    }
    public void fatalError(SAXParseException spe) throws
      SAXParseException
    {
      System.err.println("SAX fatal error " + spe.getMessage());
      showLocation();
      throw spe;
    }
    public void warning(SAXParseException spe) throws
      SAXParseException
    {
      System.err.println("SAX warning " + spe.getMessage());
      showLocation();
      throw spe;
    }
  }
}
