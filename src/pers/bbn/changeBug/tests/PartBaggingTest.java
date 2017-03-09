package pers.bbn.changeBug.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import pers.bbn.changeBug.classify.PartBagging;

public class PartBaggingTest {

	@Test
	public final void test() {
		try {
			PartBagging partBagging = new PartBagging("MyVoldemort", 501, 600,
					"/home/niu/test/voldemortR/MyVoldemortMetrics.txt",
					"/home/niu/test/voldemortR/voldemortFiles");
		} catch (Exception e) {
			fail("创建分块CSV文件异常!");
			e.printStackTrace();
		}
	}
}
