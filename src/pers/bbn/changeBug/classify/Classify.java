package pers.bbn.changeBug.classify;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;

/**
 * 分类器类,用于执行分类任务,并输出分类结果.
 * 
 * @author niu
 *
 */
public class Classify {
	private Classifier cla;
	private Evaluation eval;
	private Instances ins;
	private List<Double> res;

	/**
	 * 查看当前所用评估对象.
	 * 
	 * @return 当前的评估对象.
	 */
	public Evaluation getEval() {
		return eval;
	}

	/**
	 * 设置评估结果时使用的评估对象.
	 * 
	 * @param eval
	 */
	public void setEval(Evaluation eval) {
		this.eval = eval;
	}

	/**
	 * 返回分类器评估结果.返回的实际上是结果的保护性拷贝,可以避免对象中的结果被恶意篡改,写着玩的.
	 * 
	 * @return 模型评估结果
	 */
	public List<Double> getRes() {
		// return res;
		List<Double> result = null;
		try {
			result = new ArrayList<Double>(res);
			return result;
		} catch (NullPointerException e) {
			System.out.println("the list of result is null!");
			e.printStackTrace();
			return result;
		}
	}

	/**
	 * 返回分类器.
	 * 
	 * @return 使用的weka中的分类器.
	 */
	public Classifier getCla() {
		return cla;
	}

	/**
	 * 设置使用的分类器.
	 * 
	 * @param cla
	 *            要设置的分类器.
	 */
	public void setCla(Classifier cla) {
		this.cla = cla;
	}

	/**
	 * 返回构建分类器的训练集.
	 * 
	 * @return
	 */
	public Instances getIns() {
		return ins;
	}

	/**
	 * 设置构建分类器的训练集.
	 * 
	 * @param ins
	 */
	public void setIns(Instances ins) {
		this.ins = ins;
	}

	/**
	 * 分类器构造函数.
	 * 
	 * @param classifier
	 *            用于分类的分类器.
	 * @param instances
	 *            用于构建分类器的实例集.
	 * @param claName
	 *            用于分类的类标签,默认为bug_introducing.
	 */
	public Classify(Classifier classifier, Instances instances) {
		this.cla = classifier;
		this.ins = instances;
	}

	/**
	 * 先通过分类器构造分类器类,稍后传入训练集.
	 * 
	 * @param classifier
	 */
	public Classify(Classifier classifier) {
		this.cla = classifier;
	}

	/**
	 * 執行10*10折交叉验证.eval的实现扩展性差,但就目前来说是相对比较好的折衷.
	 * 
	 * @param choose
	 * @throws Exception
	 */
	void Evaluation100(int choose) throws Exception {
		List<List<Double>> TenRes = new ArrayList<>();
		int flods=1;
		for (int i = 0; i < flods; i++) {
			List<Double> tempResult=Evaluation10(choose, i);
			TenRes.add(tempResult);
		}
		res=new ArrayList<>();
		for (int i = 0; i < TenRes.get(0).size(); i++) {
			double temp = 0.0;
			for (int j = 0; j < flods; j++) {
				temp = temp + TenRes.get(j).get(i);
			}
			res.add(temp / flods);
		}

	}

	/**
	 * 单纯的十折交叉验证
	 * 
	 * @param choose
	 * @throws Exception
	 */
	public List<Double> Evaluation10(int choose, int randomNum)
			throws Exception {
		List<Double> LocRes = new ArrayList<>();
		eval = new MyEvaluation(ins, choose);
		eval.crossValidateModel(cla, ins, 10, new Random(randomNum));
		LocRes.add(eval.recall(0));
		LocRes.add(eval.recall(1));
		LocRes.add(eval.precision(0));
		LocRes.add(eval.precision(1));
		LocRes.add(eval.fMeasure(0));
		LocRes.add(eval.fMeasure(1));
		LocRes.add(eval.areaUnderROC(1));
		LocRes.add(Math.sqrt(LocRes.get(0) * LocRes.get(1)));//???
		LocRes.add(1 - eval.errorRate());
		res = LocRes;
		return LocRes;
	}

}
