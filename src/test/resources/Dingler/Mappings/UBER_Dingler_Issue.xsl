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
    xmlns:xalan="http://xml.apache.org/xalan"
    xmlns:xml="http://www.w3.org/XML/1998/namespace" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    
    <xsl:template name="Issue">
        <xsl:param name="theTEIHeader" as="node()"/>
        
        <ore:Aggregation>
            <xsl:attribute name="rdf:about">
                <xsl:value-of select="$aggregation"/>
                <!--<xsl:for-each select="tei:TEI/tei:text/tei:front/tei:pb">-->
                <xsl:for-each select="$theTEIHeader/tei:fileDesc/tei:publicationStmt/tei:idno">
                    <xsl:value-of select="concat('issue/',(substring-before(substring-after(substring-after(.,'http://dingler.culture.hu-berlin.de/'), '/'),'/')))"/>
                </xsl:for-each>
            </xsl:attribute>
            
            <edm:aggregatedCHO>
                
                
                <!-- Begin CHO -->
                
                <edm:ProvidedCHO>
                    <xsl:attribute name="rdf:about">
                        <xsl:value-of select="$cho"/>
                        <xsl:for-each select="$theTEIHeader/tei:fileDesc/tei:publicationStmt/tei:idno">
                            <xsl:value-of select="concat('issue/',(substring-before(substring-after(substring-after(.,'http://dingler.culture.hu-berlin.de/'), '/'),'/')))"/>
                        </xsl:for-each>
                        <!--<xsl:for-each select="tei:TEI/tei:text/tei:front/tei:pb">
                            <xsl:if test="position() = 1">
                                <xsl:value-of select="concat('issue/',substring-before(@xml:id,'_'))"/>
                            </xsl:if>
                        </xsl:for-each>-->
                    </xsl:attribute>
                    
                    <edm:type>
                        <xsl:text>TEXT</xsl:text>
                    </edm:type>
                    
                    <dc:type>
                        <xsl:attribute name="rdf:resource">
                            <xsl:text>http://purl.org/ontology/bibo/Issue</xsl:text>
                        </xsl:attribute>
                    </dc:type>
                    
                    <xsl:for-each select="$theTEIHeader/tei:fileDesc/tei:sourceDesc/tei:biblStruct/tei:monogr/tei:title">
                         <dcterms:title>
                            <xsl:if test="position() = 1">
                            <xsl:attribute name="xml:lang">
                                <xsl:text>ger</xsl:text>
                            </xsl:attribute>
                            <xsl:value-of select="."/>
                             </xsl:if>
                         </dcterms:title>
                    </xsl:for-each>
                   
                   <dm2e:subtitle>
                         <xsl:for-each select="$theTEIHeader/tei:fileDesc/tei:titleStmt/tei:title">
                                <xsl:if test="position() = 2">
                                <xsl:attribute name="xml:lang">
                                    <xsl:text>ger</xsl:text>
                                </xsl:attribute>
                                <xsl:value-of select="."/>
                                </xsl:if> 
                         </xsl:for-each>    
                    </dm2e:subtitle>

                    <dc:language>
                        <xsl:text>ger</xsl:text>
                    </dc:language>
                    
                    <dcterms:issued>
                        <edm:TimeSpan>
                            <xsl:attribute name="rdf:about">
                                <xsl:value-of select="$timespan"/>
                                <xsl:for-each select="$theTEIHeader/tei:fileDesc/tei:sourceDesc/tei:biblFull/tei:publicationStmt/tei:date">
                                    <xsl:if test="position() = 1">
                                        <xsl:value-of select="."/>
                                    </xsl:if>
                                </xsl:for-each>
                            </xsl:attribute>
                            <xsl:for-each select="$theTEIHeader/tei:fileDesc/tei:sourceDesc/tei:biblFull/tei:publicationStmt/tei:date">
                                <xsl:if test="position() = 1">
                                    <skos:prefLabel>
                                        <xsl:attribute name="xml:lang">
                                            <xsl:text>ger</xsl:text>
                                        </xsl:attribute>
                                        <xsl:value-of select="."/>
                                    </skos:prefLabel>
                                </xsl:if>
                            </xsl:for-each>
                            <xsl:for-each select="$theTEIHeader/tei:fileDesc/tei:sourceDesc/tei:biblFull/tei:publicationStmt/tei:date">
                                <edm:begin>
                                    <xsl:value-of select="."/>
                                </edm:begin>
                            </xsl:for-each>
                            <xsl:for-each select="$theTEIHeader/tei:fileDesc/tei:sourceDesc/tei:biblFull/tei:publicationStmt/tei:date">
                                <edm:end>
                                    <xsl:value-of select="."/>
                                </edm:end>
                            </xsl:for-each>
                        </edm:TimeSpan>
                    </dcterms:issued>
                    
                    <dm2e:publishedAt>
                        <edm:Place>
                            <xsl:attribute name="rdf:about">
                                <xsl:value-of select="$place"/>
                                <xsl:for-each select="$theTEIHeader/tei:fileDesc/tei:sourceDesc/tei:biblStruct/tei:monogr/tei:imprint/tei:pubPlace">
                                    <xsl:if test="position() = 1">
                                        <xsl:value-of select="."/>
                                    </xsl:if>
                                </xsl:for-each>
                            </xsl:attribute>
                            <xsl:for-each select="$theTEIHeader/tei:fileDesc/tei:sourceDesc/tei:biblStruct/tei:monogr/tei:imprint/tei:pubPlace">
                                <xsl:if test="position() = 1">
                                    <skos:prefLabel>
                                        <xsl:attribute name="xml:lang">
                                            <xsl:text>ger</xsl:text>
                                        </xsl:attribute>
                                        <xsl:value-of select="."/>
                                    </skos:prefLabel>
                                </xsl:if>
                            </xsl:for-each>
                        </edm:Place>
                    </dm2e:publishedAt>
                    
                    <xsl:for-each select="tei:TEI/tei:text/tei:front/tei:pb">
                        <dc:identifier>
                            <xsl:if test="position() = 1">
                                <xsl:value-of select="substring-before(@xml:id,'_')"/>
                            </xsl:if>
                        </dc:identifier>
                    </xsl:for-each>
                    
                    <dc:publisher>
                        <foaf:Organization>
                            <xsl:attribute name="rdf:about">
                                <xsl:value-of select="$agent"/>
                                <xsl:for-each select="$theTEIHeader/tei:fileDesc/tei:sourceDesc/tei:biblStruct/tei:monogr/tei:imprint/tei:publisher">
                                    <xsl:if test="position() = 1">
                                        <xsl:value-of select="replace(., ' ', '_')"/>
                                    </xsl:if>
                                </xsl:for-each>
                            </xsl:attribute>
                            <xsl:for-each select="$theTEIHeader/tei:fileDesc/tei:sourceDesc/tei:biblStruct/tei:monogr/tei:imprint/tei:publisher">
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
                    </dc:publisher>
                    
                    <bibo:editor>
                        <foaf:Person>
                            <xsl:attribute name="rdf:about">
                                <xsl:value-of select="$agent"/>
                                <xsl:for-each select="$theTEIHeader/tei:fileDesc/tei:sourceDesc/tei:biblStruct/tei:monogr/tei:editor">
                                    <xsl:if test="position() = 1">
                                        <xsl:value-of select="replace(., ' ', '_')"/>
                                    </xsl:if>
                                </xsl:for-each>
                            </xsl:attribute>
                            <xsl:for-each select="$theTEIHeader/tei:fileDesc/tei:sourceDesc/tei:biblStruct/tei:monogr/tei:editor">
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
                    </bibo:editor>
                    
                    <dc:subject>
                        <xsl:attribute name="xml:lang">
                            <xsl:text>eng</xsl:text>
                        </xsl:attribute>
                            <xsl:value-of select="$DEF_SUBJECT"/>
                       </dc:subject>
                   
            <!-- Related CHOs -->
                   
                    <dm2e:hasPartCHO>
                        <edm:ProvidedCHO>
                            <xsl:attribute name="rdf:about">
                                <xsl:value-of select="$cho"/>
                                <xsl:for-each select="$theTEIHeader/tei:fileDesc/tei:publicationStmt/tei:idno">
                                    <xsl:if test="position() = 1">
                                        <xsl:value-of select="substring-after(.,'.de/')"/>
                                    </xsl:if>
                                </xsl:for-each>
                            </xsl:attribute>
                        </edm:ProvidedCHO>
                    </dm2e:hasPartCHO>
                    
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
                </foaf:Organization>
            </edm:dataProvider>
            
            <edm:rights>
                <xsl:attribute name="rdf:resource">
                    <xsl:value-of select="$RIGHTS_RESOURCE"/>
                </xsl:attribute>
            </edm:rights>
            
       <!--     <edm:isShownAt>
                <edm:WebResource>
                    <xsl:if test="$theTEIHeader/tei:fileDesc/tei:publicationStmt/tei:idno">
                        <xsl:attribute name="rdf:about">
                            <xsl:for-each select="$theTEIHeader/tei:fileDesc/tei:publicationStmt/tei:idno">
                                <xsl:if test="position() = 1">
                                    <xsl:value-of select="substring-before((replace(., 'article', 'journal')), 'ar0')"/>
                                </xsl:if>
                            </xsl:for-each>
                        </xsl:attribute>
                    </xsl:if>
                </edm:WebResource>
            </edm:isShownAt> -->
            
            <xsl:for-each select="$theTEIHeader/tei:fileDesc/tei:publicationStmt/tei:idno">
                <edm:isShownAt>
                    <xsl:for-each select=".">
                        <edm:WebResource>
                            <xsl:if test=".">
                                <xsl:attribute name="rdf:about">
                                    <xsl:for-each select=".">
                                        <xsl:if test="position() = 1">
                                            <xsl:value-of select="substring-before((replace(., 'article', 'journal')), 'ar0')"/>
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
            
            <dm2e:hasAnnotatableVersionAt>
                <edm:WebResource>
                    <xsl:if test="$theTEIHeader/tei:fileDesc/tei:publicationStmt/tei:idno">
                        <xsl:attribute name="rdf:about">
                            <xsl:for-each select="$theTEIHeader/tei:fileDesc/tei:publicationStmt/tei:idno">
                                <xsl:if test="position() = 1">
                                    <xsl:value-of select="substring-before(
                                        (
                                                replace(., 'article', 'journal/page')
                                         ), 'ar0')"/>
                                </xsl:if>
                            </xsl:for-each>
                        </xsl:attribute>
                    </xsl:if>
                </edm:WebResource>
            </dm2e:hasAnnotatableVersionAt>
            
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
