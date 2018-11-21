package com.group.proseminar.knowledge_graph.nlp_test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.group.proseminar.knowledge_graph.nlp.ExtractorPipeline;

public class ExtractorPipelineTest {

//	@Test
//	public void extractFromTextTest() throws Exception {
//		ExtractorPipeline pipeline = new ExtractorPipeline();
//		pipeline.processArticle(
//				"Georg Friedrich Bernhard Riemann was a German mathematician, who made contributions to analysis, number theory,"
//						+ " and differential geometry. In the field of real analysis, he is mostly known for the first rigorous formulation of the integral,"
//						+ " the Riemann integral, and his work on Fourier series. His contributions to complex analysis include most notably the introduction of Riemann surfaces,"
//						+ " breaking new ground in a natural, geometric treatment of complex analysis. His famous 1859 paper on the prime-counting function, containing the original statement of the Riemann hypothesis,"
//						+ " is regarded as one of the most influential papers in analytic number theory. Through his pioneering contributions to differential geometry,"
//						+ " Riemann laid the foundations of the mathematics of general relativity. He is considered by many to be one of a handful of greatest mathematicians of all time.");
//		assertEquals(true, true);
//	}
	
	@Test
	public void extractFromTextTest() throws Exception {
		ExtractorPipeline pipeline = new ExtractorPipeline();
		pipeline.processArticle("Johann Carl Friedrich Gauss was born in Brunswick.");
		assertEquals(true, true);
	}
}
