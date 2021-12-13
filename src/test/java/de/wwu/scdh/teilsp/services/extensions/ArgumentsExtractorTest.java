package de.wwu.scdh.teilsp.services.extensions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;


class ArgumentsExtractorTest {


    @Test
    public void testCorrectSingle() throws Exception {
	Map<String, String> args = ArgumentsExtractor.arguments("namespace=\"mailto:me\"");
	assertEquals(1, args.size());
	assertEquals("mailto:me", args.get("namespace"));
    }

    @Test
    public void testCorrectMultiple() throws Exception {
	Map<String, String> args = ArgumentsExtractor.arguments("namespace=\"mailto:me\" xpath=\"a | b | c | daire\"");
	assertEquals(2, args.size());
	assertEquals("mailto:me", args.get("namespace"));
	assertEquals("a | b | c | daire", args.get("xpath"));
    }

    @Test
    public void testFalseNoValue() throws Exception {
	assertThrows(Exception.class,
		     () -> {ArgumentsExtractor.arguments("namespace");});
    }

    @Test
    public void testFalseNoName() throws Exception {
	assertThrows(Exception.class,
		     () -> {ArgumentsExtractor.arguments("=namespace");});
    }

    @Test
    public void testEmpty() throws Exception {
	Map<String, String> args = ArgumentsExtractor.arguments("");
	assertEquals(0, args.size());
    }

    @Test
    public void testNull() throws Exception {
	Map<String, String> args = ArgumentsExtractor.arguments(null);
	assertEquals(0, args.size());
    }

}
