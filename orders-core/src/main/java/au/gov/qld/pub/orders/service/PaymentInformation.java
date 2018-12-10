package au.gov.qld.pub.orders.service;

import java.util.List;

// Warning: do not put anything sensitive in this unless you know your source ID cannot be guessed.
public class PaymentInformation {
    private final String reference;
    private final String description;
	private final List<OrderInformation> orderInformation;

    public PaymentInformation(String reference, String description, List<OrderInformation> orderInformation) {
        this.reference = reference;
        this.description = description;
		this.orderInformation = orderInformation;
    }

    public String getReference() {
        return reference;
    }

    public String getDescription() {
        return description;
    }

	public List<OrderInformation> getOrderInformation() {
		return orderInformation;
	}

	public long getAmountOwingInCents() {
		long amount = 0;
		for (OrderInformation order : orderInformation) {
			amount += order.getTotal() + order.getGst();
		}
		return amount;
	}
	
	public long getAmountOwingGstInCents() {
		long amount = 0;
		for (OrderInformation order : orderInformation) {
			amount += order.getGst();
		}
		return amount;
	}
}
