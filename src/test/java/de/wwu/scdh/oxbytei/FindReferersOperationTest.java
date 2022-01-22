package de.wwu.scdh.oxbytei;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.regex.Matcher;


public class FindReferersOperationTest {

    @Test
    public void refererPatternTest() {
	String testString = "app, tei, @from, @to, \"substring(normalize-space(.), 50)\"";
	Matcher matcher = FindReferersOperation.REFERING_ENTRY_PATTERN.matcher(testString);
	assertEquals(true, matcher.matches());
	assertEquals("app", matcher.group("name"));
	assertEquals("tei", matcher.group("namespace"));
	assertEquals("@from", matcher.group("from"));
	assertEquals("@to", matcher.group("to"));
	assertEquals("\"substring(normalize-space(.), 50)\"", matcher.group("label"));
	Matcher labelMatcher = FindReferersOperation.XPATH_PATTERN.matcher(matcher.group("label"));
	assertEquals(true, labelMatcher.find());
	assertEquals("substring(normalize-space(.), 50)", labelMatcher.group());
    }

    //@Test
    public void refererPatternMultipleTest() {
	String testString = "//back//app, tei, @from, @to, \"substring(normalize-space(.), 50)\", app, tei, @from, self::app, \"substring(normalize-space(.), 50)\"";
	Matcher matcher = FindReferersOperation.REFERING_ENTRY_PATTERN.matcher(testString);
	assertEquals(true, matcher.find());
	assertEquals("//back//app", matcher.group("name"));
	assertEquals("tei", matcher.group("namespace"));
	assertEquals("@from", matcher.group("from"));
	assertEquals("@to", matcher.group("to"));
	assertEquals("\"substring(normalize-space(.), 50)\"", matcher.group("label"));
	Matcher labelMatcher = FindReferersOperation.XPATH_PATTERN.matcher(matcher.group("label"));
	assertEquals(true, labelMatcher.find());
	assertEquals("substring(normalize-space(.), 50)", labelMatcher.group());
	assertEquals(true, matcher.find());
	assertEquals("app", matcher.group("name"));
	assertEquals("tei", matcher.group("namespace"));
	assertEquals("@from", matcher.group("from"));
	assertEquals("self::app", matcher.group("to"));
	assertEquals("\"substring(normalize-space(.), 50)\"", matcher.group("label"));
	labelMatcher = FindReferersOperation.XPATH_PATTERN.matcher(matcher.group("label"));
	assertEquals(true, labelMatcher.find());
    }

}
