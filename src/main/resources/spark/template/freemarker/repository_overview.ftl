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
                <#if source == "" >
                    ${repoLocation}
                <#else>
                    <a href="${source}">${source}</a>
                </#if>
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
              <td colspan="2">
                <span class="glyphicon glyphicon-asterisk" aria-hidden="true"></span> ${totalLanguages} Languages<br /><br />

                  <table>
                      <tr>
                          <td valign="top" style="padding-right: 50px;">
                              <strong>By File Count</strong><br />
                              <#list languageFacet>
                              <#items as result>
                                  <a href="/?q=ln:${result.languageNameSearchable}&repo=${repoName?html}&lit=true">${result.languageName}</a> <small style="color: #999;">${result.count} files
                                  <#if totalFiles != 0>${(result.count / totalFiles * 100)?ceiling}% of project</#if></small><br />
                              </#items>
                              </#list>
                          </td>
                          <td valign="top">
                              <strong>By Line Count</strong><br />
                              <#list codeByLines>
                              <#items as result>
                                   <a href="/?q=ln:${result.languageNameSearchable}&repo=${repoName?html}&lit=true">${result.languageName}</a> <small style="color: #999;">${result.count} lines
                                  <#if totalCodeLines != 0>${(result.count / totalCodeLines * 100)?ceiling}% of project</#if></small><br />
                              </#items>
                              </#list>
                          </td>
                      </tr>
                  </table>
              </td>
              <td colspan="2">
              <span class="glyphicon glyphicon-user" aria-hidden="true"></span> ${totalOwners} Code Owners<br /><br />

              <strong>By File Count</strong><br />
                  <#list ownerFacet>
                  <#items as result>
                      <a href="/?q=on:${result.ownerSearchable}&repo=${repoName?html}&lit=true">${result.owner}</a> <small style="color: #999;">${result.count} files ${(result.count / totalFiles * 100)?ceiling}% of project</small><br />
                  </#items>
                  </#list>
              </td>
            </tr>
        </tbody>
    </table>


</div>



</@layout.masterTemplate>