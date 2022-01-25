<?xml version="1.0" encoding="UTF-8"?>
<!-- append an annotation on a text range to an RDF/XML file -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:tei="http://www.tei-c.org/ns/1.0"
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:oa="http://www.w3.org/ns/oa#"
    xmlns:scdh="http://wwu.de/scdh/annotation#" exclude-result-prefixes="tei" version="2.0">

    <xsl:output method="xml" indent="yes"/>

    <xsl:import href="oa-rdfxml.xsl"/><!-- has more parameters! -->

    <!-- the path to the annotated document -->
    <xsl:param name="annotated-doc" as="xs:string" required="yes"/>

    <!-- the URI for the annotation -->
    <xsl:param name="annotation-uri" as="xs:string" required="no"/>

    <!-- whether to insert the DOCTYPE declaration -->
    <xsl:param name="insert-doctype" as="xs:boolean" select="true()" required="no"/>

    <!-- the name of the file with ENTITIES with document identifiers -->
    <xsl:param name="docids" as="xs:string" select="'docids.ent'" required="no"/>

    <xsl:template match="/processing-instruction()">
        <xsl:copy select="."/>
    </xsl:template>

    <xsl:template match="/rdf:RDF">

        <!-- insert the DOCTYPE declaration with the link to the external entity file -->
        <xsl:if test="$insert-doctype">
            <xsl:text disable-output-escaping="yes">&lt;DOCTYPE RDF [</xsl:text>
            <xsl:text disable-output-escaping="yes">&#xa;&lt;!ENTITY % docids SYSTEM "</xsl:text>
            <xsl:value-of select="$docids"/>
            <xsl:text disable-output-escaping="yes">" &gt;&#xa;%docids;</xsl:text>
            <xsl:text disable-output-escaping="yes">&#xa;]&gt;</xsl:text>
        </xsl:if>

        <rdf:RDF>
            <!-- deep copy of all nodes -->
            <xsl:copy-of select="@*"/>
            <xsl:copy-of select="*"/>

            <xsl:text>&#xa;&#xa;</xsl:text>

            <!-- append new range annotation as last child -->
            <rdf:Description>
                <xsl:variable name="annotated" select="doc($annotated-doc)"/>
                <xsl:if test="$annotation-uri ne ''">
                    <xsl:attribute name="rdf:about" select="$annotation-uri"/>
                </xsl:if>
                <xsl:call-template name="oa-hasSource">
                    <xsl:with-param name="context" select="$annotated"/>
                </xsl:call-template>
                <xsl:call-template name="oa-hasRange-anchorsRange"/>
                <xsl:call-template name="scdh-onTextSpan">
                    <xsl:with-param name="context" select="$annotated"/>
                </xsl:call-template>
            </rdf:Description>

            <xsl:text>&#xa;&#xa;</xsl:text>

        </rdf:RDF>
    </xsl:template>

</xsl:stylesheet>
