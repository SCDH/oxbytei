<?xml version="1.0" encoding="UTF-8"?>
<!-- Stylesheet for making an element from a pair of anchors after SurroundWithAnchorsXSLTOperation

The style param decides about the resulting markup:

'aggregative' style:
When style=aggregative, then the markup depends on the situation: If both anchors are on the
same parent, then the nodes between them are simply wrapped into a tag element. Otherwise,
each non-whitespace text node is wrapped into a tag element and they are linked with @next
and @prev.
Depending on arguments:
sourceLocation: /*
targetLocation: ${anchorsContainer}
action: Replace
moveToEnd: false


'analytic' style:

When style=analytic, then the result will be a <span> containing an
element defined by tag and tag-namespace. The contents of this element will be
the text between the two anchors.
Depending arguments:
sourceLocation: /* OR ${anchorsContainer}
targetLocation: following::*:spanGrp
action: Inside as last child
moveToEnd: false OR true


'spanTo' style:

When style=spanTo, then the result will be an empty element defined by tag and namespace.
It will have a @spanTo attribute. It should replace the start anchor!
Depending arguments:
sourceLocation: /* OR ${anchorsContainer}
targetLocation: //*[@xml:id eq '${startAnchorId}']
action: Replace
moveToEnd: false


'restrictive-aggregative' style:
When style=restrictive-aggregative, then the result will be an empty element defined by tag
and namespace which @select all text between the anchors. This text will be wrapped in <seg>s
and have @xml:id.
Depending arguments:
moveToEnd: false
sourceLocation: ${anchorsContainer}
targetLocation: ${anchorsContainer}
action: Replace

-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:oxy="http://www.oxygenxml.com/ns/author/xpath-extension-functions"
    xmlns="http://www.tei-c.org/ns/1.0" xmlns:obt="http://scdh.wwu.de/oxbytei"
    exclude-result-prefixes="xs oxy obt" version="3.0"
    xpath-default-namespace="http://www.tei-c.org/ns/1.0">

    <xsl:include href="generate-id.xsl"/>

    <xsl:param name="startId" as="xs:ID" required="yes"/>
    <xsl:param name="endId" as="xs:ID" required="yes"/>

    <xsl:param name="container" as="xs:string" select="'/*'" required="false"/>

    <!-- style parameter. See above! -->
    <xsl:param name="style" as="xs:string" select="'aggregative'" required="no"/>

    <!-- Whether or not to wrap whitespace nodes into segments or tags in some styles.
        Saying false() here is reasonable, because we do not want to wrap whitespace
        between paragraphs, verses etc. -->
    <xsl:param name="wrap-whitespace" as="xs:boolean" select="false()" required="no"/>

    <!-- the tag name -->
    <xsl:param name="tag" as="xs:string" select="'seg'" required="no"/>

    <!-- the namespace of the tag -->
    <xsl:param name="tag-namespace" as="xs:string" select="'http://www.tei-c.org/ns/1.0'"
        required="no"/>

    <!-- whether or not to place the caret to the element -->
    <xsl:param name="insert-caret" as="xs:boolean" select="true()" required="no"/>

    <!-- get debugging messages -->
    <xsl:param name="debug" as="xs:boolean" select="false()" required="no"/>

    <xsl:mode on-no-match="shallow-copy"/>

    <xsl:template match="/ | *">
        <!-- get the node of the current editor position -->
        <xsl:variable name="start-parent-id"
            select="generate-id(//*[@xml:id eq $startId]/parent::*)"/>
        <xsl:variable name="end-parent-id" select="generate-id(//*[@xml:id eq $endId]/parent::*)"/>
        <xsl:variable name="same-parent" select="$start-parent-id eq $end-parent-id"/>

        <xsl:variable name="containerNode" as="node()">
            <xsl:evaluate as="node()" context-item="." expand-text="yes" xpath="$container"
                use-when="element-available('xsl:evaluate')"/>
            <xsl:value-of
                select="descendant-or-self::*[child::*[@xml:id eq $startId] and child::*[@xml:id eq $endId]][last()]"
                use-when="not(element-available('xsl:evaluate'))"/>
        </xsl:variable>

        <xsl:choose>
            <xsl:when test="$style eq 'analytic'">
                <xsl:if test="$debug">
                    <xsl:message>producing analytic markup</xsl:message>
                </xsl:if>
                <xsl:call-template name="analytic-entry"/>
            </xsl:when>
            <xsl:when test="$style eq 'spanTo'">
                <xsl:if test="$debug">
                    <xsl:message>producing anchor based markup with @spanTo="<xsl:value-of
                            select="$endId"/>"</xsl:message>
                </xsl:if>
                <xsl:call-template name="spanTo"/>
            </xsl:when>
            <xsl:when test="$style eq 'restricted-aggregative'">
                <xsl:if test="$debug">
                    <xsl:message>producing restrictive aggregative markup</xsl:message>
                </xsl:if>
                <xsl:variable name="wrapped">
                    <xsl:apply-templates select="$containerNode" mode="restrictive-aggregative"/>
                </xsl:variable>
                <xsl:apply-templates select="$wrapped" mode="restrictive-aggregative-postproc"/>
            </xsl:when>
            <xsl:when test="$same-parent and ($style eq 'aggregative')">
                <!-- start and end anchor have the same parent, so it's possible to wrap into an element -->
                <xsl:if test="$debug">
                    <xsl:message>wrapping into element</xsl:message>
                </xsl:if>
                <xsl:apply-templates select="$containerNode" mode="element">
                    <xsl:with-param name="parent-id" select="$start-parent-id" tunnel="yes"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:otherwise>
                <xsl:if test="$debug">
                    <xsl:message>falling back onto restrictive-aggregative mode</xsl:message>
                </xsl:if>
                <xsl:variable name="wrapped">
                    <xsl:apply-templates select="$containerNode" mode="linking"/>
                </xsl:variable>
                <xsl:apply-templates select="$wrapped" mode="linking-postproc"/>
            </xsl:otherwise>

        </xsl:choose>
    </xsl:template>


    <!-- wrap with element in aggregative style -->
    <xsl:mode name="element" on-no-match="shallow-copy"/>

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


    <!-- wrap into tags and link them with @prev and @next -->
    <xsl:mode name="linking" on-no-match="shallow-copy"/>
    <xsl:mode name="linking-between" on-no-match="shallow-copy"/>
    <xsl:mode name="linking-postproc" on-no-match="shallow-copy"/>

    <xsl:template mode="linking"
        match="node()[preceding::*[@xml:id eq $startId] and following::*[@xml:id eq $endId]]">
        <xsl:apply-templates select="." mode="linking-between"/>
    </xsl:template>

    <!-- wrap non-whitespace text nodes into a tag -->
    <xsl:template mode="linking-between"
        match="text()[not(matches(., '^\s+$')) or $wrap-whitespace]">
        <xsl:element name="{$tag}" namespace="{$tag-namespace}">
            <!-- we use the @n attribute to temporarily keep track of the elements -->
            <xsl:attribute name="n" select="$startId"/>
            <xsl:attribute name="xml:id" select="obt:generate-id(., $tag, $tag-namespace)"/>
            <xsl:value-of select="."/>
        </xsl:element>
    </xsl:template>

    <xsl:template mode="linking-postproc" match="*[local-name() eq $tag and @n eq $startId]">
        <xsl:copy select=".">
            <!-- do the linking with @prev and @next -->
            <xsl:if test="exists(preceding::*[@n eq $startId])">
                <xsl:attribute name="prev"
                    select="concat('#', preceding::*[@n eq $startId][1]/@xml:id)"/>
            </xsl:if>
            <xsl:if test="exists(following::*[@n eq $startId])">
                <xsl:attribute name="next"
                    select="concat('#', following::*[@n eq $startId][1]/@xml:id)"/>
            </xsl:if>
            <xsl:copy select="@xml:id"/>
            <xsl:apply-templates mode="linking-postproc"/>
            <!-- insert the caret on the last tag -->
            <xsl:if test="not(exists(following::*[@n eq $startId])) and $insert-caret">
                <xsl:text>${caret}</xsl:text>
            </xsl:if>
        </xsl:copy>
    </xsl:template>

    <!-- remove @n attributes that where used for tracking -->
    <xsl:template mode="linking-postproc"
        match="@n[. eq $startId and parent::*[local-name() eq $tag]]"/>

    <!-- remove anchors -->
    <xsl:template mode="linking-postproc" match="*[@xml:id eq $startId]"/>
    <xsl:template mode="linking-postproc" match="*[@xml:id eq $endId]"/>


    <!-- analytic style -->
    <xsl:template name="analytic-entry">
        <span from="#{$startId}" to="#{$endId}">
            <xsl:element name="{$tag}" namespace="{$tag-namespace}">
                <xsl:text>${caret}</xsl:text>
                <xsl:value-of
                    select="(//*[@xml:id eq $startId]/following::node() intersect //*[@xml:id eq $endId]/preceding::node()) => string-join('') => normalize-space()"
                />
            </xsl:element>
        </span>
    </xsl:template>


    <!-- spanTo style -->
    <xsl:template name="spanTo">
        <!--xsl:variable name="att" select="if ($insert-caret) then concat('#', $endId, '${caret}') else concat('#', $endId)"/-->
        <xsl:variable name="att" select="concat('#', $endId)"/>
        <xsl:element name="{$tag}" namespace="{$tag-namespace}">
            <xsl:attribute name="spanTo" select="$att"/>
        </xsl:element>
    </xsl:template>


    <!-- restrictive-aggregative -->

    <xsl:mode name="restrictive-aggregative" on-no-match="shallow-copy"/>
    <xsl:mode name="restrictive-aggregative-between" on-no-match="shallow-copy"/>
    <xsl:mode name="restrictive-aggregative-postproc" on-no-match="shallow-copy"/>

    <xsl:template mode="restrictive-aggregative"
        match="node()[preceding::*[@xml:id eq $startId] and following::*[@xml:id eq $endId]]">
        <xsl:apply-templates select="." mode="restrictive-aggregative-between"/>
    </xsl:template>

    <xsl:template mode="restrictive-aggregative-between"
        match="text()[not(parent::seg[count(child::node()) eq 1])][not(matches(., '^\s+$')) or $wrap-whitespace]">
        <seg xml:id="{obt:generate-id(., 'seg', 'http://www.tei-c.org/ns/1.0')}">
            <xsl:value-of select="."/>
        </seg>
    </xsl:template>

    <xsl:template mode="restrictive-aggregative-between"
        match="seg[not(@xml:id) and exists(child::text()) and count(child::node()) eq 1]">
        <seg xml:id="{obt:generate-id(.)}">
            <xsl:apply-templates select="node()" mode="restrictive-aggregative-between"/>
        </seg>
    </xsl:template>

    <xsl:template mode="restrictive-aggregative-postproc" match="*[@xml:id eq $endId]">
        <xsl:element name="{$tag}" namespace="{$tag-namespace}">
            <xsl:attribute name="select"
                select="(//seg[preceding::*[@xml:id eq $startId] and following::*[@xml:id eq $endId]]/@xml:id ! concat('#', .)) => string-join(' ')"/>
            <xsl:if test="$insert-caret">
                <xsl:text>${caret}</xsl:text>
            </xsl:if>
        </xsl:element>
    </xsl:template>

    <xsl:template mode="restrictive-aggregative-postproc" match="*[@xml:id eq $startId]"/>

</xsl:stylesheet>
