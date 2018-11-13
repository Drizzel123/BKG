package com.group.proseminar.knowledge_graph.FOX;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.management.relation.Relation;
import org.aksw.fox.binding.*;
import org.aksw.fox.data.Entity;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class FoxBinding {

	public final static Logger LOG = LogManager.getLogger(FoxBinding.class);
	static FoxResponse response;

	public static void binding(String input) throws MalformedURLException {

		final IFoxApi fox = new FoxApi();
		fox.setApiURL(new URL("http://fox-demo.aksw.org/fox"));

		fox.setTask(FoxParameter.TASK.RE);
		fox.setOutputFormat(FoxParameter.OUTPUT.TURTLE); // JSONLD, TURTLE
		fox.setLang(FoxParameter.LANG.EN);
		fox.setInput(input).send();
		// fox.setLightVersion(FoxParameter.FOXLIGHT.DEBalie);

		// LOG.info(fox.responseAsFile());
		String data = fox.responseAsFile();

		/*
		 * List<Entity> entities = response.getEntities();
		 * 
		 * System.out.println(jsonld);
		 * 
		 * System.out.println(data); List<Relation> relations = response.getRelations();
		 * 
		 */

		LOG.info(data);

		try {
			writeResults(data);
		} catch (IOException e) {
			e.printStackTrace();
		}

		response = fox.responseAsClasses();
		LOG.info(response);

		// from here may be the triplet exctracted. o-object, s-subject, p-predicate?

		System.out.println("\n Now printing only the relations: \n ");
		System.out.println(response.getRelations()); // index 0,1,2

		List<Pair> typeTuple = processResponse(response);
		System.out.println("\n" + typeTuple);
	}

	private static void writeResults(String data) throws IOException {
		File foxResults = new File(".//fox_results2.txt");

		// Create the file
		if (foxResults.createNewFile()) {
			System.out.println("File [" + foxResults.getName() + "] is created!");
		} else {
			System.out.println("File [" + foxResults.getName() + "] foxResultsalready exists.");
		}

		// Write Content
		FileWriter writer = new FileWriter(foxResults);
		writer.write(data);
		writer.close();

	}

	public static List<Pair> processResponse(FoxResponse response) {
		List<Pair> typeTuple = new ArrayList<Pair>();
		Pair pair;
		String text;
		String type;

		for (Entity e : response.getEntities()) {
			text = e.getText();
			type = getType(e);
			pair = new Pair(text, type);
			typeTuple.add(pair);
		}

		return typeTuple;
	}

	public static FoxResponse getResponse() {
		return response;
	}

	public static String getType(Entity e) {
		String type = "";
		int typeStartingIndex = 0;

		for (int i = e.getType().length() - 1; i > 0; i--) {
			if (e.getType().charAt(i) == '#') {
				typeStartingIndex = i;
				break;
			}
		}

		type = e.getType().substring(typeStartingIndex + 1, e.getType().length());
		return type;
	}

	public static void makeTriplet() {

	}
}
