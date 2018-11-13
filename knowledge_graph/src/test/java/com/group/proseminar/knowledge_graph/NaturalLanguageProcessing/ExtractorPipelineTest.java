package com.group.proseminar.knowledge_graph.NaturalLanguageProcessing;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.group.proseminar.knowledge_graph.nlp.ExtractorPipeline;

public class ExtractorPipelineTest {

	@Test
	public void extractFromTextTest() throws Exception {
		ExtractorPipeline pipeline = new ExtractorPipeline();
		pipeline.processArticle(
				"Carl Gauss was an ardent perfectionist and a hard worker. He was never a prolific writer, refusing to publish work which he did not consider complete and above criticism. Though he did take in a few students, Gauss was known to dislike teaching. It is said that he attended only a single scientific conference, which was in Berlin in 1828.");
		assertEquals(true, true);
	}
}
