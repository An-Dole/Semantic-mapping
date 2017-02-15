package project;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import com.aliasi.hmm.HiddenMarkovModel;
import com.aliasi.hmm.HmmDecoder;
import com.aliasi.tag.Tagging;
import com.aliasi.tokenizer.*;
import com.aliasi.util.Streams;


public class Main {

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		// declaration of the files and streams
		File input = new File("input.txt");
        File output = new File("output.txt");
        Scanner scanner = new Scanner(input);
        PrintStream ps = new PrintStream(output);
        
        String buffer; //temporary storage
        
        // just as an example so far
        TokenizerFactory TOKENIZER_FACTORY = new RegExTokenizerFactory("(-|'|\\d|\\p{L})+|\\S");
        
        // reading the language model (default trained)
		ObjectInputStream objIn = new ObjectInputStream(new FileInputStream("languageModel.HiddenMarkovModel"));
		HiddenMarkovModel hmm = (HiddenMarkovModel) objIn.readObject();
		Streams.closeInputStream(objIn);
		HmmDecoder decoder = new HmmDecoder(hmm);
		
		// tokens (words) collection
		List<String> tokenList = new ArrayList<String>();
		
		// loop to collect all the tokens from the text
		while(scanner.hasNext()){
			buffer = scanner.nextLine();
			char[] cs = buffer.toCharArray();
			Tokenizer tokenizer = TOKENIZER_FACTORY.tokenizer(cs,0,cs.length);
		    String[] tokens = tokenizer.tokenize();
		    tokenList.addAll(Arrays.asList(tokens));
		};
		
		// let us now tag the tokens with some part of speech from the model loaded above
		Tagging<String> tagging = decoder.tag(tokenList);
		
		// result output
		for (int i = 0; i < tagging.size(); ++i)
		    ps.println(tagging.token(i) + " : " + tagging.tag(i));
		
		
		/*
		// IndoEuropean tokenizer check
		Tokenization tokenization = new Tokenization(buffer, IndoEuropeanTokenizerFactory.INSTANCE);
		for(String t : tokenization.tokens())
		{
			ps.println(t);
		}
		*/

	}

}




