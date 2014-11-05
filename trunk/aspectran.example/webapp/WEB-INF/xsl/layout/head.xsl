<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xalan="http://xml.apache.org/xalan"
	exclude-result-prefixes="xalan">

	<xsl:template name="xui.frame.head.meta">
		<xsl:call-template name="xui.frame.meta.public" />
		<xsl:call-template name="xui.frame.meta.private" />
	</xsl:template>

	<xsl:template name="xui.frame.meta.public">
		<meta http-equiv="Cache-Control" content="no-cache" />
		<meta http-equiv="Pragma" content="no-cache" />
		<meta http-equiv="Expires" content="0" />
	</xsl:template>

	<xsl:template name="xui.frame.meta.private" />


	<xsl:template name="xui.frame.head.title">
		<title>HelloD - SMS - Site Management System</title>
	</xsl:template>

	<xsl:template name="xui.frame.head.css">
		<xsl:call-template name="xui.frame.css.public" />
		<xsl:call-template name="xui.frame.css.private" />
	</xsl:template>

	<xsl:template name="xui.frame.head.js">
		<xsl:call-template name="xui.frame.js.public" />
		<xsl:call-template name="xui.frame.js.private" />
	</xsl:template>

	<xsl:template name="xui.frame.css.public">
		<link href="http://bo.hellotvi.com/hellod-admin/css/layout.css"
			type="text/css" rel="STYLESHEET" />
	</xsl:template>

	<xsl:template name="xui.frame.css.private"></xsl:template>

	<xsl:template name="xui.frame.js.public">
		<SCRIPT language="JavaScript" type="text/javascript"
			src="http://bo.hellotvi.com/hellod-admin/js/common.js">
		</SCRIPT>
		<SCRIPT language="JavaScript" type="text/javascript"
			src="http://bo.hellotvi.com/hellod-admin/js/script.js">
		</SCRIPT>
	</xsl:template>

	<xsl:template name="xui.frame.js.private"></xsl:template>

	<xsl:template name="xui.frame.head.layout">
		<head>
			<!-- meta tag는 head section의 최상단에 위치해야 함 -->
			<xsl:call-template name="xui.frame.head.meta" />
			<xsl:call-template name="xui.frame.head.title" />
			<xsl:call-template name="xui.frame.head.css" />
			<xsl:call-template name="xui.frame.head.js" />
		</head>
	</xsl:template>
</xsl:stylesheet>