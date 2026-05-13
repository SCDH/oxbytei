<?xml version="1.0" encoding="UTF-8"?>
<!-- generates an person or place etc. entry from a VIAF API call

The fetched data is merged with the existing data from
the element with the $template-id ID, if present. This
can be used to merge with template data or an existing
data record.

USAGE: see 'add.from.viaf' author action

-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="#all" version="3.0"
    default-mode="oxy-action">

    <xsl:import href="../x2tei-transformations/xsl/rdf/viaf.xsl"/>

    <xsl:param name="template-id" as="xs:string" select="'TEMPLATE'"/>

    <xsl:param name="template" as="element()?" select="/id($template-id)"/>

    <!-- which ID to use for the resulting entry -->
    <xsl:param name="id-from-template" as="xs:boolean" select="false()"/>

    <xsl:template mode="oxy-action" match="/">
        <xsl:variable name="viaf" as="element()">
            <xsl:call-template name="from-viaf"/>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="not($template)">
                <xsl:sequence select="$viaf"/>
            </xsl:when>
            <xsl:otherwise>
                <!-- merge template and viaf data -->
                <xsl:copy select="$viaf">
                    <!-- @xml:id -->
                    <xsl:choose>
                        <xsl:when test="$id-from-template">
                            <xsl:sequence select="$template/@xml:id"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:sequence select="$viaf/@xml:id"/>
                        </xsl:otherwise>
                    </xsl:choose>
                    <!-- other attributes -->
                    <xsl:sequence select="$viaf/(@* except @xml:id)"/>
                    <xsl:sequence select="$template/(@* except @xml:id)"/>
                    <!-- elements -->
                    <xsl:variable name="element-names-in-template" as="xs:string*"
                        select="$template/* ! local-name() => distinct-values()"/>
                    <xsl:for-each select="$element-names-in-template">
                        <xsl:variable name="element-name" as="xs:string" select="."/>
                        <xsl:copy-of select="$viaf/*[local-name() = $element-name]"/>
                        <xsl:for-each select="$template/*[local-name() = $element-name]">
                            <xsl:variable name="template-element" as="element()" select="."/>
                            <xsl:copy-of select="$template-element"/>
                            <!-- copy mediately following text nodes and comments -->
                            <xsl:variable name="next-template-element" as="element()?"
                                select="./following-sibling::*[1]"/>
                            <xsl:if test="$next-template-element">
                                <xsl:copy-of
                                    select="($template-element/following-sibling::node() intersect $next-template-element/preceding-sibling::node()) => outermost()"
                                />
                            </xsl:if>
                        </xsl:for-each>
                    </xsl:for-each>
                    <xsl:copy-of select="$viaf/*[not(local-name() = $element-names-in-template)]"/>
                </xsl:copy>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
