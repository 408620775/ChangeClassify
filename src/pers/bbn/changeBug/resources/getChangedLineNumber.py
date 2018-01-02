#!/usr/bin/python
# -*- coding: UTF-8 -*- 
import MySQLdb
import os
import sys
import datetime
import getopt

def readDatabaseConfig():
    config = open('database.properties')
    username = ''
    password = ''
    line = config.readline()
    while line:
        if line.startswith("UserName"):
            username = line.split('=')[1].strip('\n')
        elif line.startswith('Password'):
            password = line.split('=')[1].strip('\n')
        line = config.readline()
    return username, password


def getLineNum(database, startNum, endNum):
    (username, password) = readDatabaseConfig()
    conn = MySQLdb.connect(host="localhost", port=3306, user=username, passwd=password, db=database)
    cursor = conn.cursor()
    cursor.execute("select id from scmlog order by commit_date")
    row = cursor.fetchall()
    resList = []
    for commit_id in row[startNum - 1:endNum]:
        sql="select commit_id,file_id,la,ld from extraction1 where commit_id=" + str(int((commit_id[0])))
        cursor.execute(sql)
        row2 = cursor.fetchall()
        for content in row2:
            tmpList=[]
            tmpList.append(int(content[0]))
            tmpList.append(int(content[1]))
            tmpList.append(int(content[2]));
            tmpList.append(int(content[3]))
            tmpList.append(int(content[2]+content[3]))
            resList.append(tmpList)
    f=open(database+"LOC",'w')
    f.write("commit_id,file_id,la,ld,la+ld")
    for list in resList:
        f.write(str(list[0])+","+str(list[1])+","+str(list[2])+","+str(list[3])+","+str(list[4]))
        f.write('\n')
#projects = [["MyLucene",1001,1500],["MyTomcat",1001,1500],["MyJedit",1001,1500],["MyAnt",1001,1500],["MySynapse",1001,1300],
 #           ["MyVoldemort",501,800],["MyItextpdf",501,800],["MyBuck",1001,1300],["MyFlink",1001,1300],["MyHadoop",5501,5800]]
projects=[["MyLucene",1001,1500]]
for s in projects:
    print s
    getLineNum(s[0],s[1],s[2])
