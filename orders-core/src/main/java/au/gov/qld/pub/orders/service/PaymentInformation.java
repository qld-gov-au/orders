package au.gov.qld.pub.orders.service;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.List;

// Warning: do not put anything sensitive in this unless you know your source ID cannot be guessed.
public class PaymentInformation {
    private static final int MAX_CENTS_BEFORE_TAX_REQUIRED = 1000 * 100;
	private final String reference;
    private final String description;
	private final List<OrderInformation> orderInformation;
	private final Applicant applicant;
	private final boolean taxDetailsRequired;

    public PaymentInformation(String reference, String description, List<OrderInformation> orderInformation, Applicant applicant) {
        this.reference = reference;
        this.description = description;
		this.orderInformation = orderInformation;
		this.applicant = applicant;
		
		long sum = orderInformation.stream().mapToLong(OrderInformation::getTotal).sum();
		this.taxDetailsRequired = sum >= MAX_CENTS_BEFORE_TAX_REQUIRED;
		if (taxDetailsRequired && isBlank(applicant.getName())) {
			throw new IllegalArgumentException("NTP requires registered name for taxable amount: " + sum);
		}
    }
    
    public boolean isIncludeTaxDetails() {
    	return taxDetailsRequired;
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

	public Applicant getApplicant() {
		return applicant;
	}
}
