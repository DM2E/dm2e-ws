<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:dc="http://purl.org/dc/elements/1.1/"
	xmlns:dcterms="http://purl.org/dc/terms/" xmlns:edm="http://www.europeana.eu/schemas/edm/"
	xmlns:dm2e="http://onto.dm2e.eu/schemas/dm2e/1.0/"
	xmlns:ore="http://www.openarchives.org/ore/terms/" xmlns:owl="http://www.w3.org/2002/07/owl#"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
	xmlns:skos="http://www.w3.org/2004/02/skos/core#"
	xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:bibo="http://purl.org/ontology/bibo/"
	xmlns:pro="http://purl.org/spar/pro" xmlns:foaf="http://xmlns.com/foaf/0.1/"
	xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/"
	xmlns:mods="http://www.loc.gov/mods/v3" xmlns:mets="http://www.loc.gov/METS/"
	xmlns:epicur="urn:nbn:de:1111-2004033116" xmlns:marcxml="http://www.loc.gov/MARC21/slim"
	xmlns:oai="http://www.openarchives.org/OAI/2.0/" xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:korbo="http://purl.org/net7/korbo/vocab#" xmlns:mets2dm2e="http://www.ub.uni-frankfurt.de">

	<!-- METS to DM2E v2
	 17.6.2013
     Marko Knepper	m.knepper@ub.uni-frankfurt.de	-->

	<xsl:output method="xml" encoding="utf-8" omit-xml-declaration="no" indent="yes"/>
	<!-- Output level: pages/items -->
	<xsl:param name="level">pages</xsl:param>
	<!--  Abbreviation of the data provider -->
	<xsl:param name="dataprovider">ub-ffm</xsl:param>
	<!-- Abbreviation of the data provider repository -->
	<xsl:param name="repository">sammlungen</xsl:param>
	<!-- Default Language -->
	<xsl:param name="def_language">und</xsl:param>
	<!-- Default dc:type -->
	<xsl:param name="itemtype">http://data.dm2e.eu/schemas/dm2e/0.2/#Manuscript</xsl:param>
	<!-- Default edm:type -->
	<xsl:param name="def_type">text</xsl:param>
	<!-- Default rights - PD/CC0 -->
	<!-- Examples http://creativecommons.org/publicdomain/zero/1.0/ http://www.europeana.eu/rights/rr-f/ -->
	<xsl:variable name="def_rights">http://creativecommons.org/publicdomain/zero/1.0/</xsl:variable>
	<!-- DM2E-ID-Root -->
	<xsl:variable name="root">http://data.dm2e.eu/data/</xsl:variable>
	<!-- Provider -->
	<xsl:variable name="provider">DM2E</xsl:variable>
	<!-- Dataprovider -->
	<xsl:variable name="dprovider">Universitätsbibliothek JCS Frankfurt am Main</xsl:variable>
	<!-- Dataprovider und Repostitory -->
	<xsl:variable name="prov_rep" select="concat($dataprovider, '/', $repository)"/>

	<!-- Process file -->

	<xsl:template match="/">
		<xsl:message>
			<xsl:text>Processing </xsl:text>
			<xsl:value-of select="base-uri()"/>
		</xsl:message>

		<xsl:variable name="rdfoutput">

			<!-- Process records -->
			<xsl:for-each select="//mets:mets">
				<xsl:message>
					<xsl:text>Processing MODS parts </xsl:text>
					<xsl:value-of
						select="mets:dmdSec/mets:mdWrap/mets:xmlData/mods:mods[1]/mods:identifier[1]"
					/>
				</xsl:message>
				<xsl:for-each select="mets:dmdSec">
					<xsl:variable name="themodspart" select="mets:mdWrap/mets:xmlData/mods:mods[1]"/>
					<xsl:call-template name="modspart">
						<xsl:with-param name="themodspart" select="$themodspart"/>
					</xsl:call-template>
				</xsl:for-each>
				<xsl:message>
					<xsl:text>Processing logical structure</xsl:text>
				</xsl:message>
				<xsl:variable name="thelogicalpart" select="mets:structMap[@TYPE='LOGICAL']"/>
				<xsl:call-template name="logicalpart">
					<xsl:with-param name="thelogicalpart" select="$thelogicalpart"/>
					<xsl:with-param name="thefilespart" select="mets:fileSec"/>
				</xsl:call-template>
				<xsl:if
					test="exists(mets:structMap[@TYPE='PHYSICAL']/mets:div[@TYPE='physSequence']) and ($level='pages')">
					<xsl:variable name="thestructlink" select="mets:structLink"/>
					<xsl:variable name="thedmdid"
						select="subsequence(mets:structMap[@TYPE='LOGICAL']//mets:div[@ID=$thestructlink/mets:smLink/@xlink:from]/@DMDID,1,1)"/>
					<xsl:variable name="themodspart"
						select="mets:dmdSec[@ID=$thedmdid]/mets:mdWrap/mets:xmlData/mods:mods[1]"/>
					<xsl:message>
						<xsl:text>Processing physical structure of </xsl:text>
						<xsl:value-of select="$themodspart/mods:identifier[1]"/>
					</xsl:message>
					<xsl:call-template name="physpart">
						<xsl:with-param name="thephyspart"
							select="mets:structMap[@TYPE='PHYSICAL']/mets:div[@TYPE='physSequence']"/>
						<xsl:with-param name="thefilespart" select="mets:fileSec"/>
						<xsl:with-param name="themodspart" select="$themodspart"/>
					</xsl:call-template>
				</xsl:if>
			</xsl:for-each>

		</xsl:variable>

		<!-- Cleanup and output -->

		<xsl:message>
			<xsl:text>Cleaning up ...</xsl:text>
		</xsl:message>

		<xsl:element name="rdf:RDF">
			<xsl:namespace name="dc" select="'http://purl.org/dc/elements/1.1/'"/>
			<xsl:namespace name="dcterms" select="'http://purl.org/dc/terms/'"/>
			<xsl:namespace name="edm" select="'http://www.europeana.eu/schemas/edm/'"/>
			<xsl:namespace name="ore" select="'http://www.openarchives.org/ore/terms/'"/>
			<xsl:namespace name="owl" select="'http://www.w3.org/2002/07/owl#'"/>
			<xsl:namespace name="rdf" select="'http://www.w3.org/1999/02/22-rdf-syntax-ns#'"/>
			<xsl:namespace name="rdfs" select="'http://www.w3.org/2000/01/rdf-schema#'"/>
			<xsl:namespace name="skos" select="'http://www.w3.org/2004/02/skos/core#'"/>
			<xsl:namespace name="wgs84" select="'http://www.w3.org/2003/01/geo/wgs84_pos#'"/>
			<xsl:namespace name="dm2e" select="'http://onto.dm2e.eu/schemas/dm2e/1.0/'"/>
			<xsl:namespace name="bibo" select="'http://purl.org/ontology/bibo/'"/>
			<xsl:namespace name="foaf" select="'http://xmlns.com/foaf/0.1/'"/>
			<xsl:namespace name="pro" select="'http://purl.org/spar/pro'"/>
			<xsl:namespace name="korbo" select="'http://purl.org/net7/korbo/vocab#'"/>

			<xsl:for-each-group select="$rdfoutput/*" group-by="concat(name(),' ',@rdf:about)">
				<xsl:sort select="concat(substring-after(@rdf:about,'urn:nbn:'),@rdf:about,name())"/>
				<xsl:element name="{current-group()[1]/name()}">
					<xsl:attribute name="rdf:about">
						<xsl:value-of select="current-group()[1]/@rdf:about"/>
					</xsl:attribute>
					<xsl:for-each-group select="current-group()/*" group-by="concat(name(),' ' ,.)">
						<xsl:sort select="name()"/>
						<xsl:copy-of select="current-group()[1]"/>
					</xsl:for-each-group>
				</xsl:element>
			</xsl:for-each-group>

			<!-- used without cleanup
				<xsl:copy-of select="$rdfoutput"/>  -->

		</xsl:element>

	</xsl:template>

	<!-- Templates for pages and structures -->

	<xsl:template name="physpart">
		<xsl:param name="themodspart" as="node()"/>
		<xsl:param name="thephyspart" as="node()"/>
		<xsl:param name="thefilespart" as="node()"/>
		<xsl:apply-templates select="$thephyspart/mets:*">
			<xsl:with-param name="themodspart" select="$themodspart"/>
			<xsl:with-param name="thefilespart" select="$thefilespart"/>
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template name="logicalpart">
		<xsl:param name="thelogicalpart" as="node()"/>
		<xsl:param name="thefilespart" as="node()"/>
		<!-- page id handling not universal -->
		<xsl:for-each select="$thelogicalpart//mets:div/mets:fptr[contains(@FILEID,'FRONTIMAGE')]">
			<xsl:variable name="webfileid" select="@FILEID"/>
			<xsl:variable name="thedmdid" select="../@DMDID"/>
			<xsl:variable name="themodspart"
				select="$thelogicalpart/../mets:dmdSec[@ID=$thedmdid]/mets:mdWrap/mets:xmlData/mods:mods[1]"/>
			<xsl:if test="exists($themodspart/mods:identifier[@type='urn'])">
				<xsl:element name="ore:Aggregation">
					<xsl:attribute name="rdf:about">
						<xsl:value-of select="mets2dm2e:modsid($themodspart,'aggregation')"/>
					</xsl:attribute>
					<xsl:element name="edm:object">
						<xsl:attribute name="rdf:resource">
							<xsl:value-of
								select="$thefilespart//mets:file[@ID=$webfileid]/mets:FLocat[@LOCTYPE='URL']/@xlink:href"
							/>
						</xsl:attribute>
					</xsl:element>
				</xsl:element>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>

	<!-- Template for mods parts -->

	<xsl:template name="modspart">
		<xsl:param name="themodspart" as="node()"/>

		<xsl:element name="edm:ProvidedCHO">
			<xsl:attribute name="rdf:about">
				<xsl:value-of select="mets2dm2e:modsid($themodspart,'item')"/>
			</xsl:attribute>
			<xsl:element name="edm:type">
				<xsl:text>TEXT</xsl:text>
			</xsl:element>
			<xsl:element name="dc:type">
				<xsl:attribute name="rdf:resource">
					<xsl:value-of select="$itemtype"/>
				</xsl:attribute>
			</xsl:element>
		</xsl:element>
		<xsl:element name="ore:Aggregation">
			<xsl:attribute name="rdf:about">
				<xsl:value-of select="mets2dm2e:modsid($themodspart,'aggregation')"/>
			</xsl:attribute>
			<xsl:element name="edm:aggregatedCHO">
				<xsl:attribute name="rdf:resource">
					<xsl:value-of select="mets2dm2e:modsid($themodspart,'item')"/>
				</xsl:attribute>
			</xsl:element>
			<xsl:element name="edm:provider">
				<xsl:attribute name="rdf:resource">
					<xsl:value-of select="concat($root,'agent/',$prov_rep,'/',$provider)"/>
				</xsl:attribute>
			</xsl:element>
			<xsl:element name="edm:rights">
				<xsl:attribute name="rdf:resource">
					<xsl:value-of select="$def_rights"/>
				</xsl:attribute>
			</xsl:element>
			<xsl:element name="edm:dataProvider">
				<xsl:attribute name="rdf:resource">
					<xsl:value-of
						select="concat($root,'agent/',$prov_rep,'/',encode-for-uri($dprovider))"/>
				</xsl:attribute>
			</xsl:element>
		</xsl:element>
		<xsl:element name="foaf:Organisation">
			<xsl:attribute name="rdf:about">
				<xsl:value-of select="concat($root,'agent/',$prov_rep,'/',$provider)"/>
			</xsl:attribute>
			<xsl:element name="skos:prefLabel">
				<xsl:value-of select="$provider"/>
			</xsl:element>
		</xsl:element>
		<xsl:element name="foaf:Organisation">
			<xsl:attribute name="rdf:about">
				<xsl:value-of
					select="concat($root,'agent/',$prov_rep,'/',encode-for-uri($dprovider))"/>
			</xsl:attribute>
			<xsl:element name="skos:prefLabel">
				<xsl:value-of select="$dprovider"/>
			</xsl:element>
		</xsl:element>

		<xsl:apply-templates select="$themodspart/mods:*"/>
	</xsl:template>

	<!-- mets elements -->

	<xsl:template match="mets:div[@TYPE='page']">
		<xsl:param name="themodspart" as="node()"/>
		<xsl:param name="thefilespart" as="node()"/>
		<xsl:variable name="prevpage" select="@ORDER + 1"/>
		<!-- page id handling not universal -->
		<xsl:variable name="webfileid" select="mets:fptr[contains(@FILEID,'DEFAULT')]/@FILEID"/>
		<xsl:if test="exists(../mets:div[@ORDER=$prevpage])">
			<xsl:variable name="prevwebfileid"
				select="../mets:div[@ORDER=$prevpage]/mets:fptr[contains(@FILEID,'DEFAULT')]/@FILEID"/>
			<xsl:element name="edm:ProvidedCHO">
				<xsl:attribute name="rdf:about">
					<xsl:value-of select="concat(mets2dm2e:modsid($themodspart,'item'),'-',@ID)"/>
				</xsl:attribute>
				<xsl:element name="edm:isNextInSequence">
					<xsl:attribute name="rdf:resource">
						<xsl:value-of
							select="concat(mets2dm2e:modsid($themodspart,'item'),'-',../mets:div[@ORDER=$prevpage]/@ID)"
						/>
					</xsl:attribute>
				</xsl:element>
			</xsl:element>
			<xsl:element name="edm:WebResource">
				<xsl:attribute name="rdf:about">
					<xsl:value-of
						select="$thefilespart/mets:fileGrp[@USE='DEFAULT']/mets:file[@ID=$webfileid]/mets:FLocat[@LOCTYPE='URL']/@xlink:href"
					/>
				</xsl:attribute>
				<xsl:element name="edm:isNextInSequence">
					<xsl:attribute name="rdf:resource">
						<xsl:value-of
							select="$thefilespart/mets:fileGrp[@USE='DEFAULT']/mets:file[@ID=$prevwebfileid]/mets:FLocat[@LOCTYPE='URL']/@xlink:href"
						/>
					</xsl:attribute>
				</xsl:element>
			</xsl:element>
		</xsl:if>
		<xsl:element name="edm:ProvidedCHO">
			<xsl:attribute name="rdf:about">
				<xsl:value-of select="concat(mets2dm2e:modsid($themodspart,'item'),'-',@ID)"/>
			</xsl:attribute>
			<xsl:element name="dcterms:title">
				<xsl:value-of select="@LABEL"/>
			</xsl:element>
			<xsl:element name="dcterms:description">
				<xsl:value-of
					select="concat(@LABEL,' von ',$themodspart/mods:titleInfo[not(@type) and ((@script='Latn') or not(@script))]/mods:title)"
				/>
			</xsl:element>
			<xsl:element name="dm2e:isPartOfCHO">
				<xsl:attribute name="rdf:resource">
					<xsl:value-of select="mets2dm2e:modsid($themodspart,'item')"/>
				</xsl:attribute>
			</xsl:element>
			<xsl:element name="edm:type">
				<xsl:text>TEXT</xsl:text>
			</xsl:element>
			<!-- fabio:Page existing? -->
			<xsl:element name="dc:type">
				<xsl:attribute name="rdf:resource">
					<xsl:text>http://purl.org/spar/fabio/#Page</xsl:text>
				</xsl:attribute>
			</xsl:element>
			<xsl:for-each
				select="$themodspart/mods:language/mods:languageTerm[@authority='iso639-2b']">
				<xsl:element name="dc:language">
					<xsl:value-of select="."/>
				</xsl:element>
			</xsl:for-each>
		</xsl:element>
		<xsl:element name="ore:Aggregation">
			<xsl:attribute name="rdf:about">
				<xsl:value-of select="concat(mets2dm2e:modsid($themodspart,'aggregation'),'-',@ID)"
				/>
			</xsl:attribute>
			<xsl:element name="edm:aggregatedCHO">
				<xsl:attribute name="rdf:resource">
					<xsl:value-of select="concat(mets2dm2e:modsid($themodspart,'item'),'-',@ID)"/>
				</xsl:attribute>
			</xsl:element>
			<xsl:element name="edm:isShownBy">
				<xsl:attribute name="rdf:resource">
					<xsl:value-of
						select="$thefilespart//mets:file[@ID=$webfileid]/mets:FLocat[@LOCTYPE='URL']/@xlink:href"
					/>
				</xsl:attribute>
			</xsl:element>
			<xsl:element name="edm:object">
				<xsl:attribute name="rdf:resource">
					<xsl:value-of
						select="$thefilespart//mets:file[@ID=$webfileid]/mets:FLocat[@LOCTYPE='URL']/@xlink:href"
					/>
				</xsl:attribute>
			</xsl:element>
			<xsl:element name="korbo:hasAnnotableVersionAt">
				<xsl:attribute name="rdf:resource">
					<xsl:value-of
						select="$thefilespart//mets:file[@ID=$webfileid]/mets:FLocat[@LOCTYPE='URL']/@xlink:href"
					/>
				</xsl:attribute>
			</xsl:element>
			<xsl:element name="edm:provider">
				<xsl:attribute name="rdf:resource">
					<xsl:value-of select="concat($root,'agent/',$prov_rep,'/',$provider)"/>
				</xsl:attribute>
			</xsl:element>
			<xsl:element name="edm:dataProvider">
				<xsl:attribute name="rdf:resource">
					<xsl:value-of
						select="concat($root,'agent/',$prov_rep,'/',encode-for-uri($dprovider))"/>
				</xsl:attribute>
			</xsl:element>
			<xsl:element name="edm:rights">
				<xsl:attribute name="rdf:resource">
					<xsl:value-of select="$def_rights"/>
				</xsl:attribute>
			</xsl:element>
		</xsl:element>
		<xsl:element name="edm:WebResource">
			<xsl:attribute name="rdf:about">
				<xsl:value-of
					select="$thefilespart/mets:fileGrp[@USE='DEFAULT']/mets:file[@ID=$webfileid]/mets:FLocat[@LOCTYPE='URL']/@xlink:href"
				/>
			</xsl:attribute>
			<xsl:element name="dc:format">
				<xsl:text>JPG</xsl:text>
			</xsl:element>
			<xsl:element name="dm2e:isPartOfWebResource">
				<xsl:attribute name="rdf:resource">
					<xsl:value-of
						select="concat('http://nbn-resolving.de/',$themodspart/mods:identifier[@type='urn'])"
					/>
				</xsl:attribute>
			</xsl:element>
		</xsl:element>
	</xsl:template>

	<!-- mods elements -->

	<xsl:template match="mods:identifier[@type='urn']">
		<xsl:element name="edm:ProvidedCHO">
			<xsl:attribute name="rdf:about">
				<xsl:value-of select="mets2dm2e:modsid(..,'item')"/>
			</xsl:attribute>
			<xsl:element name="dc:identifier">
				<xsl:value-of select="."/>
			</xsl:element>
		</xsl:element>
		<xsl:element name="ore:Aggregation">
			<xsl:attribute name="rdf:about">
				<xsl:value-of select="mets2dm2e:modsid(..,'aggregation')"/>
			</xsl:attribute>
			<xsl:element name="edm:isShownAt">
				<xsl:attribute name="rdf:resource">
					<xsl:value-of select="concat('http://nbn-resolving.de/',.)"/>
				</xsl:attribute>
			</xsl:element>
		</xsl:element>
		<xsl:element name="edm:WebResource">
			<xsl:attribute name="rdf:about">
				<xsl:value-of select="concat('http://nbn-resolving.de/',.)"/>
			</xsl:attribute>
			<xsl:element name="dc:format">
				<xsl:text>HTML</xsl:text>
			</xsl:element>
		</xsl:element>
	</xsl:template>

	<xsl:template match="mods:titleInfo[not(@type)]">
		<xsl:element name="edm:ProvidedCHO">
			<xsl:attribute name="rdf:about">
				<xsl:value-of select="mets2dm2e:modsid(..,'item')"/>
			</xsl:attribute>
			<xsl:element name="dcterms:title">
				<xsl:value-of select="mods:title"/>
			</xsl:element>
		</xsl:element>
	</xsl:template>

	<xsl:template match="mods:titleInfo[@type]">
		<xsl:element name="edm:ProvidedCHO">
			<xsl:attribute name="rdf:about">
				<xsl:value-of select="mets2dm2e:modsid(..,'item')"/>
			</xsl:attribute>
			<xsl:element name="dcterms:alternative">
				<xsl:value-of select="mods:title"/>
			</xsl:element>
		</xsl:element>
	</xsl:template>

	<xsl:template match="mods:physicalDescription">
		<xsl:element name="edm:ProvidedCHO">
			<xsl:attribute name="rdf:about">
				<xsl:value-of select="mets2dm2e:modsid(..,'item')"/>
			</xsl:attribute>
			<xsl:element name="dc:format">
				<xsl:value-of select="mods:extent"/>
			</xsl:element>
		</xsl:element>
	</xsl:template>

	<xsl:template match="mods:originInfo[contains(lower-case(mods:edition),'electronic')]">
		<xsl:element name="edm:WebResource">
			<xsl:attribute name="rdf:about">
				<xsl:value-of
					select="concat('http://nbn-resolving.de/',../mods:identifier[@type='urn'])"/>
			</xsl:attribute>
			<xsl:element name="dcterms:created">
				<xsl:value-of select="mods:dateIssued"/>
			</xsl:element>
		</xsl:element>
	</xsl:template>

	<xsl:template match="mods:originInfo[not(contains(lower-case(mods:edition),'electronic'))]">
		<xsl:if test="mods:publisher/text()">
			<xsl:element name="edm:ProvidedCHO">
				<xsl:attribute name="rdf:about">
					<xsl:value-of select="mets2dm2e:modsid(..,'item')"/>
				</xsl:attribute>
				<xsl:element name="dc:publisher">
					<xsl:attribute name="rdf:resource">
						<xsl:value-of
							select="concat($root,'agent/',$prov_rep,'/',encode-for-uri(normalize-space(mods:publisher)))"
						/>
					</xsl:attribute>
				</xsl:element>
			</xsl:element>
			<xsl:element name="edm:agent">
				<xsl:attribute name="rdf:about">
					<xsl:value-of
						select="concat($root,'agent/',$prov_rep,'/',encode-for-uri(normalize-space(mods:publisher)))"
					/>
				</xsl:attribute>
				<xsl:element name="skos:prefLabel">
					<xsl:value-of select="mods:publisher"/>
				</xsl:element>
			</xsl:element>
		</xsl:if>
		<xsl:if
			test="mods:place/mods:placeTerm/text() and not(lower-case(mods:place/mods:placeTerm)='[s.l.]')">
			<xsl:call-template name="modsplace">
				<xsl:with-param name="thename" select="mods:place"/>
				<xsl:with-param name="classname">dm2e:publishedAt</xsl:with-param>
			</xsl:call-template>
			<xsl:element name="edm:ProvidedCHO">
				<xsl:attribute name="rdf:about">
					<xsl:value-of select="mets2dm2e:modsid(..,'item')"/>
				</xsl:attribute>
				<xsl:element name="dcterms:issued">
					<xsl:value-of select="mods:dateIssued"/>
				</xsl:element>
			</xsl:element>
		</xsl:if>
	</xsl:template>

	<xsl:template match="mods:language[exists(mods:languageTerm[@authority='iso639-2b'])]">
		<xsl:element name="edm:ProvidedCHO">
			<xsl:attribute name="rdf:about">
				<xsl:value-of select="mets2dm2e:modsid(..,'item')"/>
			</xsl:attribute>
			<xsl:element name="dc:language">
				<xsl:value-of select="mods:languageTerm[@authority='iso639-2b']"/>
			</xsl:element>
		</xsl:element>
	</xsl:template>

	<xsl:template
		match="mods:name[(mods:role/mods:roleTerm[@authority='marcrelator']='aut') and ((@script='Latn') or not(@script))]">
		<xsl:call-template name="modsagent">
			<xsl:with-param name="thename" select="."/>
			<xsl:with-param name="classname">pro:author</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<xsl:template
		match="mods:name[(mods:role/mods:roleTerm[@authority='marcrelator']='edt') and ((@script='Latn') or not(@script))]">
		<xsl:call-template name="modsagent">
			<xsl:with-param name="thename" select="."/>
			<xsl:with-param name="classname">bibo:editor</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<xsl:template
		match="mods:name[(mods:role/mods:roleTerm[@authority='marcrelator']='asn') and ((@script='Latn') or not(@script))]">
		<xsl:call-template name="modsagent">
			<xsl:with-param name="thename" select="."/>
			<xsl:with-param name="classname">dc:contributor</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<xsl:template
		match="mods:name[(mods:role/mods:roleTerm[@authority='marcrelator']='scr') and ((@script='Latn') or not(@script))]">
		<xsl:call-template name="modsagent">
			<xsl:with-param name="thename" select="."/>
			<xsl:with-param name="classname">dm2e:copyist</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="mods:subject">
		<xsl:choose>
			<xsl:when test="*/@authority='gnd'">
				<xsl:element name="edm:ProvidedCHO">
					<xsl:attribute name="rdf:about">
						<xsl:value-of select="mets2dm2e:modsid(..,'item')"/>
					</xsl:attribute>
					<xsl:element name="dc:subject">
						<xsl:attribute name="rdf:resource">
							<xsl:value-of select="*/@valueURI"/>
						</xsl:attribute>
					</xsl:element>
				</xsl:element>
			</xsl:when>
			<xsl:otherwise>
				<xsl:variable name="uri">
					<xsl:value-of
						select="concat($root,'place/',$prov_rep,'/',encode-for-uri(normalize-space(*)))"
					/>
				</xsl:variable>
				<xsl:element name="skos:Concept">
					<xsl:attribute name="rdf:about">
						<xsl:value-of select="$uri"/>
					</xsl:attribute>
					<xsl:element name="skos:prefLabel">
						<xsl:value-of select="*"/>
					</xsl:element>
				</xsl:element>
				<xsl:element name="edm:ProvidedCHO">
					<xsl:attribute name="rdf:about">
						<xsl:value-of select="mets2dm2e:modsid(..,'item')"/>
					</xsl:attribute>
					<xsl:element name="dc:subject">
						<xsl:attribute name="rdf:resource">
							<xsl:value-of select="$uri"/>
						</xsl:attribute>
					</xsl:element>
				</xsl:element>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="mods:relatedItem[(@type='host') and exists(mods:identifier[@type='urn'])]">
		<xsl:element name="edm:ProvidedCHO">
			<xsl:attribute name="rdf:about">
				<xsl:value-of select="mets2dm2e:modsid(..,'item')"/>
			</xsl:attribute>
			<xsl:element name="dm2e:isPartOfCHO">
				<xsl:attribute name="rdf:resource">
					<xsl:value-of select="mets2dm2e:modsid(.,'item')"/>
				</xsl:attribute>
			</xsl:element>
			<xsl:choose>
				<xsl:when test="../mods:part/mods:detail/@type='part'">
					<xsl:element name="dcterms:description">
						<xsl:value-of
							select="concat(mods:titleInfo[not(@type) and ((@script='Latn') or not(@script))]/mods:title,', Teil ',../mods:part/mods:detail/mods:number)"
						/>
					</xsl:element>
				</xsl:when>
				<xsl:otherwise>
					<xsl:element name="dcterms:description">
						<xsl:value-of
							select="concat(mods:titleInfo[not(@type) and ((@script='Latn') or not(@script))]/mods:title,', Band ',../mods:part/mods:detail/mods:number)"
						/>
					</xsl:element>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:element>
	</xsl:template>

	<xsl:template match="*"/>

	<!-- procedures -->

	<xsl:template name="modsplace">
		<xsl:param name="thename" as="node()"/>
		<xsl:param name="classname"/>
		<xsl:variable name="uri">
			<xsl:value-of
				select="concat($root,'place/',$prov_rep,'/',encode-for-uri(normalize-space(concat($thename/mods:placeTerm,'_'))))"
			/>
		</xsl:variable>
		<xsl:element name="edm:Place">
			<xsl:attribute name="rdf:about">
				<xsl:value-of select="$uri"/>
			</xsl:attribute>
			<xsl:element name="skos:prefLabel">
				<xsl:value-of select="$thename/mods:placeTerm"/>
			</xsl:element>
		</xsl:element>
		<xsl:element name="edm:ProvidedCHO">
			<xsl:attribute name="rdf:about">
				<xsl:value-of select="mets2dm2e:modsid(..,'item')"/>
			</xsl:attribute>
			<xsl:element name="{$classname}">
				<xsl:value-of select="$uri"/>
			</xsl:element>
		</xsl:element>
	</xsl:template>

	<xsl:template name="modsagent">
		<xsl:param name="thename" as="node()"/>
		<xsl:param name="classname"/>
		<xsl:variable name="gndid">
			<xsl:if test="$thename/@authority='gnd'">
				<xsl:value-of select="substring-after($thename/@valueURI,'http://d-nb.info/gnd/')"/>
			</xsl:if>
		</xsl:variable>
		<xsl:variable name="uri">
			<xsl:value-of
				select="concat($root,'agent/',$prov_rep,'/',encode-for-uri(normalize-space(concat($thename/mods:namePart[not(@type)],'_',$gndid))))"
			/>
		</xsl:variable>
		<xsl:choose>
			<xsl:when test="$thename/@type='personal'">
				<xsl:element name="foaf:Person">
					<xsl:attribute name="rdf:about">
						<xsl:value-of select="$uri"/>
					</xsl:attribute>
					<xsl:element name="skos:prefLabel">
						<xsl:value-of select="$thename/mods:namePart[not(@type)]"/>
					</xsl:element>
					<xsl:if test="$thename/@authority='gnd'">
						<xsl:element name="owl:sameAs">
							<xsl:attribute name="rdf:resource">
								<xsl:value-of select="$thename/@valueURI"/>
							</xsl:attribute>
						</xsl:element>
					</xsl:if>
				</xsl:element>
			</xsl:when>
			<xsl:when test="$thename/@type='corporate'">
				<xsl:element name="foaf:Organisation">
					<xsl:attribute name="rdf:about">
						<xsl:value-of select="$uri"/>
					</xsl:attribute>
					<xsl:element name="skos:prefLabel">
						<xsl:value-of select="$thename/mods:namePart[not(@type)]"/>
					</xsl:element>
				</xsl:element>
			</xsl:when>
		</xsl:choose>
		<xsl:element name="edm:ProvidedCHO">
			<xsl:attribute name="rdf:about">
				<xsl:value-of select="mets2dm2e:modsid(..,'item')"/>
			</xsl:attribute>
			<xsl:element name="{$classname}">
				<xsl:attribute name="rdf:resource">
					<xsl:value-of select="$uri"/>
				</xsl:attribute>
			</xsl:element>
		</xsl:element>

	</xsl:template>

	<!-- functions -->

	<xsl:function name="mets2dm2e:modsid">
		<xsl:param name="themodspart" as="node()"/>
		<xsl:param name="thetype"/>
		<xsl:value-of
			select="concat($root,$thetype,'/',$prov_rep,'/',$themodspart/mods:identifier[@type='urn'])"
		/>
	</xsl:function>

</xsl:stylesheet>
