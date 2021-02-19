package au.gov.qld.pub.orders.config.filter;

import org.apache.commons.codec.binary.Base64;
import org.assertj.core.util.Sets;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;

import static au.gov.qld.pub.orders.config.filter.CspInterceptor.CSP_HEADER;
import static au.gov.qld.pub.orders.config.filter.CspInterceptor.CSP_NEL_HEADER;
import static au.gov.qld.pub.orders.config.filter.CspInterceptor.CSP_NONCE_ATTRIB;
import static au.gov.qld.pub.orders.config.filter.CspInterceptor.CSP_REPORT_ONLY_HEADER;
import static au.gov.qld.pub.orders.config.filter.CspInterceptor.CSP_REPORT_TO_HEADER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class CspInterceptorTest {

    private CspInterceptor cspInterceptor;

    @Before
    public void setUp() {
        cspInterceptor = new CspInterceptor(false, "filter", "reportTo", "nel");
    }

    @Test
    public void generateNonce_isBase64Encoded() {
        // given
        int numberOfGenerationsToTest = 10000;

        for (int i = 0; i < numberOfGenerationsToTest; i++) {
            // when
            String nonce = cspInterceptor.generateNonce();

            // then
            assertThat(Base64.isBase64(nonce)).isTrue();
        }
    }

    @Test
    public void generateNonce_meetsLengthRequirements() {
        // given
        int numberOfGenerationsToTest = 10000;

        for (int i = 0; i < numberOfGenerationsToTest; i++) {
            // when
            String nonce = cspInterceptor.generateNonce();

            // then
            assertThat(nonce.length()).isGreaterThanOrEqualTo(32);
        }
    }

    @Test
    public void generateNonce_generatesConsistentlyUniqueValues() {
        // given
        int numberOfGenerationsToTest = 10000;
        Set<String> existingNonceValues = Sets.newHashSet();

        for (int i = 0; i < numberOfGenerationsToTest; i++) {
            // when
            existingNonceValues.add(cspInterceptor.generateNonce());
        }

        // then
        assertThat(existingNonceValues).hasSize(numberOfGenerationsToTest);
    }

    @Test
    public void populateCspFilterWithNonce_injectsNonce() {
        // given
        String cspFilterTemplate = sampleCspFilterTemplate();
        String nonce = cspInterceptor.generateNonce();

        // when
        String cspFilterWithInjectedNonce = cspInterceptor.populateCspFilterWithNonce(cspFilterTemplate, CSP_NONCE_ATTRIB, nonce);

        // then
        assertThat(cspFilterWithInjectedNonce).isNotEqualTo(cspFilterTemplate).contains(nonce);
    }

    @Test
    public void preHandle_addsCspReportOnlyHeaderWhenNotEnforcing() throws Exception {
        // given
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        Object mockHandler = mock(Object.class);

        // when
        cspInterceptor.preHandle(mockRequest, mockResponse, mockHandler);

        // then
        verify(mockResponse).setHeader(eq(CSP_REPORT_ONLY_HEADER), anyString());
        verify(mockResponse).setHeader(eq(CSP_REPORT_TO_HEADER), anyString());
        verify(mockResponse).setHeader(eq(CSP_NEL_HEADER), anyString());
        verifyZeroInteractions(mockHandler);
    }

    @Test
    public void preHandle_addsCspHeaderWhenEnforcing() throws Exception {
        // given
        ReflectionTestUtils.setField(cspInterceptor, "cspEnforce", true);
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        Object mockHandler = mock(Object.class);

        // when
        cspInterceptor.preHandle(mockRequest, mockResponse, mockHandler);

        // then
        verify(mockResponse).setHeader(eq(CSP_HEADER), anyString());
        verify(mockResponse).setHeader(eq(CSP_REPORT_TO_HEADER), anyString());
        verify(mockResponse).setHeader(eq(CSP_NEL_HEADER), anyString());
    }

    @Test
    public void preHandle_addsCspNonceToRequest() throws Exception {
        // given
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        Object mockHandler = mock(Object.class);

        // when
        cspInterceptor.preHandle(mockRequest, mockResponse, mockHandler);

        // then
        verify(mockRequest).setAttribute(eq(CSP_NONCE_ATTRIB), anyString());
    }

    private String sampleCspFilterTemplate() {
        return "Content-Security-Policy: default-src 'nonce-[" + CSP_NONCE_ATTRIB + "]' 'strict-dynamic' 'self' ...";
    }

}
