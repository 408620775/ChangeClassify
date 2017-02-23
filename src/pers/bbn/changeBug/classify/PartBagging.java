package pers.bbn.changeBug.classify;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import pers.bbn.changeBug.extraction.Extraction1;
import pers.bbn.changeBug.extraction.SQLConnection;

public class PartBagging {
	List<List<Integer>> commitFileIds;
	String sql;
	Statement stmt;
	ResultSet resultSet;
	
	public PartBagging(String database,int start,int end) throws Exception {
		SQLConnection sqlL = new SQLConnection(database);
		this.stmt = sqlL.getStmt();
		Extraction1 extraction1=new Extraction1(database, start, end);
		commitFileIds=extraction1.getCommit_file_inExtracion1();
		
		extraction1.getContentMap(commitFileIds);
	}
}
