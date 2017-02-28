<#import "masterTemplate.ftl" as layout />
<@layout.masterTemplate title="${repoName} ${codePath}">

<div class="row">

<link rel="stylesheet" href="/css/highlight/default.css">
<script src="/js/jquery-1.11.1.min.js"></script>

    <h4 class="codepath"><a href="/repository/overview/${repoName}/">${repoName}</a> ${codePath}</h4>

    <table class="table">
        <tbody>
          <tr>
            <td><span class="glyphicon glyphicon-globe" aria-hidden="true"></span> Repository</td>
            <td>
            <#if source?? && codeOwner != "File System">
                <a href="${source}">${repoLocation}</a>
            <#else>
                ${repoLocation}
            </#if>
            </td>
            <#if codeOwner == "File System">
                <td></td><td></td>
            <#else>
                <td>
                <span class="glyphicon glyphicon-user" aria-hidden="true"></span> Owner
                </td>
                <td>${codeOwner}</td>
            </#if>
          </tr>
          <tr>
            <td><span class="glyphicon glyphicon-asterisk" aria-hidden="true"></span> Language</td>
            <td>${languageName}</td>
            <td><span class="glyphicon glyphicon-wrench" aria-hidden="true"></span> Lines</td>
            <td>${codeLength}</td>
          </tr>
          <tr>
            <td><span class="glyphicon glyphicon-random" aria-hidden="true"></span> MD5 Hash</td>
            <td>${md5Hash}</td>
            <#if estimatedCost??>
            <td><span class="glyphicon glyphicon-usd" aria-hidden="true"></span> Estimated Cost</td>
            <td>$${estimatedCost} <small><a href="/documentation/#estimatedcost">(why?)</a></small></td>
            <#else>
            <td></td>
            <td></td>
            </#if>
          </tr>
          <#if owaspResults?size != 0>
          <tr>
            <td colspan="4">
                <a id="toggleOwasp" href="#"><span class="glyphicon glyphicon-list-alt" aria-hidden="true"></span> OWASP Advisories</a>
            </td>
          </tr>
          <tr>
            <td colspan="4">
                <div id="owaspResults">
                    <#list owaspResults>
                        <dl class="dl-horizontal">
                        <#items as result>
                              <dt>${result.name?html}</dt>
                              <dd>
                                ${result.desc}
                                <#list result.matchingLines>
                                <div style="margin-top:5px; margin-bottom:5px;">Line(s)
                                <#items as line>
                                    <a href="#${line?c}">${line}</a>
                                </#items>
                                </div>
                                </#list>
                              </dd>
                        </#items>
                        </dl>
                    </#list>
                </div>
            </td>
          </tr>
          </#if>
        </tbody>
    </table>
</div>


<div class="coderesult-code">
    <table style="width:100%;">
        <tr>
        <td class="coderesult-linenos" valign="top">
            <pre><code <#if !highlight>class="hljs"</#if> >${linenos}</code></pre>
        </td>
        <td class="coderesult-code" valign="top">
            <pre><code <#if !highlight>class="hljs"</#if> >${codeValue}</code></pre>
        </td>
        </tr>
    </table>
</div>


<script>
$('#toggleOwasp').click(function(e) {
  e.preventDefault();
  $('#owaspResults').toggle();
});
</script>

<#if highlight>
<link class="codestyle" rel="stylesheet" href="/css/highlight/${highligher}.css">
<script src="/js/highlight.pack.js"></script>
<script>hljs.initHighlightingOnLoad();</script>
</#if>

</@layout.masterTemplate>