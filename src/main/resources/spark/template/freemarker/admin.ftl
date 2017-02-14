<#import "masterTemplate.ftl" as layout />
<@layout.masterTemplate title="Admin">


<div class="row inside-container">
    <div class="col-md-2">
        <nav>
          <ul class="nav nav-pills nav-stacked span2">
            <li class="active"><a href="/admin/">Dashboard</a></li>
            <li><a href="#">Repository Admin</a></li>
            <li><a href="#">Repository Bulk Admin</a></li>
            <li><a href="#">API Keys</a></li>
            <li><a href="#">Settings</a></li>
            <li><a href="#">Logs</a></li>
            <li><a href="#">Logout</a></li>
          </ul>
        </nav>
    </div>

    <div class="col-md-10">
        <h3 style="border-bottom: 1px solid #eee; padding-bottom: 14px; margin-top:0px;"><span class="label label-default"><span class="glyphicon glyphicon-tasks" aria-hidden="true"></span></span> Dashboard</h3>

        <div style="width:100%; display: inline-block;">
            <div class="col-md-4">
                <div>
                    <h4><span class="glyphicon glyphicon-fire" aria-hidden="true"></span> System Load Average</h4>
                    <p ic-poll="10s" ic-src="/admin/api/getstat/?statname=loadaverage">${loadAverage}</p>
                </div>
            </div>
            <div class="col-md-4">
                <div>
                    <h4><span class="glyphicon glyphicon-th-list" aria-hidden="true"></span> Memory Usage</h4>
                    <p ic-poll="10s" ic-src="/admin/api/getstat/?statname=memoryusage">${memoryUsage}</p>
                </div>
            </div>
            <div class="col-md-4">
                <div>
                    <h4><span class="glyphicon glyphicon-list-alt" aria-hidden="true"></span> Currently Running Jobs</h4>
                    <p ic-poll="10s" ic-src="/admin/api/getstat/?statname=runningjobs">${runningJobs}</p>
                </div>
            </div>
        </div>

        <div style="width:100%; display: inline-block;">
            <h4>System Properties <small>(from searchcode.properties file)</small></h4>

<textarea readonly="true" style="width:100%; font-family: monospace; height: 200px;">sqlite_file=${sqlite_file}
server_port=${server_port}
index_location=${index_location}
facets_location=${facets_location}
repository_location=${repository_location}
trash_location=${trash_location}
check_repo_chages=${check_repo_chages}
check_filerepo_chages=${check_filerepo_chages}
spelling_corrector_size=${spelling_corrector_size}
max_document_queue_size=${max_document_queue_size}
max_document_queue_line_size=${max_document_queue_line_size}
max_file_line_depth=${max_file_line_depth}
only_localhost=${only_localhost}
low_memory=${low_memory}
git_binary_path=${git_binary_path}
use_system_git=${use_system_git}
api_enabled=${api_enabled}
api_key_authentication=${api_key_authentication}
svn_enabled=${svn_enabled}
svn_binary_path=${svn_binary_path}
owasp_database_location=${owasp_database_location}
highlight_lines_limit=${highlight_lines_limit}
binary_extension_white_list=${binary_extension_white_list}
binary_extension_black_list=${binary_extension_black_list}
number_git_processors=${number_git_processors}
number_svn_processors=${number_svn_processors}
number_file_processors=${number_file_processors}
default_and_match=${default_and_match}
log_indexed=${log_indexed}</textarea>


        </div>

    </div>
</div>

<div class="row inside-container">
<ul class="nav nav-tabs nav-justified">
    <li role="presentation" class="active"><a href="/admin/">Admin</a></li>
    <li role="presentation"><a href="/admin/repo/">Repository Admin</a></li>
    <li role="presentation"><a href="/admin/bulk/">Repository Bulk Admin</a></li>
    <li role="presentation"><a href="/admin/api/">API</a></li>
    <li role="presentation"><a href="/admin/settings/">Settings</a></li>
    <li role="presentation"><a href="/admin/logs/">Logs</a></li>
    <li role="presentation"><a href="/logout/">Logout</a></li>
</ul>
</div>
<br>



<div class="row">

<table width="100%">

    <tr>
    <td width="50%"><b>Admin Functions</b></td>
    <td width="50%">
        <a id="recrawl-reindex" href="#" class="btn btn-danger btn-xs" role="button">Recrawl & Rebuild Indexes</a>
        <a id="force-queue" href="#" class="btn btn-danger btn-xs" role="button">Force Index Queue</a>
        <a id="pause-indexing" href="#" class="btn btn-danger btn-xs" role="button">Pause/Unpause Indexing</a>
        <span id="admin-message"></span></td>
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
        <td ic-trigger-on="load" ic-poll="60s" ic-src="/admin/api/getstat/?statname=servertime">${currentdatetime}</td>
    </tr>
    <tr>
        <td><b>Repositories Tracked</b></td>
        <td ic-trigger-on="load" ic-poll="60s" ic-src="/admin/api/getstat/?statname=repocount">${repoCount}</td>
    </tr>
    <tr>
        <td><b>Documents Indexed</b></td>
        <td ic-trigger-on="load" ic-poll="10s" ic-src="/admin/api/getstat/?statname=numdocs">${numDocs}</td>
    </tr>
    <tr>
        <td><b>Number of Searches</b></td>
        <td ic-trigger-on="load" ic-poll="10s" ic-src="/admin/api/getstat/?statname=searchcount">${numSearches}</td>
    </tr>
    <tr>
        <td><b>Uptime</b></td>
        <td ic-trigger-on="load" ic-poll="10s" ic-src="/admin/api/getstat/?statname=uptime">${uptime}</td>
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
        <td ic-trigger-on="load" ic-poll="10s" ic-src="/admin/api/getstat/?statname=deletionqueue">${deletionQueue}</td>
    </tr>
    <tr>
        <td><b>Words in Spelling Corrector</b></td>
        <td ic-trigger-on="load" ic-poll="10s" ic-src="/admin/api/getstat/?statname=spellingcount">${spellingCount}</td>
    </tr>
    <tr>
        <td><b>Index Status</b></td>
        <td id="pause-index-status">${index_paused}</td>
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
        <td><b>trash_location</b></td>
        <td>${trash_location}</td>
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
    <tr>
        <td><b>binary_extension_white_list</b></td>
        <td>${binary_extension_white_list}</td>
    </tr>
    <tr>
        <td><b>binary_extension_black_list</b></td>
        <td>${binary_extension_black_list}</td>
    </tr>
    <tr>
        <td><b>number_git_processors</b></td>
        <td>${number_git_processors}</td>
    </tr>
    <tr>
        <td><b>number_svn_processors</b></td>
        <td>${number_svn_processors}</td>
    </tr>
    <tr>
        <td><b>number_file_processors</b></td>
        <td>${number_file_processors}</td>
    </tr>
    <tr>
        <td><b>default_and_match</b></td>
        <td>${default_and_match}</td>
    </tr>
    <tr>
        <td><b>log_indexed</b></td>
        <td>${log_indexed}</td>
    </tr>

    <tr>
        <td>&nbsp;</td>
        <td>&nbsp;</td>
    </tr>
    <tr>
        <td><b>System Architecture</b></td>
        <td>${sysArch}</td>
    </tr>
    <tr>
        <td><b>System Version</b></td>
        <td>${sysVersion}</td>
    </tr>
    <tr>
        <td><b>Number of Processors</b></td>
        <td>${processorCount}</td>
    </tr>
    <tr>
        <td><b>System Load Average</b></td>
        <td ic-trigger-on="load" ic-poll="10s" ic-src="/admin/api/getstat/?statname=loadaverage">${loadAverage}</td>
    </tr>

    <tr>
        <td><b>Memory Usage</b></td>
        <td ic-trigger-on="load" ic-poll="10s" ic-src="/admin/api/getstat/?statname=memoryusage">${memoryUsage}</td>
    </tr>

    <tr>
        <td><b>Running Jobs</b></td>
        <td ic-trigger-on="load" ic-poll="10s" ic-src="/admin/api/getstat/?statname=runningjobs">${runningJobs}</td>
    </tr>
</table>
    



<script src="/js/jquery-1.11.1.min.js"></script>
<script src="/js/intercooler-1.0.3.min.js"></script>
<style>
td {
    word-break: break-all;
    vertical-align: top;
}
</style>

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

    $('#pause-indexing').click(function(e) {
            e.preventDefault();
            var thus = $(this);

            $.post('/admin/togglepause/')
               .done(function(data, textStatus, jqXHR) {
                    if (data === 'true') {
                        $('#admin-message').html('<i>Indexing is now paused.</i>');
                        $('#pause-index-status').html('paused');
                    }
                    else {
                        $('#admin-message').html('<i>Indexing is running.</i>');
                        $('#pause-index-status').html('running');
                    }
               }).fail(function(xhr, ajaxOptions, thrownError) {
                    $('#admin-message').html('<i>Pause indexing failed. Please try again later.</i>');
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