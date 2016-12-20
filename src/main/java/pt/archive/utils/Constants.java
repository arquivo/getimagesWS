package pt.archive.utils;

import java.io.Serializable;

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
	public static final int timeoutConn 		= 5000;
	public static final int timeoutreadConn 	= 10000;
	public static final String sortCriteria 	= "sort:";
	public static final String typeSearch 		= "type:";
	public static final String sizeSearch 		= "size:";
	public static enum criteriaRank implements Serializable {
		SCORE,
		NEW,
		OLD;
		
		public String toString( ){
			switch( this ) {
				case SCORE:
					return "score";
				case NEW:
					return "new";
				case OLD:
					return "old";
			}
			return null;
		}
		
		public static criteriaRank valueOf( criteriaRank  enumType, String value ){
			if( value.equalsIgnoreCase( SCORE.toString( ) ) )
	            return criteriaRank.SCORE;
	        else if( value.equalsIgnoreCase( NEW.toString( ) ) )
	            return criteriaRank.NEW;
	        else if( value.equalsIgnoreCase( OLD.toString( ) ) )
	            return criteriaRank.OLD;
	        else
	            return null;
			
		}
		
	}

}
