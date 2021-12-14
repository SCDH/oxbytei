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

		    Copyright (c) 2021 Christian Lück

		    This program is free software: you can
		    redistribute it and/or modify it under the terms
		    of the GNU General Public License as published by
		    the Free Software Foundation, either version 3 of
		    the License, or (at your option) any later
		    version.

		    This program is distributed in the hope that it
		    will be useful, but WITHOUT ANY WARRANTY; without
		    even the implied warranty of MERCHANTABILITY or
		    FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
		    General Public License for more details.

		    You should have received a copy of the GNU General
		    Public License along with this program.  If not,
		    see http://www.gnu.org/licenses/


		    Included Software and other Material

		    The included languageicon icon was desigend by
		    Onur Mustak Cobanli an is distributed on
		    http://languageicon.org by under a CC licence with
		    Relax-Attribution term.

		    The framework ships with a copy of ediarum.JAR,
		    distributed at
		    https://github.com/ediarum/ediarum.JAR under the
		    terms of the GPL v3, written by Martin Fechner and
		    copyrighted by the Berlin-Brandenburg Academy of
		    Sciences and Humanities.
		</xt:description>
		<xt:license>
		    <xsl:value-of select="unparsed-text('../LICENSE')"/>
		</xt:license>
	    </xt:extension>
	</xt:extensions>
    </xsl:template>

</xsl:stylesheet>


