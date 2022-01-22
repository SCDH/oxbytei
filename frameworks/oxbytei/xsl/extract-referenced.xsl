<?xml version="1.0" encoding="UTF-8"?>
<!-- Extract the referenced text

You can override the processing of the referenced text by replacing this file
with an XML catalog.

There are 2 requirements when replacing this:

1) The transformation is done in mode 'extract'.

2) There has to be a named template called 'finalize-extracted'.
It is called after the transformation in 'extract' mode.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns="http://www.tei-c.org/ns/1.0"
    xpath-default-namespace="http://www.tei-c.org/ns/1.0" exclude-result-prefixes="xs" version="3.0"
    default-mode="extract">
    <!-- Note, that the default mode is 'extract'! -->
    
    <!-- A template named 'finalize-extracted' is expected to be here! -->
    <xsl:template name="finalize-extracted">
        <xsl:param name="extracted" as="node()*"/>
        <xsl:value-of select="normalize-space($extracted)"/>
    </xsl:template>

    <xsl:mode name="extract" on-no-match="shallow-copy"/>

    <!-- do not reproduce notes -->
    <xsl:template match="note"/>
    <xsl:template match="text()[ancestor::note]"/>

    <!-- replace caesura with common sign -->
    <xsl:template match="caesura">
        <xsl:text> || </xsl:text>
    </xsl:template>

    <!-- replace verse start with common sign -->
    <xsl:template match="text()[parent::l and position() eq 1]">
        <xsl:text> / </xsl:text>
        <xsl:value-of select="."/>
    </xsl:template>

</xsl:stylesheet>
