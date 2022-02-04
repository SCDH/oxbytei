<?xml version="1.0" encoding="UTF-8"?>
<!-- append an annotation on a text range to an RDF/XML file -->
<!DOCTYPE stylesheet [
    <!ENTITY oa "http://www.w3.org/ns/oa#">
]>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:tei="http://www.tei-c.org/ns/1.0"
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:oa="http://www.w3.org/ns/oa#"
    xmlns:scdh="http://wwu.de/scdh/annotation#" xmlns:obt="http://scdh.wwu.de/oxbytei"
    exclude-result-prefixes="tei obt" version="3.0">

    <xsl:output method="xml" indent="yes"/>

    <xsl:import href="oa-rdfxml.xsl"/>
    <!-- has more parameters! -->

    <!-- the path to the annotated document -->
    <xsl:param name="annotated-doc" as="xs:string" required="yes"/>

    <!-- the URI for the annotation -->
    <xsl:param name="annotation-uri" as="xs:string" required="no"/>

    <!-- whether to insert the DOCTYPE declaration -->
    <xsl:param name="insert-doctype" as="xs:boolean" select="true()" required="no"/>

    <!-- the name of the file with ENTITIES with document identifiers -->
    <xsl:param name="docids" as="xs:string" select="'docids.ent'" required="no"/>

    <!-- Whether or not the new annotation should be prepended to the existing ones.
        If false, then the new annotation will be appended. -->
    <xsl:param name="prepend" as="xs:boolean" select="true()" required="no"/>

    <xsl:param name="registry" as="xs:string" select="'registry.xml'" required="no"/>

    <xsl:variable name="reg" as="document-node()" select="doc($registry)"/>

    <xsl:mode on-no-match="shallow-copy"/>

    <xsl:template match="/processing-instruction()">
        <xsl:copy select="."/>
    </xsl:template>

    <!-- let's keep the named entities -->
    <xsl:template match="text()">
        <xsl:call-template name="obt:docid">
            <xsl:with-param name="filename" select="."/>
            <xsl:with-param name="is-default" select="true()"/>
        </xsl:call-template>
    </xsl:template>

    <!-- insert the DOCTYPE declaration with the link to the external entity file -->
    <xsl:template name="doctype-decl">
        <xsl:if test="$insert-doctype">
            <xsl:text disable-output-escaping="yes">&#xa;&lt;!DOCTYPE RDF [</xsl:text>
            <xsl:text disable-output-escaping="yes">&#xa;&lt;!ENTITY % docids SYSTEM "</xsl:text>
            <xsl:value-of select="$docids"/>
            <xsl:text disable-output-escaping="yes">" &gt;&#xa;%docids;</xsl:text>
            <xsl:text disable-output-escaping="yes">&#xa;]&gt;&#xa;</xsl:text>
        </xsl:if>
    </xsl:template>

    <xsl:template match="/rdf:RDF">
        <xsl:call-template name="doctype-decl"/>
        <rdf:RDF>

            <xsl:if test="not($prepend)">
                <!-- copy of all nodes -->
                <xsl:apply-templates/>
                <xsl:text>&#xa;&#xa;</xsl:text>
            </xsl:if>

            <!-- append new range annotation as last child -->
            <rdf:Description rdf:type="oa:Annotation">
                <xsl:variable name="annotated" select="doc($annotated-doc)"/>
                <xsl:if test="$annotation-uri ne ''">
                    <xsl:attribute name="rdf:about" select="$annotation-uri"/>
                </xsl:if>
                <xsl:call-template name="oa-hasTarget">
                    <xsl:with-param name="context" select="$annotated"/>
                </xsl:call-template>
                <oa:hasBody>
                    <rdf:Description>
                        <xsl:call-template name="scdh-onTextSpan">
                            <xsl:with-param name="context" select="$annotated"/>
                        </xsl:call-template>
                    </rdf:Description>
                </oa:hasBody>
            </rdf:Description>

            <xsl:if test="$prepend">
                <xsl:text>&#xa;&#xa;</xsl:text>
                <xsl:apply-templates/>
            </xsl:if>

        </rdf:RDF>
    </xsl:template>

</xsl:stylesheet>
