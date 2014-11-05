<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
				xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
			    xmlns:xalan="http://xml.apache.org/xalan"
			    exclude-result-prefixes="xalan">
    
    <xsl:output method="xml" indent="yes"  encoding="utf-8"/>

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
    </xsl:template>
</xsl:stylesheet>
