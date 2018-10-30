package com.group.proseminar.knowledge_graph.reader;

class ArticleHolder extends Thread{
	private String plainText;
	private Controller controller;
	ArticleHolder (String plainText, Controller controller)
	{
		this.plainText=plainText;
		this.controller=controller;
	}
	
	@Override
	public void run()
	{
		// DO THE PROCESSING HERE. The text to be processed is in the variable plainText.
		
		
		
		
		
		
		controller.deleteHolderFromThePool(this);
	}
}
