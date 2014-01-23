<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet exclude-result-prefixes="ddb xsi" version="2.0"
  xmlns:dc="http://purl.org/dc/elements/1.1/"
  xmlns:dm2e="http://onto.dm2e.eu/schemas/dm2e/1.1/"
  xmlns:dm2edata="http://data.dm2e.eu/data/"
  xmlns:edm="http://www.europeana.eu/schemas/edm/"
  xmlns:dcterms="http://purl.org/dc/terms/"
  xmlns:ore="http://www.openarchives.org/ore/terms/"
  xmlns:skos="http://www.w3.org/2004/02/skos/core#"
  xmlns:mets="http://www.loc.gov/METS/"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
  xmlns:ddb="http://www.ddb.de/professionell/mabxml/mabxml-1.xsd"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:bibo="http://purl.org/ontology/bibo/"
  xmlns:foaf="http://xmlns.com/foaf/0.1/"
  xmlns:korbo="http://purl.org/net7/korbo/vocab#"
  xmlns:owl="http://www.w3.org/2002/07/owl#"
  xmlns:pro="http://purl.org/spar/pro/"
  xmlns:rdaGr2="http://rdvocab.info/ElementsGr2/"
  xmlns:void="http://rdfs.org/ns/void#"
  xmlns:wgs84_pos="http://www.w3.org/2003/01/geo/wgs84_pos#"
  xmlns:xalan="http://xml.apache.org/xalan">
<!--
  xmlns:bibo="http://purl.org/ontology/bibo/"
  xmlns:foaf="http://xmlns.com/foaf/0.1/"
  xmlns:korbo="http://purl.org/net7/korbo/vocab#"
  xmlns:owl="http://www.w3.org/2002/07/owl#"
  xmlns:pro="http://purl.org/spar/pro/"
  xmlns:rdaGr2="http://RDVocab.info/ElementsGr2/"
  xmlns:void="http://rdfs.org/ns/void#"
  xmlns:wgs84_pos="http://www.w3.org/2003/01/geo/wgs84_pos#"
  xmlns:xalan="http://xml.apache.org/xalan"
-->
  <xsl:param name="MABfile"/>
  <xsl:param name="MABdata" select="document($MABfile)" />

  <xsl:param name="baseURI">http://data.dm2e.eu/data/</xsl:param>
  <xsl:param name="provider">onb</xsl:param>
  <xsl:param name="collection">codices</xsl:param>
  <xsl:param name="dataProvider">Austrian National Library</xsl:param>
  <xsl:param name="dm2eProvider">DM2E</xsl:param>
  <xsl:param name="aggregationType">TEXT</xsl:param>
  <xsl:param name="rights">http://creativecommons.org/publicdomain/mark/1.0/</xsl:param>
  <xsl:variable name="dataProvID">
	<xsl:value-of select="concat('http://data.dm2e.eu/data/agent/',
								$provider,'/',
								$collection,'/',
								encode-for-uri(
									translate(
										translate($dataProvider, 
												  '&gt;&lt;.,[]',''),
										' ','_'
									)
								)
							)"/>	
	</xsl:variable>
	
	<xsl:variable name="dm2eID">
		<xsl:value-of select="concat('http://data.dm2e.eu/data/agent/',
					$provider,'/',
					$collection,'/',
					encode-for-uri(
						translate(
							translate(	$dm2eProvider, 
										'&gt;&lt;.,[]',''),
							' ','_'
						)
					)
				)"/>	
	</xsl:variable>
	
		<!--<xsl:variable name="filename" select="tokenize(base-uri(.), '/')[last()]"/>-->
		
		<!-- Split the filename using '\.' -->
		<!--<xsl:variable name="pid" select="tokenize($filename, '_')[3]"/>
		<xsl:variable name="pid" select="tokenize($filename, '_')[3]"/>
		<xsl:variable name="imguri" select="concat('http://fue.onb.ac.at/CODICES/', $pid, '/')"/>
		-->	
		

<!--
  <xsl:output method="xml" encoding="utf-8" indent="yes" />
-->
  <xsl:output method="xml" encoding="utf-8" indent="yes" />

	<!-- 037ba1 Sprachcode -->
	<xsl:variable name="languages">
		<xsl:choose>
			<xsl:when test="(//varfield/@id = '037') and (//varfield/@i1 = 'b') and (//varfield/subfield/@label = 'a')">
				<xsl:value-of select="//varfield/subfield[(../@id = '037') and (../@i1 = 'b') and (@label = 'a')]"/>
				<!--xsl:for-each select="varfield/subfield[(../@id = '037') and (../@i1 = 'b') and (@label = 'a')]">
	  				<xsl:variable name="language" select="."/>
	  			</xsl:for-each-->
			</xsl:when>
			
			<!-- 037ba1 Sprachcode NICHT VORHANDEN -->
			<xsl:when test="not((//varfield/@id = '037') and (//varfield/@i1 = 'b') and (//varfield/subfield/@label = 'a'))">
				<xsl:value-of select="und"/>
			</xsl:when>
		</xsl:choose>
	</xsl:variable>


  <xsl:template match="/mab2mets">
  	<xsl:param name="itemID" select="replace(//varfield[@id='001']/subfield[@label='a'], '\+','')"/>
  	<!--<xsl:variable name="pid" select="tokenize($filename, '_')[3]"/>-->
 
  	<xsl:variable name="contentlink">
  		<xsl:call-template name="getContentLink"/>
  	</xsl:variable>
  	<xsl:variable name="pid" select ="substring-before(substring-after($contentlink, 'pid='),'&amp;custom')"/>
  	<xsl:variable name="imguri" select="concat('http://fue.onb.ac.at/CODICES/', $pid, '/')"/>




  	<rdf:RDF xmlns:dm2edata="http://data.dm2e.eu/data/">
	  	<xsl:apply-templates select="//present/record/metadata/oai_marc">
  			<xsl:with-param name="itemID" select="$itemID"/>
  		</xsl:apply-templates>
  		<xsl:apply-templates select="//mets:mets">
  			<xsl:with-param name="itemID" select="$itemID"/>
  			<xsl:with-param name="imguri" select="$imguri"/>
  		</xsl:apply-templates>  		
  	</rdf:RDF>
  </xsl:template>
	
	<xsl:template match="//present/record/metadata/oai_marc">
		<xsl:param name="itemID"/>
		<!-- <xsl:param name="itemID" select="replace(varfield[@id='LOC']/subfield[@label='5'], '\+','')"/> -->
		
			<xsl:text>
</xsl:text>
			
			<xsl:call-template name="persons">
				<xsl:with-param name="itemID" select="$itemID"/>
			</xsl:call-template>
			
			<xsl:call-template name="organizations">
				<xsl:with-param name="itemID" select="$itemID"/>
			</xsl:call-template>
			
			<xsl:call-template name="places">
				<xsl:with-param name="itemID" select="$itemID"/>
			</xsl:call-template>
			
			<xsl:call-template name="timeSpans">
				<xsl:with-param name="itemID" select="$itemID"/>
			</xsl:call-template>
			
			<xsl:call-template name="providedCHO">
				<xsl:with-param name="itemID" select="$itemID"/>
			</xsl:call-template>
			
			<xsl:call-template name="oreAggregation">
				<xsl:with-param name="itemID" select="$itemID"/>
			</xsl:call-template>
			<!--	
 			<xsl:call-template name="webresources">
				<xsl:with-param name="itemID" select="$itemID"/>
  		</xsl:call-template>
			-->

	</xsl:template>

<!-- CODE FOR THE AGGREGATION -->
	<xsl:template name="oreAggregation">
		<xsl:param name="itemID"/>

		<foaf:Organization>	
			<xsl:attribute name="rdf:about">
				<xsl:value-of select="$dataProvID"/>
			</xsl:attribute>
			<skos:prefLabel>
         <xsl:attribute name="xml:lang">
         	<xsl:text>eng</xsl:text>
         </xsl:attribute>
				<xsl:value-of select="$dataProvider"/>
			</skos:prefLabel>
		</foaf:Organization>

		<foaf:Organization>	
			<xsl:attribute name="rdf:about">
				<xsl:value-of select="$dm2eID"/>
			</xsl:attribute>
			<skos:prefLabel>
        <xsl:attribute name="xml:lang">
         	<xsl:text>eng</xsl:text>
        </xsl:attribute>
				<xsl:value-of select="$dm2eProvider"/>
			</skos:prefLabel>
		</foaf:Organization>	

			
		<xsl:if test="varfield[@id='655' and @i1='e']/subfield[@label='z']='Digitalisat'">
			<edm:WebResource>
				<xsl:attribute name="rdf:about">
					<xsl:call-template name="getContentLink"/>
				</xsl:attribute>
			</edm:WebResource>
		</xsl:if>

		<ore:Aggregation>
			<xsl:attribute name="rdf:about">
				<xsl:value-of select="$baseURI"/>
				<xsl:text>aggregation/</xsl:text>
				<xsl:value-of select="$provider"/>
				<xsl:text>/</xsl:text>
				<xsl:value-of select="$collection"/>
				<xsl:text>/</xsl:text>
				<xsl:value-of select="$itemID"/>
			</xsl:attribute>
		
			<edm:aggregatedCHO>
				<xsl:attribute name="rdf:resource">
					<xsl:value-of select="$baseURI"/>
					<xsl:text>item/</xsl:text>
					<xsl:value-of select="$provider"/>
					<xsl:text>/</xsl:text>
					<xsl:value-of select="$collection"/>
					<xsl:text>/</xsl:text>
					<xsl:value-of select="$itemID"/>
				</xsl:attribute>
			</edm:aggregatedCHO>

			<edm:dataProvider>
				<xsl:attribute name="rdf:resource">
					<xsl:value-of select="$dataProvID"/>
				</xsl:attribute>
			</edm:dataProvider>
			
			<xsl:if test="varfield[@id='655' and @i1='e']/subfield[@label='z']='Digitalisat'">
				<edm:isShownAt>
					<xsl:attribute name="rdf:resource">
						<xsl:call-template name="getContentLink"/>
					</xsl:attribute>
				</edm:isShownAt>
			</xsl:if>
			
			<edm:provider>
				<xsl:attribute name="rdf:resource">
					<xsl:value-of select="$dm2eID"/>
				</xsl:attribute>
			</edm:provider>

			<edm:rights>
				<xsl:value-of select="$rights"/>
			</edm:rights>


	
			<xsl:if test="varfield[@id='002' and @i1='a']">
				<dcterms:created>
					 <xsl:value-of select="replace(	varfield[@id='002' and @i1='a']/subfield[@label='a'],
																					'(\d{4})(\d{2})(\d{2})', 
																					'$1-$2-$3T00:00:00')"/>
				</dcterms:created>
			</xsl:if>

			<xsl:if test="varfield[@id='003' and @i1='-']">
				<dcterms:modified>
					 <xsl:value-of select="replace(	varfield[@id='003' and @i1='-']/subfield[@label='a'],
																					'(\d{4})(\d{2})(\d{2})', 
																					'$1-$2-$3T00:00:00')"/>
				</dcterms:modified>
			</xsl:if>
		</ore:Aggregation>
	</xsl:template>
	
	<!-- CODE FOR RETRIEVING SHOWNAT -->
	
	<xsl:template name="getContentLink">		
		<xsl:if test="//varfield[@id='655' and @i1='e']/subfield[@label='z']='Digitalisat'">
			<xsl:for-each select="//varfield[@id='655' and @i1='e' and subfield[@label='z']='Digitalisat']">	
				<xsl:value-of select="subfield[@label='u']"/>
			</xsl:for-each>
		</xsl:if>
	</xsl:template>		

	<!-- CODE FOR THE PROVIDEDCHO -->
	<xsl:template name="providedCHO">
		<xsl:param name="itemID"/>
		<edm:ProvidedCHO>
            <xsl:attribute name="rdf:about">
              <xsl:value-of select="$baseURI"/>
              <xsl:text>item/</xsl:text>
              <xsl:value-of select="$provider"/>
              <xsl:text>/</xsl:text>
              <xsl:value-of select="$collection"/>
              <xsl:text>/</xsl:text>
              <xsl:value-of select="$itemID"/>
            </xsl:attribute>


			<!-- 100 - 199 Person -->
						<xsl:for-each select="varfield[(number(@id) &gt;= 100) and (number(@id) &lt; 200)]">	
							<xsl:call-template name="personRole">
								<xsl:with-param name="itemID" select="$itemID"/>
  						</xsl:call-template>
						</xsl:for-each>

			<!-- 200 - 299 Organization -->
						<xsl:for-each select="varfield[(number(@id) &gt;= 200) and (number(@id) &lt; 300)]">	
							<xsl:call-template name="organizationRole">
								<xsl:with-param name="itemID" select="$itemID"/>
  						</xsl:call-template>
						</xsl:for-each>
			
			<!-- 037ba1 Sprachcode -->
			<!--xsl:if test="(varfield/@id = '037') and (varfield/@i1 = 'b') and (varfield/subfield/@label = 'a')">
				<xsl:for-each select="varfield/subfield[(../@id = '037') and (../@i1 = 'b') and (@label = 'a')]">
					<dc:language>
						<xsl:value-of select="."/>
					</dc:language>
				</xsl:for-each>
			</xsl:if-->
			
			<!-- 037ba1 Sprachcode NICHT VORHANDEN -->
			<!--xsl:if test="not((varfield/@id = '037') and (varfield/@i1 = 'b') and (varfield/subfield/@label = 'a'))">
				<dc:language>und</dc:language>
			</xsl:if-->	
			
			<!-- 037ba1 Sprachcode NEU -->
			<xsl:for-each select="$languages">
				<dc:language>
					<xsl:value-of select="."/>
				</dc:language>
			</xsl:for-each>
			
			
			<dc:type>
				<xsl:attribute name="rdf:resource">
					<xsl:text>http://dublincore.org/documents/2000/07/11/dcmi-type-vocabulary/#text</xsl:text>
				</xsl:attribute>
			</dc:type>

      <!-- 310-a1 Hauptsachtitel in Ansetzungsform -->
            <xsl:if test="(varfield/@id = '310')">
              <xsl:for-each select="varfield/subfield[(../@id = '310') and (../@i1 = '-') and (@label = 'a')]">
                <dcterms:alternative>
                  <xsl:attribute name="xml:lang">
                    <xsl:text>und</xsl:text>
                  </xsl:attribute>
                  <xsl:value-of select="."/>
                </dcterms:alternative>
       	      </xsl:for-each>
            </xsl:if>

			<!-- 331-a1 Hauptsachtitel in Vorlageform -->
            <xsl:if test="(varfield/@id = '331')">
              <xsl:for-each select="varfield/subfield[(../@id = '331') and (../@i1 = '-') and (@label = 'a')]">
                <dcterms:title>
                  <xsl:attribute name="xml:lang">
                    <xsl:text>und</xsl:text>
                  </xsl:attribute>
                  <xsl:value-of select="."/>
                </dcterms:title>
              </xsl:for-each>
            </xsl:if>

      <!-- 335-a1 Zusätze zum Hauptsachtitel -->
            <xsl:if test="(varfield/@id = '335')">
              <xsl:for-each select="varfield/subfield[(../@id = '335') and (../@i1 = '-') and (@label = 'a')]">
                <dm2e:subtitle>
                  <xsl:attribute name="xml:lang">
                    <xsl:text>und</xsl:text>
                  </xsl:attribute>
                  <xsl:value-of select="."/>
                </dm2e:subtitle>
              </xsl:for-each>
            </xsl:if>

			<!-- 410aa1 Druckort -->	
            <xsl:if test="(varfield/@id = '410') and
                          (varfield/@i1 = 'a') and
                          (varfield/subfield/@label = 'a')">
              <xsl:for-each select="varfield[(@id='410') and (@i1 = 'a')]">
                <dm2e:printedAt>
                    <xsl:attribute name="rdf:resource">
          						<xsl:call-template name="placeID">
            						<xsl:with-param name="itemID" select="$itemID"/>
          						</xsl:call-template>
                    </xsl:attribute>
                </dm2e:printedAt>
              </xsl:for-each>
            </xsl:if>

			<!-- 410ua1 Unspezifische Ortsangabe -->	
            <xsl:if test="(varfield/@id = '410') and
                          (varfield/@i1 = 'u') and
                          ((varfield/subfield/@label = 'a') or (varfield/subfield/@label = 'x'))">
              <xsl:for-each select="varfield[ (@id='410') and (@i1 = 'u')]">
                <dcterms:spatial>
                    <xsl:attribute name="rdf:resource">
          						<xsl:call-template name="placeID">
            						<xsl:with-param name="itemID" select="$itemID"/>
          						</xsl:call-template>
                    </xsl:attribute>
                </dcterms:spatial>
              </xsl:for-each>
            </xsl:if>

			<!-- 425aa1 Erscheinungsjahr in Anlageform -> incl. Timespan! -->	
					<xsl:if test="(varfield/@id = '425') and (varfield/@i1 = 'a')">
    					<xsl:for-each select="varfield[number(@id) = 425 and @i1='a']">
								<dcterms:issued>
									<xsl:choose>
										<xsl:when test="contains(subfield[@label='a'],'-')">
        							<xsl:attribute name="rdf:resource">
          							<xsl:call-template name="timeSpanID">
            							<xsl:with-param name="itemID" select="$itemID"/>
          							</xsl:call-template>
        							</xsl:attribute>
										</xsl:when>
										<xsl:otherwise>
											<!--xsl:value-of select="concat(subfield[@label='a'], '-01-01T00:00:00')"/-->
											<xsl:call-template name="dateHandler">
												<xsl:with-param name="fieldval" select="subfield[@label='a']"/>
											</xsl:call-template>
										</xsl:otherwise>
									</xsl:choose>
								</dcterms:issued>
              </xsl:for-each>
          </xsl:if>


      <!-- 433-a1 Umfangsangabe´-->
            <xsl:if test="(varfield/@id = '433') and (varfield/subfield/@label = 'a')">
              <xsl:for-each select="varfield/subfield[(../@id = '433') and (@label = 'a')]">
                <xsl:if test="position() = 1">
                  <bibo:numPages>
                    <xsl:value-of select="."/>
                  </bibo:numPages>
                </xsl:if>
              </xsl:for-each>
            </xsl:if>

			<!-- 434-a1 Illustr. Angabe -->	
            <xsl:if test="(varfield/@id = '434') and
                          (varfield/@i1 = '-') and
                          (varfield/subfield/@label = 'a')">
              <xsl:for-each select="varfield[(@id='434') and (@i1 = '-')]">
                <dm2e:illustration>
                  <xsl:value-of select="subfield[@label='a']"/>
                </dm2e:illustration>
              </xsl:for-each>
            </xsl:if>

      <!-- 435-a1 Formatangabe´-->
            <xsl:if test="(varfield/@id = '435') and (varfield/subfield/@label = 'a')">
              <xsl:for-each select="varfield/subfield[(../@id = '435') and (@label = 'a')]">
                <xsl:if test="position() = 1">
                  <dm2e:pageDimension>
                    <xsl:value-of select="."/>
                  </dm2e:pageDimension>
                </xsl:if>
              </xsl:for-each>
            </xsl:if>

      <!-- 517ba1 Inhalt - enthaltene Werke´-->
            <xsl:if test="(varfield/@id = '517') and (varfield/@i1 = 'b') and (varfield/subfield/@label = 'a')">
              <xsl:for-each select="varfield/subfield[(../@id = '517') and (../@i1 = 'b') and (@label = 'a')]">
                <dcterms:tableOfContents>
                  <xsl:value-of select="."/>
                </dcterms:tableOfContents>
              </xsl:for-each>
            </xsl:if>

      <!-- 544-a1 Lokale Signatur´-->
            <xsl:if test="(varfield/@id = '544') and (varfield/subfield/@label = 'a')">
              <xsl:for-each select="varfield/subfield[(../@id = '544') and (@label = 'a')]">
                <xsl:if test="position() = 1">
                  <dm2e:shelfmarkLocation>
                    <xsl:value-of select="."/>
                  </dm2e:shelfmarkLocation>
                </xsl:if>
              </xsl:for-each>
            </xsl:if>
			
			<!-- 661aa1 Incipit d. Unterlage - Angaben zu Text Unterlage -->	
            <xsl:if test="(varfield/@id = '661') and
                          (varfield/@i1 = 'a') and
                          (varfield/subfield/@label = 'a')">
              <xsl:for-each select="varfield[(@id='661') and (@i1 = 'a')]">
                <dm2e:incipit>
                    <xsl:attribute name="xml:lang">
                      <xsl:text>und</xsl:text>
                    </xsl:attribute>
                  <xsl:value-of select="subfield[@label='a']"/>
                </dm2e:incipit>
              </xsl:for-each>
            </xsl:if>

			<!-- 661ea1 Incipit d. Unterlage - Angaben zu Text Unterlage -->	
            <xsl:if test="(varfield/@id = '661') and
                          (varfield/@i1 = 'e') and
                          (varfield/subfield/@label = 'a')">
              <xsl:for-each select="varfield[(@id='661') and (@i1 = 'e')]">
                <dm2e:explicit>
                    <xsl:attribute name="xml:lang">
                      <xsl:text>und</xsl:text>
                    </xsl:attribute>
                  <xsl:value-of select="subfield[@label='a']"/>
                </dm2e:explicit>
              </xsl:for-each>
            </xsl:if>

			
			<!-- 662aa1 Ang. zum Äusseren der Unterlage - Beschreibstoff -->	
            <xsl:if test="(varfield/@id = '662') and
                          (varfield/@i1 = 'a') and
                          (varfield/subfield/@label = 'a')">
              <xsl:for-each select="varfield[(@id='662') and (@i1 = 'a')]">
                <dm2e:support>
                  <xsl:value-of select="subfield[@label='a']"/>
                </dm2e:support>
              </xsl:for-each>
            </xsl:if>

			<!-- 662ba1 Ang. zum Äusseren der Unterlage - Einband -->	
            <xsl:if test="(varfield/@id = '662') and
                          (varfield/@i1 = 'b') and
                          (varfield/subfield/@label = 'a')">
              <xsl:for-each select="varfield[(@id='662') and (@i1 = 'b')]">
                <dm2e:cover>
                  <xsl:value-of select="subfield[@label='a']"/>
                </dm2e:cover>
              </xsl:for-each>
            </xsl:if>

			<!-- 664(a+b)a1 Provenienz (a=Herkunft, b=Erwerb) -->	
            <xsl:if test="(varfield/@id = '664') and
                          ((varfield/@i1 = 'a') or (varfield/@i1 = 'b')) and
                          (varfield/subfield/@label = 'a')">
              <xsl:for-each select="varfield[(@id='664') and (@i1 = 'b' or @i1 = 'a')]">
                <dcterms:provenance>
                    <xsl:attribute name="xml:lang">
                      <xsl:text>ger</xsl:text>
                    </xsl:attribute>
                  <xsl:value-of select="subfield[@label='a']"/>
                </dcterms:provenance>
              </xsl:for-each>
            </xsl:if>
			<edm:type>
				<xsl:value-of select="$aggregationType"/>
			</edm:type>
			
			
			
		</edm:ProvidedCHO>
	</xsl:template>

	<!-- single dates -->
	<xsl:template name="dateHandler">
		<xsl:param name="fieldval"/>
		<!--xsl:value-of select="$fieldval"/-->
		<!-- We currently simply discard the day and month -->
		<!--xsl:analyze-string select="$fieldval" regex="(\d{{2}}).(\d{{2}}).(\d{{4}})"-->
		<xsl:analyze-string select="$fieldval" regex="(\d{{4}})">
			<xsl:matching-substring>
				<!--xsl:variable name="year" select="regex-group(3)"/>
				<xsl:variable name="month" select="regex-group(2)"/>
				<xsl:variable name="day" select="regex-group(1)"/-->	
				
				<xsl:variable name="year" select="regex-group(1)"/>
				<xsl:variable name="month"/>
				<xsl:variable name="day"/>				
				<!--xsl:value-of select="concat($year, ' ', $month)"/-->
				
				<xsl:variable name="dayval">
					<xsl:choose>
						<xsl:when test="$day=''">
							<xsl:value-of select="01"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="$day"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:variable>
				<xsl:variable name="monthval">
					<xsl:choose>
						<xsl:when test="$month=''">
							<xsl:value-of select="01"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="$month"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:variable>
				<xsl:variable name="yearval">
					<xsl:choose>
						<xsl:when test="$year=''">
							<xsl:value-of select="9999"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="$year"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:variable>
				
				<xsl:variable name="date-iso" select="concat(xs:integer($yearval), '-', xs:integer($monthval), '-', xs:integer($dayval), 'T00:00:00')"/>
				
				<xsl:value-of select="$date-iso"/>
				
					<!--xsl:when test="$date-iso castable as xs:dateTime">
						<xsl:value-of select="$date-iso cast as xs:dateTime"/>
					</xsl:when-->


							
						
						<!-- $date-string was in YYYYMMDD format, but values for some of components were incorrect (e.g. February 31). -->
					
				
			</xsl:matching-substring>
		</xsl:analyze-string>
	</xsl:template>

	<!-- CODE FOR WEBRESOURCES -->

	<xsl:template name="webresources">
    <xsl:param name="itemID"/>
    <xsl:for-each select="varfield[number(@id) = 655 and @i1='e']">
      <!-- generate about uri  -->
      <edm:WebResource>
          <xsl:attribute name="rdf:about">
						<xsl:value-of select="subfield[@label='u']"/>
          </xsl:attribute>
      </edm:WebResource>
      <xsl:text>
</xsl:text>
    </xsl:for-each>
  </xsl:template>

	<!-- CODE FOR TIMESPANS -->

  <xsl:template name="timeSpans">
    <xsl:param name="itemID"/>
    <xsl:for-each select="varfield[number(@id) = 425 and @i1='a']">
      <!-- generate about uri  -->
			<xsl:if test="contains(subfield[@label='a'],'-')">
				<edm:TimeSpan>
        	<xsl:attribute name="rdf:about">
          	<xsl:call-template name="timeSpanID">
            	<xsl:with-param name="itemID" select="$itemID"/>
          	</xsl:call-template>
        	</xsl:attribute>
					<skos:prefLabel>
						<xsl:attribute name="xml:lang">
          		<xsl:text>und</xsl:text>
          	</xsl:attribute>
		 				<xsl:value-of select="subfield[@label='a']"/>
					</skos:prefLabel>
					<edm:begin>
						<xsl:call-template name="dateHandler">
							<xsl:with-param name="fieldval" select="substring-before(subfield[@label='a'], '-')"/>
						</xsl:call-template>
		 				<!--xsl:value-of select="concat(substring-before(subfield[@label='a'], '-'), '-01-01T00:00:00')"/-->
					</edm:begin>
					<edm:end>
						<xsl:call-template name="dateHandler">
							<xsl:with-param name="fieldval" select="substring-after(subfield[@label='a'], '-')"/>
						</xsl:call-template>						
		 				<!--xsl:value-of select="concat(substring-after(subfield[@label='a'], '-'), '-01-01T00:00:00')"/-->
					</edm:end>
				</edm:TimeSpan>
			</xsl:if>	
      <xsl:text>
</xsl:text>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="timeSpanID">
		<xsl:param name="itemID"/>
  	<xsl:value-of select="concat($baseURI, 'timespan/',
									$provider,'/',
									$collection,'/',
									subfield[@label='a'],'_',$itemID)"/>	
  </xsl:template>

	<!-- CODE FOR PLACES -->

  <xsl:template name="places">
    <xsl:param name="itemID"/>
    <xsl:for-each select="varfield[number(@id) = 410]">
      <!-- generate about uri  -->
      <edm:Place>
        <xsl:attribute name="rdf:about">
          <xsl:call-template name="placeID">
            <xsl:with-param name="itemID" select="$itemID"/>
          </xsl:call-template>
        </xsl:attribute>
        <skos:prefLabel>
					<xsl:attribute name="xml:lang">
          	<xsl:text>und</xsl:text>
          </xsl:attribute>
					<xsl:choose>
						<xsl:when test="subfield[@label='a']!=''">
							<xsl:value-of select="subfield[@label='a']"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:if test="subfield[@label='x']!=''">
								<xsl:value-of select="subfield[@label='x']"/>
							</xsl:if>
						</xsl:otherwise>
					</xsl:choose>
        </skos:prefLabel>
      </edm:Place>
      <xsl:text>
</xsl:text>
    </xsl:for-each>
  </xsl:template>
	

  <xsl:template name="placeID">
		<xsl:param name="itemID"/>
				<xsl:variable name="placeID"> 
					<xsl:choose>
						<xsl:when test="subfield[@label='a']='Ohne Ort' or subfield[@label='a']='o. O.' or subfield[@label='a']='o.O.'">
							 <xsl:value-of select="concat( translate(
                                                translate(subfield[@label='a'], '&gt;&lt;.,[]',''),' ','_'
                                            ),'_',$itemID)"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:choose>
								<xsl:when test="subfield[@label='x']='Ohne Ort' or subfield[@label='x']='o. O.' or subfield[@label='x']='o.O.'">
							 		<xsl:value-of select="concat( translate(
                                                translate(subfield[@label='x'], '&gt;&lt;.,[]',''),' ','_'
                                            ),'_',$itemID)"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:choose>
										<xsl:when test="subfield[@label='x']!=''">
											<xsl:value-of select="subfield[@label='x']"/>
										</xsl:when>
										<xsl:otherwise>
											<xsl:if test="subfield[@label='a']">
												<xsl:value-of select="subfield[@label='a']"/>
											</xsl:if>
										</xsl:otherwise>
									</xsl:choose>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:variable> 
  	<xsl:value-of select="concat($baseURI, 'place/',
																		$provider,'/',
																		$collection,'/',
																		encode-for-uri(
																						translate(
																								translate($placeID, '&gt;&lt;.,[]',''),' ','_'
																						)
																		)
														)"/>	
  </xsl:template>

	<!--  CODE FOR ORGANIZATIONS -->

	<xsl:template name="organizationRole">
		<xsl:param name="itemID"/>
								<xsl:variable name="oType">
                  <xsl:choose>
                    <xsl:when test="./subfield[@label='v'] = '[Adressat]'">
                      <xsl:value-of select = "'bibo:recipient'"/>
                    </xsl:when>
                    <xsl:when test="./subfield[@label='v'] = '[Auftraggeber]'">
                      <xsl:value-of select = "'dm2e:principal'"/>
                    </xsl:when>
                    <xsl:when test="./subfield[@label='v'] = '[Bearbeiter]'">
                      <xsl:value-of select = "'dm2e:contributor'"/>
                    </xsl:when>
                    <xsl:when test="./subfield[@label='v'] = '[Buchmaler]'">
                      <xsl:value-of select = "'dm2e:painter'"/>
                    </xsl:when>
                    <xsl:when test="./subfield[@label='v'] = '[Mutmaßlicher Auftraggeber]'">
                      <xsl:value-of select = "'dm2e:principal'"/>
                    </xsl:when>
                    <xsl:when test="./subfield[@label='v'] = '[Mutmaßlicher Maler]'">
                      <xsl:value-of select = "'dm2e:painter'"/>
                    </xsl:when>
                    <xsl:when test="./subfield[@label='v'] = '[Mutmaßlicher Verfasser]'">
                      <xsl:value-of select = "'pro:author'"/>
                    </xsl:when>
                    <xsl:when test="./subfield[@label='v'] = '[Mutmaßlicher Vorbesitzer]'">
                      <xsl:value-of select = "'dm2e:previousOwner'"/>
                    </xsl:when>
                    <xsl:when test="./subfield[@label='v'] = '[Schreiber]'">
                      <xsl:value-of select = "'dm2e:writer'"/>
                    </xsl:when>
                    <xsl:when test="./subfield[@label='v'] = '[Übersetzer]'">
                      <xsl:value-of select = "'pro:translator'"/>
                    </xsl:when>
                    <xsl:when test="./subfield[@label='v'] = '[Verfasserin]'">
                      <xsl:value-of select = "'pro:author'"/>
                    </xsl:when>
                    <xsl:when test="./subfield[@label='v'] = '[Verfasser]'">
                      <xsl:value-of select = "'pro:author'"/>
                    </xsl:when>
                    <xsl:when test="./subfield[@label='v'] = '[Vorbesitzer]'">
                      <xsl:value-of select = "'dm2e:previousOwner'"/>
                    </xsl:when>
                    <xsl:when test="./subfield[@label='v'] = '[Widmungsempfänger]'">
                      <xsl:value-of select = "'dm2e:mentioned'"/>
                    </xsl:when>
                    <xsl:when test="./subfield[@label='v'] = '[Zeichner]'">
                      <xsl:value-of select = "'pro:illustrator'"/>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:value-of select = "'edm:hasMet'"/>
                    </xsl:otherwise>
                  </xsl:choose>
								</xsl:variable>
	
		          <xsl:element name="{$oType}">
								<xsl:attribute name="rdf:resource">
          				<xsl:call-template name="organizationID">
            				<xsl:with-param name="itemID" select="$itemID"/>
          				</xsl:call-template>
								</xsl:attribute>
              </xsl:element>

	</xsl:template>

  <xsl:template name="organizations">
		<xsl:param name="itemID"/>
		<xsl:for-each select="varfield[number(@id) &gt;= 200 and number(@id) &lt; 300]">
			<!-- generate about uri  -->
			<foaf:Organization>
				<xsl:attribute name="rdf:about">	
					<xsl:call-template name="organizationID">
						<xsl:with-param name="itemID" select="$itemID"/>
  				</xsl:call-template>
				</xsl:attribute>
				<skos:prefLabel>
					<xsl:attribute name="xml:lang">
						<xsl:text>ger</xsl:text>
					</xsl:attribute>
					<xsl:call-template name="stitchOrganizationNameString">
						<xsl:with-param name="sep">, </xsl:with-param>
					</xsl:call-template>
				</skos:prefLabel>				
				<xsl:if test="subfield[@label='9']!=''">
					<owl:sameAs>
						<xsl:attribute name="rdf:resource">
							<xsl:value-of select="concat('http://d-nb.info/gnd/',replace(subfield[@label='9'], '\(DE-588\)',''))"/>
						</xsl:attribute>
					</owl:sameAs>
				</xsl:if>

			</foaf:Organization>
			<xsl:text>
</xsl:text>
		</xsl:for-each>
  </xsl:template>


  <xsl:template name="organizationID">
		<xsl:param name="itemID"/>
				<xsl:variable name="personID"> 
					<xsl:choose>
						<xsl:when test="subfield[@label='a']='Unbekannt' or subfield[@label='a']='Verschiedene'">
							 <xsl:value-of select="concat(subfield[@label='a'],'_',subfield[@label='v'],'_',$itemID)"/>
						</xsl:when>
						<xsl:otherwise>
								<xsl:call-template name="stitchOrganizationNameString">
									<xsl:with-param name="sep">_</xsl:with-param>
								</xsl:call-template>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:variable> 
  	<xsl:value-of select="concat($baseURI, 'agent/',
																		$provider,'/',
																		$collection,'/',
																		encode-for-uri(
																						translate(
																								translate($personID, '&gt;&lt;.,[]',''),' ','_'
																						)
																		)
														)"/>	
  </xsl:template>


	<xsl:template name="stitchOrganizationNameString">
		<xsl:param name="sep"/>
							<xsl:choose>
								<xsl:when test="subfield[@label='k']!=''">
									<xsl:variable name="appendix">
										<xsl:choose>
											<xsl:when test="subfield[@label='b']!='' and subfield[@label='h']!=''">
												<xsl:value-of select="concat($sep,subfield[@label='b'], $sep, subfield[@label='h'])"/>
											</xsl:when>
											<xsl:otherwise>
												<xsl:choose>
                      		<xsl:when test="subfield[@label='b']!=''">
                        		<xsl:value-of select="concat($sep,subfield[@label='b'])"/>
                      		</xsl:when>
                      		<xsl:otherwise>
														<xsl:if test="subfield[@label='h']!=''">
                        			<xsl:value-of select="concat($sep,subfield[@label='h'])"/>	
														</xsl:if>
                      		</xsl:otherwise>
 		                  	</xsl:choose>
											</xsl:otherwise>
										</xsl:choose>
									</xsl:variable>
									<xsl:value-of select="concat(subfield[@label='k'], $appendix)"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="subfield[@label='a']"/>
								</xsl:otherwise>
							</xsl:choose>
	</xsl:template>	


	<!--  CODE FOR PERSONS -->

	<xsl:template name="personRole">
		<xsl:param name="itemID"/>
               <xsl:variable name="pType">
                  <xsl:choose>
                    <xsl:when test="./subfield[@label='b'] = '[Adressat]'">
                      <xsl:value-of select = "'bibo:recipient'"/>
                    </xsl:when>
                    <xsl:when test="./subfield[@label='b'] = '[Auftraggeber]'">
                      <xsl:value-of select = "'dm2e:principal'"/>
                    </xsl:when>
                    <xsl:when test="./subfield[@label='b'] = '[Bearbeiter]'">
                      <xsl:value-of select = "'dm2e:contributor'"/>
                    </xsl:when>
                    <xsl:when test="./subfield[@label='b'] = '[Buchmaler]'">
                      <xsl:value-of select = "'dm2e:painter'"/>
                    </xsl:when>
                    <xsl:when test="./subfield[@label='b'] = '[Mutmaßlicher Auftraggeber]'">
                      <xsl:value-of select = "'dm2e:principal'"/>
                    </xsl:when>
                    <xsl:when test="./subfield[@label='b'] = '[Mutmaßlicher Maler]'">
                      <xsl:value-of select = "'dm2e:painter'"/>
                    </xsl:when>
                    <xsl:when test="./subfield[@label='b'] = '[Mutmaßlicher Verfasser]'">
                      <xsl:value-of select = "'pro:author'"/>
                    </xsl:when>
                    <xsl:when test="./subfield[@label='b'] = '[Mutmaßlicher Vorbesitzer]'">
                      <xsl:value-of select = "'dm2e:previousOwner'"/>
                    </xsl:when>
                    <xsl:when test="./subfield[@label='b'] = '[Schreiber]'">
                      <xsl:value-of select = "'dm2e:writer'"/>
                    </xsl:when>
                    <xsl:when test="./subfield[@label='b'] = '[Übersetzer]'">
                      <xsl:value-of select = "'pro:translator'"/>
                    </xsl:when>
                    <xsl:when test="./subfield[@label='b'] = '[Verfasserin]'">
                      <xsl:value-of select = "'pro:author'"/>
                    </xsl:when>
                    <xsl:when test="./subfield[@label='b'] = '[Verfasser]'">
                      <xsl:value-of select = "'pro:author'"/>
                    </xsl:when>
                    <xsl:when test="./subfield[@label='b'] = '[Vorbesitzer]'">
                      <xsl:value-of select = "'dm2e:previousOwner'"/>
                    </xsl:when>
                    <xsl:when test="./subfield[@label='b'] = '[Widmungsempfänger]'">
                      <xsl:value-of select = "'dm2e:mentioned'"/>
                    </xsl:when>
                    <xsl:when test="./subfield[@label='b'] = '[Zeichner]'">
                      <xsl:value-of select = "'pro:illustrator'"/>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:value-of select = "'edm:hasMet'"/>
                    </xsl:otherwise>
                  </xsl:choose>
							</xsl:variable>
              <xsl:element name="{$pType}">
                <!-- <xsl:text> PERSONNE </xsl:text> -->
								<xsl:attribute name="rdf:resource">
          				<xsl:call-template name="personID">
            				<xsl:with-param name="itemID" select="$itemID"/>
          				</xsl:call-template>
								</xsl:attribute>
              </xsl:element>
	</xsl:template>

  <xsl:template name="persons">
		<xsl:param name="itemID"/>
		<xsl:for-each select="varfield[number(@id) &gt;= 100 and number(@id) &lt; 200]">
			<!-- generate about uri  -->
			<foaf:Person>
				<xsl:attribute name="rdf:about">	
					<xsl:call-template name="personID">
						<xsl:with-param name="itemID" select="$itemID"/>
  				</xsl:call-template>
				</xsl:attribute>
				<skos:prefLabel>
					<xsl:attribute name="xml:lang">
						<xsl:text>ger</xsl:text>
					</xsl:attribute>
					<xsl:call-template name="stitchPersonNameString">
						<xsl:with-param name="sep">, </xsl:with-param>
					</xsl:call-template>
				</skos:prefLabel>				

				<xsl:if test="substring-before(subfield[@label='d'], '-')!=''">
					<rdaGr2:dateOfBirth>
						<xsl:value-of select="concat(substring-before(subfield[@label='d'], '-'),'-01-01T00:00:00')"/>
					</rdaGr2:dateOfBirth>
				</xsl:if>
				<xsl:if test="substring-after(subfield[@label='d'], '-')!=''">
					<rdaGr2:dateOfDeath>
						<xsl:value-of select="concat(substring-after(subfield[@label='d'], '-'), '-01-01T00:00:00')"/>
					</rdaGr2:dateOfDeath>
				</xsl:if>
				<xsl:if test="subfield[@label='9']!=''">
				<owl:sameAs>
					<xsl:attribute name="rdf:resource">
						<xsl:value-of select="concat('http://d-nb.info/gnd/',replace(subfield[@label='9'], '\(DE-588\)',''))"/>
					</xsl:attribute>
				</owl:sameAs>
				</xsl:if>				
			</foaf:Person>
			<xsl:text>
</xsl:text>
		</xsl:for-each>
  </xsl:template>





  <xsl:template name="personID">
		<xsl:param name="itemID"/>
				<xsl:variable name="personID"> 
					<xsl:choose>
						<xsl:when test="subfield[@label='a']='Unbekannt' or subfield[@label='a']='Verschiedene'">
							 <xsl:value-of select="concat(subfield[@label='a'],'_',subfield[@label='b'],'_',$itemID)"/>
						</xsl:when>
						<xsl:otherwise>
								<xsl:call-template name="stitchPersonNameString">
									<xsl:with-param name="sep">_</xsl:with-param>
								</xsl:call-template>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:variable> 
  	<xsl:value-of select="concat($baseURI, 'agent/',
																		$provider,'/',
																		$collection,'/',
																		encode-for-uri(
																						translate(
																								translate($personID, '&gt;&lt;.,[]',''),' ','_'
																						)
																		)
														)"/>	
  </xsl:template>


	<xsl:template name="stitchPersonNameString">
		<xsl:param name="sep"/>
							<xsl:choose>
								<xsl:when test="subfield[@label='p']!=''">
									<xsl:variable name="appendix">
										<xsl:choose>
											<xsl:when test="subfield[@label='n']!='' and subfield[@label='c']!=''">
												<xsl:value-of select="concat($sep,subfield[@label='n'], $sep, subfield[@label='c'])"/>
											</xsl:when>
											<xsl:otherwise>
												<xsl:choose>
                      		<xsl:when test="subfield[@label='n']!=''">
                        		<xsl:value-of select="concat($sep,subfield[@label='n'])"/>
                      		</xsl:when>
                      		<xsl:otherwise>
														<xsl:if test="subfield[@label='c']!=''">
                        			<xsl:value-of select="concat($sep,subfield[@label='c'])"/>	
														</xsl:if>
                      		</xsl:otherwise>
 		                  	</xsl:choose>
											</xsl:otherwise>
										</xsl:choose>
									</xsl:variable>
									<xsl:value-of select="concat(subfield[@label='p'], $appendix)"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="subfield[@label='a']"/>
								</xsl:otherwise>
							</xsl:choose>
	</xsl:template>	


  <xsl:template match="//mets:mets">
		<!-- Collect all top level info -->
		<xsl:param name="createDate" select="mets:metsHdr/@CREATEDATE"/>
		<!-- <xsl:param name="itemID" select="mets:dmdSec/mets:mdWrap/mets:xmlData/record/dc:alephsyncid"/> -->
		<!-- <xsl:param name="itemTitle" select="mets:dmdSec/mets:mdWrap/mets:xmlData/record/dc:title"/> -->
		<xsl:param name="itemID" select="//dc:alephsyncid"/>
		<xsl:param name="itemTitle" select="//dc:title"/>
  		<xsl:param name="imguri"/>

			

  		<xsl:call-template name="page">
				<xsl:with-param name="itemID" select="$itemID"/>
				<xsl:with-param name="itemTitle" select="$itemTitle"/>
				<xsl:with-param name="createDate" select="$createDate"/>
  				<xsl:with-param name="imguri" select="$imguri"/>
  		</xsl:call-template>
    
  </xsl:template>

	<xsl:template name="page">
		<xsl:param name="itemID"/>
		<xsl:param name="itemTitle"/>
		<xsl:param name="createDate"/>
		<xsl:param name="imguri"/>
		<xsl:param name="itemUri">
    	<xsl:value-of select="$baseURI"/>
    	<xsl:text>item/</xsl:text>
    	<xsl:value-of select="$provider"/>
    	<xsl:text>/</xsl:text>
    	<xsl:value-of select="$collection"/>
    	<xsl:text>/</xsl:text>
    	<xsl:value-of select="$itemID"/>
		</xsl:param>

		<xsl:param name="aggregationUri">
    	<xsl:value-of select="$baseURI"/>
    	<xsl:text>aggregation/</xsl:text>
    	<xsl:value-of select="$provider"/>
    	<xsl:text>/</xsl:text>
    	<xsl:value-of select="$collection"/>
    	<xsl:text>/</xsl:text>
    	<xsl:value-of select="$itemID"/>
		</xsl:param>

		<xsl:for-each select="mets:structMap/mets:div/mets:div">

			<!-- Extract page global info only once -->

  		<xsl:call-template name="pageProvidedCHO">
				<xsl:with-param name="itemID" select="$itemID"/>
				<xsl:with-param name="itemTitle" select="$itemTitle"/>
				<xsl:with-param name="createDate" select="$createDate"/>
				<xsl:with-param name="itemUri" select="$itemUri"/>
				<xsl:with-param name="aggregationUri" select="$aggregationUri"/>
				<xsl:with-param name="order" select="@ORDER"/>
				<xsl:with-param name="label" select="@LABEL"/>
				<xsl:with-param name="type" select="@TYPE"/>
  		</xsl:call-template>

  		<xsl:call-template name="pageOreAggregation">
				<xsl:with-param name="itemID" select="$itemID"/>
				<xsl:with-param name="itemTitle" select="$itemTitle"/>
				<xsl:with-param name="createDate" select="$createDate"/>
				<xsl:with-param name="itemUri" select="$itemUri"/>
				<xsl:with-param name="aggregationUri" select="$aggregationUri"/>
				<xsl:with-param name="order" select="@ORDER"/>
				<xsl:with-param name="label" select="@LABEL"/>
				<xsl:with-param name="type" select="@TYPE"/>
  				<xsl:with-param name="imguri" select="$imguri"/>
  		</xsl:call-template>
						
		</xsl:for-each>
	
	</xsl:template>

	<!-- CODE FOR THE AGGREGATION -->
	<xsl:template name="pageOreAggregation">
		<xsl:param name="itemID"/>
		<xsl:param name="itemTitle"/>
		<xsl:param name="createDate"/>
		<xsl:param name="itemUri"/>
		<xsl:param name="aggregationUri"/>
		<xsl:param name="order"/>
		<xsl:param name="label"/>
		<xsl:param name="type"/>
		<xsl:param name="imguri"/>

		<!-- We create a WebResource for the AnnotatableVersion -->		
		
		<!--file://streams/  -->
		
		<xsl:param name="pageuri">
			<xsl:value-of select="replace(//mets:mets/mets:fileSec/mets:fileGrp[@USE='reference image']/mets:file[@SEQ=$order]/mets:FLocat/@xlink:href, 'file://streams/' , $imguri)"/>
		</xsl:param>
		<xsl:param name="pagemime">
			<xsl:value-of select="//mets:mets/mets:fileSec/mets:fileGrp[@USE='reference image']/mets:file[@SEQ=$order]/@MIMETYPE"/>
		</xsl:param>

		<xsl:if test="$pageuri!=''">
			<edm:WebResource>
				<xsl:attribute name="rdf:about">
					<xsl:value-of select="$pageuri"/>
				</xsl:attribute>
				<dc:format>
					<xsl:value-of select="$pagemime"/>
				</dc:format>
			</edm:WebResource>
		</xsl:if>

		<ore:Aggregation>
			<xsl:attribute name="rdf:about">
				<xsl:value-of select="$aggregationUri"/>
				<xsl:text>-</xsl:text>
				<xsl:value-of select="$order"/>
			</xsl:attribute>
		
			<edm:aggregatedCHO>
				<xsl:attribute name="rdf:resource">
					<xsl:value-of select="$itemUri"/>
					<xsl:text>-</xsl:text>
					<xsl:value-of select="$order"/>
				</xsl:attribute>
			</edm:aggregatedCHO>
		
			<edm:dataProvider>
				<xsl:attribute name="rdf:resource">
					<xsl:value-of select="$dataProvID"/>
				</xsl:attribute>
			</edm:dataProvider>
			
			<edm:isShownBy>
				<xsl:attribute name="rdf:resource">
					<xsl:value-of select="$pageuri"/>
				</xsl:attribute>
			</edm:isShownBy>	
			
			<dm2e:hasAnnotatableVersionAt>
				<xsl:attribute name="rdf:resource">
					<xsl:value-of select="$pageuri"/>
				</xsl:attribute>
			</dm2e:hasAnnotatableVersionAt>
		
			
			<edm:provider>
				<xsl:attribute name="rdf:resource">
					<xsl:value-of select="$dm2eID"/>
				</xsl:attribute>
			</edm:provider>	
	
			<edm:rights>
				<xsl:value-of select="$rights"/>
			</edm:rights>
	
				<dcterms:created>
					 <xsl:value-of select="$createDate"/>
				</dcterms:created>



		</ore:Aggregation>
	</xsl:template>
	
	<!-- CODE FOR THE PROVIDEDCHO -->
	<xsl:template name="pageProvidedCHO">
		<xsl:param name="itemID"/>
		<xsl:param name="itemTitle"/>
		<xsl:param name="createDate"/>
		<xsl:param name="itemUri"/>
		<xsl:param name="aggregationUri"/>
		<xsl:param name="order"/>
		<xsl:param name="label"/>
		<xsl:param name="type"/>
		<edm:ProvidedCHO>
            <xsl:attribute name="rdf:about">
							<xsl:value-of select="$itemUri"/>
							<xsl:text>-</xsl:text>
							<xsl:value-of select="$order"/>
            </xsl:attribute>
            <dc:type>
              <xsl:attribute name="rdf:resource">
                <xsl:text>http://purl.org/spar/fabio/#Page</xsl:text>
              </xsl:attribute>
            </dc:type>


						<dcterms:isPartOf>
            	<xsl:attribute name="rdf:resource">
								<xsl:value-of select="$itemUri"/>
            	</xsl:attribute>
						</dcterms:isPartOf>

			<!-- 037ba1 Sprachcode NEU -->
			<xsl:for-each select="$languages">
				<dc:language>
					<xsl:value-of select="."/>
				</dc:language>
			</xsl:for-each>


			<!-- 331-a1 Hauptsachtitel in Vorlageform -->
            <dcterms:description>
            	<xsl:attribute name="xml:lang">
              	<xsl:text>ger</xsl:text>
              </xsl:attribute>
              <xsl:value-of select="concat('[',$type,' ',$label,'] von:',$itemTitle)"/>
            </dcterms:description>

			<!-- 331-a1 Hauptsachtitel in Vorlageform -->
            <dcterms:title>
            	<xsl:attribute name="xml:lang">
              	<xsl:text>ger</xsl:text>
              </xsl:attribute>
              <xsl:value-of select="concat('[',$type,' ',$label,']')"/>
            </dcterms:title>
			
			<xsl:if test="//mets:mets/mets:fileSec/mets:fileGrp[@USE='archive']/mets:file[@SEQ=($order - 1)]">
				<edm:isNextInSequence>
					<xsl:attribute name="rdf:resource">
						<xsl:value-of select="$itemUri"/>
						<xsl:text>-</xsl:text>
						<xsl:value-of select="$order - 1"/>
					</xsl:attribute>
				</edm:isNextInSequence>
			</xsl:if>			
			
			<edm:type>
				<xsl:value-of select="$aggregationType"/>
			</edm:type>
		</edm:ProvidedCHO>
	</xsl:template>


	
	<!-- CODE FOR WEBRESOURCES -->
	<!--
	<xsl:template name="pageWebresources">
    <xsl:param name="itemID"/>
    <xsl:for-each select="varfield[number(@id) = 655 and @i1='e']">
      <edm:WebResource>
          <xsl:attribute name="about">
						<xsl:value-of select="subfield[@label='u']"/>
          </xsl:attribute>
      </edm:WebResource>
    </xsl:for-each>
  </xsl:template>
	-->

</xsl:stylesheet>
