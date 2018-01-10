<#import "masterTemplate.ftl" as layout />
<@layout.masterTemplate title="Admin">

<div class="row inside-container">
    <div class="col-md-2">
        <nav>
          <ul class="nav nav-pills nav-stacked span2">
            <li><a href="/admin/">Dashboard</a></li>
            <li><a href="/admin/repo/">Repository Add</a></li>
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
    <p>You can use this page to edit certain portions of a repository you have set to be indexed. If you cannot edit a field then you need to delete and add the repository again with the correct values.</p>

    <h3 style="border-bottom: 1px solid #eee; padding-bottom: 14px; margin-top:0px;">Repository Edit</h3>

    <#if validatorResult??><div class="alert alert-danger" role="alert">${validatorResult.reason}</div></#if>

    <form class="form-horizontal" method="POST" id="mainForm">
      <div class="form-group" id="reponame-formgroup">
        <label for="reponame" class="col-sm-2 control-label">Repository Name</label>
        <div class="col-sm-10">
          <input type="text" class="form-control" id="reponame" value="<#if repoResult??>${repoResult.name}</#if>" name="reponame" placeholder="Repository Name" required readonly />
          <span id="helpBlock2" class="help-block">Must consist of only only alphanumeric characters - or _ and be a unique name</span>
        </div>
      </div>
     
     <div class="form-group" id="reposcm-formgroup">
          <label for="reposcm" class="col-sm-2 control-label">Repository Source Control Manager</label>
          <div class="col-sm-10">
            <input type="text" class="form-control" value="<#if repoResult??>${repoResult.scm}</#if>" id="reposcm" name="reposcm" placeholder="Repository Source Control Manager" required readonly />
          </div>
      </div>

      <div class="form-group" id="repourl-formgroup">
          <label for="repourl" class="col-sm-2 control-label">Repository Location</label>
          <div class="col-sm-10">
            <input type="text" class="form-control" value="<#if repoResult??>${repoResult.url}</#if>" id="repourl" name="repourl" placeholder="Repository URL or File Path" required readonly />
          </div>
      </div>

      <div class="form-group">
        <label for="repousername" class="col-sm-2 control-label">Repository Username</label>
        <div class="col-sm-10">
          <input type="text" class="form-control" value="<#if repoResult??>${repoResult.username}</#if>" id="repousername" name="repousername" placeholder="Repository username if required" readonly />
        </div>
      </div>

      <div class="form-group">
          <label for="repopassword" class="col-sm-2 control-label">Repository Password</label>
          <div class="col-sm-10">
            <input type="password" class="form-control" value="<#if repoResult??>${repoResult.password}</#if>" id="repopassword" name="repopassword" placeholder="Repository password if required" readonly />
          </div>
      </div>

      <div class="form-group">
            <label for="reposource" class="col-sm-2 control-label">Repository Source</label>
            <div class="col-sm-10">
              <input type="text" class="form-control" value="<#if repoResult??>${repoResult.source}</#if>" id="reposource" name="reposource" placeholder="URL to repository source location or documentation" />
            </div>
      </div>

      <div class="form-group">
          <label for="repobranch" class="col-sm-2 control-label">Repository Branch</label>
          <div class="col-sm-10">
            <input type="text" class="form-control" value="<#if repoResult??>${repoResult.branch}<#else>master</#if>" id="repobranch" name="repobranch" placeholder="For GIT repositories only what branch should be indexed" readonly />
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
                  <option value="${result}" <#if repoResult.data.source == result>selected</#if>>${result}</option>
                </#items>
              </#list>
            </select>
          </div>
      </div>
      <div class="form-group">
          <label for="sourceuser" class="col-sm-2 control-label">Source User</label>
          <div class="col-sm-10">
            <input type="text" class="form-control" value="<#if repoResult??>${repoResult.data.user}</#if>" id="sourceuser" name="sourceuser" placeholder="The user account that is used for the link." />
          </div>
      </div>
      <div class="form-group">
          <label for="sourceproject" class="col-sm-2 control-label">Source Project</label>
          <div class="col-sm-10">
            <input type="text" class="form-control" value="<#if repoResult??>${repoResult.data.project}</#if>" id="sourceproject" name="sourceproject" placeholder="The project name that is used for the link." />
          </div>
      </div>

      <hr />

      <div class="form-group">

      <div class="form-group">
        <div class="col-sm-offset-2 col-sm-10">
          <input id="addRepository" type="submit" class="btn btn-primary" name="addRepo" value="Update Repository" />
        </div>
      </div>
    </form>

</div>
</@layout.masterTemplate>