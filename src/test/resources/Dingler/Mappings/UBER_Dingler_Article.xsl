<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet exclude-result-prefixes="tei xml" version="2.0"
  xmlns:bibo="http://purl.org/ontology/bibo/"
  xmlns:dc="http://purl.org/dc/elements/1.1/"
  xmlns:dcterms="http://purl.org/dc/terms/"
  xmlns:dm2e="http://onto.dm2e.eu/schemas/dm2e/1.1/"
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

  xmlns:xml="http://www.w3.org/XML/1998/namespace" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  
  <xsl:template name="Article">
    <xsl:param name="theTEIHeader" as="node()"/>
 
      <ore:Aggregation>
        <xsl:attribute name="rdf:about">
          <xsl:value-of select="$aggregation"/>
          <xsl:for-each select="$theTEIHeader/tei:fileDesc/tei:publicationStmt/tei:idno">
            <xsl:if test="position() = 1">
              <xsl:value-of select="substring-after(.,'.de/')"/>
            </xsl:if>
          </xsl:for-each>
        </xsl:attribute>
  
        <edm:aggregatedCHO>      
          
          
          <!-- Begin CHO -->
          
          <edm:ProvidedCHO>
            <xsl:attribute name="rdf:about">
              <xsl:value-of select="$cho"/>
              <xsl:for-each select="$theTEIHeader/tei:fileDesc/tei:publicationStmt/tei:idno">
                <xsl:if test="position() = 1">
                  <xsl:value-of select="substring-after(.,'.de/')"/>
                </xsl:if>
              </xsl:for-each>
            </xsl:attribute>
            
            <edm:type>
              <xsl:text>TEXT</xsl:text>
            </edm:type>
            
            <dc:type>
              <xsl:attribute name="rdf:resource">
                <xsl:text>http://purl.org/spar/fabio/Article</xsl:text>
              </xsl:attribute>
            </dc:type>
            
            <xsl:for-each select="$theTEIHeader/tei:fileDesc/tei:sourceDesc/tei:biblFull/tei:titleStmt/tei:title">
              <dcterms:title>
                <xsl:attribute name="xml:lang">
                  <xsl:text>ger</xsl:text>
                </xsl:attribute>
                <xsl:value-of select="."/>
              </dcterms:title>
            </xsl:for-each>
            
            <dc:language>
              <xsl:text>ger</xsl:text>
            </dc:language>
            
            <dc:subject>
              <xsl:attribute name="xml:lang">
                <xsl:text>eng</xsl:text>
              </xsl:attribute>
              <xsl:value-of select="$DEF_SUBJECT"/>
            </dc:subject>
            
            <xsl:for-each select="$theTEIHeader/tei:fileDesc/tei:sourceDesc/tei:biblFull/tei:seriesStmt/tei:idno">
              <xsl:if test="position() = 2">
                <bibo:pages>
                  <xsl:value-of select="."/>
                </bibo:pages>
              </xsl:if>
            </xsl:for-each>
            
            <xsl:for-each select="$theTEIHeader/tei:fileDesc/tei:sourceDesc/tei:biblStruct/tei:monogr/tei:imprint/tei:biblScope">
              <xsl:if test="position() = 1">
                <bibo:volume>
                  <xsl:value-of select="."/>
                </bibo:volume>
              </xsl:if>
            </xsl:for-each>
            
            <dcterms:tableOfContents>
              <xsl:attribute name="xml:lang">
                <xsl:text>ger</xsl:text>
              </xsl:attribute>
              <xsl:for-each select="tei:TEI/tei:text/tei:body/tei:div/tei:head">
                <xsl:value-of select="replace(., '&#xa;                                    ', ' ')"/>
                <xsl:text>&#xa;</xsl:text>  
              </xsl:for-each>
            </dcterms:tableOfContents> 
            
            <dm2e:hasAnnotatableVersionAt>
              <edm:WebResource>
                <xsl:if test="$theTEIHeader/tei:fileDesc/tei:publicationStmt/tei:idno">
                  <xsl:attribute name="rdf:about">
                    <xsl:for-each select="$theTEIHeader/tei:fileDesc/tei:publicationStmt/tei:idno">
                      <xsl:if test="position() = 1">
                        <xsl:value-of select="."/>
                      </xsl:if>
                    </xsl:for-each>
                  </xsl:attribute>
                  <dc:format>
                    <xsl:text>text/plain</xsl:text>
                  </dc:format>
                </xsl:if>
              </edm:WebResource>
            </dm2e:hasAnnotatableVersionAt>

            <pro:author>
              <foaf:Person> 
                <xsl:attribute name="rdf:about">
                  <xsl:value-of select="$agent"/>
                  <xsl:for-each select="$theTEIHeader/tei:fileDesc/tei:sourceDesc/tei:biblFull/tei:titleStmt/tei:author/tei:name">
                    <xsl:if test="position() = 1">
                       <xsl:value-of select="replace(replace(., ',', ''), ' ', '_')"/>
                    </xsl:if>
                  </xsl:for-each>
                </xsl:attribute>
                <xsl:for-each select="$theTEIHeader/tei:fileDesc/tei:sourceDesc/tei:biblFull/tei:titleStmt/tei:author/tei:name">
                  <xsl:if test="position() = 1">
                    <skos:prefLabel>
                      <xsl:attribute name="xml:lang">
                        <xsl:text>ger</xsl:text>
                      </xsl:attribute>
                      <xsl:value-of select="."/>
                    </skos:prefLabel>
                  </xsl:if>
                </xsl:for-each>
              </foaf:Person>
            </pro:author>
            
            <xsl:for-each select="tei:TEI/tei:text/tei:body/tei:div/tei:p/tei:persName">
            <dm2e:mentioned>
              <foaf:Person>
                <xsl:attribute name="rdf:about">
                  <xsl:value-of select="$agent"/>
                  <xsl:value-of select="replace(replace(replace(., '&#xa;                                        ', ' '), ',', ''), ' ', '_')"/>
                </xsl:attribute>
                    <skos:prefLabel>
                      <xsl:attribute name="xml:lang">
                        <xsl:text>ger</xsl:text>
                      </xsl:attribute>
                      <xsl:value-of select="replace(., '&#xa;                                        ', ' ')"/>
                    </skos:prefLabel>
              </foaf:Person>
            </dm2e:mentioned>
            </xsl:for-each>
            
            <!-- Related CHOs -->
        
            <dm2e:isPartOfCHO>
              <edm:ProvidedCHO>
                <xsl:attribute name="rdf:about">
                  <xsl:value-of select="$cho"/>
                  <xsl:for-each select="$theTEIHeader/tei:fileDesc/tei:sourceDesc/tei:biblStruct/tei:monogr/tei:idno">
                    <xsl:if test="position() = 1">
                      <xsl:value-of select="concat('journal/',.)"/>
                    </xsl:if>
                  </xsl:for-each>
                </xsl:attribute>
              </edm:ProvidedCHO>
            </dm2e:isPartOfCHO>      
            
            <dm2e:isPartOfCHO>
              <edm:ProvidedCHO>
                <xsl:attribute name="rdf:about">
                  <xsl:value-of select="$cho"/>
                  <xsl:for-each select="tei:TEI/tei:text/tei:front/tei:pb">
                    <xsl:if test="position() = 1">
                      <xsl:value-of select="concat('issue/', substring-before(@xml:id,'_'))"/>
                    </xsl:if>
                  </xsl:for-each>
                </xsl:attribute>
              </edm:ProvidedCHO>
            </dm2e:isPartOfCHO>
            
            <!-- hasPartCHO Frontpage -->
            
            <dm2e:hasPartCHO>
              <edm:ProvidedCHO>
                <xsl:attribute name="rdf:about">
                  <xsl:value-of select="$cho"/>
                  <xsl:for-each select="tei:TEI/tei:text/tei:front/tei:pb">
                    <xsl:value-of select="concat('page/',@xml:id)"/>
                  </xsl:for-each>
                </xsl:attribute>
              </edm:ProvidedCHO>
            </dm2e:hasPartCHO>
            
            <!-- hasPartCHO Rest -->
            
            <xsl:for-each select="tei:TEI/tei:text/tei:body/tei:div/tei:pb">
              <dm2e:hasPartCHO>
                <edm:ProvidedCHO>
                  <xsl:attribute name="rdf:about">
                    <xsl:value-of select="$cho"/>
                    <xsl:value-of select="concat('page/',@xml:id)"/>
                  </xsl:attribute>
                </edm:ProvidedCHO>
              </dm2e:hasPartCHO>
            </xsl:for-each>
   
   
          </edm:ProvidedCHO>
        </edm:aggregatedCHO>
        
        <!-- End of CHO -->
        
        
        <!-- Aggregation -->
        
        <edm:provider>
          <foaf:Organization>
            <xsl:attribute name="rdf:about">
              <xsl:value-of select="$agent"/>
              <xsl:text>DM2E</xsl:text>
            </xsl:attribute>
            <skos:prefLabel>
              <xsl:attribute name="xml:lang">
                <xsl:text>eng</xsl:text>
              </xsl:attribute>
              <xsl:text>DM2E</xsl:text>
            </skos:prefLabel>
            <skos:altLabel>
              <xsl:attribute name="xml:lang">
                <xsl:text>eng</xsl:text>
              </xsl:attribute>
              <xsl:text>Digitised Manuscripts to Europeana</xsl:text>           
            </skos:altLabel>
          </foaf:Organization>
        </edm:provider>
        
        <edm:dataProvider>
          <foaf:Organization>
            <xsl:attribute name="rdf:about">
              <xsl:value-of select="$agent"/>
              <xsl:for-each select="$theTEIHeader/tei:fileDesc/tei:publicationStmt/tei:publisher">
                <xsl:if test="position() = 1">
                  <xsl:value-of select="."/>
                </xsl:if>
              </xsl:for-each>
            </xsl:attribute>
            <xsl:for-each select="$theTEIHeader/tei:fileDesc/tei:publicationStmt/tei:publisher">
              <xsl:if test="position() = 1">
              <skos:prefLabel>
                <xsl:attribute name="xml:lang">
                  <xsl:text>ger</xsl:text>
                </xsl:attribute>
                <xsl:value-of select="."/>
              </skos:prefLabel>
              </xsl:if>
            </xsl:for-each>
          </foaf:Organization>
        </edm:dataProvider>
 
        <edm:rights>
          <xsl:attribute name="rdf:resource">
            <xsl:value-of select="$RIGHTS_RESOURCE"/>
          </xsl:attribute>
        </edm:rights>
        
        <xsl:for-each select="$theTEIHeader/tei:fileDesc/tei:publicationStmt/tei:idno">
          <edm:isShownAt>
            <xsl:for-each select=".">
              <edm:WebResource>
                <xsl:if test=".">
                  <xsl:attribute name="rdf:about">
                    <xsl:for-each select=".">
                      <xsl:if test="position() = 1">
                        <xsl:value-of select="."/>
                      </xsl:if>
                    </xsl:for-each>
                  </xsl:attribute>
                </xsl:if>
                <xsl:for-each select="../../tei:seriesStmt/tei:title">
                  <dcterms:description>
                    <xsl:attribute name="xml:lang">
                      <xsl:text>ger</xsl:text>
                    </xsl:attribute>
                    <xsl:value-of select="."/>
                  </dcterms:description>
                </xsl:for-each>
                <dcterms:rightsHolder>
                  <foaf:Organization>
                    <xsl:attribute name="rdf:about">
                      <xsl:value-of select="$agent"/>
                      <xsl:for-each select="../tei:publisher">
                        <xsl:if test="position() = 1">
                          <xsl:value-of select="."/>
                        </xsl:if>
                      </xsl:for-each>
                    </xsl:attribute>
                    <xsl:for-each select="$theTEIHeader/tei:fileDesc/tei:publicationStmt/tei:publisher">
                      <xsl:if test="position() = 1">
                        <skos:prefLabel>
                          <xsl:attribute name="xml:lang">
                            <xsl:text>ger</xsl:text>
                          </xsl:attribute>
                          <xsl:value-of select="."/>
                        </skos:prefLabel>
                      </xsl:if>
                    </xsl:for-each>
                  </foaf:Organization>
                </dcterms:rightsHolder>
                
                <dm2e:isDerivativeOfWebResource>
                  <edm:WebResource>
                  <xsl:if test=".">
                    <xsl:attribute name="rdf:about">
                      <xsl:for-each select=".">
                        <xsl:if test="position() = 1">
                          <xsl:value-of select="substring-before(.,'article')"/>
                        </xsl:if>
                      </xsl:for-each>
                    </xsl:attribute>
                  </xsl:if>
                  </edm:WebResource>
                </dm2e:isDerivativeOfWebResource>
              </edm:WebResource>
            </xsl:for-each>
          </edm:isShownAt>
        </xsl:for-each>
        
        <dcterms:creator>
          <foaf:Organization>
            <xsl:attribute name="rdf:about">
              <xsl:value-of select="$agent"/>
              <xsl:for-each select="$theTEIHeader/tei:fileDesc/tei:publicationStmt/tei:publisher">
                <xsl:if test="position() = 1">
                  <xsl:value-of select="."/>
                </xsl:if>
              </xsl:for-each>
            </xsl:attribute>
            <xsl:for-each select="$theTEIHeader/tei:fileDesc/tei:publicationStmt/tei:publisher">
              <xsl:if test="position() = 1">
                <skos:prefLabel>
                  <xsl:attribute name="xml:lang">
                    <xsl:text>ger</xsl:text>
                  </xsl:attribute>
                  <xsl:value-of select="."/>
                </skos:prefLabel>
              </xsl:if>
            </xsl:for-each>
          </foaf:Organization>
        </dcterms:creator>
        
        <xsl:for-each select="$theTEIHeader/tei:fileDesc/tei:publicationStmt/tei:date/@when">
          <dcterms:created>
            <xsl:value-of select="."/>
          </dcterms:created>
        </xsl:for-each>
        
        <xsl:for-each select="$theTEIHeader/tei:fileDesc/tei:seriesStmt/tei:respStmt/tei:name">
        <dc:contributor>
          <foaf:Organization>
            <xsl:attribute name="rdf:about">
              <xsl:value-of select="$agent"/>
              <xsl:for-each select=".">
                <xsl:if test="position() = 1">
                  <xsl:value-of select="replace(., ' ', '_')"/>
                </xsl:if>
              </xsl:for-each>
            </xsl:attribute>
            <xsl:for-each select=".">
              <xsl:if test="position() = 1">
                <skos:prefLabel>
                  <xsl:attribute name="xml:lang">
                    <xsl:text>ger</xsl:text>
                  </xsl:attribute>
                  <xsl:value-of select="."/>
                </skos:prefLabel>
              </xsl:if>
            </xsl:for-each>
          </foaf:Organization>
        </dc:contributor>
        </xsl:for-each>
        
      </ore:Aggregation>
      
  </xsl:template>
</xsl:stylesheet>
