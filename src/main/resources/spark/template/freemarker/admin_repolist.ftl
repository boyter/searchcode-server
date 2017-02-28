<#import "masterTemplate.ftl" as layout />
<@layout.masterTemplate title="Repository List">

<div class="row inside-container">
    <div class="col-md-2">
        <nav>
          <ul class="nav nav-pills nav-stacked span2">
            <li><a href="/admin/">Dashboard</a></li>
            <li><a href="/admin/repo/">Repository Add</a></li>
            <li class="active"><a href="/admin/repolist/">Repository List</a></li>
            <li><a href="/admin/bulk/">Repository Bulk Add</a></li>
            <li><a href="/admin/api/">API Keys</a></li>
            <li><a href="/admin/settings/">Settings</a></li>
            <li><a href="/admin/logs/">Logs</a></li>
            <li><a href="/logout/">Logout</a></li>
          </ul>
        </nav>
    </div>
    <div class="col-md-10">
    <h3 style="border-bottom: 1px solid #eee; padding-bottom: 14px; margin-top:0px;">Repository List</h3>
    <p>You can use this page to find and remove repositories from the index. If you need to maintain a large amount of repositories it is advised to use the API.</p>
    <p>Please note that deleting a repository adds it to queue for deletion and as such may not be removed immediately.</p>

    <div class="center">

        <form method="GET">
            <#if hasPrevious == true>
                <a href="?offset=${previousOffset}" class="btn btn-xs btn-success filter-button" />&#9664; Previous</a>
            <#else>
                <input type="submit" value="&#9664; Previous" disabled="true" class="btn btn-xs btn-success filter-button" />
            </#if>

            <input type="text" <#if searchQuery?? >value="${searchQuery}"<#else>value=""</#if> name="q" placeholder="Filter Repositories" />
            <input class="btn btn-xs btn-primary" type="submit" value="Filter" />

            <#if hasNext == true>
                <a href="?offset=${nextOffset}" class="btn btn-xs btn-success filter-button" />Next &#9658;</a>
            <#else>
                <input type="submit" value="Next &#9658;" disabled="true" class="btn btn-xs btn-success filter-button" />
            </#if>
        </form>


    </div>
    <br><br>


    <#list repoResults>
        <#items as result>
            <div>
                <input type="text" value="${result.name?html}" name="reponame" readonly="true">
                <input type="text" value="${result.scm?html}" name="reposcm" readonly="true">
                <input type="text" value="${result.url?html}" name="repourl" readonly="true">
                <input type="text" value="${result.source?html}" name="reposource" readonly="true">
                <input type="text" value="${result.branch?html}" name="repobranch" readonly="true">
                <button class="btn btn-sm btn-danger delete" data-id="${result.name?html}" name="delete" type="submit"><span class="glyphicon glyphicon-remove" aria-hidden="true"></span> delete</button>
                <span ic-trigger-on="load" ic-poll="10s" ic-src="/admin/api/checkindexstatus/?reponame=${result.name?html}"></span>
            </div>
        </#items>
    </#list>
</div>
</div>

<script src="/js/jquery-1.11.1.min.js"></script>
<script src="/js/intercooler-1.0.3.min.js"></script>
<script>
$(document).ready(function(){
    $('button.delete').click(function(e) {
        e.preventDefault();
        var thus = $(this);

        var result = confirm("Delete this repository?");
        if (result === true) {
            $.ajax('/admin/delete/?repoName=' + encodeURIComponent(thus.data('id')))
               .done(function(data, textStatus, jqXHR) {
                    thus.parent().remove();
               }).fail(function(xhr, ajaxOptions, thrownError) {
                    alert('Sorry was unable to delete. Please reload the page and try again.');
              });
        }
    });
});
</script>


</@layout.masterTemplate>