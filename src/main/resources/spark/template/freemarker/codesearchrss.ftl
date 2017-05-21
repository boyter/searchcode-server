<?xml version="1.0" encoding="UTF-8" ?>
<rss version="2.0" xmlns:atom="http://www.w3.org/2005/Atom">
<channel>
  <title>Search for "${result.query}"</title>
  <link>${hostname}</link>
  <description>Search for "${result.query}"</description>
    <#list result.codeResultList>
    <#items as result>
    <item>
        <title>${result.fileName} in ${result.repoName}</title>
        <link>${hostname}/file/${result.codeId}/${result.codePath}</link>
        <guid>${hostname}/file/${result.codeId}/${result.codePath}</guid>
        <description>
          &lt;ol class="code-result"&gt;
              <#list result.matchingResults>
                  <#items as line>
                  &lt;li value="${line.lineNumber?c}"&gt;
                      &lt;a href="${hostname}/file/${result.codeId}/${result.codePath}#${line.lineNumber?c}"&gt;
                          &lt;pre&gt;${line.line?html}&lt;/pre&gt;
                      &lt;/a&gt;
                  &lt;/li&gt;
                  </#items>
              </#list>
           &lt;/ol&gt;
        </description>
    </item>
    </#items>
    </#list>
</channel>
</rss>