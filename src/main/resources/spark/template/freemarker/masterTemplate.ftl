<#macro masterTemplate title="searchcode server">
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${title}</title>
    <link rel="stylesheet" type="text/css" href="/css/bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" type="text/css" href="/css/newstyle.css">
    <link rel='shortcut icon' type='image/x-icon' href='/img/favicon.ico' />
  </head>
  <body>
    <nav class="navbar navbar-default" role="navigation">
      <div class="navbar-header">
        <button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1">
          <span class="sr-only">Toggle navigation</span>
          <span class="icon-bar"></span>
          <span class="icon-bar"></span>
          <span class="icon-bar"></span>
        </button>
        <a class="navbar-brand" <#if isHtml??>href="/html/"<#else>href="/"</#if>>
          <#if logoImage != "">
              <img height="24px" src="${logoImage}" />
          <#else>
              <img height="24px" src="/img/searchcode_logo_full.png" />
          </#if>
        </a>

      </div>

      <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
        <ul class="nav navbar-nav navbar-right">
            <li><a href="/repository/list/">Repositories</a></li>
            <li><a href="/documentation/">Documentation</a></li>
            <li><a href="/admin/">Admin</a></li>
        </ul>
      </div>
    </nav>
      <div class="container">
        <div class="search-options"><form method="GET" <#if isHtml??>action="/html/"<#else>action="/"</#if>><div class="form-inline"><div class="form-group"><input name="q" autocapitalize="off" autocorrect="off" autocomplete="off" spellcheck="true" size="50" placeholder="Search Expression" type="search" class="form-control" id="searchbox" <#if searchValue?? >value="${searchValue?html}"</#if> ></div><input type="submit" value="search" class="btn btn-success"></div></form></div>
        <#nested />
      </div>
      <div class="footer center">
        <p>
          <#if isCommunity??>
              <#if isCommunity == true><br /><br />You are running <a href="https://searchcode.com/product/download/">searchcode server community edition</a>.</#if>
          </#if>
          <br><br><small>&copy; searchcode ${.now?string('yyyy')}</small>
        </p>
      </div>
    </div>
    ${embed}
  </body>
</html>
</#macro>
