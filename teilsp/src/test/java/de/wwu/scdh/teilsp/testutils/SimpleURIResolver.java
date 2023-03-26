package de.wwu.scdh.teilsp.testutils;

import java.net.URI;
import java.net.URISyntaxException;

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
     * Resolve based on {@link URI#resolve(String)}.
     */
    public Source resolve(String href, String base) throws TransformerException {
	URI baseUri;
	try {
	    baseUri = new URI(base);
	} catch (URISyntaxException e) {
	    throw new TransformerException(e);
	}
	URI resolved = baseUri.resolve(href);
	InputSource resultSource = new InputSource(resolved.toString());
	SAXSource saxSource = new SAXSource(resultSource);
	return (Source) saxSource;
    }

}
