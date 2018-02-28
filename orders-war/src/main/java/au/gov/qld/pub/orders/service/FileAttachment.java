package au.gov.qld.pub.orders.service;

import java.io.IOException;


public class FileAttachment {
	private final String name;
	private final byte[] data;

	public FileAttachment(String name, byte[] data) throws IOException {
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
