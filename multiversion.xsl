<?xml version="1.0" encoding="utf-8"?>
<!-- make a plugin description for each version of oxygen given in the oxygen-versions parameter -->
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:xt="http://www.oxygenxml.com/ns/extension"
    xpath-default-namespace="http://www.oxygenxml.com/ns/extension"
    version="3.0">

   <xsl:output method="xml" indent="true"/>

   <xsl:mode on-no-match="shallow-copy"/>

   <xsl:param name="oxygen-versions" required="true"/>

   <xsl:param name="artifact" required="true"/>

   <xsl:template match="extension">
      <xsl:variable name="context" select="."/>
      <xsl:for-each select="tokenize($oxygen-versions, '\|')">
	 <xsl:variable name="plugin-version" select="tokenize(., ':')[1]"/>
	 <xsl:variable name="versions" select="tokenize(., ':')[2]"/>
	 <xsl:for-each select="tokenize($versions, ',')">
	    <xsl:variable name="oxygen-version" select="."/>
	    <xsl:copy select="$context">
	       <xsl:apply-templates select="node() | attribute()">
		  <xsl:with-param name="oxygen-version" select="$oxygen-version" tunnel="true"/>
		  <xsl:with-param name="plugin-version" select="$plugin-version" tunnel="true"/>
	       </xsl:apply-templates>
	    </xsl:copy>
	 </xsl:for-each>
      </xsl:for-each>
   </xsl:template>

   <!-- appending the version to the id is not nice. But same id for all versions doesn't work. -->
   <xsl:template match="extension/@id">
      <xsl:param name="oxygen-version" as="xs:string" tunnel="true"/>
      <xsl:attribute name="id" select="concat(., $oxygen-version)"/>
   </xsl:template>

   <xsl:template match="location/@href">
      <xsl:param name="plugin-version" as="xs:string" tunnel="true"/>
      <xsl:attribute name="href" select="replace(., concat($artifact, '[^-]*'), concat($artifact, $plugin-version))"/>
   </xsl:template>

   <xsl:template match="oxy_version/text()">
      <xsl:param name="oxygen-version" as="xs:string" tunnel="true"/>
      <xsl:value-of select="$oxygen-version"/>
   </xsl:template>

</xsl:stylesheet>
