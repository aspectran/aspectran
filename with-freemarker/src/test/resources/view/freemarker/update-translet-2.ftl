UPDATE table1
<@directive.set>
    <#if name??>
    name = :name,
    </#if>
    <#if email??>
    email = :email,
    </#if>
    <#if id??>
    id = :id
    </#if>
</@directive.set>
