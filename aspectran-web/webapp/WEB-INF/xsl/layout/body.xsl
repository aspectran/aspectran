<?xml version="1.0" encoding="UTF-8"?>

<!DOCTYPE stylesheet[
<!ENTITY  br "<br/>">
<!ENTITY  sp "&#160;">
]>

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xalan="http://xml.apache.org/xalan"
	exclude-result-prefixes="xalan">

	<xsl:template name="xui.frame.body.top">
		<xsl:param name="topno" />
		<!--	[s]Logo -->
		<div class='headerLogo'>
			<a href='/'>
				<img
					src="http://files.hellotvi.com/images/admin/logo.gif" width="188"
					height="82" alt='HELLO D' />
			</a>
			<br />
		</div>
		<!--	[e]Logo -->
		<!--	[s]Btn -->
		<div class='headerBtn'>
			<ul>
				<li>
					<a href="/admin/logout">
						<img
							src='http://files.hellotvi.com/images/admin/btn_logout.gif'
							alt='로그아웃' />
					</a>
				</li>
				<li>
					<a href="/">
						<img
							src='http://files.hellotvi.com/images/admin/btn_home.gif'
							alt='HOME' />
					</a>
				</li>
			</ul>
		</div>
		<!--	[e]Btn -->
		<!--	[s]TopMenu -->
		<div class="headerMenu">
			<ul>
				<xsl:if test="$topno=1">
					<xsl:attribute name="class">current</xsl:attribute>
				</xsl:if>
				<li>
					<a href="../admin/list" class="headerMenu">
						<span>
							<xsl:if test="$topno=1">
								<xsl:attribute name="class">current</xsl:attribute>
							</xsl:if>
							계정관리
						</span>
					</a>
				</li>
				<li>|</li>
				<li>
					<a href="http://bo.hellotvi.com/cms/categoryList"
						class="headerMenu">
						<span>
							<xsl:if test="$topno=2">
								<xsl:attribute name="class">current</xsl:attribute>
							</xsl:if>
							CMS
						</span>
					</a>
				</li>
				<li>|</li>
				<li>
					<a href="../common/main" class="headerMenu">
						<span>
							<xsl:if test="$topno=3">
								<xsl:attribute name="class">current</xsl:attribute>
							</xsl:if>
							SMS
						</span>
					</a>
				</li>
				<li>|</li>
				<li>
					<a href="../weblog/topStatus" class="headerMenu">
						<span>
							<xsl:if test="$topno=4">
								<xsl:attribute name="class">current</xsl:attribute>
							</xsl:if>
							로그통계
						</span>
					</a>
				</li>
				<li>|</li>
				<li>
					<a href="../banner/channelList"
						class="headerMenu">
						<span>
							<xsl:if test="$topno=5">
								<xsl:attribute name="class">current</xsl:attribute>
							</xsl:if>
							디자인관리
						</span>
					</a>
				</li>
			</ul>
		</div>
		<!--	[e]TopMenu -->
	</xsl:template>
	<xsl:template name="xui.frame.body.left" />
	<xsl:template name="xui.frame.body.center" />
	<xsl:template name="xui.frame.body.right" />
	<xsl:template name="xui.frame.body.bottom"></xsl:template>
</xsl:stylesheet>