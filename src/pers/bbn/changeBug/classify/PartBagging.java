package pers.bbn.changeBug.classify;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import pers.bbn.changeBug.extraction.Extraction1;
import pers.bbn.changeBug.extraction.SQLConnection;

/**
 * 根据不同部分的属性分别构建不同的分类器,最后将所有分类器集成,按照投票结果决定实例最终的标签.
 * @author niu
 *
 */
public class PartBagging {
	List<List<Integer>> commitFileIds;
	Map<List<Integer>, String> classLabels;
	String sql;
	Statement stmt;
	ResultSet resultSet;
	
	/**
	 * 构造函数,初始化变量
	 * @param database git工程对应的数据库
	 * @param start 待研究版本区间的起始号
	 * @param end 待研究版本区间的结束号
	 * @throws Exception
	 */
	public PartBagging(String database,int start,int end) throws Exception {
		SQLConnection sqlL = new SQLConnection(database);
		this.stmt = sqlL.getStmt();
		Extraction1 extraction1=new Extraction1(database, start, end);
		commitFileIds=extraction1.getCommit_file_inExtracion1();
		classLabels=extraction1.getClassLabels(commitFileIds);
		
		
	}
}
