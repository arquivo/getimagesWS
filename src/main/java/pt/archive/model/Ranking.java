package pt.archive.model;

public class Ranking {
	
	private float score;
	private String type;
	
	public Ranking( ){ }
	
	public Ranking(float socre, String rank) {
		super();
		this.score = socre;
		this.type = rank;
	}
	
	public float getScore() {
		return score;
	}
	
	public void setScore(float score) {
		this.score = score;
	}
	
	public String getRank() {
		return type;
	}
	
	public void setRank(String type) {
		this.type = type;
	}
	
	@Override
	public String toString() {
		return "Ranking [score=" + score + ", rank=" + type + "]";
	}

}
