package au.gov.qld.pub.orders.service;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.MimeMessageHelper;

import au.gov.qld.pub.orders.entity.FormFile;

public class FileAttachmentService implements AdditionalMailContentService {
	private final FileService fileService;

	@Autowired
	public FileAttachmentService(FileService fileService) {
		this.fileService = fileService;
	}
	
	@Override
	public void append(MimeMessage message, MimeMessageHelper helper, boolean customerEmail, List<Map<String, String>> paidItemsFields) throws MessagingException {
		if (customerEmail) {
			return;
		}
		
		List<FormFile> formFiles = getFormFiles(paidItemsFields);
		for (FormFile formFile : formFiles) {
			helper.addAttachment(formFile.getName(), new ByteArrayResource(formFile.getData()), formFile.getContentType());
		}
	}

	private List<FormFile> getFormFiles(List<Map<String, String>> paidItemsFields) {
		List<FormFile> formFiles = new ArrayList<>();
		for (Map<String, String> paidItemFields : paidItemsFields) {
			for (int i=0; i < 4; i++) {
				String fileId = paidItemFields.get("fileId" + i);
				if (isNotBlank(fileId)) {
					formFiles.add(fileService.find(fileId));
				}
			}
		}
		return formFiles;
	}

}
