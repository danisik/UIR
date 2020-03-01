package uirApp;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Aplikace pro automatickou detekci událostí 
 * @author Vojtech Danisik
 *
 */
public class UirApp {
	
	/** název souboru s daty */
	static private String nameOfFile;
	/** název parametrizačního algoritmu */
	static private String paramAlg;
	/** název detekčního algoritmu */
	static private String detectAlg;
	/** název s anotovanými daty*/
	static private String myTweetsFile = "A16B0019P.csv";
	
	/** oddělovač dat v csv souboru */
	static private String separator = ";";
	
	/** anotované tweety (kontrolní) */
	private static List<Tweet> anotatedTweets;
	/** tweety ze souboru*/
	static private List<Tweet> tweets;
	/** mapa anotovaných tweetů rozdělená podle typu události */
	static private Map<EventType, Cluster> anotatingTweets;
	/** slovník obsahující všechny slova z sourceFile */
	static private Map<String, Double> words;
	/** kvalita detekčních algoritmů (f-míra, úplnost, preciznost) */
	static private Quality quality = new Quality();
	
	public static void main(String[] args) throws Exception {
		
		if (args.length < 3) {
			System.out.println("Zadano malo parametru");
			info();
			System.exit(0);
		}
		
		nameOfFile = args[0];
		paramAlg = args[1].toLowerCase();
		detectAlg = args[2].toLowerCase();
	
		List<String> stringTweets = openFile(nameOfFile);
		tweets = createTweets(stringTweets);
		
		switch(paramAlg) {
			case "bow": 
						bow();
						break;
			case "tf":	
						bow();
						break;
					
			case "tf-idf":
						tfIdf();
						break;
						
			default:
						System.out.println("Spatne zadany parametrizacni algoritmus");
						info();
						System.exit(0);
		
		}
		
		for(int i = 0; i < tweets.size(); i++) {
			Tweet tweet = tweets.get(i);
			tweets.get(i).setVector(createVector(tweet.getMessage()));
		}
		
		switch(detectAlg) {
			case "k-means":
						kMeans();
						break;
						
			case "1-nn":
						List<String> tweet = openFile(myTweetsFile);
						anotatedTweets = createTweets(tweet);
			
						oneNN();
						break;
		
			default:
						System.out.println("Spatne zadany detekcni algoritmus");
						info();
						System.exit(0);
		}
		
		
		
		for(EventType et: EventType.values()) {
			System.out.println(et.name());
			for(int i = 0; i < anotatingTweets.get(et).list.size(); i++) {
				anotatingTweets.get(et).list.get(i).setEvent(et);
				
				Tweet t = anotatingTweets.get(et).list.get(i);
				if(t.getIsEvent() == true && t.getIsEventReal() == true) {
					if(t.getEvent() == t.getEventReal()) {
						quality.setTP(quality.getTP() + 1);
					}
					else quality.setTN(quality.getTN() + 1);
				}
				else if(t.getIsEvent() == false && t.getIsEventReal() == false) {
					quality.setFP(quality.getFP() + 1);
				}
				else if(t.getIsEvent() == true && t.getIsEventReal() == false) {
					quality.setFN(quality.getFN() + 1);
				}
				else quality.setTN(quality.getTN() + 1);
				
				System.out.println(anotatingTweets.get(et).list.get(i));
			}
			System.out.println();
			System.out.println();
		}
		
		writeToFile();
		calculateQuality();		
	}
	
	/**
	 * Otevři soubor a načti data
	 * @param nameOfFile název souboru
	 */
	public static List<String> openFile(String nameOfFile) {
		List<String> list = new ArrayList<>();
		try {
        	BufferedReader read = new BufferedReader(new FileReader(nameOfFile));     
        	String line;
        	while ((line = read.readLine()) != null)
        	{	
        		list.add(line);
        	}
        	read.close();
		}
		catch(Exception e) {
			System.out.println("Spatny soubor");
			System.exit(0);
		}
		return list;
	}
	
	/**
	 * Uloží anotovaný tweety do souboru results.csv
	 * @throws IOException 
	 */
	public static void writeToFile() throws IOException {
		File soubor = new File("results.csv");
		if(!soubor.exists()) 
		{
			soubor.createNewFile();
		}
		
		FileWriter fw = new FileWriter(soubor.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write("det_alg;rucne;zprava\n");
		for(EventType et: EventType.values()) {
			for(int i = 0; i < anotatingTweets.get(et).list.size(); i++) {
				Tweet t = anotatingTweets.get(et).list.get(i);
				bw.write(String.valueOf(t.getIsEvent()) + " - " + t.getEvent().toString());
				bw.write(";");
				bw.write(String.valueOf(t.getIsEventReal()) + " - " + t.getEventReal().toString());
				bw.write(";");
				bw.write(t.getMessage());
				bw.write("\n");
			}
		}
		
		bw.close();
	}
	
	/**
	 * Spočítá metriky pro zvolený detekční algoritmus
	 */
	public static void calculateQuality() {
		double precision = Math.round((quality.precision() * 1000.0)) / 1000.0;
		double recall = Math.round((quality.recall() * 1000.0)) / 1000.0;
		double fMeasure = Math.round((quality.fMeasure(precision, recall) * 1000.0)) / 1000.0;
		
		System.out.println("Přesnost: " + precision);
		System.out.println("Úplnost:  " + recall);
		System.out.println("F-míra:   " + fMeasure);
	} 
	
	/**
	 * Vytvoření listu tweetů
	 * @param lines tweety uložené ve stringu
	 * @return tweety 
	 */
	public static List<Tweet> createTweets(List<String> lines) {
		List<Tweet> tweets = new ArrayList<>();
    	String[] values;
    	for(int i = 0; i < lines.size(); i++) {
    		values = lines.get(i).split(separator);
        	tweets.add(new Tweet(values[1], values[5]));
    	}
    	return tweets;
	}
	
	/**
	 * Bag of words
	 * vezmu tweet a rozdelím ho na slova
	 * zkontroluju zda ve slovníku je či neni
	 */
	public static void bow() {
		words = new HashMap<>();
		Tweet actual;
		
		for(int i = 0; i < tweets.size(); i++) {
			actual = tweets.get(i);
			
			String text = actual.getMessage();
			text = replaceChars(text);
			String[] line = text.split(" ");
			
			for(int j = 0; j < line.length; j++) {
				Double count = words.get(line[j]);
				
				if(count == null && line[j].length() > 0) {
					words.put(line[j], 1.0);
				}
				else if(count != null && line[j].length() > 0){
					words.put(line[j], count + 1);
				}
				else {
					continue;
				}
			}
		}
	}
	
	/**
	 * Vypočítá term frequency slova
	 * @param count frekvence slova
	 * @param sizeOfDictionary velikost slovníku
	 * @return hodnota tf
	 */
	public static double tfCompute(int count, int sizeOfDictionary) {
		double tf = (double)count / (double)sizeOfDictionary;
		return tf;
	}
	
	/**
	 * Term Frequency Inverse Document Frequency
	 * vezmeme slovo ze slovníku a vypočítáme pro něj hodnotu tf-idf
	 */
	public static void tfIdf() {
		Map<String, Word> tmpMap = null;
		words = new HashMap<>();
		tmpMap = tfIdfFindWords();
		double idf = 0.0;
		
		for (Map.Entry<String, Word> entry : tmpMap.entrySet()) {
			Word tmp = entry.getValue();
			idf = idfCompute(tweets.size(), tmp.getCountInDocuments());
			words.put(entry.getKey(), idf);
		}
	}
	
	/**
	 * Výpočet inverted document frequency
	 * @param sizeOfDocuments počet dokumentů
	 * @param wordCountInDocuments v kolika dokumentech se slovo nachází
	 * @return hodnota idf
	 */
	public static double idfCompute(int sizeOfDocuments, int wordCountInDocuments) {
		double tmp = (double)sizeOfDocuments / (double)wordCountInDocuments;
		double idf = Math.log(tmp);
		return idf;
	}
	
	/**
	 * Pomocná metoda pro TF-IDF
	 * Zjištění počtu slov, jejich četnosti celkově a v kolika se nachází dokumentech
	 */
	public static Map<String, Word> tfIdfFindWords() {
		Map<String, Word> map = new HashMap<>();
		Tweet actual;
		boolean newLine = true;
		
		for(int i = 0; i < tweets.size(); i++) {
			newLine = true;
			actual = tweets.get(i);
			
			String text = actual.getMessage();
			text = replaceChars(text);
			String[] line = text.split(" ");
			
			for(int j = 0; j < line.length; j++) {
				Word tmp = map.get(line[j]);
				
				if(tmp == null && line[j].length() > 0) {
					Word word = new Word(line[j]);
					map.put(line[j], word);
					newLine = false;
				}
				else if(tmp != null && line[j].length() > 0){
					Word word = map.get(line[j]);
					word.setCount(1);
					
					if (newLine == true) {
						word.setCountInDocuments(word.getCountInDocuments() + 1);
						newLine = false;
					}
					
					map.put(line[j], word);
				}
				else {
					
				}
			}
		}
		return map;
	}
	
	/**
	 * Vytvoří vektor ve velikosti slovníku ze zadaného tweetu
	 * @param tweet převáděný na vektor
	 * @return vektor reprezentující tweet
	 */
	public static double[] createVector(String tweet) {
		tweet = replaceChars(tweet);
		tweet = tweet.toLowerCase();
		double[] vector = new double[words.size()];
		double realSizeOfText = 0;
		double[] idf = null;
		if(paramAlg.equals("tf-idf")) {
			idf = new double[words.size()];
		}
		String[] txt = tweet.split(" ");
		
		int h = 0;
		for(Map.Entry<String, Double> entry : words.entrySet()) {
			if(txt.length == 0) break;
			for(int i = 0; i < txt.length; i++) {
				if(entry.getKey().equals(txt[i])) {
					realSizeOfText++;
					vector[h] += 1.0;
					if(paramAlg.equals("tf-idf")) idf[h] = entry.getValue();
					txt = ArrayUtils.removeElement(txt, txt[i]);
				}
			}
			h++;
		}
		
		if(!paramAlg.equals("bow")) {
			for(int i = 0; i < vector.length; i++) {
				if(paramAlg.equals("tf")) {
					vector[i] = tfCompute((int)vector[i], (int)realSizeOfText);
				}
				else {
					vector[i] = tfCompute((int)vector[i], (int)realSizeOfText) * (double)idf[i];
				}
			}
		}
		return vector;
	}
	
	/**
	 * detekcni algoritmus
	 * K-means klasifikátor
	 * vytvořím clustery
	 * náhodně vyberu 9 tweetů a rozdělím je do clusterů
	 * všechny tweety rozdělím do clusterů podle vzdálenosti
	 * poté projedu všechny tweety a měřím vzdálenosti od ostatních clusterů -
	 * - pokud bude vzdálenost menší, přehodím
	 */
	public static void kMeans() {
		anotatingTweets = new HashMap<>();
		Random r = new Random();
		List<Integer> indexOfTweets = new ArrayList<>();
		for(EventType et : EventType.values()) {
			anotatingTweets.put(et, new Cluster(words.size()));
			
			int number = r.nextInt(tweets.size());
			while(indexOfTweets.contains(number)) number = r.nextInt(tweets.size());
			indexOfTweets.add(number);
			
			anotatingTweets.get(et).list.add(tweets.get(number));
			anotatingTweets.get(et).computeCenter();
			
			tweets.remove(number);
		}
		
		List<Double> distances = new ArrayList<>();
		
		for(int i = 0; i < tweets.size(); i++) {		
			for(EventType et: EventType.values()) {
				double distance = euclideanDistance(tweets.get(i).getVector()
						,anotatingTweets.get(et).getCenter());
				distances.add(distance);
			}
			int indexOfEventType = distances.indexOf(getMin(distances));
			anotatingTweets.get(EventType.values()[indexOfEventType]).list.add(tweets.get(i));
			anotatingTweets.get(EventType.values()[indexOfEventType]).computeCenter();

			distances.clear();
		}
		
		boolean flag = true;
		int[] sizes = new int[EventType.values().length];
		EventType[] types = EventType.values();
		
		while(flag) {
			
			for(int i = 0; i < sizes.length; i++) {
				sizes[i] = anotatingTweets.get(types[i]).list.size();
			}
			
			for(EventType et: EventType.values()) {
				List<Tweet> tmpList = anotatingTweets.get(et).list;
				
				for(int i = 0; i < tmpList.size(); i++) {
					for(EventType ett: EventType.values()) {
						double distance = euclideanDistance(tmpList.get(i).getVector()
								,anotatingTweets.get(ett).getCenter());
						distances.add(distance);
					}
					int indexOfEventType = distances.indexOf(getMin(distances));
					
					anotatingTweets.get(EventType.values()[indexOfEventType]).list.add(tmpList.get(i));
					anotatingTweets.get(EventType.values()[indexOfEventType]).computeCenter();
					anotatingTweets.get(et).list.remove(tmpList.get(i));
					anotatingTweets.get(et).computeCenter();
					distances.clear();
				}
			}
			
			flag = false;
			for(int i = 0; i < sizes.length; i++) {
				if(sizes[i] != anotatingTweets.get(types[i]).list.size()) {
					flag = true;
					break;
				}
			}
		}
	}
	
	
	/**
	 * Vypočítá euklidovskou vzdálenost mezi 2 vektory
	 * @param vec1 první vektor
	 * @param vec2 druhý vektor
	 * @return euklidovská vzdálenost mezi 2 vektory
	 */
	public static double euclideanDistance(double[] vec1, double[] vec2) {
		 double distance = 0.0;
		 
		 for(int i = 0; i < vec1.length; i++) {
			 distance += Math.pow((vec1[i] - vec2[i]), 2);
		 }
		 
		 distance = Math.sqrt(distance);
		 distance /= vec1.length;
		 return distance;
	}
	
	/**
	 * detekcni algoritmus
	 * klasifikátor nejbližší soused 1-nn
	 * vytvořím si clustery
	 * naplním je předanotovanými daty
	 * poté procházím jednotlivé tweety z data souboru a přiřazuji
	 * ostraním z clusterů předanotovaný data 
	 */
	public static void oneNN() {
		anotatingTweets = new HashMap<>();
		
		for(EventType et : EventType.values()) {
			anotatingTweets.put(et, new Cluster(words.size()));
		}
		
		for(int i = 0; i < anotatedTweets.size(); i++) {
			Tweet actual = anotatedTweets.get(i);
			actual.setVector(createVector(actual.getMessage()));
			anotatingTweets.get(actual.getEventReal()).list.add(actual);
		}
		
		for(EventType et : EventType.values()) {
			anotatingTweets.get(et).computeCenter();
		}
		
		List<Double> distances = new ArrayList<>();
		
		for(int i = 0; i < tweets.size(); i++) {	
			tweets.get(i).setVector(createVector(tweets.get(i).getMessage()));
			for(EventType et: EventType.values()) {
				double distance = euclideanDistance(tweets.get(i).getVector()
						,anotatingTweets.get(et).getCenter());
				distances.add(distance);
			}
			int indexOfEventType = distances.indexOf(getMin(distances));
			anotatingTweets.get(EventType.values()[indexOfEventType]).list.add(tweets.get(i));
			anotatingTweets.get(EventType.values()[indexOfEventType]).computeCenter();

			distances.clear();
		}
		
		for(int i = 0; i < anotatedTweets.size(); i++) {
			Tweet actual = anotatedTweets.get(i);
			for(int h = 0; h < anotatingTweets.get(actual.getEventReal()).list.size(); h++) {
				if(actual.getMessage().equals(anotatingTweets.get(actual.getEventReal()).list.get(h).getMessage())) {
					anotatingTweets.get(actual.getEventReal()).list.remove(h);
					break;
				}
			}
		}
	}
	
	/**
	 * Vrací minimum z listu
	 * @param list list hodnot, z něhož zjišťuji minimum
	 * @return minimální hodnota v listu
	 */
	public static double getMin(List<Double> list) {
		double min = Double.MAX_VALUE;
		
		for(int i = 0; i < list.size(); i++) {
			double number = list.get(i);
			if (min > number) min = number;
		}
		
		return min;
	}
	/**
	 * Nahrazuje speciální znaky prázdným místem
	 * @param str text se spec. znaky
	 * @return text bez spec. znaků
	 */
	public static String replaceChars(String str) {
		String replaced = " ";
		
		str = str.toLowerCase();
		str = str.replace("@", replaced);
		str = str.replace("#", replaced);
		str = str.replace(".", replaced);
		str = str.replace(":", replaced);
		str = str.replace("(", replaced);
		str = str.replace(")", replaced);
		str = str.replace("'", replaced);
		str = str.replace("„", replaced);
		str = str.replace("?", replaced);
		str = str.replace("!", replaced);
		str = str.replace(",", replaced);
		str = str.replace("*", replaced);
		str = str.replace("“", replaced);
		str = str.replace("~", replaced);
		str = str.replace("…", replaced);
		str = str.replace("\"", replaced);
		str = str.replace("\\", replaced);
		str = str.replace("/", replaced);
		str = str.replace("-", replaced);
		str = str.replace("&", replaced);
		str = str.replace("|", replaced);
		str = str.replace("+", replaced);
		str = str.replace("–", replaced);
		str = str.replace(">", replaced);
		str = str.replace("<", replaced);
		str = str.replace("‚", replaced);
		str = str.replace("%", replaced);
		str = str.replace("😂", replaced);
		str = str.replace("‚", replaced);
		str = str.replace("‘", replaced);
		str = str.replace("👏", replaced);
		str = str.replace("_", replaced);
		str = str.replace("🤔", replaced);
		str = str.replace("😝", replaced);
		str = str.replace("👀", replaced);
		str = str.replace("☺", replaced);
		
		return str;
	}
	
	/**
	 * Výpis infa pro spuštění programu
	 */
	public static void info() {
		System.out.println("Navod pro spusteni programu:");
		System.out.println("Prvni parametr: mnozina_obsahujici_udalosti (csv)");
		System.out.println("Druhy parametr: parametrizacni_algoritmus");
		System.out.println("Treti parametr: algoritmus_detekce");
		System.out.println("Parametrizacni algoritmy: bow, tf, tf-idf");
		System.out.println("Detekcni algoritmy: k-means, 1-nn");
		System.out.println("Priklad: uirApp data.csv bow k-means");
	}

	/*
	//VÝPIS SLOV ZE SLOVNÍKU PODLE ZADANÉHO VEKTORU
	int numberTweet = 25;
	System.out.println(tweets.get(numberTweet).getMessage());
	List<String> str = new ArrayList<>();
	for(int i = 0; i < tweets.get(numberTweet).getVector().length; i++) {
		if (tweets.get(numberTweet).getVector()[i] > 0) {
			int h = 0;
			for (Map.Entry<String, Double> entry : words.entrySet()) {
				if (h == i) {
					str.add(entry.getKey());
					break;
				}
				h++;
			}
		}
	}
	System.out.println(str.toString());
	*/
}
