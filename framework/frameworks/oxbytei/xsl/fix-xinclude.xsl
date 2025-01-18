<?xml version="1.0" encoding="UTF-8"?>
<!-- XSLT for rebasing broken <xi:include>s on project directory URL if available there.
This is handy for fixing <xi:include>s of central files. It requires that an Oxygen project
is opened.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xi="http://www.w3.org/2001/XInclude"
    xmlns:tei="http://www.tei-c.org/ns/1.0" exclude-result-prefixes="#all" version="3.0"
    xpath-default-namespace="http://www.w3.org/2001/XInclude">

    <xsl:mode on-no-match="shallow-copy"/>

    <xsl:param name="debug" as="xs:boolean" select="true()" static="true"/>

    <!--
        Project Directory Url as in oXygen's ${pdu} editor variable, but with trailing slash.
    -->
    <xsl:param name="pdu" as="xs:anyURI" select="xs:anyURI($pdu-string)"/>

    <!-- same as $pdu, but as string -->
    <xsl:param name="pdu-string" as="xs:string" select="''"/>

    <!-- report broken unfixable @href when no fallback present -->
    <xsl:template match="include[not(doc-available(resolve-uri(@href, base-uri(.))))]">
        <xsl:message>
            <xsl:text>Found XInclud with broken @href </xsl:text>
            <xsl:value-of select="@href"/>
            <xsl:text>. Trying to rebase on </xsl:text>
            <xsl:value-of select="$pdu"/>
        </xsl:message>
        <xsl:variable name="relative-in-pdu" as="xs:string"
            select="substring(base-uri(.), string-length($pdu) + 1)"/>
        <xsl:variable name="levels" as="xs:integer"
            select="tokenize($relative-in-pdu, '/') => count() - 1"/>
        <xsl:variable name="new-href" as="xs:string">
            <xsl:variable name="segments" as="xs:string*">
                <xsl:for-each select="1 to $levels">
                    <xsl:text>../</xsl:text>
                </xsl:for-each>
                <xsl:value-of select="tokenize(@href, '/')[last()]"/>
            </xsl:variable>
            <xsl:sequence select="string-join($segments)"/>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="doc-available(resolve-uri($new-href, base-uri(.)))">
                <xsl:message>
                    <xsl:text>setting new @href to </xsl:text>
                    <xsl:value-of select="$new-href"/>
                </xsl:message>
                <xsl:copy>
                    <xsl:attribute name="href" select="$new-href"/>
                    <xsl:copy-of select="@* except @href"/>
                    <xsl:apply-templates
                        select="node() | comment() | processing-instruction() except tei:note[@type = 'warning']"
                    />
                </xsl:copy>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message>unable to rebase</xsl:message>
                <xsl:copy-of select="."/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>


</xsl:stylesheet>
