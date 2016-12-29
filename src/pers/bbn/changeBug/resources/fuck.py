class Foo:
	def first(self):
		print "first"
		n=2		
		m=self.third(n)
		print "m=",m
	def second(self):
		self.first()
		print "second"
	def third(self,n):
		return n**2
f=Foo()
f.second()
