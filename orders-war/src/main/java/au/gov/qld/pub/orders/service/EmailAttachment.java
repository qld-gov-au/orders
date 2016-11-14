package au.gov.qld.pub.orders.service;


public class EmailAttachment {
	private final String name;
	private final byte[] data;

	public EmailAttachment(String name, byte[] data) {
		this.name = name;
		this.data = data;
	}

	public byte[] getData() {
		return clone(data);
	}
	
	public String getName() {
		return name;
	}
	
    private static byte[] clone(byte[] data) {
        if (data == null) {
            return null;
        }
        byte[] copy = new byte[data.length];
        System.arraycopy(data, 0, copy, 0, data.length);
        return copy;
    }
}
