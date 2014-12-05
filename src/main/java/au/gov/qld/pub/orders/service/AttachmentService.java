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
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import au.gov.qld.pub.orders.entity.Item;
import au.gov.qld.pub.orders.entity.Order;

@Service
public class AttachmentService {
	private static final Logger LOG = LoggerFactory.getLogger(AttachmentService.class);
	public static final int OKAY_STATUS_CODE = 200;

	public Map<String, byte[]> retrieve(Order order, NotifyType type) throws IOException {
		LOG.info("Fetching attachments");
		Map<String, byte[]> attachments = new HashMap<String, byte[]>();
		HttpClient client = createClient();
		
		for (Item item : order.getItems()) {
			if (!item.isPaid()) {
				continue;
			}
			
			String uri = NotifyType.BUSINESS == type ? item.getNotifyBusinessFormUri() : item.getNotifyCustomerFormUri();
			String filename = NotifyType.BUSINESS == type ? item.getNotifyBusinessFormFilename() : item.getNotifyCustomerFormFilename();
			if (isNotBlank(uri)) {
				attachments.put(filename, download(client, uri, order, item));
			}
		}
		
		return attachments;
	}

	private byte[] download(HttpClient client, String uri, Order order, Item item) throws IOException {
		HttpPost httpPost = createRequest(uri, order, item);
		CloseableHttpResponse response = (CloseableHttpResponse) client.execute(httpPost);
		
		if (response.getStatusLine().getStatusCode() != OKAY_STATUS_CODE) {
			throw new IOException("Could not download attachment: " + response.getStatusLine().getStatusCode() + ", " + response.getStatusLine().getReasonPhrase());
		}
		
		HttpEntity entity = response.getEntity();
		ByteArrayOutputStream memory = new ByteArrayOutputStream();
		IOUtils.copy(entity.getContent(), memory);
	    EntityUtils.consume(entity);
		return memory.toByteArray();
	}

	private HttpPost createRequest(String uri, Order order, Item item) throws UnsupportedEncodingException {
		HttpPost httpPost = new HttpPost(uri);
		
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(createField("item-quantityPaid", item.getQuantityPaid()));
		for (Map.Entry<String, String> field : item.getFieldsMap().entrySet()) {
			nvps.add(createField("item-" + field.getKey(), field.getValue()));
		}
		
		nvps.add(createField("order-paid", order.getPaid()));
		nvps.add(createField("order-receipt", order.getReceipt()));

		httpPost.setEntity(new UrlEncodedFormEntity(nvps));
		return httpPost;
	}

	private NameValuePair createField(String name, String value) {
		return new BasicNameValuePair(name, defaultString(value).trim());
	}

	protected HttpClient createClient() {
		return HttpClients.createDefault();
	}

}
