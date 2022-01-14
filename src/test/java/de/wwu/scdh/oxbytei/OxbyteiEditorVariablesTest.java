package de.wwu.scdh.oxbytei;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.regex.Matcher;


public class OxbyteiEditorVariablesTest {

    @Test
    public void configPatternTest() {
	String testString = "${teilspProp(asdf.cl)}";
	Matcher matcher = OxbyteiEditorVariables.CONFIG_PROPERTY_PATTERN.matcher(testString);
	assertEquals(true, matcher.matches());
	assertEquals("asdf.cl", matcher.group(1));
    }

    @Test
    public void startAnchorIdTest() {
	String testString = "${startAnchorId}, asdf=true";
	Matcher matcher = OxbyteiEditorVariables.START_ANCHOR_ID_PATTERN.matcher(testString);
	assertEquals(true, matcher.find());
    }

    @Test
    public void endAnchorIdTest() {
	String testString = "start=${endAnchorId},end=afj";
	Matcher matcher = OxbyteiEditorVariables.END_ANCHOR_ID_PATTERN.matcher(testString);
	assertEquals(true, matcher.find());
    }

    @Test
    public void anchorsContainerTest() {
	String testString = "container=${anchorsContainer}";
	Matcher matcher = OxbyteiEditorVariables.ANCHORS_CONTAINER_PATTERN.matcher(testString);
	assertEquals(true, matcher.find());
    }

}
