<?xml version="1.0" encoding="UTF-8"?>
<!-- used for RDF Open Annotation hasSource.
Rewrite this if you have an other document registry or do not need it for making entities.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:obt="http://scdh.wwu.de/oxbytei"
    xpath-default-namespace="http://www.tei-c.org/ns/1.0" exclude-result-prefixes="xs obt"
    version="3.0">

    <!-- whether it is allowed to go without entity. -->
    <xsl:param name="non-entity-allowed" select="false()" as="xs:boolean" required="no"/>

    <!-- path to the document registry, e.g. ${pdu}/registry.xml -->
    <xsl:param name="registry" as="xs:string" required="yes"/>

    <xsl:variable name="reg" as="document-node()" select="doc($registry)"/>

    <xsl:template name="obt:docid">
        <xsl:param name="filename" as="xs:string"/>
        <xsl:param name="is-default" as="xs:boolean"/>
        <xsl:variable name="id" select="$reg//item[child::ptr[@target eq $filename]]/@xml:id"/>
        <xsl:choose>
            <xsl:when test="exists($id)">
                <xsl:text disable-output-escaping="yes">&#x26;</xsl:text>
                <xsl:value-of select="$id[1]" disable-output-escaping="yes"/>
                <xsl:text disable-output-escaping="yes">;</xsl:text>
            </xsl:when>
            <xsl:when test="$is-default">
                <xsl:value-of select="$filename"/>
            </xsl:when>
            <xsl:when test="$non-entity-allowed">
                <!-- We simply use the local document path. This is not portable. -->
                <xsl:message>
                    <xsl:text>WARNING: using local path in RDF</xsl:text>
                </xsl:message>
                <xsl:value-of select="$filename"/>
            </xsl:when>
            <xsl:otherwise>
                <!-- Terminate with an error message. -->
                <xsl:message terminate="yes">
                    <xsl:text>ERROR: currently annotated file is not registered in the </xsl:text>
                    <xsl:value-of select="$registry"/>
                </xsl:message>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
