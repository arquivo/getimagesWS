package pt.archive.utils;

public class AdultFilter {
	
	private String safe;
	private String notSafe;
	
	
	public AdultFilter( String safe , String notSafe  ) {
		this.safe 		= safe;
		this.notSafe 	= notSafe;
	}


	public String getSafe() {
		return safe;
	}


	public void setSafe(String safe) {
		this.safe = safe;
	}


	public String getNotSafe() {
		return notSafe;
	}


	public void setNotSafe(String notSafe) {
		this.notSafe = notSafe;
	}
	
	
	
}
