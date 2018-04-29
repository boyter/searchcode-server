<#import "masterTemplate.ftl" as layout />
<@layout.masterTemplate title="Repository List">
<#setting url_escaping_charset="UTF-8">

<div class="row inside-container">
    <div class="col-md-2">
        <nav>
          <ul class="nav nav-pills nav-stacked span2">
            <li><a href="/admin/">Dashboard</a></li>
            <li><a href="/admin/repo/">Repository Add</a></li>
            <li class="active"><a href="/admin/repolist/">Repository List <span class="badge">${repoCount}</span></a></li>
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
    <p>To view the status of the index process see the <a href="/repository/list/">repositories page</a>.</p>

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
    <table class="table table-condensed table-striped">
        <thead>
            <tr>
                <th>Name</th>
                <th>SCM</th>
                <th>URL</th>
                <th>Source URL</th>
                <th>Branch</th>
                <th>Source</th>
                <th></th>
                <th></th>
                <th></th>
                <th></th>
            </tr>
        </thead>
        <tbody>
            <#items as result>
            <tr>
                <td>${result.name?html}</td>
                <td>${result.scm?html}</td>
                <td><div class="truncate">${result.url?html}</div></td>
                <td><div class="truncate">${result.source?html}</div></td>
                <td>${result.branch?html}</td>
                <td>${result.data.source?html}</td>
                <td>
                    <button class="btn btn-xs btn-danger delete" data-id="${result.name?html}" name="delete" type="submit"><span class="glyphicon glyphicon-remove" aria-hidden="true"></span> Delete</button>
                </td>
                <td>
                    <button class="btn btn-xs btn-default reindex" data-id="${result.name?html}" name="reindex" type="submit"><span class="glyphicon glyphicon-refresh" aria-hidden="true"></span> Reindex</button>
                </td>
                <td>
                    <a class="btn btn-xs btn-default edit" data-id="${result.name?html}" name="edit" type="submit" href="/admin/repo/edit/${result.name?url}/"><span class="glyphicon glyphicon-edit" aria-hidden="true"></span> Edit</a>
                </td>
                <td><#if result.data.indexError != "" ><a class="btn btn-xs btn-default edit" name="edit" type="submit" href="/admin/repo/error/${result.name?url}/"><span style="color: #d9534f;" class="glyphicon glyphicon-warning-sign" aria-hidden="true"></span></#if></td>
            </tr>
            </#items>
        </tbody>
    </table>
    </#list>
</div>
</div>
<style>
.truncate {
  width: 150px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>

<script src="/js/jquery-1.11.1.min.js"></script>
<script src="/js/intercooler-1.1.2.min.js"></script>
<script>
$(document).ready(function(){
    $('button.delete').click(function(e) {
        e.preventDefault();
        var thus = $(this);

        var result = confirm("Delete this repository?");
        if (result === true) {
            $.ajax('/admin/delete/?repoName=' + encodeURIComponent(thus.data('id')))
               .done(function(data, textStatus, jqXHR) {
                    thus.parent().parent().remove();
               }).fail(function(xhr, ajaxOptions, thrownError) {
                    alert('Sorry was unable to delete. Please reload the page and try again.');
              });
        }
    });

    $('button.reindex').click(function(e) {
        e.preventDefault();
        var thus = $(this);

        $.ajax('/admin/reindex/?repoName=' + encodeURIComponent(thus.data('id')))
           .done(function(data, textStatus, jqXHR) {
                console.log('queued to reindex');
           }).fail(function(xhr, ajaxOptions, thrownError) {
                alert('Sorry was unable to reindex. Please reload the page and try again.');
           });
    });
});
</script>


</@layout.masterTemplate>