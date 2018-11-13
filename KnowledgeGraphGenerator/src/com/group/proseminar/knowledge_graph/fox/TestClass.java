package com.group.proseminar.knowledge_graph.fox;

public class TestClass {
public static void main (String [] a)
{
	String s="öôóòع sdmksm سيبار Gauß 57 Ю́рий Дми́триевич Бура́го 福井 謙 Chinese: 曾宪义; ";
	System.out.println(s);	
	s=s.replaceAll("[^\\x00-\\xFF]", "");
	System.out.println(s);	

}
}
