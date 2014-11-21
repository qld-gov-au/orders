<head>
<title>Confirm test</title>
</head>

<form action="${context}/add" method="post">
	<ul class="actions">
		<li>
			<strong>
				<input type="submit" value="Add" id="add" />
			</strong>
		</li>
	</ul>
	
	<input type="hidden" name="productId" value="${fields['some-field-for-product1']!""}"/>
	<input type="hidden" name="productId" value="${fields['some-field-for-product2']!""}"/>
	
	<#list fields?keys as key>
		<input type="hidden" name="${key}" value="${fields[key]!""}"/>
	</#list>
	
	<script type="text/javascript"><!--
	document.write('<input type="hidden" name="ssqCartId" value="' + SSQ.cart.id + '" />');
	// --></script>
</form>