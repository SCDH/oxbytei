<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:oxy="http://www.oxygenxml.com/ns/author/xpath-extension-functions"
    xmlns="http://www.tei-c.org/ns/1.0" exclude-result-prefixes="xs oxy" version="3.0"
    xpath-default-namespace="http://www.tei-c.org/ns/1.0">

    <xsl:mode on-no-match="shallow-copy"/>

    <xsl:include href="extract-referenced.xsl"/>

    <xsl:template match="lem">
        <xsl:variable name="extracted">
            <xsl:apply-templates mode="extract" select="."/>
        </xsl:variable>
        <lem>
            <xsl:call-template name="finalize-extracted">
                <xsl:with-param name="extracted" select="$extracted"/>
            </xsl:call-template>
            <!--xsl:value-of select="normalize-space(.)"/-->
        </lem>
    </xsl:template>

    <xsl:template match="rdg">
        <rdg>
            <xsl:text>${caret}</xsl:text>
            <xsl:apply-templates/>
        </rdg>
    </xsl:template>

</xsl:stylesheet>
