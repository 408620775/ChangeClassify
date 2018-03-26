import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import pers.bbn.changeBug.extraction.Extraction1;
import pres.bbn.changeBug.exception.InsExistenceException;

public final class Extraction1Test {
	static Extraction1 extraction1;

	@BeforeClass
	public static void setup() throws Exception {
		extraction1 = new Extraction1("MyVoldemort", 501, 800);
	}

	public void testObtainCurAttributes() throws SQLException {
		List<String> curAtt = extraction1.getCurAttributes();
		TestCase.assertTrue(curAtt.get(0).equals("id")
				&& curAtt.get(1).equals("commit_id"));
	}

	public void testGetCommit_file_inExtracion1() throws SQLException {
		List<List<Integer>> commit_file_idInExtraction1 = extraction1
				.getCommit_file_inExtracion1();
		TestCase.assertEquals(commit_file_idInExtraction1.size(), 519);
	}

	public void testGetContentMap() throws Exception {
		Map<List<Integer>, StringBuffer> contentMap = extraction1
				.getContentMap(extraction1.getCommit_file_inExtracion1());
		for (List<Integer> key : contentMap.keySet()) {
			System.out.println(contentMap.get(key));
		}
	}

	@Test(expected=InsExistenceException.class)
	public void testGetClassLabels() throws SQLException, InsExistenceException {
		List<List<Integer>> commit_fileIds = new ArrayList<>();
		commit_fileIds.add(Arrays.asList(497, 686));
		commit_fileIds.add(Arrays.asList(499, 160));
		commit_fileIds.add(Arrays.asList(513, 160));
		ExpectedException thrown = ExpectedException.none();
		Map<List<Integer>, String> labels = extraction1
				.getClassLabels(commit_fileIds);
		TestCase.assertEquals(labels.get(Arrays.asList(497, 686)), "1");
		TestCase.assertEquals(labels.get(Arrays.asList(499, 160)), "1");
		TestCase.assertEquals(labels.get(Arrays.asList(513, 160)), "0");
		commit_fileIds.clear();
		commit_fileIds.add(Arrays.asList(497, 685));
		thrown.expect(InsExistenceException.class);
		labels = extraction1.getClassLabels(commit_fileIds);

	}
}
