package pers.bbn.changeBug.tests;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import pers.bbn.changeBug.extraction.Bow;

public class BowTest {
	Map<String, Integer> resMap;
	@Test
	public void testBow(){
		 resMap= Bow
				.bow("Add cluster control scripts.");
		System.out.println(resMap);
		Map<String, Integer> compareMap=new HashMap<String, Integer>();
		compareMap.put("add", 1);
		compareMap.put("cluster", 1);
		compareMap.put("scripts", 1);
		
	}

	@Test
	public void testBowPP(){
		resMap=Bow.bowPP("/home/niu/eclipse/plugins/hello.java");
		System.out.println(resMap);
	}
}
