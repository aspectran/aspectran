<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xalan="http://xml.apache.org/xalan"
	exclude-result-prefixes="xalan">

	<xsl:output method="html" indent="yes" encoding="utf-8" />

<!-- <xsl:import href="../layout/frame.xsl" /> -->
<!-- 	<xsl:import href="../common/templates.xsl" /> -->

	<xsl:template name="xui.frame.js.private">
		<script type="text/javascript">
			<![CDATA[
			]]>
		</script>
	</xsl:template>

	<xsl:template name="xui.frame.body.center">
		<pre>
			<xsl:template match="@*|node()">
				<xsl:copy>
					<xsl:apply-templates select="@*|node()" />
				</xsl:copy>
			</xsl:template>
		</pre>
	</xsl:template>
</xsl:stylesheet>