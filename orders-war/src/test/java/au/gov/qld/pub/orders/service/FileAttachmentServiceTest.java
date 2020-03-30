package au.gov.qld.pub.orders.service;

import static com.google.common.collect.ImmutableMap.of;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import javax.mail.internet.MimeMessage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mail.javamail.MimeMessageHelper;

import au.gov.qld.pub.orders.dao.FormFileDAO;
import au.gov.qld.pub.orders.entity.FormFile;

@RunWith(MockitoJUnitRunner.class)
public class FileAttachmentServiceTest {
	private static final String FILE_ID_0 = "file id 0";
	private static final String FILE_ID_1 = "file id 1";
	private static final String FILE_ID_2 = "file id 2";
	private static final String FILE_ID_3 = "file id 3";
	
	FileAttachmentService service;
	
	@Mock FileService fileService;
	@Mock FormFileDAO dao;
	@Mock FormFile formFile;
	@Mock MimeMessage message;
	@Mock MimeMessageHelper helper;
	
	List<Map<String, String>> paidItemsFields;
	
	@Before
	public void setUp() {
		paidItemsFields = asList((Map<String, String>)of("fileId0", FILE_ID_0, "fileId1", FILE_ID_1, "fileId2", FILE_ID_2, "fileId3", FILE_ID_3));
		service = new FileAttachmentService(fileService);
	}
	
	@Test
	public void attachFilesToMessageForBusiness() throws Exception {
		when(formFile.getName()).thenReturn("some name");
		when(formFile.getData()).thenReturn("some data".getBytes());
		when(fileService.find(FILE_ID_0)).thenReturn(formFile);
		when(fileService.find(FILE_ID_1)).thenReturn(formFile);
		when(fileService.find(FILE_ID_2)).thenReturn(formFile);
		when(fileService.find(FILE_ID_3)).thenReturn(formFile);
		service.append(message, helper, false, paidItemsFields);
	}

	@Test
	public void doNotAttachFilesToMessageForCustomer() throws Exception {
		service.append(message, helper, true, paidItemsFields);
		verifyZeroInteractions(fileService);
	}
}
