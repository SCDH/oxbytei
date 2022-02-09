<?xml version="1.0" encoding="UTF-8"?>
<!-- xslt for deleting the whole doc. This produces an empty document.
    This is handy for deleting anchors with XSLTOperation. -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="xs" version="2.0">

    <xsl:template match="/ | *"/>

</xsl:stylesheet>
