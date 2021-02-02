<?xml version="1.0"?> 
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/">
  <fromStyle>
    <xsl:apply-templates select="*|@*"/>
  </fromStyle>
</xsl:template>
<xsl:template match="*|@*">
  <xsl:copy>
    <xsl:apply-templates select="*|@*"/>
  </xsl:copy>
</xsl:template>
</xsl:stylesheet>
