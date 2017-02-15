package project;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import com.aliasi.hmm.HmmDecoder;
import com.aliasi.tag.Tagger;
import com.aliasi.tag.Tagging;
import com.aliasi.tokenizer.IndoEuropeanTokenizer;


public class Program {
	
	public static void main(String[] args) throws FileNotFoundException {
		// parse input params
	    File file = new File("input.txt");
	    Scanner sc = new Scanner(file);
	    String phrase = sc.nextLine();
		String[] tokens = IndoEuropeanTokenizer.tokenize(phrase);
		
		//Tagger tagger = new HmmDecoder()
	}

}
