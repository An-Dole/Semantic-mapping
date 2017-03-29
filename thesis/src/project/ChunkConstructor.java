package project;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import com.aliasi.hmm.HiddenMarkovModel;
import com.aliasi.hmm.HmmDecoder;
import com.aliasi.tag.Tagging;
import com.aliasi.tokenizer.*;
import com.aliasi.util.Streams;

public class ChunkConstructor {

	private Tagging<String> tags;

	private int reqCounter = 1;
	
	private String type;

	public void process(Scanner sc, PrintStream ps) throws Exception {
		tagging(sc);
		codeBuilder(constructPhrases(), ps);
	}

	@SuppressWarnings("deprecation")
	private void tagging(Scanner scanner) throws Exception, IOException {
		// temporary storage
		String buffer;

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
		while (scanner.hasNext()) {
			buffer = scanner.nextLine();
			char[] cs = buffer.toCharArray();
			Tokenizer tokenizer = TOKENIZER_FACTORY.tokenizer(cs, 0, cs.length);
			String[] tokens = tokenizer.tokenize();
			tokenList.addAll(Arrays.asList(tokens));
		}
		;

		// let us now tag the tokens with some part of speech from the model
		// loaded above
		tags = decoder.tag(tokenList);
		PrintStream psp = new PrintStream(new File("tagging.txt"));
		for (int i = 0; i < tags.size(); i++) {
			psp.println(tags.token(i) + "\t\t" + tags.tag(i));
		}

	}

	private ArrayList<String> constructPhrases() throws Exception {
		ArrayList<String> phrases = new ArrayList<>();
		ArrayList<String> tagPhrases = new ArrayList<>();
		String buffer = "";

		// constructing tag phrases for sentences
		for (int i = 0; i < tags.size(); i++) {
			if (tags.tag(i).equals("(")) {
				type = tags.token(i+1);
				i+=2;
				continue;
			}
			buffer += tags.tag(i);
			if (tags.tag(i).equals(".")) {
				tagPhrases.add(buffer);
				buffer = "";
			}
		}

		// regular expressions for possible requirements sentences templates
		ArrayList<String> regexp = new ArrayList<>();
		regexp.add("(at)?(jj)?(nn)+(n[ps])?");
		regexp.add("(md)(vb)");
		regexp.add("(at)?(nn)(in)");
		regexp.add("(at)?(jj)?(nn)+[ps]?");
		regexp.add("(in)(cd)?(nn)?");

		regexp.add("(at)?(jj)?(nn)+");
		regexp.add("(md)(be)?(jj)[sr]?((cs)(n)?)?(in)?");
		regexp.add("[(cd)(nn)]{2}");
		// regexp.add("((cc)(be)?(jj)[sr]?((cs)(n)?)?(in)?(cd))?");
		regexp.add("((in)(at)?(nn))?.");

		// parsing of the sentences composed from tags
		int regCounter = 0;
		int globalCounter = 0;
		int temp = 0;
		buffer = tagPhrases.get(0);
		String phrase = "";
		for (int i = 0; i < buffer.length() && regCounter < 5; i++) {
			TokenizerFactory tf = new RegExTokenizerFactory(regexp.get(regCounter));
			char[] cs = buffer.toCharArray();
			Tokenizer tokenizer = tf.tokenizer(cs, 0, cs.length);
			String[] tokenz = tokenizer.tokenize();
			String tok = tokenz[0];
			for (int j = 0; j < tok.length(); j++) {
				for (int k = 0; k < tok.length(); k++) {
					if(tags.tag(globalCounter).equals("("))
					{
						globalCounter+=3;
						k=-1;
						continue;
					}
					if (tok.contains(tags.tag(globalCounter))) {
						phrase += tags.token(globalCounter) + " ";
						temp += tags.tag(globalCounter).length();
						tok = tok.substring(tags.tag(globalCounter).length(), tok.length());
						globalCounter++;
						k = 0;
					} else {
						break;
					}
				}
			}
			phrase = phrase.substring(0, phrase.length() - 1);
			phrases.add(phrase);
			phrase = "";
			buffer = tagPhrases.get(0).substring(temp, tagPhrases.get(0).length());
			regCounter++;
			i = 0;
		}

		buffer = "";
		buffer = tagPhrases.get(1);
		regCounter = 5;
		globalCounter++;
		temp = 0;
		for (int i = 0; i < buffer.length() && regCounter < 9; i++) {
			TokenizerFactory tf = new RegExTokenizerFactory(regexp.get(regCounter));
			char[] cs = buffer.toCharArray();
			Tokenizer tokenizer = tf.tokenizer(cs, 0, cs.length);
			String[] tokenz = tokenizer.tokenize();
			String tok = tokenz[0];
			for (int j = 0; j < tok.length(); j++) {
				for (int k = 0; k < tok.length(); k++) {
					if (tok.contains(tags.tag(globalCounter))) {
						phrase += tags.token(globalCounter) + " ";
						temp += tags.tag(globalCounter).length();
						tok = tok.substring(tags.tag(globalCounter).length(), tok.length());
						globalCounter++;
						k = 0;
					} else {
						break;
					}
				}
			}
			phrase = phrase.substring(0, phrase.length() - 1);
			phrases.add(phrase);
			phrase = "";
			buffer = tagPhrases.get(1).substring(temp, tagPhrases.get(1).length());
			regCounter++;
			i = 0;
		}

		// printing the phrases to the separate file
		PrintStream psp = new PrintStream(new File("phrases.txt"));
		for (String s : phrases) {
			psp.println(s);
		}
		return phrases;
	}

	private void codeBuilder(ArrayList<String> phrases, PrintStream ps) {

		ps.print("Req_" + reqCounter++);
		String noun = "";
		for (int i = 0; i < tags.size(); i++) {
			if (tags.tag(i).equals("nn")) {
				noun = tags.token(i);
				ps.print("(" + noun + ":" + noun.toUpperCase() + ";");
				break;
			}
		}
		String buffer = phrases.get(3).substring(phrases.get(3).lastIndexOf(" ")+1);
		ps.print(" " + buffer + ":" + type.toUpperCase() + ")");
		for (int i = 0; i < phrases.size(); i++) {
			if (phrases.get(i).charAt(0) >= 'A' && phrases.get(i).charAt(0) <= 'Z') {
				ps.println();
				ps.print("\t-- ");
			}
			ps.print(phrases.get(i) + " ");
		}
		ps.println();
		ps.println("   require");
		ps.println("     " + noun + "." + buffer + " = " + buffer);
		String operation = "";
		operation = phrases.get(6).substring(0, phrases.get(6).lastIndexOf(" "));
		operation = operation.substring(operation.lastIndexOf(" ") + 1);
		switch (operation) {
		case "smaller":
		case "lower":
		case "less":
		case "under":
			operation = " < ";
			break;
		case "bigger":
		case "more":
		case "larger":
		case "over":
		case "above":
			operation = " > ";
			break;
		case "equal":
		case "equals":
		case "is":
			operation = " = ";
			break;
		}
		if(phrases.get(8).substring(0, phrases.get(8).indexOf(" ")).equals("before"))
		{
			ps.println("     " + noun + "." + buffer + operation + phrases.get(7));
		}
		ps.println("   do");
		ps.println("     " + noun + "." + phrases.get(8).substring(phrases.get(8).lastIndexOf(" ")+1, phrases.get(8).length()));
		ps.println("   ensure");
		if(phrases.get(8).substring(0, phrases.get(8).indexOf(" ")).equals("after"))
		{
			ps.println("     " + noun + "." + buffer + operation + phrases.get(7));
		}
		operation = phrases.get(1).substring(phrases.get(1).lastIndexOf(" ") + 1);
		switch (operation) {
		case "increase":
		case "add":
		case "enlarge":
			operation = " + ";
			break;
		case "decrease":
		case "reduce":
			operation = " - ";
			break;
		case "multiply":
			operation = " * ";
			break;
		case "divide":
			operation = " / ";
			break;
		case "assign":
			operation = " = ";
			break;
		}
		ps.println("     " + noun + "." + buffer + " = " + buffer + operation
				+ phrases.get(4).substring(phrases.get(4).lastIndexOf(" ")+1));
		ps.println("   end");
	}
}
