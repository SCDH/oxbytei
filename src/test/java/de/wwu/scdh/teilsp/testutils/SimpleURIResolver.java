package de.wwu.scdh.teilsp.testutils;

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.transform.URIResolver;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.InputSource;

/**
 * A simple URI resolver that matches the demands of unit tests.
 *
 */
public class SimpleURIResolver implements URIResolver {

    /**
     * We only resolve files in the same directory.
     */
    public Source resolve(String href, String base) throws TransformerException {
	Path basePath = Paths.get(base);
	String resultString = basePath.resolveSibling(href).toString();
	InputSource resultSource = new InputSource(resultString);
	SAXSource saxSource = new SAXSource(resultSource);
	return (Source) saxSource;
    }
    
}
