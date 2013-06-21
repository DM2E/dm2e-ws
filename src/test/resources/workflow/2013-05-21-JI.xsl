<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet exclude-result-prefixes="tei xml" version="2.0"
  xmlns:bibo="http://purl.org/ontology/bibo/"
  xmlns:dc="http://purl.org/dc/elements/1.1/"
  xmlns:dcterms="http://purl.org/dc/terms/"
  xmlns:dm2e="http://onto.dm2e.eu/schemas/dm2e/0.3/"
  xmlns:edm="http://www.europeana.eu/schemas/edm/"
  xmlns:foaf="http://xmlns.com/foaf/0.1/"
  xmlns:korbo="http://purl.org/net7/korbo/vocab#"
  xmlns:ore="http://www.openarchives.org/ore/terms/"
  xmlns:owl="http://www.w3.org/2002/07/owl#"
  xmlns:pro="http://purl.org/spar/pro/"
  xmlns:rdaGr2="http://RDVocab.info/ElementsGr2/"
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
  xmlns:skos="http://www.w3.org/2004/02/skos/core#"
  xmlns:tei="http://www.tei-c.org/ns/1.0"
  xmlns:void="http://rdfs.org/ns/void#"
  xmlns:wgs84_pos="http://www.w3.org/2003/01/geo/wgs84_pos#"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:xml="http://www.w3.org/XML/1998/namespace" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:param name="baseURI">http://data.dm2e.eu/data/</xsl:param>
  <xsl:template match="/">
    <xsl:apply-templates select="/tei:TEI"/>
  </xsl:template>
  <xsl:template match="/tei:TEI">
    <rdf:RDF>
      <ore:Aggregation>
        <xsl:attribute name="rdf:about">
          <xsl:text>http://data.dm2e.eu/data/</xsl:text>
          <xsl:text>aggregation/</xsl:text>
          <xsl:text>uib/</xsl:text>
          <xsl:text>wittgenstein/</xsl:text>
          <xsl:for-each select="tei:text/tei:body/tei:ab/@xml:id">
            <xsl:if test="position() = 1">
              <xsl:value-of select="substring-before(.,',')"/>
            </xsl:if>
          </xsl:for-each>
          <xsl:text>CustomFunctionNeeded</xsl:text>
        </xsl:attribute>
        <edm:aggregatedCHO>
          <edm:ProvidedCHO>
            <xsl:attribute name="rdf:about">
              <xsl:text>http://data.dm2e.eu/data/</xsl:text>
              <xsl:text>item/</xsl:text>
              <xsl:text>uib/</xsl:text>
              <xsl:text>wittgenstein/</xsl:text>
              <xsl:for-each select="tei:text/tei:body/tei:ab/@xml:id">
                <xsl:if test="position() = 1">
                  <xsl:value-of select="substring-before(.,',')"/>
                </xsl:if>
              </xsl:for-each>
              <xsl:text>CustomFunctionNeeded</xsl:text>
            </xsl:attribute>
            <edm:type>
              <xsl:text>TEXT</xsl:text>
            </edm:type>
            <dc:type>
              <xsl:attribute name="rdf:resource">
                <xsl:text>http://onto.dm2e.eu/schemas/dm2e/0.3/Paragraph</xsl:text>
              </xsl:attribute>
            </dc:type>
            <dc:language>
              <xsl:if test="tei:text/tei:body/tei:ab/@xml:lang">
                <xsl:attribute name="xml:lang">
                  <xsl:for-each select="tei:text/tei:body/tei:ab/@xml:lang">
                    <xsl:if test="position() = 1">
                      <xsl:value-of select="."/>
                    </xsl:if>
                  </xsl:for-each>
                </xsl:attribute>
              </xsl:if>
            </dc:language>
            <dm2e:isPartOfCHO>
              <edm:ProvidedCHO>
                <xsl:attribute name="rdf:about">
                  <xsl:text>http://data.dm2e.eu/data/</xsl:text>
                  <xsl:text>item/</xsl:text>
                </xsl:attribute>
              </edm:ProvidedCHO>
            </dm2e:isPartOfCHO>
          </edm:ProvidedCHO>
        </edm:aggregatedCHO>
        <edm:provider>
          <OrganisationType>
            <xsl:attribute name="rdf:about">
              <xsl:text>http://dm2e.eu</xsl:text>
            </xsl:attribute>
          </OrganisationType>
        </edm:provider>
        <edm:dataProvider>
          <OrganisationType>
            <xsl:attribute name="rdf:about">
              <xsl:text>@ref</xsl:text>
            </xsl:attribute>
          </OrganisationType>
        </edm:dataProvider>
        <dcterms:rightsHolder>
          <foaf:Organisation>
            <xsl:attribute name="rdf:about">
              <xsl:text>http://data.dm2e.eu/data/</xsl:text>
              <xsl:text>CustomFunctionNeeded/</xsl:text>
              <xsl:for-each select="tei:teiHeader/tei:fileDesc/tei:titleStmt/tei:sponsor/tei:name">
                <xsl:if test="position() = 1">
                  <xsl:value-of select="substring-before(.,' ')"/>
                </xsl:if>
              </xsl:for-each>
            </xsl:attribute>
          </foaf:Organisation>
        </dcterms:rightsHolder>
        <edm:rights>
          <xsl:attribute name="rdf:resource">
            <xsl:text>http://creativecommons.org/licenses/by-nc-sa/</xsl:text>
          </xsl:attribute>
        </edm:rights>
        <edm:isShownBy>
          <edm:WebResource>
            <xsl:attribute name="rdf:about">
              <xsl:text>http://www.wittgensteinsource.org/</xsl:text>
              <xsl:for-each select="tei:text/tei:body/tei:ab/@xml:id">
                <xsl:if test="position() = 1">
                  <xsl:value-of select="."/>
                </xsl:if>
              </xsl:for-each>
              <xsl:text>_n</xsl:text>
            </xsl:attribute>
          </edm:WebResource>
        </edm:isShownBy>
      </ore:Aggregation>
      <ore:Aggregation>
        <xsl:attribute name="rdf:about">
          <xsl:value-of select="$baseURI"/>
          <xsl:text>aggregation/</xsl:text>
          <xsl:text>uib/</xsl:text>
          <xsl:text>wittgenstein/</xsl:text>
          <xsl:for-each select="tei:teiHeader/tei:fileDesc/tei:titleStmt/tei:title/@xml:id">
            <xsl:if test="position() = 1">
              <xsl:value-of select="."/>
            </xsl:if>
          </xsl:for-each>
        </xsl:attribute>
        <edm:aggregatedCHO>
          <edm:ProvidedCHO>
            <xsl:attribute name="rdf:about">
              <xsl:text>http://data.dm2e.eu/data/</xsl:text>
              <xsl:text>item/</xsl:text>
              <xsl:text>uib/</xsl:text>
              <xsl:text>wittgenstein/</xsl:text>
              <xsl:for-each select="tei:teiHeader/tei:fileDesc/tei:titleStmt/tei:title/@xml:id">
                <xsl:if test="position() = 1">
                  <xsl:value-of select="."/>
                </xsl:if>
              </xsl:for-each>
            </xsl:attribute>
            <edm:type>
              <xsl:text>TEXT</xsl:text>
            </edm:type>
            <dc:type>
              <xsl:attribute name="rdf:resource">
                <xsl:text>http://onto.dm2e.eu/schemas/dm2e/0.3/Manuscript</xsl:text>
              </xsl:attribute>
            </dc:type>
            <xsl:for-each select="tei:teiHeader/tei:fileDesc/tei:titleStmt/tei:title">
              <dcterms:title>
                <xsl:attribute name="xml:lang">
                  <xsl:text>eng</xsl:text>
                </xsl:attribute>
                <xsl:value-of select="."/>
              </dcterms:title>
            </xsl:for-each>
            <xsl:for-each select="tei:teiHeader/tei:fileDesc/tei:sourceDesc/tei:p">
              <dcterms:description>
                <xsl:attribute name="xml:lang">
                  <xsl:text>eng</xsl:text>
                </xsl:attribute>
                <xsl:value-of select="."/>
              </dcterms:description>
            </xsl:for-each>
            <xsl:for-each select="tei:text/@xml:lang">
              <dc:language>
                <xsl:attribute name="xml:lang">
                  <xsl:text>de</xsl:text>
                </xsl:attribute>
                <xsl:value-of select="."/>
              </dc:language>
            </xsl:for-each>
            <xsl:for-each select="tei:teiHeader/tei:profileDesc/tei:textClass/tei:keywords/tei:term">
              <dc:subject>
                <xsl:attribute name="xml:lang">
                  <xsl:text>eng</xsl:text>
                </xsl:attribute>
                <xsl:value-of select="."/>
              </dc:subject>
            </xsl:for-each>
            <dm2e:hasPartCHO>
              <edm:ProvidedCHO>
                <xsl:attribute name="rdf:about">
                  <xsl:text>http://data.dm2e.eu/data/</xsl:text>
                  <xsl:text>item/</xsl:text>
                  <xsl:text>uib/</xsl:text>
                  <xsl:text>wittgenstein/</xsl:text>
                  <xsl:for-each select="tei:text/tei:body/tei:ab/@xml:id">
                    <xsl:if test="position() = 1">
                      <xsl:value-of select="substring-before(.,',')"/>
                    </xsl:if>
                  </xsl:for-each>
                  <xsl:text>CustomFunctionNeeded</xsl:text>
                </xsl:attribute>
              </edm:ProvidedCHO>
            </dm2e:hasPartCHO>
            <pro:author>
              <Person>
                <xsl:attribute name="rdf:about">
                  <xsl:text>http://data.dm2e.eu/data/</xsl:text>
                  <xsl:text>uib/</xsl:text>
                  <xsl:text>agent/</xsl:text>
                  <xsl:for-each select="tei:teiHeader/tei:fileDesc/tei:titleStmt/tei:author">
                    <xsl:if test="position() = 1">
                      <xsl:value-of select="substring-before(.,' ')"/>
                    </xsl:if>
                  </xsl:for-each>
                  <xsl:text>_</xsl:text>
                  <xsl:for-each select="tei:teiHeader/tei:fileDesc/tei:titleStmt/tei:author">
                    <xsl:if test="position() = 1">
                      <xsl:value-of select="substring-after(.,' ')"/>
                    </xsl:if>
                  </xsl:for-each>
                </xsl:attribute>
              </Person>
            </pro:author>
          </edm:ProvidedCHO>
        </edm:aggregatedCHO>
        <edm:provider>
          <OrganisationType>
            <xsl:attribute name="rdf:about">
              <xsl:text>http://dm2e.eu</xsl:text>
            </xsl:attribute>
            <skos:prefLabel>
              <xsl:attribute name="xml:lang">
                <xsl:text>eng</xsl:text>
              </xsl:attribute>
              <xsl:text>Digitised Manuscripts to Europeana</xsl:text>
            </skos:prefLabel>
          </OrganisationType>
        </edm:provider>
        <edm:dataProvider>
          <OrganisationType>
            <xsl:if test="tei:teiHeader/tei:fileDesc/tei:titleStmt/tei:editor/tei:orgName/@ref">
              <xsl:attribute name="rdf:about">
                <xsl:for-each select="tei:teiHeader/tei:fileDesc/tei:titleStmt/tei:editor/tei:orgName/@ref">
                  <xsl:if test="position() = 1">
                    <xsl:value-of select="."/>
                  </xsl:if>
                </xsl:for-each>
              </xsl:attribute>
            </xsl:if>
            <xsl:for-each select="tei:teiHeader/tei:fileDesc/tei:titleStmt/tei:editor/tei:orgName">
              <xsl:if test="position() = 1">
                <skos:prefLabel>
                  <xsl:attribute name="xml:lang">
                    <xsl:text>eng</xsl:text>
                  </xsl:attribute>
                  <xsl:value-of select="."/>
                </skos:prefLabel>
              </xsl:if>
            </xsl:for-each>
          </OrganisationType>
        </edm:dataProvider>
        <dcterms:rightsHolder>
          <foaf:Organisation>
            <xsl:attribute name="rdf:about">
              <xsl:text>http://data.dm2e.eu/data/</xsl:text>
              <xsl:text>test/</xsl:text>
              <xsl:for-each select="tei:teiHeader/tei:fileDesc/tei:titleStmt/tei:sponsor/tei:name">
                <xsl:if test="position() = 1">
                  <xsl:value-of select="substring-before(.,' ')"/>
                </xsl:if>
              </xsl:for-each>
            </xsl:attribute>
            <xsl:for-each select="tei:teiHeader/tei:fileDesc/tei:titleStmt/tei:sponsor/tei:name">
              <xsl:if test="position() = 1">
                <skos:prefLabel>
                  <xsl:attribute name="xml:lang">
                    <xsl:text>eng</xsl:text>
                  </xsl:attribute>
                  <xsl:value-of select="."/>
                </skos:prefLabel>
              </xsl:if>
            </xsl:for-each>
          </foaf:Organisation>
        </dcterms:rightsHolder>
        <edm:rights>
          <xsl:attribute name="rdf:resource">
            <xsl:text>http://creativecommons.org/licenses/by-nc-sa/</xsl:text>
          </xsl:attribute>
        </edm:rights>
        <edm:isShownBy>
          <edm:WebResource>
            <xsl:if test="tei:teiHeader/tei:fileDesc/tei:publicationStmt/tei:idno">
              <xsl:attribute name="rdf:about">
                <xsl:for-each select="tei:teiHeader/tei:fileDesc/tei:publicationStmt/tei:idno">
                  <xsl:if test="position() = 1">
                    <xsl:value-of select="."/>
                  </xsl:if>
                </xsl:for-each>
              </xsl:attribute>
            </xsl:if>
          </edm:WebResource>
        </edm:isShownBy>
        <edm:isShownAt>
          <edm:WebResource>
            <xsl:if test="tei:teiHeader/tei:fileDesc/tei:publicationStmt/tei:idno">
              <xsl:attribute name="rdf:about">
                <xsl:for-each select="tei:teiHeader/tei:fileDesc/tei:publicationStmt/tei:idno">
                  <xsl:if test="position() = 1">
                    <xsl:value-of select="."/>
                  </xsl:if>
                </xsl:for-each>
              </xsl:attribute>
            </xsl:if>
          </edm:WebResource>
        </edm:isShownAt>
        <dcterms:creator>
          <foaf:Person>
            <xsl:attribute name="rdf:about">
              <xsl:text>http://data.dm2e.eu/data/</xsl:text>
              <xsl:text>uib/</xsl:text>
              <xsl:text>wittgenstein/</xsl:text>
              <xsl:text>agent/</xsl:text>
              <xsl:for-each select="tei:teiHeader/tei:fileDesc/tei:titleStmt/tei:editor/tei:persName">
                <xsl:if test="position() = 1">
                  <xsl:value-of select="substring-before(.,' ')"/>
                </xsl:if>
              </xsl:for-each>
              <xsl:text>_</xsl:text>
              <xsl:for-each select="tei:teiHeader/tei:fileDesc/tei:titleStmt/tei:editor/tei:persName">
                <xsl:if test="position() = 1">
                  <xsl:value-of select="substring-after(.,' ')"/>
                </xsl:if>
              </xsl:for-each>
            </xsl:attribute>
            <xsl:for-each select="tei:teiHeader/tei:fileDesc/tei:titleStmt/tei:editor/tei:persName">
              <xsl:if test="position() = 1">
                <skos:prefLabel>
                  <xsl:attribute name="xml:lang">
                    <xsl:text>eng</xsl:text>
                  </xsl:attribute>
                  <xsl:value-of select="."/>
                </skos:prefLabel>
              </xsl:if>
            </xsl:for-each>
          </foaf:Person>
        </dcterms:creator>
        <dc:contributor>
          <xsl:for-each select="tei:teiHeader/tei:fileDesc/tei:titleStmt/tei:respStmt/tei:name">
            <foaf:Person>
              <xsl:attribute name="rdf:about">
                <xsl:text>http://data.dm2e.eu/data/</xsl:text>
                <xsl:text>agent/</xsl:text>
                <xsl:text>uib/</xsl:text>
                <xsl:text>wittgenstein/</xsl:text>
                <xsl:for-each select=".">
                  <xsl:if test="position() = 1">
                    <xsl:value-of select="substring-before(.,' ')"/>
                  </xsl:if>
                </xsl:for-each>
                <xsl:text>_</xsl:text>
                <xsl:text>customFunctionNeeded</xsl:text>
              </xsl:attribute>
            </foaf:Person>
          </xsl:for-each>
        </dc:contributor>
      </ore:Aggregation>
    </rdf:RDF>
  </xsl:template>
</xsl:stylesheet>
