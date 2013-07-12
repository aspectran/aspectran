<?xml version="1.0" encoding="UTF-8"?>

<!DOCTYPE stylesheet[
<!ENTITY  br "<br/>">
<!ENTITY  sp "&#160;">
]>

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xalan="http://xml.apache.org/xalan"
	exclude-result-prefixes="xalan">

	<xsl:import href="head.xsl" />
	<xsl:import href="body.xsl" />

	<xsl:variable name="menuno" select="2"/>

	<xsl:template name="xui.frame.body.left">
		<table width="188" height="100%" cellpadding="0" cellspacing="0"
			border="0" class="leftMenu">
			<tr>
				<td>
					<img
						src="http://files.hellotvi.com/images/admin/leftbox1_s.gif" />
				</td>
			</tr>
			<tr>
				<td class='list'>
					<ul class='markermenu'>

						<!--				
							<li class='tis'><a href='#'>관리자 관리</a></li>
							<li class='smenu'><xsl:if test="$menuno=1"><xsl:attribute name="class">smenu current</xsl:attribute></xsl:if>
							<a href="../admin/list">관리자 조회</a>
							</li>
							<li class='smenu'><xsl:if test="$menuno=2"><xsl:attribute name="class">smenu current</xsl:attribute></xsl:if>
							<a href="../admin/write">관리자 등록</a>
							</li>
						-->

						<li class='tis'>
							<a href='#'>FAQ 관리</a>
						</li>
						<li class='smenu'>
							<xsl:if test="$menuno=1">
								<xsl:attribute name="class">smenu current</xsl:attribute>
							</xsl:if>
							<a href="../faq/list">FAQ 검색</a>
						</li>
						<li class='smenu'>
							<xsl:if test="$menuno=2">
								<xsl:attribute name="class">smenu current</xsl:attribute>
							</xsl:if>
							<a href="../faq/write">FAQ 등록</a>
						</li>

						<li class='ti'>
							<a href='#'>고객의 소리 관리</a>
						</li>
						<li class='smenu'>
							<xsl:if test="$menuno=3">
								<xsl:attribute name="class">smenu current</xsl:attribute>
							</xsl:if>
							<a href="../councel/list">상담 검색</a>
						</li>
						<li class='smenu'>
							<xsl:if test="$menuno=4">
								<xsl:attribute name="class">smenu current</xsl:attribute>
							</xsl:if>
							<a href="../councel/stat">상담 현황</a>
						</li>

						<li class='ti'>
							<a href='#'>이벤트 관리</a>
						</li>
						<li class='smenu'>
							<xsl:if test="$menuno=5">
								<xsl:attribute name="class">smenu current</xsl:attribute>
							</xsl:if>
							<a href="../event/list">이벤트 검색</a>
						</li>
						<li class='smenu'>
							<xsl:if test="$menuno=6">
								<xsl:attribute name="class">smenu current</xsl:attribute>
							</xsl:if>
							<a href="../event/write.form">이벤트 등록</a>
						</li>

						<li class='ti'>
							<a href='#'>공지사항 관리</a>
						</li>
						<li class='smenu'>
							<xsl:if test="$menuno=7">
								<xsl:attribute name="class">smenu current</xsl:attribute>
							</xsl:if>
							<a href="../notice/list">공지사항 검색</a>
						</li>
						<li class='smenu'>
							<xsl:if test="$menuno=8">
								<xsl:attribute name="class">smenu current</xsl:attribute>
							</xsl:if>
							<a href="../notice/write">공지사항 등록</a>
						</li>

						<li class='ti'>
							<a href='#'>HELLOD POLL</a>
						</li>
						<li class='smenu'>
							<xsl:if test="$menuno=9">
								<xsl:attribute name="class">smenu current</xsl:attribute>
							</xsl:if>
							<a href="../poll/list">설문 검색</a>
						</li>
						<li class='smenu'>
							<xsl:if test="$menuno=10">
								<xsl:attribute name="class">smenu current</xsl:attribute>
							</xsl:if>
							<a href="../poll/view">설문 등록</a>
						</li>

						<li class='ti'>
							<a href='#'>20자평 관리</a>
						</li>
						<li class='smenu'>
							<xsl:if test="$menuno=11">
								<xsl:attribute name="class">smenu current</xsl:attribute>
							</xsl:if>
							<a href="../comment/list">20자평 조회</a>
						</li>

					</ul>
				</td>
			</tr>
			<tr>
				<td>
					<img
						src="http://files.hellotvi.com/images/admin/leftbox1_e.gif" />
				</td>
			</tr>
		</table>
	</xsl:template>

	<xsl:template name="xui.frame.body.layout">
		<body>
			<div id="warp">
				<!-- [s]header -->
				<div id="header">
					<xsl:call-template name="xui.frame.body.top">
						<xsl:with-param name="topno" select="3" />
					</xsl:call-template>
				</div>
				<!-- [e]header -->
				<!-- [s]Middle -->
				<div id="middle">
					<div id="left">
						<xsl:call-template name="xui.frame.body.left" />
					</div>
					<div id="content">
						<xsl:call-template name="xui.frame.body.center" />
					</div>

					<xsl:call-template name="xui.frame.body.right" />
					<xsl:call-template name="xui.frame.body.bottom" />
				</div>
				<!-- [e]Middle -->
			</div>
		</body>
	</xsl:template>

	<xsl:template match="/">
		<html>
			<xsl:call-template name="xui.frame.head.layout" />
			<xsl:call-template name="xui.frame.body.layout" />
		</html>
	</xsl:template>

</xsl:stylesheet>