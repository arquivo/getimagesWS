import sys


def extractValue( line ):
	try:
		value = int( line )
		return value
	except ValueError:
		return 0



if __name__ == '__main__':

	loadImage = 0
	safeImage = 0
	jsoup	  = 0
	query = ""
	flagsafeImage = ""
	for fname in sys.argv[1:]:
		try:

			with open( fname ) as f:
				for line in f:
					#line = fline.replace( " " , "" )
					start = line.find( "= " ) + 2
					#print line

					if "safeImage[" in line:
						start = line.find( "safeImage[" ) + 10
						flagsafeImage = line[start:-2]
					if "input => " in line:
						start = line.find( "=> " ) + 3
						query = line[start:-1]	
					if "[Load image]" in line:
						loadImage +=  extractValue( line[start:] )
					if "[Safe Image]" in line: 
						safeImage +=  extractValue( line[start:] )
					if "Time jsoup connect" in line:
						jsoup +=  extractValue( line[start:] )

			print (" Query[{0}] SafeImage[{1}] LoadImage[{2}] SafeImage[{3}] Jsoup[{4}]".format( query , flagsafeImage , loadImage , safeImage , jsoup ) )
			loadImage = 0
			safeImage = 0
			jsoup 	  = 0
			query = ""
			flagsafeImage = ""
		except (OSError, IOError) as e:
			print("Wrong file or file path")







