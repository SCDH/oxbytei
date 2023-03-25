<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:obt="http://scdh.wwu.de/oxbytei"
    xmlns:map="http://www.w3.org/2005/xpath-functions/map" exclude-result-prefixes="xs obt map"
    xpath-default-namespace="http://www.tei-c.org/ns/1.0" version="3.0">

    <xsl:param name="personography" as="xs:string" select="'persons.xml'"/>

    <xsl:template name="obt:generate-entries" as="map(xs:string, xs:string)*">
        <xsl:apply-templates select="doc($personography)//text//person[@xml:id]" mode="entries"/>
    </xsl:template>

    <xsl:template match="person" as="map(xs:string, xs:string)*" mode="entries">
        <xsl:variable name="label" as="xs:string*">
            <xsl:apply-templates mode="label" select="persName"/>
        </xsl:variable>
        <xsl:sequence select="
                map {
                    'key': concat('#', @xml:id),
                    'label': string-join($label, '') => normalize-space()
                }"/>
    </xsl:template>

</xsl:stylesheet>
