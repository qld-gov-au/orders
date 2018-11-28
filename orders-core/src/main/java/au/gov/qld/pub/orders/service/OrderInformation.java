package au.gov.qld.pub.orders.service;

public class OrderInformation {

	private final String product;
	private final long total;
	private final long gst;
	private final int quantity;

	public OrderInformation(String product, long total, long gst, int quantity) {
		this.product = product;
		this.total = total;
		this.gst = gst;
		this.quantity = quantity;
	}

	public String getProduct() {
		return product;
	}

	public long getTotal() {
		return total;
	}

	public long getGst() {
		return gst;
	}

	public int getQuantity() {
		return quantity;
	}

}
