package pt.archive.utils;

import java.math.BigDecimal;

public class SafeImage {
	
	private BigDecimal safe;
	private BigDecimal notSafe;
	
	public SafeImage( BigDecimal safe , BigDecimal notSafe ) {
		this.safe 	 = safe;
		this.setNotSafe(notSafe);
	}

	public BigDecimal getSafe() {
		return safe;
	}

	public void setSafe(BigDecimal safe) {
		this.safe = safe;
	}

	public BigDecimal getNotSafe() {
		return notSafe;
	}

	public void setNotSafe(BigDecimal notSafe) {
		this.notSafe = notSafe;
	}
		
}
