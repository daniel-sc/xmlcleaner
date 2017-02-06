package com.xmlcleaner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.beust.jcommander.Parameter;

import de.pdark.decentxml.Document;
import de.pdark.decentxml.Element;
import de.pdark.decentxml.XMLParser;
import de.pdark.decentxml.XMLWriter;

public class XmlCleaner {

	@Parameter(description = "files/directories")
	List<String> files = new ArrayList<>();

	@Parameter(names = "--help", help = true, description = "Display usage.")
	public boolean help = false;

	@Parameter(names = { "--attribute", "-a" }, description = "XML attribute name.")
	String attributeName = "rev";

	@Parameter(names = { "--matcher", "-m" }, description = "Attribute value matcher (regex).")
	String attributeValueExpression = "1[45]\\.\\d+(?:\\.\\d+)?d.*";

	@Parameter(names = { "--attribute-only",
			"-ao" }, description = "Remove only matched attribute (instead of complete node/element).")
	boolean onlyRemoveAttribute = false;

	private static final Logger LOG = Logger.getLogger(XmlCleaner.class.getName());

	protected void cleanFile(File file) throws IOException {
		Document doc = XMLParser.parse(file);
		boolean changed = cleanDoc(doc);
		if (changed) {
			Charset charset = doc.getEncoding() != null ? Charset.forName(doc.getEncoding()) : Charset.defaultCharset();
			Writer fileWriter = new OutputStreamWriter(new FileOutputStream(file), charset);
			LOG.info("Updating: " + file + " (charset=" + charset + ")");
			try (XMLWriter writer = new XMLWriter(fileWriter)) {
				doc.toXML(writer);
			}
		}
	}

	/**
	 * 
	 * @param doc
	 *            will be updated!
	 * @return <code>true</code> if changed
	 */
	protected boolean cleanDoc(Document doc) {

		Set<Element> matchedElements = StreamSupport
				.stream(Spliterators.spliteratorUnknownSize(doc.iterator(), Spliterator.ORDERED), false)
				.filter(x -> x instanceof Element).map(e -> (Element) e)
				.filter(el -> el.getAttribute(attributeName) != null)
				.filter(el -> el.getAttributeValue(attributeName).matches(attributeValueExpression))
				.collect(Collectors.toSet());

		if (onlyRemoveAttribute) {
			matchedElements.forEach(e -> e.removeAttribute(attributeName));
		} else {
			matchedElements.forEach(e -> e.remove());
		}
		LOG.info("Found " + matchedElements.size() + " matches");
		return !matchedElements.isEmpty();
	}

	public void clean() throws IOException {

		for (String file : files) {
			Path start = Paths.get(file);
			LOG.info("starting with: " + start);
			Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if (file.toString().endsWith(".xml")) {
						cleanFile(file.toFile());
					}
					return super.visitFile(file, attrs);
				}
			});
		}

		LOG.info("done.");
	}

}
