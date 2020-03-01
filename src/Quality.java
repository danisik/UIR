package uirApp;

/**
 * Třída reprezentující kvalitu detekčního algoritmu
 * @author Vojtech Danisik
 *
 */
public class Quality {
	
	/** správně odhadnuté pozitivní případy, true odhad, true výsledek*/
	private double TP;
	/** špatně odhadnuté pozitivní případy, true odhad, true výsledek*/
	private double TN;
	/** špatně odhadnuté pozitivní případy, false odhad, true výsledek */
	private double FP;
	/** špatně odhadnuté negativní případy, true odhad, false výsledek */
	private double FN;
	
	public Quality() {
		this.TP = 0;
		this.TN = 0;
		this.FP = 0;
		this.FN = 0;
	}
	
	public double getTN() {
		return TN;
	}

	public void setTN(double tN) {
		TN = tN;
	}

	public double getTP() {
		return TP;
	}
	public void setTP(double tP) {
		TP = tP;
	}
	public double getFP() {
		return FP;
	}
	public void setFP(double fP) {
		FP = fP;
	}
	public double getFN() {
		return FN;
	}
	public void setFN(double fN) {
		FN = fN;
	}
	
	/**
	 * Vypočítá přesnost detekčního algoritmu 
	 * TP = správně odhadnuté pozitivní případy
	 * FP = špatně odhadnuté pozitivní případy
	 * TP / (TP + FP)
	 * @return hodnotu přesnosti detekčního algoritmu
	 */
	public double precision() {
		return TP / (TP + FP);
	}
	
	/**
	 * Vypočítá úplnost detekčního algoritmu 
	 * TP = správně odhadnuté pozitivní případy
	 * FN = špatně odhadnuté negativní případy
	 * TP / (TP + FN)
	 * @return hodnotu úplnosti detekčního algoritmu
	 */
	public double recall() {	
		return TP / (TP + FN);
	}
	
	/**
	 * Vypočítá f-míru detekčního algoritmu
	 * @return f-míra
	 */
	public double fMeasure(double precision, double recall) {	
		return (2 * precision * recall) / (precision + recall);
	}
	
	
}
