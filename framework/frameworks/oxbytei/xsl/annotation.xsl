<?xml version="1.0" encoding="UTF-8"?>
<!-- This makes an interpretative annotation on markup.annotation like described in the guidelines:
https://www.tei-c.org/release/doc/tei-p5-doc/de/html/AI.html#AISP

The caret will be on the inserted <span>.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:oxy="http://www.oxygenxml.com/ns/author/xpath-extension-functions"
    xmlns="http://www.tei-c.org/ns/1.0" exclude-result-prefixes="xs oxy" version="2.0"
    xpath-default-namespace="http://www.tei-c.org/ns/1.0">

    <xsl:param name="startId" as="xs:ID" required="yes"/>
    <xsl:param name="endId" as="xs:ID" required="yes"/>

    <xsl:param name="reproduce-text" as="xs:boolean" select="false()" required="no"/>

    <xsl:param name="wrapper" as="xs:string" select="''" required="no"/>

    <xsl:param name="insert-caret" as="xs:boolean" select="true()" required="no"/>

    <xsl:param name="has-end-anchor" as="xs:boolean" select="true()" required="no"/>

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
        <span from="#{$startId}">
            <xsl:choose>
                <xsl:when test="$has-end-anchor">
                    <xsl:attribute name="to" select="concat('#', $endId)"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="xml:id" select="$endId"/>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:choose>
                <xsl:when test="$wrapper eq '' and $reproduce-text">
                    <xsl:variable name="extracted">
                        <xsl:call-template name="nodes-between">
                            <xsl:with-param name="startNodeId" select="$startId"/>
                            <xsl:with-param name="endNodeId" select="$endId"/>
                        </xsl:call-template>
                    </xsl:variable>
                    <xsl:call-template name="finalize-extracted">
                        <xsl:with-param name="extracted" select="$extracted"/>
                    </xsl:call-template>
                </xsl:when>
                <xsl:when test="$reproduce-text">
                    <xsl:element name="{$wrapper}">
                        <xsl:variable name="extracted">
                            <xsl:call-template name="nodes-between">
                                <xsl:with-param name="startNodeId" select="$startId"/>
                                <xsl:with-param name="endNodeId" select="$endId"/>
                            </xsl:call-template>
                        </xsl:variable>
                        <xsl:call-template name="finalize-extracted">
                            <xsl:with-param name="extracted" select="$extracted"/>
                        </xsl:call-template>
                    </xsl:element>
                </xsl:when>
            </xsl:choose>
            <xsl:if test="$insert-caret">
                <xsl:text>${caret}</xsl:text>
            </xsl:if>
        </span>
    </xsl:template>

</xsl:stylesheet>
