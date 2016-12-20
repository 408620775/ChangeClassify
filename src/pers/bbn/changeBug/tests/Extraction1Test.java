package pers.bbn.changeBug.tests;

import org.junit.BeforeClass;
import org.junit.Test;

import pers.bbn.changeBug.extraction.Extraction1;
import pers.bbn.changeBug.extraction.SQLConnection;

public class Extraction1Test {
	SQLConnection sqlL;
	String sql;
	@BeforeClass
	public final void setup() {
		sqlL=new SQLConnection("MyVoldemort");
		sqlL.connect();
	}
	@Test
	public final void testCalEntropy(){
		
	}

}
