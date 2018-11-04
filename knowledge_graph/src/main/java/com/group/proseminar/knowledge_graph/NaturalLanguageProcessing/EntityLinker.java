package com.group.proseminar.knowledge_graph.NaturalLanguageProcessing;

import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.xml.sax.SAXException;

/**
 * Links entities to their DBPedia URI with respect to their mentions and
 * DBPedia class.
 *
 */
public class EntityLinker {
	private HttpClient client;
	private SAXParserFactory factory;
	private SAXParser parser;
	private final String DBLOOKUP = "http://lookup.dbpedia.org/api/search.asmx/KeywordSearch?";
	private final String QUERYSTRING = "QueryString=";
	private final String QUERYCLASS = "QueryClass=";

	public static void main(String[] args) {
		try {
			Entity christiano = new Entity(Stream.of("Christiano", "Christiano Ronaldo").collect(Collectors.toList()));
			Entity obama = new Entity(Stream.of("Barack", "Obama", "Barack Obama").collect(Collectors.toList()));
			Entity place = new Entity(Stream.of("qqqqqqqqqqqqqqqqqqqqq", "Gauß").collect(Collectors.toList()));

			Set<Entity> entities = new HashSet<>();
			entities.add(christiano);
			entities.add(obama);
			entities.add(place);

			EntityLinker linker = new EntityLinker();
			linker.resolveURIs(entities);
			for (Entity en : entities) {
				System.out.println(en.getBestMention() + " " + en.getUri());
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public EntityLinker() throws Exception {
		this.client = new HttpClient();
		this.factory = SAXParserFactory.newInstance();
		this.parser = this.factory.newSAXParser();
	}

	/**
	 * Links entities to an URI retrieved from DBPedia.
	 * @param entities
	 * @throws Exception
	 */
	public void resolveURIs(Set<Entity> entities) throws Exception {
		for (Entity entity : entities) {
			ArrayList<String> mentions = new ArrayList<String>();
			// Insert best mention first
			mentions.add(entity.getBestMention());
			mentions.addAll(entity.getMentions());
			int index = 0;
			while (entity.getUri() == null && index < mentions.size()) {
				String mention = mentions.get(index);
				String get = DBLOOKUP + QUERYSTRING + URLEncoder.encode(mention, "UTF-8");
				if (entity.getLabel() != null) {
					get += "&" + QUERYCLASS + entity.getLabel();
				}
				HttpMethod request = new GetMethod(get);
				try {
					client.executeMethod(request);
					InputStream input = request.getResponseBodyAsStream();
					SAXHandler handler = new SAXHandler();
					try {
						parser.parse(input, handler);
					} catch (SAXException e) {
						String response = handler.getResponse();
						if (response != null) {
							// If an URI is found for this mention, assign it to be the best mention
							entity.setUri(response);
							entity.setBestMention(mention);
						}
					}

				} catch (HttpException e) {
					System.err.println("Unable to connect to DBPedia.");
				} finally {
					request.releaseConnection();
				}
				index++;
			}
		}
	}
}