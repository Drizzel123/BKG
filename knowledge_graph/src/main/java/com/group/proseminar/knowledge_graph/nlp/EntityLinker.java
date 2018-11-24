package com.group.proseminar.knowledge_graph.nlp;

import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

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
 * @author Stefan Werner
 */
public class EntityLinker {
	private HttpClient client;
	private SAXParserFactory factory;
	private SAXParser parser;
	private final String DBLOOKUP = "http://lookup.dbpedia.org/api/search.asmx/KeywordSearch?";
	private final String QUERYSTRING = "QueryString=";
	private final String QUERYCLASS = "QueryClass=";

	public EntityLinker() throws Exception {
		this.client = new HttpClient();
		this.factory = SAXParserFactory.newInstance();
		this.parser = this.factory.newSAXParser();
	}

	/**
	 * Links entities to an URI retrieved from DBPedia.
	 * 
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
			// Try to resolve mentions to an URI starting from the first to the last mention
			while (entity.getUri() == null && index < mentions.size()) {
				String mention = mentions.get(index).replace(" ","_");
				String get = DBLOOKUP + QUERYSTRING + URLEncoder.encode(mention, "UTF-8");
				// Add the label to the query if possible (excluding label "Literal")
				String label = entity.getLabel();
				if (label != null && label != "Literal") {
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
	
	/**
	 * Matches a mention to the largest entity found within.
	 * @param mention - mention to be resolved to an entity
	 * @return largest entity within mention, or null if none is found
	 */
	public Entity getLargestEntity(Set<Entity> entities, String mention) {
		List<String> words = new ArrayList<>(Arrays.asList(mention.split(" ")));
		int index = words.size();
		Entity largest = null;
		while (index > 0 && largest == null) {
			int difference = words.size() - index;
			for (int i = 0; i <= difference; i++) {
				String candidate = String.join(" ", words.subList(i, i + index));
				largest = entities.stream().filter(x -> x.getMentions().contains(candidate)).findFirst().orElse(null);
			}
			index--;
		}
		return largest;
	}
}