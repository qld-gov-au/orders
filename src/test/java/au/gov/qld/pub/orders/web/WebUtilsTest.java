package au.gov.qld.pub.orders.web;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import javax.servlet.http.Cookie;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.springframework.web.servlet.view.RedirectView;

public class WebUtilsTest {
	@Test
	public void redirectWithoutUrlEffected() {
		RedirectView redirect = WebUtils.redirect("something");
		assertThat(redirect.getUrl(), is("something"));
		assertThat(redirect.isExposePathVariables(), is(false));
	}
	
	public static Matcher<Cookie> cookieWith(final String name, final String value) {
		return new BaseMatcher<Cookie>() {
			@Override
			public boolean matches(Object arg0) {
				Cookie cookie = (Cookie)arg0;
				return name.equals(cookie.getName()) && value.equals(cookie.getValue());
			}

			@Override
			public void describeTo(Description arg0) {
			}
		};
	}
}
