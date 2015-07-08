package au.gov.qld.pub.orders.service.ws;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.qld.pub.orders.service.ServiceException;

public class SOAPClient {
    private static final Logger LOG = LoggerFactory.getLogger(SOAPClient.class);
    private static final String CREATED_FMT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static final Pattern SOAP_RESP_BODY_PATTERN = Pattern.compile("<SOAP-ENV:Body>(.+)</SOAP-ENV:Body>");

    private static final String SOAP_MSG_FMT =
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
        "<soapenv:Envelope xmlns=\"@NAMESPACE@\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
            "<soapenv:Header>" +
                "<wsse:Security soapenv:mustUnderstand=\"1\" xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">" +
                    "<wsse:UsernameToken xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">" +
                        "<wsse:Username>@USERNAME@</wsse:Username>" +
                        "<wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest\">@PASSWORD@</wsse:Password>" +
                        "<wsse:Nonce EncodingType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary\">@NONCE@</wsse:Nonce>" +
                        "<wsu:Created>@CREATED@</wsu:Created>" +
                    "</wsse:UsernameToken>" +
                "</wsse:Security>" +
            "</soapenv:Header>" +
            "<soapenv:Body>@BODY@</soapenv:Body></soapenv:Envelope>";

    private final SecureRandom nonceRandom;
    private final String endpoint;

    public SOAPClient(String endpoint) {
        this.endpoint = endpoint;
        this.nonceRandom = new SecureRandom();
    }

    public String sendRequest(String username, byte[] password, String namespace, String request) throws ServiceException {
        try {
            String payload = buildSOAPPayload(username, password, namespace).replace("@BODY@", request);

            LOG.debug("Built payload: {}", payload);

            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(endpoint);
            httpPost.setHeader("Content-Type", "text/xml");
            httpPost.setEntity(new StringEntity(payload));
            CloseableHttpResponse response = httpclient.execute(httpPost);
            
            try {
                if (response.getStatusLine().getStatusCode() != 200) {
                    LOG.debug("Failed to send: {}", payload);
                    throw new IOException("Could not connect - HTTP Status code: " + response.getStatusLine().getStatusCode() + " (" + response.getStatusLine().getReasonPhrase() + ")");
                }
                
                return extractBody(EntityUtils.toString(response.getEntity()));
            } finally {
                response.close();
            }
            
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new ServiceException(e);
        }
    }

    private String extractBody(String result) {
        Matcher body = SOAP_RESP_BODY_PATTERN.matcher(result);
        if (!body.find()) {
            throw new IllegalArgumentException("Result does not match expected format");
        }
        return body.group(1);
    }

    private String buildSOAPPayload(String username, byte[] password, String namespace) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        String nonceStr = String.valueOf(nonceRandom.nextInt(999999999));
        String created = getCreated();
        byte[] wssePassword = concat((nonceStr + created).getBytes("UTF-8"), password);
        String encPassword = Base64.encodeBase64String(DigestUtils.sha1(wssePassword));

        String payload = SOAP_MSG_FMT.replace("@USERNAME@", username);
        payload = payload.replace("@PASSWORD@", encPassword);
        payload = payload.replace("@CREATED@", created);
        payload = payload.replace("@NONCE@", Base64.encodeBase64String(nonceStr.getBytes("UTF-8")));
        payload = payload.replace("@NAMESPACE@", namespace);
        return payload;
    }

    private static byte[] concat(byte[] first, byte[] second) {
      byte[] result = Arrays.copyOf(first, first.length + second.length);
      System.arraycopy(second, 0, result, first.length, second.length);
      return result;
    }

    private static String getCreated() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(CREATED_FMT);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(new Date());
    }
}
