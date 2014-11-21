Hey business!

Here's an email for the items that a customer bought with receipt: ${order.receipt} at ${order.paid}:
<#list order.items as item>
<#if item.paid>
	productId: ${item.productId}
	title: ${item.title}
	quantity: ${item.quantityPaid}
	
	With your fields:
	<#list item.fieldsMap?keys as key>
		Field: ${key} - ${item.fieldsMap[key]}
	</#list>
</#if>
</#list>