<#import "masterTemplate.ftl" as layout />
<@layout.masterTemplate title="Search Results">


<div class="row search-count">



    <#if searchResult.totalHits == 0>
        <#if repoCount == 0>
        <b>You have no repositories indexed.</b>
        <h5>Add some using the <a href="/admin/">admin</a> page. Read the <a href="/documentation/#repositories">documentation</a> for more details.</h5>
        <#else>
        <h4>No results found for <i>${searchValue?html}</i></h4>
            <#if searchValue == altQuery>
            <h5>Try searching with fewer keywords or more general keywords.</h5>
            <#else>
            <h5>Try searching using for "<a href="/?q=${altQuery?url('ISO-8859-1')}">${altQuery?html}</a>" instead.</h5>
            </#if>
        </#if>
    <#else>
        <b>${searchResult.totalHits} results:</b> <span class="grey">"test"</span>
    </#if>

</div>


<div class="row">

  <div class="col-md-3 search-filters-container search-filters">

    <form <#if isHtml??>action="/html/"<#else>action="/"</#if>>
    <input name="q" value="${searchValue}" type="hidden">

    <div>
      <h5>Page 1 of 1</h5>

      <div class="center">
        <input type="submit" disabled="disabled" value="&#9664; Previous" class="btn btn-xs btn-success filter-button" /><span>&nbsp;</span><input type="submit" disabled="disabled" value="Next &#9654;" class="btn btn-xs btn-success filter-button" />
      </div>
    </div>

    <div>
      <h5>Filter Results</h5>
      <div class="center">
        <a href="?q=${searchValue}" class="btn btn-xs btn-success filter-button">Remove</a><span>&nbsp;</span><input type="submit" value="Apply" class="btn btn-xs btn-success filter-button" />
      </div>
    </div>

    <div>
      <#list searchResult.repoFacetResults>
      <h5>Repositories</h5>
      <#items as result>
      <div class="checkbox">
        <label><input type="checkbox" name="repo" value="${result.repoName}" <#if result.selected >checked</#if> /><span>${result.repoName[0..*12]}</span><span class="badge pull-right">${result.count}</span></label>
      </div>
      </#items>
      </#list>
    </div>

    <div>
      <#list searchResult.languageFacetResults>
      <h5>Languages</h5>
      <#items as result>
      <div class="checkbox">
        <label><input type="checkbox" name="lan" value="${result.languageName}" <#if result.selected >checked</#if> /><span>${result.languageName[0..*12]}</span><span class="badge pull-right">${result.count}</span></label>
      </div>
      </#items>
      </#list>
    </div>


    <div>
      <#list searchResult.repoOwnerResults>
      <h5>Owners</h5>
      <#items as result>
      <div class="checkbox">
        <label><input type="checkbox" name="own" value="${result.owner}" <#if result.selected >checked</#if>/><span>${result.owner}</span><span class="badge pull-right">${result.count}</span></label>
      </div>
      </#items>
      </#list>
    </div>

    </form>
  </div>

  <div class="col-md-9 search-results">

    <#list searchResult.codeResultList>
    <#items as result>
      <div>
        <h5><a href="/file/1/${result.repoName}/SOMETHINGSOMETHING">UpgradeReport.xslt in ${result.repoName}</a> <small>| ${result.repoLocation?html} | ${result.codeLines} lines | ${result.languageName?html}</small></h5>
      </div>
      <ol class="code-result">
          <#list result.matchingResults>
              <#items as line>
              <li value="${line.lineNumber?c}">
                  <a href="/file/${result.documentId?c}#${line.lineNumber?c}">
                      <pre>${line.line}</pre>
                  </a>
                  <#if line.addBreak ><hr class="codesplit"></#if>
              </li>
              </#items>
          </#list>
       </ol>
      <hr class="spacer" />
    </#items>
    </#list>

  </div>

  <div class="search-pagination">
    <ul class="pagination"><#list searchResult.pages>
      <#items as page>
        <li <#if page == searchResult.page>class="active"</#if>> <a href="?q=${searchValue}&p=${page}${reposQueryString}${langsQueryString}">${page + 1}</a></li>
        </#items>
      </#list>
    </ul>
  </div>

</div>

<!--
<div class="col-md-10 inside-container">

    <#if searchResult.totalHits == 0>
        <#if repoCount == 0>
        <h4>You have no repositories indexed.</h4>
        <h5>Add some using the <a href="/admin/">admin</a> page. Read the <a href="/documentation/#repositories">documentation</a> for more details.</h5>
        <#else>
        <h4>No results found for <i>${searchValue?html}</i></h4>
            <#if searchValue == altQuery>
            <h5>Try searching with fewer keywords or more general keywords.</h5>
            <#else>
            <h5>Try searching using for "<a href="/?q=${altQuery?url('ISO-8859-1')}">${altQuery?html}</a>" instead.</h5>
            </#if>
        </#if>
    <#else>
    <b>About ${searchResult.totalHits} results</b>
    </#if>

    <ol>
    <#list searchResult.codeResultList>
        <#items as result>
            <div class="code-result">
              <div>
                <h5><a href="/codesearch/view/${result.documentId?c}">${result.fileName?html} in ${result.repoName}</a> <small> | ${result.repoLocation?html} | ${result.codeLines} lines | ${result.languageName?html}</small></h5>
              </div>
              <ol class="code-result">
                  <#list result.matchingResults>
                      <#items as line>
                      <li value="${line.lineNumber?c}">
                          <a href="/codesearch/view/${result.documentId?c}#${line.lineNumber?c}">
                              <pre>${line.line}</pre>
                          </a>
                          <#if line.addBreak ><hr class="codesplit"></#if>
                      </li>
                      </#items>
                  </#list>
               </ol>
            </div>
            <hr class="spacer" />
        </#items>
    </#list>
    </ol>

    <center>
    <ul id="pages" class="pagination">

    <#list searchResult.pages>
        <#items as page>
            <li <#if page == searchResult.page>class="active"</#if>> <a href="?q=${searchValue}&p=${page}${reposQueryString}${langsQueryString}">${page + 1}</a></li>
        </#items>
    </#list>
    </center>
</div>


<div class="col-md-2 inside-container">
    <form <#if isHtml??>action="/html/"<#else>action="/"</#if>>
        <input name="q" value="${searchValue}" type="hidden">
        <div class="form-group">
            <button type="submit" class="btn btn-success" style="width:100%;">refine search</button>
        </div>

        <#list searchResult.repoFacetResults>
        <h5>Repo Filter</h5>
            <#items as result>
                <div class="checkbox">
                  <label>
                    <input name="repo" type="checkbox" <#if result.selected >checked</#if> value="${result.repoName}"> ${result.repoName[0..*12]} <span class="badge pull-right">${result.count}</span>
                  </label>
                </div>
            </#items>
        </#list>


        <#list searchResult.languageFacetResults>
        <h5>Language Filter</h5>
            <#items as result>
                <div class="checkbox">
                  <label>
                    <input name="lan" type="checkbox" <#if result.selected >checked</#if> value="${result.languageName}"> ${result.languageName[0..*12]} <span class="badge pull-right">${result.count}</span>
                  </label>
                </div>
            </#items>
        </#list>
    </form>
</div>
-->
</@layout.masterTemplate>