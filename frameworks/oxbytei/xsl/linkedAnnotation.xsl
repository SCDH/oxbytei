<?xml version="1.0" encoding="UTF-8"?>
<!-- This make an interpretative annotation on markup.annotation linke described in the guidelines:
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

    <xsl:param name="annotation-wrapper" as="xs:string" select="''" required="no"/>

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
                <xsl:text>. Current node: </xsl:text>
                <xsl:value-of select="local-name(oxy:current-element())"/>
            </xsl:message>
        </xsl:if>
        <span from="#{$startId}" to="#{$endId}">
            <xsl:choose>
                <xsl:when test="$annotation-wrapper eq '' and $reproduce-text">
                    <xsl:variable name="extracted">
                        <xsl:apply-templates mode="extract"
                            select="//*[@xml:id eq $startId]/following::node() intersect //*[@xml:id eq $endId]/preceding::node()"
                        />
                    </xsl:variable>
                    <xsl:call-template name="finalize-extracted">
                        <xsl:with-param name="extracted" select="$extracted"/>
                    </xsl:call-template>
                </xsl:when>
                <xsl:when test="$reproduce-text">
                    <xsl:element name="{$annotation-wrapper}">
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
            <xsl:if test="$insert-caret">
                <xsl:text>${caret}</xsl:text>
            </xsl:if>
        </span>
    </xsl:template>

</xsl:stylesheet>
