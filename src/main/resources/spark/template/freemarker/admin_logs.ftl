<#import "masterTemplate.ftl" as layout />
<@layout.masterTemplate title="Admin Logs">

<div class="row inside-container">
<ul class="nav nav-tabs nav-justified">
    <li role="presentation"><a href="/admin/">Admin</a></li>
    <li role="presentation"><a href="/admin/repo/">Repository Admin</a></li>
    <li role="presentation"><a href="/admin/bulk/">Repository Bulk Admin</a></li>
    <li role="presentation"><a href="/admin/api/">API</a></li>
    <li role="presentation"><a href="/admin/settings/">Settings</a></li>
    <li role="presentation" class="active"><a href="/admin/logs/">Logs</a></li>
</ul>
</div>
<br>

<div class="row">
<p>This page will display the last 1,000 log entries for all logging levels.</p>

    <pre>
    <textarea style="width: 100%; height: 100%;" rows="20" disabled="true"><#list logs><#items as result>${result}
</#items></#list></textarea>
    </pre>
</div>

</@layout.masterTemplate>