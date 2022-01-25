<?xml version="1.0" encoding="UTF-8"?>
<!--
This can be used instead of annotation.xsl for generating annotations. It will write an RDF annotation into <xenoData>.

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

    <xsl:param name="startId" as="xs:ID" required="yes"/>
    <xsl:param name="endId" as="xs:ID" required="yes"/>

    <xsl:variable name="idno-attribute-default" select="'document-identifier'"/>
    <xsl:param name="idno-attribute" select="$idno-attribute-default" as="xs:string" required="no"/>

    <xsl:param name="annotation-uri" select="'http://example.org/annotation/${uuid}'" as="xs:string" required="no"/>

    <xsl:param name="non-entity-allowed" select="false()" as="xs:boolean" required="no"/>

    <xsl:param name="reproduce-text" as="xs:boolean" select="false()" required="no"/>

    <xsl:param name="wrapper" as="xs:string" select="''" required="no"/>

    <xsl:param name="insert-caret" as="xs:boolean" select="true()" required="no"/>

    <xsl:param name="debug" as="xs:boolean" select="false()" required="no"/>

    <xsl:include href="extract-referenced.xsl"/>

    <xsl:template match="/">
        <xsl:if test="$debug">
            <xsl:message>
                <xsl:text>Adding annotation to span between #</xsl:text>
                <xsl:value-of select="$startId"/>
                <xsl:text> and #</xsl:text>
                <xsl:value-of select="$endId"/>
            </xsl:message>
        </xsl:if>
        <rdf:Description>
            <xsl:if test="$annotation-uri ne ''">
                <xsl:attribute name="rdf:about" select="$annotation-uri"/>
            </xsl:if>
            <xsl:call-template name="oa-hasSource">
                <xsl:with-param name="context" select="/"/>
            </xsl:call-template>
            <xsl:call-template name="oa-hasRange-anchorsRange"/>
            <xsl:call-template name="scdh-onTextSpan">
                <xsl:with-param name="context" select="/"/>
            </xsl:call-template>
        </rdf:Description>
    </xsl:template>

    <xsl:template name="oa-hasSource">
        <xsl:param name="context" as="node()"/>
        <oa:hasSource>
            <xsl:choose>
                <!-- We make an entity name from the document identifier.
                    This is portable. But it needs the entity declared in DOCTYPE -->
                <xsl:when test="exists($context//teiHeader//idno[@type eq $idno-attribute])">
                    <xsl:variable name="identifier"
                        select="$context//teiHeader//idno[@type eq $idno-attribute]"/>
                    <xsl:value-of select="concat('&#x26;', $identifier, ';')"
                        disable-output-escaping="yes"/>
                </xsl:when>
                <xsl:when test="$non-entity-allowed">
                    <!-- We simply use the local document path. This is not portable. -->
                    <xsl:message>
                        <xsl:text>WARNING: using local path in RDF</xsl:text>
                    </xsl:message>
                    <xsl:value-of select="base-uri($context)"/>
                </xsl:when>
                <xsl:otherwise>
                    <!-- Terminate with an error message. -->
                    <xsl:message terminate="yes">
                        <xsl:text>ERROR: no &lt;idno type="</xsl:text>
                        <xsl:value-of select="$idno-attribute"/>
                        <xsl:text>"&gt; provided and the parameter 'non-entity-allowed' is set to false.</xsl:text>
                    </xsl:message>
                </xsl:otherwise>
            </xsl:choose>
        </oa:hasSource>
    </xsl:template>

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

    <xsl:template name="scdh-onTextSpan">
        <xsl:param name="context"/>
        <xsl:choose>
            <xsl:when test="$wrapper eq '' and $reproduce-text">
                <scdh:onTextSpan>
                    <xsl:variable name="extracted">
                        <xsl:apply-templates mode="extract"
                            select="//*[@xml:id eq $startId]/following::node() intersect //*[@xml:id eq $endId]/preceding::node()"
                        />
                    </xsl:variable>
                    <xsl:call-template name="finalize-extracted">
                        <xsl:with-param name="extracted" select="$extracted"/>
                    </xsl:call-template>
                </scdh:onTextSpan>
            </xsl:when>
            <xsl:when test="$reproduce-text">
                <xsl:element name="{$wrapper}">
                    <xsl:variable name="extracted">
                        <xsl:apply-templates mode="extract"
                            select="//*[@xml:id eq $startId]/following::node() intersect //*[@xml:id eq $endId]/preceding::node()"
                        />
                    </xsl:variable>
                    <xsl:call-template name="finalize-extracted">
                        <xsl:with-param name="extracted" select="$extracted"/>
                    </xsl:call-template>
                </xsl:element>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
