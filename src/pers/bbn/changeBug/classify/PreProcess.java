package pers.bbn.changeBug.classify;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.LinearForwardSelection;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.filters.supervised.attribute.Discretize;

/**
 * 预处理类，主要用于属性选择。 选择的过程主要分为两步，第一步去除属性种大部分值为0的情况，默认下去除属性值99%为0的情况。 第二部根据调用weka
 * api实现无关属性的选择。
 * 
 * @author niu
 *
 */

public class PreProcess {
	/**
	 * 将csv文件转为arff文件。 需要特别注意的是类标签的设定容易出错，如果不确定类标签为多少号，需要看一下csv文件。
	 * 
	 * @param csv
	 *            需要转化的csv文件。
	 * @param arff
	 *            转化后的arff文件。
	 * @throws IOException
	 */
	public static void csvToArff(String csv, String arff) throws IOException {
		CSVLoader loader = new CSVLoader();
		loader.setSource(new File(csv));
		Instances data = loader.getDataSet();
	    data.setClass(data.attribute("bug_introducing")); //
		// 未使用weka去除id前类标签索引为11，此处一定要注意。
		ArffSaver saver = new ArffSaver();
		saver.setInstances(data);
		saver.setFile(new File(arff));
		saver.setDestination(new File(arff));
		saver.writeBatch();
	}

	/**
	 * 属性选择方法，首先删除99%为0的属性，然后使用weka的属性选择方法再进行属性选择。
	 * 
	 * @param string
	 *            需要进行属性选择的文件的路径名称
	 * @return 进行属性选择后产生的arff文件，默认放在workspace的工程目录下。
	 * @throws Exception
	 */
	public static Instances rmAttribute(String string) throws Exception {
		File file = new File(string);
		ArffLoader atf = new ArffLoader();
		atf.setFile(file);
		Instances instances = atf.getDataSet();
		int numAttr = instances.numAttributes();
		if (instances.attribute("bug_introducing").isNumeric()) {
			Discretize discretize = new Discretize();
			int[] dis = new int[1];
			dis[0] = instances.attribute("bug_introducing").index();
			int index=instances.attribute("bug_introducing").index();
			System.out.println(index);
			discretize.setAttributeIndices(index+"");
			if (discretize.batchFinished()) {
				System.out.println("类标签离散化成功");
			}
		}
		System.out.println(instances.attribute("bug_introducing"));
		instances.setClass(instances.attribute("bug_introducing"));
		int numIns = instances.numInstances();
		System.out.println("total number of the instance is " + numIns);
		System.out.println("total number of the attribute is " + numAttr);
		List<Integer> deleAttri = new ArrayList<>();
		for (int i = 0; i < numAttr; i++) {
			if (instances.attribute(i).isNumeric()) {
				double count = 0;
				for (int j = 0; j < numIns; j++) {
					if (instances.instance(j).value(i) == 0) {
						count++;
					}
				}
				double rate = count / numIns;
				if (rate > 0.99) {
					deleAttri.add(i);
				}
			}
		}
		// 怎么删掉了这么多？
		System.out.println("delete " + deleAttri.size()
				+ " redundancy attributes");
		for (int i = 0; i < deleAttri.size(); i++) {
			int deltIndex = deleAttri.get(i) - i;
			instances.deleteAttributeAt(deltIndex);
		}
		instances = selectAttr(instances);

		ArffSaver arffSaver = new ArffSaver();
		arffSaver.setInstances(instances);
		String newFile = string.split("\\.")[0] + "2F" + ".arff";
		System.out.println(newFile);
		File file2 = new File(newFile);
		if (!file2.exists()) {
			file2.createNewFile();
		}
		arffSaver.setFile(file2);
		arffSaver.writeBatch();
		return instances;
	}

	/**
	 * 使用cfs和LinearForward搜索选择属性。
	 * 
	 * @param data
	 *            需要进行属性选择的实例。
	 * @return 选择后的实例集。
	 * @throws Exception
	 */
	protected static Instances selectAttr(Instances data) throws Exception {
		System.out.println("use cfs select the attributes!");
		data.setClass(data.attribute("bug_introducing"));
		System.out.println(data.classIndex());
		AttributeSelection attributeSelection = new AttributeSelection();
		attributeSelection.setEvaluator(new CfsSubsetEval());
		LinearForwardSelection eval = new LinearForwardSelection();
		eval.setOptions(new String[] { "-I", "-K", "3000" });
		attributeSelection.setSearch(eval);
		attributeSelection.SelectAttributes(data);
		data = attributeSelection.reduceDimensionality(data);

		// Instances data2=Filter.useFilter(data, attributeSelection);
		// System.out.println(data2.numAttributes());
		return data;
	}
	
	public Instances NumLn(Instances data,String className) {
		System.out.println("数值型属性自然对数化");
		data.setClass(data.attribute(className));
		int numA=data.numAttributes();
		int numI=data.numInstances();
		for (int i = 0; i < numA; i++) {
			if (data.attribute(i).isNumeric()) {
				double min=Double.MAX_VALUE;
				if (data.attribute(i).name().contains("s")) {
					min=0.0001;
				}else {
					for (int j = 0; j <numI; j++) {
						if (data.instance(j).value(i)<min) {
							min=data.instance(j).value(i);
						}
					}
					if (min==0) {
						min=0.0001;
					}
				}
				if (min>0) {
					for (int j = 0; j <numI; j++) {
						data.instance(j).setValue(i, Math.log(data.instance(j).value(i)+min));
					}
				}else {
					double delta=Math.abs(min);
					for (int j = 0; j < numI; j++) {
						data.instance(j).setValue(i, Math.log(data.instance(j).value(i)+delta+0.0001));
					}
				}
			}
			
		}
		return data;
	}
}
