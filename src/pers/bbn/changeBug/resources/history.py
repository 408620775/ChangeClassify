#!/usr/bin/python
# -*- coding: UTF-8 -*- 
import MySQLdb
import os
import sys

class extraction1:
	#定义构造方法  
	def __init__(self,database,startNum,endNum):          
		self.conn = MySQLdb.connect (host = "localhost", port=3306,user = "root", passwd = "password",db=database)
		self.cursor = self.conn.cursor()
		self.curAttributes=set()					
		self.cursor.execute("select id from scmlog order by commit_date")
		row=self.cursor.fetchall()
		self.commit_ids=[]
		self.commit_fileIdInExtraction1={}
		for content in row[startNum-1:endNum]:
			self.commit_ids.append(int(content[0]))
		#print commit_ids,len(commit_ids)
		
	def history(self,gitProject):
		self.cursor.execute("desc extraction1")
 		row=self.cursor.fetchall()
		for content in row:
			self.curAttributes.add(content[0])
		if 'NEDV' not in self.curAttributes:
			self.cursor.execute("ALTER TABLE extraction1 ADD (NEDV int,AGE long,NUC int)")
		self.commit_fileIdInExtraction1=self.getCommitFileIdMap(self.commit_ids);
		tmpFile=os.path.split( os.path.realpath( sys.argv[0] ) )[0]+'/tmp.txt'
		f=open(tmpFile,'w') #在脚本所在地创建临时文件
		os.chdir(gitProject) #进入git工程所在目录
		for key in self.commit_fileIdInExtraction1.keys():
			self.cursor.execute("select rev from scmlog where id="+str(key))
			row=self.cursor.fetchone()
			rev=row[0]
			os.system('git reset --hard '+rev)
			for fileName in self.commit_fileIdInExtraction1[key]:
				os.system('git whatchanged '+fileName+' >>'+tmpFile)
			
			
	
	def getCommitFileIdMap(self,commit_ids):
		myDict={}
		count=0
		for commit_id in commit_ids:
			self.cursor.execute("select current_file_path from extraction1,actions where extraction1.commit_id="+str(commit_id)+" and extraction1.file_id=actions.file_id and extraction1.commit_id=actions.commit_id")
			row=self.cursor.fetchall()
			if row:
				if commit_id not in myDict.keys():
					myDict[commit_id]=[]
				for res in row:
					count=count+1
					myDict[commit_id].append(res[0])
					
		print myDict
		print "the num of total commit is "+str(len(myDict))+" and the total file is "+str(count)
		return myDict

	def __del__(self):
		self.cursor.close ()  
		self.conn.close ()	

e=extraction1("MyVoldemort",501,505)
e.history("/home/niu/test/voldemort")

