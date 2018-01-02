package pers.bbn.changeBug.extraction;

import java.util.LinkedHashMap;
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
public final class Merge {


	/**
	 * 将多个contentMap根据其commit_id和file_id合并,但是这个函数感觉空间复杂度很高.
	 * @param list
	 * @return
	 * @throws Exception
	 */
	public static Map<List<Integer>, StringBuffer> mergeMap(List<Map<List<Integer>, StringBuffer>> list) throws Exception {
		if (list.size()==0) {
			throw new Exception("the list size can't be 0!");
		}
		System.out.println("Merge");
		Map<List<Integer>, StringBuffer> resMap=new LinkedHashMap<>();
		for (List<Integer> key: list.get(0).keySet()) {
			resMap.put(key, new StringBuffer());
		}
		for (List<Integer> keyList: resMap.keySet()) {
			for (Map<List<Integer>, StringBuffer> part : list) {
				resMap.get(keyList).append(part.get(keyList));
			}
			if (resMap.get(keyList).charAt(resMap.get(keyList).length()-1)==',') {
				resMap.get(keyList).deleteCharAt(resMap.get(keyList).length()-1);
			}
		}
		return resMap;
	}
}
