<#import "masterTemplate.ftl" as layout />
<@layout.masterTemplate title="Repository List">
<#setting url_escaping_charset="UTF-8">

<div class="row inside-container">
    <#list repoList>
        <center>
            <#if hasPrevious == true>
                <a href="?offset=${previousOffset}" class="btn btn-xs btn-success filter-button" />&#9664; Previous</a>
            <#else>
                <input type="submit" value="&#9664; Previous" disabled="true" class="btn btn-xs btn-success filter-button" />
            </#if>
            <#if hasNext == true>
                <a href="?offset=${nextOffset}" class="btn btn-xs btn-success filter-button" />Next &#9658;</a>
            <#else>
                <input type="submit" value="Next &#9658;" disabled="true" class="btn btn-xs btn-success filter-button" />
            </#if>
        </center>


        <table class="table table-striped table-hover">
            <thead>
                <tr>
                    <th>Repository Name</th>
                    <th>Repository Source</th>
                    <th>Index Status</th>
                    <th>Last Index Time</th>
                    <th>File Count</th>
                    <th>Index Time (seconds)</th>
                </tr>
            </thead>
            <tbody>
            <#items as result>
                  <tr>
                    <td><a href="/repository/overview/${result.name?url}/">${result.name?html}</a></td>
                    <td>${result.url}</td>
                    <td><span ic-trigger-on="load" ic-poll="30s" ic-src="/admin/api/checkindexstatus/?reponame=${result.name?html}"></span></td>
                    <td><span ic-trigger-on="load" ic-poll="30s" ic-src="/api/repo/indextime/?reponame=${result.name?html}"></span></td>
                    <td><span ic-trigger-on="load" ic-poll="30s" ic-src="/api/repo/filecount/?reponame=${result.name?html}"></span></td>
                    <td><span ic-trigger-on="load" ic-poll="30s" ic-src="/api/repo/indextimeseconds/?reponame=${result.name?html}"></span></td>
                  </tr>
            </#items>
            </tbody>
        </table>

         <center>
            <#if hasPrevious == true>
                <a href="?offset=${previousOffset}" class="btn btn-xs btn-success filter-button" />&#9664; Previous</a>
            <#else>
                <input type="submit" value="&#9664; Previous" disabled="true" class="btn btn-xs btn-success filter-button" />
            </#if>
            <#if hasNext == true>
                <a href="?offset=${nextOffset}" class="btn btn-xs btn-success filter-button" />Next &#9658;</a>
            <#else>
                <input type="submit" value="Next &#9658;" disabled="true" class="btn btn-xs btn-success filter-button" />
            </#if>
        </center>
    </#list>

</div> <!-- end row -->

<script src="/js/jquery-1.11.1.min.js"></script>
<script src="/js/intercooler-1.1.2.min.js"></script>
</@layout.masterTemplate>