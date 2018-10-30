package com.group.proseminar.knowledge_graph.reader;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

public class Controller {
	private Semaphore start_view_wait=new Semaphore(0);
	private String ontology;
	private LinkedList<ArticleHolder> pool=new LinkedList<ArticleHolder>();
	
	public Controller ()
	{
		StartView start_view=new StartView(start_view_wait,this);
		try {
			start_view_wait.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		new DataFetcher(ontology,this).start();
		
	}
	public void setOdb(String text)
	{
		ontology=text;
	}
	public void addHolderToThePool(ArticleHolder ah)
	{
		pool.add(ah);
	}
	public void deleteHolderFromThePool(ArticleHolder ah)
	{
		pool.remove(ah);
	}

}
