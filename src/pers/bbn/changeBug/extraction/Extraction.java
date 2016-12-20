package pers.bbn.changeBug.extraction;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 提取数据的超类。
 * <p>
 * 将commit按照时间排序，提取指定范围内所有commit的若干信息。若干信息的提取分别
 * 由三个子类去实现。需要注意的是，由于miningit分配给各commit的id并不是其实际提交的
 * 顺序（由于多线程并发导致），所以对于commit的排序不应根据其id排序，而应根据 commit_date排序。
 * 
 * @param sql
 *            需要连接的数据库
 * @param start
 *            指定的commit的起始值（按照时间排序）
 * @param end
 *            指定的commit的结束值（按照时间排序）
 * @see ResultSet
 * @see SQLException
 * @see Statement
 * @author niu
 *
 */
public class Extraction {
    String sql;
    Statement stmt;
	ResultSet resultSet;
	List<Integer> commit_ids;
	private SQLConnection sqlL;

	/**
	 * extraction2提取信息并不需要miningit生成的数据，此构造函数只是为了统一接口。
	 * 
	 * @param database
	 * @throws SQLException
	 */
	public Extraction(String database) throws SQLException { // 为extraction2提供构造函数。
		sqlL = new SQLConnection(database);
		this.stmt = sqlL.getStmt();
		commit_ids = new ArrayList<>();
		sortCommit_id();
		//start = 0; // 实际没有用到
		//end = 0; // 实际没有用到
	}

	/**
	 * 按时间序返回commit_id列表。
	 * 
	 * @return 按时间排序的指定范围内的commit_id列表。
	 */
	public List<Integer> getCommit_id() {
		return commit_ids;
	}

	/**
	 * 提取scmlog中全部的commit_id（按时间排序）。
	 * 
	 * @throws SQLException
	 */
	public void sortCommit_id() throws SQLException {
		sql = "select id from scmlog order by commit_date";
		resultSet = stmt.executeQuery(sql);
		while (resultSet.next()) {
			commit_ids.add(resultSet.getInt(1));
		}
	}

}
