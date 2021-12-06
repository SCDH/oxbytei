<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns="http://www.oxygenxml.com/ns/ccfilter/config"
    xpath-default-namespace="http://www.tei-c.org/ns/1.0" exclude-result-prefixes="xs" version="2.0">

    <xsl:param name="documentSystemID"/>


    <xsl:template name="start">
        <xsl:variable name="prefixDef"
            select="doc($documentSystemID)//teiHeader/encodingDesc/listPrefixDef/prefixDef[matches(@ident, '^(psn|prs|prsn|pers|person)$') or @corresp eq 'person'][1]"/>
        <xsl:variable name="personography"
            select="doc(tokenize($prefixDef/@replacementPattern, '#')[1])"/>
        <items>
            <item value=""/>
            <xsl:apply-templates select="$personography//person">
                <xsl:with-param name="prefix" select="$prefixDef/@ident"/>
            </xsl:apply-templates>
        </items>
    </xsl:template>

    <xsl:template match="person">
        <xsl:param name="prefix"/>
        <item>
            <xsl:attribute name="value" select="concat($prefix, ':', @xml:id)"/>
        </item>
    </xsl:template>

</xsl:stylesheet>
