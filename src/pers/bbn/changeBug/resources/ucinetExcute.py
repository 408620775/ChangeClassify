# -*- coding: UTF-8 -*- 
'''
Created on 2016年11月14日

@author: Administrator
'''
import understandExcute as un
import win32gui 
import time
import os
import re
import shutil
import win32api
import win32con
def importDL():
    un.leftOneClick(468, 877)
    time.sleep(5)
    un.leftDoubleClick(402, 15)#max the window
    time.sleep(0.5)
    win32api.keybd_event(0x10,0,0,0)
    time.sleep(0.2)
    win32api.keybd_event(0x10,0,win32con.KEYEVENTF_KEYUP,0)#close Chinese input
    un.leftOneClick(52, 33)
    un.leftOneClick(127, 153)
    un.leftOneClick(436, 153)

    un.leftOneClick(962,414)#choose dl file
    un.leftOneClick(230,165)
    un.key_Ok()
    un.key_Ok()
 
def egBM():   
    un.leftOneClick(224, 32)#egbm
    time.sleep(1)
    un.leftOneClick(299, 135)
    time.sleep(1)
    un.leftOneClick(602,167)
    time.sleep(1)
    un.key_Ok()

def egSH(): 
    un.leftOneClick(224, 32)#egsh
    un.leftOneClick(299, 135)
    un.leftOneClick(596, 231)
    un.leftOneClick(966, 361)
    #un.leftOneClick(236, 188)#ordinary position
    un.leftOneClick(238, 162)
    
    un.key_Ok()
    un.key_Ok()

def degree():
    un.leftOneClick(224, 32)
    un.leftOneClick(290, 157)
    un.leftOneClick(569, 184)
    procHandle = win32gui.FindWindow(None,"  Degree Centrality")
    win32gui.SetWindowPos(procHandle,win32con.HWND_TOPMOST,200,200,0,0,win32con.SWP_NOSIZE);
    time.sleep(0.5)
    un.leftOneClick(718, 295)
    #un.leftOneClick(236, 188)
    un.leftOneClick(238, 162)
    un.key_Ok()
    un.key_Ok()

def eig():
    
    un.leftOneClick(224, 32)
    un.leftOneClick(290, 157)
    un.leftOneClick(618, 252)
    procHandle = win32gui.FindWindow(None,"Eigenvector Centrality")
    win32gui.SetWindowPos(procHandle,win32con.HWND_TOPMOST,200,200,0,0,win32con.SWP_NOSIZE);
    time.sleep(1)
    un.leftOneClick(773, 300)
    #un.leftOneClick(236, 188)
    un.leftOneClick(238, 162)
    un.key_Ok()
    un.key_Ok()

def clo():
    un.leftOneClick(224, 32)
    un.leftOneClick(290, 157)
    un.leftOneClick(614, 414)
    un.leftOneClick(456, 363)
   # un.leftOneClick(236, 188)#initial graph file
    un.leftOneClick(238, 162)
    un.key_Ok()
    un.key_Ok()

def info():
    un.leftOneClick(224, 32)
    un.leftOneClick(290, 157)
    un.leftOneClick(639, 503)
    procHandle = win32gui.FindWindow(None,"Information Centrality")
    win32gui.SetWindowPos(procHandle,win32con.HWND_TOPMOST,200,200,0,0,win32con.SWP_NOSIZE);
    time.sleep(0.5)
    un.leftOneClick(810, 292)
    #un.leftOneClick(236, 188)#initial graph file
    un.leftOneClick(238, 162)
    un.key_Ok()
    un.key_Ok()

def bet():
    un.leftOneClick(224, 32)
    un.leftOneClick(290, 157)
    un.leftOneClick(609, 554)
    un.leftOneClick(916, 552)
    un.leftOneClick(952, 421)
    #un.leftOneClick(236, 188)#initial graph file
    un.leftOneClick(238, 162)
    un.key_Ok()
    un.key_Ok()
    
def join(fileName):
    un.leftOneClick(47, 32)
    un.leftOneClick(127, 386)
    un.leftOneClick(461, 467)
    #win32api.keybd_event(un.VK_CODE['j'][0],0,0,0)
    #time.sleep(0.02)
    #win32api.keybd_event(un.VK_CODE['j'][0],0,win32con.KEYEVENTF_KEYUP,0)
    
    un.leftOneClick(662, 296)#choose the title of join
    un.leftOneClick(529, 313)
    
    #un.leftOneClick(539, 393)#egnet
    un.leftOneClick(538, 536)
    un.leftOneClick(751, 415)#add
    #un.leftOneClick(576, 524)#SH
    un.leftOneClick(549, 522)
    un.leftOneClick(751, 415)#add
    #un.leftOneClick(575, 446)#degree
    un.leftOneClick(539, 444)
    un.leftOneClick(751, 415)#add
    #un.leftOneClick(563, 419)#closeness
    un.leftOneClick(547, 419)
    un.leftOneClick(751, 415)#add
    #un.leftOneClick(554, 458)#eig
    un.leftOneClick(554, 458)
    un.leftOneClick(751, 415)#add
    #un.leftOneClick(554, 473)#info
    un.leftOneClick(555, 470)
    un.leftOneClick(751, 415)#add
    #un.leftOneClick(563, 407)#bet
    un.leftOneClick(550, 404)
    un.leftOneClick(751, 415)#add
    
    un.leftOneClick(598, 628)
    un.key_del()
    un.key_input(fileName)
    un.key_Ok()

def exportExcel():
    un.leftOneClick(51, 30)
    un.leftOneClick(168, 175)
    un.leftOneClick(450, 334)
    un.leftOneClick(1002, 355)
    
    un.leftDoubleClick(1018, 245)
    #un.leftOneClick(231, 393)
    un.leftOneClick(240, 374)
    un.key_Ok()
    un.key_Ok()
    un.leftOneClick(491, 23)
    un.leftOneClick(1574, 10)#close ucinet

def chooseRange(x0,y0,x1,y1):
    un.leftOneClick(x0, y0)
    win32api.keybd_event(0x10,0,0,0)
    un.leftOneClick(x1, y1)
    win32api.keybd_event(0x10,0,win32con.KEYEVENTF_KEYUP,0)
      
def solveFiles(folder):
    os.mkdir(folder,0777)
    time.sleep(3)
    chooseRange(222,169,258,737)#choose the files and ctrl x+ctrl v
    win32api.keybd_event(0x11,0,0,0)
    un.key_input('x')
    win32api.keybd_event(0x11,0,win32con.KEYEVENTF_KEYUP,0)
    time.sleep(0.5)
    un.leftDoubleClick(234, 129)
    time.sleep(1)
    win32api.keybd_event(0x11,0,0,0)
    un.key_input('v')
    time.sleep(0.5)
    win32api.keybd_event(0x11,0,win32con.KEYEVENTF_KEYUP,0)
    un.leftOneClick(286, 66)#return uncinet data
    time.sleep(1)

    win32api.keybd_event(0x11,0,0,0)
    un.key_input('x')
    win32api.keybd_event(0x11,0,win32con.KEYEVENTF_KEYUP,0)
    time.sleep(0.5)
    un.leftDoubleClick(278, 150)
    time.sleep(1)
    win32api.keybd_event(0x11,0,0,0)
    un.key_input('v')
    win32api.keybd_event(0x11,0,win32con.KEYEVENTF_KEYUP,0)
    time.sleep(0.5)
    un.leftOneClick(286, 66)#return uncinet data
def closeTxt():
    un.mouse_move(665, 877)
    win32api.mouse_event(win32con.MOUSEEVENTF_RIGHTDOWN, 0, 0, 0, 0)
    time.sleep(0.05)
    win32api.mouse_event(win32con.MOUSEEVENTF_RIGHTUP, 0, 0, 0, 0)  
    time.sleep(0.2)
    un.mouse_move(665,838)
    win32api.mouse_event(win32con.MOUSEEVENTF_LEFTDOWN, 0, 0, 0, 0)
    time.sleep(0.05)
    win32api.mouse_event(win32con.MOUSEEVENTF_LEFTUP, 0, 0, 0, 0)
    time.sleep(0.2)
def totalForOne(fileName,folder):
    importDL()
    egBM()
    egSH()
    degree()
    eig()
    clo()
    info()
    bet()
    join(fileName)
    exportExcel()
    solveFiles(folder)
    closeTxt()

def ucinet(filepath):
    i=0
    files=os.listdir(filepath)
    versionR=[]
    version=[]
    
    for fileName in files:
        if os.path.isfile(filepath+"\\"+fileName):
            version.append(int(fileName.split('.')[0]))
            print versionR[-1]
    version.sort()
    for i in range(len(version)):
        versionR.append(str(version[i])+'R') 
        totalForOne(versionR[-1],filepath+"\\"+fileName.split('.')[0])
        
if __name__ == "__main__":
    time.sleep(5)
    ucinet("C:\\Users\\Administrator\\Documents\\UCINET data")
    