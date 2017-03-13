package pers.bbn.changeBug.classify;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import db.g;
import pers.bbn.changeBug.extraction.Extraction1;
import pers.bbn.changeBug.extraction.Extraction2;
import pers.bbn.changeBug.extraction.Extraction3;
import sun.security.jca.GetInstance;
import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

/**
 * 根据不同部分的属性分别构建不同的分类器,最后将所有分类器集成,按照投票结果决定实例最终的标签.
 * 执行过程:(1)执行构造函数获得CSV文件.(2)执行getInstanceList获取instances实例集.
 * @author niu
 *
 */
public class PartBagging {
	/**
	 * 
	 */
	
	private Classifier baseClassifier;

	public Classifier getBaseClassifier() {
		return baseClassifier;
	}

	public void setBaseClassifier(Classifier baseClassifier) {
		this.baseClassifier = baseClassifier;
	}

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

	/**
	 * 根据给定的content,以及类标签的对应关系,将实例信息写入csv文件,以便读取instance实例.
	 * 
	 * @param fileName
	 *            写入的文件的名称,命名规则为 工程名+MateData/Metrics/Bow.csv
	 * @param content
	 *            包含实例属性信息的Map,不包含类标签.
	 * @param labelsMap
	 *            包含类标签信息的Map.
	 * @throws Exception
	 */
	
}
