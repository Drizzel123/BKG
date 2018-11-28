package com.group.proseminar.knowledge_graph.reader;

import com.group.proseminar.knowledge_graph.preprocessing.Preprocessor;

/**
 * An object from this class is generated for each article fetched by {@link DataFetcher}.
 * It calls all the required steps for handling the article.
 * 
 * @author Sibar Soumi
 *
 */

class ArticleHolder extends Thread {
	private String plainText, uri;
	private Controller controller;

	ArticleHolder(String plainText, String uri, Controller controller) {
		this.plainText = plainText;
		this.uri = uri;
		this.controller = controller;
	}

	@Override
	public void run() {
		// DO THE PROCESSING HERE. The text to be processed is in the variable
		// plainText.
		// Preprocessing:
		plainText = Preprocessor.preprocess(uri, plainText);
				
		controller.deleteHolderFromThePool(this);
		System.out.println(controller);
	}

}
