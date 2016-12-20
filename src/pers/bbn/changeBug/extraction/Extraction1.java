package pers.bbn.changeBug.extraction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import weka.classifiers.meta.nestedDichotomies.ND;
import weka.classifiers.trees.j48.EntropyBasedSplitCrit;

import com.sun.org.apache.xml.internal.utils.NSInfo;

/**
 * 从miningit生成的数据库中提取一些基本信息，例如作者姓名，提交时间，累计的bug计数等信息。 构造函数中提供需要连接的数据库。
 * 根据指定的范围获取commit_id列表（按照时间顺序）。通过对各表的操作获取一些基本数据。
 * 除了基本表，miningit还需执行extension=bugFixMessage，metrics
 * 
 * @param commitIdPart
 *            指定范围内的commit的id（按照时间排序）。由于数据库的存取会消耗大量时间,
 *            所以可以用该list来替代父类中的commit_ids
 *            这样使得提取某些属性值的时候可以只针对指定数据提取,而非所有的extraction1表中的数据
 *            ,以此节约时间.需要注意的是,累计的信息的提取,例如累计的bug计数,需要extraction1中所有的数据参与.
 * @author niu
 *
 */
public class Extraction1 extends Extraction {
	List<Integer> commitIdPart;

	/**
	 * 提取第一部分change info，s为指定开始的commit_id，e为结束的commit_id
	 * 
	 * @param database
	 *            指定的miningit生成数据的数据库。
	 * @param s
	 *            指定的commit的起始值,从1开始算.
	 * @param e
	 *            指定的commit的结束值
	 * @throws Exception
	 */
	public Extraction1(String database, int s, int e) throws Exception {
		super(database);
		commitIdPart = new ArrayList<>();
		for (int j = s - 1; j < e; j++) {
			commitIdPart.add(commit_ids.get(j));
		}
	}

	/**
	 * extraction1表中必须通过执行所有的实例才能获取的信息.
	 * 
	 * @throws Exception
	 */
	public void mustTotal() throws Exception {
		CreateTable();
		initial();
		cumulative_change_count();
		bug_introducing();
		cumulative_bug_count();
	}

	/**
	 * extraction1表中可以选择一部分一部分执行的信息.
	 * 
	 * @throws Exception
	 */
	public void canPart() throws Exception {
		String name1 = null;
		for (int i = 0; i < commitIdPart.size(); i++) {
			sql = "select author_name from extraction1 where id=(select min(id) from extraction1 where commit_id="
					+ commitIdPart.get(i) + ")";
			resultSet = stmt.executeQuery(sql);
			if (!resultSet.next()) {
				continue;
			} else {
				name1 = resultSet.getString(1);
				break;
			}
		}
		String name2 = null;
		for (int i = commitIdPart.size() - 1; i >= 0; i--) {
			sql = "select author_name from extraction1 where id=(select max(id) from extraction1 where commit_id="
					+ commitIdPart.get(i) + ")";
			resultSet = stmt.executeQuery(sql);
			if (!resultSet.next()) {
				continue;
			} else {
				name2 = resultSet.getString(1);
				break;
			}
		}
		if (name1 != null && name2 != null) {
			return;
		}
		author_name(false);
		commit_day(false);
		commit_hour(false);
		change_log_length(false);
		changed_LOC(false);
	}

	/**
	 * 创建数据表extraction1。 若构造函数中所连接的数据库中已经存在extraction1表，则会产生冲突。
	 * 解决方案有2：（1）若之前的extraction1为本程序生成的表，则可将其卸载。
	 * （2）若之前的extraction1为用户自己的表，则可考虑备份原表的数据，并删除原表（建议），
	 * 或者重命名本程序中的extraction1的名称（不建议）。
	 * 
	 * @throws SQLException
	 */
	public void CreateTable() throws SQLException {
		sql = "create table extraction1(id int(11) primary key not null auto_increment,commit_id int(11),file_id int(11),author_name varchar(255),commit_day varchar(15),commit_hour int(2),"
				+ "cumulative_change_count int(15) default 0,cumulative_bug_count int(15) default 0,change_log_length int(10),changed_LOC int(13),"
				+ "sloc int(15),bug_introducing tinyint(1) default 0)";
		int result = stmt.executeUpdate(sql);
		if (result != -1) {
			System.out.println("创建表extraction1成功");
		}
	}

	/**
	 * 初始化表格。 根据指定范围内的按时间排序的commit列表（commit_ids）初始化extraction1。
	 * 初始化内容包括id，commit_id，file_id。需要注意的是，目前只考虑java文件，且不考虑java中的测试文件
	 * 所以在actions表中选择对应的项时需要进行过滤。参数表示想要提取file change信息的commit跨度
	 * 
	 * @throws SQLException
	 */
	public void initial() throws SQLException {
		System.out.println("initial the table");
		for (Integer integer : commit_ids) {
			sql = "select commit_id,file_id,file_name,current_file_path from actions,files where commit_id="
					+ integer + " and file_id=files.id and type!='D'"; // 只选取java文件,同时排除测试文件。
			resultSet = stmt.executeQuery(sql);
			List<List<Integer>> list = new ArrayList<>();
			while (resultSet.next()) {
				if (resultSet.getString(3).contains(".java")
						&& (!resultSet.getString(4).toLowerCase()
								.contains("test"))) {
					List<Integer> temp = new ArrayList<>();
					temp.add(resultSet.getInt(1));
					temp.add(resultSet.getInt(2));
					list.add(temp);
				}
			}

			for (List<Integer> list2 : list) {
				sql = "insert extraction1 (commit_id,file_id) values("
						+ list2.get(0) + "," + list2.get(1) + ")";
				stmt.executeUpdate(sql);
			}
		}
	}

	/**
	 * 获取作者姓名。如果excuteAll为真,则获取extraction1中所有数据的作者.
	 * 否则只获取commit_id在commitIdPart中的数据的作者.
	 * 
	 * @throws SQLException
	 */
	public void author_name(boolean excuteAll) throws SQLException {
		List<Integer> excuteList;
		if (excuteAll) {
			excuteList = commit_ids;
		} else {
			excuteList = commitIdPart;
		}
		System.out.println("get author_name");
		for (Integer integer : excuteList) {
			sql = "update extraction1,scmlog,people set extraction1.author_name=people.name where extraction1.commit_id="
					+ integer
					+ " and extraction1.commit_id="
					+ "scmlog.id and scmlog.author_id=people.id";
			stmt.executeUpdate(sql);
		}

	}

	/**
	 * 获取提交的日期，以星期标示。如果excuteAll为真,则获取extraction1中所有数据的日期.
	 * 否则只获取commit_id在commitIdPart中的数据的日期.
	 * 
	 * @throws SQLException
	 */
	public void commit_day(boolean excuteAll) throws SQLException {
		System.out.println("get commit_day");
		List<Integer> excuteList;
		if (excuteAll) {
			excuteList = commit_ids;
		} else {
			excuteList = commitIdPart;
		}
		Map<Integer, String> mapD = new HashMap<>(); // 加入修改日期
		for (Integer integer : excuteList) {
			sql = "select id,commit_date from scmlog where id=" + integer;
			resultSet = stmt.executeQuery(sql);
			while (resultSet.next()) {
				mapD.put(resultSet.getInt(1),
						resultSet.getString(2).split(" ")[0]);
			}
		}

		// System.out.println(mapD.size()); //测试是否提取出时间，结果正确
		Calendar calendar = Calendar.getInstance();// 获得一个日历
		String[] str = { "Sunday", "Monday", "Tuesday", "Wednesday",
				"Thursday", "Friday", "Saturday", };
		for (Integer i : mapD.keySet()) {
			int year = Integer.parseInt(mapD.get(i).split("-")[0]);
			int month = Integer.parseInt(mapD.get(i).split("-")[1]);
			int day = Integer.parseInt(mapD.get(i).split("-")[2]);

			calendar.set(year, month - 1, day);// 设置当前时间,月份是从0月开始计算
			int number = calendar.get(Calendar.DAY_OF_WEEK);// 星期表示1-7，是从星期日开始，
			mapD.put(i, str[number - 1]);
			sql = "update extraction1 set commit_day=\" " + str[number - 1]
					+ "\" where commit_id=" + i;
			stmt.executeUpdate(sql);
		}
	}

	/**
	 * 获取提交的时间，以小时标示。如果excuteAll为真,则获取extraction1中所有数据的时间.
	 * 否则只获取commit_id在commitIdPart中的数据的时间.
	 * 
	 * @throws NumberFormatException
	 * @throws SQLException
	 */
	public void commit_hour(boolean excuteAll) throws NumberFormatException,
			SQLException {
		System.out.println("get commit_hour");
		List<Integer> excuteList;
		if (excuteAll) {
			excuteList = commit_ids;
		} else {
			excuteList = commitIdPart;
		}
		Map<Integer, Integer> mapH = new HashMap<>(); // 加入修改时间
		for (Integer integer : excuteList) {
			sql = "select id,commit_date from scmlog where id=" + integer;
			resultSet = stmt.executeQuery(sql);
			while (resultSet.next()) {
				mapH.put(resultSet.getInt(1), Integer.parseInt(resultSet
						.getString(2).split(" ")[1].split(":")[0]));
			}
		}

		Iterator<Entry<Integer, Integer>> iter = mapH.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Integer, Integer> e = iter.next();
			int key = e.getKey();
			int value = e.getValue();
			sql = "update  extraction1 set commit_hour=" + value
					+ "  where commit_id=" + key;
			stmt.executeUpdate(sql);
		}
	}

	/**
	 * 获取changlog的长度。如果excuteAll为真,则获取extraction1中所有数据的changlog长度.
	 * 否则只获取commit_id在commitIdPart中的数据的changelog长度.
	 * 
	 * @throws SQLException
	 */
	public void change_log_length(boolean excuteAll) throws SQLException {
		System.out.println("get change log length");
		List<Integer> excuteList;
		if (excuteAll) {
			excuteList = commit_ids;
		} else {
			excuteList = commitIdPart;
		}
		for (Integer integer : excuteList) {
			sql = "select message from scmlog where id=" + integer;
			resultSet = stmt.executeQuery(sql);
			String message = null;
			while (resultSet.next()) {
				message = resultSet.getString(1);
			}
			sql = "update extraction1 set change_log_length ="
					+ message.length() + " where commit_id=" + integer;
			stmt.executeUpdate(sql);
		}
	}

	/**
	 * 获取源码长度。 得到表metrics的复杂度开销很大，
	 * 而得到的信息在此后的extraction2中非常方便的提取，所以真心觉得此处提起这个度量没有什么意义。
	 * 
	 * 但是由于需要获取size()中的lt属性值,还必须得有sloc才好算.而extraction2写入数据库的时间成本过高,所以在merger时,
	 * extraction2的信息往往是根据metrics的txt直接生成的,而非从数据库中提取.所以本方法也必须依赖于metrics
	 * txt来获取sloc的值.
	 * 
	 * @throws SQLException
	 * @throws IOException
	 */
	public void sloc(String metricsTxt) throws SQLException, IOException {
		File metricsFile = new File(metricsTxt);
		BufferedReader bReader = new BufferedReader(new FileReader(metricsFile));
		String line;
		String metric;
		Map<List<Integer>, Integer> countLineCode = new HashMap<>();
		while ((line = bReader.readLine()) != null) {
			if ((!line.contains("pre"))&&line.contains(".java")) {
				String commit_file_id = line.substring(
						line.lastIndexOf("\\") + 1, line.lastIndexOf("."));
				int commitId = Integer.parseInt(commit_file_id.split("_")[0]);
				int fileId = Integer.parseInt(commit_file_id.split("_")[1]);
				List<Integer> key = new ArrayList<>();
				key.add(commitId);
				key.add(fileId);
				while ((metric = bReader.readLine()) != null) {
					if (metric.contains("CountLine")) {
						countLineCode.put(key,
								Integer.parseInt(metric.split(":")[1].trim()));
						break;
					}
				}
			}
		}
		bReader.close();
		for (List<Integer> key : countLineCode.keySet()) {
			sql = "UPDATE extraction1 SET sloc=" + countLineCode.get(key)
					+ " where commit_id=" + key.get(0) + " and file_id="
					+ key.get(1);
			stmt.executeUpdate(sql);
		}
	}

	/**
	 * 获取累计的bug计数。首先得判断出某个commit_id，file_id对应的那个文件是否是bug_introducing。
	 * 也就是本程序需要在bug_introducing之后执行.
	 * 
	 * @throws Exception
	 *             主要是为了想体验一下这个异常怎么用才加的，其实没啥用，因为bug_introducing非常不可能出现除0,1外的其他值。
	 */
	public void cumulative_bug_count() throws Exception {
		System.out.println("get cumulative bug count");
		sql = "select count(*) from extraction1";
		resultSet = stmt.executeQuery(sql);
		int totalNum = 0;
		while (resultSet.next()) {
			totalNum = resultSet.getInt(1);
		}
		Map<String, Integer> fileName_curBugCount = new HashMap<>();
		for (int i = 1; i <= totalNum; i++) {
			sql = "select file_name,bug_introducing from files,extraction1 where file_id=files.id and extraction1.id="
					+ i;
			resultSet = stmt.executeQuery(sql);
			String file_name = null;
			int bug_introducing = 0;
			while (resultSet.next()) {
				file_name = resultSet.getString(1);
				bug_introducing = resultSet.getInt(2);
			}
			if (bug_introducing == 1) {
				if (fileName_curBugCount.containsKey(file_name)) {
					fileName_curBugCount.put(file_name,
							fileName_curBugCount.get(file_name) + 1);
				} else {
					fileName_curBugCount.put(file_name, 1);
				}
			} else if (bug_introducing == 0) {
				if (!fileName_curBugCount.containsKey(file_name)) {
					fileName_curBugCount.put(file_name, 0);
				}
			} else {
				Exception e = new Exception(
						"class label is mistake! not 1 and not 0");
				e.printStackTrace();
				throw e;
			}
			sql = "update extraction1 set cumulative_bug_count="
					+ fileName_curBugCount.get(file_name) + " where id=" + i;
			stmt.executeUpdate(sql);
		}
	}

	/**
	 * 获取累计的change计数。
	 * 
	 * @throws SQLException
	 */
	public void cumulative_change_count() throws SQLException {
		System.out.println("get cumulative change count");
		sql = "select count(*) from extraction1";
		resultSet = stmt.executeQuery(sql);
		int totalNum = 0;
		while (resultSet.next()) {
			totalNum = resultSet.getInt(1);
		}
		Map<String, Integer> fileName_curChangeCount = new HashMap<>();

		for (int i = 1; i <= totalNum; i++) {
			sql = "select file_name from files,extraction1 where file_id=files.id and extraction1.id="
					+ i;
			resultSet = stmt.executeQuery(sql);
			String file_name = null;
			while (resultSet.next()) {
				file_name = resultSet.getString(1);
			}
			if (fileName_curChangeCount.containsKey(file_name)) {
				fileName_curChangeCount.put(file_name,
						fileName_curChangeCount.get(file_name) + 1);
			} else {
				fileName_curChangeCount.put(file_name, 1);
			}
			sql = "update extraction1 set cumulative_change_count="
					+ fileName_curChangeCount.get(file_name) + " where id=" + i;
			stmt.executeUpdate(sql);
		}
	}

	/**
	 * 获取改变的代码的长度。 主要从hunks中提取数据，如果在miningit中hunks运行两遍会导致hunks中数据有问题，出现重复项。
	 * 数据库中为null的项取出的数值是0,而不是空。
	 * 
	 * @throws SQLException
	 */
	public void changed_LOC(boolean excuteAll) throws SQLException {
		System.out.println("get changed loc");
		List<Integer> excuteList;
		if (excuteAll) {
			excuteList = commit_ids;
		} else {
			excuteList = commitIdPart;
		}
		List<List<Integer>> re = new ArrayList<>();
		for (Integer integer : excuteList) {
			sql = "select id,file_id from extraction1 where commit_id="
					+ integer;
			resultSet = stmt.executeQuery(sql);
			while (resultSet.next()) {
				List<Integer> temp = new ArrayList<>();
				temp.add(resultSet.getInt(1));
				temp.add(integer);
				temp.add(resultSet.getInt(2));
				re.add(temp);
			}
		}
		for (List<Integer> list : re) {
			sql = "select old_start_line,old_end_line,new_start_line,new_end_line from hunks where commit_id="
					+ list.get(1) + " and file_id=" + list.get(2);
			resultSet = stmt.executeQuery(sql);
			int changeLoc = 0;
			while (resultSet.next()) {
				if (resultSet.getInt(1) != 0) {
					changeLoc = changeLoc + resultSet.getInt(2)
							- resultSet.getInt(1) + 1;
				}
				if (resultSet.getInt(3) != 0) {
					changeLoc = changeLoc + resultSet.getInt(4)
							- resultSet.getInt(3) + 1;
				}
			}
			sql = "update extraction1 set changed_LOC=" + changeLoc
					+ " where id=" + list.get(0);
			stmt.executeUpdate(sql); // 这个信息，似乎在extraction2中的detal计算时已经包含了啊。
		}
	}

	/**
	 * 相比于老的bug_introducing函数,此函数运行更快.
	 * 
	 * @throws SQLException
	 */
	public void bug_introducing() throws SQLException {
		System.out.println("get bug introducing");
		sql = "select hunks.id,file_name from hunks,files,"
				+ "(select commit_id as c,file_id as f from extraction1,scmlog where extraction1.commit_id=scmlog.id and is_bug_fix=1) as tb "
				+ "where hunks.commit_id=tb.c and hunks.file_id=tb.f and hunks.file_id=files.id;";
		resultSet = stmt.executeQuery(sql);
		Map<Integer, String> fId_name = new HashMap<>();
		while (resultSet.next()) {
			fId_name.put(resultSet.getInt(1), resultSet.getString(2));
		}
		for (Integer integer : fId_name.keySet()) {
			sql = "update extraction1,files set bug_introducing=1 where extraction1.file_id=files.id and file_name='"
					+ fId_name.get(integer)
					+ "' and commit_id IN (select bug_commit_id "
					+ "from hunk_blames where hunk_id=" + integer + ")";
			stmt.executeUpdate(sql);
		}
	}

	/**
	 * 获取类标号。 对于表extraction1中的每个实例（每一行内容）标识其是否为引入bug。bug_introducing为每个实例的类标签，用于
	 * 构建分类器。
	 * 
	 * @throws SQLException
	 */
	public void oldBug_introducing() throws SQLException {
		List<Integer> ids = new ArrayList<>();
		sql = "select id from scmlog where is_bug_fix=1";
		resultSet = stmt.executeQuery(sql);
		while (resultSet.next()) {
			if (commit_ids.contains(resultSet.getInt(1))) {
				ids.add(resultSet.getInt(1));
			}
		}

		for (Integer integer : ids) {
			sql = "select  id,file_id from hunks where commit_id=" + integer;
			resultSet = stmt.executeQuery(sql);
			List<List<Integer>> hunkFileId = new ArrayList<>(); // 有些只是行错位了也会被标记为bug_introducing。但是作为hunks的一部分好像也成。
			while (resultSet.next()) {
				List<Integer> temp = new ArrayList<>();
				temp.add(resultSet.getInt(1));
				temp.add(resultSet.getInt(2));
				hunkFileId.add(temp);
			}

			for (List<Integer> integer2 : hunkFileId) {
				sql = "update extraction1,files set  bug_introducing=1 where file_id=files.id and file_name= (select file_name from files where id="
						+ integer2.get(1)
						+ ")"
						+ " and commit_id IN (select bug_commit_id "
						+ "from hunk_blames where hunk_id="
						+ integer2.get(0)
						+ ")";
				stmt.executeUpdate(sql);
			}
		}
	}

	/**
	 * 根据论文A Large-Scale Empirical Study Of Just-in-Time Quality
	 * Assurance,增加分类实例的diffusion(传播)属性.包括NS,ND,NF和Entropy四类.
	 * 具体信息可参考论文中的定义.起始根据其实现,感觉此函数是针对commitId的,而非(commitId,fileId)对.
	 * 
	 * @throws SQLException
	 */
	public void Diffusion() throws SQLException {
		sql = "desc extraction1";
		resultSet = stmt.executeQuery(sql);
		Set<String> column = new HashSet<>();
		while (resultSet.next()) {
			column.add(resultSet.getString(1));
		}
		if (!column.contains("ns")) {
			sql = "alter table extraction1 add (ns int(4),nd int(4),nf int(4),entropy float)";
			stmt.executeUpdate(sql);
		}
		for (Integer commitId : commitIdPart) {
			Set<String> subsystem = new HashSet<>();
			Set<String> directories = new HashSet<>();
			Set<String> files = new HashSet<>();
			sql = "select current_file_path from actions where commit_id="
					+ commitId;
			resultSet = stmt.executeQuery(sql);
			while (resultSet.next()) {
				String pString = resultSet.getString(1);
				if ((!pString.endsWith(".java"))
						|| pString.toLowerCase().contains("test")) {
					continue;
				}
				String[] path = pString.split("/");
				files.add(path[path.length - 1]);
				if (path.length > 1) {
					subsystem.add(path[0]);
					directories.add(path[path.length - 2]);
				}
			}
			sql = "select changed_LOC from extraction1 where commit_id="
					+ commitId;
			resultSet = stmt.executeQuery(sql);
			List<Integer> changeOfFile = new ArrayList<>();
			while (resultSet.next()) {
				changeOfFile.add(resultSet.getInt(1));
			}
			System.out.println(commitId + ":" + changeOfFile);
			float entropy = MathOperation.calEntropy(changeOfFile);
			sql = "UPDATE extraction1 SET ns=" + subsystem.size() + ",nd="
					+ directories.size() + ",nf=" + files.size() + ",entropy="
					+ entropy + " where commit_id=" + commitId;
			stmt.executeUpdate(sql);
		}
	}

	/**
	 * 根据论文A Large-Scale Empirical Study Of Just-in-Time Quality
	 * Assurance,增加分类实例的size属性
	 * ,包括la,ld,lt三类.但似乎这三类属性跟之前的属性或者extraction2中的一些属性重合度很高.
	 * 值得注意的是,这个函数写的太烂了,跟之前的changed_LOC重合太多,但是由于创建这两个函数的时间维度不同,暂时保持这样.
	 * 
	 * @throws SQLException
	 */
	public void size() throws SQLException {
		sql = "desc extraction1";
		resultSet = stmt.executeQuery(sql);
		Set<String> column = new HashSet<>();
		while (resultSet.next()) {
			column.add(resultSet.getString(1));
		}
		if (!column.contains("la")) {
			sql = "alter table extraction1 add (la int,ld int,lt int)";
			stmt.executeUpdate(sql);
		}

		List<List<Integer>> re = new ArrayList<>();
		for (Integer integer : commitIdPart) {
			sql = "select id,file_id from extraction1 where commit_id="
					+ integer;
			resultSet = stmt.executeQuery(sql);
			while (resultSet.next()) {
				List<Integer> temp = new ArrayList<>();
				temp.add(resultSet.getInt(1));
				temp.add(integer);
				temp.add(resultSet.getInt(2));
				re.add(temp);
			}
		}
		for (List<Integer> list : re) {
			sql = "select old_start_line,old_end_line,new_start_line,new_end_line from hunks where commit_id="
					+ list.get(1) + " and file_id=" + list.get(2);
			resultSet = stmt.executeQuery(sql);
			int la = 0;
			int ld = 0;
			int lt = 0;
			while (resultSet.next()) {
				if (resultSet.getInt(1) != 0) {
					ld = ld + resultSet.getInt(2) - resultSet.getInt(1) + 1;
				}
				if (resultSet.getInt(3) != 0) {
					la = la + resultSet.getInt(4) - resultSet.getInt(3) + 1;
				}
			}
			sql = "SELECT sloc FROM extraction1 where commit_id="
					+ list.get(1) + " and file_id=" + list.get(2);
			resultSet = stmt.executeQuery(sql);
			while (resultSet.next()) {
				lt = resultSet.getInt(1);
			}
			lt = lt - la + ld;
			if (lt < 0) {
				System.out.println("lt<0!!!" + " id=" + list.get(0));
			}
			sql = "UPDATE extraction1 SET la=" + la + ",ld=" + ld + ",lt=" + lt
					+ " where id=" + list.get(0);
			stmt.executeUpdate(sql); // 这个信息，似乎在extraction2中的detal计算时已经包含了啊。
		}
	}
}
