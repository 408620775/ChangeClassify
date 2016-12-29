package pers.bbn.changeBug.extraction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
public final class Extraction1 extends Extraction {
	private List<Integer> commitIdPart;
	private List<String> curAttributes;
	private List<List<Integer>> commit_file_inExtracion1;

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
	 * 有些操作仅仅连接数据库就行了,并不需要其他更具体的信息,比如执行sloc的函数.目前本构造函数也只是针对sloc更清晰而已.
	 * 
	 * @param database
	 * @throws SQLException
	 */
	public Extraction1(String database) throws SQLException {
		super(database);
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

	public SQLConnection getConnection() {
		return sqlL;
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
	 * 查看当前extraction1表中所有已存在的属性,对外的接口.
	 * 
	 * @return
	 * @throws SQLException
	 */
	public List<String> getCurAttributes() throws SQLException {
		if (curAttributes == null) {
			obtainCurAttributes();
		}
		return curAttributes;
	}

	/**
	 * 将extraction1表中现有的属性填入curAttributes.
	 * 
	 * @throws SQLException
	 */
	private void obtainCurAttributes() throws SQLException {
		if (curAttributes == null) {
			curAttributes = new ArrayList<>();
			sql = "desc extraction1";
			resultSet = stmt.executeQuery(sql);
			while (resultSet.next()) {
				curAttributes.add(resultSet.getString(1));
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
			if ((!line.contains("pre")) && line.contains(".java")) {
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
	 * Assurance,增加分类实例的某些属性
	 * 
	 * @throws SQLException
	 * @throws ParseException
	 */
	public void just_in_time() throws SQLException, ParseException {
		diffusion();
		size();
		purpose();
		history();
		experience();
	}

	/**
	 * 根据论文A Large-Scale Empirical Study Of Just-in-Time Quality
	 * Assurance,增加分类实例的diffusion(传播)属性.包括NS,ND,NF和Entropy四类.
	 * 具体信息可参考论文中的定义.起始根据其实现,感觉此函数是针对commitId的,而非(commitId,fileId)对.
	 * 
	 * @throws SQLException
	 */
	public void diffusion() throws SQLException {
		if (curAttributes == null) {
			obtainCurAttributes();
		}
		if (!curAttributes.contains("ns")) {
			sql = "alter table extraction1 add (ns int(4),nd int(4),nf int(4),entropy float)";
			stmt.executeUpdate(sql);
			curAttributes.add("ns");
			curAttributes.add("nd");
			curAttributes.add("nf");
			curAttributes.add("entropy");
		}
		if (commit_file_inExtracion1 == null) {
			obtainCFidInExtraction1();
		}
		for (List<Integer> commit_fileId : commit_file_inExtracion1) {
			Set<String> subsystem = new HashSet<>();
			Set<String> directories = new HashSet<>();
			Set<String> files = new HashSet<>();
			sql = "select current_file_path from actions where commit_id="
					+ commit_fileId.get(0);
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
					+ commit_fileId.get(0);
			resultSet = stmt.executeQuery(sql);
			List<Integer> changeOfFile = new ArrayList<>();
			while (resultSet.next()) {
				changeOfFile.add(resultSet.getInt(1));
			}
			// 如果为没有相对应的更改的文件,说明该commit很有可能没有更改java文件,或者其修改的java文件都是test类型的.
			if (changeOfFile.size() == 0) {
				continue;
			}
			float entropy = MyTool.calEntropy(changeOfFile);
			float maxEntropy = (float) (Math.log(changeOfFile.size()) / Math
					.log(2));
			if (Math.abs(maxEntropy - 0) < 0.0001) {
				entropy = 0;
			} else {
				entropy = entropy / maxEntropy;
			}
			sql = "UPDATE extraction1 SET ns=" + subsystem.size() + ",nd="
					+ directories.size() + ",nf=" + files.size() + ",entropy="
					+ entropy + " where commit_id=" + commit_fileId.get(0);
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
		if (curAttributes == null) {
			obtainCurAttributes();
		}
		if (!curAttributes.contains("la")) {
			sql = "alter table extraction1 add (la int,ld int,lt int)";
			stmt.executeUpdate(sql);
			curAttributes.add("la");
			curAttributes.add("ld");
			curAttributes.add("lt");
		}

		if (commit_file_inExtracion1 == null) {
			obtainCFidInExtraction1();
		}
		List<List<Integer>> re = new ArrayList<>();
		for (List<Integer> list : commit_file_inExtracion1) {
			sql = "select id,file_id from extraction1 where commit_id="
					+ list.get(0);
			resultSet = stmt.executeQuery(sql);
			while (resultSet.next()) {
				List<Integer> temp = new ArrayList<>();
				temp.add(resultSet.getInt(1));
				temp.add(list.get(0));
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
			sql = "SELECT sloc FROM extraction1 where commit_id=" + list.get(1)
					+ " and file_id=" + list.get(2);
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

	/**
	 * 根据给定的两个commitId,获取这两个commitId所对应的时间.并需保证firstCommit出现在secondCommit之前.
	 * 
	 * @param firstCommit
	 * @param secondCommit
	 * @return 起始时间和结束时间组成的list
	 * @throws SQLException
	 */
	private List<String> getTimeRangeBetweenTwoCommit(int firstCommit,
			int secondCommit) throws SQLException {
		List<String> res = new ArrayList<>();
		sql = "select commit_date from scmlog where id=" + firstCommit
				+ " or id=" + secondCommit;
		resultSet = stmt.executeQuery(sql);
		String startTime = null;
		String endTime = null;
		while (resultSet.next()) {
			if (startTime == null) {
				startTime = resultSet.getString(1);
				continue;
			}
			if (endTime == null) {
				endTime = resultSet.getString(1);
			}
		}
		if (endTime == null) {
			endTime = startTime;
		}
		res.add(startTime);
		res.add(endTime);
		return res;
	}

	/**
	 * 根据论文A Large-Scale Empirical Study Of Just-in-Time Quality
	 * Assurance,增加分类实例的fix信息,该信息表明某次change是否fix了一个bug.由于fix
	 * bug的change相对于增加新功能的change更容易引入缺陷(论文中说的),所以该信息也许对分类有帮助.
	 * 
	 * @throws SQLException
	 */
	public void purpose() throws SQLException {
		if (curAttributes == null) {
			obtainCurAttributes();
		}
		if (!curAttributes.contains("fix")) {
			sql = "alter table extraction1 add fix tinyint(1) default 0";
			stmt.executeUpdate(sql);
			curAttributes.add("fix");
		}
		if (commit_file_inExtracion1 == null) {
			obtainCFidInExtraction1();
		}
		for (List<Integer> list : commit_file_inExtracion1) {
			sql = "UPDATE extraction1,scmlog SET fix=is_bug_fix where extraction1.commit_id=scmlog.id and extraction1.commit_id="
					+ list.get(0);
			stmt.executeUpdate(sql);
		}
	}

	/**
	 * 根据论文A Large-Scale Empirical Study Of Just-in-Time Quality
	 * Assurance,增加分类实例的历史信息,包括NDEV,AGE,NUC三部分,具体含义见论文.
	 * 
	 * @throws SQLException
	 * @throws ParseException
	 */
	public void history() throws SQLException, ParseException {
		if (curAttributes == null) {
			obtainCurAttributes();
		}
		if (!curAttributes.contains("NEDV")) {
			sql = "ALTER TABLE extraction1 ADD (NEDV int,AGE long,NUC int)";
			stmt.executeUpdate(sql);
			curAttributes.add("NEDV");
			curAttributes.add("AGE");
			curAttributes.add("NUC");
		}
		if (commit_file_inExtracion1 == null) {
			obtainCFidInExtraction1();
		}
		for (List<Integer> commit_fileIdList : commit_file_inExtracion1) {
			updateHistory(commit_fileIdList.get(0), commit_fileIdList.get(1));
		}
	}

	/**
	 * 针对给定的commitId,fileId对,update其在extraction1表中的history属性.
	 * 
	 * @param curCommitId
	 * @param curFileId
	 * @throws SQLException
	 * @throws ParseException
	 */
	// FIXME
	public void updateHistory(Integer curCommitId, Integer curFileId)
			throws SQLException, ParseException {

		int firstAddCommitId = getFirstAppearOfFile(curCommitId, curFileId)
				.get(0);
		List<String> timeRange = getTimeRangeBetweenTwoCommit(firstAddCommitId,
				curCommitId);
		String startTime = timeRange.get(0);
		String endTime = timeRange.get(1);
		int lastCommitId = getLastChangeOfFile(curCommitId, curFileId);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sql = "select commit_date from scmlog where id=" + lastCommitId;
		resultSet = stmt.executeQuery(sql);
		String lastTime = null;
		while (resultSet.next()) {
			lastTime = resultSet.getString(1);
		}
		java.util.Date lt = sdf.parse(lastTime);
		java.util.Date et = sdf.parse(endTime);
		long seconds = (et.getTime() - lt.getTime()) / 1000;
		sql = "select author_id from actions,scmlog where file_id=" + curFileId
				+ " and scmlog.id=actions.commit_id and commit_date between '"
				+ startTime + "' and '" + endTime + "'";
		resultSet = stmt.executeQuery(sql);
		int count = 0;
		Set<Integer> author_id = new HashSet<>();
		while (resultSet.next()) {
			count++;
			author_id.add(resultSet.getInt(1));
		}
		int nedv = author_id.size();
		long age = seconds;
		int nuc = count;
		sql = "update extraction1 set NEDV=" + nedv + ",AGE=" + age + ",NUC="
				+ nuc + " where commit_id=" + curCommitId + " and file_id="
				+ curFileId;
		stmt.executeUpdate(sql);
	}

	/**
	 * 获取文件的上一次change.
	 * 
	 * @param curCommitId
	 * @param curFileId
	 * @return 上一次修改的commit_id.
	 * @throws SQLException
	 */
	public int getLastChangeOfFile(int curCommitId, int curFileId)
			throws SQLException {
		sql = "SELECT type from actions where commit_id=" + curCommitId
				+ " and file_id=" + curFileId;
		resultSet = stmt.executeQuery(sql);
		String curType = null;
		while (resultSet.next()) {
			curType = resultSet.getString(1);
		}
		if (curType.equals("A") || curType.equals("C")) {
			return curCommitId;
		}
		sql = "SELECT MAX(extraction1.id) from extraction1 where id<(select id from extraction1 where commit_id="
				+ curCommitId
				+ " and file_id="
				+ curFileId
				+ ") and file_id="
				+ curFileId;
		resultSet = stmt.executeQuery(sql);
		int lastId = 0;
		while (resultSet.next()) {
			lastId = resultSet.getInt(1);
		}
		sql = "SELECT commit_id from extraction1 where id=" + lastId;
		resultSet = stmt.executeQuery(sql);
		int lastCommitId = 0;
		while (resultSet.next()) {
			lastCommitId = resultSet.getInt(1);
		}
		return lastCommitId;
	}

	/**
	 * 对于(commit_id,file_id)所对应的文件,返回该文件第一次出现,也就是该文件上次被add时的位置.默认为同一文件的file_id相同
	 * .
	 * 
	 * @param commit_id
	 * @param file_id
	 * @return 该文件对应的第一次被加入时的commit_id.
	 * @throws SQLException
	 */
	public List<Integer> getFirstAppearOfFile(int commit_id, int file_id)
			throws SQLException {
		sql = "SELECT MIN(extraction1.id) from extraction1,actions where extraction1.id<=(select id from extraction1 where commit_id="
				+ commit_id
				+ " and file_id="
				+ file_id
				+ ") and extraction1.file_id="
				+ file_id
				+ " and extraction1.commit_id=actions.commit_id and extraction1.file_id=actions.file_id";
		resultSet = stmt.executeQuery(sql);
		int id = 0;
		while (resultSet.next()) {
			id = resultSet.getInt(1);
		}
		int firtAppearCommitIdOnCurBranch = 0;
		String firstTypeOnCurBranch = null;
		String fileName = null;
		// 文件删除后file_id会不会重新分配?
		sql = "select extraction1.commit_id,type,current_file_path from extraction1,actions where extraction1.id="
				+ id
				+ " and extraction1.commit_id=actions.commit_id and extraction1.file_id=actions.file_id";
		resultSet = stmt.executeQuery(sql);
		while (resultSet.next()) {
			firtAppearCommitIdOnCurBranch = resultSet.getInt(1);
			firstTypeOnCurBranch = resultSet.getString(2);
			fileName = resultSet.getString(3);
		}
		if (fileName.contains("/")) {
			fileName = fileName.substring(fileName.lastIndexOf("/"));
			fileName = "/" + fileName;
		}
		List<Integer> res = new ArrayList<>();
		if (firstTypeOnCurBranch.equals("A")
				|| firstTypeOnCurBranch.equals("C")) {
			res.add(firtAppearCommitIdOnCurBranch);
			res.add(file_id);
			return res;
		}
		sql = "select MAX(extraction1.id) from extraction1,actions where extraction1.id<"
				+ id
				+ " and extraction1.commit_id=actions.commit_id and extraction1.file_id=actions.file_id"
				+ " and current_file_path like \"" + fileName + "\" and type='A'";
		resultSet=stmt.executeQuery(sql);
		int acturalId=0;
		while (resultSet.next()) {
			acturalId=resultSet.getInt(1);
		}
		sql="select commit_id,file_id from extraction1 where id="+acturalId;
		resultSet=stmt.executeQuery(sql);
		while (resultSet.next()) {
			res.add(resultSet.getInt(1));
			res.add(resultSet.getInt(2));
		}
		return res;
	}

	/**
	 * 根据已存在的extraction1表,获得commit_id,file_id对,
	 * 否则总是根据commitIdPart就总得去考虑文件类型是不是java文件,是否为test文件,而这一步起始在initial函数中已经做过了.
	 * 之前有几个函数就是根据commitIdPart然后再判断文件类型来获取数据,那样的方法不可取,复杂度高而且容易出错,有时间的话需要重构.
	 * 本来是不需要进行类型判断的,但是由于extraction1表中竟然还存在type为D的情况,但是initial()函数明明已经过滤了该类型的实例,
	 * 所以目前还不知道到底是哪一步导致了extraction1中还有D类型的实例,所以只能再次判读.
	 * 
	 * @throws SQLException
	 */
	// FIXME
	private void obtainCFidInExtraction1() throws SQLException {
		commit_file_inExtracion1 = new ArrayList<>();
		for (Integer integer : commitIdPart) {
			sql = "select extraction1.commit_id,extraction1.file_id from extraction1,actions where extraction1.commit_id="
					+ integer
					+ " and extraction1.commit_id=actions.commit_id and extraction1.file_id=actions.file_id and type!='D'";
			resultSet = stmt.executeQuery(sql);
			while (resultSet.next()) {
				List<Integer> temp = new ArrayList<>();
				temp.add(resultSet.getInt(1));
				temp.add(resultSet.getInt(2));
				commit_file_inExtracion1.add(temp);
			}
		}

	}

	/**
	 * 对外的接口,用于查看当前extraction1中指定范围(构造函数中指定)内的commit_id,file_id对.
	 * 
	 * @return
	 * @throws SQLException
	 */
	public List<List<Integer>> getCommit_file_inExtracion1()
			throws SQLException {
		if (commit_file_inExtracion1 == null) {
			obtainCFidInExtraction1();
		}
		return commit_file_inExtracion1;
	}

	/**
	 * 根据论文A Large-Scale Empirical Study Of Just-in-Time Quality
	 * Assurance,增加分类实例的作者经验信息,包括EXP,REXP,SEXP三部分,具体含义见论文.
	 * 
	 * @throws SQLException
	 */
	public void experience() throws SQLException {
		if (curAttributes == null) {
			obtainCurAttributes();
		}
		if (!curAttributes.contains("EXP")) {
			sql = "ALTER TABLE extraction1 ADD (EXP int,REXP float,SEXP int)";
			stmt.executeUpdate(sql);
			curAttributes.add("EXP");
			curAttributes.add("REXP");
			curAttributes.add("SEXP");
		}
		if (commit_file_inExtracion1 == null) {
			obtainCFidInExtraction1();
		}
		for (List<Integer> list : commit_file_inExtracion1) {
			updateExperience(list.get(0), list.get(1));
		}
	}

	/**
	 * 针对给定的commitId,fileId,update该实例的作者experience.目前experience表示,自fileId文件创建以来,
	 * commitId对应的作者总共更改过多少次fileId文件(不包括当前的change,因为要预测当前是否引入缺陷)
	 * .rexp标示根据时间距离现今的长短对rexp进行加权,时间单位为年.sexp为该作者对于当前子系统做过多少次更改.
	 * 真正进行测试的时候发现exp和rexp的值基本一致,这是因为很少选定的范围内commit跨越了年.
	 * 
	 * @param integer
	 * @param integer2
	 * @throws SQLException
	 */
	// FIXME
	public void updateExperience(int commitId, int fileId) throws SQLException {
		int firstAppearCommitId = getFirstAppearOfFile(commitId, fileId).get(0);
		List<String> timeRange = getTimeRangeBetweenTwoCommit(
				firstAppearCommitId, commitId);
		String startTime = timeRange.get(0);
		String endTime = timeRange.get(1);
		int exp = 0;
		float rexp = 0f;
		int sexp = 0;
		int curAuthor_id = 0;
		String curFilePath = null;
		sql = "select author_id,current_file_path from scmlog,actions where scmlog.id=actions.commit_id and scmlog.id="
				+ commitId + " and file_id=" + fileId;
		resultSet = stmt.executeQuery(sql);
		while (resultSet.next()) {
			curAuthor_id = resultSet.getInt(1);
			curFilePath = resultSet.getString(2);
		}
		sql = "select commit_date from extraction1,scmlog where extraction1.commit_id=scmlog.id and commit_date between '"
				+ startTime
				+ "' and '"
				+ endTime
				+ "' and author_id="
				+ curAuthor_id + " and file_id=" + fileId;
		resultSet = stmt.executeQuery(sql);
		List<String> datesList = new ArrayList<>();
		while (resultSet.next()) {
			datesList.add(resultSet.getString(1));
		}
		exp = datesList.size() - 1;
		if (datesList.size() == 1) {
			exp = 0;
		}
		rexp = changeWeightedByYear(datesList);

		if (!curFilePath.contains("/")) {
			System.out.println("cur file not in ang subsystem!");
		} else {
			String subsystem = curFilePath.split("/")[0];
			int count = 0;
			int curIdInExtraction1 = 0;
			sql = "select id from extraction1 where commit_id=" + commitId
					+ " and file_id=" + fileId;
			resultSet = stmt.executeQuery(sql);
			while (resultSet.next()) {
				curIdInExtraction1 = resultSet.getInt(1);
			}
			sql = "select current_file_path from extraction1,actions,scmlog where extraction1.id<"
					+ curIdInExtraction1
					+ " and extraction1.commit_id=actions.commit_id and extraction1.file_id=actions.file_id"
					+ " and actions.commit_id="
					+ "scmlog.id and author_id="
					+ curAuthor_id;
			resultSet = stmt.executeQuery(sql);
			while (resultSet.next()) {
				String lastFilePath = resultSet.getString(1);
				if (lastFilePath.split("/")[0].equals(subsystem)) {
					count++;
				}
			}
			sexp = count;
		}

		sql = "update extraction1 set EXP=" + exp + ",REXP=" + rexp + ",SEXP="
				+ sexp;
		stmt.executeUpdate(sql);
	}

	/**
	 * 根据年份对change进行加权平均,以评估rexp.默认上次更改据现在不会超过9年.有点粗糙,比如,如果同年的一月和十二月的差距会算作零年,
	 * 但是前年12月和后年一月会算作一年的差距.
	 * 
	 * @param datesList
	 *            输入的做出change的年份list.其中最后一个元素为作者当前所做的change,也就是year为当前的标准(
	 *            最新的year).
	 * @return
	 */
	private float changeWeightedByYear(List<String> datesList) {
		if (datesList.size() == 1) {
			return 0;
		}
		int[] yearsToNow = new int[10];
		int cur = getYearFromCommitdateString(datesList
				.get(datesList.size() - 1));
		for (int i = datesList.size() - 2; i >= 0; i--) {
			int last = getYearFromCommitdateString(datesList.get(i));
			yearsToNow[cur - last]++;
		}
		float res = 0f;
		for (int i = 0; i < yearsToNow.length; i++) {
			res = res + (float) yearsToNow[i] / (i + 1);
		}
		return res;
	}

	/**
	 * 根据数据库中的commit_date的字符串,获取commit_date的年份.
	 * 
	 * @param string
	 * @return
	 */
	private int getYearFromCommitdateString(String commit_date) {
		return Integer.parseInt(commit_date.split(" ")[0].split("-")[0]);
	}

}
