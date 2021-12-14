package de.wwu.scdh.teilsp.xml;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
import static org.junit.jupiter.api.Assertions.*;

import javax.xml.namespace.NamespaceContext;

public class NamespaceContextImplTest {

    @Test
    void testMultipleFromString() {
	NamespaceContext ctx =
	    new NamespaceContextImpl("tei:http://www.tei-c.org/ns/1.0 rng:http://relaxng.org/ns/structure/1.0");
	//assertEquals(2, ctx.getLength());
	assertEquals(null, ctx.getPrefix("tei"));
	assertEquals("tei", ctx.getPrefix("http://www.tei-c.org/ns/1.0"));
	assertEquals("http://www.tei-c.org/ns/1.0", ctx.getNamespaceURI("tei"));
   }

    @Test
    void testMultipleFromStringWithDefault() {
	NamespaceContext ctx =
	    new NamespaceContextImpl("tei:http://www.tei-c.org/ns/1.0 :http://sample.org");
	assertEquals("tei", ctx.getPrefix("http://www.tei-c.org/ns/1.0"));
	assertEquals("http://www.tei-c.org/ns/1.0", ctx.getNamespaceURI("tei"));
	assertEquals("", ctx.getPrefix("http://sample.org"));
	assertEquals("http://sample.org", ctx.getNamespaceURI(""));
    }

    @Test
    void testNoColon() {
	NamespaceContext ctx = new NamespaceContextImpl("hello");
	assertEquals("", ctx.getPrefix("hello"));
	assertEquals("hello", ctx.getNamespaceURI(""));
    }
    

    @Test
    void testEmpty() {
	NamespaceContext ctx = new NamespaceContextImpl("");
	assertEquals(null, ctx.getNamespaceURI("tei"));
	assertEquals(null, ctx.getNamespaceURI(""));
    }
	
    @Test
    void testNull() {
	NamespaceContext ctx = new NamespaceContextImpl(null);
	assertEquals(null, ctx.getNamespaceURI("tei"));
	assertEquals(null, ctx.getNamespaceURI(""));
    }
	
    
}
