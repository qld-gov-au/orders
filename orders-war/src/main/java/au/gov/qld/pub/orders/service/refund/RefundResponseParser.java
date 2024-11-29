package au.gov.qld.pub.orders.service.refund;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import au.gov.qld.pub.orders.service.refund.dto.LineItem;
import au.gov.qld.pub.orders.service.refund.dto.RefundQueryResponse;
import au.gov.qld.pub.orders.service.refund.dto.RefundRequestResponse;

@Component
public class RefundResponseParser {
	private static final Logger LOG = LoggerFactory.getLogger(RefundResponseParser.class);

	public RefundQueryResponse parseQueryResponse(String wsResponse) {
		RefundQueryResponse response = new RefundQueryResponse();
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(new ByteArrayInputStream(wsResponse.getBytes(StandardCharsets.UTF_8)));

			XPath xPath =  XPathFactory.newInstance().newXPath();
			NodeList nodeList = (NodeList) xPath.compile("//lineItem").evaluate(doc, XPathConstants.NODESET);

			for (int i=0; i < nodeList.getLength(); i++) {
				LineItem lineItem = new LineItem();
				Element node = (Element) nodeList.item(i);
				lineItem.setPapiLineItemId(Integer.parseInt(node.getElementsByTagName("papiLineItemId").item(0).getTextContent()));
				lineItem.setQuatity(Integer.parseInt(node.getElementsByTagName("quantity").item(0).getTextContent()));
				lineItem.setOrderLineId(node.getElementsByTagName("orderLineId").item(0).getTextContent());
				lineItem.setAgencyReference(node.getElementsByTagName("agencyReference").item(0).getTextContent());
				response.getLineItem().add(lineItem);
			}
		} catch (SAXException | IOException | ParserConfigurationException | XPathExpressionException e) {
			LOG.warn("Unable to parse: {}", wsResponse);
			throw new IllegalArgumentException(e.getMessage(), e);
		}

		return response;
	}

	public RefundRequestResponse parseRequestResponse(String wsResponse) {
		RefundRequestResponse response = new RefundRequestResponse();
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(new ByteArrayInputStream(wsResponse.getBytes(StandardCharsets.UTF_8)));

			XPath xPath =  XPathFactory.newInstance().newXPath();
			// namespace sometimes in response
			Element refundResponse = (Element) xPath.compile("//*[local-name()='errorMessage']").evaluate(doc, XPathConstants.NODE);
			response.setErrorMessage(refundResponse.getTextContent());
		} catch (SAXException | IOException | ParserConfigurationException | XPathExpressionException e) {
			LOG.warn("Unable to parse: {}", wsResponse);
			throw new IllegalArgumentException(e.getMessage(), e);
		}

		return response;
	}

}
