-------------- Tesing for FreeMarkerViewDispatcher --------------
<#list fruits>
<p>Fruits: ${fruits?join(', ')}</p>
<ul>
    <#items as fruit>
    <li>${fruit}<#sep> and</#sep></li>
    </#items>
</ul>
<#else>
<p>We have no fruits.</p>
</#list>
-------------- Testing for FreeMarkerViewDispatcher --------------