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

}
