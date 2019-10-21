package au.gov.qld.pub.orders.service;

public class Applicant {

	private final String name;
	private final String addressLine1;
	private final String suburb;
	private final String state;
	private final String postcode;
	private final String country;

	public Applicant(String name, String addressLine1, String suburb, String state, String postcode, String country) {
		this.name = name;
		this.addressLine1 = addressLine1;
		this.suburb = suburb;
		this.state = state;
		this.postcode = postcode;
		this.country = country;
	}

	public String getName() {
		return name;
	}

	public String getAddressLine1() {
		return addressLine1;
	}

	public String getSuburb() {
		return suburb;
	}

	public String getState() {
		return state;
	}

	public String getPostcode() {
		return postcode;
	}

	public String getCountry() {
		return country;
	}
	
}
