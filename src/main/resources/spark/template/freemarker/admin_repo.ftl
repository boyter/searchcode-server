<#import "masterTemplate.ftl" as layout />
<@layout.masterTemplate title="Admin">

<div class="row inside-container">
    <div class="col-md-2">
        <nav>
          <ul class="nav nav-pills nav-stacked span2">
            <li><a href="/admin/">Dashboard</a></li>
            <li class="active"><a href="/admin/repo/">Repository Add</a></li>
            <li><a href="/admin/repolist/">Repository List <span class="badge">${repoCount}</span></a></li>
            <li><a href="/admin/bulk/">Repository Bulk Add</a></li>
            <li><a href="/admin/api/">API Keys</a></li>
            <li><a href="/admin/settings/">Settings</a></li>
            <li><a href="/admin/logs/">Logs</a></li>
            <li><a href="/logout/">Logout</a></li>
          </ul>
        </nav>
    </div>
    <div class="col-md-10">
    <h3 style="border-bottom: 1px solid #eee; padding-bottom: 14px; margin-top:0px;">Repository Admin</h3>
    <p>You can use this page to add repositories to index. If you need to maintain a large amount of repositories it is advised to use the API.</p>

    <h3 style="border-bottom: 1px solid #eee; padding-bottom: 14px; margin-top:0px;">Repository Add</h3>

    <#if validatorResult??><div class="alert alert-danger" role="alert">${validatorResult.reason}</div></#if>

    <form class="form-horizontal" method="POST" id="mainForm">
      <div class="form-group" id="reponame-formgroup">
        <label for="reponame" class="col-sm-2 control-label">Repository Name</label>
        <div class="col-sm-10">
          <input type="text" class="form-control" id="reponame" value="<#if validatorResult??>${validatorResult.repoResult.name}</#if>" name="reponame" placeholder="Repository Name" required />
          <span id="helpBlock2" class="help-block">Must be a unique name</span>
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
      <div class="form-group" id="repourl-formgroup">
          <label for="repourl" class="col-sm-2 control-label">Repository Location</label>
          <div class="col-sm-10">
            <input type="text" class="form-control" value="<#if validatorResult??>${validatorResult.repoResult.url}</#if>" id="repourl" name="repourl" placeholder="Repository URL or File Path" required />
          </div>
      </div>

      <div class="form-group">
        <label for="repousername" class="col-sm-2 control-label">Repository Username</label>
        <div class="col-sm-10">
          <input type="text" class="form-control" value="<#if validatorResult??>${validatorResult.repoResult.username}</#if>" id="repousername" name="repousername" placeholder="Repository username if required" />
        </div>
      </div>

      <div class="form-group">
          <label for="repopassword" class="col-sm-2 control-label">Repository Password</label>
          <div class="col-sm-10">
            <input type="password" class="form-control" value="<#if validatorResult??>${validatorResult.repoResult.password}</#if>" id="repopassword" name="repopassword" placeholder="Repository password if required" />
          </div>
      </div>

      <div class="form-group">
            <label for="reposource" class="col-sm-2 control-label">Repository Source</label>
            <div class="col-sm-10">
              <input type="text" class="form-control" value="<#if validatorResult??>${validatorResult.repoResult.source}</#if>" id="reposource" name="reposource" placeholder="URL to repository source location or documentation" />
            </div>
      </div>

      <div class="form-group">
          <label for="repobranch" class="col-sm-2 control-label">Repository Branch</label>
          <div class="col-sm-10">
            <input type="text" class="form-control" value="<#if validatorResult??>${validatorResult.repoResult.branch}<#else>master</#if>" id="repobranch" name="repobranch" placeholder="For GIT repositories only what branch should be indexed" />
          </div>
      </div>

      <hr />

      <p>The below are only required if you want deeplinks to the original repository to be enabled.</p>

      <div class="form-group">
          <label for="source" class="col-sm-2 control-label">Source</label>
          <div class="col-sm-10">
            <select id="source" name="source" class="form-control">
              <#list repositorySource>
                <option value=""></option>
                <#items as result>
                  <option value="${result}">${result}</option>
                </#items>
              </#list>
            </select>
          </div>
      </div>
      <div class="form-group">
          <label for="sourceuser" class="col-sm-2 control-label">Source User</label>
          <div class="col-sm-10">
            <input type="text" class="form-control" value="<#if validatorResult??>${validatorResult.repoResult.source}</#if>" id="sourceuser" name="sourceuser" placeholder="The user account that is used for the link." />
          </div>
      </div>
      <div class="form-group">
          <label for="sourceproject" class="col-sm-2 control-label">Source Project</label>
          <div class="col-sm-10">
            <input type="text" class="form-control" value="<#if validatorResult??>${validatorResult.repoResult.source}</#if>" id="sourceproject" name="sourceproject" placeholder="The project name that is used for the link." />
          </div>
      </div>

      <hr />

      <div class="form-group">
        <label for="repobranch" class="col-sm-2 control-label">Add Another</label>
        <div class="col-sm-10">
          <input type="checkbox" style="margin-top:12px;" name="return" value="return">
        </div>
      </div>

      <div class="form-group">
        <div class="col-sm-offset-2 col-sm-10">
          <input id="addRepository" type="submit" class="btn btn-primary" name="addRepo" value="Add Repository" />
        </div>
      </div>
    </form>

</div>

<script src="/js/jquery-1.11.1.min.js"></script>
<script>
function validateRepoName() {
  var input = $('#reponame');

  $.ajax('/api/repo/repo/?reponame=' + input.val())
    .done(function(data, textStatus, jqXHR) {
        if (input.val() && (data === 'null' || data === null)) {
            $('#reponame-formgroup').removeClass('has-error');
        }
        else {
            $('#reponame-formgroup').addClass('has-error');
        }
    }).fail(function(xhr, ajaxOptions, thrownError) {
        $('#reponame-formgroup').addClass('has-error');
    });
}

function validateRepoUrl() {
    var input = $('#repourl');

    if (input.val()) {
        $('#repourl-formgroup').removeClass('has-error');
        return true;
    }
    else {
        $('#repourl-formgroup').addClass('has-error');
        return false;
    }
}

$('#reponame').on('input', validateRepoName);
$('#repourl').on('input', validateRepoUrl);
</script>

</@layout.masterTemplate>