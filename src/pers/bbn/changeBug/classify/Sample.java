package pers.bbn.changeBug.classify;

import java.io.IOException;
import java.util.Random;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.instance.SMOTE;

/**
 * 采样工具类，用于对不均衡数据进行处理。方式主要有过采样和欠采样两种。
 * 
 * @author niu
 *
 */
public class Sample {

	private static String className = "bug_introducing";

	/**
	 * 查看当前采样样例的类标签.
	 * 
	 * @return
	 */
	public static String getClassName() {
		return className;
	}

	/**
	 * 设置当前采样样例的类标签,默认为"bug_introducing".
	 * 
	 * @param className
	 */
	public static void setClassName(String className) {
		Sample.className = className;
	}

	/**
	 * 构造函数,使类不可实例化.
	 * 
	 * @param claName
	 */
	private Sample() {

	}

	/**
	 * 过采样方法。
	 * 
	 * @param init
	 * @return
	 * @throws IOException
	 */
	public static Instances OverSample(Instances init) throws IOException {
		FastVector attInfo = new FastVector();
		for (int i = 0; i < init.numAttributes(); i++) {
			weka.core.Attribute temp = init.attribute(i);
			attInfo.addElement(temp);
		}
		Instances YesInstances = new Instances("DefectSample1", attInfo,
				init.numInstances());// 这里的初始容量需要注意，不要小了。
		YesInstances.setClass(YesInstances.attribute(className));

		// YesInstances.setClassIndex(init.numAttributes() - 1);
		// 未能统一的将类标签作为最后一个属性，可能导致计算上的复杂，有待改进。
		Instances Noinstances = new Instances("DefectSample2", attInfo,
				init.numInstances());
		Noinstances.setClass(Noinstances.attribute(className));
		init.setClass(init.attribute(className));
		int classIndex = init.classIndex();
		int numInstance = init.numInstances();
		int numYes = 0;
		int numNo = 0;
		for (int i = 0; i < numInstance; i++) {
			Instance temp = init.instance(i);
			double Value = temp.value(classIndex);
			if (Value == 1) { // weka的内部值并不与属性的值相对应，参考weka api。
				YesInstances.add(temp);
				numYes++;
			} else // clear change
			{
				Noinstances.add(temp);
				numNo++;
			}
		}

		// 如果数量相等，实际上是没有执行过采样的。
		if (numYes == numNo) {
			return init;
		}
		Instances res;
		if (numYes > numNo) {
			res = excuteSample(YesInstances, Noinstances, 1);
		} else {
			res = excuteSample(Noinstances, YesInstances, 1);
		}
		return res;
	}

	/**
	 * 按照给定的比例进行过抽样。
	 * 
	 * @param instances1
	 *            主实例集，即依据的实例集，也就是全部使用的实例集。
	 * @param instances2
	 *            副实例集，也就是真正实行采样的实例集。
	 * @param i
	 *            抽样后得到的不同的类标签的比例，即抽样后num(yesInstances)/num(noinstances)的比例，注意，
	 *            由于为了 加速程序运行速度，最后实验结果抽样时设置为1。
	 */
	private  static Instances excuteSample(Instances instances1,
			Instances instances2, double ratio) {
		int numSample = (int) Math.ceil(instances1.numInstances() * ratio); // 会不会由于实例数过多而崩溃？
		int numNo = instances2.numInstances();
		// instances2.randomize(random);
		Random rn = new Random();
		for (int i = 0; i < numSample; i++) {
			instances1.add(instances2.instance(rn.nextInt(numNo)));
		}
		instances1.randomize(rn);
		return instances1;
	}

	/**
	 * 欠采样方法.
	 * 
	 * @param init
	 *            用于采样的实例集.
	 * @return
	 * @throws IOException
	 */
	public static Instances UnderSample(Instances init) throws IOException {
		int numAttr = init.numAttributes();
		int numInstance = init.numInstances();

		FastVector attInfo = new FastVector();
		for (int i = 0; i < numAttr; i++) {
			weka.core.Attribute temp = init.attribute(i);
			attInfo.addElement(temp);
		}

		Instances NoInstances = new Instances("No", attInfo, numInstance);
		NoInstances.setClass(NoInstances.attribute(className));
		Instances YesInstances = new Instances("yes", attInfo, numInstance);
		YesInstances.setClass(YesInstances.attribute(className));
		init.setClass(init.attribute(className));
		int classIndex = init.classIndex();

		int numYes = 0;
		int numNo = 0;

		for (int i = 0; i < numInstance; i++) {
			Instance temp = init.instance(i);
			double Value = temp.value(classIndex);
			if (Value == 0) { // yes
				NoInstances.add(temp);
				numNo++;
			} else {
				YesInstances.add(temp);
				numYes++;
			}
		}
		if (numYes == numNo) {
			return init;
		}
		Instances res;
		if (numYes > numNo) {
			res = excuteSample(NoInstances, YesInstances, 1);
		} else {
			res = excuteSample(YesInstances, NoInstances, 1);
		}
		return res;
	}

	/**
	 * smote采样.
	 * @param ins
	 * @return
	 * @throws Exception
	 */
	public static Instances smote(Instances ins) throws Exception {
		SMOTE smote = new SMOTE();
		ins.setClass(ins.attribute(className));
		smote.setInputFormat(ins);
		Instances smoteInstances = Filter.useFilter(ins, smote);
		return smoteInstances;
	}

	/**
	 * 有放回的随机抽样.
	 * @param init 初始的样例集.
	 * @param ratio 抽样比率.
	 * @return 抽样后得到的样例集.
	 */
	public static Instances randomSampleWithReplacement(Instances init, double ratio) {
		int numAttr = init.numAttributes();
		int numInstance = init.numInstances();
		int totalNum = (int) (numInstance * ratio);

		FastVector attInfo = new FastVector();
		for (int i = 0; i < numAttr; i++) {
			weka.core.Attribute temp = init.attribute(i);
			attInfo.addElement(temp);
		}
		Instances res = new Instances("Res", attInfo, totalNum);
		Random rn = new Random();
		for (int i = 0; i < totalNum; i++) {
			res.add(init.instance(rn.nextInt(numInstance)));
		}
		res.setClass(res.attribute(className));
		return res;
	}

}