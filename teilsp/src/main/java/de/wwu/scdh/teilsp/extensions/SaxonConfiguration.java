package de.wwu.scdh.teilsp.extensions;

import net.sf.saxon.Configuration;

/**
 * Encapsulation of configuration settings.
 */
public class SaxonConfiguration extends Configuration {

    public SaxonConfiguration() {
	super();
    }

    // Note about xsl:evaluate
    // In oxygen 23.1 this is not available, in 24.1 it is.

}
