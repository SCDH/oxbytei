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
    public void testRealWorld() throws Exception {
	Map<String, String> args = ArgumentsExtractor.arguments("selection=//t:text//t:place key=@xml:id label=\"normalize-space(descendant-or-self::t:persName)\" namespaces=\"t:http://www.tei-c.org/ns/1.0 xml:http://www.w3.org/XML/1998/namespace\"");
	assertEquals(4, args.size());
	assertEquals("//t:text//t:place", args.get("selection"));
	assertEquals("@xml:id", args.get("key"));
	assertEquals("normalize-space(descendant-or-self::t:persName)", args.get("label"));
	assertEquals("t:http://www.tei-c.org/ns/1.0 xml:http://www.w3.org/XML/1998/namespace", args.get("namespaces"));
    }

    @Test
    public void testRealWorldWhitespace() throws Exception {
	Map<String, String> args = ArgumentsExtractor.arguments(" \n\n\t selection=//t:text//t:place \nkey=@xml:id\n    label=\"normalize-space(descendant-or-self::t:persName)\"\n\tnamespaces=\"t:http://www.tei-c.org/ns/1.0 xml:http://www.w3.org/XML/1998/namespace\"\t\n");
	assertEquals(4, args.size());
	assertEquals("//t:text//t:place", args.get("selection"));
	assertEquals("@xml:id", args.get("key"));
	assertEquals("normalize-space(descendant-or-self::t:persName)", args.get("label"));
	assertEquals("t:http://www.tei-c.org/ns/1.0 xml:http://www.w3.org/XML/1998/namespace", args.get("namespaces"));
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
