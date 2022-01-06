<?xml version="1.0" encoding="UTF-8"?>
<!-- Stylesheet for making an element from a pair of anchors after SurroundWithAnchorsXSLTOperation
If the anchors are on the same node, they are replaced with an element.
Otherwise, the end anchor is replaced with an empty element that refers
to the start anchor by @from.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:oxy="http://www.oxygenxml.com/ns/author/xpath-extension-functions"
    xmlns="http://www.tei-c.org/ns/1.0" exclude-result-prefixes="xs oxy" version="3.0"
    xpath-default-namespace="http://www.tei-c.org/ns/1.0">

    <xsl:param name="startId" as="xs:ID" required="yes"/>
    <xsl:param name="endId" as="xs:ID" required="yes"/>

    <!-- In any case, do not wrap nodes between anchors into an element.
        Keep markup with anchors instead. -->
    <xsl:param name="keepAnchors" as="xs:boolean" select="false()" required="no"/>

    <!-- When keeping anchored markup: Replace the start anchor with the empty element. -->
    <xsl:param name="atStart" as="xs:boolean" select="false()" required="no"/>

    <!-- the tag name -->
    <xsl:param name="tag" as="xs:string" select="'seg'" required="no"/>

    <!-- the namespace of the tag -->
    <xsl:param name="tag-namespace" as="xs:string" select="'http://www.tei-c.org/ns/1.0'"
        required="no"/>

    <!-- When keeping anchored markup: The name of the attribute that has the link to the anchor. -->
    <xsl:param name="linking-att" as="xs:string" select="'from'" required="no"/>

    <!-- whether or not to place the caret to the element -->
    <xsl:param name="insert-caret" as="xs:boolean" select="true()" required="no"/>

    <!-- get debugging messages -->
    <xsl:param name="debug" as="xs:boolean" select="false()" required="no"/>

    <xsl:mode on-no-match="shallow-copy"/>
    <xsl:mode name="element" on-no-match="shallow-copy"/>
    <xsl:mode name="anchors" on-no-match="shallow-copy"/>


    <xsl:template match="/">
        <!-- get the node of the current editor position -->
        <xsl:variable name="current-node" as="node()" select="oxy:current-element()"/>
        <xsl:variable name="start-parent-id"
            select="generate-id(//*[@xml:id eq $startId]/parent::*)"/>
        <xsl:variable name="end-parent-id" select="generate-id(//*[@xml:id eq $endId]/parent::*)"/>
        <xsl:choose>
            <xsl:when test="$start-parent-id eq $end-parent-id and not($keepAnchors)">
                <!-- start and end anchor have the same parent, so it's possible to wrap into an element -->
                <xsl:if test="$debug">
                    <xsl:message>wrapping into element</xsl:message>
                </xsl:if>
                <xsl:apply-templates select="$current-node[1]" mode="element">
                    <xsl:with-param name="parent-id" select="$start-parent-id" tunnel="yes"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:otherwise>
                <xsl:if test="$debug">
                    <xsl:message>keeping anchored markup</xsl:message>
                </xsl:if>
                <xsl:apply-templates select="." mode="anchors"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template mode="element"
        match="*[child::*[@xml:id eq $startId] and child::*[@xml:id eq $endId]]">
        <!-- we are on the element, that contains both anchors -->
        <xsl:if test="$debug">
            <xsl:message>in containing parent <xsl:value-of select="local-name(.)"/></xsl:message>
        </xsl:if>
        <xsl:element name="{name(.)}">
            <xsl:copy-of select="@*"/>
            <xsl:copy-of select="node()[following-sibling::*[@xml:id eq $startId]]"/>
            <xsl:element name="{$tag}" namespace="{$tag-namespace}">
                <xsl:if test="$insert-caret">
                    <xsl:text>${caret}</xsl:text>
                </xsl:if>
                <xsl:copy-of
                    select="node()[preceding-sibling::*[@xml:id eq $startId]] intersect node()[following-sibling::*[@xml:id eq $endId]]"
                />
            </xsl:element>
            <xsl:copy-of select="node()[preceding-sibling::*[@xml:id eq $endId]]"/>
        </xsl:element>
    </xsl:template>

    <xsl:template mode="anchors" match="*[@xml:id eq $endId and not($atStart)]">
        <xsl:element name="{$tag}" namespace="{$tag-namespace}">
            <xsl:attribute name="{$linking-att}" select="concat('#', $startId)"/>
            <xsl:if test="$insert-caret">
                <xsl:text>${caret}</xsl:text>
            </xsl:if>
        </xsl:element>
    </xsl:template>

    <xsl:template mode="anchors" match="*[@xml:id eq $startId and $atStart]">
        <xsl:element name="{$tag}" namespace="{$tag-namespace}">
            <xsl:attribute name="{$linking-att}" select="concat('#', $endId)"/>
            <xsl:if test="$insert-caret">
                <xsl:text>${caret}</xsl:text>
            </xsl:if>
        </xsl:element>
    </xsl:template>

</xsl:stylesheet>
