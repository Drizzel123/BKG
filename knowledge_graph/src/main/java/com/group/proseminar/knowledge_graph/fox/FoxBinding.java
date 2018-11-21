package com.group.proseminar.knowledge_graph.fox;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.aksw.fox.binding.*;
import org.aksw.fox.data.Entity;
import org.aksw.fox.data.RelationSimple;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import com.group.proseminar.knowledge_graph.nlp.Triplet;

public class FoxBinding {

  public final static Logger LOG = LogManager.getLogger(FoxBinding.class);
  private FoxResponse response;
  public List<Triplet> triplets = new ArrayList<Triplet>();


  /**
   * Binds to the fox-server and returns a response of the given input-String
   * 
   * @param input - String sentence from which the triplet will be created
   * @throws MalformedURLException
   */
  private FoxResponse binding(String input) throws MalformedURLException {
    FoxResponse resp;
    final IFoxApi fox = new FoxApi();
    fox.setApiURL(new URL("http://fox-demo.aksw.org/fox"));
    
    fox.setTask(FoxParameter.TASK.RE);
    fox.setOutputFormat(FoxParameter.OUTPUT.TURTLE); // JSONLD, TURTLE
    fox.setLang(FoxParameter.LANG.EN);
    fox.setInput(input).send();
    // fox.setLightVersion(FoxParameter.FOXLIGHT.DEBalie);
    // String data = fox.responseAsFile();
    // LOG.info(data);

    resp = fox.responseAsClasses();
    return resp;
    
  }

  /**
   * Connects to the server and sends the input-string.
   * Creates a triplet from the relations given in the fox-response, and adds those triplets into the list. 
   */
  public void createFoxTriplets(String input) {
    Triplet<String, String, String> triplet;
    boolean differentTriplet;
    
    try {
      this.response = binding(input);
    } catch (MalformedURLException e) {
      LOG.error("Exception caught while binding to fox-server. " + e.getMessage());
    }

    LOG.info(this.response);
    for (RelationSimple rs : this.response.getRelations()) {
      triplet = makeTriplet(rs);
      differentTriplet = true;
      
      //if Triplet already there -> don't add it.
      for (Triplet t : triplets) {
        if (t.toString().equals(triplet.toString())) {
          differentTriplet = false;
          break;
        }
      }

      if (differentTriplet) {
        this.triplets.add(triplet);
      }
    }
    
    System.out.println(triplets);

    List<Pair> typeTuple = processResponse(response);
    System.out.println("\n" + typeTuple);
  }
  
  /**
   * Writes the data (as file) returned from fox into a file.
   * 
   * @param data - the data returned from fox.
   * @throws IOException
   */
  private static void writeResults(String data) throws IOException {
    File foxResults = new File(".//fox_results2.txt");

    // Create the file
    if (foxResults.createNewFile()) {
      System.out.println("File [" + foxResults.getName() + "] is created!");
    } else {
      System.out.println("File [" + foxResults.getName() + "] foxResultsalready exists.");
    }

    // Write Content
    FileWriter writer = new FileWriter(foxResults);
    writer.write(data);
    writer.close();
  }

  /**
   * Extracts the pairs [[object|subject], type] from the response.
   * 
   * @param response - FoxResponse
   * @return List<Pair>
   */
  public static List<Pair> processResponse(FoxResponse response) {
    List<Pair> typeTuple = new ArrayList<Pair>();
    Pair pair;
    String text;
    String type;

    for (Entity e : response.getEntities()) {
      text = e.getText();
      type = getType(e);
      pair = new Pair(text, type);
      typeTuple.add(pair);
    }
    
    return typeTuple;
  }

  /**
   * @param e - extracts only the type from a Entity, returned by fox
   * @return String - type of the entity. (example: person, location, etc.)
   */
  public static String getType(Entity e) {
    String type = "";
    int typeStartingIndex = 0;

    for (int i = e.getType().length() - 1; i > 0; i--) {
      if (e.getType().charAt(i) == '#') {
        typeStartingIndex = i;
        break;
      }
    }

    type = e.getType().substring(typeStartingIndex + 1, e.getType().length());
    return type;
  }

  /**
   * Creates a Triplet which contains the URIs from a single RelationSimple, which was taken from
   * the fox-response. Triplet<String,String,String> := <subject, predicate, object>
   * 
   * @param rs - RelationSimple: a relation extracted from the fox-response
   * @return a Triplet in format [subject, predicate, object]
   */
  private Triplet<String, String, String> makeTriplet(RelationSimple rs) {
    Triplet<String, String, String> triplet;
    String subject = "", predicate = "", object = "";
    int startPointer = 0;
    int endPointer = 0;

    for (int i = 0; i < rs.toString().length(); i++) {
      if (rs.toString().charAt(i) == '=') {
        if (rs.toString().charAt(i - 1) == 's') {
          startPointer = i;
        } else if (rs.toString().charAt(i - 1) == 'p') {
          subject = rs.toString().substring(startPointer + 1, endPointer);
          startPointer = i;
        } else if (rs.toString().charAt(i - 1) == 'o') {
          predicate = rs.toString().substring(startPointer + 1, endPointer);
          startPointer = i;
        } else {
          object = rs.toString().substring(startPointer + 1, endPointer);
        }
      }
      if (rs.toString().charAt(i) == ',') {
        endPointer = i;
      }

    }

    triplet = new Triplet<String, String, String>(subject, predicate, object);

    return triplet;
  }

  public void addTriplet(Triplet<String, String, String> t) {
    this.triplets.add(t);
  }

  public List<Triplet> getTriplets() {
    return this.triplets;
  }
  
  private FoxResponse getResponse() {
    return this.response;
  }
}
