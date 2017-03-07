<#import "masterTemplate.ftl" as layout />
<@layout.masterTemplate title="${repoName} - Repository Overview">

<div class="row">
    <br />

    <center>
        <form method="GET" action="/">
            <div class="form-inline">
                <div class="form-group">
                    <input name="q" autocapitalize="off" autocorrect="off" autocomplete="off" spellcheck="true" size="50" placeholder="Search within ${repoName?html}" type="search" class="form-control" />
                    <input type="hidden" name="repo" value="${repoName?html}" />
                </div>
                <input type="submit" value="search" class="btn btn-primary">
            </div>
        </form>
    </center>

    <h4>Repository Overview for ${repoName}</h4>
    <p>${busBlurb}</p>
    <table class="table">
        <tbody>
          <tr>
            <td><span class="glyphicon glyphicon-globe" aria-hidden="true"></span> Repository</td>
            <td>
                ${repoLocation}
            </td>
            <td><span class="glyphicon glyphicon-file" aria-hidden="true"></span> Total Files</td>
            <td>${totalFiles}</td>
          </tr>
          <tr>
            <td><span class="glyphicon glyphicon-usd" aria-hidden="true"></span> Estimated Cost</td>
            <td>$${estimatedCost} <small><a href="/documentation/#estimatedcost">(why?)</a></small></td>
            <td><span class="glyphicon glyphicon-wrench" aria-hidden="true"></span> Code Lines</td>
            <td>${totalCodeLines}</td>
          </tr>
          <tr>
              <td><span class="glyphicon glyphicon-asterisk" aria-hidden="true"></span> ${totalLanguages} Languages</td>
              <td>
                  <#list languageFacet>
                  <#items as result>
                      ${result.languageName} <small style="color: #999;">${result.count} files ${(result.count / totalFiles * 100)?ceiling}% of project</small><br />
                  </#items>
                  </#list>
              </td>
              <td><span class="glyphicon glyphicon-user" aria-hidden="true"></span> ${totalOwners} Code Owners</td>
              <td>
                  <#list ownerFacet>
                  <#items as result>
                      ${result.owner} <small style="color: #999;">${result.count} files ${(result.count / totalFiles * 100)?ceiling}% of project</small><br />
                  </#items>
                  </#list>
              </td>
            </tr>
        </tbody>
    </table>


</div>



</@layout.masterTemplate>