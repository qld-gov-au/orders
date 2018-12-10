package au.gov.qld.pub.orders.service;

import java.util.Date;
import java.util.List;
import java.util.Map;


public class StubAdditionalNotificationService implements AdditionalNotificationService {

	@Override
	public void notifedPaidOrder(String id, Date created, String paid,
			String receipt, String cartId,
			Map<String, String> customerDetailsMap,
			Map<String, String> deliveryDetailsMap,
			List<Map<String, String>> fieldMaps) {
	}

	@Override
	public void notifedPaidNoticeToPay(String id, Date notifiedAt,
			String receiptNumber, String description, String paymentInformationId) {
	}

}
