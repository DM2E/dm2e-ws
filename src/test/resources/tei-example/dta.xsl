<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet exclude-result-prefixes="tei xml" version="2.0"
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:dcterms="http://purl.org/dc/terms/"
    xmlns:edm="http://www.europeana.eu/schemas/edm/"
    xmlns:ore="http://www.openarchives.org/ore/terms/"
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
    xmlns:skos="http://www.w3.org/2004/02/skos/core#"
    xmlns:tei="http://www.tei-c.org/ns/1.0"
    xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
    xmlns:xalan="http://xml.apache.org/xalan"
    xmlns:xml="http://www.w3.org/XML/1998/namespace" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:variable name="map0">
        <map value="">PND:118563076</map>
        <map value=""/>
    </xsl:variable>
    <xsl:template match="/">
        <xsl:apply-templates select="/tei:TEI/tei:teiHeader"/>
    </xsl:template>
    <xsl:template match="/tei:TEI/tei:teiHeader">
        <rdf:RDF>
            <edm:ProvidedCHO>
                <dc:creator>
                    <xsl:for-each select="tei:fileDesc/tei:titleStmt/tei:author/tei:name/@key">
                        <xsl:variable name="idx1" select="index-of($map0/map, normalize-space())"/>
                        <xsl:choose>
                            <xsl:when test="$idx1 > 0">
                                <xsl:value-of select="$map0/map[$idx1]/@value"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="."/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:for-each>
                    <xsl:for-each select="tei:fileDesc/tei:titleStmt/tei:author/tei:name/tei:surname">
                        <xsl:value-of select="."/>
                    </xsl:for-each>
                    <xsl:for-each select="tei:fileDesc/tei:titleStmt/tei:author/tei:name/tei:forename">
                        <xsl:value-of select="."/>
                    </xsl:for-each>
                </dc:creator>
                <xsl:for-each select="tei:profileDesc/tei:langUsage/tei:language">
                    <dc:language>
                        <xsl:value-of select="."/>
                    </dc:language>
                </xsl:for-each>
                <xsl:for-each select="tei:fileDesc/tei:sourceDesc/tei:biblFull/tei:publicationStmt/tei:publisher">
                    <dc:publisher>
                        <xsl:value-of select="."/>
                    </dc:publisher>
                </xsl:for-each>
                <xsl:for-each select="tei:profileDesc/tei:textClass/tei:keywords/tei:term">
                    <dc:subject>
                        <xsl:value-of select="."/>
                    </dc:subject>
                </xsl:for-each>
                <xsl:for-each select="tei:fileDesc/tei:titleStmt/tei:title">
                    <dc:title>
                        <xsl:value-of select="."/>
                    </dc:title>
                </xsl:for-each>
                <edm:type>TEXT</edm:type>
            </edm:ProvidedCHO>
            <ore:Aggregation>
                <xsl:if test="(tei:fileDesc/tei:publicationStmt/tei:idno/@type = 'DTAID')">
                    <xsl:attribute name="rdf:about">http://deutschestextarchiv.de/</xsl:attribute>
                    <xsl:for-each select="tei:fileDesc/tei:publicationStmt/tei:idno">
                        <xsl:if test="position() = 1">
                            <xsl:attribute name="rdf:about">
                                <xsl:value-of select="TEST"/>
                            </xsl:attribute>
                        </xsl:if>
                    </xsl:for-each>
                </xsl:if>
                <xsl:if test="tei:fileDesc/tei:publicationStmt/tei:idno/@type = 'DTAID'">
                    <edm:aggregatedCHO>
                        <xsl:for-each select="tei:fileDesc/tei:publicationStmt/tei:idno[@type='DTAID']">
                            <!-- <xsl:attribute name="rdf:resource"> -->
                            <!--     <xsl:value-of select="concat('http://deutschestextarchiv/', current())"/> -->
                            <!-- </xsl:attribute> -->
                             <!-- <xsl:value-of select="concat('http://deutschestextarchiv/', current())"/> -->
                             <!-- <xsl:attribute name="rdf:about">http://deutschestextarchiv.de/</xsl:attribute> -->
                             <xsl:for-each select="tei:fileDesc/tei:publicationStmt/tei:idno">
                                 <xsl:if test="position() = 1">
                                     <!-- <xsl:attribute name="rdf:about"> -->
                                         <!-- <xsl:value-of select="if (@type='DTAID') then 'FOO' else 'BAR'"/> -->
                                     <!-- </xsl:attribute> -->
                                 </xsl:if>
                             </xsl:for-each>
                        </xsl:for-each>
                    </edm:aggregatedCHO>
                </xsl:if>
                <xsl:if test="tei:fileDesc/tei:publicationStmt/tei:idno/@type = 'URN'">
                    <edm:hasView>urn-resolve.../<xsl:for-each select="tei:fileDesc/tei:publicationStmt/tei:idno[@type = 'URN']">
                            <xsl:value-of select="."/>
                        </xsl:for-each>
                    </edm:hasView>
                </xsl:if>
                <xsl:for-each select="tei:fileDesc/tei:publicationStmt/tei:publisher">
                    <xsl:if test="position() = 1">
                        <edm:dataProvider>
                            <xsl:value-of select="."/>
                        </edm:dataProvider>
                    </xsl:if>
                </xsl:for-each>
                <xsl:for-each select="tei:fileDesc/tei:publicationStmt/tei:availability/tei:p/tei:ref">
                    <xsl:if test="position() = 1">
                        <edm:rights>
                            <xsl:value-of select="."/>
                        </edm:rights>
                    </xsl:if>
                </xsl:for-each>
            </ore:Aggregation>
        </rdf:RDF>
    </xsl:template>
</xsl:stylesheet>

