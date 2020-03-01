package uirApp;

import java.util.ArrayList;

public class Cluster {
	
	/** střed všech tweetů v listu */
	private double[] center;
	/** velikost vektoru reprezentující střed shluku */
	private int sizeOfVector;
	/** list tweetů ve shluku */
	public ArrayList<Tweet> list = new ArrayList<Tweet>();
	
	public Cluster(int sizeOfVector) {
		this.sizeOfVector = sizeOfVector;
		center = new double[sizeOfVector];
	}
	
	public void setCenter(double[] center) {
		this.center = center;
	}
	
	public double[] getCenter() {
		return center;
	}
	
	/**
	 * Vypočítá nový střed z listu vektorů ve shluku (pro k-means)
	 */
	public void computeCenter() {
		double[] newCenter = new double[sizeOfVector];
		if(list.size() > 0) {
			double top = 0.0;
			double bottom = list.size();
			
			for(int i = 0; i < newCenter.length; i++) {
				top = 0.0;
				for(int h = 0; h < list.size(); h++) {
					top += list.get(h).getVector()[i]; 
				}
				newCenter[i] = top/bottom;
			}
		}
		setCenter(newCenter);
	}
}
