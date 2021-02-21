package au.gov.qld.pub.orders.service;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.multipart.MultipartFile;

import au.gov.qld.pub.orders.dao.FormFileDAO;
import au.gov.qld.pub.orders.entity.FormFile;

@RunWith(MockitoJUnitRunner.class)
public class FileServiceTest {
	private static final String SAVED_FORM_FILE_ID = "some form file id";
	private static final String SUPPORTED_FILENAME = "some file.another supported";
	private static final int DELETE_UPLOAD_AGE = 10;
	private byte[] DATA = "some data".getBytes();
	@Mock MultipartFile upload;
	
	FileService service;
	@Mock FormFileDAO dao;
	@Mock FormFile savedFormFile;
	@Mock FileValidator validator;
	
	@Before
	public void setUp() throws Exception {
		DateTimeUtils.setCurrentMillisFixed(new Date().getTime());//prevent milli second differences breaking tests
		when(upload.getSize()).thenReturn((long)DATA.length);
		when(upload.getBytes()).thenReturn(DATA);
		when(upload.getOriginalFilename()).thenReturn(SUPPORTED_FILENAME);
		service = new FileService(dao, DELETE_UPLOAD_AGE, asList(validator, validator));
	}
	
	@After
	public void tearDown() {
		DateTimeUtils.setCurrentMillisSystem();
	}
	
	@Test
	public void notSaveWhenValidationError() throws IOException, ValidationException {
		doThrow(new ValidationException("bogus")).when(validator).validate(anyString(), anyLong(), anyObject());
		try {
			service.save(asList(upload));
			fail();
		} catch (ValidationException e) {
		}
		verifyNoInteractions(dao);
	}
	
	@Test
	public void cleanupFile() {
		Date createdAt = new LocalDate().minusDays(DELETE_UPLOAD_AGE).toDate();
		when(dao.findIdByCreatedAtBefore(createdAt)).thenReturn(asList(SAVED_FORM_FILE_ID));
		service.cleanUp();
		verify(dao).deleteById(SAVED_FORM_FILE_ID);
	}
	
	@Test
	public void saveFile() throws Exception {
		Matcher<FormFile> formFileWithData = hasProperty("data", equalTo(DATA));
		when(savedFormFile.getId()).thenReturn(SAVED_FORM_FILE_ID);
		when(dao.save(argThat(formFileWithData))).thenReturn(savedFormFile);
		
		assertThat(service.save(asList(upload)), is(asList(SAVED_FORM_FILE_ID)));
	}
	
	
}