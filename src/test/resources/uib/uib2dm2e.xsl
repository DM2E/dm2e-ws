<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:dcterms="http://purl.org/dc/terms/"
                xmlns:tei="http://www.tei-c.org/ns/1.0"
                xmlns:edm="http://www.europeana.eu/schemas/edm/"
                xmlns:dm2e="http://onto.dm2e.eu/schemas/dm2e/1.1/"
                xmlns:ore="http://www.openarchives.org/ore/terms/"
                xmlns:oai="http://www.openarchives.org/OAI/2.0/"
                xmlns:owl="http://www.w3.org/2002/07/owl#"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                xmlns:wgs84_pos="http://www.w3.org/2003/01/geo/wgs84_pos#"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:tei2edm="http://sbb.spk-berlin.de/dm2e/tei2edm"
                version="2.0">
   <xsl:param name="TARGET_FORMAT">DM2E</xsl:param>
   <xsl:param name="EDM_RICH">true</xsl:param>
   <xsl:param name="TRANSFORMATION_DEPTH">AB</xsl:param>
   <xsl:param name="THE_DETAIL">Remark</xsl:param>
   <xsl:param name="DATAPROVIDER_ABB">UiB</xsl:param>
   <xsl:param name="REPOSITORY_ABB">Wittgenstein_Archives</xsl:param>
   <xsl:param name="DEF_URL">http://www.myLibraryArchiveMusem.org/</xsl:param>
   <xsl:param name="DEF_DATAPROVIDER">Wittgenstein Archives UiB</xsl:param>
   <xsl:param name="DEF_COVERAGE">19th-20th century</xsl:param>
   <xsl:param name="DEF_LANGUAGE">ger</xsl:param>
   <xsl:param name="DEF_DCTYPE">text</xsl:param>

    <!--
	Description: 
	This stylesheet converts TEI (Text Encoding Initiative) to EDM (Europeana Data Model).	
	It is accompanied by a schematron validation for the transformation results.
	
	Version:	0.3.0
	Date: 		2013-07-03
	Authors: 	Øyvind Gjesdal <Oyvind.Gjesdal@ub.uib.no>, Kilian Schmidtner <kilian.schmidtner@sbb.spk-berlin.de>
	
	LOG: 
	Version 0.2.2: Fixed namespaces and XSD-references, according to recent EDM.xsd (2013-06-19) 
	Version 0.2.1: Corrections according to UIB requirements
	Version 0.2:   Changes according to UIB requirements
	Version 0.1:   Initial Version
	-->

    <!-- Target Format imports -->
    

    <xsl:template name="writeProcessingInstruction">
        <xsl:if test="$TARGET_FORMAT eq 'EDM'">
            <xsl:processing-instruction name="xml-model">href="http://europeanalabs.eu/svn/europeana/trunk/corelib/corelib-solr-definitions/src/main/resources/eu/EDM.xsd" type="application/xml" schematypens="http://purl.oclc.org/dsdl/schematron"</xsl:processing-instruction>
        </xsl:if>
        <xsl:if test="$TARGET_FORMAT eq 'DM2E'">
            <xsl:processing-instruction name="xml-model">href="../../03_EDM_xsd/EDM.xsd" type="application/xml" schematypens="http://purl.oclc.org/dsdl/schematron"</xsl:processing-instruction>
        </xsl:if>
    </xsl:template>
    
    
    <xsl:template name="writeNamespaces">
        <xsl:namespace name="xsi" select="'http://www.w3.org/2001/XMLSchema-instance'"/> 
        <xsl:if test="$TARGET_FORMAT eq 'EDM'">
            
        </xsl:if>
        <xsl:if test="$TARGET_FORMAT eq 'DM2E'">
            
        </xsl:if>
        
        <xsl:namespace name="dc" select="'http://purl.org/dc/elements/1.1/'"/>
        <xsl:namespace name="edm" select="'http://www.europeana.eu/schemas/edm/'"/>
        <xsl:namespace name="wgs84_pos" select="'http://www.w3.org/2003/01/geo/wgs84_pos#'"/>
        <xsl:namespace name="enrichment"
                     select="'http://www.europeana.eu/schemas/edm/enrichment/'"/>
        <xsl:namespace name="oai" select="'http://www.openarchives.org/OAI/2.0/'"/>
        <xsl:namespace name="owl" select="'http://www.w3.org/2002/07/owl#'"/>
        <xsl:namespace name="rdf" select="'http://www.w3.org/1999/02/22-rdf-syntax-ns#'"/>		    
        <xsl:namespace name="ore" select="'http://www.openarchives.org/ore/terms/'"/>
        <xsl:namespace name="skos" select="'http://www.w3.org/2004/02/skos/core#'"/>
        <xsl:namespace name="dcterms" select="'http://purl.org/dc/terms/'"/>
        
        <xsl:if test="$TARGET_FORMAT eq 'DM2E'">
            <!-- <xsl:namespace name="rdfs" select="'http://www.w3.org/2000/01/rdf-schema#'"/> -->
            <xsl:namespace name="foaf" select="'http://xmlns.com/foaf/0.1/'"/>
            <xsl:namespace name="bibo" select="'http://purl.org/ontology/bibo'"/>
            <xsl:namespace name="pro" select="'http://purl.org/spar/pro/'"/>
            <xsl:namespace name="dm2e" select="$DM2E_SCHEMA_NS"/>    
        </xsl:if>
        
    </xsl:template>
    

    
    <!-- Datamodel imports -->
	
  
	  <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 xmlns:bibo="http://purl.org/ontology/bibo"
                 name="createProvidedCHOBook">

		    <xsl:variable name="AnID" select="tei2edm:searchForID()"/>
		
		    <xsl:element name="edm:ProvidedCHO">
			      <xsl:attribute name="rdf:about">
				        <xsl:value-of select="tei2edm:concatItemURI($AnID)"/>
			      </xsl:attribute>
			
			      <xsl:call-template name="applyDCProperties"/>
			
			      <xsl:call-template name="applyDCTermsProperties"/>

			      <xsl:call-template name="applyEDMProperties"/>
			
			      <xsl:if test="$TARGET_FORMAT eq 'DM2E'">
				        <xsl:call-template name="applyDM2EProperties"/>
			      </xsl:if>
			
		    </xsl:element>	
	  </xsl:template>
  
  	<xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 xmlns:bibo="http://purl.org/ontology/bibo"
                 name="applyDCProperties">
		<!-- 
		DONE <element ref="dc:contributor"/>
		DONE <element ref="dc:coverage"/>
		DONE <element ref="dc:creator"/>
		<element ref="dc:date"/>
		DONE <element ref="dc:description"/>
		<element ref="dc:format"/>
		DONE <element ref="dc:identifier"/>
		DONE <element ref="dc:language"/>
		DONE <element ref="dc:publisher"/>
		<element ref="dc:relation"/>
		<element ref="dc:rights"/>
		<element ref="dc:source"/>
		DONE<element ref="dc:subject"/>
		DONE <element ref="dc:title"/> 	
		<element ref="dc:type"/>	
		-->
	    
	    <!-- dc:contributor -->
  	    <xsl:if test="not($TARGET_FORMAT eq 'DM2E')">
  	        <xsl:call-template name="writeDcContributors"/>    
  	    </xsl:if>
        	
	    <!-- dc:creator -->
	    <xsl:apply-templates select="./tei:fileDesc/tei:titleStmt/tei:author"/>
		
		    <!-- dc:description -->
	    <xsl:apply-templates select="./tei:fileDesc/tei:sourceDesc/tei:p | tei:encodingDesc/tei:p"/>	
		
		    <!-- identifier -->
		    <xsl:call-template name="writeIdentifier">
			      <xsl:with-param name="identifier" select="tei2edm:searchForID()"/>
		    </xsl:call-template>

		    <!-- dc:language -->
		    <xsl:choose>
			      <xsl:when test="exists(tei:profileDesc/tei:langUsage/tei:language/@ident)">
				        <xsl:element name="dc:language">
					          <xsl:value-of select="tei:profileDesc/tei:langUsage/tei:language/@ident"/>
				        </xsl:element>
			      </xsl:when>
			      <xsl:when test="count(distinct-values(//@xml:lang) ) gt 0">
				        <xsl:for-each select="distinct-values(//@xml:lang)">
					          <xsl:element name="dc:language">
							           <xsl:value-of select="."/>
					          </xsl:element>
				        </xsl:for-each>
			      </xsl:when>
			      <xsl:otherwise>
				        <xsl:element name="dc:language">
					          <xsl:value-of select="$DEF_LANGUAGE"/>
				        </xsl:element>
			      </xsl:otherwise>
		    </xsl:choose>
		
		    <!-- dc:publisher -->
		    <xsl:call-template name="writePublisher"/>
		
		    <!-- dc:subject -->
		    <xsl:call-template name="writeSubjects"/>
		
		    <!-- dc:title -->
		    <xsl:apply-templates select="tei:fileDesc/tei:titleStmt/tei:title"/>

	  </xsl:template>	
  
  	<xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 xmlns:bibo="http://purl.org/ontology/bibo"
                 name="applyDCTermsProperties">
		<!--
		DCTerms 
		<element ref="dcterms:alternative"/>
		<element ref="dcterms:conformsTo"/>
		<element ref="dcterms:created"/>
		DONE <element ref="dcterms:extent"/>
		<element ref="dcterms:hasFormat"/>
		DONE <element ref="dcterms:hasPart"/>
		<element ref="dcterms:hasVersion"/>
		<element ref="dcterms:isFormatOf"/>
		<element ref="dcterms:isPartOf"/>
		<element ref="dcterms:isReferencedBy"/>
		<element ref="dcterms:isReplacedBy"/>
		<element ref="dcterms:isRequiredBy"/>
		DONE <element ref="dcterms:issued"/>
		<element ref="dcterms:isVersionOf"/>
		<element ref="dcterms:medium"/>
		<element ref="dcterms:provenance"/>
		<element ref="dcterms:references"/>
		<element ref="dcterms:replaces"/>
		<element ref="dcterms:requires"/>
		DONE <element ref="dcterms:spatial"/>
		DONE <element ref="dcterms:tableOfContents"/>
		<element ref="dcterms:temporal"/>
		-->
	
  	    <xsl:apply-templates select="./tei:fileDesc/tei:sourceDesc/tei:bibl"/>
	
		    <!-- dcterms:extent -->
		    <xsl:apply-templates select="./tei:fileDesc/tei:sourceDesc/tei:biblFull/tei:extent/tei:measure"/>
  	    
  	    <!-- dcterms:hasPart -->
        <xsl:call-template name="writeDctermsHasParts"/>
		
		    <!-- dcterms:issued -->
		    <xsl:apply-templates select="./tei:fileDesc/tei:sourceDesc/tei:biblFull/tei:publicationStmt/tei:date"/>
		
  	    <!-- dc:coverage (dm2e:printedAt) -->
  	    <xsl:apply-templates select="./tei:fileDesc/tei:sourceDesc/tei:biblFull/tei:publicationStmt/tei:pubPlace"/>
		
  	    <!-- dcterms:tableOfContents -->
		    <xsl:call-template name="writeDctermsTableOfContents"/> 
		
 	</xsl:template> 
  
  
	  <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 xmlns:bibo="http://purl.org/ontology/bibo"
                 name="applyEDMProperties">
		<!--
		Base:
		<element ref="edm:currentLocation" maxOccurs="1" minOccurs="0"/>
		<element ref="edm:hasMet" maxOccurs="unbounded" minOccurs="0"/>
		<element ref="edm:incorporates" maxOccurs="unbounded" minOccurs="0"/>
		<element ref="edm:isDerivativeOf" maxOccurs="unbounded" minOccurs="0"/>
		<element ref="edm:isNextInSequence" maxOccurs="1" minOccurs="0"/>
		<element ref="edm:isRelatedTo" maxOccurs="unbounded" minOccurs="0"/>
		<element ref="edm:isRepresentationOf" maxOccurs="1" minOccurs="0"/>
		<element ref="edm:isSimilarTo" maxOccurs="unbounded" minOccurs="0"/>
		<element ref="edm:isSuccessorOf" maxOccurs="unbounded" minOccurs="0"/>
		<element ref="edm:realizes" maxOccurs="unbounded" minOccurs="0"/>
		DONE <element name="type" maxOccurs="1" minOccurs="1" type="edm:EdmType"/> 
		EDM:
		<element ref="owl:sameAs" maxOccurs="unbounded" minOccurs="0"/> 
		-->	

	    <!--  edm:currentLocation -->
	    <xsl:apply-templates select="./descendant::tei:msDesc/descendant::tei:repository[1]"/>
	    
	    <xsl:if test="$TARGET_FORMAT eq 'DM2E'">
	        <xsl:apply-templates select="./descendant::tei:msDesc/descendant::tei:idno[@type eq 'shelfmark']"/>    
	    </xsl:if>
	    
		    <!-- edm:type | TEXT-->
		    <xsl:element name="edm:type">
			      <xsl:text>TEXT</xsl:text>		
		    </xsl:element>
	  </xsl:template>
  
	  <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 xmlns:bibo="http://purl.org/ontology/bibo"
                 name="applyDM2EProperties">
		<!-- 
		  The editors mentioned in titleStmt are more editors/contributors of the Aggregation 
		  than the providedCHO itself.
		-->
	    
	    <!-- 
	        
	        Deleted generation of dm2e:contributor, duplicates maintenance.
	        Went back to dc:contributor (see above)  
	    -->
	</xsl:template>

	  <!-- dc:creator -->
    <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 xmlns:bibo="http://purl.org/ontology/bibo"
                 match="tei:fileDesc/tei:titleStmt/tei:author">
			   <xsl:choose>
				<!-- DTA uses PND, forename and surname -->
			      <xsl:when test="exists(./tei:persName/tei:forename) and exists(./tei:persName/tei:surname)">
					       <xsl:call-template name="writeCreatorAuthor">
					          <xsl:with-param name="authorName" select="./tei:persName"/>
					       </xsl:call-template>
				     </xsl:when>
				     <xsl:otherwise>
				        <xsl:call-template name="writeCreatorAuthor">
				           <xsl:with-param name="authorName" select="normalize-space(.)"/>
				        </xsl:call-template>
				     </xsl:otherwise>
			   </xsl:choose>
	  </xsl:template>
	
	  <!-- dc:title -->
	  <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 xmlns:bibo="http://purl.org/ontology/bibo"
                 match="tei:fileDesc/tei:titleStmt/tei:title">
		    <xsl:call-template name="writeDCTitle">
		       <xsl:with-param name="title" select="."/>
		    </xsl:call-template>
	  </xsl:template>
	
    <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 xmlns:bibo="http://purl.org/ontology/bibo"
                 match="tei:bibl">
        <xsl:element name="dcterms:alternative">
            <xsl:attribute name="xml:lang">de</xsl:attribute>
            <xsl:value-of select="normalize-space(.)"/>
        </xsl:element>
    </xsl:template>
	
	
	  <!-- dcterms:spatial  -->
	  <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 xmlns:bibo="http://purl.org/ontology/bibo"
                 match="tei:fileDesc/tei:sourceDesc/tei:biblFull/tei:publicationStmt/tei:pubPlace">
	    <xsl:choose>
	        <xsl:when test="$TARGET_FORMAT eq 'DM2E'">
	            <xsl:call-template name="writeDm2ePrintedAt">
	                <xsl:with-param name="placeName" select="."/>
	            </xsl:call-template>
	        </xsl:when>
	        <xsl:otherwise>
	            <xsl:call-template name="writeDctermsSpatial">
	                <xsl:with-param name="placeName" select="."/>
	            </xsl:call-template>
	        </xsl:otherwise>
	    </xsl:choose>
	  </xsl:template>
    
    
	  <!-- dcterms:issued -->
	  <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 xmlns:bibo="http://purl.org/ontology/bibo"
                 match="tei:fileDesc/tei:sourceDesc/tei:biblFull/tei:publicationStmt/tei:date">
		    <xsl:element name="dcterms:issued">
			      <xsl:value-of select="."/>	
		    </xsl:element>
	  </xsl:template>
  
	  <!-- dcterms:extent -->
	  <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 xmlns:bibo="http://purl.org/ontology/bibo"
                 match="tei:fileDesc/tei:sourceDesc/tei:biblFull/tei:extent/tei:measure">
		    <xsl:element name="dcterms:extent">
			      <xsl:value-of select="."/>
		    </xsl:element>
	  </xsl:template>
  
	  <!-- dc:description (in sense of condition) -->
	  <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 xmlns:bibo="http://purl.org/ontology/bibo"
                 match="tei:fileDesc/tei:sourceDesc/tei:p | tei:encodingDesc/tei:p">
	    <xsl:if test="not(empty(./text() ) )">
	        <xsl:call-template name="writeDescription">
	            <xsl:with-param name="theDescription" select="."/>			
	        </xsl:call-template>    
	    </xsl:if>
	  </xsl:template>	
	  
	  <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 xmlns:bibo="http://purl.org/ontology/bibo"
                 match="tei:repository">
	    <xsl:element name="edm:currentLocation">
	        <xsl:attribute name="rdf:resource">
	            <xsl:value-of select="tei2edm:concatAgentURI(.)"/>
	        </xsl:attribute>
	    </xsl:element>
	  </xsl:template>  
    
    <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 xmlns:bibo="http://purl.org/ontology/bibo"
                 match="tei:idno[@type eq 'shelfmark']">
        <xsl:element name="dm2e:shelfmarkLocation">
            <xsl:value-of select="."/>
        </xsl:element>
    </xsl:template>
	  
 
	
  
	  <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 name="createProvidedCHOPB">	
		    <xsl:param name="thePB" as="node()"/>
		    <xsl:variable name="AnID" select="tei2edm:searchForID()"/>
		    <xsl:variable name="thePBID" select="$thePB/@facs"/>
	    <xsl:variable name="concatIds" select="($AnID, $thePBID)"/>
        

		    <xsl:element name="edm:ProvidedCHO">
			      <xsl:attribute name="rdf:about">
				        <xsl:value-of select="tei2edm:concatItemURI($concatIds)"/>
			      </xsl:attribute>
		
			      <xsl:call-template name="applyDCPropertiesPB">
				        <xsl:with-param name="thePB" select="$thePB"/>
			      </xsl:call-template>
			
			      <xsl:call-template name="applyDCTermsPropertiesPB">
				        <xsl:with-param name="thePB" select="$thePB"/>
			      </xsl:call-template>

			      <xsl:call-template name="applyEDMPropertiesPB">
				        <xsl:with-param name="thePB" select="$thePB"/>
			      </xsl:call-template>

		    </xsl:element>	
	  </xsl:template>
  
	  <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 name="applyDCPropertiesPB">
		    <xsl:param name="thePB" as="node()"/>
		    <!-- 
		<element ref="dc:contributor"/>
		<element ref="dc:coverage"/>
		DONE <element ref="dc:creator"/>
		<element ref="dc:date"/>
		DONE <element ref="dc:description"/>
		<element ref="dc:format"/>
		DONE <element ref="dc:identifier"/>
		DONE <element ref="dc:language"/>
		DONE <element ref="dc:publisher"/>
		<element ref="dc:relation"/>
		<element ref="dc:rights"/>
		<element ref="dc:source"/>
		DONE<element ref="dc:subject"/>
		DONE <element ref="dc:title"/> 	
		<element ref="dc:type"/>	
		-->
		
		    <!-- dc:creator -->
	    <xsl:apply-templates select="$theTEIHeader/tei:fileDesc/tei:titleStmt/tei:author"/>
		
		    <!-- dc:description -->
		    <xsl:call-template name="writeDescription">
			      <xsl:with-param name="theDescription"
                         select="tei2edm:substituteDescription($thePB, $theTEIHeader)"/>	
		    </xsl:call-template> 
		
		    <xsl:if test="$thePB/following::tei:fw[1][@type='header']">
			      <xsl:call-template name="writeDescription">
				        <xsl:with-param name="theDescription"
                            select="$thePB/following::tei:fw[1][@type='header']"/>	
			      </xsl:call-template> 
		    </xsl:if>	
		
		    <!-- identifier -->
		    <xsl:call-template name="writeIdentifier">
			      <xsl:with-param name="identifier" select="$thePB/@facs"/>
		    </xsl:call-template>

		    <!-- dc:language -->
		    <xsl:variable name="precedingNodes"
                    select="$thePB/following::tei:pb[1]/preceding::node()"/>
		    <xsl:variable name="followingNodes" select="$thePB/following::node()"/>
		    <xsl:variable name="intersectingNodes"
                    select="$precedingNodes intersect $followingNodes"/>
		
	    <xsl:variable name="languagesOnPage"
                    select="distinct-values($intersectingNodes/descendant-or-self::element()/@xml:lang)"/>

	    <xsl:choose>
			      <xsl:when test="count($languagesOnPage) gt 0">
				        <xsl:for-each select="$languagesOnPage">
					          <xsl:element name="dc:language">
							           <xsl:value-of select="."/>
					          </xsl:element>
				        </xsl:for-each>
			      </xsl:when>
			      <xsl:when test="exists($theTEIHeader/tei:profileDesc/tei:langUsage/tei:language/@ident)">
				        <xsl:element name="dc:language">
					          <xsl:value-of select="$theTEIHeader/tei:profileDesc/tei:langUsage/tei:language/@ident"/>
				        </xsl:element>
			      </xsl:when>
			      <xsl:otherwise>
				        <xsl:element name="dc:language">
					          <xsl:value-of select="$DEF_LANGUAGE"/>
				        </xsl:element>
			      </xsl:otherwise>
		    </xsl:choose>
		
	    <!-- dc:publisher -->
	    <xsl:call-template name="writePublisher"/>
		
	    <!-- dc:subject -->
		    <xsl:call-template name="writeSubjectsOnPage">
		       <xsl:with-param name="nodesOnPage" select="$intersectingNodes"/>
		    </xsl:call-template>
	    
	    <xsl:call-template name="writeTitlesOnPage">
	        <xsl:with-param name="nodesOnPage" select="$intersectingNodes"/>  
	    </xsl:call-template>
		
		    <xsl:element name="dc:type">
			      <xsl:value-of select="$DEF_DCTYPE"/>
		    </xsl:element>

	    <xsl:element name="dc:type">
	        <xsl:attribute name="rdf:resource">
	            <xsl:value-of select="concat($DM2E_SCHEMA_NS, 'Page')"/>
	        </xsl:attribute>
	    </xsl:element>
	    
	  </xsl:template>	
  
  	<xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 name="applyDCTermsPropertiesPB">
		    <xsl:param name="thePB" as="node()"/>
		
		    <xsl:variable name="AnID" select="tei2edm:searchForID()"/>
		    <!--
		DCTerms 
		<element ref="dcterms:alternative"/>
		<element ref="dcterms:conformsTo"/>
		- <element ref="dcterms:created"/> 
		- <element ref="dcterms:extent"/> Page CHO is always for one page.
		<element ref="dcterms:hasFormat"/>
		<element ref="dcterms:hasPart"/>
		<element ref="dcterms:hasVersion"/>
		<element ref="dcterms:isFormatOf"/>
		DONE <element ref="dcterms:isPartOf"/>
		<element ref="dcterms:isReferencedBy"/>
		<element ref="dcterms:isReplacedBy"/>
		<element ref="dcterms:isRequiredBy"/>
		DONE <element ref="dcterms:issued"/>
		<element ref="dcterms:isVersionOf"/>
		<element ref="dcterms:medium"/>
		<element ref="dcterms:provenance"/>
		<element ref="dcterms:references"/>
		<element ref="dcterms:replaces"/>
		<element ref="dcterms:requires"/>
		DONE <element ref="dcterms:spatial"/>
		DONE <element ref="dcterms:tableOfContents"/>
		<element ref="dcterms:temporal"/>
		-->
	
		    <xsl:element name="dcterms:isPartOf">
			      <xsl:attribute name="rdf:resource">
			         <xsl:value-of select="tei2edm:concatItemURI($AnID)"/>
			      </xsl:attribute>
		    </xsl:element>
		
		    <!-- 
		    dcterms:issued 
		-->
		    <xsl:apply-templates select="$theTEIHeader/tei:fileDesc/tei:sourceDesc/tei:biblFull/tei:publicationStmt/tei:date"/>
		
		    <!-- dcterms:spatial | 410 -->
		    <xsl:apply-templates select="$theTEIHeader/tei:fileDesc/tei:sourceDesc/tei:biblFull/tei:publicationStmt/tei:pubPlace"/>
 	</xsl:template> 
  
  
	  <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 name="applyEDMPropertiesPB">
		    <xsl:param name="thePB" as="node()"/>
		
		    <xsl:variable name="AnID" select="tei2edm:searchForID()"/>
		    <xsl:variable name="theNextPBID" select="$thePB/following::tei:pb[1]/@facs"/>
	    <xsl:variable name="concatNextID" select="($AnID, $theNextPBID)"/>
		    <!--
		Base:
		<element ref="edm:currentLocation" maxOccurs="1" minOccurs="0"/>
		<element ref="edm:hasMet" maxOccurs="unbounded" minOccurs="0"/>
		<element ref="edm:incorporates" maxOccurs="unbounded" minOccurs="0"/>
		<element ref="edm:isDerivativeOf" maxOccurs="unbounded" minOccurs="0"/>
		DONE <element ref="edm:isNextInSequence" maxOccurs="1" minOccurs="0"/>
		<element ref="edm:isRelatedTo" maxOccurs="unbounded" minOccurs="0"/>
		<element ref="edm:isRepresentationOf" maxOccurs="1" minOccurs="0"/>
		<element ref="edm:isSimilarTo" maxOccurs="unbounded" minOccurs="0"/>
		<element ref="edm:isSuccessorOf" maxOccurs="unbounded" minOccurs="0"/>
		<element ref="edm:realizes" maxOccurs="unbounded" minOccurs="0"/>
		DONE <element name="type" maxOccurs="1" minOccurs="1" type="edm:EdmType"/> 
		EDM:
		<element ref="owl:sameAs" maxOccurs="unbounded" minOccurs="0"/> 
		-->	

	    <xsl:variable name="precedingNodes"
                    select="$thePB/following::tei:pb[1]/preceding::node()"/>
	    <xsl:variable name="followingNodes" select="$thePB/following::node()"/>
	    <xsl:variable name="intersectingNodes"
                    select="$precedingNodes intersect $followingNodes"/>
	    
		    <xsl:if test="$theNextPBID">
	        <xsl:call-template name="writeIsNextInSequence">
	            <xsl:with-param name="theNextID" select="$concatNextID"/>
	        </xsl:call-template>
		    </xsl:if>
		
		    <!-- edm:type | TEXT-->
		    <xsl:element name="edm:type">
			      <xsl:text>TEXT</xsl:text>		
		    </xsl:element>
	  </xsl:template>
	  

	
  
    <!-- 
        Persons/Agents, who were involved in the creation of the parent CHO, are omitted. 
	-->
  
	  <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 name="createProvidedCHOAB">
		    <xsl:param name="theAB" as="node()"/>
		    <xsl:variable name="AnID" select="tei2edm:searchForID()"/>
		    <xsl:variable name="theABID" select="$theAB/@xml:id"/>
	    <xsl:variable name="concatIDs" select="($AnID, $theABID)"/>
	    
		    <xsl:element name="edm:ProvidedCHO">
			      <xsl:attribute name="rdf:about">
				        <xsl:value-of select="tei2edm:concatItemURI($concatIDs)"/>
			      </xsl:attribute>
			
			      <xsl:call-template name="applyDCPropertiesAB">
				        <xsl:with-param name="theAB" select="$theAB"/>
			      </xsl:call-template>
			
			      <xsl:call-template name="applyDCTermsPropertiesAB">
				        <xsl:with-param name="theAB" select="$theAB"/>
			      </xsl:call-template>

			      <xsl:call-template name="applyEDMPropertiesAB">
				        <xsl:with-param name="theAB" select="$theAB"/>
			      </xsl:call-template>

		       <xsl:if test="$TARGET_FORMAT eq 'DM2E'">
		          <xsl:call-template name="applyDM2EPropertiesParagraph">	
		            <xsl:with-param name="theAB" select="$theAB"/>	
		          </xsl:call-template>
            </xsl:if>
			
		    </xsl:element>	
	  </xsl:template>
  
	  <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 name="applyDCPropertiesAB">
		    <xsl:param name="theAB" as="node()"/>
		    <!-- 
		<element ref="dc:contributor"/>
		<element ref="dc:coverage"/>
		DONE <element ref="dc:creator"/>
		<element ref="dc:date"/>
		DONE <element ref="dc:description"/>
		<element ref="dc:format"/>
		DONE <element ref="dc:identifier"/>
		DONE <element ref="dc:language"/>
		<element ref="dc:publisher"/>
		<element ref="dc:relation"/>
		<element ref="dc:rights"/>
		<element ref="dc:source"/>
		DONE<element ref="dc:subject"/>
		DONE <element ref="dc:title"/> 	
		DONE <element ref="dc:type"/>	
		-->
		
		    <!-- dc:creator -->
		    <xsl:apply-templates select="$theTEIHeader/tei:fileDesc/tei:titleStmt/tei:author"/>
		
		    <!-- dc:description -->
		    <xsl:call-template name="writeDescription">
			      <xsl:with-param name="theDescription"
                         select="tei2edm:substituteDescription($theAB, $theTEIHeader)"/>	
		    </xsl:call-template> 
		
		    <!-- identifier -->
		    <xsl:call-template name="writeIdentifier">
			      <xsl:with-param name="identifier" select="$theAB/@xml:id"/>
		    </xsl:call-template>

		    <!-- dc:language -->
		    <xsl:variable name="languagesInParagraph"
                    select="distinct-values($theAB/descendant-or-self::node()/@xml:lang)"/>

		    <xsl:choose>
			      <xsl:when test="count($languagesInParagraph) gt 0">
				        <xsl:for-each select="$languagesInParagraph">
					          <xsl:element name="dc:language">
							           <xsl:value-of select="."/>
					          </xsl:element>
				        </xsl:for-each>
			      </xsl:when>
			      <xsl:when test="exists($theTEIHeader/tei:profileDesc/tei:langUsage/tei:language/@ident)">
				        <xsl:element name="dc:language">
					          <xsl:value-of select="$theTEIHeader/tei:profileDesc/tei:langUsage/tei:language/@ident"/>
				        </xsl:element>
			      </xsl:when>
			      <xsl:otherwise>
				        <xsl:element name="dc:language">
					          <xsl:value-of select="$DEF_LANGUAGE"/>
				        </xsl:element>
			      </xsl:otherwise>
		    </xsl:choose>
		
		    <xsl:call-template name="writeDcSubjectsOnParagraph">
		       <xsl:with-param name="theDetail" select="$theAB"/>
		    </xsl:call-template>

		
		    <!-- dc:type --> 
		
		    <xsl:element name="dc:type">
			      <xsl:value-of select="$DEF_DCTYPE"/>
		    </xsl:element>
		
	    <xsl:element name="dc:type">
	        <xsl:attribute name="rdf:resource">
	            <xsl:text>http://onto.dm2e.eu/schemas/dm2e/1.1/Paragraph</xsl:text>
	        </xsl:attribute>
	    </xsl:element>

	  </xsl:template>	
  
  	<xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 name="applyDCTermsPropertiesAB">
		    <xsl:param name="theAB" as="node()"/>
		
		    <xsl:variable name="AnID" select="tei2edm:searchForID()"/>
		    <!--
		DCTerms 
		<element ref="dcterms:alternative"/>
		<element ref="dcterms:conformsTo"/>
		<element ref="dcterms:created"/>
		<element ref="dcterms:extent"/>
		<element ref="dcterms:hasFormat"/>
		<element ref="dcterms:hasPart"/>
		<element ref="dcterms:hasVersion"/>
		<element ref="dcterms:isFormatOf"/>
		DONE <element ref="dcterms:isPartOf"/>
		<element ref="dcterms:isReferencedBy"/>
		<element ref="dcterms:isReplacedBy"/>
		<element ref="dcterms:isRequiredBy"/>
		DONE <element ref="dcterms:issued"/>
		<element ref="dcterms:isVersionOf"/>
		<element ref="dcterms:medium"/>
		<element ref="dcterms:provenance"/>
		DONE <element ref="dcterms:references"/>
		<element ref="dcterms:replaces"/>
		<element ref="dcterms:requires"/>
		<element ref="dcterms:spatial"/>
		<element ref="dcterms:tableOfContents"/>
		<element ref="dcterms:temporal"/>
		-->
		
		    <!-- dcterms:alternative -->
		    <!-- <xsl:apply-templates select="XXX"/> -->
	
		    <!-- dcterms:extent -->
		    <!-- Todo: 
		<xsl:apply-templates select="./tei:fileDesc/tei:sourceDesc/tei:biblFull/tei:extent/tei:measure"/> -->
	
		    <!-- Todo: dcterms:hasPart 
		<xsl:element name="dcterms:isPartOf">
			<xsl:attribute name="rdf:resource">
				<xsl:value-of select="concat($EDM_ID_ROOT, 'item/', $PROV_REP, '/', $AnID)"/>
			</xsl:attribute>
		</xsl:element> -->
		
		    <!-- dcterms:issued -->
        <xsl:call-template name="findDatesInAB">
            <xsl:with-param name="theAB" select="$theAB"/>
        </xsl:call-template>
  	    <!--
		<xsl:for-each select="./tei:date">
			<xsl:call-template name="writeIssued">
					<xsl:with-param name="dateStr" select="
					concat(
						substring(./@when-iso, 1, 4) , '-' ,
						substring(./@when-iso, 5, 2) , '-' ,
						substring(./@when-iso, 7, 2) 
					)
				"/>
			</xsl:call-template>
		</xsl:for-each>  -->
		
		
		    <!-- dcterms:references -->
		    <xsl:for-each select="distinct-values($theAB/descendant::tei:rs[@type='extref']/concat(./@key, ', ', ./@n) )">
			      <xsl:element name="dcterms:references">
				        <xsl:value-of select="."/>
			      </xsl:element>
		    </xsl:for-each>
		
		    <!-- dcterms:spatial | 410 
		<xsl:apply-templates select="./tei:fileDesc/tei:sourceDesc/tei:biblFull/tei:publicationStmt/tei:pubPlace"/>
		-->
 	</xsl:template> 
  
  
	  <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 name="applyEDMPropertiesAB">
		    <xsl:param name="theAB" as="node()"/>
		
		    <xsl:variable name="AnID" select="tei2edm:searchForID()"/>
		    <xsl:variable name="theNextABID" select="$theAB/following::tei:ab[1]/@xml:id"/>
	    <xsl:variable name="concatNextID" select="($AnID, $theNextABID)"/>
		
		    <!--
		Base:
		<element ref="edm:currentLocation" maxOccurs="1" minOccurs="0"/>
		<element ref="edm:hasMet" maxOccurs="unbounded" minOccurs="0"/>
		<element ref="edm:isDerivativeOf" maxOccurs="unbounded" minOccurs="0"/>
		DONE <element ref="edm:isNextInSequence" maxOccurs="1" minOccurs="0"/>
		DONE <element ref="edm:isRelatedTo" maxOccurs="unbounded" minOccurs="0"/>
		<element ref="edm:isRepresentationOf" maxOccurs="1" minOccurs="0"/>
		<element ref="edm:isSimilarTo" maxOccurs="unbounded" minOccurs="0"/>
		<element ref="edm:isSuccessorOf" maxOccurs="unbounded" minOccurs="0"/>
		<element ref="edm:realizes" maxOccurs="unbounded" minOccurs="0"/>
		DONE <element name="type" maxOccurs="1" minOccurs="1" type="edm:EdmType"/> 
		EDM:
		<element ref="owl:sameAs" maxOccurs="unbounded" minOccurs="0"/> 
		-->	
		
		    <xsl:if test="$theNextABID">
			      <xsl:call-template name="writeIsNextInSequence">
			         <xsl:with-param name="theNextID" select="$concatNextID"/>
			      </xsl:call-template>
		    </xsl:if>
		
		    <xsl:if test="not($TARGET_FORMAT eq 'DM2E')">
		       <xsl:call-template name="writeEdmRelatedTo">
		          <xsl:with-param name="theDetail" select="$theAB"/>
		       </xsl:call-template>
		    </xsl:if>
		
		    <!-- edm:type | TEXT-->
		    <xsl:element name="edm:type">
			      <xsl:text>TEXT</xsl:text>		
		    </xsl:element>
	  </xsl:template>
  
  
  	<xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 name="applyDM2EPropertiesParagraph">
		    <xsl:param name="theAB" as="node()"/>

		    <xsl:call-template name="writeDm2eMentioned">
		       <xsl:with-param name="theDetail" select="$theAB"/>
		    </xsl:call-template>
		
	  </xsl:template>
  

    
    
    <!-- 
        This file contains all parameterized, context-insensitive templates.
    -->
    
    <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 xmlns:bibo="http://purl.org/ontology/bibo"
                 xmlns:pro="http://purl.org/spar/pro/"
                 name="writeDcContributors">
        <!-- No contributor infos yet -->
    </xsl:template>
    
    <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 xmlns:bibo="http://purl.org/ontology/bibo"
                 xmlns:pro="http://purl.org/spar/pro/"
                 name="writeDcContributor">
        <xsl:param name="name" as="xs:string"/>
        
        <xsl:element name="dc:contributor">
            <xsl:call-template name="writeAgentRdfResourceOrLiteral">
                <xsl:with-param name="agentsName" select="$name"/>
            </xsl:call-template>
        </xsl:element>	            
    </xsl:template>
    
    <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 xmlns:bibo="http://purl.org/ontology/bibo"
                 xmlns:pro="http://purl.org/spar/pro/"
                 name="writeBiboEditor">
        <xsl:param name="editorName"/>
        
        <xsl:element name="bibo:editor">
            <xsl:call-template name="writeAgentRdfResourceOrLiteral">
                <xsl:with-param name="agentsName" select="$editorName"/>
            </xsl:call-template>
        </xsl:element>
        
    </xsl:template>
    
    <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 xmlns:bibo="http://purl.org/ontology/bibo"
                 xmlns:pro="http://purl.org/spar/pro/"
                 name="writeCreatorAuthor">
        <xsl:param name="authorName"/>
        <xsl:choose>
            <xsl:when test="$TARGET_FORMAT eq 'DM2E'">
                <xsl:element name="pro:author">
                    <xsl:call-template name="writeAgentRdfResourceOrLiteral">
                        <xsl:with-param name="agentsName" select="tei2edm:formatPersName($authorName)"/>
                    </xsl:call-template>
                </xsl:element>
            </xsl:when>
            <xsl:otherwise>
                <xsl:element name="dc:creator">
                    <xsl:call-template name="writeAgentRdfResourceOrLiteral">
                        <xsl:with-param name="agentsName" select="tei2edm:formatPersName($authorName)"/>
                    </xsl:call-template>
                </xsl:element>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <!-- dc:identifier -->
    <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 xmlns:bibo="http://purl.org/ontology/bibo"
                 xmlns:pro="http://purl.org/spar/pro/"
                 name="writeIdentifier">
        <xsl:param name="identifier"/>
        
        <xsl:element name="dc:identifier">
            <xsl:value-of select="$identifier"/>
        </xsl:element>
    </xsl:template>
    
    <!-- dc:description -->
    <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 xmlns:bibo="http://purl.org/ontology/bibo"
                 xmlns:pro="http://purl.org/spar/pro/"
                 name="writeDescription">
        <xsl:param name="theDescription"/>
        <xsl:element name="dc:description">
            <xsl:attribute name="xml:lang">
                <xsl:value-of select="$DEF_LANGUAGE"/>
            </xsl:attribute>
            <xsl:value-of select="normalize-space($theDescription)"/>			
        </xsl:element>
    </xsl:template>	
    
    <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 xmlns:bibo="http://purl.org/ontology/bibo"
                 xmlns:pro="http://purl.org/spar/pro/"
                 name="writeDcCoverage">
        <xsl:param name="placeName" as="xs:string"/>
        <xsl:element name="dc:coverage">
            <xsl:call-template name="writePlaceRdfResourceOrLiteral">
                <xsl:with-param name="placeName" select="$placeName"> </xsl:with-param>
            </xsl:call-template>
        </xsl:element>
    </xsl:template>
    
    <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 xmlns:bibo="http://purl.org/ontology/bibo"
                 xmlns:pro="http://purl.org/spar/pro/"
                 name="writeDctermsSpatial">
        <xsl:param name="placeName" as="xs:string"/>
        <xsl:element name="dcterms:spatial">
            <xsl:call-template name="writePlaceRdfResourceOrLiteral">
                <xsl:with-param name="placeName" select="$placeName"> </xsl:with-param>
            </xsl:call-template>
        </xsl:element>
    </xsl:template>
    
    <!-- dc:subject -->
    <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 xmlns:bibo="http://purl.org/ontology/bibo"
                 xmlns:pro="http://purl.org/spar/pro/"
                 name="writeDcSubject">
        <xsl:param name="subject" as="xs:string"/>
            
        <xsl:choose>
            <xsl:when test="$EDM_RICH eq 'true'">
                <xsl:element name="dc:subject">
                    <xsl:attribute name="rdf:resource">
                        <xsl:value-of select="tei2edm:concatConceptURI($subject)"/>    
                    </xsl:attribute>
                </xsl:element>
            </xsl:when>
            <xsl:otherwise>
                <xsl:element name="dc:subject">
                    <xsl:value-of select="normalize-space($subject)"/>			
                </xsl:element>
            </xsl:otherwise>
        </xsl:choose>    
            
    </xsl:template>	
    
    <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 xmlns:bibo="http://purl.org/ontology/bibo"
                 xmlns:pro="http://purl.org/spar/pro/"
                 name="writeDCTitle">
        <xsl:param name="title" as="xs:string"/>
        
        <xsl:element name="dc:title">
            <xsl:attribute name="xml:lang">
                <xsl:value-of select="$DEF_LANGUAGE"/>
            </xsl:attribute>
            <xsl:value-of select="normalize-space($title)"/>				
        </xsl:element>        
    </xsl:template>
    
    
    <!-- dcterms:issued -->
    <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 xmlns:bibo="http://purl.org/ontology/bibo"
                 xmlns:pro="http://purl.org/spar/pro/"
                 name="writeIssued">
        <xsl:param name="dateStr"/>
        <xsl:element name="dcterms:issued">
            <xsl:value-of select="$dateStr"/>	
        </xsl:element>
    </xsl:template> 
    
    <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 xmlns:bibo="http://purl.org/ontology/bibo"
                 xmlns:pro="http://purl.org/spar/pro/"
                 name="writeDm2ePublishedAt">
        <xsl:param name="placeName" as="xs:string"/>
        <xsl:element name="dm2e:publishedAt">
            <xsl:call-template name="writePlaceRdfResourceOrLiteral">
                <xsl:with-param name="placeName" select="$placeName"> </xsl:with-param>
            </xsl:call-template>
        </xsl:element>
    </xsl:template>
    
    <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 xmlns:bibo="http://purl.org/ontology/bibo"
                 xmlns:pro="http://purl.org/spar/pro/"
                 name="writeDm2ePrintedAt">
        <xsl:param name="placeName" as="xs:string"/>
        <xsl:element name="dm2e:printedAt">
            <xsl:call-template name="writePlaceRdfResourceOrLiteral">
                <xsl:with-param name="placeName" select="$placeName"> </xsl:with-param>
            </xsl:call-template>
        </xsl:element>
    </xsl:template>
    
    <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 xmlns:bibo="http://purl.org/ontology/bibo"
                 xmlns:pro="http://purl.org/spar/pro/"
                 name="writeIsNextInSequence">
        <xsl:param name="theNextID" as="xs:string*"/>
        
        <xsl:element name="edm:isNextInSequence">
            <xsl:attribute name="rdf:resource">
                <xsl:value-of select="tei2edm:concatItemURI($theNextID)"/>
            </xsl:attribute>
        </xsl:element>
        
    </xsl:template>
    
    <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 xmlns:bibo="http://purl.org/ontology/bibo"
                 xmlns:pro="http://purl.org/spar/pro/"
                 name="writeAgentRdfResourceOrLiteral">
        <xsl:param name="agentsName"/>
            
        <xsl:choose>
            <xsl:when test="$EDM_RICH eq 'true'">
                <xsl:attribute name="rdf:resource">
                    <xsl:value-of select="tei2edm:concatAgentURI($agentsName)"/>
                </xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="normalize-space($agentsName)"/>  
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>  
    
    <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 xmlns:bibo="http://purl.org/ontology/bibo"
                 xmlns:pro="http://purl.org/spar/pro/"
                 name="writePlaceRdfResourceOrLiteral">
        <xsl:param name="placeName"/>
        <xsl:choose>
            <xsl:when test="$EDM_RICH eq 'true'">
                <xsl:attribute name="rdf:resource">
                    <xsl:value-of select="tei2edm:concatPlaceURI($placeName)"/>
                </xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="normalize-space($placeName)"/>  
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>    
    

	
    
    <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 name="createAggregation">
        <xsl:param name="theContextNode" as="node()"/>
        <xsl:variable name="theContextID"
                    select="tei2edm:searchForContextID($theContextNode)"/>
        
        <xsl:element name="ore:Aggregation">
            
            <xsl:call-template name="writeRdfAbout_CHO_DataProvider">
                <xsl:with-param name="theIds" select="$theContextID" as="xs:string*"/>
            </xsl:call-template>
            
            <xsl:choose>
                <xsl:when test="$theContextNode/self::tei:teiHeader">
                    <xsl:call-template name="writeAggregationResources"/>
                </xsl:when>
                <xsl:when test="$theContextNode/self::tei:pb">
                    <xsl:call-template name="writeAggregationPBResources">
                        <xsl:with-param name="thePB" select="$theContextNode"/>
                    </xsl:call-template>
                </xsl:when>
                <xsl:when test="$theContextNode/self::tei:ab">
                    <xsl:call-template name="writeAggregationABResources">
                        <xsl:with-param name="theAB" select="$theContextNode"/>
                    </xsl:call-template>
                </xsl:when>
            </xsl:choose>
            
            <xsl:call-template name="writeProviderRightsAndDates"/>
            
        </xsl:element>
        
    </xsl:template>
    
    <xsl:function xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 name="tei2edm:searchForContextID"
                 as="xs:string*">
        <xsl:param name="theContextNode" as="node()"/>
        <xsl:variable name="AnID" select="tei2edm:searchForID()"/>
        
        <xsl:choose>
            <xsl:when test="$theContextNode/self::tei:teiHeader">
                <xsl:copy-of select="($AnID)"/>
            </xsl:when>
            <xsl:when test="$theContextNode/self::tei:pb">
                <xsl:variable name="thePBID" select="$theContextNode/@facs"/>
                <xsl:copy-of select="($AnID, $thePBID)"/>
            </xsl:when>
            <xsl:when test="$theContextNode/self::tei:ab">
                <xsl:variable name="theABID" select="$theContextNode/@xml:id"/>
                <xsl:copy-of select="($AnID, $theABID)"/>
            </xsl:when>
        </xsl:choose>
        
    </xsl:function>

	
 
    
    
    <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 name="writeRdfAbout_CHO_DataProvider">
        <xsl:param name="theIds" as="xs:string*"/>
        
        <xsl:attribute name="rdf:about">
            <xsl:value-of select="tei2edm:concatAggregationURI($theIds)"/>
        </xsl:attribute>
        
        <!-- edm:aggregatedCHO -->
        <xsl:element name="edm:aggregatedCHO">
            <xsl:attribute name="rdf:resource">
                <xsl:value-of select="tei2edm:concatItemURI($theIds)"/>
            </xsl:attribute>
        </xsl:element>
        
        <!-- edm:dataProvider  -->
        <xsl:call-template name="writeAggregationDataprovider"/>
        
    </xsl:template>
        
    <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 name="writeProviderRightsAndDates">
        
        <!-- edm:provider -->
        <xsl:call-template name="writeAggregationProvider"/>
        
        <!-- dc:rights & edm:rights-->
        <xsl:call-template name="writeRights"/>
            
        <xsl:call-template name="writeDM2EAggregationDates"/>
        
        <xsl:if test="$TARGET_FORMAT eq 'DM2E'">
            <xsl:call-template name="writeDcContributorsForAggregation"/>    
        </xsl:if>
            
    </xsl:template>
    
    
    <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 name="writeAggregationDataprovider">
        <xsl:element name="edm:dataProvider">
            <xsl:call-template name="writeProviderResourceOrLiteral"/>
        </xsl:element>
    </xsl:template>
    
    <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 name="writeAggregationProvider">
        <xsl:choose>
            <xsl:when test="$TARGET_FORMAT = 'DM2E'">
                <xsl:element name="edm:provider">
                    <xsl:attribute name="rdf:resource">
                        <xsl:value-of select="concat($EDM_ID_ROOT, 'agent/DM2E')"/>
                    </xsl:attribute>
                </xsl:element>
            </xsl:when>
            <xsl:otherwise>
                <xsl:element name="edm:provider">
                   <xsl:call-template name="writeProviderResourceOrLiteral"/>
                </xsl:element>    
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 name="writeProviderResourceOrLiteral">
        <xsl:choose>
            <xsl:when test="$theTEIHeader/tei:fileDesc/tei:publicationStmt/tei:publisher/tei:orgName">
                <xsl:call-template name="writeAgentRdfResourceOrLiteral">
                    <xsl:with-param name="agentsName"
                               select="$theTEIHeader/tei:fileDesc/tei:publicationStmt/tei:publisher/tei:orgName[1]"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="writeAgentRdfResourceOrLiteral">
                    <xsl:with-param name="agentsName"
                               select="$theTEIHeader/tei:fileDesc/tei:titleStmt/tei:editor/tei:orgName[1]"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    
    <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 name="writeRights">
        <xsl:param name="theTEIHeader"/>
        <!-- dc:rights -->
        <xsl:call-template name="writeAggregationDcRights"/>
            
        <!-- edm:rights -->
        <xsl:call-template name="createEdmRights"/>
    </xsl:template>
    
    <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 name="writeAggregationDcRights">
        <xsl:element name="dc:rights">
            <xsl:value-of select="normalize-space($theTEIHeader/tei:fileDesc/tei:publicationStmt/tei:availability/tei:p)"/>
            <xsl:value-of select="normalize-space($theTEIHeader/tei:fileDesc/tei:publicationStmt/tei:availability/tei:licence)"/>
        </xsl:element>
    </xsl:template>
    
    <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 name="writeDM2EAggregationDates">
        <xsl:if test="$TARGET_FORMAT eq 'DM2E'">	
            <!-- dcterms:created -->
            <!-- Todo: dateformat -->
            <xsl:if test="exists($theTEIHeader/tei:fileDesc/tei:publicationStmt/tei:date)">
                <xsl:element name="dcterms:created">
                    <xsl:value-of select="$theTEIHeader/tei:fileDesc/tei:publicationStmt/tei:date"/> 
                </xsl:element>
            </xsl:if>
            
            <!-- dcterms:modified -->
            <!-- Todo: dateformat -->
            <!-- <xsl:element name="dcterms:modified">
					<xsl:value-of select="tei:fileDesc/tei:publicationStmt/tei:date"/>
				</xsl:element> -->
        </xsl:if>
    </xsl:template>
    

	

  	<xsl:template xmlns:dct="http://purl.org/dc/terms/"
                 xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 name="createWebResourcesBook">
		
  	    <xsl:for-each select="(//tei:idno[@type='URL'], //tei:idno[@type='URLWeb'], //tei:idno[@type='URL-Web'])">
			      <xsl:call-template name="writeWebResource">
				        <xsl:with-param name="theAboutURL" select="."/>
			      </xsl:call-template>
		    </xsl:for-each>
		
  	    <xsl:call-template name="writeWebResourceBookSpec"/>
		
	  </xsl:template>
  
	  <xsl:template xmlns:dct="http://purl.org/dc/terms/"
                 xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 name="writeWebResource">
		    <xsl:param name="theAboutURL" as="xs:string"/>
	    <xsl:param name="mime" as="xs:string" select="'NONE'"/>
				
		    <xsl:element name="edm:WebResource">
			      <xsl:attribute name="rdf:about">
				        <xsl:value-of select="$theAboutURL"/>
			      </xsl:attribute>
			      <xsl:call-template name="writeDcFormat">
                <xsl:with-param name="mime" select="$mime"/>			    
			      </xsl:call-template>
		       <!-- <xsl:call-template name="createDcRights"/> -->
			      <xsl:call-template name="createEdmRights"/>	
		    </xsl:element>
		
	  </xsl:template>

  
    <xsl:template xmlns:dct="http://purl.org/dc/terms/"
                 xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 name="writeDcFormat">
        <xsl:param name="mime" as="xs:string" select="'NONE'"/>
        <xsl:if test="not($mime eq 'NONE')">
            <xsl:element name="dc:format">
                <xsl:call-template name="writeDCFormatResourceOrLiteral">
                    <xsl:with-param name="mime" select="$mime"/>
                </xsl:call-template>
            </xsl:element> 
        </xsl:if>
    </xsl:template>
  
    <xsl:template xmlns:dct="http://purl.org/dc/terms/"
                 xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 name="writeDCFormatResourceOrLiteral">
        <xsl:param name="mime" as="xs:string" select="'NONE'"/>
        <xsl:choose>
            <xsl:when test="$TARGET_FORMAT eq 'DM2E'">
                <xsl:attribute name="rdf:resource">
                    <xsl:value-of select="concat($DM2E_SCHEMA_NS, 'mime-types/', $mime)"/>
                </xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$mime"/>
            </xsl:otherwise>            
        </xsl:choose>
    </xsl:template>
  
    
  
   <!--	<xsl:template name="createDcRights">
		<xsl:if test="/tei:TEI/tei:teiHeader/tei:fileDesc/tei:publicationStmt/tei:availability/tei:p">
			<xsl:element name="dc:rights">
					<xsl:value-of select="/tei:TEI/tei:teiHeader/tei:fileDesc/tei:publicationStmt/tei:availability/tei:p"/>
			</xsl:element>
		</xsl:if>
	</xsl:template>-->
    
	  <xsl:template xmlns:dct="http://purl.org/dc/terms/"
                 xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 name="createEdmRights">
		    <xsl:element name="edm:rights">
			      <xsl:attribute name="rdf:resource">
				        <xsl:value-of select="$RIGHTS_RESOURCE"/>
			      </xsl:attribute>
		    </xsl:element>
	  </xsl:template>
    
 
	

    <!-- AGENTS -->
    <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 xmlns:foaf="http://xmlns.com/foaf/0.1/"
                 name="writeAllAgents">
        <!-- all names in teiHeader -->
        <xsl:call-template name="writeAgentsBook"/>
        
        <xsl:if test="$TRANSFORMATION_DEPTH eq 'AB'">
            <!-- all names within the text-->
            <xsl:call-template name="createAgentsAB"/>
        </xsl:if>
        <xsl:if test="$TRANSFORMATION_DEPTH eq 'PB'">
            <!-- all names within the text-->
            <xsl:call-template name="createAgentsPB"/>
        </xsl:if>
    </xsl:template>

    <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 xmlns:foaf="http://xmlns.com/foaf/0.1/"
                 name="createAgent">
        <xsl:param name="prefLabel"/>
        <xsl:param name="PND" required="no"/>

        <xsl:choose>
            <xsl:when test="$TARGET_FORMAT eq 'EDM'">
                <xsl:call-template name="writeAgentClass">
                    <xsl:with-param name="prefLabel" select="$prefLabel"/>
                    <xsl:with-param name="CLASSNAME" select="'edm:Agent'"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$TARGET_FORMAT eq 'DM2E'">
                <xsl:call-template name="writeAgentClass">
                    <xsl:with-param name="prefLabel" select="$prefLabel"/>
                    <xsl:with-param name="CLASSNAME" select="'foaf:Person'"/>
                </xsl:call-template>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 xmlns:foaf="http://xmlns.com/foaf/0.1/"
                 name="writeAgentClass">
        <xsl:param name="prefLabel"/>
        <xsl:param name="PND" required="no"/>
        <xsl:param name="CLASSNAME"/>

        <xsl:element name="{$CLASSNAME}">
            <xsl:attribute name="rdf:about">
                <xsl:value-of select="tei2edm:concatAgentURI($prefLabel)"/>
            </xsl:attribute>
            <xsl:element name="skos:prefLabel">
                <xsl:value-of select="normalize-space($prefLabel)"/>
            </xsl:element>
        </xsl:element>

    </xsl:template>


 
	

    <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 name="writePlaces">
        
        <xsl:variable name="allPlaces"
                    select="(             for $place in //tei:pubPlace[ancestor::tei:sourceDesc] return normalize-space($place),             for $place in //tei:placeName return normalize-space($place)             )"/>
        
        <xsl:for-each select="distinct-values($allPlaces)">
            <xsl:call-template name="createPlace">
                <xsl:with-param name="prefLabel" select="."/>
            </xsl:call-template>
        </xsl:for-each>
        
    </xsl:template>

    <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 name="createPlace">
        <xsl:param name="prefLabel" as="xs:string"/>
        
        <xsl:element name="edm:Place">
            <xsl:attribute name="rdf:about">
                <xsl:value-of select="tei2edm:concatPlaceURI($prefLabel)"/>
            </xsl:attribute>
            <xsl:element name="skos:prefLabel">
                <xsl:value-of select="$prefLabel"/>
            </xsl:element>
            <!-- <xsl:element name="skos:altLabel">
                <xsl:value-of select="$geoNode/geonames/geoname[1]/toponymName"/>
            </xsl:element>	
            <xsl:element name="wgs84:lat">
                <xsl:value-of select="$geoNode/geonames/geoname[1]/lat"/>
            </xsl:element>	
            <xsl:element name="wgs84:long">
                <xsl:value-of select="$geoNode/geonames/geoname[1]/lng"/>
            </xsl:element>	-->	
        </xsl:element>	
    </xsl:template>


	  <!-- Places -->
   <!--	<xsl:template name="createPlace">
		<xsl:param name="datensatz" as="node()"/>
	
		<xsl:if test="not(ddb:feld[@nr='410'] = 'o.O.') and not(ddb:feld[@nr='410'] = 'o. O.')">
		
			<xsl:variable name="feld410" select="ddb:feld[@nr='410']"/>

			<xsl:for-each select="tokenize($feld410, ';')">
				<xsl:variable name="geoNamesURL" select="concat('http://api.geonames.org/search?q=', encode-for-uri(.) ,'&amp;maxRows=1&amp;username=sbb_test')"/> 
				
				<xsl:variable name="geoNode" select="document($geoNamesURL)" />
		
				<xsl:message>
					<xsl:text> - </xsl:text>
					<xsl:value-of select="$geoNode/geonames/geoname[1]/toponymName"/>
				</xsl:message>		
		
				<xsl:element name="edm:Place">
					<xsl:attribute name="rdf:about">
						<xsl:value-of select="concat('http://sws.geonames.org/', $geoNode/geonames/geoname[1]/geonameId)"/>
					</xsl:attribute>
					<xsl:element name="skos:prefLabel">
						<xsl:value-of select="."/>
					</xsl:element>
					<xsl:element name="skos:altLabel">
						<xsl:value-of select="$geoNode/geonames/geoname[1]/toponymName"/>
					</xsl:element>	
					<xsl:element name="wgs84:lat">
						<xsl:value-of select="$geoNode/geonames/geoname[1]/lat"/>
					</xsl:element>	
					<xsl:element name="wgs84:long">
						<xsl:value-of select="$geoNode/geonames/geoname[1]/lng"/>
					</xsl:element>		
				</xsl:element>	
			</xsl:for-each>
				
		</xsl:if>

	</xsl:template>-->
	
 
	


    <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 name="writeConcepts">
        
        <xsl:variable name="allConcepts"
                    select="(             //tei:classCode,             //tei:hi[@rendition eq '#g']             )"/>
        
        <xsl:for-each select="distinct-values($allConcepts)">
            <xsl:if test="tei2edm:probablyASubject(.)">
                <xsl:call-template name="writeConcept">
                    <xsl:with-param name="concept" select="."/>
                </xsl:call-template>    
            </xsl:if>
        </xsl:for-each>
  
    </xsl:template>
    
    <xsl:function xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 name="tei2edm:probablyASubject"
                 as="xs:boolean">
        <xsl:param name="concept" as="xs:string"/>
        <xsl:value-of select="(count(tokenize($concept, ' ') ) &lt; 4) and              (substring($concept,1,1) eq upper-case(substring($concept,1,1) ) )"/>
    </xsl:function>
    
    <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 name="writeConceptsBook">
        <xsl:variable name="allConcepts" select="//tei:classCode"/>
        
        <xsl:for-each select="$allConcepts">            
            <xsl:call-template name="writeConcept">
                <xsl:with-param name="concept" select="."/>
            </xsl:call-template>
        </xsl:for-each>
    </xsl:template>
    
    <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 name="writeConcept">
        <xsl:param name="concept" as="xs:string"/>
        
        <xsl:element name="skos:Concept">
            <xsl:attribute name="rdf:about">
                <xsl:value-of select="tei2edm:concatConceptURI($concept)"/>
            </xsl:attribute>
            <xsl:element name="skos:prefLabel">
                <xsl:attribute name="xml:lang">
                    <xsl:value-of select="$DEF_LANGUAGE"/>
                </xsl:attribute>
                <xsl:value-of select="normalize-space($concept)"/>
            </xsl:element>
        </xsl:element> 
    </xsl:template>
  
 
	
  
  
	  <!-- Custom functions -->
	  <xsl:function xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 name="tei2edm:replaceApplicationChars">
		    <xsl:param name="aString"/>
		    <xsl:value-of select="replace( replace($aString, '¬\[', '('), '\]¬', ')' ) "/>
	  </xsl:function>
	  
	  <xsl:function xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 name="tei2edm:replaceNoneChar">
		    <xsl:param name="aString"/>
		    <xsl:value-of select="replace($aString, '¬', '')"/>
	  </xsl:function>
	  
	  <xsl:function xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 name="tei2edm:isPND">
		    <xsl:param name="aString"/>
		    <xsl:value-of select="matches($aString, '[0-9]{9}')"/>
	  </xsl:function>
  
	  <xsl:function xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 name="tei2edm:substituteDescription">
		    <xsl:param name="theNode" as="node()"/>
		    <xsl:param name="theTEIHeader"/>
		
		    <xsl:choose>
			      <xsl:when test="local-name($theNode) eq 'pb'">
				        <xsl:choose>
					          <xsl:when test="$theNode/@n">
						            <xsl:value-of select="concat($THE_DETAIL, ' ', $theNode/@n , ' from ', $theTEIHeader/tei:fileDesc/tei:titleStmt/tei:title[1])"/>
					          </xsl:when>
					          <xsl:otherwise>
						            <xsl:value-of select="concat('A ', $THE_DETAIL, ' from ', $theTEIHeader/tei:fileDesc/tei:titleStmt/tei:title[1])"/>
					          </xsl:otherwise>
				        </xsl:choose>
			      </xsl:when>
			      <xsl:when test="local-name($theNode) eq 'ab'">
				        <xsl:choose>
					          <xsl:when test="$theNode/@xml:id">
						            <xsl:value-of select="concat($THE_DETAIL, ' ', $theNode/@xml:id , ' from ', $theTEIHeader/tei:fileDesc/tei:titleStmt/tei:title[1])"/>
					          </xsl:when>
					          <xsl:otherwise>
						            <xsl:value-of select="concat('A ', $THE_DETAIL, ' from ', $theTEIHeader/tei:fileDesc/tei:titleStmt/tei:title[1])"/>
					          </xsl:otherwise>
				        </xsl:choose>
			      </xsl:when>
		    </xsl:choose> 
	  </xsl:function>
	
	  <xsl:function xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 name="tei2edm:withoutLeadingZero">
		    <xsl:param name="aString"/>
		    <xsl:param name="aReplaceable"/>

		    <xsl:variable name="aStringRepl" select="replace($aString, $aReplaceable, '')"/>
		
		    <xsl:choose>
			      <xsl:when test="$aStringRepl castable as xs:integer">
				        <xsl:value-of select="number($aStringRepl)"/>
			      </xsl:when>
			      <xsl:otherwise>
				        <xsl:value-of select="0"/>
			      </xsl:otherwise>	
		    </xsl:choose>
	  </xsl:function>
	
	  <!-- <xsl:function name="tei2edm:starts-with-Zero">
		<xsl:param name="aString">
		
		<xsl:value-of select="starts-with($aString, '0')"/>
	</xsl:function> -->
	
	  <xsl:function xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 name="tei2edm:concatAggregationURI">
	     <xsl:param name="theIDs" as="xs:string*"/>
	    <xsl:variable name="theIDsencoded"
                    select="for $i in $theIDs return encode-for-uri(normalize-space($i))"/>
	    <xsl:value-of select="concat($EDM_ID_ROOT, 'aggregation/', $PROV_REP, '/', string-join($theIDsencoded, '/') )"/>
	  </xsl:function>
    
    <xsl:function xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 name="tei2edm:concatItemURI">
        <xsl:param name="theIDs" as="xs:string*"/>
        <xsl:variable name="theIDsencoded"
                    select="for $i in $theIDs return encode-for-uri(normalize-space($i))"/>
        <xsl:value-of select="concat($EDM_ID_ROOT, 'item/', $PROV_REP, '/', string-join($theIDsencoded, '/') )"/>
    </xsl:function>
    
    <xsl:function xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 name="tei2edm:concatAgentURI">
        <xsl:param name="prefLabel" as="xs:string"/>
        <xsl:value-of select="concat($EDM_ID_ROOT, 'agent/', $PROV_REP, '/', encode-for-uri(normalize-space($prefLabel) ) )"/>
    </xsl:function>
    
    <xsl:function xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 name="tei2edm:formatPersName">
        <xsl:param name="persName"/>
        
        <xsl:choose>
            <xsl:when test="$persName instance of node()">
                <xsl:value-of select="concat(normalize-space($persName/tei:forename), ' ', normalize-space($persName/tei:surname) )"/>        
            </xsl:when>
            <xsl:when test="$persName instance of xs:string">
                <xsl:value-of select="$persName"/>        
            </xsl:when>
        </xsl:choose>
        
    </xsl:function>
    
    <xsl:function xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 name="tei2edm:concatPlaceURI">
        <xsl:param name="prefLabel" as="xs:string"/>
        <xsl:value-of select="concat($EDM_ID_ROOT, 'place/', $PROV_REP, '/', encode-for-uri(normalize-space($prefLabel) ) )"/>
    </xsl:function>
    
    <xsl:function xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 name="tei2edm:concatConceptURI">
        <xsl:param name="prefLabel" as="xs:string"/>
        <xsl:value-of select="concat($EDM_ID_ROOT, 'concept/', $PROV_REP, '/', encode-for-uri(normalize-space($prefLabel) ) )"/>
    </xsl:function>
    
 
    
    <!-- provider specific imports -->

   
    
    
    <xsl:template name="writeDctermsTableOfContents">
        <xsl:message>TEMPLATE writeDctermsTableOfContents not implemented!</xsl:message>
    </xsl:template>
    
    <xsl:template name="writeDctermsHasParts">
        <xsl:message>TEMPLATE writeHasParts not implemented!</xsl:message>
    </xsl:template>
    
    <xsl:template name="writeSubjectsOnPage">
        <xsl:param name="nodesOnPage" as="node()*"/>
        <xsl:message>
            <xsl:text>TEMPLATE writeSubjectsOnPage not implemented!</xsl:text>
        </xsl:message>
    </xsl:template>
    
    <xsl:template name="writeTitlesOnPage">
        <xsl:param name="nodesOnPage" as="node()*"/>
        <xsl:message>
            <xsl:text>TEMPLATE writeSubjectsOnPage not implemented!</xsl:text>
        </xsl:message>
    </xsl:template>

    <xsl:template name="writeSubjects">
        <!-- dc:subject -->
        <xsl:for-each select="$theTEIHeader/descendant::tei:textClass/tei:keywords/tei:term">
            <xsl:element name="dc:subject">
                <xsl:value-of select="normalize-space(.)"/>			
            </xsl:element>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="writePublisher">
        <xsl:message>
            <xsl:text>TEMPLATE writePublisher not implemented!</xsl:text>
        </xsl:message>
    </xsl:template>
    
    <xsl:template name="writeCurrentLocationAgent">
        <xsl:message>
            <xsl:text>TEMPLATE writeCurrentLocationAgent not implemented!</xsl:text>
        </xsl:message>
    </xsl:template>

    
    
    
    <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 xmlns:bibo="http://purl.org/ontology/bibo"
                 name="findDatesInAB">
        <xsl:param name="theAB" as="node()"/>
        
        <xsl:variable name="dateStr"
                    select="replace(tokenize($theAB/@ana, '_')[3], 'date:','')"/>
        <xsl:choose>
            <xsl:when test="matches($dateStr, '^[0-9]{8}$')">
                
                <xsl:call-template name="writeIssued">
                    <xsl:with-param name="dateStr"
                               select="                         concat(                         substring($dateStr, 1, 4) , '-' ,                         substring($dateStr, 5, 2) , '-' ,                         substring($dateStr, 7, 2)                          )                         "/>
                </xsl:call-template>
                
            </xsl:when>
            <xsl:when test="matches($dateStr, '^([0-9]{8})\-([0-9]{8})$')">
                
                <xsl:call-template name="writeIssued">
                    <xsl:with-param name="dateStr"
                               select="                         concat(                         substring($dateStr, 1, 4) , '-' ,                         substring($dateStr, 5, 2) , '-' ,                         substring($dateStr, 7, 2)                          )                         "/>
                </xsl:call-template>
                <xsl:call-template name="writeIssued">
                    <xsl:with-param name="dateStr"
                               select="                         concat(                         substring($dateStr, 10, 4) , '-' ,                         substring($dateStr, 14, 2) , '-' ,                         substring($dateStr, 16, 2)                          )                         "/>
                </xsl:call-template>
            </xsl:when>
        </xsl:choose>
        
    </xsl:template>
    
    <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 xmlns:bibo="http://purl.org/ontology/bibo"
                 name="writeEdmRelatedTo">
        <xsl:param name="theDetail" as="node()"/>
        <xsl:for-each select="distinct-values($theDetail/descendant::tei:persName/@key)">
            <xsl:element name="edm:isRelatedTo">
                <xsl:call-template name="writeAgentRdfResourceOrLiteral">
                    <xsl:with-param name="agentsName" select="."/>
                </xsl:call-template>
            </xsl:element>
        </xsl:for-each>
    </xsl:template>
    
    <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 xmlns:bibo="http://purl.org/ontology/bibo"
                 name="writeDm2eMentioned">
        <xsl:param name="theDetail" as="node()"/>
        <xsl:for-each select="distinct-values($theDetail/descendant::tei:persName/@key)">
            <xsl:element name="dm2e:mentioned">
                <xsl:attribute name="rdf:resource">
                    <xsl:value-of select="tei2edm:concatAgentURI(.)"/>
                </xsl:attribute>
            </xsl:element>
        </xsl:for-each>
    </xsl:template>
    
    <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 xmlns:bibo="http://purl.org/ontology/bibo"
                 name="writeDcSubjectsOnParagraph">
        <xsl:param name="theDetail" as="node()"/>
        
        <xsl:element name="dc:subject">
            <xsl:value-of select="replace(tokenize($theDetail/@ana, '_')[1], '(subject|field):', '','i')"/>			
        </xsl:element> 
    </xsl:template>    
    

    
    
    <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 name="writeAggregationResources">
        
        <!-- edm:hasView -->
        <xsl:for-each select="tei:fileDesc/tei:publicationStmt/tei:idno[1][@type='URL']/following-sibling::tei:idno[@type='URL']">
            <xsl:element name="edm:hasView">
                <xsl:attribute name="rdf:resource">
                    <xsl:value-of select="."/>
                </xsl:attribute>
            </xsl:element>
        </xsl:for-each>
        
        <!-- edm:isShownAt -->			
        
        <!-- edm:isShownBy -->
        <!-- The first field is expected as mandatory edm:ShownBy -->
        <xsl:choose>
            <xsl:when test="tei:fileDesc/tei:publicationStmt/tei:idno[1][@type='URL']">
                <xsl:element name="edm:isShownBy">
                    <xsl:attribute name="rdf:resource">
                        <xsl:value-of select="tei:fileDesc/tei:publicationStmt/tei:idno[1][@type='URL']"/>
                    </xsl:attribute>
                </xsl:element>	
            </xsl:when>
            <xsl:otherwise>
                <xsl:element name="edm:isShownBy">
                    <xsl:attribute name="rdf:resource">
                        <xsl:value-of select="concat($DEF_URL, tei:feld)"/>
                    </xsl:attribute>
                </xsl:element>
            </xsl:otherwise>
        </xsl:choose>
        
        
    </xsl:template>
    
    <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 name="writeAggregationPBResources">
        <xsl:param name="thePB" as="node()"/>
        <!-- edm:hasView -->
        <xsl:element name="edm:hasView">
            <xsl:attribute name="rdf:resource">
                <xsl:value-of select="tei2edm:searchForURL($thePB, //tei:surface)"/>
            </xsl:attribute>
        </xsl:element>
        
        <!-- edm:isShownAt -->	
        <xsl:element name="edm:isShownAt">
            <xsl:attribute name="rdf:resource">
                <xsl:value-of select="tei2edm:searchForURL($thePB, //tei:surface)"/>
            </xsl:attribute>
        </xsl:element>
    </xsl:template>
    
    <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 name="writeAggregationABResources">
        <xsl:param name="theAB" as="node()"/>
        
        <!-- Resolving isShownBy, isShownAt, hasView usw. -->
        <xsl:variable name="thePBs" select="$theAB/descendant::tei:pb"/>
        <xsl:choose>
            
            <!-- Ein Absatz überspannt mehrere Seiten(-umbrüche) -->
            <xsl:when test="count($thePBs) gt 0">
                
                <!-- Images -->
                <!-- Der Seitenumbruch vor dem Absatz 
					- der Absatz beginnt auf dieser Seite-->
                <xsl:if test="$theAB/preceding::tei:pb[1]">
                    <xsl:element name="edm:hasView">						
                        <xsl:attribute name="rdf:resource">
                            <xsl:value-of select="tei2edm:searchForURL($theAB/preceding::tei:pb[1], //tei:surface)"/>
                        </xsl:attribute>
                    </xsl:element>
                </xsl:if>
                
                <!-- Alle weiteren Absätze -->
                <xsl:for-each select="$thePBs">
                    <xsl:element name="edm:hasView">						
                        <xsl:attribute name="rdf:resource">
                            <xsl:value-of select="tei2edm:searchForURL(., //tei:surface)"/>
                        </xsl:attribute>
                    </xsl:element>
                </xsl:for-each>
                <!-- Images ENDE -->
                
                <!-- Textfassungen
					Die gesamte Bemerkung wird als normalisiert angezeigt 
					-->
                <xsl:element name="edm:hasView">
                    <xsl:attribute name="rdf:resource">
                        <xsl:value-of select="concat($DEF_URL, encode-for-uri($theAB/@xml:id), '_n')"/>
                    </xsl:attribute>
                </xsl:element>
                <!-- Single Remark in diplomatic Version -->
                <xsl:element name="edm:hasView">
                    <xsl:attribute name="rdf:resource">
                        <xsl:value-of select="concat($DEF_URL, encode-for-uri($theAB/@xml:id), '_d')"/>
                    </xsl:attribute>
                </xsl:element>
                
                <!-- Die Facsimilefassungen -->
                <!-- Ms-114,34r[3]et34v[1] -->
                <!-- No view implemented @wittgensteinsource for multiple pages, removed choose statement 
                     I think there is no real loss-->
                <!-- Single Remark in facsimile Version -->
                
                <!--Fetches edm:hasView from @facs with corresponding xml:id, for current pb from last pagebreak before AB element started-->
                <xsl:if test="exists($theAB/preceding::tei:pb[1]) and $theAB[*[1] &lt;&lt; child::pb[1]]">
                    <xsl:element name="edm:hasView">
                        <xsl:attribute name="rdf:resource">
                            <xsl:value-of select="id($theAB/preceding::tei:pb[1]/@facs)/tei:graphic/@url"/>
                        </xsl:attribute>
                    </xsl:element>
                </xsl:if>
 
            <xsl:variable name="pages"
                          select="tokenize(substring-after($theAB/@xml:id, ',') , 'et')"/>
              
              <xsl:for-each select="$pages">
                  <!--edm:hasView for wittgensteinsource facsimile views-->    
                  <xsl:element name="edm:hasView">
                      <xsl:attribute name="rdf:resource"
                                 select="concat($DEF_URL,encode-for-uri(concat(substring-before($theAB/@xml:id,','),',',substring-before(.,'[' ))),'_f') "/>
                  </xsl:element>
              </xsl:for-each>
              
                <xsl:for-each select="$pages">    
                <!--Defaults to first page-->
                    <xsl:if test="position()=1">
                  <xsl:element name="edm:isShownAt">
                    <xsl:attribute name="rdf:resource">
                        <xsl:value-of select="concat($DEF_URL,encode-for-uri(concat(substring-before($theAB/@xml:id,','),',',substring-before(.,'[' ))),'_f') "/>
                    </xsl:attribute>
                  </xsl:element>
                </xsl:if>

              </xsl:for-each>
              
                
                <!-- edm:isShownAt -->	
                <!--<xsl:element name="edm:isShownAt">
                    <xsl:attribute name="rdf:resource">
                        <xsl:value-of select="concat($DEF_URL,substring-before($theAB/@xml:id,','),',',substring-before($theAB/@xml:id, '['), '_f')"/>
                    
                    
                    </xsl:attribute>
                </xsl:element> -->
            </xsl:when>
        </xsl:choose>
        
        
        <!-- isShownBy -->
        <xsl:element name="edm:isShownBy">
            <xsl:attribute name="rdf:resource">
                <xsl:value-of select="concat($DEF_URL, encode-for-uri($theAB/@xml:id), '_n')"/>
            </xsl:attribute>
        </xsl:element>
        
    </xsl:template>
    
    <xsl:template xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 name="writeDcContributorsForAggregation">
        <xsl:variable name="allNames"
                    select="for $name in $theTEIHeader/tei:fileDesc/tei:titleStmt/tei:respStmt/tei:name             return tokenize($name, ', ')"/>
        
        <xsl:for-each select="distinct-values($allNames)">
            <xsl:call-template name="writeDcContributor">
                <xsl:with-param name="name" select="."/>
            </xsl:call-template>
        </xsl:for-each>	
    </xsl:template>
    
    
    <xsl:function xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 name="tei2edm:searchForURL">
        <xsl:param name="thePB" as="node()"/>
        <xsl:param name="allSurfaces"/> 
        
        <!--
		OIB: tei:facsimile/tei:surface[xml:id="Ms-152_FC"]/tei:graphic/@url
		-->
        <xsl:choose>
            <!-- Diese URL ist OIB spezifisch -->
            <xsl:when test="count($allSurfaces) gt 0">
                <xsl:value-of select="$allSurfaces[@xml:id eq $thePB/@facs]/tei:graphic/@url"/>
            </xsl:when>
        </xsl:choose>
    </xsl:function>
    

    
    
    <xsl:template name="writeAgentsBook">
        
        <xsl:variable name="allNames"
                    select="(              $theTEIHeader/descendant-or-self::tei:persName/text() ,              distinct-values(for $i in (//tei:name/tokenize(., ',')) return normalize-space($i)) ,             $theTEIHeader/descendant-or-self::tei:author/text()             )"
                    as="xs:string*"/>
        
        <xsl:for-each select="distinct-values($allNames)">
            <xsl:call-template name="createAgent">
                <xsl:with-param name="prefLabel" select="."/>
            </xsl:call-template> 
        </xsl:for-each>

        <xsl:call-template name="writeOrganizations"/>
        
    </xsl:template>
    
    <xsl:template name="writeOrganizations">
        
      
        <!-- UIB-TEI publisher organization-->
        <xsl:if test="exists($theTEIHeader/tei:fileDesc/tei:titleStmt/tei:editor/tei:orgName)">
            <xsl:variable name="prefLabel"
                       select="normalize-space($theTEIHeader/tei:fileDesc/tei:titleStmt/tei:editor/tei:orgName)"/>
            <xsl:choose>
                <xsl:when test="$TARGET_FORMAT eq 'EDM'">
                    <xsl:call-template name="createAgent">
                        <xsl:with-param name="prefLabel" select="$prefLabel"/>
                    </xsl:call-template>
                </xsl:when>
                <xsl:when test="$TARGET_FORMAT eq 'DM2E'">
                    <xsl:call-template name="writeAgentClass">
                        <xsl:with-param name="prefLabel" select="$prefLabel"/>
                        <xsl:with-param name="CLASSNAME" select="'foaf:Organization'"/>
                    </xsl:call-template>
                </xsl:when>
            </xsl:choose>
        </xsl:if>
        
    </xsl:template>
    
    <xsl:template name="createAgentsAB">
        
        <xsl:for-each select="distinct-values(//tei:persName/@key)">
            <xsl:call-template name="createAgent">
                <xsl:with-param name="prefLabel" select="."/>
            </xsl:call-template>
        </xsl:for-each>
        
    </xsl:template>
    
    <xsl:template name="createAgentsPB">
        <xsl:message>
            Function:createAgentsAB not implemented for UIB!
        </xsl:message>
    </xsl:template>

    
    
    <xsl:template xmlns:dct="http://purl.org/dc/terms/"
                 xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 name="writeWebResourceBookSpec">

        <xsl:variable name="manuscriptId"
                    select="$theTEIHeader/tei:fileDesc/tei:titleStmt/tei:title/@xml:id"/>
            
        <xsl:call-template name="writeWebResource">
            <xsl:with-param name="theAboutURL"
                         select="concat($DEF_URL, encode-for-uri($manuscriptId), '_d')"/>
        </xsl:call-template>
        <xsl:call-template name="writeWebResource">
            <xsl:with-param name="theAboutURL"
                         select="concat($DEF_URL, encode-for-uri($manuscriptId), '_f')"/>
        </xsl:call-template>
        
    </xsl:template>
    
    
    <xsl:template xmlns:dct="http://purl.org/dc/terms/"
                 xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 name="createWebResourcesPB">
        <xsl:param name="thePB" as="node()"/>
        
        <xsl:call-template name="writeWebResource">
            <xsl:with-param name="theAboutURL" select="tei2edm:searchForURL($thePB, //tei:surface)"/>
        </xsl:call-template>
    </xsl:template>
    
    <xsl:template xmlns:dct="http://purl.org/dc/terms/"
                 xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
                 xmlns:xalan="http://xml.apache.org/xalan"
                 name="createWebResourcesAB">
        <xsl:param name="theAB" as="node()"/>
        
        <xsl:variable name="thePBs" select="$theAB/descendant::tei:pb"/>
        
        <!-- Die JPEG-Faksimiles werden zugeordnet -->
        <xsl:choose>
            <!-- Ein Absatz überspannt mehrere Seiten(-umbrüche) -->
            <xsl:when test="count($thePBs) gt 0">
                
                <!-- Der Seitenumbruch vor dem Absatz 
				- der Absatz beginnt auf dieser Seite-->
                <xsl:if test="$theAB/preceding::tei:pb[1]">
                    <xsl:call-template name="writeWebResource">
                        <xsl:with-param name="theAboutURL"
                                  select="tei2edm:searchForURL($theAB/preceding::tei:pb[1], //tei:surface)"/>
                    </xsl:call-template>
                </xsl:if>
                
                <!-- Alle weiteren Absätze -->
                <xsl:for-each select="$thePBs">
                    <xsl:call-template name="writeWebResource">
                        <xsl:with-param name="theAboutURL" select="tei2edm:searchForURL(., //tei:surface)"/>
                    </xsl:call-template>
                </xsl:for-each>
            </xsl:when>
            <!-- Ein Absatz auf einer Seite - der Umbruch liegt davor -->
            <xsl:when test="count($thePBs) = 0">
                <xsl:call-template name="writeWebResource">
                    <xsl:with-param name="theAboutURL"
                               select="tei2edm:searchForURL($theAB/preceding::tei:pb[1], //tei:surface)"/>
                </xsl:call-template>
            </xsl:when>
        </xsl:choose>
        <!-- ENDE: Die JPEG-Faksimiles werden zugeordnet -->
        
        <!-- Die gesamte Bemerkung wird als normalisiert und diplomatisch angezeigt -->
        <!-- Single Remark in normalized version -->
        <xsl:call-template name="writeWebResource">
            <xsl:with-param name="theAboutURL"
                         select="concat($DEF_URL, encode-for-uri($theAB/@xml:id), '_n')"/>
        </xsl:call-template>
        <!-- Single Remark in diplomatic version -->
        <xsl:call-template name="writeWebResource">
            <xsl:with-param name="theAboutURL"
                         select="concat($DEF_URL, encode-for-uri($theAB/@xml:id), '_d')"/>
        </xsl:call-template>
        
        <!-- Ms-114,34r[3]et34v[1] There is no single page view for 'et' @ wittgensteinsource -->
        <xsl:variable name="manuscriptId" select="substring-before($theAB/@xml:id, ',')"/>
        <xsl:variable name="pages"
                    select="tokenize(substring-after($theAB/@xml:id, ',') , 'et')"/>
        
        <xsl:choose>
            <xsl:when test="count($pages) gt 1">
                <xsl:for-each select="$pages">
 <!--
     <xsl:call-template name="writeWebResource">
                        <xsl:with-param name="theAboutURL" select="concat($DEF_URL, encode-for-uri($manuscriptId), ',', 
                            encode-for-uri(.), '_n')"/>
                    </xsl:call-template>
                    <xsl:call-template name="writeWebResource">
                        <xsl:with-param name="theAboutURL" select="concat($DEF_URL, encode-for-uri($manuscriptId), ',', 
                            encode-for-uri(.), '_d')"/>
                    </xsl:call-template>-->
<!-- Obsolete choose statement the for each loop would generate same URI as on count=one page-->
                    <xsl:call-template name="writeWebResource">
                        <xsl:with-param name="theAboutURL"
                                  select="concat($DEF_URL, encode-for-uri($manuscriptId), ',', substring-before(.,'['), '_f')"/>
                    </xsl:call-template>
                </xsl:for-each>
            </xsl:when>
            <xsl:when test="count($pages) = 1">
                <xsl:call-template name="writeWebResource">
                    <xsl:with-param name="theAboutURL"
                               select="concat($DEF_URL, encode-for-uri($manuscriptId), ',', substring-before($pages[1],'['), '_f')"/>
                </xsl:call-template>
            </xsl:when>
        </xsl:choose>
    </xsl:template>
    

    
    
    <xsl:function name="tei2edm:searchForID">
        
        <xsl:choose>
            <!-- UIB - Wittgenstein Source -->
            <xsl:when test="$theTEIHeader/tei:fileDesc/tei:titleStmt/tei:title/@xml:id">
                <xsl:value-of select="$theTEIHeader/tei:fileDesc/tei:titleStmt/tei:title/@xml:id"/>
            </xsl:when>
            <!-- Fallback suggestion -->
            <xsl:otherwise>
                <xsl:value-of select="replace($theTEIHeader/descendant::tei:idno[@type='URL'][1], 'http://www.', '')"/>
            </xsl:otherwise>
        </xsl:choose>
    
    </xsl:function>
        

	  <!-- 
    <xsl:import href="./specs/BBAW_TEI2EDM_ProvidedCHO.xsl"/>   
    <xsl:import href="./specs/BBAW_TEI2EDM_ProvidedCHO_Paragraph.xsl"/>
    <xsl:import href="./specs/BBAW_TEI2EDM_Aggregation.xsl"/>
    <xsl:import href="./specs/BBAW_TEI2EDM_Agents.xsl"/>
    <xsl:import href="./specs/BBAW_TEI2EDM_WebResources.xsl"/>
    <xsl:import href="./specs/BBAW_TEI2EDM_Functions.xsl"/>
    -->
	  <!-- output -->
	  <xsl:output method="xml"
               encoding="utf-8"
               omit-xml-declaration="no"
               indent="yes"/>
	
	  <!-- PARAMETERs -->
	  <!-- Target-Format: EDM | DM2E -->
	
    <!-- 
        Rich output (comp. EDM docs) means linked data or usage or URIs instead of literals. 
        In contrast, EDM_RICH = 'false' writes literals as values. 
        - Default is 'false'!
        - For DM2E set EDM_RICH = 'true'.
    -->
    
	  <!-- Transformation Depth: PB = pagebreak | AB = paragraph | NONE -->
	
	  <!-- A short literal description of the transofrmation depth element, e.g.: page, remark, ... -->
	
	  <!--  Abbreviation of the data provider, e.g. sbb = Staatsbibliothek Berlin -->
	
	  <!-- Abbreviation of the data provider repository, e.g. kpe = Kalliope -->
	
	  <!-- Default Web Resource URL -->
	
	  <!-- Default DATAPROVIDER URL -->	
	
	  <!-- Default Coverage -->
	
	  <!-- Language -->
		
	  <!-- Language -->
		
	
	  <!-- DEFAULT VALUES -->
    <!-- Default rights - SBB: BY-NC-SA -->
	  <!-- Default rights - UiB: -->
    <xsl:variable name="RIGHTS_RESOURCE">http://creativecommons.org/licenses/by-nc/3.0/</xsl:variable>
    <!-- DM2E-ID-Root -->
	  <xsl:variable name="DM2E_ID_Root">http://data.dm2e.eu/data/</xsl:variable>
    <!-- DM2E-Schema / Namespace -->
    <xsl:variable name="DM2E_SCHEMA_NS">http://onto.dm2e.eu/schemas/dm2e/1.1/</xsl:variable>
	  <!-- Default ID for EDM -->
	  <xsl:variable name="EDM_ID_ROOT" select="$DM2E_ID_Root"/>
	  <!-- Dataprovider und Repostitory -->
	  <xsl:variable name="PROV_REP"
                 select="concat($DATAPROVIDER_ABB, '/', $REPOSITORY_ABB)"/>
    <!-- the TEI-Header als global variable, resused at several occasions -->
    <xsl:variable name="theTEIHeader" select="tei:TEI/tei:teiHeader"/>
    
	  <!-- Start -->
	  <xsl:template match="/">
  
		    <xsl:message>
			      <xsl:text>Processing </xsl:text>
			      <xsl:value-of select="base-uri()"/>
		    </xsl:message>
  
        <xsl:call-template name="writeProcessingInstruction"/>
		
	    <xsl:element name="rdf:RDF">	
	        <xsl:call-template name="writeNamespaces"/>

	        <xsl:apply-templates select="tei:TEI/tei:teiHeader"/>

	        <xsl:call-template name="transformItems"/>
			
			      <xsl:choose>
			         <xsl:when test="$EDM_RICH eq 'true'">
			            <xsl:call-template name="writeAllAgents"/>
			            <xsl:call-template name="writePlaces"/>
			            <xsl:call-template name="writeConcepts"/>
			         </xsl:when>
			         <xsl:otherwise>
			            <xsl:call-template name="writeCurrentLocationAgent"/>        
			         </xsl:otherwise>
			      </xsl:choose>
	        
		    </xsl:element>
	  </xsl:template>
  
	  <xsl:template match="tei:teiHeader">
		    <xsl:message>
            <xsl:text>- Transforming Header ... </xsl:text>
        </xsl:message>

        <!-- ProvidedCHO for the whole file | book | doc 
			based on information in the teiHeader	-->
		    <xsl:call-template name="createProvidedCHOBook"/>
			
		    <xsl:call-template name="createWebResourcesBook"/>
			
	    <xsl:call-template name="createAggregation">
	        <xsl:with-param name="theContextNode" select="."/>
	    </xsl:call-template>
			
	  </xsl:template>
	
    <!-- Transform items within the document -->
    <xsl:template name="transformItems">

        <xsl:choose>
            <xsl:when test="$TRANSFORMATION_DEPTH eq 'NONE'">
                <xsl:comment>Do explicitly nothing!</xsl:comment>
            </xsl:when>
            <xsl:when test="$TRANSFORMATION_DEPTH eq 'PB'">
                <xsl:message>
                    <xsl:text>- Transforming </xsl:text>
                    <xsl:value-of select="count(//tei:pb)"/> 
                    <xsl:text> pbs ... </xsl:text>
                </xsl:message>
                <xsl:apply-templates select="descendant-or-self::tei:pb"/>
            </xsl:when>
            <xsl:when test="$TRANSFORMATION_DEPTH eq 'AB'">
                <xsl:message>
                    <xsl:text>- Transforming </xsl:text>
                    <xsl:value-of select="count(//tei:ab)"/> 
                    <xsl:text> abs ... </xsl:text>
                </xsl:message>
                <xsl:apply-templates select="descendant-or-self::tei:ab"/>
            </xsl:when>
        </xsl:choose>
    </xsl:template>
	
	  <xsl:template match="tei:pb">
		
			<!-- ProvidedCHO-->
			   <xsl:call-template name="createProvidedCHOPB">
				     <xsl:with-param name="thePB" select="."/>
			   </xsl:call-template> 
			
			   <!-- WebResource -->
			   <xsl:call-template name="createWebResourcesPB">
				     <xsl:with-param name="thePB" select="."/>
			   </xsl:call-template> 
			
			   <!-- Aggregation -->
      <!--			<xsl:call-template name="createAggregationPB"> 
				<xsl:with-param name="thePB" select="."/>
			</xsl:call-template> -->
	    
    	    <xsl:call-template name="createAggregation">
    	        <xsl:with-param name="theContextNode" select="."/>
    	    </xsl:call-template>
		
	  </xsl:template>
	
	  <xsl:template match="tei:ab">
	
			<!-- ProvidedCHO for paragraphs -->
			   <xsl:call-template name="createProvidedCHOAB">
				     <xsl:with-param name="theAB" select="."/>
			   </xsl:call-template> 
	
			   <xsl:call-template name="createWebResourcesAB">
				     <xsl:with-param name="theAB" select="."/>
			   </xsl:call-template> 
	
      <!--			<xsl:call-template name="createAggregationAB"> 
				<xsl:with-param name="theAB" select="."/>
			</xsl:call-template> -->
	    
    	    <xsl:call-template name="createAggregation">
    	        <xsl:with-param name="theContextNode" select="."/>
    	    </xsl:call-template>
	    
	  </xsl:template>

  
</xsl:stylesheet>
