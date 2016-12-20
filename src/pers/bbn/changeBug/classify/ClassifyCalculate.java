package pers.bbn.changeBug.classify;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import weka.classifiers.Classifier;
import weka.core.Instances;

/**
 * 分类计算类。用于计算各种分类模型下的结果。
 * 
 * @param classifys
 *            使用的分类器模型,当前采用的分类模型包括J48,NaiveBayes,和SMO。
 * @param methods
 *            使用的采样模型,当前采用的采样模型包括标准(不采用),欠采样,过采样以及与Bagging方法的结合。
 * @author niu
 *
 */
public class ClassifyCalculate {
	private String[] classifys = { "weka.classifiers.trees.J48",
			"weka.classifiers.bayes.NaiveBayes",
			"weka.classifiers.functions.SMO" };
	// "weka.classifiers.meta.AdaBoostM1" };
	private String[] methods = { "standard", "undersample", "oversample", "bagging",
			"underBagging", "overBagging" };
	private Instances ins;
	private Map<List<String>, List<Double>> res;
	private String className = "bug_introducing";
	
	/**
	 * 获取各种分类模型+采样模型下的结果.
	 * @return 所以分类模型和所有采样模型组合下的结果.
	 */
	public Map<List<String>, List<Double>> getRes() {
		return res;
	}

	/**
	 * 查看分类时的类标签.
	 * @return 分类时指定的类标签.
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * 设置分类时的类标签,默认为"bug_introducing".
	 * @param className
	 */
	public void setClassName(String className) {
		this.className = className;
	}

	/**
	 * 构造函数，初始化要使用的用例集。
	 * 
	 * @param instances
	 *            将要用于分类的用力集，可能是不均衡的数据。
	 */
	public ClassifyCalculate(Instances instances, String claName) {
		this.ins = instances;
		this.className = claName;
		this.ins.setClass(ins.attribute(className));
		res = new LinkedHashMap<>();
	}

	/**
	 * 针对不同的分类器不同的采样方法,获取不同情况下的分类评估结果.
	 * 
	 * @throws Exception
	 */
	public void totalCal() throws Exception {
		Classify classify = null;
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				List<String> keyList = new ArrayList<>();
				keyList.add(classifys[i]);
				keyList.add(methods[j]);
				classify = new Classify((Classifier) Class
						.forName(classifys[i]).newInstance(), ins, className);
				classify.Evaluation100(j);
				res.put(keyList, classify.getRes());
			}

			for (int j = 3; j < 6; j++) {
				List<String> keyList = new ArrayList<>();
				keyList.add(classifys[i]);
				keyList.add(methods[j]);
				MyBagging bagging = new MyBagging(className, j - 3);
				// Bagging bagging=new Bagging();
				bagging.setClassifier((Classifier) Class.forName(classifys[i])
						.newInstance());
				classify = new Classify(bagging, ins, className);
				classify.Evaluation100(0);
				res.put(keyList, classify.getRes());
			}
		}
		DecimalFormat df = new DecimalFormat("0.00");
		for (List<String> m : res.keySet()) {
			for (String string : m) {
				System.out.print(string + "  ");
			}
			for (Double value : res.get(m)) {
				System.out.print(df.format(value) + "  ");
			}
			System.out.println();
		}
	}
}
