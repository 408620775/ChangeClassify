package pers.bbn.changeBug.tests;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import pers.bbn.changeBug.extraction.Extraction1;
import pers.bbn.changeBug.extraction.Extraction2;

public class Extraction2Test {
	List<List<Integer>> commit_fileIds;
	
	@Before
	public final void init() throws Exception {
		Extraction1 extraction1 = new Extraction1("MyVoldemort", 501, 800);
		commit_fileIds = extraction1.getCommit_file_inExtracion1();
	}
	@Test
	public final void testGetContentMap() throws Exception {
		Extraction2 extraction2=new Extraction2("MyVoldemort", 501, 800);
		extraction2.extraFromTxt("/home/niu/test/voldemortR/MyVoldemortMetrics.txt");
		Map<List<Integer>, String> contentMap=extraction2.getContentMap(commit_fileIds);
		for (List<Integer> key : contentMap.keySet()) {
			System.out.println(contentMap.get(key));
		}
	}
}
