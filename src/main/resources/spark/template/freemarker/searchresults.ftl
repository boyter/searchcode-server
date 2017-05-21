<#import "masterTemplate.ftl" as layout />
<@layout.masterTemplate title="Search Results">


<div class="row search-count">
    <b>${searchResult.totalHits} results:</b> <span class="grey">"${searchValue?html}"</span>
</div>


<div class="row">

  <div class="col-md-3 search-filters-container search-filters">

    <form <#if isHtml??>action="/html/"<#else>action="/"</#if>>
    <input name="q" value="${searchValue?html}" type="hidden">

    <div>
      <h5>Page ${searchResult.page + 1} of ${totalPages + 1}</h5>

      <div class="center">
        <a class="btn btn-xs btn-success filter-button" href="?q=${searchValue?html}&p=${searchResult.page - 1}${reposQueryString}${langsQueryString}${ownsQueryString}" <#if searchResult.page == 0 >disabled="disabled"</#if>>&#9664; Previous</a><span>&nbsp;</span><a class="btn btn-xs btn-success filter-button" href="?q=${searchValue?html}&p=${searchResult.page + 1}${reposQueryString}${langsQueryString}${ownsQueryString}" <#if searchResult.page == totalPages >disabled="disabled"</#if>>Next &#9654;</a>
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

    <div>
      <h5>Filter Results</h5>
      <div class="center">
        <a href="?q=${searchValue?html}" class="btn btn-xs btn-success filter-button">Remove</a><span>&nbsp;</span><input type="submit" value="Apply" class="btn btn-xs btn-success filter-button" />
      </div>
    </div>

    <div>
        <div>
            <div class="checkbox">
                <label><a href="/api/codesearch/rss/?q=${searchValue?html}">RSS Feed of Search</a></label>
            </div>
        </div>
    </div>

    </form>
  </div>

  <div class="col-md-9 search-results">

    <#if searchResult.totalHits == 0>
        <h4>No results found for <i class="grey">${searchValue?html}</i></h4>
        <#if searchValue == altQuery>
        <h5>Try clearing filters then searching with fewer keywords or more general keywords.</h5>
        <#else>
        <h5>Try searching using for "<a href="/?q=${altQuery?url('ISO-8859-1')}">${altQuery?html}</a>" instead.</h5>
        </#if>
    </#if>

    <#list searchResult.codeResultList>
    <#items as result>
      <div>
        <h5>
            <div>
                <a href="/file/${result.codeId}/${result.codePath}">${result.fileName}</a><span> in </span><a href="/repository/overview/${result.repoName}/">${result.repoName}</a> <small>| ${result.codePath?html} | ${result.codeLines} lines | ${result.languageName?html}</small>
            </div>
        </h5>
      </div>
      <ol class="code-result">
          <#list result.matchingResults>
              <#items as line>
              <li value="${line.lineNumber?c}">
                  <a href="/file/${result.codeId}/${result.codePath}#${line.lineNumber?c}">
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
        <li <#if page == searchResult.page>class="active"</#if>> <a href="?q=${searchValue?html}&p=${page}${reposQueryString}${langsQueryString}${ownsQueryString}">${page + 1}</a></li>
        </#items>
      </#list>
    </ul>
  </div>

</div>
</@layout.masterTemplate>