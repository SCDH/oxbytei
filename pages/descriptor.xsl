<?xml version="1.0" encoding="UTF-8"?>
<!-- generate an oxygen plugin download descriptor file from the pom file -->
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xt="http://www.oxygenxml.com/ns/extension"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xpath-default-namespace="http://maven.apache.org/POM/4.0.0"
    version="2.0">
    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="/">
	<xt:extensions 
	    xsi:schemaLocation="http://www.oxygenxml.com/ns/extension http://www.oxygenxml.com/ns/extension/extensions.xsd">
	    <xt:extension id="{/project/artifactId}">
		<!---xt:location href="https://github.com/scdh/oxbytei/releases/tag/{/project/version}/{/project/artifactId}-{/project/version}-package.zip"/-->
		<xt:location href="https://scdh.zivgitlabpages.uni-muenster.de/tei-processing/oxbytei/{/project/artifactId}-{/project/version}-package.zip"/>
		<xt:version><xsl:value-of select="/project/version"/></xt:version>
		<xt:oxy_version>23.1+</xt:oxy_version>
		<xt:type>framework</xt:type>
		<xt:author>Christian Lück</xt:author>
		<xt:name>oXbytei</xt:name>
		<xt:description xmlns="http://www.w3.org/1999/xhtml">
		    An &lt;oXygen/&gt; author framework extending TEI P5,
		    developed at SCDH, Westfälische Wilhelms-Universität
		    Münster. It is configured by the TEI's header and
		    offers high level functions.

		    Visit
		    https://github.com/scdh/oxbytei/releases/tag/{/project/version}
		    to see what's new.
		</xt:description>
		<xt:license>
		    <xsl:value-of select="unparsed-text('../LICENSE')"/>
		</xt:license>
	    </xt:extension>
	</xt:extensions>
    </xsl:template>

</xsl:stylesheet>


