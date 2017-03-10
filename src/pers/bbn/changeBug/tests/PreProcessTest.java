package pers.bbn.changeBug.tests;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import pers.bbn.changeBug.classify.PreProcess;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.core.Instances;

public class PreProcessTest {

	@Test
	public final void test() {
		try {
			Instances instances = PreProcess
					.readInstancesFromCSV("/home/niu/bufferDir/MyVoldemortBow.csv");
			PreProcess.selectAttributes(instances, new CfsSubsetEval(), new BestFirst());
			//System.out.println(instances);
		} catch (Exception e) {
			fail();
		}

	}

}
