package com.group.proseminar.knowledge_graph.preprocessing;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.simple.Document;

/**
 * Provides preprocessing using its static method preprocess.
 * 
 * @author Sibar Soumi
 *
 */

public class Preprocessor {
	/**
	 * Removes special characters from the text and converts patterns like:
	 * "<i>Albert Einstein (14 March 1879 – 18 April 1955)</i>"
	 * into "<i>Albert Einstein was born in 14 March 1879. Albert Einstein died in 18 April 1955</i>"
	 * @param uri
	 * @param plainText The text to be preprocessed.
	 * @return The text {@link plainTesxt} after preprocessing being performed on it.
	 */
	public static String preprocess(String uri, String plainText) {
		plainText = plainText.replaceAll("[^(\\x00-\\xFF)|\\u2013|\\u2014]", "");
		
		String firstSentence = new Document(plainText).sentences().get(0).toString();
		
		firstSentence = firstSentence.replaceAll("\\[.*\\]", "");
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
		props.setProperty("ner.useSUTime", "false");
		props.setProperty("ner.applyFineGrained", "false");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		CoreDocument doc = new CoreDocument(firstSentence);
		pipeline.annotate(doc);
		       
		String person="";
		List<String> Dates=new LinkedList<String>();
		for (CoreEntityMention em : doc.entityMentions())
		    if (em.entityType().equals("DATE"))
		    	Dates.add(em.text());
		    else if (person.isEmpty() && em.entityType().equals("PERSON"))
		       	person = em.text();
		       
		if (person.isEmpty() && Dates.size()>=1)
			person=firstSentence.substring(0, firstSentence.indexOf('(')-1);
		    	
		
		if (Dates.size()>=2)
			plainText=person+" died in "+Dates.get(1)+". "+plainText;
		    	
		if (Dates.size()>=1)
			plainText=person+" was born in "+Dates.get(0)+". "+plainText;
		
		return plainText;
	}
	/**
	 * Converts Dates from format "May 26, 1980" into "26 May 1980".
	 * @param Date The date whose format is to be converted
	 * @return The date {@link Date} after being converted into the new format.
	 */
	private static String fixDateFormat(String Date) {
		if (Date.matches(
				"\\d{1,2}\\s(January|February|March|April|May|June|July|August|September|October|November|December)\\s\\d{1,4}"))
			return Date;
		else {

			Matcher m = Pattern
					.compile("January|February|March|April|May|June|July|August|September|October|November|December")
					.matcher(Date);
			m.find();
			String month = m.group(0);

			m = Pattern.compile("\\s\\d{1,2},").matcher(Date);
			m.find();
			String day = m.group(0).replace(",", "").replace(" ", "");

			return Date.replaceFirst(
					"(January|February|March|April|May|June|July|August|September|October|November|December)\\s\\d{1,2},",
					day + " " + month);
		}

	}
}