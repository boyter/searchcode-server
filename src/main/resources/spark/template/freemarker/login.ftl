<#import "masterTemplate.ftl" as layout />
<@layout.masterTemplate title="Admin">


    <form action="/login/" method="post" class="form-signin">
    	<h4>Enter Password</h4>
	   	<input type="password" class="form-control" placeholder="Password" name="password" size="30"><br />
	   	<div class="actions"><input class="btn btn-success btn-block" type="submit" value="Sign In"></div>
	   	<small><a href="/documentation/#properties">What's my password?</a></small>
	 </form>

	<#if passwordInvalid?? ><div class="alert alert-danger" role="alert"><span class="glyphicon glyphicon-warning-sign"></span> You appear to have used an invalid password or have not configured your <a href="/documentation/#properties" class="alert-link">properties file</a> correctly.</div></#if>

</@layout.masterTemplate>