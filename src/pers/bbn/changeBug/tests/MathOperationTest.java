package pers.bbn.changeBug.tests;

import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import pers.bbn.changeBug.extraction.MyTool;

public class MathOperationTest {

	@Test
	public final void calEntropy() {
		List<Integer> list=Arrays.asList(30,20,10);
		float res=MyTool.calEntropy(list);
		assertEquals(1.46, res, 0.001);
	
	}

}
