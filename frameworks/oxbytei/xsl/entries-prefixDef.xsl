<?xml version="1.0" encoding="UTF-8"?>
<!-- Generate labelled entries from a prefix definition.
This stylesheet contains the generic parts.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:obt="http://scdh.wwu.de/oxbytei"
    xmlns:map="http://www.w3.org/2005/xpath-functions/map" exclude-result-prefixes="xs map obt"
    xpath-default-namespace="http://www.tei-c.org/ns/1.0" version="3.0">

    <!-- the currently edited document,
        parameter needed for de.wwu.scdh.teilsp.extension.LabelledEntriesXSLTWithContext -->
    <xsl:param name="document" as="document-node()" select="doc($testfile)"/>

    <!-- the current editing context as XPath,
        parameter needed for de.wwu.scdh.teilsp.extension.LabelledEntriesXSLTWithContext -->
    <xsl:param name="context" as="xs:string" select="'/*'"/>

    <!-- Not used with the plugin, only used for testing because we like a default value 
        for $document because it is difficult to pass in a document node. -->
    <xsl:param name="testfile" as="xs:string" select="'test.tei.xml'" required="false"/>

    <!-- the prefix/identifier of the local URI scheme -->
    <xsl:param name="prefix" as="xs:string" required="true"/>

    <!-- the position of the prefixDef with that identifier. There may be several. Defaults to the first. -->
    <xsl:param name="number" as="xs:integer" required="false" select="1"/>

    <xsl:param name="extract-reference-xpath" as="xs:string">
        <xsl:text>tokenize(., '#')[1]</xsl:text>
    </xsl:param>

    <!-- get the context node. This is generic. -->
    <xsl:variable name="context-node" as="node()">
        <xsl:evaluate as="node()" context-item="$document" expand-text="true" xpath="$context"
            use-when="element-available('xsl:evaluate')"/>
        <xsl:sequence select="$document//prefixDef[@ident eq $prefix][1]"
            use-when="not(element-available('xsl:evaluate'))"/>
    </xsl:variable>

    <xsl:template name="obt:generate-entries" as="map(xs:string, xs:string)*">
        <!-- get the prefixDef node by its prefix -->
        <xsl:variable name="prefixDefNode" as="node()*"
            select="$context-node/ancestor-or-self::TEI/teiHeader//prefixDef[@ident eq $prefix]"/>
        <xsl:variable name="reference" as="xs:string">
            <xsl:evaluate as="xs:string" context-item="$prefixDefNode/@replacementPattern"
                expand-text="true" xpath="$extract-reference-xpath"
                use-when="element-available('xsl:evaluate')"/>
            <xsl:value-of select="tokenize($prefixDefNode/@replacementPattern, '#')[1]"
                use-when="not(element-available('xsl:evaluate'))"/>
        </xsl:variable>
        <xsl:variable name="absolute-reference">
            <xsl:choose>
                <xsl:when
                    test="$prefixDefNode/@xml:base or $prefixDefNode/@relativeFrom eq 'definition'">
                    <xsl:value-of select="resolve-uri($reference, base-uri($prefixDefNode))"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="resolve-uri($reference, base-uri($context-node))"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:if test="not(doc-available($absolute-reference))">
            <xsl:message>Document not found: <xsl:value-of select="$absolute-reference"
                /></xsl:message>
        </xsl:if>
        <xsl:apply-templates select="doc($absolute-reference)" mode="generate-entries"/>
    </xsl:template>

    <xsl:mode name="generate-entries" on-no-match="shallow-skip"/>

    <xsl:template name="debug">
        <xsl:variable as="map(xs:string, xs:string)" name="entries">
            <xsl:call-template name="obt:generate-entries"/>
        </xsl:variable>
        <entries>
            <xsl:for-each select="$entries">
                <entry>
                    <key>
                        <xsl:value-of select="map:get(., 'key')"/>
                    </key>
                    <label>
                        <xsl:value-of select="map:get(., 'label')"/>
                    </label>
                </entry>
            </xsl:for-each>
        </entries>
    </xsl:template>

</xsl:stylesheet>
