package pers.bbn.changeBug.extraction;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;


public class Main {
	static FileOperation fileOperation;

	public static void main(String[] args) throws Exception {
		Extraction1 extraction1=new Extraction1("MyVoldemort",501,800);
		extraction1.Diffusion();
	}

	static public void Automatic1(String project, int start_commit_id,
			int end_commit_id) throws Exception {
		String database = "My"
				+ project.toLowerCase().substring(0, 1).toUpperCase()
				+ project.toLowerCase().substring(1);
		Extraction1 extraction1 = new Extraction1(database, start_commit_id, end_commit_id);
		extraction1.mustTotal();
		extraction1.canPart();

		Extraction2 extraction2 = new Extraction2(database, start_commit_id,
				end_commit_id);
		extraction2.Get_icfId();
		// Process process = Runtime.getRuntime().exec(
		// "/bin/sh /home/niu/workspace/changeClassify/src/extraction/GetFile.sh "
		// + project);
		// System.out.println("the exit value of process is "
		// + process.exitValue());
	}

	static public void Automatic2(String project, int start_commit_id,
			int end_commit_id) throws Exception {
		String database = "My"
				+ project.toLowerCase().substring(0, 1).toUpperCase()
				+ project.toLowerCase().substring(1);
		Extraction1 extraction1=new Extraction1(database, start_commit_id, end_commit_id);
		extraction1.canPart();
		
		Extraction2 extraction2 = new Extraction2(database, start_commit_id,
				end_commit_id);
		String metric = database + "Metrics.txt";
		extraction2.extraFromTxt(metric);

		Extraction3 extraction3 = new Extraction3(database,
				extraction2.getId_commitId_fileIds(), project + "Files");
		fileOperation = new FileOperation();
		Merge merge = new Merge(extraction3.getContent(),
				extraction2.getContentMap(),
				extraction2.getId_commitId_fileIds(), database);
		fileOperation.writeContent(merge.merge123(), database + ".csv");
		fileOperation.writeDict(database + "Dict.txt",
				extraction3.getDictionary());
	}

}
