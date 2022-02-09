<?xml version="1.0" encoding="UTF-8"?>
<!-- Extract the referenced text

You can override the processing of the referenced text by replacing this file
with an XML catalog.

There are 3 requirements when replacing this:

1) The transformation is done in mode 'extract'.

2) There has to be a named template called 'finalize-extracted'.
It is called after the transformation in 'extract' mode.

3) There is a named template called 'nodes-between'. See below.
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

    <!-- A named template as a convenient entry point.
        It returns the sequence of nodes between the start and end node given by IDs
        The start and end node is include in the sequence -->
    <xsl:template name="nodes-between" as="node()*">
        <xsl:param name="startNodeId" as="xs:string"/>
        <xsl:param name="endNodeId" as="xs:string"/>
        <xsl:variable name="startNode" select="//*[@xml:id eq $startNodeId]"/>
        <xsl:variable name="endNode" select="//*[@xml:id eq $endNodeId]"/>
        <!-- we make a sequence of the start node's childs and the following nodes etc. before the intersection
            in order to include the demarking nodes -->
        <xsl:apply-templates mode="extract"
            select="$startNode/(following::text() | following::*[empty(child::node())]) intersect $endNode/(preceding::text() | preceding::*[empty(child::node())])"
        />
    </xsl:template>

    <xsl:mode name="extract" on-no-match="shallow-copy"/>

    <!-- do not reproduce processing instructions -->
    <xsl:template match="processing-instruction()"/>

    <!-- do not reproduce apparatus entries if using an other variant encoding than parallel segementation -->
    <xsl:template match="app[//variantEncoding/@method ne 'parallel-segmentation']"/>
    <xsl:template
        match="text()[ancestor::app][//variantEncoding/@method ne 'parallel-segmentation']"/>

    <!-- do not reproduce variant readings (in parallel segmentation) -->
    <xsl:template match="rdg"/>
    <xsl:template match="text()[ancestor::rdg]"/>

    <!-- do not reproduce both of choice/(sic|corr) -->
    <xsl:template match="choice[child::sic and child::corr]">
        <xsl:apply-templates select="corr"/>
    </xsl:template>

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
