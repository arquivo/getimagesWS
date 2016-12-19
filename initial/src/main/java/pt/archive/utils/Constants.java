package pt.archive.utils;

public class Constants {
	
	public static final String andOP 		 	= "&";
	public static final String inOP  		 	= "+";
	public static final String equalOP 		 	= "=";
	public static final String colonOP 		 	= ":";
	public static final String urlBarOP 	 	= "/";
	public static final String quotationMarks	= "\"";
	public static final String negSearch		= "-";
	public static final String siteSearch		= "site:";
	public static final float  titleScore 	 	= 2.0f;
	public static final float  altScore 	 	= 2.0f;
	public static final float  incrementSrcMore = 3;
	public static final float  incrScoreSrcLess = 0.5f;
	public static final String[] stopWord = { //86 stop words
		"a","e","o","é","à","s","n","d","c","i","p","de","do","da","para","em","os","com",
		"no","por","que","um","as","se","na","uma","não","dos","pt","ao","mais","ou","das","nos",
		"como","you","estes","te","it","your","então","essa","esse","estas","are","an","not",
		"that","sob","am","out","seu","este","ser","sua","são","aos","esta","já","the","tem","sem",
		"está","pela","pelo","foi","mas","às","pode","para-a","ter","to","nas","me","só","www","of",
		"here","&","seus","by","and","on","for","há","és"
	};
	public static final int timeoutConn 	= 5000;
	public static final int timeoutreadConn = 10000;
	
}
