package pers.bbn.changeBug.extraction;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 将extraction1、extraction2和extraction3得到的数据进行合成。
 * extraction1和extraction2的数据都存储于数据库中，而extraction3的数据以content的形式存在。为了将整体信息
 * 能够写入csv文件，所以将extraction1和extraction2中的数据整合到conten中。
 * 
 * @param content3
 *            合成的内容，其中key值为id，commit_id，file_id组成的list，StringBuffer存放具体的属性信息，
 *            属性信息同时又包括了id、commit_id和file_id(因为将来在写文件的时候，这三个属性的值也需要被写入)。
 *            初始化时标示extraction3中的数据，执行merge123()后为三个表的整合数据。
 * @param sql
 *            sql语句的标识。
 * @param sqlConnection
 *            连接extraction1和extraction2所在的数据库。
 * @param stmt
 *            执行sql语句。
 * @param resultSet
 *            执行sql语句后得到的结果集。
 * @param id_commit_fileIds
 *            合成的实例的主键。分别对应与实例的id、commit_id和file_id，当这三项都为-1时，表明对应着的是属性名称而非属性值。
 * @author niu
 *
 */
public class Merge {
	Map<List<Integer>, StringBuffer> content2;
	Map<List<Integer>, StringBuffer> content3;
	String sql;
	SQLConnection sqlConnection;
	Statement stmt;
	ResultSet resultSet;
	List<List<Integer>> id_commit_fileIds; // 对接的时候要注意，之前都是三维的，需要改

	/**
	 * 获取实例的主键集。函数名和实际功能不太对应，有待改正。
	 * 
	 * @return 所有实例的主键的集合。
	 */
	public List<List<Integer>> getCommit_fileId() {
		return id_commit_fileIds;
	}

	/**
	 * 根据连接的数据库中的extraction2表直接设置实例的主键集。
	 * 
	 * @throws SQLException
	 */
	public void setCommit_fileId() throws SQLException {
		id_commit_fileIds = new ArrayList<>();
		sql = "select id,commit_id,file_id from extraction2";
		resultSet = stmt.executeQuery(sql);

		while (resultSet.next()) {
			List<Integer> temp = new ArrayList<>();
			temp.add(resultSet.getInt(1));
			temp.add(resultSet.getInt(2));
			temp.add(resultSet.getInt(3));
			id_commit_fileIds.add(temp);
		}
	}
	

	/**
	 * 通过参数设置id_commit_fileIds。
	 * 
	 * @param icf_id
	 */
	public void setCommit_fileId(List<List<Integer>> icf_id) {
		this.id_commit_fileIds = icf_id;
	}

	/**
	 * 获取所有实例内容。
	 * 
	 * @return content 所有实例内容，key为各实例的主键，value为实例属性的值。
	 */
	public Map<List<Integer>, StringBuffer> getContent3() {
		return content3;
	}

	/**
	 * 设置实例。
	 * 
	 * @param content
	 */
	public void setContent3(Map<List<Integer>, StringBuffer> content) {
		this.content3 = content;
	}

	/**
	 * Merge的构造函数。 根据已有的信息构造Merge。
	 * 
	 * @param content
	 * @param database
	 * @throws SQLException
	 */
	public Merge(Map<List<Integer>, StringBuffer> content3, String database)
			throws SQLException {
		this.sqlConnection = new SQLConnection(database);
		stmt = sqlConnection.getStmt();
		setContent3(content3);
		setCommit_fileId();

	}

	/**
	 * Merge的构造函数。 根据已有的信息构造Merge。
	 * 
	 * @param content
	 * @param id_commit_fileId
	 * @param database
	 * @throws SQLException
	 */
	public Merge(Map<List<Integer>, StringBuffer> content3,
			Map<List<Integer>, StringBuffer> content2,
			List<List<Integer>> icf_id, String database) throws SQLException {
		setContent3(content3);
		setContent2(content2);
		setCommit_fileId(icf_id);
		this.sqlConnection = new SQLConnection(database);
		stmt = sqlConnection.getStmt();
	}

	private Map<List<Integer>, StringBuffer> merge12_2() throws SQLException {
		System.out.println("merge12_2");
		Map<List<Integer>, StringBuffer> m12 = new HashMap<List<Integer>, StringBuffer>();
		for (List<Integer> commit_fileId : id_commit_fileIds) {
			System.out.println(commit_fileId.get(1) + "_"
					+ commit_fileId.get(2));
			StringBuffer temp = new StringBuffer();
			if (commit_fileId.get(1) == -1) {
				sql = "select * from extraction1 where id=1";
				resultSet = stmt.executeQuery(sql);
				int colcount = resultSet.getMetaData().getColumnCount();

				for (int i = 1; i <= colcount; i++) {
					temp.append(resultSet.getMetaData().getColumnName(i) + ",");
				}
			} else {
				sql = "select * from extraction1 where commit_id="
						+ commit_fileId.get(1) + " and file_id="
						+ commit_fileId.get(2);
				resultSet = stmt.executeQuery(sql);
				int colCount = resultSet.getMetaData().getColumnCount();
				resultSet.next();
				// 按照extraction2的序号初始化实际的id。
				temp.append(commit_fileId.get(0) + ",");
				for (int i = 2; i <= colCount; i++) {
					temp.append(resultSet.getString(i) + ",");
				}
			}
			
			temp.append(content2.get(commit_fileId) );
			m12.put(commit_fileId, temp);
		}
		System.out.println(m12.size());
		return m12;
	}

	/**
	 * 合并extraction1和extraction2得到的数据。
	 * 
	 * @return 由extraction1和extraction2合并得到的实例集。
	 * @throws SQLException
	 */
	public Map<List<Integer>, StringBuffer> merge12() throws SQLException {
		System.out.println("merge12");

		// 用以记录表头，以备将来打印。
		List<Integer> head = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			head.add(-1);
		}
		id_commit_fileIds.add(head);
		Map<List<Integer>, StringBuffer> m12 = new HashMap<List<Integer>, StringBuffer>();
		for (List<Integer> commit_fileId : id_commit_fileIds) {
			System.out.println(commit_fileId.get(1) + "_"
					+ commit_fileId.get(2));
			StringBuffer temp = new StringBuffer();
			if (commit_fileId.get(1) == -1) {
				sql = "select * from extraction1 where id=1";
				resultSet = stmt.executeQuery(sql);
				int colcount = resultSet.getMetaData().getColumnCount();

				for (int i = 1; i <= colcount; i++) {
					temp.append(resultSet.getMetaData().getColumnName(i) + ",");
				}
			} else {
				sql = "select * from extraction1 where commit_id="
						+ commit_fileId.get(1) + " and file_id="
						+ commit_fileId.get(2);
				resultSet = stmt.executeQuery(sql);
				int colCount = resultSet.getMetaData().getColumnCount();
				resultSet.next();
				// 按照extraction2的序号初始化实际的id。
				temp.append(commit_fileId.get(0) + ",");
				for (int i = 2; i <= colCount; i++) {
					temp.append(resultSet.getString(i) + ",");
				}

			}

			if (commit_fileId.get(1) == -1) {
				sql = "select * from extraction2 where id=1";
				resultSet = stmt.executeQuery(sql);
				int colcount = resultSet.getMetaData().getColumnCount();
				for (int i = 4; i < colcount; i++) {
					temp.append(resultSet.getMetaData().getColumnName(i) + ",");
				}
				temp.append(resultSet.getMetaData().getColumnName(colcount));
			} else {
				sql = "select * from extraction2 where commit_id="
						+ commit_fileId.get(1) + " and file_id="
						+ commit_fileId.get(2);
				resultSet = stmt.executeQuery(sql);
				int colCount = resultSet.getMetaData().getColumnCount();
				resultSet.next();
				for (int i = 4; i < colCount; i++) {
					temp.append(resultSet.getString(i) + ",");
				}
				temp.append(resultSet.getString(colCount));
			}
			m12.put(commit_fileId, temp);
		}
		System.out.println(m12.size());
		return m12;
	}

	/**
	 * 合并extraction1、extraction2和extraction3得到的数据。
	 * 
	 * @return 
	 *         由extraction1、extraction2和extraction3合并得到的实例集。此出的content作为一个参数更合理，待修改
	 *         。
	 * @throws SQLException
	 */
	public Map<List<Integer>, StringBuffer> merge123() throws SQLException,
			IOException {
		System.out.println("merge123");
		Map<List<Integer>, StringBuffer> temp = null;
		if (content2 == null) {
			temp = merge12();
		} else {
			temp = merge12_2();
		}

		System.out.println(temp.size());
		for (List<Integer> list4 : id_commit_fileIds) {
			StringBuffer addString = content3.get(list4);
			addString.delete(0, addString.indexOf(",") + 1);
			addString.delete(0, addString.indexOf(",") + 1);
			addString.delete(0, addString.indexOf(",") + 1);
			content3.put(list4, temp.get(list4).append(addString));
		}
		return content3;
	}

	public Map<List<Integer>, StringBuffer> getContent2() {
		return content2;
	}

	public void setContent2(Map<List<Integer>, StringBuffer> content2) {
		this.content2 = content2;
	}
}
