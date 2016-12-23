package pers.bbn.changeBug.extraction;

import java.util.Date;
import java.util.List;
/**
 * 工具类,用于处理一些简单的数学运算,禁止实例化.
 * @author niu
 *
 */
public class MyTool {
	private MyTool(){
		
	}
	/**
	 * 根据给定的一些列数据,计算该list的熵.
	 * @param changeOfFile
	 * @return
	 */
	public static float calEntropy(List<Integer> changeOfFile) throws IllegalArgumentException{
		float sum=0f;
		for (Integer integer : changeOfFile) {
			if (integer<0) {
				System.out.println("概率值不能为小于等于0!");
				throw new IllegalArgumentException();
			}else if (integer==0) {
				System.out.println("abnormal point");
			}
			sum+=integer;
		}
		float entropy=0f;
		for (Integer integer : changeOfFile) {
			if (integer==0) {
				continue;
			}
			float ratio=integer/sum;
			entropy-=ratio*(Math.log(ratio)/Math.log(2));
		}
		return entropy;
	}
	/**
	 * 格式化打印数据库数据,每个条目是一个list.
	 * @param data
	 */
	public static void printDBdata(List<List<String>> datas) {
		if (datas==null) {
			return;
		}
		int[] lengths=new int[datas.get(0).size()];
		for (List<String> data: datas) {
			for (int i = 0; i < data.size(); i++) {
				if (data.get(i).length()>lengths[i]) {
					lengths[i]=data.get(i).length();
				}
			}
		}
		for (List<String> data : datas) {
			for (int i = 0; i < data.size(); i++) {
				if (data.get(i).length()<=lengths[i]) {
					String tmp=data.get(i);
					for (int j = 0; j < lengths[i]-data.get(i).length(); j++) {
						tmp=tmp+" ";
					}
					System.out.print(tmp+"  ");
					System.out.print("|  ");
				}
			}
			System.out.println();
		}
	}
}
