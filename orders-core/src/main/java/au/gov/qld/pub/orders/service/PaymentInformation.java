package au.gov.qld.pub.orders.service;

import java.util.List;

// Warning: do not put anything sensitive in this unless you know your source ID cannot be guessed.
public class PaymentInformation {
    private final long amountOwingInCents;
    private final long amountOwingGstInCents;
    private final String reference;
    private final String description;
	private final List<OrderInformation> orderInformation;

    public PaymentInformation(String reference, String description, long amountOwingInCents, long amountOwingGstInCents, List<OrderInformation> orderInformation) {
        this.reference = reference;
        this.description = description;
        this.amountOwingInCents = amountOwingInCents;
        this.amountOwingGstInCents = amountOwingGstInCents;
		this.orderInformation = orderInformation;
    }

	public long getAmountOwingInCents() {
        return amountOwingInCents;
    }

    public long getAmountOwingGstInCents() {
        return amountOwingGstInCents;
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
}
