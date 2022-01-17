<?xml version="1.0" encoding="UTF-8"?>
<!-- This provides functions for getting xml IDs.
    
You can override it in order to plug in your own policy for generating IDs.
Then you should provide the funtions obt:generate-id(node()) and
obt:generate-id(document-node(), xs:string).
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:obt="http://scdh.wwu.de/oxbytei"
    exclude-result-prefixes="xs obt" version="3.0">

    <xsl:function name="obt:generate-id" as="xs:ID">
        <xsl:param name="context-node" as="node()"/>
        <xsl:sequence select="generate-id($context-node)"/>
    </xsl:function>

    <xsl:function name="obt:generate-id" as="xs:ID">
        <xsl:param name="context-node" as="node()"/>
        <xsl:param name="element-name" as="xs:string"/>
        <xsl:param name="element-namespace" as="xs:string"/>
        <xsl:sequence select="generate-id($context-node)"/>
    </xsl:function>

    <xsl:function name="obt:generate-id" as="xs:ID">
        <xsl:param name="doc" as="document-node()"/>
        <xsl:param name="context-xpath" as="xs:string"/>
        <xsl:variable name="context-node" as="node()">
            <xsl:evaluate as="node()" expand-text="yes" context-item="$doc" xpath="$context-xpath"/>
        </xsl:variable>
        <xsl:value-of select="generate-id($context-node)"/>
    </xsl:function>

</xsl:stylesheet>
