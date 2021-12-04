<?xml version="1.0" encoding="UTF-8"?>
<!-- make a testable xsl stylesheet with the xpath expressions in an external author action -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:axsl="http://www.w3.org/1999/XSL/TransformAlias"
    xmlns:a="http://www.oxygenxml.com/ns/author/external-action" xmlns="http://test.it"
    exclude-result-prefixes="xs a" version="3.0">

    <xsl:namespace-alias stylesheet-prefix="axsl" result-prefix="xsl"/>

    <xsl:output method="xml" indent="yes"/>

    <!-- A list of xpath expressions used as match conditions for the tests.
        Separated by double pipe, i.e. '||'. If a position is empty, the
        xpathCondition of the operation is used instead.
        Example value for link-person: 'persName|person||prefixDef' -->
    <xsl:param name="match-conditions" as="xs:string" select="''" required="no"/>

    <xsl:mode on-no-match="shallow-skip"/>

    <xsl:template match="/">
        <axsl:stylesheet version="3.0" xpath-default-namespace="http://www.tei-c.org/ns/1.0">
            <axsl:output method="xml" indent="yes"/>
            <axsl:template match="/">
                <tests>
                    <axsl:apply-templates/>
                </tests>
            </axsl:template>
            <xsl:apply-templates/>
            <axsl:template match="text()"/>
        </axsl:stylesheet>
    </xsl:template>

    <!-- extract the contents of ${xpath_eval(...)} -->
    <xsl:template match="text()[matches(., '\$\{xpath_eval')]">
        <xsl:variable name="n" select="count(preceding::text()[matches(., '\$\{xpath_eval')]) + 1"/>
        <xsl:variable name="match-condition" select="
                let $cond := tokenize($match-conditions, '\|\|')[$n]
                return
                    if ($cond ne '') then
                        $cond
                    else
                        ancestor::a:operation/a:xpathCondition"/>
        <axsl:template match="{$match-condition}">
            <test class-number="{$n}" class-type="{ancestor::a:operation/@id}"
                class-match="{$match-condition}">
                <axsl:attribute name="case" select="name(.)"/>
                <xsl:analyze-string select="." regex="xpath_eval\(([^\}}]+)\)\}}">
                    <xsl:matching-substring>
                        <case>
                            <axsl:value-of>
                                <xsl:attribute name="select" select="regex-group(1)"/>
                            </axsl:value-of>
                            <!--axsl:text>$$$</axsl:text-->
                        </case>
                    </xsl:matching-substring>
                </xsl:analyze-string>
                <!--            
                <xsl:value-of select="."/>
                <xsl:text>&#xa;&#xa;</xsl:text>
                -->
            </test>
        </axsl:template>
    </xsl:template>

    <xsl:template match="text()"/>

</xsl:stylesheet>
