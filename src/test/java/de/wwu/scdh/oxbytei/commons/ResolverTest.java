package de.wwu.scdh.oxbytei.commons;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import static org.junit.jupiter.api.Assertions.*;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import javax.xml.transform.TransformerException;


public class ResolverTest {

    @Test
    @Disabled("")
    public void testAbsoluteLink()
	throws MalformedURLException, NullPointerException, TransformerException, URISyntaxException {
	assertEquals("file:/here/we/go", Resolver.resolve(null, "/here/we/go"));
	assertEquals("file:~/here/we/go", Resolver.resolve(null, "~/here/we/go"));
    }

    @Test
    @Disabled("")
    public void testURI()
	throws MalformedURLException, NullPointerException, TransformerException, URISyntaxException {
	assertEquals("file:/here/we/go", Resolver.resolve(null, "file:/here/we/go"));
    }
}
