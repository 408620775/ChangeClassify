package pers.bbn.changeBug.tests;

import java.sql.SQLException;
import java.util.List;
import junit.framework.TestCase;

import org.junit.BeforeClass;
import org.junit.Test;

import pers.bbn.changeBug.extraction.Extraction1;

public class Extraction1Test {
	static Extraction1 extraction1;

	@BeforeClass
	public static void setup() throws Exception {
		extraction1 = new Extraction1("MyVoldemort", 501, 800);
	}

	@Test
	public final void testObtainCurAttributes() throws SQLException {
		System.out.println("Hello");
		List<String> curAtt = extraction1.getCurAttributes();
		System.out.println(curAtt);
		TestCase.assertTrue(curAtt.get(0).equals("id")
				&& curAtt.get(1).equals("commit_id")
				&& curAtt.get(curAtt.size() - 1).equals("lt"));
	}

}
