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
  xmlns:xml="http://www.w3.org/XML/1998/namespace"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">


  <!--
	Mappings: Polytechnisches Journal (Dingler) to DM2E v1.1
	
	Version:	1.5
	Date:		  2013-08-26
	Authors:	Evelyn Dröge     evelyn.droege@ibi.hu-berlin.de
	          Julia Iwanowa    julia.iwanowa@ibi.hu-berlin.de
	          	
	         
	         
	LOG:
	
	Version:  1.5
	           - Corrected subtitles
	
	Version:  1.4
	           - Checked mandatory elements
	           - Checked RDF result
	
	Version:  1.3 
	           - Journal: Removed alternativeTitle
	           - Journal: Changed dc:contributor
	           - Volume: Changed dc:contributor
	           - Article: Changed TableOfContents
	           - Article: Added type for dm2e:hasAnnotatableVersionAt
	           - Article: Remodved isPartOfWebResource for <edm:WebResource rdf:about="http://dingler.culture.hu-berlin.de">
	           - Article and Page: Changed isDerivativeOfWebResource in <edm:WebResource rdf:about="http://dingler.culture.hu-berlin.de">
	           - Page: Added type for dm2e:hasAnnotatableVersionAt; changed Urls
	           - Page: Removed bibo:imageNum
	           - Page: Changed dc:contributor
	           - Page: Changed edm:isShownBy
	Version:  1.2 Added Pages and Journal           
	Version:  1.1 Correction of the inital mapping. Page mapping is incomplete.
	Version:	1.0 Initial mapping (based on an MINT export; structure from Kilian Schmidtners TEItoEDM mappings)
	-->


  <!-- imports -->
  <xsl:import href="UBER_Dingler_Journal.xsl"/>
  <xsl:import href="UBER_Dingler_Article.xsl"/>
  <xsl:import href="UBER_Dingler_Page.xsl"/>
  <xsl:import href="UBER_Dingler_Issue.xsl"/>
  <!--<xsl:import href="UBER_Dingler_Recursion.xsl"/>-->
  
  <!-- output -->
  <xsl:output method="xml" encoding="utf-8" omit-xml-declaration="no" indent="yes"/>

  <!-- parameters and default values -->
  <!-- Target-Format: EDM | EDMplus -->
  <xsl:param name="TARGET_FORMAT">DM2E</xsl:param>
  <!--  Abbreviation of the data provider -->
  <xsl:param name="DATAPROVIDER_ABB">uber</xsl:param>
  <!-- Abbreviation of the data provider repository -->
  <xsl:param name="REPOSITORY_ABB">dingler</xsl:param>
  <!-- Default Web Resource URL -->
  <xsl:param name="DEF_URL">http://dingler.culture.hu-berlin.de/</xsl:param>
  <!-- Default data provider -->
  <xsl:param name="DEF_DATAPROVIDER">Polytechnisches Journal</xsl:param>
  <!-- Default Language -->
  <xsl:param name="DEF_LANGUAGE">ger</xsl:param>
  <!-- Default type -->
  <xsl:param name="DEF_DCTYPE">text</xsl:param>
  <!-- Default rights -->
  <xsl:variable name="RIGHTS_RESOURCE">http://creativecommons.org/licenses/by-nc-sa/3.0/</xsl:variable>
  <!-- DM2E-ID-Root -->
  <xsl:param name="baseURI">http://data.dm2e.eu/data/</xsl:param>
  <!-- Dataprovider und Repostitory -->
  <xsl:variable name="PROV_REP" select="concat('/', $DATAPROVIDER_ABB, '/', $REPOSITORY_ABB, '/')"/>
  <!-- Default subject -->
  <xsl:param name="DEF_SUBJECT">engineering and natural sciences (19th century)</xsl:param>

  <!-- DM2E URIs -->
  <!-- aggregation -->
  <xsl:variable name="aggregation" select="concat($baseURI, 'aggregation', $PROV_REP)"/>
  <!-- CHO -->
  <xsl:variable name="cho" select="concat($baseURI, 'item', $PROV_REP)"/>
  <!-- agent -->
  <xsl:variable name="agent" select="concat($baseURI, 'agent', $PROV_REP)"/>
  <!-- concept -->
  <xsl:variable name="concept" select="concat($baseURI, 'concept', $PROV_REP)"/>
  <!-- place -->
  <xsl:variable name="place" select="concat($baseURI, 'place', $PROV_REP)"/>
  <!-- timespan -->
  <xsl:variable name="timespan" select="concat($baseURI, 'timespan', $PROV_REP)"/>
  <!-- event -->
  <xsl:variable name="event" select="concat($baseURI, 'event', $PROV_REP)"/>



  <!-- adapted MINT part -->

  <!-- <xsl:template match="tei:teiHeader"> -->

  <xsl:template match="/">

  <rdf:RDF>

    <xsl:call-template name="Journal">
      <xsl:with-param name="theTEIHeader" select="tei:TEI/tei:teiHeader"/>
    </xsl:call-template>
    
    <xsl:call-template name="Issue">
      <xsl:with-param name="theTEIHeader" select="tei:TEI/tei:teiHeader"/>
    </xsl:call-template> 

    <xsl:call-template name="Article">
      <xsl:with-param name="theTEIHeader" select="tei:TEI/tei:teiHeader"/>
    </xsl:call-template> 

    <xsl:call-template name="Page">
      <xsl:with-param name="theTEIHeader" select="tei:TEI/tei:teiHeader"/>
    </xsl:call-template>
    
   
   </rdf:RDF>
  </xsl:template>



</xsl:stylesheet>
