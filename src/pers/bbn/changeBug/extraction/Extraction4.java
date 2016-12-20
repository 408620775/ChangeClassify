package pers.bbn.changeBug.extraction;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class Extraction4 extends Extraction {
	static StringBuffer attrsTitle;
	static Set<Integer> uselessAttriId;
	static String projectName;
	static int start;
	static int end;
	public Extraction4(String database, int s, int e) throws Exception {
		super(database);
	}

	public void printRevInfo(String outFile) throws SQLException, IOException {
		int idsInEx1 = 0;
		int finds = start-1;
		while (idsInEx1 == 0) {
			sql = "select min(id) from extraction1 where commit_id="
					+ commit_ids.get(finds);
			resultSet = stmt.executeQuery(sql);
			while (resultSet.next()) {
				idsInEx1 = resultSet.getInt(1);
			}
			if (idsInEx1 == 0) {
				finds++; // warning: change the value of start
			}
		}
		int ideInEx1 = 0;
		int finde = end - 1;
		while (ideInEx1 == 0) {
			sql = "select max(id) from extraction1 where commit_id="
					+ commit_ids.get(finde);
			resultSet = stmt.executeQuery(sql);
			while (resultSet.next()) {
				ideInEx1 = resultSet.getInt(1);
			}
			if (ideInEx1 == 0) {
				finde--; // warning: change the value of start
			}
		}

		sql = "select extraction1.commit_id,extraction1.file_id,current_file_path,rev from extraction1,actions,scmlog "
				+ "where extraction1.id>="
				+ idsInEx1
				+ " and extraction1.id<="
				+ ideInEx1
				+ " and extraction1.commit_id=actions.commit_id and extraction1.file_id=actions.file_id and type!='D' and extraction1.commit_id=scmlog.id";
		resultSet = stmt.executeQuery(sql);
		StringBuffer sBuffer = new StringBuffer();
		String line = null;
		while (resultSet.next()) {
			line = resultSet.getInt(1) + "   " + resultSet.getInt(2) + "   "
					+ resultSet.getString(3) + "   " + resultSet.getString(4)
					+ "\n";
			sBuffer.append(line);
		}

		FileOperation.writeStringBuffer(sBuffer, outFile);
	}

	public static StringBuffer getGraphAttrCsv(String infoFile, String folder,String absoFilter)
			throws IOException, BiffException {
		int[] useless = { 0, 3, 5, 6, 8, 10, 12, 14, 17, 22, 23, 24, 25, 26, 39 };
		uselessAttriId = new HashSet<>();
		for (int integer : useless) {
			uselessAttriId.add(integer);
		}
		Map<String, List<String>> infoMap = readInfoMap(infoFile);
		StringBuffer res = mergeRecord(folder, infoMap,absoFilter);
		return res;
	}

	/**
	 * 将understand得到的csv格式的图文件转为ucinet所能识别的图文件格式。
	 * 
	 * @param filename
	 * @throws IOException
	 */
	public void covert(String filename) throws IOException {
		BufferedReader bReader = new BufferedReader(new FileReader(new File(filename)));
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
		tile.append("format = nodelist1" + "\n");
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
	 * list中的元素为该commit_id下需要度量图属性的文件的名称。
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
	public static StringBuffer getRecordForSpeciaC(String flod, String versionNum,
			List<String> filenames,int len) {
		StringBuffer res = new StringBuffer();
		File versionFile = new File(flod + "//"+projectName + versionNum + "R.xls");
		Workbook workbook;
		System.out.println("versionNum:" + versionNum);
		try {
			workbook = Workbook.getWorkbook(versionFile);
			Sheet sheet = workbook.getSheet(0);
			if (attrsTitle == null) {
				StringBuffer temp = getRecordForSpeciaCF("ID", sheet);
				attrsTitle = new StringBuffer();
				attrsTitle.append("commit_id,file_id,").append(temp);
				res.append(attrsTitle + "\n");
			}
			for (String string : filenames) {
				string = string.replace("/", "\\");
				String name = string.split(",")[1].substring(len);
				String file_id = string.split(",")[0];
				StringBuffer temp = getRecordForSpeciaCF(name, sheet);
				res.append(versionNum + "," + file_id + "," + temp + "\n");

			}
		} catch (BiffException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Can't find flod " + "//"+projectName + versionNum
					+ "R.xls");
			e.printStackTrace();
		}

		return res;
	}

	private static StringBuffer getRecordForSpeciaCF(String filename, Sheet sheet) {
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
			int fills=sheet.getColumns()-uselessAttriId.size();
			for (int i = 0; i <fills; i++) {
				res.append("0,");
			}
			res.delete(res.length()-1, res.length());
			System.out.println("contain expection point! Fill 0 automaticly!");
		}

		return res;
	}

	public static StringBuffer mergeRecord(String flod,
			Map<String, List<String>> infoMap,String filter) throws BiffException,
			IOException {
		StringBuffer res = new StringBuffer();
		for (String key : infoMap.keySet()) {
			StringBuffer temp = getRecordForSpeciaC(flod, key, infoMap.get(key),filter.length());
			res.append(temp);
		}
		return res;
	}

	public static StringBuffer mergeGraphAttrToTotalAttr(String totalAttr,
			String graphAttr) throws IOException {
		BufferedReader bReader = new BufferedReader(new FileReader(new File(
				totalAttr)));
		StringBuffer res = new StringBuffer();
		Map<List<Integer>, StringBuffer> mapO = new HashMap<>();
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
		//System.out.println(title2);
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
		
		Extraction4.projectName="iTextpdf";
		StringBuffer sBuffer=Extraction4.getGraphAttrCsv("E:\\GraphAttri\\iTextpdf\\ItextpdfInfo",
		"E:\\GraphAttri\\iTextpdf\\uciTextpdf","itext/src/com/lowagie/");
		FileOperation.writeStringBuffer(sBuffer, "iTextpdfGraph.csv");
		StringBuffer sv = mergeGraphAttrToTotalAttr("MyItextpdf.csv",
				"iTextpdfGraph.csv");
		FileOperation.writeStringBuffer(sv, "iTextpdfCurRes.csv");
	}

}
