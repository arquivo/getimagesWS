package pt.archive.utils;

public class AdultFilter {
	
	private float safe;
	private float notSafe;
	
	
	public AdultFilter( float safe , float notSafe  ) {
		this.safe 		= safe;
		this.notSafe 	= notSafe;
	}


	public float getSafe() {
		return safe;
	}


	public void setSafe(float safe) {
		this.safe = safe;
	}


	public float getNotSafe() {
		return notSafe;
	}


	public void setNotSafe(float notSafe) {
		this.notSafe = notSafe;
	}
	
	
	
}
