package com.group.proseminar.knowledge_graph.fox;

public class FoxBindingMain {
  static FoxBinding fb = new FoxBinding();

  public static void main(final String[] a) throws Exception {
    String input = "Barack Obama was born in America.";
    fb.createFoxTriplets(input);
  }
}
