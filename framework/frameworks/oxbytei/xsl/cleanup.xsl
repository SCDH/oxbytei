<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:obt="http://scdh.wwu.de/oxbytei"
    xmlns:oxy="http://www.oxygenxml.com/ns/author/xpath-extension-functions"
    xpath-default-namespace="http://www.tei-c.org/ns/1.0" exclude-result-prefixes="#all"
    version="3.0">

    <xsl:param name="template-id" as="xs:string" select="'TEMPLATE'"/>

    <xsl:param name="entry-id" as="xs:string?" required="1"/>

    <xsl:variable name="w3c-date" as="xs:string" select="'^\d\d\d\d(-\d\d(-\d\d)?)?$'"/>

    <xsl:mode on-no-match="shallow-copy"/>

    <xsl:template match="/" use-when="function-available('oxy:current-element', 0)">
        <xsl:variable name="current-id" as="xs:string"
            select="oxy:current-element() => generate-id()"/>
        <xsl:variable name="entry" as="element()" select="id($entry-id)"/>
        <xsl:message>
            <xsl:text>cleaning up from </xsl:text>
            <xsl:value-of select="oxy:current-element() => name()"/>
            <xsl:text> (</xsl:text>
            <xsl:value-of select="$current-id"/>
            <xsl:text>) entry with ID </xsl:text>
            <xsl:value-of select="$entry/@xml:id"/>
        </xsl:message>
        <xsl:apply-templates select="$entry"/>
    </xsl:template>

    <xsl:function name="obt:get-template" as="element()">
        <xsl:param name="context" as="node()"/>
        <xsl:sequence select="root($context)//id($template-id)"/>
    </xsl:function>

    <xsl:template name="notify">
        <xsl:message>
            <xsl:text>removing </xsl:text>
            <xsl:value-of select="local-name(.)"/>
            <xsl:text> from </xsl:text>
            <xsl:value-of select="ancestor::person/@xml:id"/>
            <xsl:text>   </xsl:text>
            <xsl:value-of select="serialize(.)"/>
        </xsl:message>
    </xsl:template>


    <!-- remove top-level properties un-changed from template -->
    <xsl:template match="
            node()[let $this := .
            return
                some $p in obt:get-template($this)/node()
                    satisfies deep-equal($p, $this)]">
        <xsl:call-template name="notify"/>
    </xsl:template>


    <!-- remove un-changed parts of top-level properties -->

    <xsl:template match="persName/*[normalize-space(.) eq '']">
        <xsl:call-template name="notify"/>
    </xsl:template>


    <!-- structural changes -->

    <!-- rewrites years like 1266/7 to custom attribute -->
    <xsl:template
        match="(@when | @notBefore | @notAfter | @from | @to)[matches(., $w3c-date) => not()]">
        <xsl:attribute name="{local-name(.)}-custom" select="."/>
    </xsl:template>

</xsl:stylesheet>
