package pers.bbn.changeBug.extraction;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 文件操作类，主要用于将得到的数据写入csv文件。
 * 
 * @author niu
 *
 */
public class FileOperation {
	String sql;
	SQLConnection sqlL;
	Statement stmt;
	ResultSet resultSet;
	List<Integer> commit_ids;
	List<List<Integer>> id_commit_fileIds;

	/**
	 * 获取文本的主键.
	 * 
	 * @return
	 */
	public List<List<Integer>> getId_commit_fileIds() {
		return id_commit_fileIds;
	}

	/**
	 * 设置文本操作的主键.
	 * 
	 * @param id_commit_fileIds
	 */
	public void setId_commit_fileIds(List<List<Integer>> id_commit_fileIds) {
		this.id_commit_fileIds = id_commit_fileIds;
	}

	/**
	 * 写字典.
	 * 
	 * @param dict
	 *            字典名称
	 * @param dictionary
	 *            字典内容
	 * @throws IOException
	 */
	public void writeDict(String dict, Map<String, String> dictionary)
			throws IOException {
		File di = new File(dict);
		BufferedWriter br = new BufferedWriter(new FileWriter(di));
		for (String string : dictionary.keySet()) {
			br.write(string + "   " + dictionary.get(string) + "\n");
		}
		br.flush();
		br.close();
	}

	/**
	 * 写反序字典
	 * 
	 * @param dict
	 *            反序字典名称
	 * @param dictionary
	 *            反序字典内容
	 * @throws IOException
	 */
	public void writeColName(String dict, Map<String, String> dictionary)
			throws IOException {
		File di = new File(dict);
		BufferedWriter br = new BufferedWriter(new FileWriter(di));
		List<String> colNameList = new ArrayList<>();
		for (String string : dictionary.keySet()) {
			colNameList.add(dictionary.get(string));
		}
		Collections.sort(colNameList);
		for (String string : colNameList) {
			br.write(string + "\n");
		}
		br.flush();
		br.close();
	}

	/**
	 * 将Merge整合的数据写入csv文件
	 * 
	 * @param content
	 *            需要写入的文件
	 * @param csvFile
	 *            被写入的文件名
	 * @throws IOException
	 */
	public void writeContent(Map<List<Integer>, StringBuffer> content,
			String csvFile) throws IOException {

		File writeFile = new File(csvFile);
		BufferedWriter bw = new BufferedWriter(new FileWriter(writeFile));
		StringBuffer stringBuffer = new StringBuffer();
		List<List<Integer>> keySet = new ArrayList<>();
		keySet.addAll(content.keySet());
		Collections.sort(keySet, new Comparator<List<Integer>>() {
			@Override
			public int compare(List<Integer> o1, List<Integer> o2) {
				return o1.get(0).intValue() - o2.get(0).intValue();
			}
		});

		for (List<Integer> list : keySet) {
			stringBuffer = content.get(list).append("\n"); // 哪里多加了个换行？
			bw.write(stringBuffer.toString());
		}
		bw.flush();
		bw.close();
	}

	public List<List<Integer>> readCommitFileIds(String filename)
			throws IOException {
		BufferedReader bReader = new BufferedReader(new FileReader(new File(
				filename)));
		List<List<Integer>> commit_fileIds = new ArrayList<>();
		String line;
		while ((line = bReader.readLine()) != null) {
			List<Integer> temp = new ArrayList<>();
			temp.add(Integer.parseInt(line.split("[\\s{1,},]")[0]));
			temp.add(Integer.parseInt(line.split("\\s{1,}")[1]));
			commit_fileIds.add(temp);
		}
		bReader.close();
		return commit_fileIds;
	}

	public void writeCommitFileIds(List<List<Integer>> commit_fileIds,
			String fileName) throws IOException {
		BufferedWriter bWriter = new BufferedWriter(new FileWriter(new File(
				fileName)));
		for (List<Integer> list : commit_fileIds) {
			bWriter.write(list.get(0) + "   " + list.get(1) + "\n");
		}
		bWriter.flush();
		bWriter.close();
	}

	public List<List<Integer>> secondSort(List<List<Integer>> keySet) {
		int start = 0;
		int end = 0;
		while (end < keySet.size()) {
			while (end < keySet.size()
					&& (keySet.get(start).get(0).intValue() == keySet.get(end)
							.get(0).intValue())) {
				end++;
			}
			end--;
			if (end == start) {
				start = end + 1;
				end = start;
				continue;
			}
			for (int i = start; i < end; i++) {
				for (int j = end; j > i; j--) {
					if (keySet.get(j).get(1).intValue() < keySet.get(j - 1)
							.get(1).intValue()) {
						List<Integer> temp = keySet.get(j - 1);
						keySet.remove(j - 1);
						keySet.add(j, temp);
					}
				}
			}
			end++;
			start = end;
		}
		return keySet;
	}

	public static void writeStringBuffer(StringBuffer sBuffer, String outFile)
			throws IOException {
		BufferedWriter bWriter = new BufferedWriter(new FileWriter(new File(
				outFile)));
		bWriter.append(sBuffer);
		bWriter.flush();
		bWriter.close();
	}
}
