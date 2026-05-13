<?xml version="1.0" encoding="UTF-8"?>
<!-- Generate labelled entries from a prefix definition.
This stylesheet contains the generic parts.
-->
<xsl:package name="http://scdh.wwu.de/oxbytei/xsl/entries.xsl"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" package-version="1.0.0"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions"
    xmlns:obt="http://scdh.wwu.de/oxbytei" xmlns:map="http://www.w3.org/2005/xpath-functions/map"
    exclude-result-prefixes="#all" xpath-default-namespace="http://www.tei-c.org/ns/1.0"
    version="3.0">

    <!-- the currently edited document,
        parameter needed for de.wwu.scdh.teilsp.extension.LabelledEntriesXSLTWithContext -->
    <xsl:param name="document" as="document-node()" select="doc($testfile)"/>

    <!-- the current editing context as XPath,
        parameter needed for de.wwu.scdh.teilsp.extension.LabelledEntriesXSLTWithContext -->
    <xsl:param name="context" as="xs:string" select="'/*'"/>

    <!-- Not used with the plugin, only used for testing because we like a default value 
        for $document because it is difficult to pass in a document node. -->
    <xsl:param name="testfile" as="xs:string" select="'test.tei.xml'" required="false"/>

    <!-- the prefix for the output keys -->
    <xsl:param name="prefix" as="xs:string" required="true"/>

    <xsl:param name="from-context-function-name" as="xs:string" select="'fn:id#1'"/>

    <xsl:variable name="obt:from-context-function" as="function (node()) as node()"
        select="function-lookup(xs:QName($from-context-function-name), 1)" visibility="public"/>

    <!-- get the context node by evaluating the XPath passed in as $context -->
    <xsl:variable name="obt:context-node" as="node()" visibility="final">
        <xsl:evaluate as="node()" context-item="$document" expand-text="true" xpath="$context"
            use-when="element-available('xsl:evaluate')"/>
        <xsl:sequence select="$document" use-when="not(element-available('xsl:evaluate'))"/>
    </xsl:variable>

    <xsl:template name="obt:generate-entries" as="map(xs:string, xs:string)*" visibility="public">
        <xsl:apply-templates select="$obt:from-context-function($obt:context-node)"
            mode="generate-entries"/>
    </xsl:template>

    <xsl:mode name="generate-entries" on-no-match="shallow-skip" visibility="public"/>

    <xsl:template name="debug" visibility="final">
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


    <!-- functions to be used as obt:from-context-function -->

    <!-- for prefixDef processing, needs to be evaluated partially -->
    <xsl:function name="obt:from-prefixDef" as="node()" visibility="final">
        <xsl:param name="context" as="node()"/>
        <xsl:param name="extract-reference-xpath" as="xs:string"/>
        <!-- get the prefixDef node by its prefix -->
        <xsl:variable name="prefixDefNode" as="node()*"
            select="$context/ancestor-or-self::TEI/teiHeader//prefixDef[@ident eq $prefix]"/>
        <xsl:variable name="reference" as="xs:string">
            <xsl:evaluate as="xs:string" context-item="$prefixDefNode/@replacementPattern"
                expand-text="true" xpath="$extract-reference-xpath"
                use-when="element-available('xsl:evaluate')"/>
            <xsl:value-of select="tokenize($prefixDefNode/@replacementPattern, '#')[1]"
                use-when="not(element-available('xsl:evaluate'))"/>
        </xsl:variable>
        <xsl:variable name="absolute-reference" as="xs:anyURI">
            <xsl:choose>
                <xsl:when
                    test="$prefixDefNode/@xml:base or $prefixDefNode/@relativeFrom eq 'definition'">
                    <xsl:sequence select="resolve-uri($reference, base-uri($prefixDefNode))"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:sequence select="resolve-uri($reference, base-uri($context))"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:if test="not(doc-available($absolute-reference))">
            <xsl:message>Document not found: <xsl:value-of select="$absolute-reference"
                /></xsl:message>
        </xsl:if>
        <xsl:sequence select="doc($absolute-reference)"/>
    </xsl:function>

</xsl:package>
