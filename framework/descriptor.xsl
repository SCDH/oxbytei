<?xml version="1.0" encoding="UTF-8"?>
<!-- generate an oxygen plugin download descriptor file from the pom file -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xt="http://www.oxygenxml.com/ns/extension"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xpath-default-namespace="http://maven.apache.org/POM/4.0.0" version="2.0">
	<xsl:output method="xml" indent="yes"/>

	<xsl:template match="/">
		<xt:extensions
			xsi:schemaLocation="http://www.oxygenxml.com/ns/extension http://www.oxygenxml.com/ns/extension/extensions.xsd">
			<xt:extension id="{/project/artifactId}">
				<xt:location
					href="https://github.com/SCDH/oxbytei/releases/download/{/project/version}/{/project/artifactId}-{/project/version}-package.zip"/>
				<!--xt:location href="https://SCDH.zivgitlabpages.uni-muenster.de/tei-processing/oxbytei/{/project/artifactId}-{/project/version}-package.zip"/-->
				<xt:version>
					<xsl:value-of select="/project/version"/>
				</xt:version>
				<xt:oxy_version>23.1+</xt:oxy_version>
				<xt:type>framework</xt:type>
				<xt:author>Christian Lück</xt:author>
				<xt:name>oXbytei</xt:name>
				<xt:description xmlns="http://www.w3.org/1999/xhtml">
					<html>
						<head>
							<title>oXbytei framework</title>
						</head>
						<body>
							<div>
								<p>oXbytei is an &lt;oXygen/&gt; framework extending TEI P5,
									developed at SCDH, Westfälische Wilhelms-Universität Münster. It
									is configured by the document's header and offers high level
									functions.</p>
								<p>Visit the <a
										href="https://github.com/SCDH/oxbytei/releases/tag/{/project/version}"
										>release page</a> to see what's new.</p>
								<p>Copyright (c) 2021, 2022 Christian Lück</p>

								<p>This program is free software: you can redistribute it and/or
									modify it under the terms of the GNU General Public License as
									published by the Free Software Foundation, either version 3 of
									the License, or (at your option) any later version.<br/> This
									program is distributed in the hope that it will be useful, but
									WITHOUT ANY WARRANTY; without even the implied warranty of
									MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
									General Public License for more details.<br/> You should have
									received a copy of the GNU General Public License along with
									this program. If not, see <a href="http://www.gnu.org/licenses/"
										>http://www.gnu.org/licenses/</a></p>
							</div>

							<div>
								<head>Included Software and other Material</head>

								<p>The included languageicon icon was desigend by Onur Mustak
									Cobanli an is distributed on <a href="http://languageicon.org"
										>http://languageicon.org</a> by under a CC licence with
									Relax-Attribution term.</p>

								<p>The framework ships with a copy of ediarum.JAR, distributed at <a
										href="https://github.com/ediarum/ediarum.JAR"
										>https://github.com/ediarum/ediarum.JAR</a> under the terms
									of the GPL v3, written by Martin Fechner and copyrighted by the
									Berlin-Brandenburg Academy of Sciences and Humanities.</p>
							</div>
						</body>
					</html>
				</xt:description>
				<xt:license>
					<xsl:text disable-output-escaping="yes">&lt;![CDATA[</xsl:text>
					<xsl:value-of select="unparsed-text('../LICENSE')"/>
					<xsl:text disable-output-escaping="yes">]]&gt;</xsl:text>
				</xt:license>
			</xt:extension>
		</xt:extensions>
	</xsl:template>

</xsl:stylesheet>
