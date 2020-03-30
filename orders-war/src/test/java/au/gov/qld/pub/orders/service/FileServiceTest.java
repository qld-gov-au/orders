package au.gov.qld.pub.orders.service;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Date;

import org.hamcrest.Matcher;
import org.joda.time.DateTimeUtils;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.multipart.MultipartFile;

import au.gov.qld.pub.orders.dao.FormFileDAO;
import au.gov.qld.pub.orders.entity.FormFile;

@RunWith(MockitoJUnitRunner.class)
public class FileServiceTest {
	private static final String SAVED_FORM_FILE_ID = "some form file id";
	private static final String SUPPORTED_TYPES = "supported;another supported";
	private static final long MAX_UPLOAD_SIZE = 123;
	private static final String SUPPORTED_FILENAME = "some file.another supported";
	private static final int DELETE_UPLOAD_AGE = 10;
	private byte[] DATA = "some data".getBytes();
	@Mock MultipartFile upload;
	
	FileService service;
	@Mock FormFileDAO dao;
	@Mock FormFile savedFormFile;
	
	@Before
	public void setUp() throws Exception {
		DateTimeUtils.setCurrentMillisFixed(new Date().getTime());//prevent milli second differences breaking tests
		when(upload.getSize()).thenReturn((long)DATA.length);
		when(upload.getBytes()).thenReturn(DATA);
		when(upload.getOriginalFilename()).thenReturn(SUPPORTED_FILENAME);
		service = new FileService(dao, SUPPORTED_TYPES, MAX_UPLOAD_SIZE, DELETE_UPLOAD_AGE);
	}
	
	@After
	public void tearDown() {
		DateTimeUtils.setCurrentMillisSystem();
	}
	
	@Test
	public void cleanupFile() {
		Date createdAt = new LocalDate().minusDays(DELETE_UPLOAD_AGE).toDate();
		when(dao.findIdByCreatedAtBefore(createdAt)).thenReturn(asList(SAVED_FORM_FILE_ID));
		service.cleanUp();
		verify(dao).delete(SAVED_FORM_FILE_ID);
	}
	
	@Test
	public void saveFile() throws Exception {
		Matcher<FormFile> formFileWithData = hasProperty("data", equalTo(DATA));
		when(savedFormFile.getId()).thenReturn(SAVED_FORM_FILE_ID);
		when(dao.save(argThat(formFileWithData))).thenReturn(savedFormFile);
		
		assertThat(service.save(asList(upload)), is(asList(SAVED_FORM_FILE_ID)));
	}
	
	@Test
	public void throwExceptionWhenFileTooBig() throws IOException {
		try {
			when(upload.getSize()).thenReturn(MAX_UPLOAD_SIZE + 1);
			service.save(asList(upload));
			fail();
		} catch (ValidationException e) {
			assertThat(e.getMessage(), containsString("too big"));
		}
	}
	
	@Test
	public void throwExceptionWhenFileTypeInvalid() throws IOException {
		try {
			when(upload.getOriginalFilename()).thenReturn("bogus file.bogus extension");
			service.save(asList(upload));
			fail();
		} catch (ValidationException e) {
			assertThat(e.getMessage(), containsString("invalid type"));
		}
	}
}