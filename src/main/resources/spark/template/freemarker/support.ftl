<#import "masterTemplate.ftl" as layout />
<@layout.masterTemplate title="Support">

<link rel="stylesheet" href="/css/highlight/default.css">
<script src="/js/highlight.pack.js"></script>
<link class="codestyle" rel="stylesheet" href="/css/highlight/${highligher}.css">
<script>hljs.initHighlightingOnLoad();</script>

<pre><code>${codeValue}</code></pre>

</@layout.masterTemplate>