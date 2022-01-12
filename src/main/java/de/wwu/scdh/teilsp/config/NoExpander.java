package de.wwu.scdh.teilsp.config;

/**
 * A very simple implementation of EditorVariablesExpander, that does
 * not expand a string at all.
 */
public class NoExpander implements EditorVariablesExpander {

    public String expand(String unexpanded) {
	return unexpanded;
    }

}
