<?xml version="1.0" encoding="UTF-8"?>
<!-- XSLT library for generating RDF/XML annotations. -->
<!DOCTYPE stylesheet [
    <!ENTITY oa "http://www.w3.org/ns/oa#">
]>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:oa="http://www.w3.org/ns/oa#"
    xmlns:obt="http://scdh.wwu.de/oxbytei" xmlns:scdh="http://wwu.de/scdh/annotation#"
    exclude-result-prefixes="obt" version="3.0">

    <xsl:output method="xml" indent="yes"/>

    <xsl:import href="rdf-docid.xsl"/>

    <!-- the xml:id of the start anchor -->
    <xsl:param name="startId" as="xs:ID" required="yes"/>

    <!-- the xml:id of the end anchor -->
    <xsl:param name="endId" as="xs:ID" required="yes"/>

    <!-- whether to reproduce the annotated text -->
    <xsl:param name="reproduce-text" as="xs:boolean" select="false()" required="no"/>

    <!-- the element (predicate) to which the reproduced text goes to -->
    <xsl:param name="wrapper" as="xs:string" select="''" required="no"/>

    <xsl:include href="extract-referenced.xsl"/>

    <!-- Generates oa:hasTarget 
        @param context   the annotated TEI document -->
    <xsl:template name="oa-hasTarget">
        <xsl:param name="context" as="node()*"/>
        <oa:hasTarget>
            <rdf:Description>
                <xsl:call-template name="oa-hasSource">
                    <xsl:with-param name="context" select="$context"/>
                </xsl:call-template>
                <xsl:call-template name="oa-hasRange-anchorsRange"/>
            </rdf:Description>
        </oa:hasTarget>
    </xsl:template>

    <!-- Generates an oa:hasSource.
        @param context   the annotated TEI document
    -->
    <xsl:template name="oa-hasSource">
        <xsl:param name="context" as="node()*"/>
        <oa:hasSource>
            <xsl:call-template name="obt:docid">
                <xsl:with-param name="filename" select="base-uri($context)"/>
                <xsl:with-param name="is-default" select="false()"/>
            </xsl:call-template>
        </oa:hasSource>
    </xsl:template>

    <!-- Generates a oa:hasRange with two XPathSelectors that point to the anchors. -->
    <xsl:template name="oa-hasRange-anchorsRange">
        <oa:hasSelector>
            <rdf:Description rdf:type="&oa;RangeSelector">
                <oa:hasStartSelector>
                    <rdf:Description rdf:type="&oa;XPathSelector">
                        <xsl:attribute name="rdf:value">
                            <xsl:text>//*[@xml:id='</xsl:text>
                            <xsl:value-of select="$startId"/>
                            <xsl:text>']</xsl:text>
                        </xsl:attribute>
                    </rdf:Description>
                </oa:hasStartSelector>
                <oa:hasEndSelector>
                    <rdf:Description rdf:type="&oa;XPathSelector">
                        <xsl:attribute name="rdf:value">
                            <xsl:text>//*[@xml:id='</xsl:text>
                            <xsl:value-of select="$endId"/>
                            <xsl:text>']</xsl:text>
                        </xsl:attribute>
                    </rdf:Description>
                </oa:hasEndSelector>
            </rdf:Description>
        </oa:hasSelector>
    </xsl:template>

    <!-- Generates a predication scdh:onTextSpan or the like.
        @param context   the annotated document
        
        Note, that this is not oa:TextSelector, since nodes may be stripped from the annotated span.
        extract-referenced.xsl is used to make the span of text.
    -->
    <xsl:template name="scdh-onTextSpan">
        <xsl:param name="context" as="node()*"/>
        <xsl:choose>
            <xsl:when test="$wrapper eq '' and $reproduce-text">
                <scdh:onTextSpan>
                    <xsl:call-template name="xml-lang">
                        <xsl:with-param name="context" select="$context//*[@xml:id eq $startId]"/>
                    </xsl:call-template>
                    <xsl:call-template name="scdh-onTextSpan-text">
                        <xsl:with-param name="context" select="$context"/>
                    </xsl:call-template>
                </scdh:onTextSpan>
            </xsl:when>
            <xsl:when test="$reproduce-text">
                <xsl:element name="{$wrapper}">
                    <xsl:call-template name="xml-lang">
                        <xsl:with-param name="context" select="$context//*[@xml:id eq $startId]"/>
                    </xsl:call-template>
                    <xsl:call-template name="scdh-onTextSpan-text">
                        <xsl:with-param name="context" select="$context"/>
                    </xsl:call-template>
                </xsl:element>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <!-- get the text extracted from between the anchors
        @param context   the annotated document -->
    <xsl:template name="scdh-onTextSpan-text">
        <xsl:param name="context" as="node()*"/>
        <xsl:variable name="extracted">
            <xsl:apply-templates mode="extract"
                select="$context//*[@xml:id eq $startId]/following::node() intersect $context//*[@xml:id eq $endId]/preceding::node()"
            />
        </xsl:variable>
        <!-- coming from finalized, the text may still contain element nodes.
            So we store it and then push it to normlized-text() -->
        <xsl:variable name="finalized">
            <xsl:call-template name="finalize-extracted">
                <xsl:with-param name="extracted" select="$extracted"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:value-of select="normalize-space($finalized)"/>
    </xsl:template>

    <!-- make an @xml:lang for the context item
        @param context    the node to get the language for -->
    <xsl:template name="xml-lang">
        <xsl:param name="context" as="node()*"/>
        <xsl:variable name="lang" select="$context/ancestor-or-self::*[@xml:lang][1]/@xml:lang"/>
        <xsl:if test="exists($lang) and $lang ne ''">
            <xsl:attribute name="xml:lang" select="$lang"/>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>
