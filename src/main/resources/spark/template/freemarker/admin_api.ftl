<#import "masterTemplate.ftl" as layout />
<@layout.masterTemplate title="Admin Settings">

<div class="row inside-container">
<ul class="nav nav-tabs nav-justified">
    <li role="presentation"><a href="/admin/">Admin</a></li>
    <li role="presentation"><a href="/admin/repo/">Repository Admin</a></li>
    <li role="presentation"><a href="/admin/bulk/">Repository Bulk Admin</a></li>
    <li role="presentation" class="active"><a href="/admin/api/">API</a></li>
    <li role="presentation"><a href="/admin/settings/">Settings</a></li>
    <li role="presentation"><a href="/admin/logs/">Logs</a></li>
</ul>
</div>
<br>

<div class="row">
<script src="/js/jquery-1.11.1.min.js"></script>


    <#if apiAuthentication == true>
    <p>
    The API is set to be secured and some API requests require <a href="https://en.wikipedia.org/wiki/Hash-based_message_authentication_code">HMAC Authentication</a>. You
    can use this page to create and delete API keys which can be used by consuming applications.
    </p>

    <table width="100%">
        <tr>
            <th width="40%">Public Key</th>
            <th width="40%">Private Key</th>
            <th width="20%"></th>
        </tr>
    <#list apiKeys>
        <#items as result>
            <tr>
                <td><pre>${result.publicKey}</pre></td>
                <td><pre>${result.privateKey}</pre></td>
                <td><button class="btn btn-sm btn-danger delete" data-id="${result.publicKey}" name="delete" type="submit"><span class="glyphicon glyphicon-remove" aria-hidden="true"></span> delete</button></td>
            </tr>
        </#items>
    </#list>
    </table>
    <br /><br />
    <form method="POST">
            <input class="btn btn-primary" type="submit" name="Generate New API Key" value="Generate New API Key" />
    </form>

    <#else>
    <p>The API is disabled or calls are set to be unauthenticated.</p>
    </#if>

</div>

<script>
$(document).ready(function(){
    $('button.delete').click(function(e) {
        e.preventDefault();
        var thus = $(this);

        var result = confirm("Delete this key?");
        if (result === true) {
            $.ajax('/admin/api/delete/?publicKey=' + thus.data('id'))
               .done(function(data, textStatus, jqXHR) {
                    thus.parent().parent().remove();
               }).fail(function(xhr, ajaxOptions, thrownError) {
                    alert('Sorry was unable to delete. Please reload the page and try again.');
              });
        }
    });
});
</script>

</@layout.masterTemplate>