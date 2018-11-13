package com.group.proseminar.knowledge_graph.preprocessing;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Preprocessor {
	public static String preprocess(String uri, String plainText) {
		plainText = plainText.replaceAll("[^\\x00-\\xFF]", "");
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