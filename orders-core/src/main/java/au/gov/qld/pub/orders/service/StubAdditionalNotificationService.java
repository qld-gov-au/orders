package au.gov.qld.pub.orders.service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;


public class StubAdditionalNotificationService implements AdditionalNotificationService {

	@Override
	public void notifedPaidOrder(String id, LocalDateTime created, String paid,
								 String receipt, String cartId,
								 Map<String, String> customerDetailsMap,
								 Map<String, String> deliveryDetailsMap,
								 List<Map<String, String>> fieldMaps) {
	}

	@Override
	public void notifedPaidNoticeToPay(String id, LocalDateTime notifiedAt,
			String receiptNumber, String description, String paymentInformationId) {
	}

}
