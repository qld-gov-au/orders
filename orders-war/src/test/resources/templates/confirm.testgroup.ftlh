<#ftl strip_whitespace=true>
<#import "macro/page.ftlh" as page>
<#global title = "Confirm test"/>

<#global head>

</#global>

<#global footer_script>

</#global>

<@page.page>

<content tag="top_nav">
<li>Top nav item</li>
</content>

<content tag="left_nav">
<li>Left nav item</li>
</content>


<form action="${context}/add" method="post">
    <ul class="actions">
        <li>
            <strong>
                <input type="submit" value="Add" id="add" />
            </strong>
        </li>
    </ul>
    
    <#if fileIds?has_content>
        <#list fileIds as fileId>
            <input type="text" id="fileId-${fileId}" name="fileId${fileId_index}" value="${fileId}" />
        </#list>
    </#if>    
    <input type="hidden" name="productId" value="${fields['some-field-for-product1']!""}"/>
    <input type="hidden" name="productId" value="${fields['some-field-for-product2']!""}"/>
    
    <#list fields?keys as key>
        <input type="hidden" name="${key}" value="${fields[key]!""}"/>
    </#list>
    
    <script nonce="${__csp_nonce}" type="text/javascript"><!--
    document.write('<input type="hidden" name="ssqCartId" value="' + SSQ.cart.id + '" />');
    // --></script>
</form>

</@page.page>