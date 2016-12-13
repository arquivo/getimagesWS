package pt.archive.model;

import java.math.BigDecimal;

public class ItemOpenSearch {
	
	private long id;
	private long index;
	private String url;
	private String link;
	private String arcname;
	private BigDecimal arcoffset;
	private String digest;
	private String tstamp;
	private BigDecimal contentLength;
	private String primaryType;
	private String subtype;
	
	public ItemOpenSearch( ) { }
	
	public ItemOpenSearch( long id, long index, String url, String link, String arcname, BigDecimal arcoffset, String digest, String tstamp,
			BigDecimal contentLength, String primaryType, String subtype ) {
		this.id 			= id;
		this.index 			= index;
		this.url			= url;
		this.link 			= link;
		this.arcname 		= arcname;
		this.arcoffset 		= arcoffset;
		this.digest 		= digest;
		this.tstamp 		= tstamp;
		this.contentLength 	= contentLength;
		this.primaryType 	= primaryType;
		this.subtype 		= subtype;
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getIndex() {
		return index;
	}
	public void setIndex(long index) {
		this.index = index;
	}
	public String getArcname() {
		return arcname;
	}
	public void setArcname(String arcname) {
		this.arcname = arcname;
	}
	public BigDecimal getArcoffset() {
		return arcoffset;
	}
	public void setArcoffset(BigDecimal arcoffset) {
		this.arcoffset = arcoffset;
	}
	public String getDigest() {
		return digest;
	}
	public void setDigest(String digest) {
		this.digest = digest;
	}
	public String getTstamp() {
		return tstamp;
	}
	public void setTstamp(String tstamp) {
		this.tstamp = tstamp;
	}
	public BigDecimal getContentLength() {
		return contentLength;
	}
	public void setContentLength(BigDecimal contentLength) {
		this.contentLength = contentLength;
	}
	public String getPrimaryType() {
		return primaryType;
	}
	public void setPrimaryType(String primaryType) {
		this.primaryType = primaryType;
	}
	public String getSubtype() {
		return subtype;
	}
	public void setSubtype(String subtype) {
		this.subtype = subtype;
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}
	
	@Override
	public String toString() {
		return "ItemXML [id=" + id + ", index=" + index + ", arcname=" + arcname + ", arcoffset=" + arcoffset
				+ ", digest=" + digest + ", tstamp=" + tstamp + ", contentLength=" + contentLength + ", primaryType="
				+ primaryType + ", subtype=" + subtype + "]";
	}

	
	
}
