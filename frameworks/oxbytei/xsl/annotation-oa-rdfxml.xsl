<?xml version="1.0" encoding="UTF-8"?>
<!-- This make an interpretative annotation on markup.annotation linke described in the guidelines:
https://www.tei-c.org/release/doc/tei-p5-doc/de/html/AI.html#AISP

The caret will be on the inserted <span>.
-->
<!DOCTYPE stylesheet [
    <!ENTITY oa "http://www.w3.org/ns/oa#">
]>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:oxy="http://www.oxygenxml.com/ns/author/xpath-extension-functions"
    xmlns:tei="http://www.tei-c.org/ns/1.0" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:oa="http://www.w3.org/ns/oa#" xmlns:scdh="http://wwu.de/scdh/annotation"
    exclude-result-prefixes="xs oxy" version="2.0"
    xpath-default-namespace="http://www.tei-c.org/ns/1.0">

    <xsl:param name="startId" as="xs:ID" required="yes"/>
    <xsl:param name="endId" as="xs:ID" required="yes"/>

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
            <oa:hasSource>
                <xsl:apply-templates select="/" mode="canonical-url"/>
            </oa:hasSource>
            <oa:hasSelector>
                <rdf:Description rdf:type="&oa;rangeSelector">
                    <oa:hasStartSelector>
                        <rdf:Description rdf:type="&oa;XPathSelector">
                            <xsl:attribute name="rdf:value">
                                <xsl:text>//*[@xml:id='</xsl:text>
                                <xsl:value-of select="$startId"/>
                                <xsl:text>']</xsl:text>
                            </xsl:attribute>
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
                    </oa:hasStartSelector>
                </rdf:Description>
            </oa:hasSelector>
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
                    <scdh:onTextSpan>
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
                    </scdh:onTextSpan>
                </xsl:when>
            </xsl:choose>
        </rdf:Description>
    </xsl:template>

    <xsl:template mode="canonical-urlOFF" match="/|*">
        <xsl:value-of
            select="concat('&#x26;', //teiHeader//idno[@type eq 'document-identifier'], ';')"
            disable-output-escaping="yes"/>
    </xsl:template>

    <xsl:template mode="canonical-url" match="/|*">
        <xsl:value-of
            select="//teiHeader//idno[@type eq 'document-identifier']"
            disable-output-escaping="yes"/>
    </xsl:template>

</xsl:stylesheet>
