<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:obt="http://scdh.wwu.de/oxbytei"
    xmlns:map="http://www.w3.org/2005/xpath-functions/map" exclude-result-prefixes="xs obt map"
    xpath-default-namespace="http://www.tei-c.org/ns/1.0" version="3.0">

    <!-- the currently edited document,
        parameter needed for de.wwu.scdh.teilsp.extension.LabelledEntriesXSLTWithContext -->
    <xsl:param name="document" as="document-node()" select="doc($testfile)"/>

    <!-- the current editing context as XPath,
        parameter needed for de.wwu.scdh.teilsp.extension.LabelledEntriesXSLTWithContext -->
    <xsl:param name="context" as="xs:string" select="'/*'"/>

    <!-- parameter specific for this plugin configuration -->
    <xsl:param name="witness-catalog" as="xs:string" select="'../../WitnessCatalogue.xml'"/>

    <!-- Not used with the plugin, only used for testing because we like a default value 
        for $document because it is difficult to pass in a document node. -->
    <xsl:param name="testfile" as="xs:string" select="'test.tei.xml'" required="false"/>


    <!-- get the context node. This is generic. -->
    <xsl:variable name="context-node" as="node()">
        <xsl:evaluate as="node()" context-item="$document" expand-text="true" xpath="$context"/>
    </xsl:variable>

    <!-- entry template -->
    <xsl:template name="obt:generate-entries" as="map(xs:string, xs:string)*">
        <!-- Get the IDs of the witnesses in the header and use them
            as a filter for witnesses from the catalog. -->
        <xsl:variable name="registered-witnesses" as="xs:string*"
            select="$document//teiHeader//sourceDesc//witness/@xml:id"/>
        <xsl:apply-templates select="
                doc($witness-catalog)//text//witness[let $id := @xml:id
                return
                    some $wit in $registered-witnesses
                        satisfies $wit eq $id]" mode="entries"/>
    </xsl:template>

    <xsl:template match="witness" as="map(xs:string, xs:string)*" mode="entries">
        <xsl:variable name="label" as="xs:string*">
            <xsl:apply-templates mode="label"/>
        </xsl:variable>
        <xsl:sequence select="
                map {
                    'key': concat('#', @xml:id),
                    'label': string-join($label, '') => normalize-space()
                }"/>
    </xsl:template>

    <xsl:template match="*" mode="label">
        <xsl:apply-templates mode="label"/>
    </xsl:template>

    <xsl:template match="text()" mode="label">
        <xsl:value-of select="."/>
    </xsl:template>

</xsl:stylesheet>
