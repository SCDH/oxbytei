package de.wwu.scdh.oxbytei.xpath;

import java.net.URL;
import java.net.MalformedURLException;

import net.sf.saxon.om.Sequence;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.trans.XPathException;


/**
 * Utilities used throughout the binding library.
 */
public class XPathUtils {

    /**
     * Cast a argument of type <code>xs:string</code> to a {@link String}.
     *
     * @param arg  the argument as {@link Sequence}
     * @return a {@link String}
     */
    public static String getStringArgument(Sequence arg) throws XPathException {
        StringValue inputValue = (StringValue) arg.materialize();
        return inputValue.getStringValue();
    }

    /**
     * Cast a argument of type <code>xs:string</code> to a {@link URL}.
     *
     * @param arg  the argument as {@link Sequence}
     * @return a {@link URL}
     */
    public static URL getUrlArgument(Sequence arg) throws XPathException {
        StringValue inputValue = (StringValue) arg.materialize();
	try {
	    return new URL(inputValue.getStringValue());
	} catch (MalformedURLException e) {
	    throw new XPathException(e);
	}
    }

}
