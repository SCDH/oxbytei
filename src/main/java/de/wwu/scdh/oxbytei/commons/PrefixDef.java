package de.wwu.scdh.oxbytei.commons;

import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorElement;

public class PrefixDef {
    private final String matchPattern;
    private final String replacementPattern;
    private final String ident;

    public PrefixDef(String mp, String rp, String id) {
	this.matchPattern = mp;
	this.replacementPattern = rp;
	this.ident = id;
    }

    public PrefixDef(AuthorElement prefixDef) {
	AttrValue attr;
	attr = prefixDef.getAttribute("replacementPattern");
	if (attr != null) {
	    this.replacementPattern = attr.getValue();
	} else {
	    this.replacementPattern = null;
	}
	attr = prefixDef.getAttribute("matchPattern");
	if (attr != null) {
	    this.matchPattern = attr.getValue();
	} else {
	    this.matchPattern = null;
	}
	attr = prefixDef.getAttribute("ident");
	if (attr != null) {
	    this.ident = attr.getValue();
	} else {
	    this.ident = null;
	}
    }

    public String getMatchPattern() {
	return matchPattern;
    }

    public String getReplacementPattern() {
	return replacementPattern;
    }

    public String getIdent() {
	return ident;
    }
}
