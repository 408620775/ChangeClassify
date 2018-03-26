name = 'MyBuckDict.txt'
f = open(name)
dict = {}
for line in f.readlines():
	content = line.split()
	if len(content) ==1 :
		continue
	dict[content[1]] = content[0]
f.close()
f = open(name,'w')
for k,v in dict.items():
	f.write(k)
	f.write('   ')
	f.write(v+'\n')
f.close()
