package pers.bbn.changeBug.classify;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import pers.bbn.changeBug.extraction.Extraction1;
import pers.bbn.changeBug.extraction.SQLConnection;

/**
 * 根据不同部分的属性分别构建不同的分类器,最后将所有分类器集成,按照投票结果决定实例最终的标签.
 * @author niu
 *
 */
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
