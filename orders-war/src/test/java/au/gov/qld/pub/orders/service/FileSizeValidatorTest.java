package au.gov.qld.pub.orders.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class FileSizeValidatorTest {
	@Test
	public void throwExceptionWhenFileTooBig() throws IOException, ValidationException {
		FileSizeValidator validator = new FileSizeValidator(123);
		validator.validate("anything", 123, null);
		try {

			validator.validate("anything", 124, null);
			fail();
		} catch (ValidationException e) {
			assertThat(e.getMessage(), containsString("too big"));
		}
	}
}
