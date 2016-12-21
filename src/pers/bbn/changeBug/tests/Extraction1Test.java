package pers.bbn.changeBug.tests;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.List;

import junit.framework.TestCase;

import org.junit.BeforeClass;
import org.junit.Test;

import com.mysql.jdbc.Statement;

import pers.bbn.changeBug.extraction.Extraction1;
import pers.bbn.changeBug.extraction.SQLConnection;

public class Extraction1Test {
	static Extraction1 extraction1;
	static Statement stmt;

	@BeforeClass
	public static void setup() throws Exception {
		extraction1 = new Extraction1("MyVoldemort", 501, 800);
		stmt = (Statement) extraction1.getConnection().getStmt();
	}

	@Test
	public final void testObtainCurAttributes() throws SQLException {
		List<String> curAtt = extraction1.getCurAttributes();
		TestCase.assertTrue(curAtt.get(0).equals("id")
				&& curAtt.get(1).equals("commit_id"));
	}

	@Test
	public final void testGetCommit_file_inExtracion1() throws SQLException {
		List<List<Integer>> commit_file_idInExtraction1 = extraction1
				.getCommit_file_inExtracion1();
		TestCase.assertEquals(commit_file_idInExtraction1.size(), 519);
	}

	@Test
	public final void testGetFirstAppearOfFile() throws SQLException {
		int testCommit_id = 497;
		int testFile_id = 686;
		String sql = "SELECT MAX(extraction1.id) from extraction1,actions where "
				+ "extraction1.id<(select id from extraction1 where commit_id="
				+ testCommit_id
				+ " and file_id="
				+ testFile_id
				+ ")"
				+ "and extraction1.file_id="
				+ testFile_id
				+ " and extraction1.commit_id=actions.commit_id"
				+ " and extraction1.file_id=actions.file_id and type='A'";
		ResultSet resultSet = stmt.executeQuery(sql);
		int idInExtraction1 = 0;
		while (resultSet.next()) {
			idInExtraction1 = resultSet.getInt(1);
		}
		sql = "SELECT commit_id from extraction1 where id=" + idInExtraction1;
		resultSet = stmt.executeQuery(sql);
		int firstAddCommitId = 0;
		while (resultSet.next()) {
			firstAddCommitId = resultSet.getInt(1);
		}

		sql = "select commit_date from scmlog where id=" + firstAddCommitId;
		resultSet = stmt.executeQuery(sql);
		String startDate = null;
		while (resultSet.next()) {
			startDate = resultSet.getString(1);
		}
		sql = "select commit_date from scmlog where id=" + testCommit_id;
		resultSet = stmt.executeQuery(sql);
		String endDate = null;
		while (resultSet.next()) {
			endDate = resultSet.getString(1);
		}
		sql = "select actions.*,commit_date from actions,scmlog where file_id="+testFile_id+" and actions.commit_id=scmlog.id and commit_date between '"
				+ startDate + "' and '" + endDate + "'";
		resultSet = stmt.executeQuery(sql);
		int typeD=0;
		int typeA=0;
		int firstCommitId=0;
		while (resultSet.next()) {
			if (firstCommitId==0) {
				firstCommitId=resultSet.getInt(4);
			}
			System.out.println(resultSet.getInt(1) + "  |  "
					+ resultSet.getString(2) + "  |  " + resultSet.getInt(3)
					+ "  |  " + resultSet.getInt(4) + "  |  "
					+ resultSet.getString(7));
			if (resultSet.getString(2).equals("D")) {
				typeD++;
			}else if (resultSet.getString(2).equals("A")) {
				typeA++;
			}
		}
		TestCase.assertEquals(true, typeA==1&&typeD==0);
		int getCommitId=extraction1.getFirstAppearOfFile(testCommit_id, testFile_id);
		System.out.println(getCommitId);
		TestCase.assertEquals(firstCommitId,getCommitId );
	}
	
	@Test
	public final void testUpdateHistory() throws SQLException, ParseException{
		extraction1.updateHistory(497, 686);
	} 
}
