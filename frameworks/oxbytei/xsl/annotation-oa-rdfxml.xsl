<?xml version="1.0" encoding="UTF-8"?>
<!--
This can be used instead of annotation.xsl for generating annotations. It will write an RDF annotation into <xenoData>.

This script must be called on the TEI document.

USAGE:
1) copy this file into your project

2) redirect oxbytei/xsl/annotation.xsl with the copied file via an XML catalog.

3) Put something similar into your local oxbytei configuration:

<property name="oxbytei.action.annotate.targetLocation">//xenoData[1]/*[1]</property>
<property name="oxbytei.action.annotate.externalParams">reproduce-text=true,non-entity-allowed=true</property>


4) Put this into your TEI header:

<xenoData>
  <rdf:RDF
      xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" 
      xmlns:oa="http://www.w3.org/ns/oa#"
      xmlns:scdh="http://wwu.de/scdh/annotation#"
      xmlns:xs="http://www.w3.org/2001/XMLSchema">

  </rdf:RDF>
</xenoData>


5) Optionally: Put something similar into your header:

<idno type="document-identifier">IDENTIFIER</idno>

-->
<!DOCTYPE stylesheet [
    <!ENTITY oa "http://www.w3.org/ns/oa#">
]>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:oxy="http://www.oxygenxml.com/ns/author/xpath-extension-functions"
    xmlns:tei="http://www.tei-c.org/ns/1.0" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:oa="http://www.w3.org/ns/oa#" xmlns:scdh="http://wwu.de/scdh/annotation#"
    exclude-result-prefixes="oxy tei" version="3.0"
    xpath-default-namespace="http://www.tei-c.org/ns/1.0">

    <xsl:output method="xml" indent="yes"/>

    <xsl:import href="oa-rdfxml.xsl"/>

    <xsl:param name="annotation-uri" select="'http://example.org/annotation/${uuid}'" as="xs:string"
        required="no"/>

    <!--
    <xsl:param name="startId" as="xs:ID" required="yes"/>
    <xsl:param name="endId" as="xs:ID" required="yes"/>

    <xsl:param name="reproduce-text" as="xs:boolean" select="false()" required="no"/>

    <xsl:param name="wrapper" as="xs:string" select="''" required="no"/>

    <xsl:param name="insert-caret" as="xs:boolean" select="true()" required="no"/>
    -->

    <xsl:param name="debug" as="xs:boolean" select="false()" required="no"/>

    <xsl:template match="/">
        <xsl:if test="$debug">
            <xsl:message>
                <xsl:text>Adding annotation to span between #</xsl:text>
                <xsl:value-of select="$startId"/>
                <xsl:text> and #</xsl:text>
                <xsl:value-of select="$endId"/>
            </xsl:message>
        </xsl:if>
        <rdf:Description rdf:type="oa:Annotation">
            <xsl:if test="$annotation-uri ne ''">
                <xsl:attribute name="rdf:about" select="$annotation-uri"/>
            </xsl:if>
            <xsl:call-template name="oa-hasTarget">
                <xsl:with-param name="context" select="/"/>
            </xsl:call-template>
            <!--rdf:type>
                <xsl:text disable-output-escaping="yes">&lt;&oa;Annotation&gt;</xsl:text>
            </rdf:type-->
            <oa:hasBody>
                <rdf:Description>
                    <xsl:call-template name="scdh-onTextSpan">
                        <xsl:with-param name="context" select="/"/>
                    </xsl:call-template>
                </rdf:Description>
            </oa:hasBody>
        </rdf:Description>
    </xsl:template>

</xsl:stylesheet>
