package pers.bbn.changeBug.tests;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import org.junit.Test;

import com.mysql.jdbc.Statement;

import pers.bbn.changeBug.extraction.Extraction1;

public class Extraction1Test {
	static Extraction1 extraction1;
	static Statement stmt;
	static String sql;
	static int testCommit_id = 821;
	static int testFile_id = 1047;
	static ResultSet resultSet;

	public static void setup() throws Exception {
		extraction1 = new Extraction1("MyLucene", 1001, 1500);
		stmt = (Statement) extraction1.getConnection().getStmt();
	}

	public final void testObtainCurAttributes() throws SQLException {
		List<String> curAtt = extraction1.getCurAttributes();
		TestCase.assertTrue(curAtt.get(0).equals("id")
				&& curAtt.get(1).equals("commit_id"));
	}

	public final void testGetCommit_file_inExtracion1() throws SQLException {
		List<List<Integer>> commit_file_idInExtraction1 = extraction1
				.getCommit_file_inExtracion1();
		TestCase.assertEquals(commit_file_idInExtraction1.size(), 519);
	}

	@Test
	public final void testGetContentMap() throws Exception {
		Extraction1 extraction1 = new Extraction1("MyVoldemort", 501, 800);
		Map<List<Integer>, String> contentMap = extraction1
				.getContentMap(extraction1.getCommit_file_inExtracion1());
		for (List<Integer> key : contentMap.keySet()) {
			System.out.println(contentMap.get(key));
		}
	}
	
}
