package pers.bbn.changeBug.tests;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import junit.framework.TestCase;

import org.junit.BeforeClass;
import org.junit.Test;

import pers.bbn.changeBug.extraction.Extraction1;

public class Extraction1Test {
	static Extraction1 extraction1;

	@BeforeClass
	public static void setup() throws Exception {
		extraction1 = new Extraction1("MyVoldemort", 501, 800);
	}

	@Test
	public final void testObtainCurAttributes() throws SQLException {
		List<String> curAtt = extraction1.getCurAttributes();
		TestCase.assertTrue(curAtt.get(0).equals("id")
				&& curAtt.get(1).equals("commit_id"));
	}
	
	@Test
	public final void testGetCommit_file_inExtracion1() throws SQLException{
		List<List<Integer>> commit_file_idInExtraction1=extraction1.getCommit_file_inExtracion1();
		System.out.println(commit_file_idInExtraction1.size());
		TestCase.assertEquals(commit_file_idInExtraction1.size(), 519);
	}

}
