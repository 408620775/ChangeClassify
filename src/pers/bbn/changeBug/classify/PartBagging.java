package pers.bbn.changeBug.classify;

import java.util.Enumeration;

import weka.classifiers.Classifier;
import weka.classifiers.RandomizableIteratedSingleClassifierEnhancer;
import weka.core.AdditionalMeasureProducer;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformationHandler;
import weka.core.WeightedInstancesHandler;

/**
 * 根据不同部分的属性分别构建不同的分类器,最后将所有分类器集成,按照投票结果决定实例最终的标签.
 * 执行过程:(1)执行构造函数获得CSV文件.(2)执行getInstanceList获取instances实例集.
 * 
 * @author niu
 *
 */
public class PartBagging extends RandomizableIteratedSingleClassifierEnhancer
		implements  AdditionalMeasureProducer,
		TechnicalInformationHandler {

	private static final long serialVersionUID = 1L;

	/**
	 * 构造函数,初始化变量
	 * 
	 * @param database
	 *            git工程对应的数据库
	 * @param start
	 *            待研究版本区间的起始号
	 * @param end
	 *            待研究版本区间的结束号
	 * @throws Exception
	 */
	public PartBagging(String database, int start, int end, String metricsFile,
			String projectHome) throws Exception {
	}

	public void buildClassifier() throws Exception {

	}

	@Override
	public TechnicalInformation getTechnicalInformation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration enumerateMeasures() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getMeasure(String measureName) {
		// TODO Auto-generated method stub
		return 0;
	}



}
