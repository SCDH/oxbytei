package de.wwu.scdh.teilsp.config;

/**
 * {@link EditorVariablesExpander} is an interface for a utility, that
 * is able to expand editor variables like oXygen's editor variables.
 */
public interface EditorVariablesExpander  {

    /**
     * Expand an oXygen-like editor variable.
     * @param unexpanded the unexpanded string
     * @returns the expanded string
     */
    public String expand(String unexpanded);

}
