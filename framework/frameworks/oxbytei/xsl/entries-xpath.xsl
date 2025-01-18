<?xml version="1.0" encoding="UTF-8"?>
<!-- Generate labelled entries from the xpath given in $values-xpath applied to the current document
Duplicates are removed.

This is handy for vocabularies in the current file, e.g., for @type or @subtype
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions"
    xmlns:obt="http://scdh.wwu.de/oxbytei" xmlns:map="http://www.w3.org/2005/xpath-functions/map"
    exclude-result-prefixes="#all" xpath-default-namespace="http://www.tei-c.org/ns/1.0"
    version="3.0">

    <!-- the currently edited document,
        parameter needed for de.wwu.scdh.teilsp.extension.LabelledEntriesXSLTWithContext -->
    <xsl:param name="document" as="document-node()"/>

    <!-- the current editing context as XPath,
        parameter needed for de.wwu.scdh.teilsp.extension.LabelledEntriesXSLTWithContext -->
    <xsl:param name="context" as="xs:string" select="'/*'"/>

    <!-- the XPath that describes the nodes to generate values from -->
    <xsl:param name="values-xpath" as="xs:string" select="'//@type'"/>

    <xsl:template name="obt:generate-entries" as="map(xs:string, xs:string)*">
        <xsl:variable name="values" as="item()*">
            <xsl:evaluate context-item="$document" xpath="$values-xpath" as="item()*"/>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="not($values)">
                <xsl:sequence select="()"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:for-each select="$values ! normalize-space(.) ! tokenize(.) => distinct-values()">
                    <xsl:map>
                        <xsl:map-entry key="'key'" select="string(.)"/>
                        <xsl:map-entry key="'label'" select="string(.)"/>
                    </xsl:map>
                </xsl:for-each>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
