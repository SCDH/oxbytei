<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns="http://wwu.de/scdh/teilsp/config/"
    exclude-result-prefixes="xs" xpath-default-namespace="http://wwu.de/scdh/teilsp/config/"
    version="3.0">

    <xsl:output method="xml" indent="true"/>

    <xsl:global-context-item as="document-node(element(teilspConfiguration))" use="required"/>

    <xsl:param name="default-config" as="xs:anyURI" required="true"/>

    <xsl:variable name="properties" as="element(property)+"
        select="doc($default-config)/teilspConfiguration/properties/property"/>

    <xsl:variable name="new-properties" as="element(property)*">
        <xsl:for-each select="$properties">
            <xsl:variable name="id" select="@name"/>
            <xsl:if test="exists(/teilspConfiguration/properties/property[@name eq $id])">
                <xsl:sequence select="."/>
            </xsl:if>
        </xsl:for-each>
    </xsl:variable>

    <xsl:mode on-no-match="shallow-copy"/>

    <xsl:template match="properties[1]">
        <xsl:copy>
            <xsl:apply-templates select="attribute() | node()"/>
            <xsl:choose>
                <xsl:when test="$new-properties">
                    <xsl:message>
                        <xsl:text>Adding new properties:&#xa;</xsl:text>
                        <xsl:value-of select="$new-properties/@name"/>
                    </xsl:message>
                    <xsl:text>&#xa;&#xa;</xsl:text>
                    <xsl:comment>new properties added on <xsl:value-of select="current-date()"/></xsl:comment>
                    <xsl:sequence select="$new-properties"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:message>No new properties to add!</xsl:message>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
