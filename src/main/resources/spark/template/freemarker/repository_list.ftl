<#import "masterTemplate.ftl" as layout />
<@layout.masterTemplate title="Repository List">

<div class="row inside-container">
    <#list repoList>
        <table class="table table-striped">
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
                    <td><a href="/repository/overview/${result.name}/">${result.name}</a></td>
                    <td></td>
                    <td></td>
                  </tr>
            </#items>
            </tbody>
        </table>
    </#list>
</div> <!-- end row -->


</@layout.masterTemplate>