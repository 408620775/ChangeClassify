#!/usr/bin/python
# -*- coding: UTF-8 -*- 
import MySQLdb
import os
import sys
import datetime
import getopt

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
		self.month={'Jan':1,'Feb':2,'Mar':3,'Apr':4,'May':5,'Jun':6,'Jul':7,'Aug':8,'Sep':9,'Oct':10,'Nov':11,'Dec':12}
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
			self.conn.commit()
		self.commit_fileIdInExtraction1=self.getCommitFileIdMap(self.commit_ids);
		tmpFile=os.path.split( os.path.realpath( sys.argv[0] ) )[0]+'/tmp.txt'
		f=open(tmpFile,'w') #在脚本所在地创建临时文件
		os.chdir(gitProject) #进入git工程所在目录
		for key in self.commit_fileIdInExtraction1.keys():
			print 'commitId:'+str(key)
			self.cursor.execute("select rev from scmlog where id="+str(key))
			row=self.cursor.fetchone()
			rev=row[0]
			os.system('git reset --hard '+rev)
			for content in self.commit_fileIdInExtraction1[key]:
				file_id=content[0]
				file_name=content[1]
				print 'file_id:',file_id
				os.system('git whatchanged '+file_name+' >'+tmpFile)
				(nedv,age,nuc)=self.dealWithGitLog(tmpFile)
				self.cursor.execute('update extraction1 set NEDV='+str(nedv)+',AGE='+str(age)+',NUC='+str(nuc)+' where commit_id='+str(key)+' and file_id='+str(file_id))
				self.conn.commit()
	
	def getCommitFileIdMap(self,commit_ids):
		myDict={}
		count=0
		for commit_id in commit_ids:
			self.cursor.execute("select extraction1.file_id,current_file_path from extraction1,actions where extraction1.commit_id="+str(commit_id)+" and extraction1.file_id=actions.file_id and extraction1.commit_id=actions.commit_id")
			row=self.cursor.fetchall()
			if row:
				if commit_id not in myDict.keys():
					myDict[commit_id]=[]
				for res in row:
					count=count+1
					tmp=[res[0],res[1]]
					myDict[commit_id].append(tmp)
					
		print myDict
		print "the num of total commit is "+str(len(myDict))+" and the total file is "+str(count)
		return myDict

	def __del__(self):
		self.cursor.close ()  
		self.conn.close ()	
	
	def dealWithGitLog(self,logFile):
		f = open(logFile)
		count=0;
		authors=set()
		age=0
		curDateTime=0
		lastDateTime=0
		line=f.readline()
		while line:
			if line.startswith('commit'):
				curRev=line.split()[1]
				
			if line.startswith('Author'):
				count=count+1
				author=line.split(':')[1].split('<')[0].strip()
				authors.add(author)
				if curDateTime==0:
					line=f.readline()
					array=line.split()
					time=str(self.month[array[2]])+' '+array[3]+' '+array[4]+' '+array[5]
					curDateTime=datetime.datetime.strptime(time,'%m %d %H:%M:%S %Y')
				elif lastDateTime==0:
					line=f.readline()
					array=line.split()
					time=str(self.month[array[2]])+' '+array[3]+' '+array[4]+' '+array[5]
					lastDateTime=datetime.datetime.strptime(time,'%m %d %H:%M:%S %Y')
			line=f.readline()
		if lastDateTime==0:
			lastDateTime=curDateTime
		return len(authors),(curDateTime-lastDateTime).seconds,count

def usage():
	print """
Obtain history infomation for the specified data range, and the result will be saved in the extraction1 table in the database which miningit obtain.

Options:

  -h, --help                     print this usage message.
  -d,--database                  the database which will save the result.
  -s, --start                    start commit_id of the  date range.
  -e, --end                      end commit_id of the data range.
  -g, --gitfile                  the git project which need to obtain the history information.
"""				  

def execute(argv,short_opts, long_opts):
	opts, args = getopt.getopt(argv, short_opts, long_opts)
	database=''
	start=0
	end=0
	gitfile=''
	for op, value in opts:
        	if op in ("-d","--database"):
            		database = value
        	elif op in ("-s","--start"):
            		start = value
        	elif op in ("-e","--end"):
            		end = value
		elif op in ("-g","--gitfile"):
			gitfile=value
        	elif op in ("-h","--help"):
            		usage()
			return
	print database,start,end,gitfile
	if database and start and end and gitfile:
		e=extraction1(database,int(start),int(end))
		e.history(gitfile)
	else:
		print 'Parameter does not meet the requirements.'

if __name__=='__main__':
	short_opts="hd:s:e:g:"
	long_opts=["help","database","start","end","gitfile"]
	argv=sys.argv[1:]
	execute(argv,short_opts, long_opts)
