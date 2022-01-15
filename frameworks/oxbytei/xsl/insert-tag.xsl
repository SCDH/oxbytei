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

    <xsl:param name="context" as="xs:string" select="'/*:TEI[1]'" required="false"/>

    <xsl:param name="container" as="xs:string" select="'/*'" required="false"/>

    <!-- style can either be 'aggregative' or 'spanning' or 'analytic' -->
    <xsl:param name="style" as="xs:string" select="'aggregative'" required="no"/>

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


    <xsl:template match="/ | *">
        <xsl:variable name="ctx" select="."/>
        <xsl:variable name="current-node" use-when="function-available('oxy:current-element')"
            select="oxy:current-element()"/>
        <xsl:variable name="current-node" as="node()*"
            use-when="not(function-available('oxy:current-element')) and element-available('xsl:evaluate')">
            <xsl:message>using parameter 'context'</xsl:message>
            <xsl:evaluate as="node()*" context-item="/" expand-text="yes"
                xpath="$context"/>
        </xsl:variable>
        <xsl:variable name="current-node"
            use-when="not(function-available('oxy:current-element', 0) or element-available('xsl:evaluate'))"
            select="
                if (exists($ctx/self::*)) then
                    $ctx
                else
                    $ctx/*"/>
        <xsl:if test="$debug">
            <xsl:message>Entering from element <xsl:value-of select="local-name(.)"/></xsl:message>
            <xsl:message>Current node is <xsl:value-of select="local-name($current-node)"
                /></xsl:message>
            <xsl:message>Container element is <xsl:value-of select="$container"/></xsl:message>
        </xsl:if>
        <!-- get the node of the current editor position -->
        <xsl:variable name="start-parent-id"
            select="generate-id(//*[@xml:id eq $startId]/parent::*)"/>
        <xsl:variable name="end-parent-id" select="generate-id(//*[@xml:id eq $endId]/parent::*)"/>
        <xsl:variable name="same-parent" select="$start-parent-id eq $end-parent-id"/>
        <xsl:choose>
            <xsl:when test="$style eq 'analytic'">
                <xsl:if test="$debug">
                    <xsl:message>producing analytic markup</xsl:message>
                </xsl:if>
                <xsl:call-template name="analytic-entry"/>
            </xsl:when>
            <xsl:when test="$same-parent and ($style eq 'aggregative')">
                <!-- start and end anchor have the same parent, so it's possible to wrap into an element -->
                <xsl:if test="$debug">
                    <xsl:message>wrapping into element</xsl:message>
                </xsl:if>
                <xsl:apply-templates select="$current-node" mode="element">
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

    <xsl:template name="analytic-entry">
        <span from="{$startId}" to="{$endId}">
            <xsl:element name="{$tag}" namespace="{$tag-namespace}">
                <xsl:text>${caret}</xsl:text>
                <xsl:value-of
                    select="(//*[@xml:id eq $startId]/following::node() intersect //*[@xml:id eq $endId]/preceding::node()) => string-join('') => normalize-space()"
                />
            </xsl:element>
        </span>
    </xsl:template>

</xsl:stylesheet>
