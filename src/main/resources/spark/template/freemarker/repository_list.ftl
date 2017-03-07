<#import "masterTemplate.ftl" as layout />
<@layout.masterTemplate title="Repository List">

<div class="row inside-container">
    <#list repoList>
        <table class="table table-striped table-hover">
            <thead>
                <tr>
                    <th>Repository Name</th>
                    <th>Index Status</th>
                    <th>File Count</th>
                </tr>
            </thead>
            <tbody>
            <#items as result>
                  <tr>
                    <td><a href="/repository/overview/${result.name?html}/">${result.name?html}</a></td>
                    <td><span ic-trigger-on="load" ic-poll="30s" ic-src="/admin/api/checkindexstatus/?reponame=${result.name?html}"></span></td>
                    <td><span ic-trigger-on="load" ic-poll="30s" ic-src="/api/repo/filecount/?reponame=${result.name?html}"></span></td>
                  </tr>
            </#items>
            </tbody>
        </table>
    </#list>
</div> <!-- end row -->

<script src="/js/jquery-1.11.1.min.js"></script>
<script src="/js/intercooler-1.0.3.min.js"></script>
</@layout.masterTemplate>