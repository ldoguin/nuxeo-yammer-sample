<@extends src="base.ftl">
<@block name="content">

<#if nsp>
<div style="margin: 10px 10px 10px 10px">
<div id="oauthServiceDescription">
  <p>You are currently trying to access the following service:</p>
  <p>${nsp.serviceName} - ${nsp.gadgetUrl}</p>
  <p>${nsp.description}</p>
</div>
<p>Do you want to authorize Nuxeo to access this service?</p>

 <a href="${Root.path}/authorize?${queryString}" target="_blank">Authorize</a>
 <a href="#" onclick="win = top;

win.opener = top;

win.close ();
">close</a>


<#include "@enter_code">

</div>

<#else>
  <p>Cannot find the requested service provider.</p>
</#if>

</@block>
</@extends>