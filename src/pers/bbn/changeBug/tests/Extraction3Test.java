package pers.bbn.changeBug.tests;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import pers.bbn.changeBug.extraction.Extraction1;
import pers.bbn.changeBug.extraction.Extraction3;

public class Extraction3Test {
	List<List<Integer>> commit_fileIds;

	@Before
	public final void init() throws Exception {
		Extraction1 extraction1 = new Extraction1("MyVoldemort", 501, 800);
		commit_fileIds = extraction1.getCommit_file_inExtracion1();
	}

	@Test
	public final void testGetContentMap() throws Exception {
		Extraction3 extraction3 = new Extraction3("MyVoldemort",
				"/home/niu/test/voldemortR/voldemortFiles", 501, 800,
				commit_fileIds);
		Map<List<Integer>, StringBuffer> contentMap = extraction3
				.getContentMap(commit_fileIds);
		System.out.println(contentMap.size());
	}
}
