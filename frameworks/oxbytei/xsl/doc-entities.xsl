<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xpath-default-namespace="http://www.tei-c.org/ns/1.0" exclude-result-prefixes="xs" version="3.0">

    <xsl:output method="text"/>

    <!-- path to the project root -->
    <xsl:param name="pdu" as="xs:string" required="true"/>

    <!-- the suffix of your TEI documents -->
    <xsl:param name="suffix" as="xs:string" select="'.xml'" required="false"/>

    <!-- an XPath expression for making an identifier in an document context.
        E.g. //teiHeader//idno[@type eq 'document-identifier']
    -->
    <xsl:param name="identifier" as="xs:string" required="true"/>

    <xsl:template match="/">
        <xsl:variable name="col" select="collection(concat($pdu, '?recurse=yes;select=*', $suffix))"/>
        <xsl:for-each select="$col">
            <xsl:variable name="doc" select="." as="node()"/>
            <xsl:if test="$debug">
                <xsl:message>
                    <xsl:text>found file </xsl:text>
                    <xsl:value-of select="base-uri($doc)"/>
                </xsl:message>
            </xsl:if>
            <xsl:variable name="id" as="xs:string*">
                <xsl:evaluate as="xs:string*" context-item="$doc" expand-text="true"
                    xpath="$identifier"/>
            </xsl:variable>
            <xsl:if test="exists($id)">
                <xsl:text disable-output-escaping="true">&lt;!ENTITY </xsl:text>
                <xsl:value-of select="$id"/>
                <xsl:text disable-output-escaping="true"> "</xsl:text>
                <xsl:value-of select="base-uri($doc)"/>
                <xsl:text disable-output-escaping="true">"&gt;&#xa;</xsl:text>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

</xsl:stylesheet>
