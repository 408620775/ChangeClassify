# -*- coding: UTF-8 -*- 
'''
Created on 2016��11��15��

@author: Administrator
'''
import os
import shutil


topFile="E:\\GraphAttri\\iTextpdf\\iTextpdfGraphFiles"
saveFile="E:\\GraphAttri\\iTextpdf\\uciTextpdf"
files=os.listdir(topFile)
for file in files:
    cFiles=os.listdir(topFile+"\\"+file)
    for ccfile in cFiles:
        if ccfile.endswith('xls'):
            botm=topFile+"\\"+file+"\\"+ccfile
            shutil.copyfile(botm, saveFile+"\\"+ccfile)
            print ccfile