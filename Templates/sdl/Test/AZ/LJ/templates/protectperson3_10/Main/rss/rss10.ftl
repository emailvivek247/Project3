<?xml version="1.0" encoding="${encoding}"?>
<rdf:RDF
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  xmlns="http://purl.org/rss/1.0/"
  xmlns:dc="http://purl.org/dc/elements/1.1/"
>
  <channel rdf:about="${(WebserverStatic.getServerURL() + "search.do?indexName=" + indexName + "&templateName=" + templateName + "&q=" + URLEncodedQuery)?html}">
    <title><![CDATA[This is an example from Search Platform: ${q}]]></title>
    <link>${(WebserverStatic.getServerURL() + "search.do?indexName=" + indexName + "&templateName=" + templateName + "&q=" + URLEncodedQuery)?html}</link>
    <description><![CDATA[RSS feed from Search Platform]]></description>
    <items>
      <rdf:Seq>
<#foreach doc in searchResult.docs>
        <rdf:li rdf:resource="http://www.sample.com/id=${doc.get("PERSON_ALIAS_ID")}" />
</#foreach>
      </rdf:Seq>
    </items>
  </channel>
<#foreach doc in searchResult.docs>
  <item rdf:about="http://www.sample.com/id=${doc.get("PERSON_ALIAS_ID")}">
    <title><![CDATA[${doc.get("PERSON_ALIAS_ID")}:]]></title>
    <link>http://www.sample.com/id=${doc.get("PERSON_ALIAS_ID")}}</link>
    <description><![CDATA[]]></description>
  </item>
</#foreach></rdf:RDF>
