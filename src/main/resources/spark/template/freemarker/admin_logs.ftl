<#import "masterTemplate.ftl" as layout />
<@layout.masterTemplate title="Admin Logs">



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
            <li class="active"><a href="/admin/logs/">Logs</a></li>
            <li><a href="/logout/">Logout</a></li>
          </ul>
        </nav>
    </div>
    <div class="col-md-10">
        <h3 style="border-bottom: 1px solid #eee; padding-bottom: 14px; margin-top:0px;">Logs <small>(displaying the last 1000 entries from logs)</small></h3>
        <p>
        <a href="?level=all">All</a> | <a href="?level=info">Info</a> | <a href="?level=severe">Severe</a> | <a href="?level=SEARCH">Search Queries</a> | <a href="?level=API">API</a>
        </p>
        <p>Displaying <strong>${level}</strong> logs</p>

        <textarea ic-poll="30s" ic-src="/admin/api/getstat/?statname=${level}logs" style="width: 100%; height: 100%; font-family: monospace;" rows="20" readonly="true">${logs?html}
        </textarea>
    </div>
</div>
<script src="/js/jquery-1.11.1.min.js"></script>
<script src="/js/intercooler-1.1.2.min.js"></script>
</@layout.masterTemplate>