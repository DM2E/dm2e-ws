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
    <xsl:apply-templates select="/tei:TEI/tei:teiHeader/tei:fileDesc/tei:titleStmt"/>
  </xsl:template>
  <xsl:template match="/tei:TEI/tei:teiHeader/tei:fileDesc/tei:titleStmt">
    <rdf:RDF>
      <xsl:for-each select="../tei:publicationStmt/tei:idno">
        <edm:ProvidedCHO>
          <xsl:attribute name="rdf:about">
            <xsl:for-each select=".">
              <xsl:if test="position() = 1">
                <xsl:value-of select="."/>
              </xsl:if>
            </xsl:for-each>
          </xsl:attribute>
          <dc:creator>
            <xsl:for-each select="../../tei:titleStmt/tei:author/tei:name/@key">
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
            <xsl:for-each select="../../tei:titleStmt/tei:author/tei:name/tei:surname">
              <xsl:value-of select="."/>
            </xsl:for-each>
            <xsl:for-each select="../../tei:titleStmt/tei:author/tei:name/tei:forename">
              <xsl:value-of select="."/>
            </xsl:for-each>
          </dc:creator>
          <xsl:for-each select="../../../tei:profileDesc/tei:langUsage/tei:language">
            <dc:language>
              <xsl:value-of select="."/>
            </dc:language>
          </xsl:for-each>
          <xsl:for-each select="../../tei:sourceDesc/tei:biblFull/tei:publicationStmt/tei:publisher">
            <dc:publisher>
              <xsl:value-of select="."/>
            </dc:publisher>
          </xsl:for-each>
          <xsl:for-each select="../tei:availability/tei:p">
            <xsl:if test="position() = 1">
              <dc:rights>
                <xsl:value-of select="."/>
              </dc:rights>
            </xsl:if>
          </xsl:for-each>
          <xsl:for-each select="../../../tei:profileDesc/tei:textClass/tei:keywords/tei:term">
            <dc:subject>
              <xsl:value-of select="."/>
            </dc:subject>
          </xsl:for-each>
          <xsl:for-each select="../../tei:titleStmt/tei:title">
            <dc:title>
              <xsl:value-of select="."/>
            </dc:title>
          </xsl:for-each>
          <edm:type>TEXT</edm:type>
          <edm:currentLocation>
            <xsl:attribute name="rdf:about">http://LOOK.ME.UP</xsl:attribute>
            <xsl:if test="../../tei:sourceDesc/tei:biblFull/tei:notesStmt/tei:note/tei:name/@type = 'repository'">
              <xsl:for-each select="../../tei:sourceDesc/tei:biblFull/tei:notesStmt/tei:note/tei:name[@type = 'repository']">
                <skos:prefLabel>
                  <xsl:value-of select="."/>
                </skos:prefLabel>
              </xsl:for-each>
            </xsl:if>
          </edm:currentLocation>
        </edm:ProvidedCHO>
      </xsl:for-each>
      <edm:Place>
        <xsl:attribute name="rdf:about">http://dbpedia.org/<xsl:for-each select="../tei:sourceDesc/tei:biblFull/tei:publicationStmt/tei:pubPlace">
            <xsl:if test="position() = 1">
              <xsl:value-of select="."/>
            </xsl:if>
          </xsl:for-each>
        </xsl:attribute>
        <skos:prefLabel>http://dbpedia.org/<xsl:for-each select="../tei:sourceDesc/tei:biblFull/tei:publicationStmt/tei:pubPlace">
            <xsl:value-of select="."/>
          </xsl:for-each>
        </skos:prefLabel>
      </edm:Place>
      <xsl:for-each select="../tei:publicationStmt/tei:idno">
        <xsl:if test="@type = 'DTAID'">
          <ore:Aggregation>
            <xsl:if test="@type = 'DTAID'">
              <xsl:attribute
                  name="rdf:about">http://deutschestextarchiv.de/<xsl:for-each select=".">
                  <xsl:if test="position() = 1">
                    <xsl:value-of select="."/>
                  </xsl:if>
                </xsl:for-each>
              </xsl:attribute>
            </xsl:if>
            <edm:aggregatedCHO>
              <xsl:attribute
                  name="rdf:resource">http://deutschestextarchiv.de/<xsl:for-each select=".">
                  <xsl:if test="position() = 1">
                    <xsl:value-of select="."/>
                  </xsl:if>
                </xsl:for-each>

              </xsl:attribute>http://deutschestextarchiv.de/<xsl:for-each select=".">
                <xsl:value-of select="."/>
              </xsl:for-each>
            </edm:aggregatedCHO>
            <xsl:for-each select="../tei:publisher">
              <xsl:if test="position() = 1">
                <edm:dataProvider>
                  <xsl:value-of select="."/>
                </edm:dataProvider>
              </xsl:if>
            </xsl:for-each>
            <xsl:for-each select="../../tei:sourceDesc/tei:biblFull/tei:notesStmt/tei:note/tei:name">
              <xsl:if test="position() = 1">
                <edm:provider>
                  <xsl:value-of select="."/>
                </edm:provider>
              </xsl:if>
            </xsl:for-each>
            <xsl:for-each select="../tei:availability/tei:p/tei:ref">
              <xsl:if test="position() = 1">
                <edm:rights>
                  <xsl:value-of select="."/>
                </edm:rights>
              </xsl:if>
            </xsl:for-each>
          </ore:Aggregation>
        </xsl:if>
      </xsl:for-each>
    </rdf:RDF>
  </xsl:template>
</xsl:stylesheet>
