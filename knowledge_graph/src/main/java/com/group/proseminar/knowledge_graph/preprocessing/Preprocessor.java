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


public class Preprocessor {
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
			plainText=person+" is born in "+Dates.get(0)+". "+plainText;
		
		return plainText;
	}

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