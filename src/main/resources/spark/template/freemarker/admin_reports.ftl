<#import "masterTemplate.ftl" as layout />
<@layout.masterTemplate title="Admin Bulk">

<div class="row inside-container">
<ul class="nav nav-tabs nav-justified">
    <li role="presentation"><a href="/admin/">Admin</a></li>
    <li role="presentation"><a href="/admin/repo/">Repository Admin</a></li>
    <li role="presentation"><a href="/admin/bulk/">Repository Bulk Admin</a></li>
    <li role="presentation"><a href="/admin/api/">API</a></li>
    <li role="presentation"><a href="/admin/settings/">Settings</a></li>
    <li role="presentation" class="active"><a href="/admin/reports/">Reports</a></li>
    <li role="presentation"><a href="/admin/logs/">Logs</a></li>
    <li role="presentation"><a href="/logout/">Logout</a></li>
</ul>
</div>
<br>

<div class="row">
Reports go here
</div>

</@layout.masterTemplate>