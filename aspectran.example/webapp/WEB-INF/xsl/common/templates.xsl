<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xalan="http://xml.apache.org/xalan"
	exclude-result-prefixes="xalan">

	<xsl:template name="common.format.yyyymmddhhmiss">
		<xsl:param name="dttm" />
		<xsl:value-of select="concat(substring($dttm, 1, 4), 
								'.', substring($dttm, 5, 2), 
								'.', substring($dttm, 7, 2),
								' ', substring($dttm, 9, 2),
								':', substring($dttm, 11, 2),
								':', substring($dttm, 13, 2))" />
	</xsl:template>

	<xsl:template name="common.format.yyyymmddhhmi">
		<xsl:param name="dttm" />
		<xsl:value-of select="concat(substring($dttm, 1, 4), 
								'.', substring($dttm, 5, 2), 
								'.', substring($dttm, 7, 2),
								' ', substring($dttm, 9, 2),
								':', substring($dttm, 11, 2))" />
	</xsl:template>

</xsl:stylesheet>