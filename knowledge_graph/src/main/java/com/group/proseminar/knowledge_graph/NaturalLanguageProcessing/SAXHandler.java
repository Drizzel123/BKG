package com.group.proseminar.knowledge_graph.NaturalLanguageProcessing;

import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SAXHandler extends DefaultHandler {

	private String response;
	private Stack<String> elements;

	public SAXHandler() {
		this.elements = new Stack<>();
		this.response = null;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		this.elements.push(qName);
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		this.elements.pop();
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		String value = new String(ch, start, length).trim();

		if (value.length() == 0) {
			return;
		}

		if ("URI".equals(currentElement())) {
			this.response = value;
			throw new SAXException("Extracted URI");
		}
	}

	private String currentElement() {
		return this.elements.peek();
	}

	public String getResponse() {
		return response;
	}
}
