<head>
<title>Example home</title>
</head>

<content tag="top_nav">
<li>Top nav item</li>
</content>

<content tag="left_nav">
<li>Left nav item</li>
</content>

<form action="confirm" method="post">
    Group: <input type="text" name="group" id="group" value="testgroup"/><br/>    
    Product ID 1: <input type="text" name="some-field-for-product1" id="productId" value="test"/><br/>
    Product ID 2: <input type="text" name="some-field-for-product2" id="productId" value="test2"/><br/>
    Field 1: <input type="text" name="field1" id="field1" value="field1"/><br/>
    Field 2: <input type="text" name="field2" id="field2" value="field2"/><br/>
    
    <ul class="actions">
        <li>
            <strong>
                <input type="submit" value="Order" id="confirm" />
            </strong>
        </li>
    </ul>
    
</form>

<hr/>
<form action="pay-in-full" method="post">
    <input type="text" value="123" name="sourceId" id="sourceId" />
    <input type="text" value="${sourceUrl}" name="sourceUrl" id="sourceUrl" />
    
    <ul class="actions">
        <li>
            <strong>
                <input type="submit" value="Notice to pay" id="noticeToPay" />
            </strong>
        </li>
    </ul>
</form>
