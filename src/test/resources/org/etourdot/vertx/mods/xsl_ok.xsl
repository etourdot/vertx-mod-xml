<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:param name="filename"/>
    <xsl:template match="/">
        <xsl:element name="toto"/>
        <xsl:value-of select="$filename"/>
    </xsl:template>
</xsl:stylesheet>
