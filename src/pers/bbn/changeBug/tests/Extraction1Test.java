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
		System.out.println(curAtt);
		TestCase.assertTrue(curAtt.get(0).equals("id")
				&& curAtt.get(1).equals("commit_id"));
	}
	
	@Test
	public final void testGetCommit_file_inExtracion1() throws SQLException{
		List<List<Integer>> commit_file_idInExtraction1=extraction1.getCommit_file_inExtracion1();
		System.out.println(commit_file_idInExtraction1.size());
		//System.out.println(commit_file_idInExtraction1.get(0));
		//System.out.println(commit_file_idInExtraction1.get(commit_file_idInExtraction1.size()-1));
		Collections.sort(commit_file_idInExtraction1,new Comparator<List<Integer>>() {

			@Override
			public int compare(List<Integer> o1, List<Integer> o2) {
				return o1.get(0)-o2.get(0);
			}
		});
		for (List<Integer> list : commit_file_idInExtraction1) {
			System.out.println(list);
		}
		TestCase.assertEquals(commit_file_idInExtraction1.size(), 519);
	}

}
