package pers.bbn.changeBug.extraction;

import java.util.List;
/**
 * 工具类,用于处理一些简单的数学运算,禁止实例化.
 * @author niu
 *
 */
public class MathOperation {
	private MathOperation(){
		
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
}
