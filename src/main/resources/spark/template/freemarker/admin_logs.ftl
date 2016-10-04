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
<p>This page will display the last 1,000 log entries for different logging levels, defaulting to the value in your properties file.</p>
<p>
<a href="?level=all">All</a> | <a href="?level=info">Info</a> | <a href="?level=warning">Warning</a> | <a href="?level=severe">Severe</a>
</p>
<p>Displaying <strong>${level}</strong> logs</p>
<#list logs>
    <#items as result>
          <pre>${result}</pre>
    </#items>
</#list>
</div>

</@layout.masterTemplate>