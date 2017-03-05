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
	}

	private ArrayList<String> constructPhrases() throws Exception {
		ArrayList<String> phrases = new ArrayList<>();
		ArrayList<String> tagPhrases = new ArrayList<>();
		String buffer = "";

		// constructing tag phrases for sentences
		for (int i = 0; i < tags.size(); i++) {
			buffer += tags.tag(i);
			if (tags.tag(i).equals(".")) {
				tagPhrases.add(buffer);
				buffer = "";
			}
		}

		ArrayList<String> regexp = new ArrayList<>();
		regexp.add("(at)?(nn)+");
		regexp.add("(md)(vb)");
		regexp.add("(at)?(nn)(in)");
		regexp.add("(at)?(jj)(nn)+");
		regexp.add("(in)(cd)");

		regexp.add("(at)?(jj)?(nn)+");
		regexp.add("(md)(be)?(jj)[sr]?((cs)(n)?)?");
		regexp.add("(cd)");
		// regexp.add("((cc)(be)?(jj)[sr]?((cs)(n)?)?(cd))?");
		regexp.add("((in)(nn))?.");

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
		PrintStream psp = new PrintStream(new File("phrases.txt"));
		for (String s : phrases) {
			psp.println(s);
		}
		return phrases;
	}

	private void codeBuilder(ArrayList<String> phrases, PrintStream ps) {
		Random r = new Random();
		r.setSeed(System.currentTimeMillis());
		ps.print("Req_" + r.nextInt());
		String noun = "";
		for (int i = 0; i < tags.size(); i++) {
			if (tags.tag(i).equals("nn")) {
				noun = tags.token(i);
				ps.print("(" + noun + ":" + noun.toUpperCase() + ";");
				break;
			}
		}
		String buffer = phrases.get(3).substring(phrases.get(3).lastIndexOf(" ") + 1, phrases.get(3).length());
		ps.print(" " + buffer + ":" + buffer.toUpperCase() + ")");
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
		ps.println("     " + noun + "." + buffer + " < " + phrases.get(7));
		ps.println("   do");
		ps.println("     " + noun + "." + "method");
		ps.println("   ensure");
		ps.println("     " + noun + "." + buffer + " = " + buffer + " + "
				+ phrases.get(4).charAt(phrases.get(4).length() - 1));
		ps.println("   end");
	}
}