<#import "masterTemplate.ftl" as layout />
<@layout.masterTemplate title="Admin Settings">

<div class="row inside-container">
<ul class="nav nav-tabs nav-justified">
    <li role="presentation"><a href="/admin/">Admin</a></li>
    <li role="presentation"><a href="/admin/repo/">Repository Admin</a></li>
    <li role="presentation"><a href="/admin/bulk/">Repository Bulk Admin</a></li>
    <li role="presentation"><a href="/admin/api/">API</a></li>
    <li role="presentation" class="active"><a href="/admin/settings/">Settings</a></li>
</ul>
</div>
<br>

<div class="row">

    <form method="POST" class="form-inline">

        <#if isCommunity??>
            <#if isCommunity == true>
            <div class="alert alert-warning" role="alert">
            <span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>
            <span class="sr-only">Warning:</span> You are running the community edition. Changes applied in here will be ignored! To make changes and get sweet support <a href="https://searchcode.com/product/download/">purchase a supported version</a>.
            </div>
            </#if>
        </#if>

        <table>
        <tr>
        <td valign="top"><b>Logo (base64 encoded image)</b></td>
        <td valign="top"><input id="base64logo" maxlength="16000" class="form-control" type="text" value="${logoImage}" name="logo"></td>
        </tr>
        <tr>
        <td></td>
        <td>Empty this field to reset. Should be in format similar to the below.
            This format can be created using <a target="_blank" href="http://www.dailycoding.com/Utils/Converter/ImageToBase64.aspx">Online Image to Base64 Converter</a>
            but consider resizing the image to a height of 24 px first.
            <pre>data:image/png;base64, iVBORw0KGgoA...</pre>
            <pre>data:image/jpeg;base64, iVBORw0KGgoA...</pre>
            Max length of 16000 characters.
            </td>
        </tr>

        <tr>
        <td valign="top"><b>Syntax Highlighter</b></td>
        <td valign="top">
            <select name="syntaxhighligher" id="syntaxhighligher" class="form-control">
            <#list highlighters>
            <#items as highlighter>
              <option <#if syntaxHighlighter == highlighter >selected</#if>>${highlighter}</option>
            </#items>
            </#list>
            </select></td>
        </tr>

        <tr>
        <td valign="top"><b>Average Salary</b></td>
        <td valign="top"><input id="averagesalary" class="form-control" type="text" value="${averageSalary}" name="averagesalary"></td>
        </tr>
        <tr>
        <td></td>
        <td>Empty this field to reset. Used to calculate the cost estimates. Set by default to 56000 but adjust accordingly up or down
        to make results reflect reality. Must contain only whole numbers.</td>
        </tr>

        <tr>
        <td valign="top"><b>Match Lines</b></td>
        <td valign="top"><input id="matchlines" class="form-control" type="text" value="${matchLines}" name="matchlines"></td>
        </tr>
        <tr>
        <td></td>
        <td>Empty this field to reset. How many matching lines to look for when searching. Set by default to 15. Must contain only whole numbers.</td>
        </tr>

        <tr>
        <td valign="top"><b>Max Line Depth</b></td>
        <td valign="top"><input id="maxlinedepth" class="form-control" type="text" value="${maxLineDepth}" name="maxlinedepth"></td>
        </tr>
        <tr>
        <td></td>
        <td>Empty this field to reset. How many lines to look into when searching. Set by default to 10000. Increasing this value will slow down searches. Must contain only whole numbers.</td>
        </tr>

        <tr>
        <td valign="top"><b>Minified Length</b></td>
        <td valign="top"><input id="minifiedlength" class="form-control" type="text" value="${minifiedLength}" name="minifiedlength"></td>
        </tr>
        <tr>
        <td></td>
        <td>Empty this field to reset. What average line length (ignoring blank lines) indicates the file as being minified and hence it will not indexed. Set by default to 255. Must contain only whole numbers.</td>
        </tr>

        <tr>
            <td valign="top"><input id="changeSettings" class="btn btn-primary" type="submit" name="Change Settings" value="Change Settings" /></td>
            <td></td>
        </tr>

        </table>

    </form>
</div>

</@layout.masterTemplate>