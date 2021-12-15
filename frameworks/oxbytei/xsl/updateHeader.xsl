<?xml version="1.0" encoding="UTF-8"?>
<!-- Copyright (c) 2021 Christian Lück

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see http://www.gnu.org/licenses/.

USAGE:
java -jar ~/.m2/repository/net/sf/saxon/Saxon-HE/10.2/Saxon-HE-10.2.jar \
    -xsl:xsl/updateHeader.xsl \
    -s:<local-file-to-be-updated> \
    headerfile=<file-with-central-header-and-tags>
    
In the file with the central header, mark elements with @source="local"
for keeping the local content. Use @source="local*" for keeping all
corresponding elements in the local file, not only the one at exactly this
position. @source="local*" is useful for keeping e.g. several title
elements in the local file, while there's only one in the central file.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns="http://www.tei-c.org/ns/1.0"
    exclude-result-prefixes="xs" xpath-default-namespace="http://www.tei-c.org/ns/1.0" version="3.0">

    <!-- All we need are some identity transformations: -->
    <xsl:mode on-no-match="shallow-copy"/>
    <xsl:mode name="update" on-no-match="shallow-copy"/>
    <xsl:mode name="local" on-no-match="shallow-copy"/>
    <xsl:mode name="remove-tags" on-no-match="shallow-copy"/>

    <!-- path or URI of the central header file -->
    <xsl:param name="headerfile" as="xs:string" required="yes"/>

    <!-- XPath to the <teiHeader> in the central header file -->
    <xsl:param name="headerXPath" as="xs:string" select="'//teiHeader'" required="no"/>

    <!-- whether or not to show debug messages -->
    <xsl:param name="debug" as="xs:boolean" select="true()" required="no"/>

    <xsl:variable name="header" as="node()*">
        <xsl:evaluate expand-text="yes" xpath="concat('doc($file)', $headerXPath)" as="node()*"
            context-item=".">
            <xsl:with-param name="file" select="$headerfile"/>
        </xsl:evaluate>
    </xsl:variable>

    <xsl:template match="teiHeader">
        <xsl:variable name="updated" as="node()">
            <xsl:apply-templates select="$header" mode="update">
                <xsl:with-param name="local-header" as="node()" select="." tunnel="yes"/>
            </xsl:apply-templates>
        </xsl:variable>
        <xsl:apply-templates select="$updated" mode="remove-tags"/>
    </xsl:template>

    <!-- rule matching tagged elements -->
    <xsl:template match="*[matches(@source, '^local\*?')]" mode="update">
        <xsl:param name="local-header" as="node()" tunnel="yes"/>
        <xsl:variable name="tag" as="xs:string" select="@source"/>
        <!-- We want to access the local header at the corresponding location.
            Therefore we need to get the xpath (from root or here: from the teiHeader element)
            to the current element. Then we can select the local node by evaluating the path and
            apply another set of templates on it. -->
        <!-- 1. get the path in the form fileDesc[1]/titleStmt[1]/title[2] -->
        <xsl:variable name="path-all" as="xs:string" select="
                string-join(
                for $node in ancestor-or-self::*[ancestor::teiHeader]
                return
                    concat(name($node), '[', count(preceding-sibling::*[name() eq name($node)]) + 1, ']'),
                '/')"/>
        <!-- If tagged local* then remove the position information on the last path segment. -->
        <xsl:variable name="path" as="xs:string" select="
                if ($tag eq 'local*') then
                    replace($path-all, '\[[0-9]+\]$', '')
                else
                    $path-all"/>
        <xsl:if test="$debug">
            <xsl:message>
                <xsl:text>Replacing </xsl:text>
                <xsl:value-of select="$path"/>
                <xsl:text> with local contents.</xsl:text>
            </xsl:message>
        </xsl:if>
        <!-- 2. access the local header at the corresponding location -->
        <xsl:variable name="local-node" as="node()*">
            <xsl:evaluate expand-text="yes" xpath="concat('$local', '/', $path)" as="node()*"
                context-item=".">
                <xsl:with-param name="local" select="$local-header"/>
            </xsl:evaluate>
        </xsl:variable>
        <!-- 3. apply templates from 'local' mode -->
        <!-- To get the spaces right, we copy the whitespace node infront of this node
            between the nodes that come from the local file. -->
        <xsl:apply-templates select="$local-node[1]" mode="local"/>
        <xsl:apply-templates select="$local-node[position() > 1]" mode="local-space">
            <!-- This expression get the directly preceding whitespace node, if present. -->
            <xsl:with-param name="space" as="text()*"
                select="preceding-sibling::node()[1][self::text() and normalize-space(self::text()) eq '']"
            />
        </xsl:apply-templates>
    </xsl:template>

    <!-- template for printing space before the node -->
    <xsl:template match="*" mode="local-space">
        <xsl:param name="space" as="text()*"/>
        <xsl:value-of select="$space"/>
        <xsl:apply-templates select="." mode="local"/>
    </xsl:template>

    <!-- delete tags encoded in @source -->
    <xsl:template match="@source[matches(., '^(local)$')]"/>

</xsl:stylesheet>
