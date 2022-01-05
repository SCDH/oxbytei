<?xml version="1.0" encoding="UTF-8"?>
<!-- This make an external double end-point attached apparatus entry as described in the guidelines:
https://www.tei-c.org/release/doc/tei-p5-doc/de/html/TC.html#TCAPDE

The caret will be on the inserted <rdg>.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:oxy="http://www.oxygenxml.com/ns/author/xpath-extension-functions"
    xmlns="http://www.tei-c.org/ns/1.0" exclude-result-prefixes="xs oxy" version="2.0"
    xpath-default-namespace="http://www.tei-c.org/ns/1.0">

    <xsl:param name="startId" as="xs:ID" required="yes"/>
    <xsl:param name="endId" as="xs:ID" required="yes"/>

    <xsl:param name="withLemma" as="xs:boolean" select="true()" required="no"/>

    <xsl:param name="debug" as="xs:boolean" select="false()" required="no"/>

    <xsl:template match="/">
        <app from="#{$startId}" to="#{$endId}">
            <xsl:if test="$withLemma">
                <lem>
                    <xsl:value-of
                        select="(//*[@xml:id eq $startId]/following::text() intersect //*[@xml:id eq $endId]/preceding::text()) => string-join('') => normalize-space()"
                    />
                </lem>
            </xsl:if>
            <rdg>${caret}</rdg>
        </app>
    </xsl:template>

</xsl:stylesheet>
