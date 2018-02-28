package au.gov.qld.pub.orders.service;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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
    
    public FileAttachment retrieve(Order groupedOrder, NotifyType type, String itemId) throws IOException, InterruptedException {
        LOG.info("Starting downloading attachments for order {} and type {} for item {}", groupedOrder.getId(), type, itemId);
        List<Item> bundled = groupedOrder.getBundledPaidItems();
        Item inBundle = findItemInList(bundled, itemId);
        if (inBundle != null) {
        	String uri = inBundle.getNotifyFormUri(type);
        	if (isNotBlank(uri)) {
		        LOG.info("Downloading attachments for bundled items in order {} and type {}", groupedOrder.getId(), type);
		        return downloadRetrying(createClient(), uri, groupedOrder, bundled, NotifyType.CUSTOMER);
        	}
        }

        List<Item> unbundled = groupedOrder.getUnbundledPaidItems();
        Item inUnbundled = findItemInList(unbundled, itemId);
        if (inUnbundled != null) {
		    String uri = inUnbundled.getNotifyFormUri(type);
		    if (isNotBlank(uri)) {
		        LOG.info("Downloading attachments for item {} and type {}", inUnbundled.getId(), type);
		        return downloadRetrying(createClient(), uri, groupedOrder, asList(inUnbundled), NotifyType.CUSTOMER);
		    }
        }

        LOG.warn("Could not download attachments for item because it was not paid or does not exist in order {}", groupedOrder.getId());
		throw new IllegalStateException();
    }

    public List<FileAttachment> retrieve(Order groupedOrder, NotifyType type) throws IOException, InterruptedException {
        LOG.info("Starting downloading attachments for order {} and type {}", groupedOrder.getId(), type);
        List<FileAttachment> attachments = new ArrayList<>();
        
		List<Item> bundled = groupedOrder.getBundledPaidItems();
        if (bundled.size() > 0) {
        	String uri = bundled.get(0).getNotifyFormUri(type);
        	if (isNotBlank(uri)) {
		        LOG.info("Downloading attachments for bundled items in order {} and type {}", groupedOrder.getId(), type);
		        attachments.add(downloadRetrying(createClient(), uri, groupedOrder, bundled, type));
		    }
        }

        List<Item> unbundled = groupedOrder.getUnbundledPaidItems();
		for (Item item : unbundled) {
		    String uri = item.getNotifyFormUri(type);
		    if (isNotBlank(uri)) {
		        LOG.info("Downloading attachments for item {} and type {}", item.getId(), type);
		        attachments.add(downloadRetrying(createClient(), uri, groupedOrder, asList(item), type));
		    }
		}
		
		return attachments;
    }
    
    private Item findItemInList(List<Item> items, String id) {
        for (Item item : items) {
        	if (id.equals(item.getId())) {
        		return item;
        	}
        }
        return null;
    }

	private FileAttachment downloadRetrying(HttpClient client, String uri, Order order, List<Item> items, NotifyType type) throws IOException, InterruptedException {
        HttpPost httpPost = createRequest(uri, order, items);
        for (int attempt=0; attempt < retryCount; attempt++) {
            try {
                return new FileAttachment(firstFilename(items, type), download(client, httpPost));
            } catch(IOException e) {
                LOG.error(e.getMessage(), e);
                Thread.sleep(retryWait);
            }
        }
        
        throw new IOException("Retries exhausted for bundled items in order" + order.getId() + " to uri " + uri);
    }

    private String firstFilename(List<Item> items, NotifyType type) {
    	for (Item item : items) {
    		String filename = NotifyType.BUSINESS.equals(type) ? item.getNotifyBusinessFormFilename() : item.getNotifyCustomerFormFilename();
    		if (isNotBlank(filename)) {
    			return filename;
    		}
    	}
		throw new IllegalStateException("Could not get a filename for any items which have attachments");
	}

	private byte[] download(HttpClient client, HttpPost httpPost) throws IOException {
        CloseableHttpResponse response = (CloseableHttpResponse) client.execute(httpPost);
        
        if (response.getStatusLine().getStatusCode() != OKAY_STATUS_CODE) {
        	response.close();
            throw new IOException("Could not download attachment: " + response.getStatusLine().getStatusCode() + ", " + response.getStatusLine().getReasonPhrase());
        }
        
        HttpEntity entity = response.getEntity();
        byte[] data = IOUtils.toByteArray(entity.getContent());
        EntityUtils.consume(entity);
        response.close();
        return data;
    }

    private HttpPost createRequest(String uri, Order order, List<Item> items) throws UnsupportedEncodingException {
        HttpPost httpPost = new HttpPost(uri);
        
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.addAll(createItemPostData(items.get(0), ""));
        if (items.size() > 1) {
        	for (int i=1; i < items.size(); i++) {
        		nvps.addAll(createItemPostData(items.get(i), "-" + i));	
        	}
        }
        
        nvps.add(createField("paid", order.getPaid()));
        nvps.add(createField("receipt", order.getReceipt()));

        httpPost.setEntity(new UrlEncodedFormEntity(nvps));
        return httpPost;
    }

	private List<NameValuePair> createItemPostData(Item item, String suffix) {
		List<NameValuePair> nvps = new ArrayList<>();
        for (Map.Entry<String, String> field : item.getFieldsMap().entrySet()) {
            nvps.add(createField(field.getKey() + suffix, field.getValue()));
        }

        nvps.add(createField("quantityPaid" + suffix, item.getQuantityPaid()));
        nvps.add(createField("productGroup" + suffix, item.getProductGroup()));
        nvps.add(createField("productId" + suffix, item.getProductId()));
        nvps.add(createField("priceTotal" + suffix, String.valueOf(Long.parseLong(item.getPriceGst()) + Long.parseLong(item.getPriceExGst()))));
        nvps.add(createField("priceGst" + suffix, item.getPriceGst()));
        nvps.add(createField("priceExGst" + suffix, item.getPriceExGst()));
		return nvps;
	}

    private NameValuePair createField(String name, String value) {
        return new BasicNameValuePair(name, defaultString(value).trim());
    }

    protected HttpClient createClient() {
        RequestConfig config = RequestConfig.custom().setSocketTimeout(timeout).setConnectTimeout(timeout).build();
        return HttpClients.custom().setDefaultRequestConfig(config).build();
    }

}
