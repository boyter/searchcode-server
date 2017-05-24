<#import "masterTemplate.ftl" as layout />
<@layout.masterTemplate title="Admin">

<div class="row inside-container">
    <div class="col-md-2">
        <nav>
          <ul class="nav nav-pills nav-stacked span2">
            <li><a href="/admin/">Dashboard</a></li>
            <li class="active"><a href="/admin/repo/">Repository Add</a></li>
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
    <h3 style="border-bottom: 1px solid #eee; padding-bottom: 14px; margin-top:0px;">Repository Admin</h3>
    <p>You can use this page to add repositories to index or find and remove them from the index. If you need to maintain a large amount of repositories it is advised to use the API.</p>
    <p>Please note that deleting a repository adds it to queue for deletion and as such may not be removed immediately.</p>

    <h3 style="border-bottom: 1px solid #eee; padding-bottom: 14px; margin-top:0px;">Repository Add</h3>
    <form class="form-horizontal" method="POST">
      <div class="form-group" id="reponame-formgroup">
        <label for="reponame" class="col-sm-2 control-label">Repository Name</label>
        <div class="col-sm-10">
          <input type="text" class="form-control" id="reponame" value="" name="reponame" placeholder="Repository Name" />
          <span id="helpBlock2" class="help-block">Must consist of only only alphanumeric characters or - and be a unique name</span>
        </div>
      </div>
      <div class="form-group">
        <label for="reposcm" class="col-sm-2 control-label">SCM</label>
        <div class="col-sm-10">
          <select id="reposcm" name="reposcm" class="form-control">
              <option value="git">GIT</option>
              <option value="svn">SVN</option>
              <option value="file">File System</option>
          </select>
        </div>
      </div>
      <div class="form-group" id="repourl-formgroup">
          <label for="repourl" class="col-sm-2 control-label">Repository Location</label>
          <div class="col-sm-10">
            <input type="text" class="form-control" value="" id="repourl" name="repourl" placeholder="Repository URL or File Path" />
          </div>
      </div>

      <div class="form-group">
        <label for="repousername" class="col-sm-2 control-label">Repository Username</label>
        <div class="col-sm-10">
          <input type="text" class="form-control" value="" id="repousername" name="repousername" placeholder="Repository username if required" />
        </div>
      </div>

      <div class="form-group">
          <label for="repopassword" class="col-sm-2 control-label">Repository Password</label>
          <div class="col-sm-10">
            <input type="password" class="form-control" value="" id="repopassword" name="repopassword" placeholder="Repository password if required" />
          </div>
      </div>

      <div class="form-group">
            <label for="reposource" class="col-sm-2 control-label">Repository Source</label>
            <div class="col-sm-10">
              <input type="text" class="form-control" value="" id="reposource" name="reposource" placeholder="URL to repository source location or documentation" />
            </div>
      </div>

      <div class="form-group">
          <label for="repobranch" class="col-sm-2 control-label">Repository Branch</label>
          <div class="col-sm-10">
            <input type="text" class="form-control" value="master" id="repobranch" name="repobranch" placeholder="For GIT repositories only what branch should be indexed" />
          </div>
      </div>

      <div class="form-group">
        <label for="repobranch" class="col-sm-2 control-label">Add Another</label>
        <div class="col-sm-10">
          <input type="checkbox" style="margin-top:12px;" name="return" value="return">
        </div>
      </div>

      <div class="form-group">
        <div class="col-sm-offset-2 col-sm-10">
          <input type="submit" class="btn btn-primary" name="addRepo" value="Add Repository" />
        </div>
      </div>
    </form>

</div>

<script src="/js/jquery-1.11.1.min.js"></script>
<script>
$('#reponame').on('input', function() {
	var input = $(this);
	var re = /^[a-zA-Z0-9-]*$/;
	var is_valid = re.test(input.val());

	$.ajax('/api/repo/repo/?reponame=' + input.val())
    .done(function(data, textStatus, jqXHR) {
        if (is_valid && input.val() && data === 'null') {
            $('#reponame-formgroup').removeClass('has-error');
        }
        else {
            $('#reponame-formgroup').addClass('has-error');
        }

    }).fail(function(xhr, ajaxOptions, thrownError) {
        $('#reponame-formgroup').addClass('has-error');
    });
});

$('#repourl').on('input', function() {
	var input = $(this);

	if (input.val()) {
	    $('#repourl-formgroup').removeClass('has-error');
    }
	else {
	    $('#repourl-formgroup').addClass('has-error');
    }
});
</script>

</@layout.masterTemplate>