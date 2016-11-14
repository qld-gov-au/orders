package au.gov.qld.pub.orders.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.ByteArrayResource;


public class EmailAttachment {
	private final String name;
	private final ByteArrayResource data;

	public EmailAttachment(String name, ByteArrayOutputStream data) throws IOException {
		this.name = name;
		this.data = new ByteArrayResource(data.toByteArray());
		data.flush();
		data.close();
	}

	public AbstractResource getData() {
		return data;
	}
	
	public String getName() {
		return name;
	}
}
