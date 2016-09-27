<#import "masterTemplate.ftl" as layout />
<@layout.masterTemplate title="Admin">

<div class="row inside-container">
<ul class="nav nav-tabs nav-justified">
    <li role="presentation"><a href="/admin/">Admin</a></li>
    <li role="presentation" class="active"><a href="/admin/repo/">Repository Admin</a></li>
    <li role="presentation"><a href="/admin/bulk/">Repository Bulk Admin</a></li>
    <li role="presentation"><a href="/admin/api/">API</a></li>
    <li role="presentation"><a href="/admin/settings/">Settings</a></li>
    <li role="presentation"><a href="/admin/logs/">Logs</a></li>
</ul>
</div>
<br>

<div class="row">

<p>You can use this page to insert and delete repositories. Please note that updates are not supported.</p>
<p>If you need to maintain more than 1,000 repositories is is advised to use the <a href="/documentation/#api">API</a>.

<script src="/js/jquery-1.11.1.min.js"></script>

     <form method="POST">
        <input type="text" value="" name="reponame" placeholder="Repo Name" id="reponame" />
        <select name="reposcm" id="reposcm">
            <option value="git">GIT</option>
            <option value="svn">SVN</option>
            <option value="file">File</option>
        </select>
        <input type="text" value="" name="repourl" placeholder="Repo URL or File Path" id="repourl" />
        <input type="text" value="" name="repousername" placeholder="Repo Username" />
        <input type="password" value="" name="repopassword" placeholder="Repo Password" />
        <input type="text" value="" name="reposource" placeholder="Repo Source" />
        <input type="text" value="master" name="repobranch" placeholder="Repo Branch" />
        <input type="text" value="*" name="repomasks" placeholder="Repo File Masks" />
        
        <input class="btn btn-sm btn-primary" tabindex="1" type="submit" name="Add Repo" value="Add Repository" />
    </form>
    <br>
    <br>

    <div class="center">


        <form method="GET">
            <#if hasPrevious == true>
                <a href="?offset=${previousOffset}" class="btn btn-xs btn-success filter-button" />&#9664; Previous</a>
            <#else>
                <input type="submit" value="&#9664; Previous" disabled="true" class="btn btn-xs btn-success filter-button" />
            </#if>

            <input type="text" <#if searchQuery?? >value="${searchQuery}"<#else>value=""</#if> name="q" placeholder="Filter Repositories" />
            <input class="btn btn-xs btn-primary" type="submit" value="Filter" />

            <#if hasNext == true>
                <a href="?offset=${nextOffset}" class="btn btn-xs btn-success filter-button" />Next &#9658;</a>
            <#else>
                <input type="submit" value="Next &#9658;" disabled="true" class="btn btn-xs btn-success filter-button" />
            </#if>
        </form>


    </div>
    <br><br>


    <#list repoResults>
        <#items as result>
            <div>
                <input type="text" value="${result.name}" name="reponame" disabled="true">
                <input type="text" value="${result.scm}" name="reposcm" disabled="true">
                <input type="text" value="${result.url}" name="repourl" disabled="true">
                <input type="text" value="${result.username}" name="repousername" disabled="true">
                <input type="password" value="${result.password}" name="repopassword" disabled="true">
                <input type="text" value="${result.source}" name="reposource" disabled="true">
                <input type="text" value="${result.branch}" name="repobranch" disabled="true">
                <input type="text" value="${result.masks}" name="repomasks" disabled="true">
                <button class="btn btn-sm btn-danger delete" data-id="${result.name}" name="delete" type="submit"><span class="glyphicon glyphicon-remove" aria-hidden="true"></span> delete</button>
            </div>
        </#items>
    </#list>



<script>
$(document).ready(function(){
    $('button.delete').click(function(e) {
        e.preventDefault();
        var thus = $(this);

        var result = confirm("Delete this repository?");
        if (result === true) {
            $.ajax('/admin/delete/?repoName=' + thus.data('id'))
               .done(function(data, textStatus, jqXHR) {
                    thus.parent().remove();
               }).fail(function(xhr, ajaxOptions, thrownError) {
                    alert('Sorry was unable to delete. Please reload the page and try again.');
              });
        }
    });

    function isLocalPath(s)
    {
        return s != null && s.length > 0 &&
                (s.charAt(0) == '\\' || s.charAt(0) == '/' || (s.length > 1 && s.charAt(1) == ':'));
    }

    function baseName(str)
    {
        str = str.replace(/\\/g, '/');
        var base = new String(str).substring(str.lastIndexOf('/') + 1);
        if(base.lastIndexOf(".") != -1)
            base = base.substring(0, base.lastIndexOf("."));
        return base;
    }

    var userNameInput = false;
    var userScmSelect = false;
    $('#repourl').on('input', function() {
        var p = $(this).val();
        if (!userNameInput)
            $("#reponame").val(baseName(p).trim());
        if (!userScmSelect && isLocalPath(p))
            $("#reposcm").val("file");
            //$("#reposcm option[value='file']").prop("selected", true);
    });

    $('#reponame').on('input', function() {
        userNameInput = $(this).val() != "";
    });

    $('#reposcm').change(function() {
        userScmSelect = true;
    });

});
</script>

</div>

</@layout.masterTemplate>