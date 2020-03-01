package uirApp;

/**
 * Jednoduchá reprezentace slova pro algoritmus TF-IDF
 * @author Vojtech Danisik
 *
 */
public class Word {
	
	/** text slova */
	private String word;
	/** počet výskytů v dokumentech */
	private Integer count;
	/** v kolika dokumentech se slovo vyskytuje */
	private Integer countInDocuments;
	
	public Word (String word) {
		this.word = word;
		this.count = 1;
		this.countInDocuments = 1;
	}

	public String getWord() {
		return word;
	}
	
	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public Integer getCountInDocuments() {
		return countInDocuments;
	}

	public void setCountInDocuments(Integer countInDocuments) {
		this.countInDocuments = countInDocuments;
	}
}
