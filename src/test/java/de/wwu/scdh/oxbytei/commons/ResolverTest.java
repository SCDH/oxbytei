package de.wwu.scdh.oxbytei.commons;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.net.MalformedURLException;


public class ResolverTest {

    @Test
    public void testAbsoluteLink()
	throws MalformedURLException, NullPointerException {
	assertEquals("file:/here/we/go", Resolver.resolve(null, "/here/we/go"));
	assertEquals("file:~/here/we/go", Resolver.resolve(null, "~/here/we/go"));
    }

    @Test
    public void testURI()
	throws MalformedURLException, NullPointerException {
	assertEquals("file:/here/we/go", Resolver.resolve(null, "file:/here/we/go"));
    }
}
