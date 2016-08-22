<#import "masterTemplate.ftl" as layout />
<@layout.masterTemplate title="${repoName} ${codePath}">

<div class="row">

<link rel="stylesheet" href="/css/highlight/default.css">
<link class="codestyle" rel="stylesheet" href="/css/highlight/${highligher}.css">
<#if highlight>
<script src="/js/highlight.pack.js"></script>
<script>hljs.initHighlightingOnLoad();</script>
</#if>


    <h4 class="codepath">${repoName} ${codePath}</h4>


    <table class="table">
        <tbody>
          <tr>
            <td><span class="glyphicon glyphicon-globe" aria-hidden="true"></span> Repository</td>
            <td>
            <#if source?? >
                <a href="${source}">${repoLocation}</a>
            <#else>
                ${repoLocation}
            </#if>
            </td>
            <td><span class="glyphicon glyphicon-user" aria-hidden="true"></span> Owner</td>
            <td>${codeOwner}</td>
            <!-- <button type="button" class="btn btn-success btn-xs"><span class="glyphicon glyphicon-indent-left" aria-hidden="true"></span> Blame</button> -->
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
        </tbody>
    </table>
</div>


<pre><code <#if !highlight>class="hljs"</#if> >${codeValue}</code></pre>

</@layout.masterTemplate>