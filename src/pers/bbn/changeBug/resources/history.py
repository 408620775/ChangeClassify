#!/usr/bin/python
# -*- coding: UTF-8 -*- 
import MySQLdb

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
		
	def history(self):
		self.cursor.execute("desc extraction1")
 		row=self.cursor.fetchall()
		for content in row:
			self.curAttributes.add(content[0])
		if 'NEDV' not in self.curAttributes:
			self.cursor.execute("ALTER TABLE extraction1 ADD (NEDV int,AGE long,NUC int)")
		self.commit_fileIdInExtraction1=self.getCommitFileIdMap(self.commit_ids);
		#for key in self.commit_fileIdInExtraction1.keys():
			#print key,":",self.commit_fileIdInExtraction1[key]
	
	def getCommitFileIdMap(self,commit_ids):
		dict={}
		count=0
		for commit_id in commit_ids:
			self.cursor.execute("select file_id from extraction1 where commit_id="+str(commit_id))
			row=self.cursor.fetchall()
			if row:
				if commit_id not in dict.keys():
					dict[commit_id]=[]
				for file_id in row:
					count=count+1
					dict[commit_id].append(int(file_id[0]))
		#print dict
		print "the num of total commit is"+str(len(dice))+" and the total file is"+str(count)
		return dict

	def __del__(self):
		self.cursor.close ()  
		self.conn.close ()	

e=extraction1("MyVoldemort",501,800)
e.history()

