package au.gov.qld.pub.orders.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class EmailAttachment {
	private final String name;
	private final InputStream data;

	public EmailAttachment(String name, ByteArrayOutputStream data) throws IOException {
		this.name = name;
		this.data = new ByteArrayInputStream(data.toByteArray());
		data.flush();
		data.close();
	}

	public InputStream getData() {
		return data;
	}
	
	public String getName() {
		return name;
	}
}
