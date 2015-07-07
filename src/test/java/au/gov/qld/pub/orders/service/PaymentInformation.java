package au.gov.qld.pub.orders.service;

public class PaymentInformation {
	private final long amountOwingInCents;
	private final long amountOwingGstInCents;
	private final String reference;
	private final String description;

	public PaymentInformation(String reference, String description, long amountOwingInCents, long amountOwingGstInCents) {
		this.reference = reference;
		this.description = description;
		this.amountOwingInCents = amountOwingInCents;
		this.amountOwingGstInCents = amountOwingGstInCents;
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
}
