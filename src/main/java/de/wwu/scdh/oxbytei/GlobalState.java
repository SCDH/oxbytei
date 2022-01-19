package de.wwu.scdh.oxbytei;

/**
 * A static class for storing the framework's state.
 */
public class GlobalState {

    /**
     * set by {@link SurroundWithAnchorsOperation}
     */
    public static String startAnchorId = null;

    /**
     * set by {@link SurroundWithAnchorsOperation}
     */
    public static String endAnchorId = null;

    /**
     * set by {@link SurroundWithAnchorsOperation}
     */
    public static String anchorsContainer = null;

    /**
     * set by {@link SelectLabelledEntryInteraction}
     */
    public static String selection = null;

}
