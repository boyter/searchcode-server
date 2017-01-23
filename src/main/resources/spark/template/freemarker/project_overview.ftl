<#import "masterTemplate.ftl" as layout />
<@layout.masterTemplate title="${repoName} - Project Overview">

<div class="row">

    <h4>Project Overview for ${repoName}</h4>

    <table class="table">
        <tbody>
          <tr>
            <td><span class="glyphicon glyphicon-globe" aria-hidden="true"></span> Repository</td>
            <td>
                ${repoLocation}
            </td>

            <td><span class="glyphicon glyphicon-user" aria-hidden="true"></span> Total Code Owners</td>
            <td>
                ${totalOwners} owners<br />
                <#list ownerFacet>
                <#items as result>
                    ${result.owner}<br />
                </#items>
                </#list>
            </td>


          </tr>
          <tr>
            <td><span class="glyphicon glyphicon-asterisk" aria-hidden="true"></span> Languages</td>
            <td>
                ${totalLanguages} languages<br />
                <#list languageFacet>
                <#items as result>
                    ${result.languageName} ${result.count}<br />
                </#items>
                </#list>
            </td>
            <td><span class="glyphicon glyphicon-wrench" aria-hidden="true"></span> Total Code Lines</td>
            <td>${totalCodeLines}</td>
          </tr>
          <tr>
            <td><span class="glyphicon glyphicon-usd" aria-hidden="true"></span> Estimated Cost</td>
            <td>$${estimatedCost} <small><a href="/documentation/#estimatedcost">(why?)</a></small></td>

            <td><span class="glyphicon glyphicon-file" aria-hidden="true"></span> Total Files</td>
            <td>${totalFiles}</td>
            ${totalFiles}
          </tr>
        </tbody>
    </table>


</div>



</@layout.masterTemplate>