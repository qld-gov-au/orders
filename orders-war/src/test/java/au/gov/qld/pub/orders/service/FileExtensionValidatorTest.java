package au.gov.qld.pub.orders.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;

public class FileExtensionValidatorTest {
	@Test
	public void throwExceptionWhenFileTypeInvalid() throws IOException, ValidationException {
		FileExtensionValidator validator = new FileExtensionValidator("a");
		validator.validate("test.a", 123);
		
		try {
			validator.validate("test.b", 123);
			fail();
		} catch (ValidationException e) {
			assertThat(e.getMessage(), containsString("invalid type"));
		}
	}
}
