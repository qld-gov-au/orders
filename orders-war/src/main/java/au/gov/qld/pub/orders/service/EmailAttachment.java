package au.gov.qld.pub.orders.service;

public class EmailAttachment {
	private final String name;
	private final byte[] data;

	public EmailAttachment(String name, byte[] data) {
		this.name = name;
		this.data = data;
	}

	public byte[] getData() {
		return data;
	}
	
	public String getName() {
		return name;
	}
}
