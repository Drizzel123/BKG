package com.group.proseminar.knowledge_graph.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import org.json.JSONException;
import org.json.JSONObject;

class DataFetcher extends Thread {
	private String url;
	private Controller controller;

	DataFetcher(String ontology, Controller controller) {
		this.controller = controller;
		url = "http://dbpedia.org/sparql?default-graph-uri=http%3A%2F%2Fdbpedia.org&query=SELECT+*+%7B%3Fs+a+dbo%3A"
				+ ontology
				+ ".+%3Fs+dbo%3Aabstract+%3Fabs.+FILTER%28lang%28%3Fabs%29%3D%22en%22%29%7D&format=application%2Fsparql-results%2Bjson&CXML_redir_for_subjs=121&CXML_redir_for_hrefs=&timeout=30000&debug=on&run=+Run+Query+";
	}

	@Override
	public void run() {
		JSONObject json;
		try {
			json = readJsonFromUrl(url);
			for (Object i : json.getJSONObject("results").getJSONArray("bindings")) {
				String uri = ((JSONObject) i).getJSONObject("s").getString("value");
				String textField = ((JSONObject) i).getJSONObject("abs").getString("value");
				ArticleHolder ah = new ArticleHolder(textField, uri, controller);
				ah.start();
				controller.addHolderToThePool(ah);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

	private static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
		InputStream is = new URL(url).openStream();
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			String jsonText = readAll(rd);
			JSONObject json = new JSONObject(jsonText);
			return json;
		} finally {
			is.close();
		}
	}

}
