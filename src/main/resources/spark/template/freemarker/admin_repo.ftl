<#import "masterTemplate.ftl" as layout />
<@layout.masterTemplate title="Admin">

<div class="row inside-container">
    <div class="col-md-2">
        <nav>
          <ul class="nav nav-pills nav-stacked span2">
            <li><a href="/admin/">Dashboard</a></li>
            <li class="active"><a href="/admin/repo/">Repository Admin</a></li>
            <li><a href="/admin/bulk/">Repository Bulk Admin</a></li>
            <li><a href="/admin/api/">API Keys</a></li>
            <li><a href="/admin/settings/">Settings</a></li>
            <li><a href="/admin/logs/">Logs</a></li>
            <li><a href="/logout/">Logout</a></li>
          </ul>
        </nav>
    </div>
    <div class="col-md-10">
    <h3 style="border-bottom: 1px solid #eee; padding-bottom: 14px; margin-top:0px;"><span class="label label-default"><span class="glyphicon glyphicon-tasks" aria-hidden="true"></span></span> Repository Admin</h3>
    <p>You can use this page to add repositories to index or find and remove them from the index. If you need to maintain a large amount of repositories it is advised to use the API.</p>
    <p>Please note that deleting a repository adds it to queue for deletion and as such may not be removed immediately.</p>

    <h3 style="border-bottom: 1px solid #eee; padding-bottom: 14px; margin-top:0px;"><span class="label label-default"><span class="glyphicon glyphicon-tasks" aria-hidden="true"></span></span> Repository Add</h3>
    <form class="form-horizontal" method="POST">
      <div class="form-group">
        <label for="reponame" class="col-sm-2 control-label">Repository Name</label>
        <div class="col-sm-10">
          <input type="text" class="form-control" id="reponame" value="" name="reponame" placeholder="Repository Name" />
        </div>
      </div>
      <div class="form-group">
        <label for="reposcm" class="col-sm-2 control-label">SCM</label>
        <div class="col-sm-10">
          <select id="reposcm" name="reposcm" class="form-control">
              <option value="git">GIT</option>
              <option value="svn">SVN</option>
              <option value="file">File System</option>
          </select>
        </div>
      </div>
      <div class="form-group">
          <label for="repourl" class="col-sm-2 control-label">Repository Location</label>
          <div class="col-sm-10">
            <input type="text" class="form-control" value="" id="repourl" name="repourl" placeholder="Repository URL or File Path" />
          </div>
      </div>

      <div class="form-group">
        <label for="repousername" class="col-sm-2 control-label">Repository Username</label>
        <div class="col-sm-10">
          <input type="text" class="form-control" value="" id="repousername" name="repousername" placeholder="Repository username if required" />
        </div>
      </div>

      <div class="form-group">
          <label for="repopassword" class="col-sm-2 control-label">Repository Password</label>
          <div class="col-sm-10">
            <input type="password" class="form-control" value="" id="repopassword" name="repopassword" placeholder="Repository password if required" />
          </div>
      </div>

      <div class="form-group">
            <label for="reposource" class="col-sm-2 control-label">Repository Source</label>
            <div class="col-sm-10">
              <input type="text" class="form-control" value="" id="reposource" name="reposource" placeholder="URL to repository source location or documentation" />
            </div>
      </div>

      <div class="form-group">
          <label for="repobranch" class="col-sm-2 control-label">Repository Branch</label>
          <div class="col-sm-10">
            <input type="text" class="form-control" value="master" id="repobranch" name="repobranch" placeholder="For GIT repositories only what branch should be indexed" />
          </div>
      </div>

      <div class="form-group">
        <div class="col-sm-offset-2 col-sm-10">
          <input type="submit" class="btn btn-primary" name="Add Repo" value="Add Repository" />
        </div>
      </div>
    </form>


     <!--<form method="POST">
        <input type="text" value="" name="reponame" placeholder="Repo Name" />
        <select name="reposcm">
            <option value="git">GIT</option>
            <option value="svn">SVN</option>
            <option value="file">File</option>
        </select>
        <input type="text" value="" name="repourl" placeholder="Repo URL or File Path" />
        <input type="text" value="" name="repousername" placeholder="Repo Username" />
        <input type="password" value="" name="repopassword" placeholder="Repo Password" />
        <input type="text" value="" name="reposource" placeholder="Repo Source" />
        <input type="text" value="master" name="repobranch" placeholder="Repo Branch" />
        
        <input class="btn btn-sm btn-primary" tabindex="1" type="submit" name="Add Repo" value="Add Repository" />
    </form>-->
    <br>
    <br>

    <h3 style="border-bottom: 1px solid #eee; padding-bottom: 14px; margin-top:0px;"><span class="label label-default"><span class="glyphicon glyphicon-tasks" aria-hidden="true"></span></span> Repository List</h3>

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
                <!--<input type="text" value="${result.username?html}" name="repousername" readonly="true">
                <input type="password" value="${result.password?html}" name="repopassword" readonly="true">-->
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