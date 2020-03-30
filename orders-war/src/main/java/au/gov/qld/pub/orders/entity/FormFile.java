package au.gov.qld.pub.orders.entity;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class FormFile {
	@Id
	private String id;

	private byte[] data;

	private String name;

	private String contentType;
	
	@Column(name = "created_at")
	private Date createdAt;
	
	private FormFile() {
		this.createdAt = new Date();
	}
	
	public FormFile(String name, byte[] data, String contentType) {
		this();
		this.createdAt = new Date();
		this.contentType = contentType;
		this.name = name.trim();
		this.id = UUID.randomUUID().toString();
		this.data = data;
	}

	public String getId() {
		return id;
	}

	public byte[] getData() {
		return data;
	}

	public String getName() {
		return name;
	}

	public String getContentType() {
		return contentType;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
}