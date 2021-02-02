package uk.co.demon.mcdowella.misc;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import org.xml.sax.ErrorHandler;
import javax.xml.transform.ErrorListener;
import java.io.File;
import java.io.FileReader;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import javax.xml.transform.OutputKeys;
import org.xml.sax.SAXException;
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
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.XMLReader;

/**
 * Test/utility class to parse an XML document from its input
 * stream and optionally
 * apply an XSL transform to it
 */
public class Xsl
{
  public static void main(String[] s) throws Exception
  {
    boolean validating = false;
    String schema = null;
    String xslFile = null;
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
    SAXParserFactory spf = SAXParserFactory.newInstance();
    spf.setValidating(validating);
    // Need to set namespaces on to work with XSL
    spf.setNamespaceAware(true);
    SAXParser sp = spf.newSAXParser();
    LocatorFilter filterReader = new LocatorFilter(sp.getXMLReader());
    InputSource is = new InputSource(System.in);
    EH eh = new EH(is, filterReader);
    filterReader.setErrorHandler(eh);
    tf.setErrorListener(eh);
    trans.setErrorListener(eh);
    StreamResult out = new StreamResult(System.out);
    if (schema != null)
    {
      // Get strange IllegalStateException when we try to use
      // schema validation as part of the SAX pipeline, so build
      // a DOM tree and work from that
      DOMResult dr = new DOMResult();
      tf.newTransformer().transform(eh, dr);
      SchemaFactory scf =
	SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      Schema sc = scf.newSchema(new File(schema));
      DOMSource ds = new DOMSource(dr.getNode());
      sc.newValidator().validate(ds);
      trans.transform(ds, out);
    }
    else
    {
      trans.transform(eh, out);
    }
  }
  private static class EH extends SAXSource implements ErrorHandler,
    ErrorListener
  {
    private LocatorFilter forLoc;
    EH(InputSource is, LocatorFilter lf)
    {
      super(lf, is);
      forLoc = lf;
    }
    EH(InputSource is) throws Exception
    {
      super(is);
    }
    void showLocation()
    {
    }
    void showLocation(TransformerException te)
    {
      SourceLocator ourLoc = te.getLocator();
      if (ourLoc == null)
      {
	Locator l = null;
	if (forLoc != null)
	{
	  l = forLoc.getDocumentLocator();
	}
	if (l == null)
	{
	  System.err.println("Null source location info");
	  return;
	}
	System.out.println("Trouble near Line " + l.getLineNumber() +
	  " column " + l.getColumnNumber() + " public id " +
	  l.getPublicId() + " system id " + l.getSystemId());
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
  private static class LocatorFilter extends XMLFilterImpl
  {
    private Locator loc;
    public void setDocumentLocator(Locator l)
    {
      loc = l;
      super.setDocumentLocator(l);
    }
    Locator getDocumentLocator()
    {
      return loc;
    }
    LocatorFilter(XMLReader parent)
    {
      super(parent);
    }
    /* Can use this to peek into info going to XSL
    public void startElement(String uri, String localName, String qName,
      Attributes atts) throws SAXException
    {
      System.err.println("Got element uri " + uri + " localName " + localName +
        " qName " + qName);
      super.startElement(uri, localName, qName, atts);
    }
    */
  }
}
