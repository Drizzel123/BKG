package com.group.proseminar.knowledge_graph.reader;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
		
		Path path = Paths.get("input_data\\"+uri.substring(uri.lastIndexOf("/")+1));
        try {
			Files.createDirectories(path.getParent());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

        try {
            Files.createFile(path);
            BufferedWriter writer;
            writer = new BufferedWriter(new FileWriter("input_data\\"+uri.substring(uri.lastIndexOf("/")+1)));
			writer.write(plainText);
			 writer.close();
        } catch (IOException e) {
        }

  
		
		controller.deleteHolderFromThePool(this);
		//System.out.println(controller);
	}

}
