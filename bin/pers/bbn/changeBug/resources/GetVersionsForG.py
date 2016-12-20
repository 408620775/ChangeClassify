#!/usr/bin/python

import sys
import getopt
import os


def usage():
	print """
Obtain different versions of the given git preject according to the infomation file.

Options:

  -h, --help                     print this usage message.
  -i, --info                     the file which contain the information of versions.
  -g, --gitfile                  the git project which need to recover different versions.
  -o, --outfile                  outfile which contain all recover versions
"""

def main(argv):
    short_opts="hi:g:o:"
    long_opts=["help","info","gitfile","outfile"]
    opts, args = getopt.getopt(argv, short_opts, long_opts)
    info_file=""
    git_file=""
    out_file=""
    for op, value in opts:
        if op in ("-i","--info"):
            info_file = value
        elif op in ("-g","--gitfile"):
            git_file = value
        elif op in ("-o","--outfile"):
            out_file = value
        elif op in ("-h","--help"):
            usage()
    f = open(info_file, 'r')      
    idRevDict={}
    for line in f:
	if line.strip()=='':
	    break
    	elements=line.split("   ")
    	if not idRevDict.has_key(elements[0]):
    		idRevDict[elements[0]]=elements[3]
    
    for element in idRevDict:
	print element,'   ',idRevDict[element].replace('\n','')
	os.chdir(git_file)
	cmd='git reset --hard '+idRevDict[element].replace('\n','')
        os.system(cmd)
        parent_path = os.path.dirname(os.getcwd())
	os.chdir(parent_path)
        cmd='cp -rf '+git_file+' '+out_file+'/'+element
        print cmd
        os.system(cmd)

if __name__ == "__main__":
    main(sys.argv[1:])


	
	
