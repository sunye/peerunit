package test.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;

import fr.inria.peerunit.exception.AnnotationFailure;
import fr.inria.peerunit.parser.MethodDescription;
import fr.inria.peerunit.parser.ParserImpl;

public class ParserImplTest {

	private Logger log = Logger.getLogger("test");
	ParserImpl parser = new ParserImpl(-1, log);
	TestData data = new TestData();


	public ParserImplTest() {
		log.setLevel(Level.FINEST);
	}

	@Before
	public void setUp() throws Exception {
	}


	@Test
	public void testBis() {

		List<MethodDescription> l = parser.parseBis(data.getClass());
		List<MethodDescription> m = parser.parse(data.getClass());

		assertEquals(l.size(), m.size());

		for(MethodDescription each : l) {
			assertTrue(m.contains(each));
		}

	}

	@Test
	public void testHasFailure() {
		try {
			parser.hasFailure(0, -1);
			fail("Exception not catch");
		} catch (AnnotationFailure af) {
			assertEquals(af.getLocalizedMessage(),"Annotation FROM without TO");
		}
		try {
			parser.hasFailure(-1, 0);
			fail("Exception not catch");
		} catch (AnnotationFailure af) {
			assertEquals(af.getLocalizedMessage(),"Annotation TO without FROM");
		}

		try {
			parser.hasFailure(-1, -4);
			fail("Exception not catch");
		} catch (AnnotationFailure af) {
			assertEquals(af.getLocalizedMessage(),"Invalid value for FROM / TO");
		}

		try {
			parser.hasFailure(4, 0);
			fail("Exception not catch");
		} catch (AnnotationFailure af) {
			assertEquals(af.getLocalizedMessage(), "The value of FROM must be smaller than TO");
		}


	}


/*	@Test
	public void testParse() {
		List<MethodDescription> l = parser.parse(data.getClass());

		System.out.println(l.size());
		assertTrue(l.size() == 7);

		for(MethodDescription each : l) {
			log.info(each.getAnnotation() + " - " + each.getName());
			//assertEquals(each.getAnnotation(), "Test");
		}

	}*/


}
