from xml.dom import minidom
import re
import json


def addkeywords(existingnames, keywords, description, results, language):
    for key in keywords:
        if key not in existingnames:
            existingnames.append(key)
            results.append({ 'name': key, 'desc': description, 'type': '', 'lang': language})
    return results


# XML is taken from OWASP Code Crawler which is taken from OWASP so must be under the https://creativecommons.org/licenses/by-sa/3.0/
# Creative Commons 3.0 License. To avoid any issues lets convert it to JSON and then ship with that
xmldoc = minidom.parse('owasp_database.xml')
itemlist = xmldoc.getElementsByTagName('KeyPointer')

results = []

for s in itemlist:
    name = s.getElementsByTagName('k_name')[0].firstChild.nodeValue
    desc = s.getElementsByTagName('k_description')[0].firstChild.nodeValue.strip()
    # Ugly hacks ahoy!
    vunl = ','.join(set([x.strip() for x in re.sub('<.*?>', '', s.getElementsByTagName('Stride')[0].toprettyxml()).split('\n') if x.strip() != '']))

    language = ''

    if 'java' in name.lower() or 'java' in desc.lower():
        language = 'java'
    if '.net' in name.lower() or '.net' in desc.lower() or 'microsoft' in name.lower() or 'microsof' in desc.lower():
        language = 'c#'

    results.append({ 'name': name, 'desc': desc, 'type': vunl, 'lang': language})


existingnames = [x['name'] for x in results]


keywords = '''password'''.split(',')
desc = '''It is inadvisable to embed passwords in source code.'''
results = addkeywords(existingnames, keywords, desc, results, '')

keywords = '''HtmlEncode,URLEncode,<applet>,<frameset>,<embed>,<frame>,<iframe>,<img>,<style>,<layer>,<ilayer>,<meta>,<object>,<body>,<frame security,<iframe security '''.split(',')
desc = '''Many of the HTML tags below can be used for client side attacks such as cross site scripting. It is important to examine the context in which these tags are used and to examine any relevant data validation associated with the display and use of such tags within a web application.'''
results = addkeywords(existingnames, keywords, desc, results, 'HTML')

keywords = '''response.write,<%=,HttpUtility,HtmlEncode,UrlEncode,innerText,innerHTML'''.split(',')
desc = '''Responses which go unvalidated or which echo external input without data validation are key areas to examine. Many client side attacks result from poor response validation. XSS relies on this somewhat.'''
results = addkeywords(existingnames, keywords, desc, results, '')

keywords = '''exec sp_executesql,execute sp_executesql,select from,Insert,update,delete from where,delete,exec sp_,execute sp_,exec xp_,execute sp_,exec @,execute @,executestatement,executeSQL,setfilter,executeQuery,GetQueryResultInXML,adodb,sqloledb,sql server,driver,Server.CreateObject,.Provider,.Open,ADODB.recordset,New OleDbConnection,ExecuteReader,DataSource,SqlCommand,Microsoft.Jet,SqlDataReader,ExecuteReader,GetString,SqlDataAdapter,CommandType,StoredProcedure,System.Data.sql'''.split(',')
desc = '''Locating where a database may be involved in the code is an important aspect of the code review. Looking at the database code will help determine if the application is vulnerable to SQL injection. One aspect of this is to verify that the code uses either SqlParameter, OleDbParameter, or OdbcParameter(System.Data.SqlClient). These are typed and treat parameters as the literal value and not executable code in the database.'''
results = addkeywords(existingnames, keywords, desc, results, '')

keywords = '''eval(,document.cookie,document.referrer,document.attachEvent,document.body,document.body.innerHtml,document.body.innerText,document.close,document.create,document.createElement,document.execCommand,document.forms[0].action,document.location,document.open,document.URL,document.URLUnencoded,document.write,document.writeln,location.hash,location.href,location.search,window.alert,window.attachEvent,window.createRequest,window.execScript,window.location,window.open,window.navigate,window.setInterval,window.setTimeout,XMLHTTP,request.accepttypes,request.browser,request.files,request.headers,request.httpmethod,request.item,request.querystring,request.form ,request.cookies,request.certificate,request.rawurl,request.servervariables,request.url,request.urlreferrer,request.useragent,request.userlanguages,request.IsSecureConnection,request.TotalBytes,request.BinaryRead,InputStream,HiddenField.Value,TextBox.Text,recordSet'''.split(',')
desc = '''Ajax and JavaScript have brought functionality back to the client side, which has brought a number of old security issues back to the forefront. The following keywords relate to API calls used to manipulate user state or the control the browser. The event of AJAX and other Web 2.0 paradigms has pushed security concerns back to the client side, but not excluding traditional server side security concerns.'''
results = addkeywords(existingnames, keywords, desc, results, 'javascript')

keywords = '''Java.io,java.util.zip,java.util.jar,FileInputStream,ObjectInputStream,FilterInputStream,PipedInputStream,SequenceInputStream,StringBufferInputStream,BufferedReader,ByteArrayInputStream,CharArrayReader,File,ObjectInputStream,PipedInputStream,StreamTokenizer,getResourceAsStream,java.io.FileReader,java.io.FileWriter,java.io.RandomAccessFile,java.io.File,java.io.FileOutputStream,mkdir,renameTo,java.io.File.delete,java.io.File.listFiles,java.io.File.list'''.split(',')
desc = '''These are used to read data into one's application. They may be potential entry points into an application. The entry points may be from an external source and must be investigated. These may also be used in path traversal attacks or DoS attacks.'''
results = addkeywords(existingnames, keywords, desc, results, 'java')

keywords = '''javax.servlet.*,getParameterNames,getParameterValues,getParameter,getParameterMap,getScheme,getProtocol,getContentType,getServerName,getRemoteAddr,getRemoteHost,getRealPath,getLocalName,getAttribute,getAttributeNames,getLocalAddr,getAuthType,getRemoteUser,getCookies,isSecure,HttpServletRequest,getQueryString,getHeaderNames,getHeaders,getPrincipal,getUserPrincipal,isUserInRole,getInputStream,getOutputStream,getWriter,addCookie,addHeader,setHeader,setAttribute,putValue,javax.servlet.http.Cookie,getName,getPath,getDomain,getComment,getMethod,getPath,getReader,getRealPath,getRequestURI,getRequestURL,getServerName,getValue,getValueNames,getRequestedSessionId'''.split(',')
desc = '''These API calls may be avenues for parameter, header, URL, and cookie tampering, HTTP Response Splitting and information leakage. They should be examined closely as many of such APIs obtain the parameters directly from HTTP requests.'''
results = addkeywords(existingnames, keywords, desc, results, 'java')

keywords = '''javax.servlet.ServletOutputStream.print,javax.servlet.jsp.JspWriter.print,java.io.PrintWriter.print'''.split(',')
desc = '''These API calls may be susceptable to Cross Site Scripting attacks.'''
results = addkeywords(existingnames, keywords, desc, results, 'java')

keywords = '''jdbc,executeQuery,select,insert,update,delete,execute,executestatement,createStatement,java.sql.ResultSet.getString,java.sql.ResultSet.getObject,java.sql.Statement.executeUpdate,java.sql.Statement.executeQuery,java.sql.Statement.execute,java.sql.Statement.addBatch,java.sql.Connection.prepareStatement,java.sql.Connection.prepareCall'''.split(',')
desc = '''Searching for Java Database related code this list should help you pinpoint classes/methods which are involved in the persistence layer of the application being reviewed.'''
results = addkeywords(existingnames, keywords, desc, results, 'java')

keywords = '''com.sun.net.ssl,SSLContext,SSLSocketFactory,TrustManagerFactory,HttpsURLConnection,KeyManagerFactory'''.split(',')
desc = '''Looking for code which utilises SSL as a medium for point to point encryption. The following fragments should indicate where SSL functionality has been developed.'''
results = addkeywords(existingnames, keywords, desc, results, 'java')

keywords = '''java.lang.Runtime.exec,java.lang.Runtime.getRuntime'''.split(',')
desc = '''Here we may be vulnerable to command injection attacks or OS injection attacks. Java linking to the native OS can cause serious issues and potentially give rise to total server compromise.'''
results = addkeywords(existingnames, keywords, desc, results, 'java')

keywords = '''java.io.PrintStream.write,log4j,jLo,Lumberjack,MonoLog,qflog,just4log,log4Ant,JDLabAgent'''.split(',')
desc = '''We may come across some information leakage by examining code below contained in one's application.'''
results = addkeywords(existingnames, keywords, desc, results, 'java')

keywords = '''java.lang.ClassLoader.defineClass,java.net.URLClassLoader,java.beans.Instrospector.getBeanInfo,System.load,System.loadLibrary,Runtime.exec,ProcessBuilder,javax.script.ScriptEngine.eval'''.split(',')
desc = '''The following may lead to arbitrary java code execution if source of input data is entrusted or not validated.'''
results = addkeywords(existingnames, keywords, desc, results, 'java')

print json.dumps(results)