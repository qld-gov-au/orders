package au.gov.qld.pub.orders.service.ws;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Component
public class CartResponseParser {

	public String getReceipt(String xml) {
		Document document = documentForXml(xml);
		XPathFactory pathFactory = XPathFactory.newInstance();
		Node node = getXPathNode(pathFactory, "//receiptNumber", document);
		return node != null ? node.getTextContent() : null;
	}

	public OrderDetails getPaidOrderDetails(String xml) {
		Document document = documentForXml(xml);
		XPathFactory pathFactory = XPathFactory.newInstance();
		
		NodeList orderlineNodes = getXPathNodeList(pathFactory, "//orderline", document);
		Map<String, String> orderlineQuantities = new HashMap<String, String>();
		for (int i=0; i < orderlineNodes.getLength(); i++) {
			Node node = orderlineNodes.item(i);
			String id = node.getAttributes().getNamedItem("id").getTextContent();
			String quantity = node.getAttributes().getNamedItem("quantity").getTextContent();
			orderlineQuantities.put(id, quantity);
		}
		
		NodeList deliveryDetailsNodes = getXPathNodeList(pathFactory, "//deliveryAddress/detail", document);
		NodeList customerDetailsNodes = getXPathNodeList(pathFactory, "//customerDetail/detail", document);
		
		OrderDetails orderDetails = new OrderDetails();
		orderDetails.setDeliveryDetails(getDetailsMap(deliveryDetailsNodes));
		orderDetails.setCustomerDetails(getDetailsMap(customerDetailsNodes));
		orderDetails.setOrderlineQuantities(orderlineQuantities);
		return orderDetails;
	}

	private Map<String, String> getDetailsMap(NodeList nodes) {
		Map<String, String> details = new HashMap<String, String>();
		for (int i=0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			String type = node.getAttributes().getNamedItem("type").getTextContent();
			String value = node.getTextContent();
			details.put(type, value);
		}
		return details;
	}
	
	private Node getXPathNode(XPathFactory pathFactory, String xpath, Document document) {
		try {
			XPathExpression expression = pathFactory.newXPath().compile(xpath);
			return (Node)expression.evaluate(document, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
	
	private NodeList getXPathNodeList(XPathFactory pathFactory, String xpath, Document document) {
		try {
			XPathExpression expression = pathFactory.newXPath().compile(xpath);
			return (NodeList)expression.evaluate(document, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	private Document documentForXml(String xml) {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			return builder.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
		} catch (Exception e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		}
	}

}
