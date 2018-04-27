<#import "masterTemplate.ftl" as layout />
<@layout.masterTemplate title="Admin">


<div class="row inside-container">
    <div class="col-md-2">
        <nav>
          <ul class="nav nav-pills nav-stacked span2">
            <li class="active"><a href="/admin/">Dashboard</a></li>
            <li><a href="/admin/repo/">Repository Add</a></li>
            <li><a href="/admin/repolist/">Repository List <span class="badge">${repoCount}</span></a></li>
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
        .panel-body {
            height:160px !important;
            overflow: auto;
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
                        <br>Uptime: <span ic-poll="3s" ic-src="/admin/api/getstat/?statname=uptime">${uptime}</span>
                        <br>Version: ${version} <a ic-get-from="/admin/checkversion/" ic-indicator="#demo-spinner">(check if latest version)</a>
                                            <i id="demo-spinner" class="fa fa-spinner fa-spin" style="display:none"><img src="/img/loading_small.gif" /></i>
                        <br>Threads: <span ic-poll="3s" ic-src="/admin/api/getstat/?statname=threads">${threads}</span>
                        <br>Repo Adder Status: <span ic-poll="1s" ic-src="/admin/api/getstat/?statname=adderpaused">${paused}</span>
                        <br>Repo Parser Status: <span ic-poll="1s" ic-src="/admin/api/getstat/?statname=parserpaused">${paused}</span>
                    </div>
                </div>
            </div>
            <div class="col-md-8">
                <div class="panel panel-default">
                    <div class="panel-heading">
                        <h3 class="panel-title"><span class="glyphicon glyphicon-list-alt" aria-hidden="true"></span> Running Index Jobs</h3>
                    </div>
                    <div class="panel-body"><p ic-poll="3s" ic-src="/admin/api/getstat/?statname=runningjobs">${runningJobs}</p></div>
                </div>
            </div>
        </div>

        <div style="width:100%; display: inline-block;">
            <div class="col-md-8">
                <div class="panel panel-default">
                    <div class="panel-heading">
                        <h3 class="panel-title"><span class="glyphicon glyphicon-search" aria-hidden="true"></span> Index Status</h3>
                    </div>
                    <div class="panel-body">

                    <table style="width:100%;">
                        <tr>
                        <td valign="TOP" width="50%">
                            Number of Searches: <span ic-poll="3s" ic-src="/admin/api/getstat/?statname=searchcount">${numSearches}</span>
                            <br>Documents Indexed: <span ic-poll="3s" ic-src="/admin/api/getstat/?statname=numdocs">${numDocs}</span>
                            <br>Total Repositories: <span ic-poll="3s" ic-src="/admin/api/getstat/?statname=repocount">${repoCount}</span>
                            <br>Queued for Deletion: <span ic-poll="3s" ic-src="/admin/api/getstat/?statname=deletionqueue">${deletionQueue}</span>
                            <br>Queued for Indexing: <span ic-poll="3s" ic-src="/admin/api/getstat/?statname=repoqueuesize">${repoQueueSize}</span>
                        </td>
                        <td valign="TOP"  width="50%">
                            Index Read Location: <span ic-poll="3s" ic-src="/admin/api/getstat/?statname=indexreadlocation"></span>
                            <br>Index Write Location: <span ic-poll="3s" ic-src="/admin/api/getstat/?statname=indexwritelocation"></span>
                            <br>Facet Write Location: <span ic-poll="3s" ic-src="/admin/api/getstat/?statname=facetwritelocation"></span>
                            <br>Index Lines Count: <span ic-poll="1s" ic-src="/admin/api/getstat/?statname=codeindexlinescount"></span>
                            <br>Index Queue Count: <span ic-poll="1s" ic-src="/admin/api/getstat/?statname=codeindexqueuesize"></span>
                        </td>
                        </tr>
                    </table>

                    </div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="panel panel-default">
                    <div class="panel-heading">
                        <h3 class="panel-title"><span class="glyphicon glyphicon-th-list" aria-hidden="true"></span> Memory Usage</h3>
                    </div>
                    <div class="panel-body">
                        <span ic-poll="3s" ic-src="/admin/api/getstat/?statname=memoryusage">${memoryUsage}</span>
                        <br>spelling corrector count: <span ic-poll="3s" ic-src="/admin/api/getstat/?statname=spellingcount">${spellingCount}</span>
                    </div>
                </div>
            </div>
        </div>


        <div style="width:100%; display: inline-block;">

            <h4>System Actions <small>(warning potentially destructive)</small></h4>

            <table class="table">
                <thead>
                    <tr>
                        <th>Description</th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                <tr>
                    <td>Add all repositories into the index processing queue. Repositories are added to the queue when added. Click this if you have set a high value for check_repo_chages or check_filerepo_changes and want to jumpstart the process.
                    <i id="action-spinner1" class="ic-indicator" style="display:none"><img src="/img/loading_small.gif" /></i></td>
                    <td><a style="width:180px;" ic-post-to="/admin/forcequeue/" ic-target="#force-target" ic-indicator="#action-spinner1" class="btn-function btn btn-default btn-xs" role="button">Force Index Queue</a></td>
                </tr>
                 <tr>
                    <td>Reset the number of searches count to zero.
                    <i id="action-spinner2" class="ic-indicator" style="display:none"><img src="/img/loading_small.gif" /></i></td>
                    <td><a style="width:180px;" ic-post-to="/admin/clearsearchcount/" ic-target="#force-target" ic-indicator="#action-spinner2" class="btn-function btn btn-default btn-xs" role="button">Clear Search Count</a></td>
                </tr>
                <tr>
                    <td>Reset the spelling corrector. When repositories are deleted their suggestions remain in the spelling suggestion list. This will push out suggestions which no longer return results.
                    <i id="action-spinner3" class="ic-indicator" style="display:none"><img src="/img/loading_small.gif" /></i></td>
                    <td><a style="width:180px;" ic-post-to="/admin/resetspellingcorrector/" ic-target="#rebuild-target" ic-indicator="#action-spinner3" class="btn-function btn btn-default btn-xs" role="button">Reset Spelling</a></td>
                </tr>
                <tr>
                    <td>Reset the index lines count value. If nothing is being indexed after a while click this button to reset the internal line count value. This should cause the index to start processing again.
                    <i id="action-spinner8" class="ic-indicator" style="display:none"><img src="/img/loading_small.gif" /></i></td>
                    <td><a style="width:180px;" ic-post-to="/admin/resetindexlinescount/" ic-target="#rebuild-target" ic-indicator="#action-spinner8" class="btn-function btn btn-default btn-xs" role="button">Reset Index Lines</a></td>
                </tr>
                <tr>
                    <td>Stops adding repositories to the queue which feed the parsers to download code. Use this to reduce load on searchcode server or source control systems.
                    <i id="action-spinner6" class="ic-indicator" style="display:none"><img src="/img/loading_small.gif" /></i></td>
                    <td><a style="width:180px;" ic-post-to="/admin/togglepause/" ic-target="#rebuild-target" ic-indicator="#action-spinner6" class="btn-function btn btn-default btn-xs" role="button">Pause / Unpause Indexer</a></td>
                </tr>
                <tr>
                    <td>Flip the read index from A to B or B to A.
                    <i id="action-spinner7" class="ic-indicator" style="display:none"><img src="/img/loading_small.gif" /></i></td>
                    <td><a style="width:180px;" ic-post-to="/admin/flipreadindex/" ic-target="#rebuild-target" ic-indicator="#action-spinner7" class="btn-function btn btn-default btn-xs" role="button">Flip Index</a></td>
                </tr>
                <tr>
                    <td>Enable Repo Adder Status. This will enable job parsers to start running again.
                    <i id="action-spinner7" class="ic-indicator" style="display:none"><img src="/img/loading_small.gif" /></i></td>
                    <td><a style="width:180px;" ic-post-to="/admin/enableadder/" ic-target="#rebuild-target" ic-indicator="#action-spinner7" class="btn-function btn btn-default btn-xs" role="button">Enable Repo Adder</a></td>
                </tr>
                <tr>
                    <td>Click this if you want to rebuild the entire index. The index will be rebuilt side by side the existing index and then swapped to avoid any downtime. Updates in the index will only appear when this process is finished.
                    <i id="action-spinner4" class="ic-indicator" style="display:none"><img src="/img/loading_small.gif" /></i></td>
                    <td><a style="width:180px;" ic-post-to="/admin/rebuild/" ic-target="#rebuild-target" ic-indicator="#action-spinner4" class="btn-function btn btn-danger btn-xs" role="button">Recrawl & Rebuild Indexes</a></td>
                </tr>
                </tbody>
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
check_filerepo_changes=${check_filerepo_changes}
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
deep_guess_files=${deep_guess_files}
host_name=${host_name}
index_all_fields=${index_all_fields}</textarea>
        </div>

        <div style="width:100%; display: inline-block;">
            <h4>Data Values <small>(from database)</small></h4>
            <textarea readonly="true" style="width:100%; font-family: monospace; height: 200px;">
            <#list dataValues>
            <#items as data>
${data.key}=${data.value}
</#items></#list></textarea>
        </div>

    </div>
</div>

<script src="/js/jquery-1.11.1.min.js"></script>
<script src="/js/intercooler-1.1.2.min.js"></script>

</div>

</@layout.masterTemplate>