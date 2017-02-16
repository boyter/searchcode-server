<#import "masterTemplate.ftl" as layout />
<@layout.masterTemplate title="Admin">


<div class="row inside-container">
    <div class="col-md-2">
        <nav>
          <ul class="nav nav-pills nav-stacked span2">
            <li class="active"><a href="/admin/">Dashboard</a></li>
            <li><a href="/admin/repo/">Repository Admin</a></li>
            <li><a href="/admin/bulk/">Repository Bulk Admin</a></li>
            <li><a href="/admin/api/">API Keys</a></li>
            <li><a href="/admin/settings/">Settings</a></li>
            <li><a href="/admin/logs/">Logs</a></li>
            <li><a href="/logout/">Logout</a></li>
          </ul>
        </nav>
    </div>

    <div class="col-md-10">
        <h3 style="border-bottom: 1px solid #eee; padding-bottom: 14px; margin-top:0px;"><span class="label label-default"><span class="glyphicon glyphicon-tasks" aria-hidden="true"></span></span> Dashboard <small>(Arch:${sysArch} Version:${sysVersion} Cores:${processorCount})</small></h3>

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
            <div class="col-md-4">
                <div>
                    <h4><span class="glyphicon glyphicon-search" aria-hidden="true"></span> Number of Searches</h4>
                    <p ic-poll="10s" ic-src="/admin/api/getstat/?statname=searchcount">${numSearches}</p>
                </div>
            </div>
            <div class="col-md-4">
                <div>
                    <h4><span class="glyphicon glyphicon-time" aria-hidden="true"></span> Uptime</h4>
                    <p ic-poll="10s" ic-src="/admin/api/getstat/?statname=uptime">${uptime}</p>
                </div>
            </div>
            <div class="col-md-4">
                <div>
                    <h4><span class="glyphicon glyphicon-font" aria-hidden="true"></span> Words in Spelling Corrector</h4>
                    <p ic-trigger-on="load" ic-poll="10s" ic-src="/admin/api/getstat/?statname=spellingcount">${spellingCount}</p>
                </div>
            </div>
        </div>

        <div style="width:100%; display: inline-block;">
            <div class="col-md-4">
                <div>
                    <h4><span class="glyphicon glyphicon-asterisk" aria-hidden="true"></span> Version</h4>
                    <p>
                    ${version} <a ic-get-from="/admin/checkversion/" ic-indicator="#demo-spinner">(check if latest version)</a>
                               <i id="demo-spinner" class="fa fa-spinner fa-spin" style="display:none"><img src="/img/loading_small.gif" /></i>
                    </p>
                </div>
            </div>
            <div class="col-md-4">
                <div>
                    <h4><span class="glyphicon glyphicon-book" aria-hidden="true"></span> Repositories</h4>
                    <p>
                    Total: <span ic-trigger-on="load" ic-poll="60s" ic-src="/admin/api/getstat/?statname=repocount">${repoCount}</span><br />
                    Queued for Deletion: <span ic-poll="60s" ic-src="/admin/api/getstat/?statname=deletionqueue">${deletionQueue}</span>
                    </p>
                </div>
            </div>
            <div class="col-md-4">
                <div>
                    <h4><span class="glyphicon glyphicon-cog" aria-hidden="true"></span> Documents Indexed</h4>
                    <td ic-poll="10s" ic-src="/admin/api/getstat/?statname=numdocs">${numDocs}</td>
                </div>
            </div>
        </div>

        <div style="width:100%; display: inline-block;">
            <table>
                <tr>
                    <td>
                    Add all repositories into the index processing queue.
                    </td>
                    <td>
                    <a ic-post-to="/admin/forcequeue/" ic-indicator="#force-spinner" ic-target="#force-target" class="btn-block btn btn-danger btn-xs" role="button">Force Index Queue</a>
                    <i id="force-spinner" style="display:none"><img src="/img/loading_small.gif" /></i>
                    </td>
                </tr>
                <tr>
                    <td>
                    Reset the search count to 0
                    </td>
                    <td>
                    <a ic-post-to="/admin/clearsearchcount/" ic-indicator="#clearsearchcount-spinner" ic-target="#force-target" class="btn-block btn btn-danger btn-xs" role="button">Clear Search Count</a>
                    <i id="clearsearchcount-spinner" style="display:none"><img src="/img/loading_small.gif" /></i>
                    </td>
                </tr>
                <tr>
                    <td>
                    Reset the spelling corrector.
                    </td>
                    <td>
                    <a ic-post-to="/admin/resetspellingcorrector/" ic-indicator="#resetspellingcorrector-spinner" ic-target="#rebuild-target" class="btn-block btn btn-danger btn-xs" role="button">Reset Spelling</a>
                    <i id="resetspellingcorrector-spinner" style="display:none"><img src="/img/loading_small.gif" /></i>
                    </td>
                </tr>
                <tr>
                    <td>
                    Delete the entire index, all checked out code and then queue everything to be re-indexed.
                    </td>
                    <td>
                    <a ic-post-to="/admin/rebuild/" ic-indicator="#rebuild-spinner" ic-target="#rebuild-target" class="btn-block btn btn-danger btn-xs" role="button">Recrawl & Rebuild Indexes</a>
                    <i id="rebuild-spinner" style="display:none"><img src="/img/loading_small.gif" /></i>
                    </td>
                </tr>
            </table>
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
<!--


    <tr>
    <td width="50%"><b>Admin Functions</b></td>
    <td width="50%">
        <a id="recrawl-reindex" href="#" class="btn btn-danger btn-xs" role="button">Recrawl & Rebuild Indexes</a>
        <a id="force-queue" href="#" class="btn btn-danger btn-xs" role="button">Force Index Queue</a>
        <a id="pause-indexing" href="#" class="btn btn-danger btn-xs" role="button">Pause/Unpause Indexing</a>
        <span id="admin-message"></span></td>
    </tr>
-->



<script src="/js/jquery-1.11.1.min.js"></script>
<script src="/js/intercooler-1.0.3.min.js"></script>

<script>
$(document).ready(function(){


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