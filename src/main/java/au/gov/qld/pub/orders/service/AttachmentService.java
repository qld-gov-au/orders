package au.gov.qld.pub.orders.service;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import au.gov.qld.pub.orders.entity.Item;
import au.gov.qld.pub.orders.entity.Order;

@Service
public class AttachmentService {
	private static final Logger LOG = LoggerFactory.getLogger(AttachmentService.class);
	public static final int OKAY_STATUS_CODE = 200;
	
	private final int retryCount;
    private final int retryWait;
    private final int timeout;
	
	@Autowired
	public AttachmentService(ConfigurationService config) {
		this.retryCount = config.getNotifyFormRetryCount();
		this.retryWait = config.getNotifyFormRetryWait();
		this.timeout = config.getNotifyFormTimeout();
	}

	public Map<String, byte[]> retrieve(Order order, NotifyType type) throws IOException, InterruptedException {
		LOG.info("Starting downloading attachments for order {} and type {}", order.getId(), type);
		Map<String, byte[]> attachments = new HashMap<String, byte[]>();
		HttpClient client = createClient();
		
		for (Item item : order.getItems()) {
			if (!item.isPaid()) {
				continue;
			}
			
			LOG.info("Downloading attachments for item {} and type {}", item.getId(), type);
			String uri = NotifyType.BUSINESS == type ? item.getNotifyBusinessFormUri() : item.getNotifyCustomerFormUri();
			if (isNotBlank(uri)) {
				attachments.put(item.getId(), downloadRetrying(client, uri, order, item));
			}
		}
		
		return attachments;
	}
	
	private byte[] downloadRetrying(HttpClient client, String uri, Order order, Item item) throws IOException, InterruptedException {
		HttpPost httpPost = createRequest(uri, order, item);
		for (int attempt=0; attempt < retryCount; attempt++) {
			try {
				return download(client, httpPost);
			} catch(IOException e) {
				LOG.error(e.getMessage(), e);
				Thread.sleep(retryWait);
			}
		}
		
		throw new IOException("Retries exhausted for " + order.getId() + " with item " + item.getId() + " to uri " + uri);
	}

	private byte[] download(HttpClient client, HttpPost httpPost) throws IOException {
		CloseableHttpResponse response = (CloseableHttpResponse) client.execute(httpPost);
		
		if (response.getStatusLine().getStatusCode() != OKAY_STATUS_CODE) {
			throw new IOException("Could not download attachment: " + response.getStatusLine().getStatusCode() + ", " + response.getStatusLine().getReasonPhrase());
		}
		
		HttpEntity entity = response.getEntity();
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		IOUtils.copy(entity.getContent(), output);
	    EntityUtils.consume(entity);
		return output.toByteArray();
	}

	private HttpPost createRequest(String uri, Order order, Item item) throws UnsupportedEncodingException {
		HttpPost httpPost = new HttpPost(uri);
		
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(createField("quantityPaid", item.getQuantityPaid()));
		for (Map.Entry<String, String> field : item.getFieldsMap().entrySet()) {
			nvps.add(createField(field.getKey(), field.getValue()));
		}
		
		nvps.add(createField("paid", order.getPaid()));
		nvps.add(createField("receipt", order.getReceipt()));

		httpPost.setEntity(new UrlEncodedFormEntity(nvps));
		return httpPost;
	}

	private NameValuePair createField(String name, String value) {
		return new BasicNameValuePair(name, defaultString(value).trim());
	}

	protected HttpClient createClient() {
	    RequestConfig config = RequestConfig.custom().setSocketTimeout(timeout).setConnectTimeout(timeout).build();
		return HttpClients.custom().setDefaultRequestConfig(config).build();
	}

}
