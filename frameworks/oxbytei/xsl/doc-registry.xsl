<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.tei-c.org/ns/1.0"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xpath-default-namespace="http://www.tei-c.org/ns/1.0" exclude-result-prefixes="xs" version="3.0">

    <xsl:output method="xml" indent="true"/>

    <xsl:mode on-no-match="shallow-copy"/>

    <xsl:import href="registry-meta.xsl"/>

    <!-- path to the project root -->
    <xsl:param name="pdu" as="xs:string" required="true"/>

    <!-- the suffix of your TEI documents -->
    <xsl:param name="suffix" as="xs:string" select="'.xml'" required="false"/>

    <!-- an XPath expression for making an identifier in an document context.
        E.g. //teiHeader//idno[@type eq 'document-identifier']
    -->
    <xsl:param name="identifier" as="xs:string" required="true"/>

    <xsl:param name="debug" as="xs:boolean" select="false()" required="false"/>

    <!-- system path to entity file -->
    <xsl:param name="docids" as="xs:string" select="'docids.ent'" required="false"/>

    <xsl:param name="insert-doctype" as="xs:boolean" select="true()" required="false"/>

    <!-- insert the DOCTYPE declaration with the link to the external entity file -->
    <xsl:template name="doctype-decl">
        <xsl:if test="$insert-doctype">
            <xsl:text disable-output-escaping="yes">&#xa;&lt;!DOCTYPE TEI [</xsl:text>
            <xsl:text disable-output-escaping="yes">&#xa;&lt;!ENTITY % docids SYSTEM "</xsl:text>
            <xsl:value-of select="$docids"/>
            <xsl:text disable-output-escaping="yes">" &gt;&#xa;%docids;</xsl:text>
            <xsl:text disable-output-escaping="yes">&#xa;]&gt;&#xa;</xsl:text>
        </xsl:if>
    </xsl:template>

    <xsl:template match="/*">
        <xsl:call-template name="doctype-decl"/>
        <xsl:element name="{name(.)}">
            <xsl:apply-templates/>
        </xsl:element>
    </xsl:template>

    <!-- append to the last list, that is not nested in another list -->
    <xsl:template match="//text/body//list[not(ancestor::list)][last()]">
        <list>

            <!-- reproduce what we have -->
            <xsl:apply-templates/>

            <xsl:call-template name="append-new-docs">
                <xsl:with-param name="root" select="/"/>
            </xsl:call-template>

        </list>
    </xsl:template>

    <xsl:template name="append-new-docs">
        <xsl:param name="root"/>
        <xsl:variable name="col" select="collection(concat($pdu, '?recurse=yes;select=*', $suffix))"/>
        <xsl:variable name="abspath-length" select="string-length($pdu) + 2" as="xs:integer"/>
        <xsl:for-each select="$col">
            <xsl:variable name="doc" select="." as="node()"/>
            <xsl:if test="$debug">
                <xsl:message>
                    <xsl:text>testing </xsl:text>
                    <xsl:value-of select="base-uri($doc)"/>
                </xsl:message>
            </xsl:if>
            <xsl:variable name="id" as="xs:string*">
                <xsl:evaluate as="xs:string*" context-item="$doc" expand-text="true"
                    xpath="$identifier"/>
            </xsl:variable>
            <xsl:variable name="old-item" select="$root//*[@xml:id eq $id]"/>
            <xsl:if test="exists($id) and empty($old-item)">
                <xsl:variable name="filename" select="base-uri($doc)"/>
                <item xml:id="{$id}">
                    <idno type="document-identifier">
                        <xsl:value-of select="$id"/>
                    </idno>
                    <idno type="absolute-uri">
                        <xsl:text disable-output-escaping="true">&amp;</xsl:text>
                        <xsl:value-of select="$id"/>
                        <xsl:text disable-output-escaping="true">;</xsl:text>
                    </idno>
                    <ptr type="relative">
                        <xsl:attribute name="target" select="substring($filename, $abspath-length)"
                        />
                    </ptr>
                    <!--
                    <ptr type="absolute">
                        <xsl:attribute name="target">
                            <xsl:text disable-output-escaping="true">&#x26;</xsl:text>
                            <xsl:value-of select="$id"
                                disable-output-escaping="true"/>
                            <xsl:text disable-output-escaping="true">;</xsl:text>
                        </xsl:attribute>
                    </ptr>
                    -->
                    <!-- add so custom meta data -->
                    <xsl:apply-templates mode="registry-meta" select="$doc"/>
                </item>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

    <!-- keep the entity reference -->
    <xsl:template match="idno[@type eq 'absolute-uri']">
        <xsl:variable name="id" select="parent::*/@xml:id"/>
        <idno type="absolute-uri">
            <xsl:text disable-output-escaping="true">&amp;</xsl:text>
            <xsl:value-of select="$id"/>
            <xsl:text disable-output-escaping="true">;</xsl:text>
        </idno>
    </xsl:template>

</xsl:stylesheet>
