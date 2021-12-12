/**
 * de.wwu.scdh.teilsp.xml.NamespaceContextImpl is an implementation of
 * javax.xml.namespace.NamespaceContext
 *
 */
package de.wwu.scdh.teilsp.xml;

import javax.xml.namespace.NamespaceContext;
import java.util.Iterator;

//import ro.sync.ecss.images.xmlimages.NamespaceConstants;

public class NamespaceContextImpl implements NamespaceContext {

    private String[][] namespaces;

    /**
     * Constructor that splits the input string on spaces and splits
     * the parts at the first colon into a pair of prefix and
     * namespace URI.
     * @param namespaceDef
     */
    public NamespaceContextImpl(String namespaceDef) {
	if (namespaceDef == null || namespaceDef == "") {
	    namespaces = null;
	} else {
	    String[] defs = namespaceDef.split(" ");
	    int i, k, l;
	    l = defs.length;
	    namespaces = new String[defs.length][2];
	    for (i = 0; i < l; i++) {
		k = defs[i].indexOf(":");
		if (k == -1) {
		    // no ':' found, then empty prefix (default namespace)
		    namespaces[i][0] = "";
		    namespaces[i][1] = defs[i];
		} else {
		    namespaces[i][0] = defs[i].substring(0, k);
		    namespaces[i][1] = defs[i].substring(k + 1);
		}
	    }
	}
    }

    /**
     * @see javax.xml.namespace.NamespaceContext.getPrefix()
     *
     */
    public String getPrefix(String namespaceURI) {
	String prefix = null;
	if (namespaces == null) {
	    System.out.println("NULL");
	    return prefix;
	} else {
	    int i;
	    for (i = 0; i < namespaces.length; i++) {
		if (namespaces[i][1].equals(namespaceURI)) {
		    prefix = namespaces[i][0];
		    break;
		}
	    }
	    return prefix;
	}
    }

    /**
     * @see javax.xml.namespace.NamespaceContext.getNamespaceURI()
     *
     */
    public String getNamespaceURI(String prefix) {
	String namespaceURI = null;
	if (namespaces == null) {
	    return namespaceURI;
	} else {
	    int i;
	    for (i = 0; i < namespaces.length; i++) {
		if (namespaces[i][0].equals(prefix)) {
		    namespaceURI = namespaces[i][1];
		    break;
		}
	    }
	    return namespaceURI;
	}
    }
    
    /**
     * @see javax.xml.namespace.NamespaceContext.getPrefixes()
     * TODO: This is not yes implemented!
     */
    public Iterator<String> getPrefixes(String namespaceURI) {
	return null;
    }
    
    /**
     * returns the count of definitions in the namespace context. This
     * is not part of the interface.
     *
     */
    public int getLength() {
	if (namespaces == null) {
	    return 0;
	} else {
	    return namespaces.length;
	}
    }
}
