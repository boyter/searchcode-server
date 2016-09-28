<#import "masterTemplate.ftl" as layout />
<@layout.masterTemplate title="Admin">

<div class="row inside-container">
<ul class="nav nav-tabs nav-justified">
    <li role="presentation" class="active"><a href="/admin/">Admin</a></li>
    <li role="presentation"><a href="/admin/repo/">Repository Admin</a></li>
    <li role="presentation"><a href="/admin/bulk/">Repository Bulk Admin</a></li>
    <li role="presentation"><a href="/admin/api/">API</a></li>
    <li role="presentation"><a href="/admin/settings/">Settings</a></li>
    <li role="presentation"><a href="/admin/logs/">Logs</a></li>
</ul>
</div>
<br>

<div class="row">

<table width="100%">

    <tr>
    <td width="50%"><b>Admin Functions</b></td>
    <td width="50%"><a id="recrawl-reindex" href="#" class="btn btn-danger btn-xs" role="button">Recrawl & Rebuild Indexes</a> <a id="force-queue" href="#" class="btn btn-danger btn-xs" role="button">Force Index Queue</a> <span id="admin-message"></span></td>
    </tr>

    <tr>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    </tr>

    <tr>
    <td><b>Server Version</b></td>
    <td>${version} <a id="check-version" href="#">(check if latest version)</a><span id="latest-check"></span></td>
    </tr>

    <tr>
    <td><b>Current Server Time</b></td>
    <td>${currentdatetime}</td>
    </tr>

    <tr>
    <td><b>Repositories Tracked</b></td>
    <td>${repoCount}</td>
    </tr>
    <tr>
    <td><b>Documents Indexed</b></td>
    <td>${numDocs}</td>
    </tr>

    <tr>
    <td><b>Number of Searches</b></td>
    <td>${numSearches}</td>
    </tr>

    <tr>
    <td><b>Uptime</b></td>
    <td>${uptime}</td>
    </tr>

    <tr>
    <td><b>Edition</b></td>
    <td><#if isCommunity == true>
        Community Edition
        <#else>
        Full Edition
        </#if></td>
    </tr>

    <tr>
        <td><b>Number of Repositories Queued for Deletion</b></td>
        <td>${deletionQueue}</td>
    </tr>

    <tr>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    </tr>

    <tr>
    <td><b>sqlite_file</b></td>
    <td>${sqlite_file}</td>
    </tr>
    <tr>
    <td><b>server_port</b></td>
    <td>${server_port}</td>
    </tr>
    <tr>
    <td><b>index_location</b></td>
    <td>${index_location}</td>
    </tr>
    <tr>
    <td><b>facets_location</b></td>
    <td>${facets_location}</td>
    </tr>
    <tr>
    <td><b>repository_location</b></td>
    <td>${repository_location}</td>
    </tr>
    <tr>
    <td><b>check_repo_chages</b></td>
    <td>${check_repo_chages}</td>
    </tr>
    <tr>
    <td><b>check_filerepo_chages</b></td>
    <td>${check_filerepo_chages}</td>
    </tr>
    <tr>
    <td><b>spelling_corrector_size</b></td>
    <td>${spelling_corrector_size}</td>
    </tr>
    <tr>
    <td><b>max_document_queue_size</b></td>
    <td>${max_document_queue_size}</td>
    </tr>
    <tr>
    <td><b>max_document_queue_line_size</b></td>
    <td>${max_document_queue_line_size}</td>
    </tr>
    <tr>
    <td><b>max_file_line_depth</b></td>
    <td>${max_file_line_depth}</td>
    </tr>
    <tr>
    <td><b>only_localhost</b></td>
    <td>${only_localhost}</td>
    </tr>
    <tr>
    <td><b>low_memory</b></td>
    <td>${low_memory}</td>
    </tr>
    <tr>
    <td><b>git_binary_path</b></td>
    <td>${git_binary_path}</td>
    </tr>
    <tr>
    <td><b>use_system_git</b></td>
    <td>${use_system_git}</td>
    </tr>
    <tr>
    <td><b>api_enabled</b></td>
    <td>${api_enabled}</td>
    </tr>
    <tr>
    <td><b>api_key_authentication</b></td>
    <td>${api_key_authentication}</td>
    </tr>
    <tr>
    <td><b>svn_enabled</b></td>
    <td>${svn_enabled}</td>
    </tr>
    <tr>
    <td><b>svn_binary_path</b></td>
    <td>${svn_binary_path}</td>
    </tr>
    <tr>
    <td><b>owasp_database_location</b></td>
    <td>${owasp_database_location}</td>
    </tr>
    <tr>
        <td><b>highlight_lines_limit</b></td>
        <td>${highlight_lines_limit}</td>
    </tr>
</table>
    



<script src="/js/jquery-1.11.1.min.js"></script>



<script>
$(document).ready(function(){
    $('#check-version').click(function(e) {
        e.preventDefault();
        var thus = $(this);

        $.ajax('/admin/checkversion/')
           .done(function(data, textStatus, jqXHR) {
                $('#check-version').hide();
                $('#latest-check').html('<i>'+data.replace(/"/g, '')+'</i>');
           }).fail(function(xhr, ajaxOptions, thrownError) {
                $('#check-version').hide();
                $('#latest-check').html('Was unable to check this version. Refresh the page and try again.');
          });
    });

    $('#force-queue').click(function(e) {
            e.preventDefault();
            var thus = $(this);

            $.post('/admin/forcequeue/')
               .done(function(data, textStatus, jqXHR) {
                    $('#admin-message').html('<i>Queue forced successfully.</i>');
               }).fail(function(xhr, ajaxOptions, thrownError) {
                    $('#admin-message').html('<i>Queue force failed. Please try again later.</i>');
               });
    });


    $('#recrawl-reindex').click(function(e) {
        e.preventDefault();
        var thus = $(this);

        var result = confirm("Rebuild everything? Searches will be unreliable until finished.");
        if (result === true) {
            $('#admin-message').html('<i>Please wait...</i>');

            $.post('/admin/rebuild/')
               .done(function(data, textStatus, jqXHR) {
                    $('#admin-message').html('<i>Rebuild reindex run successfully.</i>');
               }).fail(function(xhr, ajaxOptions, thrownError) {
                    $('#admin-message').html('<i>Rebuild reindex failed. Please try again later.</i>');
               });
        }
    });


});
</script>

</div>

</@layout.masterTemplate>