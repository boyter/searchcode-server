<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" type="text/css" href="/css/newstyle.css">
    <link rel="stylesheet" type="text/css" href="/css/bootstrap/css/bootstrap.min.css">
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
    <div class="container"></div>

    <div class="footer center">
        <p>
          <#if isCommunity??>
              <#if isCommunity == true><br /><br />You are running <a href="https://searchcode.com/product/download/">searchcode server community edition</a>.</#if>
          </#if>
          <br><br><small>&copy; searchcode ${.now?string('yyyy')}</small>
        </p>
      </div>
</div>
<script>
var preload = ${searchResultJson};
</script>
${embed}
</body>
<script src="/js/script.min.js"></script>
</html>
