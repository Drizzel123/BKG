package com.group.proseminar.knowledge_graph.nlp_test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.group.proseminar.knowledge_graph.nlp.ExtractorPipeline;

public class ExtractorPipelineTest {
	
	@Test
	public void extractFromTextTest() throws Exception {
		ExtractorPipeline pipeline = new ExtractorPipeline();
		pipeline.processArticle("New York City is the most densely populated major city in the United States.");
		pipeline.processArticle("Barack Obama is the next president of America.");
		pipeline.processArticle("Obama was the king of the United States.");
		pipeline.processArticle("New York was founded in 1993.");
		pipeline.writeResultToFile();
		assertEquals(true, true);
	}
}
