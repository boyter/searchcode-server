<#import "masterTemplate.ftl" as layout />
<@layout.masterTemplate title="Admin Bulk">

<div class="row inside-container">
    <div class="col-md-2">
        <nav>
          <ul class="nav nav-pills nav-stacked span2">
            <li><a href="/admin/">Dashboard</a></li>
            <li><a href="/admin/repo/">Repository Add</a></li>
            <li><a href="/admin/repolist/">Repository List <span class="badge">${repoCount}</span></a></li>
            <li class="active"><a href="/admin/bulk/">Repository Bulk Add</a></li>
            <li><a href="/admin/api/">API Keys</a></li>
            <li><a href="/admin/settings/">Settings</a></li>
            <li><a href="/admin/logs/">Logs</a></li>
            <li><a href="/logout/">Logout</a></li>
          </ul>
        </nav>
    </div>
    <div class="col-md-10">
    <h3 style="border-bottom: 1px solid #eee; padding-bottom: 14px; margin-top:0px;">Repository Bulk API</h3>

    <p>You can use this page to insert repositories in bulk up-to 1,000 repositories. Please note that you cannot delete or update repositories using this page. To delete
    use the <a href="/admin/repolist/">Repository List</a> page.</p>
    <p>If you need to maintain more than 1,000 repositories is is advised to use the <a href="/documentation/#api">API</a>.</p>


<#if validatorResults??>
<div class="alert alert-danger" role="alert">
<#list validatorResults>
    <#items as result>
        ${result.reason?html}<br>
    </#items>
</#list>
</div>
</#if>

<form method="POST">

<textarea rows="20" style="width:100%;" name="repos"><#if validatorResults??>
<#list validatorResults>
<#items as result>
${result.line?html}
</#items>
</#list>
</#if></textarea>
    <br /><br />
    <input class="btn btn-primary" type="submit" value="Update/Add Repositories" />
    </form>


    <br>
    Add to the above in the CSV format as shown in the examples below, with one repository per line.
    <p>
        <pre>reponame,scm,gitrepolocation,username,password,repourl,repobranch</pre>
    </p>
    For example a public repository which does not require username or password
    <p>
        <pre>phindex,git,https://github.com/boyter/Phindex.git,,,https://github.com/boyter/Phindex,master</pre>
    </p>

    For example a private repository which requires a username and password
    <p>
        <pre>searchcode,git,https://searchcode@bitbucket.org/searchcode/hosting.git,myusername,mypassword,https://bitbucket.org/searchcode/hosting,master</pre>
    </p>
</div>

    </div>
</div>

</@layout.masterTemplate>