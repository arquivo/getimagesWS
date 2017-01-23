import sys




if __name__ == '__main__':

	#print "Input files:"
	#print "\n".join( sys.argv[1:] )
	loadImage = 0
	safeImage = 0
	jsoup	  = 0
	for fname in sys.argv[1:]:
		try:

			with open( fname ) as f:
				for line in f:
					#line = fline.replace( " " , "" )
					start = line.find( "= " ) + 2
					if "[Load image]" in line :
						loadImage +=  int( line[start:] )
					if "[Safe Image]" in line:
						safeImage +=  int( line[start:] )
					if "Time jsoup connect" in line:
						jsoup +=  int( line[start:] )

			print ("LoadImage[{0}] SafeImage[{1}] Jsoup[{2}]".format( loadImage , safeImage , jsoup ) )
			loadImage = 0
			safeImage = 0
			jsoup 	  = 0
		except (OSError, IOError) as e:
			print("Wrong file or file path")







