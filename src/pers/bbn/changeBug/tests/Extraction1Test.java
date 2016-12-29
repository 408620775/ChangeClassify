package pers.bbn.changeBug.tests;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.tools.Tool;

import junit.framework.TestCase;

import org.junit.BeforeClass;
import org.junit.Test;

import com.mysql.jdbc.Statement;

import pers.bbn.changeBug.extraction.Extraction1;
import pers.bbn.changeBug.extraction.MyTool;
import pers.bbn.changeBug.extraction.SQLConnection;

public class Extraction1Test {
	static Extraction1 extraction1;
	static Statement stmt;
	static String sql;
	static int testCommit_id = 821;
	static int testFile_id = 1047;
	static ResultSet resultSet;
	@BeforeClass
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
	public final void testGetFirstAppearOfFile() throws SQLException {
		System.out.println("===============TestGetFirstAppearOfFile===============");
		List<Integer> test=extraction1.getFirstAppearOfFile(testCommit_id, testFile_id);
		System.out.println(test);
	}

	public final void testUpdateHistory() throws SQLException, ParseException {
		extraction1.updateHistory(497, 686);
	}


	public final void testGetLastChangeOfFile() throws SQLException {
		int lastCommitId = extraction1.getLastChangeOfFile(860, 1244);
		TestCase.assertEquals(lastCommitId, 860);
	}


	public final void testUpdateExperience() throws SQLException {
		System.out.println("===============TestUpdateExperience===============");
		sql = "select commit_date,type,author_id,current_file_path,actions.commit_id,actions.file_id from actions,extraction1,scmlog"
				+ " where extraction1.id<=(select id from extraction1 where commit_id="
				+ testCommit_id
				+ " and file_id="
				+ testFile_id
				+ ") and extraction1.commit_id=scmlog.id and extraction1.commit_id=actions.commit_id"
				+ " and extraction1.file_id=actions.file_id and extraction1.file_id="
				+ testFile_id + " order by extraction1.id";
		resultSet=stmt.executeQuery(sql);
		List<List<String>> resList=new ArrayList<>();
		List<String> title=new ArrayList<>();
		title.add("commit_date");
		title.add("commit_id");
		title.add("file_id");
		title.add("type");
		title.add("author_id");
		title.add("current_file_path");
		
		resList.add(title);
		while (resultSet.next()) {
			List<String> tmpList=new ArrayList<>();
			tmpList.add(resultSet.getString(1));
			tmpList.add(resultSet.getString(5));
			tmpList.add(resultSet.getString(6));
			tmpList.add(resultSet.getString(2));
			tmpList.add(resultSet.getString(3));
			tmpList.add(resultSet.getString(4));
			resList.add(tmpList);
		}
		MyTool.printDBdata(resList);
		extraction1.updateExperience(testCommit_id, testFile_id);
		sql="select author_id from scmlog where id="+testCommit_id;
		resultSet=stmt.executeQuery(sql);
		int curAuthor_id=0;
		while (resultSet.next()) {
			curAuthor_id=resultSet.getInt(1);
		}
		int count=0;
		for (int i = 1; i < resList.size()-1; i++) {
			if (Integer.parseInt(resList.get(i).get(4))==curAuthor_id) {
				count++;
			}
		}
		
		sql="select EXP from extraction1 where commit_id="+testCommit_id+" and file_id="+testFile_id;
		resultSet=stmt.executeQuery(sql);
		int dbCount=0;
		while (resultSet.next()) {
			dbCount=resultSet.getInt(1);
		}
		
		TestCase.assertEquals(count, dbCount);
	}
}
