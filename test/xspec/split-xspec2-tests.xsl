<?xml version="1.0" encoding="UTF-8"?>
<!-- split an XSpec test suite into single XSpec tests for use with maven
USAGE:
java -jar saxon.jar -xsl:split-xspec2-tests.xsl -s:insert-tag.xspec.OFF
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:x="http://www.jenitennison.com/xslt/xspec"
    xmlns:t="http://www.tei-c.org/ns/1.0" exclude-result-prefixes="xs" version="3.0">

    <xsl:output method="xml"/>

    <xsl:mode on-no-match="shallow-copy"/>

    <xsl:template match="@run-as"/>

    <xsl:template match="x:scenario">
        <xsl:variable name="test-file"
            select="concat(replace(base-uri(), '\.OFF$', ''), '.', count(preceding-sibling::x:scenario) + 1, '.xspec')"/>
        <xsl:result-document href="{$test-file}">
            <xsl:comment>Do not edit this file. It was auto-generated from <xsl:value-of select="tokenize(base-uri(), '/')[last()]"/></xsl:comment>
            <x:description>
                <xsl:apply-templates select="/x:description/@*"/>
                <xsl:text>&#xa;&#xa;   </xsl:text>
                <xsl:apply-templates select="x:param"/>
                <xsl:text>&#xa;&#xa;   </xsl:text>
                <x:scenario>
                    <xsl:apply-templates select="@*"/>
                    <xsl:if test="child::x:expect/@pending">
                        <!-- FIXME: do not mark the whole scenario as pending! -->
                        <xsl:attribute name="pending" select="'Pending since pending expectation'"/>
                    </xsl:if>
                    <xsl:apply-templates select="*[not(name() eq 'x:param')]"/>
                </x:scenario>
                <xsl:text>&#xa;&#xa;</xsl:text>
            </x:description>
        </xsl:result-document>
    </xsl:template>

</xsl:stylesheet>
