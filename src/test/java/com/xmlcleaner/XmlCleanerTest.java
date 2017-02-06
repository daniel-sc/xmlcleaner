package com.xmlcleaner;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.pdark.decentxml.Document;
import de.pdark.decentxml.XMLParser;

public class XmlCleanerTest {

	private static final String DEFAULT_XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<!DOCTYPE boschemata PUBLIC \"-//OASIS//DTD DITA Boschemata//EN\" \"http://docs.oasis-open.org/dita/v1.1/OS/dtd/boschemata.dtd\">\n";

	private static final String DEFAULT_TEST_XML = DEFAULT_XML_HEADER + "<boschemata id=\"someid\">\n"
			+ "	<title>some title</title>\n" + "	<prolog>\n" + "		<author>someone</author>\n"
			+ "		<critdates myattrname=\"testvalue\">\n" + "			<created date=\"14.07.2020\"/>\n"
			+ "			<revised modified=\"04.09.2023\"/>\n" + "		</critdates>\n" + "	</prolog></boschemata>";

	XmlCleaner xmlCleaner;

	@Before
	public void setUp() throws Exception {
		xmlCleaner = new XmlCleaner();
		xmlCleaner.attributeName = "myattrname";
		xmlCleaner.attributeValueExpression = "testvalue";
	}

	@Test
	public void testCleanDocRemoveElement() {
		Document doc = XMLParser.parse(DEFAULT_TEST_XML);
		xmlCleaner.cleanDoc(doc);

		String result = doc.toXML();
		assertEquals(DEFAULT_XML_HEADER + "<boschemata id=\"someid\">\n" + "	<title>some title</title>\n"
				+ "	<prolog>\n" + "		<author>someone</author>\n" + "		\n" + "	</prolog></boschemata>", result);
	}

	@Test
	public void testCleanDocRemoveAttribute() {
		Document doc = XMLParser.parse(DEFAULT_TEST_XML);
		xmlCleaner.onlyRemoveAttribute = true;
		xmlCleaner.cleanDoc(doc);

		String result = doc.toXML();
		assertEquals(DEFAULT_XML_HEADER + "<boschemata id=\"someid\">\n" + "	<title>some title</title>\n"
				+ "	<prolog>\n" + "		<author>someone</author>\n" + "		<critdates>\n"
				+ "			<created date=\"14.07.2020\"/>\n" + "			<revised modified=\"04.09.2023\"/>\n"
				+ "		</critdates>\n" + "	</prolog></boschemata>", result);
	}

	@Test
	public void testCleanDocNoChangeUnknownAttr() {
		Document doc = XMLParser.parse(DEFAULT_TEST_XML);
		xmlCleaner.attributeName = "unknownattr";
		xmlCleaner.attributeValueExpression = ".*";

		boolean result = xmlCleaner.cleanDoc(doc);
		Assert.assertFalse(result);
	}

	@Test
	public void testCleanDocNoChangeNoMatch() {
		Document doc = XMLParser.parse(DEFAULT_TEST_XML);
		xmlCleaner.attributeName = "date";
		xmlCleaner.attributeValueExpression = "no-match";

		boolean result = xmlCleaner.cleanDoc(doc);
		Assert.assertFalse(result);
	}

}
