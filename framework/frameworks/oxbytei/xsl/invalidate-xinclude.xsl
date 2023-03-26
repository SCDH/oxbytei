<?xml version="1.0" encoding="UTF-8"?>
<!-- XSLT for marking broken <xi:include>s with a fallback, saying that it's broken. -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xi="http://www.w3.org/2001/XInclude"
    xmlns:tei="http://www.tei-c.org/ns/1.0" exclude-result-prefixes="xs" version="3.0"
    xpath-default-namespace="http://www.w3.org/2001/XInclude">

    <xsl:mode on-no-match="shallow-copy"/>

    <xsl:param name="debug" select="true()"/>

    <!-- report broken @href -->
    <xsl:template match="include[not(doc-available(resolve-uri(@href, base-uri(.)))) and not(*)]">
        <xsl:message>Found XInclud with broken @href <xsl:value-of select="@href"/></xsl:message>
        <xsl:call-template name="broken-link"/>
    </xsl:template>

    <!-- report broken @xpointer -->
    <xsl:template match="include[doc-available(resolve-uri(@href, base-uri(.))) and @xpointer]">
        <xsl:variable name="referenced" select="doc(resolve-uri(@href, base-uri(.)))"/>
        <xsl:variable name="bare-name" select="@xpointer"/>
        <xsl:choose>
            <xsl:when test="$referenced//*[@xml:id eq $bare-name]">
                <!-- make a deep copy if not broken -->
                <xsl:copy-of select="."/>
                <xsl:if test="$debug">
                    <xsl:message>found working XInclude</xsl:message>
                </xsl:if>
            </xsl:when>
            <xsl:when test="tei:note[@type eq 'warning']">
                <!-- make a deep copy if warning is present already -->
                <xsl:copy-of select="."/>
                <xsl:if test="$debug">
                    <xsl:message>Found XInclude with broken @xpointer and fallback.</xsl:message>
                </xsl:if>
            </xsl:when>
            <xsl:otherwise>
                <!-- report broken link -->
                <xsl:call-template name="broken-link"/>
                <xsl:message>Found broken @xpointer <xsl:value-of select="$bare-name"
                    /></xsl:message>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- when marking a broken link, we simply add a fallback to the xi:include -->
    <xsl:template name="broken-link">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
            <note xmlns="http://www.tei-c.org/ns/1.0" type="warning">BROKEN LINK</note>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
