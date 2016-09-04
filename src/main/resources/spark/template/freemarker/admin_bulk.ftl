<#import "masterTemplate.ftl" as layout />
<@layout.masterTemplate title="Admin Bulk">

<div class="row inside-container">
<ul class="nav nav-tabs nav-justified">
    <li role="presentation"><a href="/admin/">Admin</a></li>
    <li role="presentation"><a href="/admin/repo/">Repository Admin</a></li>
    <li role="presentation" class="active"><a href="/admin/bulk/">Repository Bulk Admin</a></li>
    <li role="presentation"><a href="/admin/api/">API</a></li>
    <li role="presentation"><a href="/admin/settings/">Settings</a></li>
    <li role="presentation"><a href="/admin/logs/">Logs</a></li>
</ul>
</div>
<br>

<div class="row">

<form method="POST">

    <p>You can use this page to insert repositories in bulk up-to 1,000 repositories. Please note that you cannot delete or update repositories using this page. To delete
    use the <a href="/admin/repo/">Repository Admin</a> page.</p>
    <p>If you need to maintain more than 1,000 repositories is is advised to use the <a href="/documentation/#api">API</a>.</p>

<textarea rows="20" style="width:100%;" name="repos"></textarea>
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
        <pre>phindex,git,https://github.com/boyter/Phindex.git,,https://github.com/boyter/Phindex,master</pre>
    </p>

    For example a private repository which requires a username and password
    <p>
        <pre>searchcode,git,https://searchcode@bitbucket.org/searchcode/hosting.git,myusername,mypassword,https://bitbucket.org/searchcode/hosting,master</pre>
    </p>
</div>

</@layout.masterTemplate>