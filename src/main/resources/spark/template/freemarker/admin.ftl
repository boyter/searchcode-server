<#import "masterTemplate.ftl" as layout />
<@layout.masterTemplate title="Admin">


<div class="row inside-container">
    <div class="col-md-2">
        <nav>
          <ul class="nav nav-pills nav-stacked span2">
            <li class="active"><a href="/admin/">Dashboard</a></li>
            <li><a href="/admin/repo/">Repository Add</a></li>
            <li><a href="/admin/repolist/">Repository List</a></li>
            <li><a href="/admin/bulk/">Repository Bulk Add</a></li>
            <li><a href="/admin/api/">API Keys</a></li>
            <li><a href="/admin/settings/">Settings</a></li>
            <li><a href="/admin/logs/">Logs</a></li>
            <li><a href="/logout/">Logout</a></li>
          </ul>
        </nav>
    </div>

    <div class="col-md-10">
        <h3 style="border-bottom: 1px solid #eee; padding-bottom: 14px; margin-top:0px;">Dashboard <small>(Arch:${sysArch} Version:${sysVersion} Cores:${processorCount})</small></h3>

        <style>
        .panel {
            height:180px !important;
        }
        </style>

        <div style="width:100%; display: inline-block;">
            <div class="col-md-4">
                <div class="panel panel-default">
                    <div class="panel-heading">
                        <h3 class="panel-title"><span class="glyphicon glyphicon-fire" aria-hidden="true"></span> System Statistics</h3>
                    </div>
                    <div class="panel-body">
                        System Load Average: <span ic-poll="3s" ic-src="/admin/api/getstat/?statname=loadaverage">${loadAverage}</span>
                        <br>Uptime: <span ic-poll="60s" ic-src="/admin/api/getstat/?statname=uptime">${uptime}</span>
                        <br>Version: ${version} <a ic-get-from="/admin/checkversion/" ic-indicator="#demo-spinner">(check if latest version)</a>
                                            <i id="demo-spinner" class="fa fa-spinner fa-spin" style="display:none"><img src="/img/loading_small.gif" /></i>
                        <br>Threads: <span ic-poll="60s" ic-src="/admin/api/getstat/?statname=threads">${threads}</span>
                        <br>Index Status: <span ic-poll="1s" ic-src="/admin/api/getstat/?statname=paused">${paused}</span>
                    </div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="panel panel-default">
                    <div class="panel-heading">
                        <h3 class="panel-title"><span class="glyphicon glyphicon-th-list" aria-hidden="true"></span> Memory Usage</h3>
                    </div>
                    <div class="panel-body"><p ic-poll="3s" ic-src="/admin/api/getstat/?statname=memoryusage">${memoryUsage}</p></div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="panel panel-default">
                    <div class="panel-heading">
                        <h3 class="panel-title"><span class="glyphicon glyphicon-font" aria-hidden="true"></span> Words in Spelling Corrector</h3>
                    </div>
                    <div class="panel-body"><p ic-poll="3s" ic-src="/admin/api/getstat/?statname=spellingcount">${spellingCount}</p></div>
                </div>
            </div>
        </div>

        <div style="width:100%; display: inline-block;">
            <div class="col-md-4">
                <div class="panel panel-default">
                    <div class="panel-heading">
                        <h3 class="panel-title"><span class="glyphicon glyphicon-search" aria-hidden="true"></span> Index Status</h3>
                    </div>
                    <div class="panel-body">
                    Number of Searches: <span ic-poll="3s" ic-src="/admin/api/getstat/?statname=searchcount">${numSearches}</span>
                    <br>Documents Indexed: <span ic-poll="3s" ic-src="/admin/api/getstat/?statname=numdocs">${numDocs}</span>
                    <br>
                    <br>Total Repositories: <span ic-poll="60s" ic-src="/admin/api/getstat/?statname=repocount">${repoCount}</span>
                    <br>Queued for Deletion: <span ic-poll="60s" ic-src="/admin/api/getstat/?statname=deletionqueue">${deletionQueue}</span>
                    </div>
                </div>
            </div>
            <div class="col-md-4">

            </div>
            <div class="col-md-4">
                <div class="panel panel-default">
                    <div class="panel-heading">
                        <h3 class="panel-title"><span class="glyphicon glyphicon-list-alt" aria-hidden="true"></span> Running Index Jobs</h3>
                    </div>
                    <div class="panel-body"><p ic-poll="3s" ic-src="/admin/api/getstat/?statname=runningjobs">${runningJobs}</p></div>
                </div>
            </div>
        </div>


        <div style="width:100%; display: inline-block;">

            <a data-text="Add all repositories into the index processing queue. Click this if you want to force the index to be updated with newly added repositories." style="width:180px;" ic-post-to="/admin/forcequeue/" ic-target="#force-target" ic-indicator="#action-spinner" class="btn-function btn btn-danger btn-xs" role="button">Force Index Queue</a>
            <a data-text="Reset the search count to zero." style="width:180px;" ic-post-to="/admin/clearsearchcount/" ic-target="#force-target" ic-indicator="#action-spinner" class="btn-function btn btn-danger btn-xs" role="button">Clear Search Count</a>
            <a data-text="Reset the spelling corrector. If many repositories have been deleted this will push out suggestions which no longer return results." style="width:180px;" ic-post-to="/admin/resetspellingcorrector/" ic-target="#rebuild-target" ic-indicator="#action-spinner" class="btn-function btn btn-danger btn-xs" role="button">Reset Spelling</a>
            <a data-text="Delete the entire index, checked out code and queue everything to be re-indexed. Click this if you are getting inconsistent search results." style="width:180px;" ic-post-to="/admin/rebuild/" ic-target="#rebuild-target" ic-indicator="#action-spinner" class="btn-function btn btn-danger btn-xs" role="button">Recrawl & Rebuild Indexes</a>
            <a data-text="Pauses the indexer from running. Use this to reduce load on the searchcode server or source control system." style="width:180px;" ic-post-to="/admin/togglepause/" ic-target="#rebuild-target" ic-indicator="#action-spinner" class="btn-function btn btn-danger btn-xs" role="button">Pause/Unpause Indexer</a>


            <i id="action-spinner" class="ic-indicator" style="display:none"><img src="/img/loading_small.gif" /></i>

            <p><strong id="function-description">&nbsp;</strong></p>

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
directory_black_list=${directory_black_list}
number_git_processors=${number_git_processors}
number_svn_processors=${number_svn_processors}
number_file_processors=${number_file_processors}
default_and_match=${default_and_match}
log_indexed=${log_indexed}
follow_links=${follow_links}
deep_guess_files=${deep_guess_files}</textarea>


        </div>

    </div>
</div>

<script src="/js/jquery-1.11.1.min.js"></script>
<script src="/js/intercooler-1.0.3.min.js"></script>

<script>
$(document).ready(function(){
    $('.btn-function').hover(
        function(e){
            $('#function-description').html($(this).data('text'));
        },
        function(e) {
            $('#function-description').html('&nbsp;');
        }
    );
});
</script>

</div>

</@layout.masterTemplate>