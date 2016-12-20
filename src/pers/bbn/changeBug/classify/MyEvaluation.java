package pers.bbn.changeBug.classify;

import java.util.Random;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.Range;

/**
 * 改进传统的Evalution类.
 * 传统的Evaluation方法在构建分类器模型的时候无法控制训练集的均衡性.本类可以保证在训练阶段时使用的训练集的均衡性.
 * 
 * @author niu
 *
 */
public final class MyEvaluation extends Evaluation {
	private int choose = 0;

	/**
	 * 获取均衡性类型.若choose==0,则表示在训练模型时不执行均衡操作.若choose==1,则表示训练模型时采用欠采样方式达到训练集的均衡性;
	 * 若choose==2,则表示使用过采样.
	 * 
	 * @return
	 */
	public MyEvaluation(Instances data,int choose) throws Exception {
		super(data);
		this.choose=choose;
	}

	@Override
	public void crossValidateModel(Classifier classifier, Instances data,
			int numFolds, Random random, Object... forPredictionsPrinting)
			throws Exception {
		// Make a copy of the data we can reorder
		data = new Instances(data);
		data.randomize(random);
		if (data.classAttribute().isNominal()) {
			data.stratify(numFolds);
		}
		if (forPredictionsPrinting.length > 0) {
			// print the header first
			StringBuffer buff = (StringBuffer) forPredictionsPrinting[0];
			Range attsToOutput = (Range) forPredictionsPrinting[1];
			boolean printDist = ((Boolean) forPredictionsPrinting[2])
					.booleanValue();
			printClassificationsHeader(data, attsToOutput, printDist, buff);
		}
		// Do the folds
		for (int i = 0; i < numFolds; i++) {
			Instances trainOrigin = data.trainCV(numFolds, i, random);
			Instances train = null;
			if (choose == 0) {
				train = trainOrigin;
			} else if (choose == 1) {
				train = Sample.UnderSample(trainOrigin);
			} else {
				train = Sample.OverSample(trainOrigin);
			}
			setPriors(train);
			Classifier copiedClassifier = Classifier.makeCopy(classifier);
			copiedClassifier.buildClassifier(train);
			Instances test = data.testCV(numFolds, i);
			evaluateModel(copiedClassifier, test, forPredictionsPrinting);
		}
		m_NumFolds = numFolds;
	}

}
