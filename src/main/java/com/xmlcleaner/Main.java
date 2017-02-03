package com.xmlcleaner;

import java.io.IOException;

import com.beust.jcommander.JCommander;

public class Main {

	public static void main(String[] args) throws IOException {

		XmlCleaner xmlCleaner = new XmlCleaner();
		JCommander jCommander = new JCommander(xmlCleaner, args);
		if (xmlCleaner.help) {
			JCommander.getConsole().println(
					"Remove XML elements from files (recursively) where given attribute name has a matching value.\n");
			jCommander.usage();
		} else {
			xmlCleaner.clean();
		}
	}

}
