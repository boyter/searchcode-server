<!DOCTYPE html>
<#compress>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>searchcode server</title>
    <link rel="stylesheet" type="text/css" href="/css/bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" type="text/css" href="/css/newstyle.css">
    <link rel='shortcut icon' type='image/x-icon' href='/img/favicon.ico' />
  </head>

  <body style="background-image: url('/img/${photoId}.jpg'); background-size:cover; background-repeat: no-repeat;">
    <nav class="navbar navbar-default" role="navigation">
      <div class="navbar-header">
        <button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1">
          <span class="sr-only">Toggle navigation</span>
          <span class="icon-bar"></span>
          <span class="icon-bar"></span>
          <span class="icon-bar"></span>
        </button>
        <a class="navbar-brand" href="/">
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

<!-- content -->



<div style="height:400px;">
  <div style="text-align:center; top: 350px;left: 50%; transform: translate(-50%, 0); position:absolute;">
    <div class="white repocount">
    <form method="GET" class="form-inline" <#if isHtml??>action="/html/"</#if>>
      <div class="form-group">
        <input id="searchbox" type="text" size="50" class="form-control" name="q" autofocus="autofocus" placeholder="Type a code snippet or function" autocapitalize="off" autocorrect="off" autocomplete="off" spellcheck="false">
      </div>
      <button type="submit" class="btn btn-success">search</button>
    </form>
    <br />
    <#if repoCount == 0>
    You have no repositories indexed. Add some using the <a href="/admin/">admin</a> page. Read the <a href="/documentation/#repositories">documentation</a> for more details.
    <#else>
    Searching across ${numDocs} files in <a href="/repository/list/">${repoCount} repositories</a>
    </#if>
    </div>
  </div>
</div>



<!-- /content -->

      </div>

      <div class="footer center white">
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
</#compress>
</html>
