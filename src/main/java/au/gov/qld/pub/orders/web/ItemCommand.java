package au.gov.qld.bdm.orders.web;

import java.util.List;

public class ItemCommand {

	private List<String> productIds;
	
	public void setProductId(List<String> productId) {
		this.productIds = productId;
	}
	
	public List<String> getProductId() {
		return productIds;
	}

}
