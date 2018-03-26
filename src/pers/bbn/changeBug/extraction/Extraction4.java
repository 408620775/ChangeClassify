package pers.bbn.changeBug.extraction;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

/**
 * 提取源码的图属性.
 * 
 * @author nbb
 *
 */
public class Extraction4 extends Extraction {
	static StringBuffer attrsTitle;
	static Set<Integer> uselessAttriId;
	static String projectName;
	static Map<List<Integer>, StringBuffer> contentMap;

	/**
	 * 构造函数,提取指定工程指定阶段内的文件的图属性.
	 * 
	 * @param database
	 *            工程对应的数据库
	 * @param s
	 *            阶段的起始commit_id(时间序)
	 * @param e
	 *            阶段的结束commit_id(时间序)
	 * @throws Exception
	 */
	public Extraction4(String database, int s, int e) throws Exception {
		super(database, s, e);
		contentMap = new LinkedHashMap<>();
	}

	/**
	 * 为了获取源文件(使用git获得),打印相关的信息.
	 * 
	 * @param outFile
	 * @throws SQLException
	 * @throws IOException
	 */
	public void printRevInfo(String outFile) throws SQLException, IOException {
		StringBuffer sBuffer = new StringBuffer();
		for (List<Integer> list : commit_fileIds) {
			sql = "select extraction1.commit_id,extraction1.file_id,current_file_path,rev from extraction1,actions,scmlog "
					+ "where extraction1.commit_id="
					+ list.get(0)
					+ " and extraction1.file_id="
					+ list.get(1)
					+ " and extraction1.commit_id=actions.commit_id and extraction1.file_id=actions.file_id and extraction1.commit_id=scmlog.id";
			resultSet = stmt.executeQuery(sql);
			String line = null;
			while (resultSet.next()) {
				line = resultSet.getInt(1) + "   " + resultSet.getInt(2)
						+ "   " + resultSet.getString(3) + "   "
						+ resultSet.getString(4) + "\n";
				sBuffer.append(line);
			}
		}
		FileOperation.writeStringBuffer(sBuffer, outFile);
	}

	/**
	 * 获取构造函数指定范围内的图属性的StringBuffer.
	 * 
	 * @param infoFile
	 *            包含commit_id,file_id,rev,current_file_path的info文件
	 * @param folder
	 * @param absoFilter
	 * @return
	 * @throws IOException
	 * @throws BiffException
	 */
	public static void extracGraphAttr(String infoFile, String folder,
			String absoFilter) throws IOException, BiffException {
		int[] useless = { 0, 3, 5, 6, 8, 10, 12, 14, 17, 22, 23, 24, 25, 26, 39 };
		uselessAttriId = new HashSet<>();
		for (int integer : useless) {
			uselessAttriId.add(integer);
		}
		Map<String, List<String>> infoMap = readInfoMap(infoFile);
	    getRecord(folder, infoMap, absoFilter);
	}

	/**
	 * 将understand得到的csv格式的图文件转为ucinet所能识别的图文件格式。
	 * 
	 * @param filename
	 * @throws IOException
	 */
	public void covert(String filename) throws IOException {
		BufferedReader bReader = new BufferedReader(new FileReader(new File(
				filename)));
		bReader.readLine();
		StringBuffer PoP = new StringBuffer();
		Set<String> files = new HashSet<>();
		String line;
		while ((line = bReader.readLine()) != null) {
			String[] contents = line.split(",");
			files.add(contents[0]);
			files.add(contents[1]);
			PoP.append(contents[0] + "    " + contents[1] + "\n");
		}
		bReader.close();
		System.out.println(files.size());
		StringBuffer tile = new StringBuffer();
		tile.append("DL n=" + files.size() + "\n");
		tile.append("format = edgelist1" + "\n");
		tile.append("labels embedded:" + "\n");
		tile.append("data:" + "\n");
		tile.append(PoP);

		String outfile = filename.replace("csv", "txt");
		BufferedWriter bWriter = new BufferedWriter(new FileWriter(outfile));
		bWriter.append(tile);
		bWriter.flush();
		bWriter.close();
	}

	/**
	 * 根据信息文件获取文件基本信息，并将这些信息存储到map中。其中key值为commit_id，values为list，
	 * list中的元素为该commit_id下需要度量图属性的文件的id和路径。
	 * 
	 * @param infoFilename
	 * @throws IOException
	 */
	public static Map<String, List<String>> readInfoMap(String infoFilename)
			throws IOException {
		Map<String, List<String>> infoMap = new TreeMap<String, List<String>>();
		BufferedReader bReader = new BufferedReader(new FileReader(new File(
				infoFilename)));
		String line;
		while ((line = bReader.readLine()) != null && (!line.equals(""))) {
			String[] content = line.split("\\s+");
			if (!infoMap.containsKey(content[0])) {
				infoMap.put(content[0], new ArrayList<String>());
			}
			if (!infoMap.get(content[0])
					.contains(content[1] + "," + content[2])) {
				infoMap.get(content[0]).add(content[1] + "," + content[2]);
			}
		}
		bReader.close();
		return infoMap;
	}

	/**
	 * get the attribute-value records under the special commit_id.
	 * 
	 * @return the values of the special attributes. The results don't contain
	 *         the attributs' name. The attributs respectively are Size, Ties,
	 *         Density, nWeakComp, TwoStepReach, ReachEfficency, nBrokerage,
	 *         EgoBetewwn, nEgoBetween(from EN.txt). EffSize, Efficiency,
	 *         Constraint, Hierarchy(from S.txt). Degree, nDegree(from D.txt)
	 *         all attributes(from C.txt). Eigen(from EI.txt). information(from
	 *         info.txt)
	 *         0,1,2,4,7,9,11,13,15,16,18,19,20,21,27,28,29,30,31,32,33,34
	 *         ,35,36,37,38,40.
	 * @throws IOException
	 * @throws BiffException
	 */
	public static void getRecordForSpeciaC(String flod, String versionNum,
			List<String> filenames, int len) {
		File versionFile = new File(flod + "//" + projectName + versionNum
				+ "R.xls");
		Workbook workbook;
		System.out.println("versionNum:" + versionNum);
		try {
			workbook = Workbook.getWorkbook(versionFile);
			Sheet sheet = workbook.getSheet(0);
			if (attrsTitle == null) {
				StringBuffer temp = getRecordForSpeciaCF("ID", sheet);
				List<Integer> title = new ArrayList<>();
				title.add(-1);
				title.add(-1);
				contentMap.put(title, temp);
				attrsTitle = new StringBuffer();
				attrsTitle.append("commit_id,file_id,").append(temp);
			}
			for (String string : filenames) {
				string = string.replace("/", "\\");
				String name = string.split(",")[1].substring(len);
				String file_id = string.split(",")[0];
				StringBuffer temp = getRecordForSpeciaCF(name, sheet);
				List<Integer> key = new ArrayList<>();
				key.add(Integer.parseInt(versionNum));
				key.add(Integer.parseInt(file_id));
				contentMap.put(key, temp);
			}
		} catch (BiffException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Can't find flod " + "//" + projectName
					+ versionNum + "R.xls");
			e.printStackTrace();
		}
	}

	/**
	 * 针对指定的commit_id和文件名,获取该文件名对应的图属性,以StringBuffer的形式将其返回.
	 * 
	 * @param filename
	 * @param sheet
	 * @return
	 */
	private static StringBuffer getRecordForSpeciaCF(String filename,
			Sheet sheet) {
		StringBuffer res = new StringBuffer();
		Cell test = sheet.findCell(filename);
		try {
			int curRow = test.getRow();
			int totalCol = sheet.getColumns();
			for (int i = 0; i < totalCol; i++) {
				if (!uselessAttriId.contains(i)) {
					try {
						String content = sheet.getCell(i, curRow).getContents();
						if (content.equals("")) {
							content = "0";
						}
						res.append(content + ",");
					} catch (Exception e) {
						System.out.println("获取cell内容失败！");
					}

				}
			}
		} catch (Exception e) {
			System.out.println("找不到当前cell");
			System.out.println("cell:" + filename);
		}
		if (res.length() > 1) {
			res.delete(res.length() - 1, res.length());
		} else {
			int fills = sheet.getColumns() - uselessAttriId.size();
			for (int i = 0; i < fills; i++) {
				res.append("0,");
			}
			res.delete(res.length() - 1, res.length());
			System.out.println("contain expection point! Fill 0 automaticly!");
		}

		return res;
	}

	/**
	 * 针对指定的commit_id,以StringBuffer的形式返回其图属性结果.
	 * 
	 * @param flod
	 *            包含图属性的文件夹
	 * @param infoMap
	 *            包含rev,current_file_path等信息的文件.
	 * @param filter
	 *            由于从understand导出的问题,文件的路径存在绝对和相对的问题.
	 * @return 图属性组成的StringBuffer
	 * @throws BiffException
	 * @throws IOException
	 */
	public static void getRecord(String flod,
			Map<String, List<String>> infoMap, String filter)
			throws BiffException, IOException {
		for (String key : infoMap.keySet()) {
			getRecordForSpeciaC(flod, key, infoMap.get(key), filter.length());
		}
	}

	public static StringBuffer mergeGraphAttrToTotalAttr(String totalAttr,
			String graphAttr) throws IOException {
		BufferedReader bReader = new BufferedReader(new FileReader(new File(
				totalAttr)));
		StringBuffer res = new StringBuffer();
		Map<List<Integer>, StringBuffer> mapO = new LinkedHashMap<>();
		String line;

		line = bReader.readLine();
		StringBuffer title = new StringBuffer(line);
		title.delete(0, title.indexOf(",") + 1);

		while ((line = bReader.readLine()) != null) {
			StringBuffer curLine = new StringBuffer(line);
			curLine.delete(0, curLine.indexOf(",") + 1);
			int commit_id = Integer.parseInt(curLine.substring(0,
					curLine.indexOf(",")));
			curLine.delete(0, curLine.indexOf(",") + 1);
			int file_id = Integer.parseInt(curLine.substring(0,
					curLine.indexOf(",")));
			curLine.delete(0, curLine.indexOf(",") + 1);
			List<Integer> key = new ArrayList<>();
			key.add(commit_id);
			key.add(file_id);
			mapO.put(key, curLine);
		}

		bReader = new BufferedReader(new FileReader(new File(graphAttr)));
		line = bReader.readLine();
		StringBuffer title2 = new StringBuffer(line);
		title2.delete(0, title2.indexOf(",") + 1);
		title2.delete(0, title2.indexOf(",") + 1);
		// System.out.println(title2);
		int count = 0;

		while ((line = bReader.readLine()) != null) {
			StringBuffer buffer = new StringBuffer(line);
			int commit_id = Integer.parseInt(buffer.substring(0,
					buffer.indexOf(",")));
			buffer.delete(0, buffer.indexOf(",") + 1);
			int file_id = Integer.parseInt(buffer.substring(0,
					buffer.indexOf(",")));
			buffer.delete(0, buffer.indexOf(",") + 1);
			List<Integer> key = new ArrayList<>();
			key.add(commit_id);
			key.add(file_id);
			try {
				mapO.put(key, mapO.get(key).append(",").append(buffer));
				count++;
			} catch (Exception e) {
				System.out.println("Origin file don't contain the info of "
						+ commit_id + "_" + file_id);
			}
		}

		System.out.println("total merge number is " + count);
		res.append(title).append(",").append(title2);
		// System.out.println(title);
		res.append("\n");
		for (List<Integer> list : mapO.keySet()) {
			res.append(list.get(0)).append(",").append(list.get(1)).append(",")
					.append(mapO.get(list)).append("\n");
		}
		return res;
	}

	public static void main(String[] args) throws Exception {
		Extraction4 covert = new Extraction4("MyCamel", 2501, 2800);
		covert.printRevInfo("CamelInfo");

		Extraction4.projectName = "iTextpdf";
/*		StringBuffer sBuffer = Extraction4.getGraphAttrCsv(
				"E:\\GraphAttri\\iTextpdf\\ItextpdfInfo",
				"E:\\GraphAttri\\iTextpdf\\uciTextpdf",
				"itext/src/com/lowagie/");
		FileOperation.writeStringBuffer(sBuffer, "iTextpdfGraph.csv");*/
		StringBuffer sv = mergeGraphAttrToTotalAttr("MyItextpdf.csv",
				"iTextpdfGraph.csv");
		FileOperation.writeStringBuffer(sv, "iTextpdfCurRes.csv");
	}

	@Override
	public Map<List<Integer>, StringBuffer> getContentMap(
			List<List<Integer>> someCommit_fileIds) throws SQLException {
		Map<List<Integer>, StringBuffer> content=new LinkedHashMap<>();
		List<Integer> title = new ArrayList<>();
		title.add(-1);
		title.add(-1);
		content.put(title, contentMap.get(title));
		for (List<Integer> list : someCommit_fileIds) {
			content.put(list, contentMap.get(list));
		}
		return content;
	}

}
