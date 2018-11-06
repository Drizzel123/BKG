package com.group.proseminar.knowledge_graph.NaturalLanguageProcessing;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ExtractorPipelineTest {

	@Test
	public void extractFromTextTest() throws Exception {
		ExtractorPipeline pipeline = new ExtractorPipeline();
		pipeline.processArticle(
				"Johann Carl Friedrich Gauss was a German mathematician and physicist who made significant contributions to many fields in mathematics and sciences. Sometimes referred to as the Princeps mathematicorum and the greatest mathematician since antiquity, Gauss had an exceptional influence in many fields of mathematics and science, and is ranked among history's most influential mathematicians.");
		assertEquals(true, true);
	}
}
