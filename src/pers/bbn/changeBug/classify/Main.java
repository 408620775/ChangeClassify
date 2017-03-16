package pers.bbn.changeBug.classify;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import pers.bbn.changeBug.extraction.MyTool;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

/**
 * 客户端类,在此处执行单个或多个arff文件的分类任务.
 * 
 * @author niu
 *
 */
public class Main {
	

	public static void main(String[] args) throws Exception {
		excuteClassifyCalfulateForSingleFile(
				"/home/niu/ExpData/jit/inputArff/bugzilla.arff",
				"/home/niu/ExpData/jit/inputArff/bugzillaR.csv");
	}

	/**
	 * 对单一的arff文件使用classifyCalculate执行分类计算,并将结果保存到文件中.
	 * 
	 * @param fileO
	 *            原始的arff类输入文件.
	 * @param fileS
	 *            保存结果的csv文件.
	 * @author niu
	 * @throws Exception
	 */
	static void excuteClassifyCalfulateForSingleFile(String fileO, String fileS)
			throws Exception {
		System.out.println("====================" + fileO
				+ "=======================");
		File arffFile = new File(fileO);
		ArffLoader arffLoader = new ArffLoader();
		arffLoader.setFile(arffFile);
		Instances instances = arffLoader.getDataSet();
		instances.setClassIndex(instances.numAttributes()-1);
		ClassifyCalculate classifyCalculate = new ClassifyCalculate(instances);
		classifyCalculate.totalCal();
		Map<List<String>, List<Double>> resMap = classifyCalculate.getRes();

		File saverFile = new File(fileS);
		if (saverFile.exists()) {
			saverFile.createNewFile();
		}
		MyTool.saveRes(resMap,saverFile);
	}

	/**
	 * 对文件夹folderO下所有的arff文件使用classifyCalculate执行分类计算,并将结果保存到指定文件夹folderS中.
	 * 
	 * @param folderO
	 *            包含待执行所有arff文件的文件夹.
	 * @param folderS
	 *            保存所有arff文件分类结果的文件夹.
	 * @author niu
	 * @throws Exception
	 */
	static void excuteClassifyCalfulateForMulFile(String folderO, String folderS)
			throws Exception {
		File file = new File(folderO);
		File save = new File(folderS);
		if (!save.exists()) {
			save.mkdir();
		}
		String[] arffFiles = file.list();
		for (String string : arffFiles) {
			String saveFile = folderS + "/" + string.replace(".arff", ".csv");
			excuteClassifyCalfulateForSingleFile(folderO + "/" + string,
					saveFile);
		}
	}
}
