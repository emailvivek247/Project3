<?xml version="1.0" encoding="${encoding}"?>
${response.setContentType("application/rss+xml; charset=" + encoding)}
<#assign modifiedDateColumnName = searchResult.datasetConfiguration.modifiedDateColumn.columnName>
<rss version="2.0">
  <channel>
    <title><![CDATA[This is an example from Search Platform: ${q}]]></title>
    <link>${(WebserverStatic.getServerURL() + "search.do?indexName=" + indexName + "&templateName=" + templateName + "&q=" + URLEncodedQuery)?html}</link>
    <description><![CDATA[RSS feed from Search Platform]]></description>
<#foreach doc in searchResult.docs>
    <item>
      <title><![CDATA[:]]></title>
      <link>http://www.sample.com/id=</link>
      <description><![CDATA[]]></description>
      <#if modifiedDateColumnName?has_content>
       <pubDate>${doc.getDate(modifiedDateColumnName)?string("EEE, d MMM yyyy hh:mm:ss z")}</pubDate>
      </#if>
    </item>
</#foreach>
  </channel>
</rss>
