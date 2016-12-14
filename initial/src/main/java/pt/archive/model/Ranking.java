package pt.archive.model;

public class Ranking {
	
	private float score;
	private int rank;
	
	public Ranking( ){ }
	
	public Ranking(float socre, int rank) {
		super();
		this.score = socre;
		this.rank = rank;
	}
	
	public float getScore() {
		return score;
	}
	
	public void setScore(float score) {
		this.score = score;
	}
	
	public int getRank() {
		return rank;
	}
	
	public void setRank(int rank) {
		this.rank = rank;
	}
	
	@Override
	public String toString() {
		return "Ranking [socre=" + score + ", rank=" + rank + "]";
	}

}
