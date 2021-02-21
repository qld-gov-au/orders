package au.gov.qld.pub.orders.web;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import au.gov.qld.pub.orders.service.FileService;
import au.gov.qld.pub.orders.service.ValidationException;

@RunWith(MockitoJUnitRunner.class)
public class OrderWithFileControllerTest {
	private static final List<String> FIELDS = Arrays.asList("dueYear","dueMonth","dueDay","debt-under-25000","originate-qld","parties-agreement","location","amount-owing","due-date","claim-interest","claim-interest","agree-to-rate","agree-to-rate","agreed-rate","interest-amount","reason","interpreter","interpreter","specify-language","res1-type","res1-givennames","res1-surname","res1-type","res1-organisation","res1-organisationnumber","res1-address1","res1-suburb","res1-state","res1-postcode","second-respondent","res2-type","res2-givennames","res2-surname","res2-type","res2-organisation","res2-organisationnumber","res2-address1","res2-suburb","res2-state","res2-postcode","third-respondent","res3-type","res3-givennames","res3-surname","res3-type","res3-organisation","res3-organisationnumber","res3-address1","res3-suburb","res3-state","res3-postcode","fourth-respondent","res4-type","res4-givennames","res4-surname","res4-type","res4-organisation","res4-organisationnumber","res4-address1","res4-suburb","res4-state","res4-postcode","fifth-respondent","res5-type","res5-givennames","res5-surname","res5-type","res5-organisation","res5-organisationnumber","res5-address1","res5-suburb","res5-state","res5-postcode","app1-type","app1-givennames","app1-surname","app1-type","app1-organisation","app1-organisationnumber","app1-address1","app1-suburb","app1-state","app1-postcode","second-applicant","app2-type","app2-givennames","app2-surname","app2-type","app2-organisation","app2-organisationnumber","app2-address1","app2-suburb","app2-state","app2-postcode","third-applicant","app3-type","app3-givennames","app3-surname","app3-type","app3-organisation","app3-organisationnumber","app3-address1","app3-suburb","app3-state","app3-postcode","fourth-applicant","app4-type","app4-givennames","app4-surname","app4-type","app4-organisation","app4-organisationnumber","app4-address1","app4-suburb","app4-state","app4-postcode","fifth-applicant","app5-type","app5-givennames","app5-surname","app5-type","app5-organisation","app5-organisationnumber","app5-address1","app5-suburb","app5-state","app5-postcode","conditions-of-use");
	private static final String GROUP = "some group";
	OrderWithFileController controller;
	@Mock FileService fileService;
	MockHttpServletRequest request;
	Collection<OrderValidator> validators;
	@Mock OrderValidator validator;
	@Mock MultipartFile file;
	List<MultipartFile> emptyUploads;
	
	@Before
	public void setUp() {
		validators = asList(validator);
		request = new MockHttpServletRequest();
		for (String field : FIELDS) {
			request.setParameter(field, RandomStringUtils.randomAlphabetic(10));
		}
		request.setParameter("dueYear", "2016");
		request.setParameter("dueMonth", "12");
		request.setParameter("dueDay", "31");
		when(validator.getProductGroup()).thenReturn(GROUP);
		controller = new OrderWithFileController(fileService, validators);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void submitWithNoFiles() throws Exception {
		ModelAndView mav = controller.confirmWithFile(GROUP, emptyUploads, null, null, null, null, request);
		assertThat(mav.getViewName(), is("confirm." + GROUP));
		verify(validator).validate(eq((Collection)Collections.emptyList()), (Map<String, String>) argThat(hasEntry("dueYear", "2016")));
	}

	@SuppressWarnings({ "unchecked" })
	@Test
	public void submitWithOptionalFiles() throws Exception {
		ModelAndView mav = controller.confirmWithFile(GROUP, emptyUploads, null, file, null, null, request);
		assertThat(mav.getViewName(), is("confirm." + GROUP));
		verify(validator).validate(eq(asList(file)), (Map<String, String>) argThat(hasEntry("dueYear", "2016")));
	}
	
	@SuppressWarnings({ "unchecked" })
	@Test
	public void submitWithOptionalFilesCombinedWithList() throws Exception {
		ModelAndView mav = controller.confirmWithFile(GROUP, asList(file), null, file, null, null, request);
		assertThat(mav.getViewName(), is("confirm." + GROUP));
		verify(validator).validate(eq(asList(file, file)), (Map<String, String>) argThat(hasEntry("dueYear", "2016")));
	}
	
	@SuppressWarnings({ "unchecked" })
	@Test
	public void submitWithListOfUploads() throws Exception {
		ModelAndView mav = controller.confirmWithFile(GROUP, asList(file), null, null, null, null, request);
		assertThat(mav.getViewName(), is("confirm." + GROUP));
		verify(validator).validate(eq(asList(file)), (Map<String, String>) argThat(hasEntry("dueYear", "2016")));
	}
	
	@Test(expected = ValidationException.class)
	public void throwExceptionWhenTooManyUploads() throws Exception {
		controller.confirmWithFile(GROUP, listOf(file, 11), null, null, null, null, request);
	}
	
	@Test(expected = ValidationException.class)
	public void throwExceptionWhenTooManyUploadsIncludingAdditionals() throws Exception {
		controller.confirmWithFile(GROUP, listOf(file, 7), file, file, file, file, request);
	}
	
	@Test
	public void submitWithNoValidator() throws Exception {
		String group = "no validator on this group";
		ModelAndView mav = controller.confirmWithFile(group, emptyUploads, null, file, null, null, request);
		assertThat(mav.getViewName(), is("confirm." + group));
	}
	
	@Test(expected = ValidationException.class)
	public void throwExceptionWhenNoGroup() throws Exception {
		controller.confirmWithFile(null, emptyUploads, null, file, null, null, request);
	}
	
	@Test(expected = ValidationException.class)
	public void throwExceptionWhenGroupTooLong() throws Exception {
		controller.confirmWithFile(StringUtils.repeat("a", 1000), emptyUploads, null, file, null, null, request);
	}
	
	private List<MultipartFile> listOf(MultipartFile file, int count) {
		List<MultipartFile> files = new ArrayList<>();
		for (int i=0; i < count; i++) {
			files.add(file);
		}
		return files;
	}
}