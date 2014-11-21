<head>
<title>Added to cart</title>
</head>

<h1>Order</h1>
<#if cartId?has_content>
<div class="status success">
<h2></h2>
Product(s) have been added to your shopping basket.
<br>
<br>
</div>

<br>
You can proceed to the checkout or continue shopping.

<br>
<br>
<div class="actions">
    <strong>
        <a class="button" href="${papiBase}/minicart/synchronise?cartId=${cartId}&redirectUrl=cart/checkout" id="checkout">Checkout</a>
    </strong>
    <a class="button" style="float: right;" href="${sourceUrl}" id="continue">Continue shopping</a>
</div>
</#if>
