<#import "masterTemplate.ftl" as layout />
<@layout.masterTemplate title="Documentation">

<script src="/js/jquery-1.11.1.min.js"></script>
<link rel="stylesheet" href="/css/highlight/default.css">
<link class="codestyle" rel="stylesheet" href="/css/highlight/monokai_sublime.css">
<script src="/js/highlight.pack.js"></script>
<script>hljs.initHighlightingOnLoad();</script>

<div class="row inside-container">

    <div class="col-sm-3">
          <div class="sidebar-module sidebar-module-inset">
            <h2>Documentation</h2>
            <p>How to search and administrate your searchcode server.</p>

          </div>
          <div class="sidebar-module">
            <h5>Guide</h5>
            <ol class="list-unstyled">
              <li><a href="#searching">Searching</a></li>
              <li><a href="#literal">Literal Search</a></li>
              <li><a href="#html">HTML Only</a></li>
              <li><a href="#filters">Filters</a></li>
              <li><a href="#owners">Code Owners</li>
              <li><a href="#considerations">Considerations</a></li>
              <li><a href="#estimatedcost">Estimated Cost</a></li>
              <li><a href="#api">API</a></li>
            </ol>
            <h5>Administration</h5>
            <ol class="list-unstyled">
              <li><a href="#web-server">Web Server</a></li>
              <li><a href="#properties">Properties</a></li>
              <li><a href="#settings">Settings</a></li>
              <li><a href="#apikeys">API Keys</a></li>
              <li><a href="#backups">Backups</a></li>
              <li><a href="#recovery">Recovery</a></li>
              <li><a href="#repositories">Repositories</a></li>
              <li><a href="#filerepositories">File Repositories</a></li>
              <li><a href="#troubleshooting">Troubleshooting</a></li>
              <li><a href="#support">Support</a></li>
            </ol>
          </div></div>
    <div class="col-sm-9">

      <div>
        <h2>Guide</h2>
        <p>
        With searchcode server you can search across any repository of code that has been added by your administrator.
        </p>
        <p>
        Type in anything you want to find and you will be presented with the results that match with the relevant lines highlighted.
        Searches can filtered down using the right filter panel. Suggested search terms are,
          <ul>
            <li>Function/Method names E.G. <a href="/?q=Format">Format</a>, <a href="/?q=re.compile">re.compile</a></li>
            <li>Constant and variable names E.G. <a href="/?q=ERROR">ERROR</a>, <a href="/?q=username">username</a></li>
            <li>Operations E.G. <a href="/?q=foreach">foreach</a>, <a href="/?q=while">while</a></li>
            <li>Security Flaws E.G. <a href="/?q=eval+%24_GET">eval $_GET</a></li>
            <li>Usage E.G. <a href="/?q=import+flash.display.Sprite%3B">import flash.display.Sprite;</a></li>
            <li>Special Chracters E.G. <a href="/?q=%2B">+</a></li>
          </ul>
        </p>

        <h3 id="searching">Searching</h3>
        <p>
        Type any term you want to search for in the search box and press the enter key. Generally best results can be
        gained by searching for terms that you expect to be close to each other on the same line.
        </p>
        <p>
        The following search operators are supported.

        <dl class="dl-horizontal">
            <dt>AND</dt>
            <dd>Match where documents contain terms on both sides of the operator. E.G. <a href="/?q=test%20AND%20import">test AND import</a></dd>
            <dt>OR</dt>
            <dd>Match where documents contain terms on either side of the operator. E.G. <a href="/?q=test%20OR%20import">test OR import</a></dd>
            <dt>NOT</dt>
            <dd>Match where documents do not contain terms on the right hand side of the operator. E.G. <a href="/?q=test%20NOT%20import">test NOT import</a></dd>
            <dt>( )</dt>
            <dd>Group terms. Allows creation of exclusive matches. E.G. <a href="/?q=(test%20OR%20import)%20AND%20other">(test OR import) AND other</a></dd>
            <dt>*</dt>
            <dd>Wildcard. Only applies at end of a query. E.G. <a href="/?q=test*">test*</a></dd>
        </dl>

        An example using all of the above would be <a href="/?q=(mkdir%20NOT%20sphinx*)%20OR%20(php%20AND%20print*)">(mkdir NOT sphinx*) OR (php AND print*)</a>
        This will search for documents containing mkdir but not starting with sphinx or documents containing php and containing terms starting with print.
        Operators must be in upper case where show or they will be treated as part of the query itself. I.E. to match on documents containing <a href="/?q=and">and</a> search for and lowercase.
        <br /><br />
        Other characters are treated as part of the search itself. This means that a search for something such as <a href="/?q=i%2B%2B%3B">i++;</a> is
        not only a legal search it is likely to return results for most code bases.
        </p>
        <p>
        If a search does not return the results you are expecting or no results at all consider rewriting the query.
        For example searching for <strong>Arrays.asList("a1", "a2", "b1", "c2", "c1")</strong> could be turned into a
        looser query by searching for <strong>Arrays.asList</strong> or <strong>Arrays asList</strong>. Another example would be <strong>EMAIL_ADDRESS_REGEX</strong> for
        <strong>email address regex</strong>.
        </p>
        <p>
        To view the full file that is returned click on the name of the file, or click on any line to be taken to that line.
        Syntax highlighting is enabled for all files less than 1000 lines in length.
        </p>

        <h3 id="literal">Literal Search</h3>
        <p>
        You can perform a literal search against the index by enabling literal search. To do so check the box "Literal Search" in the Search Options
        panel of the search result page. This search includes all the standard searches performed by <a href="https://lucene.apache.org/core/5_2_1/queryparser/org/apache/lucene/queryparser/classic/package-summary.html">Lucene</a>.
        </p>

        <h4>Wildcard Searches</h4>
        <p>Lucene supports single and multiple character wildcard searches within single terms (not within phrase queries).</p>
        <p>To perform a single character wildcard search use the "?" symbol.</p>
        <p>To perform a multiple character wildcard search use the "*" symbol.</p>
        <p>The single character wildcard search looks for terms that match that with the single character replaced. For example, to search for "text" or "test" you can use the search: <pre class="code">te?t</pre></p>
        <p>Multiple character wildcard searches looks for 0 or more characters. For example, to search for test, tests or tester, you can use the search: <pre class="code">test*</pre></p>
        <p>You can also use the wildcard searches in the middle of a term. <pre class="code">te*t</pre></p>
        <p>Note: You cannot use a * or ? symbol as the first character of a search.</p>

        <h4>Regular Expression Searches</h4>
        <p>Lucene supports regular expression searches matching a pattern between forward slashes "/". For example to find documents containing "moat" or "boat": <pre class="code">/[mb]oat/</pre></p>


        <h4>Fuzzy Searches</h4>
        <p>Lucene supports fuzzy searches based on Damerau-Levenshtein Distance. To do a fuzzy search use the tilde, "~", symbol at the end of a Single word Term. For example to search for a term similar in spelling to "roam" use the fuzzy search: <pre class="code">roam~</pre></p>
        <p>This search will find terms like foam and roams.</p>
        <p>An additional (optional) parameter can specify the maximum number of edits allowed. The value is between 0 and 2, For example: <pre class="code">roam~1</pre></p>
        <p>The default that is used if the parameter is not given is 2 edit distances.</p>

        <h4>Proximity Searches</h4>
        <p>Lucene supports finding words are a within a specific distance away. To do a proximity search use the tilde, "~", symbol at the end of a Phrase. For example to search for a "apache" and "jakarta" within 10 words of each other in a document use the search:
        </p><pre class="code">"jakarta apache"~10</pre>

        <p>
        The following fields are supported. All spaces and / characters are replaced with _
         <dl class="dl-horizontal">
            <dt>fn</dt>
            <dd>File name. E.G. fn:search*</dd>
            <dt>rn</dt>
            <dd>Repository name. E.G. rn:searchcode*</dd>
            <dt>ln</dt>
            <dd>Language name. E.G. ln:java OR ln:bourne_again_shell</dd>
            <dt>on</dt>
            <dd>Owner name. E.G. on:ben</dd>
            <dt>fl</dt>
            <dd>File location. E.G. fl:src*</dd>
        </dl>
        </p>

        <h3 id="html">HTML Only</h3>
        <p>
        You can search using a pure HTML interface (no javascript) <a href="/html/">by clicking here</a>. Note that this page generally
        lags behind the regular interface in functionality.
        </p>

        <h3 id="filters">Filters</h3>
        <p>
        Any search can be filtered down to a specific repository, source, identified language or code owner using the refinement options.
        Select one or multiple repositories, sources, languages or owners and click the "Filter Selected or "refine search" button to do this.
        </p>
        <p>
        Note that in the case that a filter has only a single option for source it will not display any option to filter. This is to avoid cluttering the display were you have only a single source to search from.
        </p>
        <p>
        Filters on the normal interface persist between searches. This allows you to select a specific repository or language and continue searching. To clear applied filters uncheck the filters indivudually and click on "Filter Selected". You can also click "Clear Filters" button to clear all active filters. The HTML only page filters are cleared between every new search.
        </p>

        <h3 id="owners">Code Owners</h3>
        <p>
        The owner of any piece of code is determined differently between source control systems. See below for details.
        </p>
        <p>
        GIT owners are determined by counting the number of lines edited by each user. This is then weighted
        against the last commit time. For example, Bob added a file of 100 lines in length 1 year ago.
        Mary modified 30 lines of the file last week. In this situation Mary would be marked as the owner as she has modified
        enough of the file and recently enough to be more familiar with it then Bob would be. If she has only modified a single
        line however Bob would still be marked as the owner.
        </p>
        <p>
        The name is taken based on the git config user.name setting attached to the user in commits.
        </p>
        <p>
        SVN owners are determined by looking at the last user to change the file. For example, Bob edited a single line in a file with 100 lines. Bob will be
        considered the owner even if Mary edited the other 99 previously.
        </p>


        <h3 id="considerations">Considerations</h3>
        <p>Source code is complex to search. As such the following restrictions currently apply
        <ul>
            <li>Relevant lines in the search display favor lines in the beginning of file however there may be other matching lines within the file. By default searchcode will only inspect the first 10000 lines for matches when serving results.</li>
            <li>The following characters are not indexed and will be ignored from searches <strong>< > ) ( [ ] | =</strong>.</li>
            <li>Where possible if there are no matches searchcode server will attempt to suggest an alternate search which is more likely to produce results.</li>
        </ul>
        </p>

        <h3 id="estimatedcost">Estimated Cost</h3>
        <p>The estimated cost for any file or project is created using the <a target="_blank" href="https://en.wikipedia.org/wiki/COCOMO">Basic COCOMO</a>
        algorithmic software cost estimation model. The cost reflected includes design, coding, testing, documentation for both
        developers and users, equipment, buildings etc... which can result in a higher estimate then would be expected. Generally
        consider this the cost of developing the code, and not what it is "worth".
        It is based on an average salary of $56,000 per year but this value can be changed by the system administrator if
        the values appear to be too out of expectation.</p>


        <h3 id="api">API</h3>
        <p>API endpoints offered by your searchcode server instance are described below. Note that some require API authentication
        which will also be covered.</p>
        <p>
          <h4>API Authentication</h4>
          API authentication is done through the use of shared secret key HMAC generation. If enabled you will be required to sign
          the arguments sent to the API endpoint as detailed. Ask your administrator for a public and private key to be generated for you
          if you require access to the API.
          <br><br>
          To sign a request see the below examples in Python demonstrating how to perform all repository API calls.
          The most important thing to note is that parameter order is important. All API endpoints will list the order
          that parameters should have passed in. The below code is has no license and is released as public domain. The second
          example is identical to the first but performs the signing using SHA512 for greater security.<br /><br />
          SHA1 Example
<textarea style="font-family: monospace,serif; width:100%; height:150px;" readonly="true">from hashlib import sha1
from hmac import new as hmac
import urllib2
import json
import urllib
import pprint

publickey = "REALPUBLICKEYHERE"
privatekey = "REALPRIVATEKEYHERE"

reponame = "myrepo"
repourl = "myrepourl"
repotype = "git"
repousername = ""
repopassword = ""
reposource = ""
repobranch = "master"

message = "pub=%s&reponame=%s&repourl=%s&repotype=%s&repousername=%s&repopassword=%s&reposource=%s&repobranch=%s" % (
        urllib.quote_plus(publickey),
        urllib.quote_plus(reponame),
        urllib.quote_plus(repourl),
        urllib.quote_plus(repotype),
        urllib.quote_plus(repousername),
        urllib.quote_plus(repopassword),
        urllib.quote_plus(reposource),
        urllib.quote_plus(repobranch)
    )

sig = hmac(privatekey, message, sha1).hexdigest()

url = "http://localhost:8080/api/repo/add/?sig=%s&%s" % (urllib.quote_plus(sig), message)

data = urllib2.urlopen(url)
data = data.read()

data = json.loads(data)
print data['sucessful'], data['message']

################################################################

reponame = "myrepo"
repourl = "myrepourl"
repotype = "git"
repousername = ""
repopassword = ""
reposource = ""
repobranch = "master"
source = "source"
sourceuser = "sourceuser"
sourceproject = "sourceproject"

message = "pub=%s&reponame=%s&repourl=%s&repotype=%s&repousername=%s&repopassword=%s&reposource=%s&repobranch=%s&source=%s&sourceuser=%s&sourceproject=%s" % (
        urllib.quote_plus(publickey),
        urllib.quote_plus(reponame),
        urllib.quote_plus(repourl),
        urllib.quote_plus(repotype),
        urllib.quote_plus(repousername),
        urllib.quote_plus(repopassword),
        urllib.quote_plus(reposource),
        urllib.quote_plus(repobranch),
        urllib.quote_plus(source),
        urllib.quote_plus(sourceuser),
        urllib.quote_plus(sourceproject),
    )

sig = hmac(privatekey, message, sha1).hexdigest()

url = "http://localhost:8080/api/repo/add/?sig=%s&%s" % (urllib.quote_plus(sig), message)

data = urllib2.urlopen(url)
data = data.read()

data = json.loads(data)
print data['sucessful'], data['message']

################################################################

message = "pub=%s" % (urllib.quote_plus(publickey))

sig = hmac(privatekey, message, sha1).hexdigest()

url = "http://localhost:8080/api/repo/list/?sig=%s&%s" % (urllib.quote_plus(sig), message)

data = urllib2.urlopen(url)
data = data.read()

data = json.loads(data)
print data['sucessful'], data['message'], data['repoResultList']

################################################################

message = "pub=%s&reponame=%s" % (
        urllib.quote_plus(publickey),
        urllib.quote_plus(reponame),
    )

sig = hmac(privatekey, message, sha1).hexdigest()

url = "http://localhost:8080/api/repo/delete/?sig=%s&%s" % (urllib.quote_plus(sig), message)

data = urllib2.urlopen(url)
data = data.read()

data = json.loads(data)
print data['sucessful'], data['message']

################################################################

message = "pub=%s" % (urllib.quote_plus(publickey))

sig = hmac(privatekey, message, sha1).hexdigest()

url = "http://localhost:8080/api/repo/reindex/?sig=%s&%s" % (urllib.quote_plus(sig), message)

data = urllib2.urlopen(url)
data = data.read()

data = json.loads(data)
print data['sucessful'], data['message']</textarea>

SHA512 example

<textarea style="font-family: monospace,serif; width:100%; height:150px;" readonly="true">from hashlib import sha512
from hmac import new as hmac
import urllib2
import json
import urllib
import pprint

'''Simple usage of the signed key API endpoints using SHA512 hmac'''
publickey = "REALPUBLICKEYHERE"
privatekey = "REALPRIVATEKEYHERE"


reponame = "myrepo"
repourl = "myrepourl"
repotype = "git"
repousername = ""
repopassword = ""
reposource = ""
repobranch = "master"

message = "pub=%s&reponame=%s&repourl=%s&repotype=%s&repousername=%s&repopassword=%s&reposource=%s&repobranch=%s" % (
  urllib.quote_plus(publickey),
  urllib.quote_plus(reponame),
  urllib.quote_plus(repourl),
  urllib.quote_plus(repotype),
  urllib.quote_plus(repousername),
  urllib.quote_plus(repopassword),
  urllib.quote_plus(reposource),
  urllib.quote_plus(repobranch)
)

sig = hmac(privatekey, message, sha512).hexdigest()

url = "http://localhost:8080/api/repo/add/?sig=%s&%s&hmac=sha512" % (urllib.quote_plus(sig), message)

data = urllib2.urlopen(url)
data = data.read()

data = json.loads(data)
print data['sucessful'], data['message']

################################################################

message = "pub=%s" % (urllib.quote_plus(publickey))

sig = hmac(privatekey, message, sha512).hexdigest()

url = "http://localhost:8080/api/repo/list/?sig=%s&%s&hmac=sha512" % (urllib.quote_plus(sig), message)

data = urllib2.urlopen(url)
data = data.read()

data = json.loads(data)
print data['sucessful'], data['message'], data['repoResultList']

################################################################

message = "pub=%s&reponame=%s" % (
  urllib.quote_plus(publickey),
  urllib.quote_plus(reponame),
)

sig = hmac(privatekey, message, sha512).hexdigest()

url = "http://localhost:8080/api/repo/delete/?sig=%s&%s&hmac=sha512" % (urllib.quote_plus(sig), message)

data = urllib2.urlopen(url)
data = data.read()

data = json.loads(data)
print data['sucessful'], data['message']

################################################################

message = "pub=%s" % (urllib.quote_plus(publickey))

sig = hmac(privatekey, message, sha512).hexdigest()

url = "http://localhost:8080/api/repo/reindex/?sig=%s&%s&hmac=sha512" % (urllib.quote_plus(sig), message)

data = urllib2.urlopen(url)
data = data.read()

data = json.loads(data)
print data['sucessful'], data['message']</textarea>
          <br><br>
          To achive the same result in Java use <a href="https://commons.apache.org/proper/commons-codec/apidocs/org/apache/commons/codec/digest/HmacUtils.html">HmacUtils</a> as follows,
          <br><br>
          <textarea style="font-family: monospace,serif; width:100%; height:55px;" readonly="true">String myHmac = HmacUtils.hmacSha1Hex(MYPRIVATEKEY, PARAMSTOHMAC);
String myHmac = HmacUtils.hmacSha512Hex(MYPRIVATEKEY, PARAMSTOHMAC);</textarea>
        </p>
        <p>
          <h4>Repository API (secured)</h4>
          The repository API allows you to list, add/update and delete repositories that are currently being indexed within your searchcode server instance. All calls to the repository API methods are secured if the appropiate property has been set by your administrator.
        
          <h5>Endpoint List All Repositories</h5>
          <pre>/api/repo/list/</pre>
          <p>Some repositories returned by this endpoint may be queued for deletion. They will continue to appear in this list until they
          are sucessfully removed.</p>
          <h5>Params</h5>
              <ul>
                <li>sig: signed value (optional if unsecured)</li>
                <li>pub: the public key supplied by your administrator (optional if unsecured)</li>
              </ul>
        
          <h5>Signing</h5>
          To sign requests to this endpoint you need to HMAC as follows<br>
          <pre>hmac_sha1("MYPRIVATEKEY", "pub=MYPUBLICKEY")</pre>

          <h5>Examples</h5>
          <pre>http://localhost/api/repo/list/</pre>
          <pre>http://localhost/api/repo/list/?sig=SIGNEDKEY&pub=PUBLICKEY</pre>
        
          <h5>Return Field Definitions</h5>

          <dl class="dl-horizontal">
            <dt>message</dt>
            <dd>A message containing debug information if the request fails.</dd>
            <dt>sucessful</dt>
            <dd>True or false value if the request was processed.</dd>

            <dt>repoResultList</dt>
            <dd>An array containing the repository results. Will only be present if the call was sucessful.
              <dl class="dl-horizontal">
                <dt>branch</dt>
                <dd>Branch that is being monitored. N.B. this is not applicable to SVN repositories.</dd>
                <dt>name</dt>
                <dd>name used to idenity this repository.</dd>
                <dt>password</dt>
                <dd>The password used to authenticate for clone and update requests</dd>
                <dt>rowId</dt>
                <dd>Only used internally. Refers to the rowId of the database</dd>
                <dt>scm</dt>
                <dd>The source control management system used</dd>
                <dt>source</dt>
                <dd>The source URL that should point to where this repository is located</dd>
                <dt>url</dt>
                <dd>The endpoint URL that us used for clone and update requests</dd>
                <dt>username</dt>
                <dd>The username used to authenticate for clone and update requests</dd>
              </dl>
            </dd>
          </dl>

          <h5>Sample Response</h5>
          <pre>{
    "message": "",
    "repoResultList": [
        {
            "branch": "master",
            "name": "test",
            "password": "",
            "rowId": 1,
            "scm": "git",
            "source": "http://github.com/myuser/myrepo",
            "url": "git://github.com/myuser/myrepo.git",
            "username": ""
        }
    ],
    "sucessful": true
}</pre>


        <h5>Endpoint Add Repository</h5>
          <pre>/api/repo/add/</pre>
          <p>It is not possible to update an existing repository. To do so you must first delete the existing repository and wait for the background
           tasks finish cleaning the repository.
          </p>
          <h5>Params</h5>
            <ul>
              <li>sig: signed value (optional if unsecured)</li>
              <li>pub: the public key supplied by your administrator (optional if unsecured)</li>
              <li>reponame: unique name to identify the repository if matches existing it will delete the existing and recreate</li>
              <li>repourl: the url to the repository endpoint</li>
              <li>repotype: the type of repository this is, NB only git is currently supported</li>
              <li>repousername: username used to pull from the repository</li>
              <li>repopassword: password used to pull from the repository</li>
              <li>reposource: a http link pointing where the repository can be browsed or a helpful link</li>
              <li>repobranch: what branch should be indexed</li>
              <li>source: (Optional) which source to use for deeplinks, needs to match a value in source_database_location to build the link</li>
              <li>sourceuser: (Optional) populates the user value of the source_database_location values</li>
              <li>sourceproject: (Optional) populates the project value of the source_database_location values</li>
            </ul>

        
          <h5>Signing</h5>
          To sign requests to this endpoint you need to HMAC as follows<br>
          <pre>hmac_sha1("MYPRIVATEKEY", "pub=MYPUBLICKEY&reponame=REPONAME&repourl=REPOURL&repotype=REPOTYPE&repousername=REPOUSERNAME&repopassword=REPOPASSWORD&reposource=REPOSOURCE&repobranch=REPOBRANCH")</pre>
          <pre>hmac_sha1("MYPRIVATEKEY", "pub=MYPUBLICKEY&reponame=REPONAME&repourl=REPOURL&repotype=REPOTYPE&repousername=REPOUSERNAME&repopassword=REPOPASSWORD&reposource=REPOSOURCE&repobranch=REPOBRANCH&source=SOURCE&sourceuser=SOURCEUSER&sourceproject=SOURCEPROJECT")</pre>

          <h5>Examples</h5>
          <pre>http://localhost/api/repo/add/?reponame=testing&repourl=git://github.com/test/test.git&repotype=git&repousername=MYUSER&repopassword=MYPASSWORD&reposource=http://githib.com/test/test/&repobranch=master</pre>
          <pre>http://localhost/api/repo/add/?sig=SIGNEDKEY&pub=PUBLICKEY&reponame=testing&repourl=git://github.com/test/test.git&repotype=git&repousername=MYUSER&repopassword=MYPASSWORD&reposource=http://githib.com/test/test/&repobranch=master</pre>
          <pre>http://localhost/api/repo/add/?sig=SIGNEDKEY&pub=PUBLICKEY&reponame=testing&repourl=git://github.com/someone/test/test.git&repotype=git&repousername=MYUSER&repopassword=MYPASSWORD&reposource=http://githib.com/test/test/&repobranch=master&source=GitHub&sourceuser=someone&sourceproject=test</pre>
        
          <h5>Return Field Definitions</h5>

          <dl class="dl-horizontal">
            <dt>message</dt>
            <dd>A message containing debug information if the request fails.</dd>
            <dt>sucessful</dt>
            <dd>True or false value if the request was processed.</dd>
          </dl>

          <h5>Sample Response</h5>
          <pre>{
    "message": "added repository sucessfully",
    "sucessful": true
}</pre>


        <h5>Endpoint Delete Repository</h5>
          <pre>/api/repo/delete/</pre>
          <p>Successful calls to this endpoint will insert a request into a queue to remove the repository. The actual deletion
          can take several minutes.<p>
          <h5>Params</h5>
            <ul>
              <li>sig: signed value (optional if unsecured)</li>
              <li>pub: the public key supplied by your administrator (optional if unsecured)</li>
              <li>reponame: unique name to identify the repository</li>
            </ul>
        
          <h5>Signing</h5>
          To sign requests to this endpoint you need to HMAC as follows<br>
          <pre>hmac_sha1("MYPRIVATEKEY", "pub=MYPUBLICKEY&reponame=REPONAME)"</pre>

          <h5>Examples</h5>
          <pre>http://localhost/api/repo/delete/?reponame=testing</pre>
          <pre>http://localhost/api/repo/delete/?sig=SIGNEDKEY&pub=PUBLICKEY&reponame=testing</pre>
        
          <h5>Return Field Definitions</h5>

          <dl class="dl-horizontal">
            <dt>message</dt>
            <dd>A message containing debug information if the request fails.</dd>
            <dt>sucessful</dt>
            <dd>True or false value if the request was processed.</dd>
          </dl>

          <h5>Sample Response</h5>
          <pre>{
    "message": "deleted repository sucessfully",
    "sucessful": true
}</pre>

        <h5>Endpoint Rebuild & Reindex Repository</h5>
          <pre>/api/repo/reindex/</pre>
          <p>Successful calls to this endpoint will cause the index and repository directories to be deleted and schedule all repositories to be reindexed. Note that queries to the system while the reindex is running may not return expected results.<p>
          <h5>Params</h5>
            <ul>
              <li>sig: signed value (optional if unsecured)</li>
              <li>pub: the public key supplied by your administrator (optional if unsecured)</li>
            </ul>
        
          <h5>Signing</h5>
          To sign requests to this endpoint you need to HMAC as follows<br>
          <pre>hmac_sha1("MYPRIVATEKEY", "pub=MYPUBLICKEY")</pre>

          <h5>Examples</h5>
          <pre>http://localhost/api/repo/reindex/?sig=SIGNEDKEY&pub=PUBLICKEY</pre>
          <pre>http://localhost/api/repo/delete/?sig=SIGNEDKEY&pub=PUBLICKEY</pre>
        
          <h5>Return Field Definitions</h5>

          <dl class="dl-horizontal">
            <dt>message</dt>
            <dd>A message containing debug information if the request fails.</dd>
            <dt>sucessful</dt>
            <dd>True or false value if the request was processed.</dd>
          </dl>

          <h5>Sample Response</h5>
          <pre>{
    "message": "reindex forced",
    "sucessful": true
}</pre>



        <h5>Post commit hook index</h5>
          <pre>/api/repo/index/</pre>
          <p>Successful calls to this endpoint will suggest to searchcode that a repository has been updated and add it to the
          index queue. If already on the queue this method does nothing. The queue is a first in first out queue and repositories
          will be processed in order.<p>
          <h5>Params</h5>
            <ul>
              <li>repoUrl: the repository url you wish to index (required)</li>
            </ul>
           <h5>Examples</h5>
           <pre>http://localhost/api/repo/index/?repoUrl=https://github.com/boyter/searchcode-server.git</pre>
           <pre>http://localhost/api/repo/index/?repoUrl=/disk/location/</pre>
          <h5>Sample Response</h5>
          <pre>{
    sucessful: true,
    message: "Enqueued repository https://github.com/boyter/searchcode-server.git"
}</pre>




      </div>

      <hr>

      <div>
        <h2>Administration</h2>
        <p>
        searchcode server is designed to require as little maintenance as possible and look after itself once setup and
        repositories are indexed. However it can be tuned using the settings mentioned below in the searchcode.properties
        file or through the <a href="/admin/settings/">admin settings page</a>.
        </p>

        <h3 id="web-server">Web Server</h3>

        <p>searchcode server uses the high performance jetty web server. It should perform well even with thousands of requests
        as a front facing solution. If a reverse proxy solution is required there is no need to configure static assets, simply
        configure all requests to pass back to searchcode server. You should also set the config property only_localhost to true
        in this case.</p>

        <h3 id="properties">Properties</h3>

        <p>There are two properties files in the base directory of searchcode server, searchcode.properties and quartz.properties.</p>

        <p>
        The searchcode.properties file in the base directory is a simple text file that can be used to configure aspects of searchcode server. By default
        it is setup using suggested defaults. <b>It is important to note that the password to administer your server is located
        in this file</b>.
        To apply changes, modify the file as required then restart searchcode. All slashes used in the properties file should be forward not backwards. I.E. Unix style not Windows.

            <dl class="dl-horizontal">
              <dt id="password">password</dt>
              <dd>The password used to login to the admin section. <strong>It is suggested that this is changed.</strong></dd>
              <dt>database</dt>
              <dd>Do not modify this value. Additional database support is planned but not implemented.</dd>
              <dt>sqlite_file</dt>
              <dd>The name of the sqlite database file. If you change this you will need to copy or move the existing file to match the new value.</dd>
              <dt>server_port</dt>
              <dd>The port number that will be bound to. Needs to be a number or will default to 8080.</dd>
              <dt>repository_location</dt>
              <dd>Path to where the checked out repositories will be.</dd>
              <dt>index_location</dt>
              <dd>Path to where the index will be built.</dd>
              <dt>facets_location</dt>
              <dd>Path to where the index facets will be built. This must not be the same value as index_location.</dd>
              <dt>trash_location</dt>
              <dd>Path to where the trash folders will be put. Sometimes files or folders will be created in the repository or index locations which searchcode cannot remove. If found they will be placed into this directory where it is up to a System Administrator to investigate and remove. Usually caused by the immutable bit being set.</dd>
              <dt>check_repo_chages</dt>
              <dd>Interval in seconds to check when repositories will be scanned for changes. Needs to be a number or will default to 600.</dd>
              <dt>check_filerepo_changes</dt>
              <dd>Interval in seconds to check when file path repositories will be scanned for changes. Needs to be a number or will default to 600.</dd>
              <dt>only_localhost</dt>
              <dd>Boolean value true or false. Will only process connections on 127.0.0.1 (not localhost) if set to true and return 204 content not found otherwise. By default set to false.</dd>
              <dt>low_memory</dt>
              <dd>If running searchcode server on a low memory system set this to true. It will use less memory at the expense of indexing time. If set to false you may experience out of memory exceptions if you attempt to index large repositories with insufficient RAM. By default set to false.</dd>
              <dt>spelling_corrector_size</dt>
              <dd>Number of most common "words" to keep for when spell suggesting. When on a memory constrained system it can be advisable to reduce the size. Needs to be a number or will default to 10000.</dd>
              <dt>max_document_queue_size</dt>
              <dd>Maximum number of documents to store in indexing queue. When on a memory constrained system it can be advisable to reduce the size. Needs to be a number or will default to 1000.</dd>
              <dt>max_document_queue_line_size</dt>
              <dd>Maximum number of lines of code to store in indexing queue. This is a soft cap which can be exceeded to allow large documents to be indexed. When on a memory constrained system it can be advisable to reduce the size. 100000 lines equals about 200mb of in memory storage which will be used during the index pipeline. Needs to be a number or will default to 100000.</dd>
              <dt>max_file_line_depth</dt>
              <dd>Maximum number of lines in a file to index. If you want to index very large files set this value to a high number and lower the size of max_document_queue_size to avoid out of memory exceptions. 100000 lines equals about 200mb of in memory storage which will be used during the index pipeline. Needs to be a number or will default to 10000.</dd>
              <dt>use_system_git</dt>
              <dd>If you have git installed on the system you can choose to use external calls to it. This may resolve memory pressure issues but will generally be slower. By default set to false.</dd>
              <dt>git_binary_path</dt>
              <dd>If you enable use_system_git you need to ensure that this equals the path to your git executable for your system. By default set to /usr/bin/git</dd>
              <dt>log_level</dt>
              <dd>What level of logging is requested both to STDOUT and the default log file. Accepts the uppercase values of INFO, WARNING, SEVERE or OFF. A setting of OFF will not even create the log file. The last 1000 records of all logging levels are kept in memory and can be viewed on the Admin Log page. By default set to SEVERE.</dd>
              <dt>log_path</dt>
              <dd>The path to where should logs be written. Can be set to STDOUT and if so all logs that would normally be written to file will be sent to standard output. By default set to ./logs/</dd>
              <dt>log_count</dt>
              <dd>How many rolling log files to keep. By default set to 10.</dd>
              <dt>api_enabled</dt>
              <dd>Boolean value true or false. Should the searchcode server API be enabled. By default set to false.</dd>
              <dt>api_key_authentication</dt>
              <dd>Boolean value true or false. Should the searchcode server API be secured through the use of manually created API keys. If you expose searchcode server publicly and enable the API you should set this to true. By default set to true.</dd>
              <dt>svn_enabled</dt>
              <dd>Boolean value true or false. Will SVN repositories added be crawled and indexed. If you set this value be sure to set svn_binary_path as well. By default set to false.</dd>
              <dt>svn_binary_path</dt>
              <dd>If svn_enabled is set to true you need to ensure that this equals the path to your svn executable for your system. By default set to /usr/bin/svn</dd>
              <dt>owasp_database_location</dt>
              <dd>The location of the JSON owasp database. By default set to ./include/owasp/database.json</dd>
              <dt>classifier_database_location</dt>
              <dd>The location of the JSON file classifier database. By default set to ./include/classifier/database.json</dd>
              <dt>source_database_location</dt>
              <dd>The location of the JSON file source database. By default set to ./include/source/database.json</dd>
              <dt>highlight_lines_limit</dt>
              <dd>The maximum number of lines that will be highlighted by the JavaScript highlighter. Defaults to 3000.</dd>
              <dt>binary_guess</dt>
              <dd>Should searchcode attempt to guess if a file is binary and if so exclude it from the index. Defaults to true.</dd>
              <dt>binary_extension_white_list</dt>
              <dd>A white list of file extensions that if match will always be added to the index. The white list has a higher priority then the blacklist and so if an extension appears in both it will be indexed.</dd>
              <dt>binary_extension_black_list</dt>
              <dd>A black list of file extensions that if match will never be added to the index. The black list has a lower priority then the whitelist and so if an extension appears in both it will be indexed.</dd>
              <dt>directory_black_list</dt>
              <dd>A black list of directories that if match will not be added to the index. Typically used to exclude binary directories such as bin. Example, directory_black_list=bin,target</dd>
              <dt>number_git_processors</dt>
              <dd>Number of background threads to spawn to deal with pulling from and indexing git repositories. Servers with many CPU's should have this value changed to half the number of CPU's. If you increase this value you may need to increase the <a href="#quartz">quartz.properties value see below</a>. Defaults to 2.</dd>
              <dt>number_svn_processors</dt>
              <dd>Number of background threads to spawn to deal with pulling from and indexing svn repositories. Servers with many CPU's should have this value changed to half the number of CPU's. If you increase this value you may need to increase the <a href="#quartz">quartz.properties value see below</a>. Defaults to 2.</dd>
              <dt>number_file_processors</dt>
              <dd>Number of background threads to spawn to deal with pulling from and indexing file repositories.  Servers with many CPU's should have this value changed to half the number of CPU's. If you increase this value you may need to increase the <a href="#quartz">quartz.properties value see below</a>. Defaults to 1.</dd>
              <dt>default_and_match</dt>
              <dd>Should the matching logic default to AND matching where nothing is specified. If set to true all queries will be similar to "import AND junit". If set to false all queries will be similar to "import OR junit". Default logic can be overridden by explicitly adding search operators. Defaults to true.</dd>
              <dt>log_indexed</dt>
              <dd>If set to true a csv containing the results of the last index run will be written to the log directory with the repository name as the filename. Can be used to determine why files are being indexed or not. Defaults to false.</dd>
              <dt>follow_links</dt>
              <dd>Boolean value true or false. If set to true indicates that symbolic links should be followed when indexing using file paths. Can be enabled if required to walk repositories containing symlinks. Be careful, this can produce infinite loops. Defaults to false.</dd>
              <dt>deep_guess_files</dt>
              <dd>Boolean value true or false. If set to true when a file is encountered that cannot be classified though naming conventions its keywords will be analysed and a best guess made. This can be CPU heavy or incorrectly classify some files. Defaults to false.</dd>
              <dt>host_name</dt>
              <dd>String value. Set this to the expected DNS host name for your searchcode server instance. This will allow things like RSS links to work.</dd>
              <dt>index_all_fields</dt>
              <dd>A list of file content that will be added to the all portion of the index. You could use this to exclude filename or paths. Defaults to index_all_fields=content,filename,filenamereverse,path,interesting</dd>
            </dl>
        </p>

        <p id="quartz">
        The quartz.properties file in the base directory should only need to be modified when changing the searchcode.properties values of number_git_processors, number_svn_processors and number_file_processors.
        By default searchcode spawns 10 background threads which are used for repository processing and internal processing logic. By itself searchcode uses 5 threads
        by itself leaving over 5 for background repository processing tasks. If you adjust the number of repository processors higher then you should increase the value for
        org.quartz.threadPool.threadCount to a higher number up-to a maximum of 100.
        </p>

        <h3 id="settings">Settings</h3>

        <p>
        The admin settings page can be used change look and feel settings for searchcode server. Change the settings
        on the page. Changes are applied instantly.
        <#if isCommunity??>
                    <#if isCommunity == true>
                    You are using the community edition of searchcode server. As such you will be unable to change anything here. If you would like the ability to configure the settings page
                    you can purchase a copy at <a href="hhttps://searchcodeserver.com/pricing.html">https://searchcodeserver.com/pricing.html</a>
                    </#if>
        </#if>

          <dl class="dl-horizontal">
            <dt>Logo</dt>
            <dd>The logo that appears on the top left of all searchcode server pages. Should be added as a Base64 encoded image string.</dd>
            <dt>Syntax Highlighter</dt>
            <dd>Change the highlight style for code result pages.</dd>
            <dt>OWASP Advisories</dt>
            <dd>Should OWASP Advisories appear on the code result pages. If set to true code will be scanned using the OWASP database and lines flagged for investigation. Most useful for codebases written using C# and Java.</dd>
            <dt>Average Salary</dt>
            <dd>Used as the base salary for the code display calculation. See <a href="#estimatedcost">estimated cost</a> for more
            details about this value.</dd>
            <dt>Match Lines</dt>
            <dd>Maximum number of lines to find for a search. Increasing this value will display more on search result pages for a given match. Needs to be a number or will default to 15.</dd>
            <dt>Max Line Depth</dt>
            <dd>How many lines into a file should be scanned for matching code. Increasing this value will slow down searches for larger files but is more likely to display the correct lines. Needs to be a number or will default to 10000.</dd>
            <dt>Minified Length</dt>
            <dd>What the average length of lines in a file (ignoring empty) needs to be to mark the file as minified and being excluded from being indexed. Changing this value will affect files as they are re-indexed when the watched repositories change. Needs to be a number or will default to 255.</dd>
            <dt>Backoff Value</dt>
            <dd>Used for controlling the indexer CPU usage. If set to a non zero value it will attempt to keep the CPU load value below the set value. You can view the reported load average on the default admin page. Works off the CPU load averages reported. If you find searchcode to be slow to respond then set this value to half the number of processors. Note that other processes on the machine can affect this value and if set too low will cause the index to never run. Needs to be a number or will default to 0.</dd>
            <dt>Embed</dt>
            <dd>Used to embed HTML/CSS/JS on every page. This allows for custom CSS styles or tracking pixels.</dd>
          </dl>
        </p>

        <h3 id="apikeys">API Keys</h3>

        <p>
        The api key page is used to maintain keys used for authenticated API requests. This page is only relevant if you firstly
        enble the API through properties and then enable authenticated API reqeusts as well. To generate a key click the "Generate New API Key"
        button. A new API key will be created and appear at the bottom of the list. The key consists of two parts. The first portion is the public
        key which is used to identify who is making the request to the API. The second is the private key and should be shared only with the consuming
        application. This key is used to sign the request. To delete a key click the delete button next to the key you wish to remove. Generally it is
        considered good practice to create individual keys for each application using the API.
        </p>

        <h3 id="backups">Backups</h3>
        <p>Generally searchcode server should only need the <b>searchcode.properties</b> and <b>searchcode.sqlite</b> files to be backed up.
        However where many repositories are indexed or when connectivity to source control can be problematic you may want to back up
        the index and repo directories and their contents.</p>

        <h3 id="recovery">Recovery / Restore</h3>
        <p>Assuming you want to recover searchcode you will need to install the application sources. Then copy a backup of the
        <b>searchcode.sqlite</b> and <b>searchcode.properties</b> files into the same directory. When started searchcode will
        analyse the code and rebuild the index. This process will take longer for setups that contain many or large repositories.
        If faster restores are required restore the index and repo directories as well.</p>

        <h3 id="repositories">Repositories</h3>
        <p>
        To index a repository browse to the <a href="/admin/">admin</a> page. Enter a repository name and url for publicly
        available repositories and for private a username and password for a user with enough access to checkout a copy
        of the repository. Repo Source should be a URL that relates to the repository (but can be anything) and will appear
        as a link on the code pages.
        When done click "Add Repo". The repository will be downloaded and indexed as soon as any other
        indexing operations are finished. Note that repository names cannot include a space, and any spaces will be replaced
        with a hyphen character.
        </p>
        <p>
        GIT and SVN repositories are able to be indexed. To enable indexing of SVN repositories set the property value
        svn_enabled to true and svn_binary_path to the path of your SVN executable.
        </p>
        <p id="filerepositories">
        File locations on the machine searchcode server is running on are also able to be indexed. This allows you to index code that is not in a repository or is in a SCM that searchcode server currently does not support. To do so select the file option from the drop down and replace the repository URL with the path on the local machine such as <code>/opt/projects</code> Note that searchcode server needs permission to read the directory, subdirectories and contents of all files otherwise it will crash out with a AccessDeniedException in the logs. There are a few things to note
        <ul>
          <li>You can index any directory on the machine that searchcode server has permissions to read from.</li>
          <li>It is inadvisable to store the file repository in the same location as the property repository_location as it will be removed if a full rebuild operation is triggered.</li>
          <li>Very large directories will need a lot of RAM to index, so consider breaking them up if possible to multiple sub-directories to index.</li>
          <li>If you index the same file in the same path twice only a single result will appear in the results, however deleting may remove the "wrong" file from the index. Try to avoid indexing the same path where possible.</li>
        </ul> 
        </p>
        <p>
        To delete a repository click the delete button at the end of the repository list. This will remove all copies of code
        from disk (not for file repositories however) and the index. This action is not reversible. To undo the operation add the repository again. Note that all delete operations are queued and it may take several minutes for the repository to be removed.
        </p>
        <p>
        Updating the details of a repository will require you to delete the repository, wait for the delete operation to finish and add it again with the new details.
        </p>

        <p>To quickly add a large amount of repositories use the <a href="/admin/bulk/">bulk admin</a> page. This page will only
        allow the adding of repositories using a CSV format with one repository per line. Use the values git, svn or file for the choice of repository.
        </p>
        <p>
        The format for adding follows.<br><br>
        <code>reponame,scm,gitrepolocation,username,password,repourl,branch</code><br><br>

        For example a public repository which does not require username or password<br><br>

        <code>phindex,git,https://github.com/boyter/Phindex.git,,,https://github.com/boyter/Phindex,master</code> <small>*</small><br><br>

        For example a private repository which requires a username and password<br><br>

        <code>searchcode,git,https://searchcode@bitbucket.org/searchcode/hosting.git,myusername,mypassword,https://searchcode@bitbucket.org/searchcode/,master</code><br><br>

        <small>* This is a real repository can can be indexed. Copy paste into the bulk admin page to test.</small>
        </p>

        <h3 id="troubleshooting">Troubleshooting</h3>
        <p>
          <b>A repository is not being indexed?</b><br/>
          Check the console output, you should see something similar to<br />
          <pre>ERROR - caught a class org.eclipse.jgit.api.errors.TransportException with message: https://username@bitbucket.org/username/myrepo.git: not authorized</pre><br />
          This means your username or password for the repository is invalid. Try pulling a copy down locally and replacing the credentials.
        </p>
        <p>
          <b>A file in a repository is not being indexed?</b><br/>
          Files with an average file line length >= 255 are considered minified and will not be indexed. Files that are considered binary will also not be indexed. You should get a message like the ones below on the console saying as such when trying to index the file.<br />
          <pre>Appears to be minified will not index FILENAME</pre>
          <pre>Appears to be binary will not index FILENAME</pre>
        </p>
        <p>
          <b>A repository is not being indexed on Windows</b><br/>
          There are reserved file names on Windows such as CON, PRN, AUX, NUL, COM1, COM2, COM3, COM4, COM5, COM6, COM7, COM8, COM9, LPT1, LPT2, LPT3, LPT4, LPT5, LPT6, LPT7, LPT8, and LPT9.
          If a repository created on another OS contains one of these filenames it is likely that the attempt to clone or checkout will fail. Generally
          it is better to deploy searchcode server on a Unix style OS to avoid this problem. If you are only going to index repositories that
          were created using Windows then Windows is still a valid choice.
        </p>
        <p>
          <b>OutOfMemoryError</b><br/>
          If you are getting the classic java out of memory error such as <br />
          <pre>java.lang.OutOfMemoryError: Java heap space</pre><br />
          There are a few things you can do. Try each one individually with a restart of your searchcode server instance.

          <ol>
            <li>Upgrade the amount of system RAM available on the host system.</li>
            <li>Edit the searchcode-server.sh or searchcode-server.bat file and add the Xmx and Xms arguments.</li>
            <li>Install git and set the searchcode.properties property git_binary_path to the path of your git binary and set use_system_git to true.</li>
            <li>Lower the value of max_document_queue_size.</li>
            <li>Lower the value of max_file_line_depth to be less than the expected length of any file you need to search.</li>
            <li>Set the searchcode.properties property low_memory to true and restart your instance. This should be method of last resort as it will lower memory usage with the impact of less indexing performance.</li>
            <li>Set the searchcode.properties property spelling_corrector_size to a lower number such as 1000.</li>
          </ol>
        </p>

        <p>
          <b>java.io.IOException: Too many open files</b><br/>
          This issue typically occurs on Unix/Linux servers with a low ulimit.
          If you are getting errors like the above you may need to change your ulimit to a higher number as the default
          of 1024 for most systems can be too low.<br />
          Also consider lowering the values for number_git_processors, number_svn_processors and number_file_processors.
        </p>
        <p>
          <b>java.nio.file.AccessDeniedException</b><br/>
          This is usually caused when using the filepath indexing. Usually it means that the user running searchcode server does not have the required permissions to read from the path selection. You will need to set the permissions so that searchcode server has full read rights on the directory. Otherwise it can be cause if the index or repo directories have been denied to searchcode server which requires full read write delete permissions for these directories.
        </p>
        <p>
          <b>How do I index code in Perforce/BitKeeper/Fossil</b><br/>
          You can index code in unsupported repositories by checking out a copy of the repository on disk and adding a <a href="#filerepositories">file repository</a> which is pointed at this location. Suggested methods to keep it in sync would be setting up a cron job or scheduled task to constantly update the repositories.
        </p>
        <p>
          <b>Odd Results</b><br/>
          If you have had an instance that has been running for a long time or that has stopped and started without notice
          the index may need to rebuilt. Click the "Recrawl & Rebuild Indexes" button in the admin pages. This will clear
          the repository and index directories and rebuild everything from scratch which should resolve the issue. Note that
          this process may take some time if you have a lot of repositories or very large ones.
        </p>
        <p>
          <b>Help! Nothing is working!</b><br/>
          Its possible that you may enter a state where nothing is working. In this case save the console output and try
          restarting searchcode. This may resolve the issue. If not, try stopping searchcode and deleting the index and repo directories.
          This will force searchcode server to re-download and re-index. If all else fails contact support.
        </p>

        <h3 id="support">Support</h3>

        <#if isCommunity??>
            <#if isCommunity == true>
            <p>
                You are using the community edition of searchcode server. Sorry but you are own your own. If you would like support (and the ability to configure the settings page)
                you can purchase a copy at <a href="https://searchcodeserver.com/pricing.html">https://searchcodeserver.com/pricing.html</a>
            </p>
            <#else>
            <p>
            To get support for your searchcode server instance email Ben directly at searchcode@boyter.org Please include the following
            information along with the problem you are experiencing.
            <ul>
              <li>Operating system used</li>
              <li>Number of repositories indexed</li>
              <li>Approx size of repositories when checked out</li>
              <li>Java version</li>
              <li>Details from the console output or log screen</li>
              <li>Hardware specifications</li>
            </ul>
            </p>
            </#if>
        </#if>


</div> <!-- end row -->

</@layout.masterTemplate>