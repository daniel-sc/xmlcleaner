# XmlCleaner
Cleans XML files (in place), by removing all elements (and their children), that have a specific attribute with a value matching the given pattern.

Formatting of the files is kept/unchanged.

## Build

	mvn clean install

## Usage

	Usage: java -jar xmlcleaner-*-jar-with-dependencies.jar [options] files/directories
	  Options:
	    --attribute, -a
	      XML attribute name.
	      Default: rev
	    --attribute-only, -ao
	      Remove only matched attribute (instead of complete node/element).
	      Default: false
	    --help
	      Display usage.
	    --matcher, -m
	      Attribute value matcher (regex).
	      Default: 1[45]\.\d+(?:\.\d+)?d.*



## Known issues

1. Might remove last `<li>` without removing parent `<ul>`. Results in invalid XML.
2. Might remove child objects, where the parent element is not valid (w.r.t XML schema) without this child. This is the generalization of #1.