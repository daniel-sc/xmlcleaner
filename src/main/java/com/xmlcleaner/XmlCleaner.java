package com.xmlcleaner;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;

import com.beust.jcommander.Parameter;

import de.pdark.decentxml.Document;
import de.pdark.decentxml.Element;
import de.pdark.decentxml.XMLParser;
import de.pdark.decentxml.XMLWriter;

public class XmlCleaner {

	@Parameter(description = "files/directories")
	private List<String> files = new ArrayList<>();

	@Parameter(names = "--help", help = true, description = "Display usage.")
	public boolean help = false;

	@Parameter(names = { "--attribute", "-a" }, description = "XML attribute name.")
	private String attribute = "rev";

	@Parameter(names = { "--matcher", "-m" }, description = "Attribute value matcher (regex).")
	private String attributeValueExpression = "1[45]\\.\\d+(?:\\.\\d+)?d.*";

	private static final Logger LOG = Logger.getLogger(XmlCleaner.class.getName());

	protected void cleanFile(File file) throws IOException {
		Document doc = XMLParser.parse(file);
		Set<Element> toRemove = new HashSet<>();
		StreamSupport.stream(Spliterators.spliteratorUnknownSize(doc.iterator(), Spliterator.ORDERED), false)
				.filter(x -> x instanceof Element).map(e -> (Element) e)
				.filter(el -> el.getAttribute(attribute) != null).forEach(node -> {
					String rev = node.getAttributeValue(attribute);
					if (rev.matches(attributeValueExpression)) {
						toRemove.add(node);
					}
				});

		toRemove.forEach(n -> n.remove());
		if (!toRemove.isEmpty()) {
			LOG.info("removing " + toRemove.size() + " elements from " + file);
			Writer fileWriter = new FileWriter(file);
			try (XMLWriter writer = new XMLWriter(fileWriter)) {
				doc.toXML(writer);
			}
		}
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
