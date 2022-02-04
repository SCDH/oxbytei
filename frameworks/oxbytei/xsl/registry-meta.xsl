<?xml version="1.0" encoding="UTF-8"?>
<!-- rewrite if you need more meta data in your document registry -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns="http://www.tei-c.org/ns/1.0"
    xpath-default-namespace="http://www.tei-c.org/ns/1.0" exclude-result-prefixes="xs" version="3.0"
    default-mode="registry-meta">

    <xsl:mode on-no-match="shallow-skip"/>

    <xsl:template match="titleStmt">
        <title>
            <xsl:value-of select="normalize-space(.)"/>
        </title>
    </xsl:template>

    <xsl:template match="text()"/>

</xsl:stylesheet>
