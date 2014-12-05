package au.gov.qld.pub.orders.service;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import au.gov.qld.pub.orders.entity.Order;

@Service
public class AttachmentService {
	private static final Logger LOG = LoggerFactory.getLogger(AttachmentService.class);

	public Map<String, InputStream> retrieve(Order order, NotifyType type) {
		LOG.info("Fetching attachments");
		return Collections.EMPTY_MAP;
	}

}
