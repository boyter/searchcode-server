<#import "masterTemplate.ftl" as layout />
<@layout.masterTemplate title="Repository Error">

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
    <h3 style="border-bottom: 1px solid #eee; padding-bottom: 14px; margin-top:0px;">Repository Error - ${repoResult.name}</h3>
    <p>There was an issue with indexing the repository "${repoResult.name}". Details of the repository are included below.</p>
    

    <dl class="dl-horizontal">
        <dt>Name</dt>
        <dd>${repoResult.name}</dd>
        <dt>URL</dt>
        <dd>${repoResult.url}</dd>
        <dt>SCM</dt>
        <dd>${repoResult.scm}</dd>
        <dt>Branch</dt>
        <dd>${repoResult.branch}</dd>
        <dt>Username</dt>
        <dd>${repoResult.username}</dd>
        <dt>Password</dt>
        <dd><i>This value is always hidden</i></dd>
    </dl>

    <p>The exact error reported is included below.</p>

    <pre><code style="white-space: initial;">${repoResult.data.indexError}</code></pre>

    <br>
    <p>To resolve the issue consult the below to identify the issue and see what resolution is required.</p>

    <dl class="dl-horizontal">
        <dt>Invalid remote: origin</dt>
        <dd>The URL provided is incorrect or you have selected the wrong SCM. Check that you are able to clone or checkout a copy using the provided details above. To remedy you will need to delete the repository and add it again.</dd>
        <dt>not authorized</dt>
        <dd>The username or password provided is incorrect or is required in order to check out this repository. To resolve this you will need to delete the repository and add it again with correct details.</dd>
    </dl>

</div>
</div>

</@layout.masterTemplate>