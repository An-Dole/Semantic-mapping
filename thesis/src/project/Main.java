package project;

import java.io.File;
import java.io.PrintStream;
import java.util.Scanner;



public class Main {

	public static void main(String[] args) throws Exception {
		// declaration of the files and streams
		File input = new File("input.txt");
        File output = new File("output.txt");
        Scanner scanner = new Scanner(input);
        PrintStream ps = new PrintStream(output);
        
        ChunkConstructor cc = new ChunkConstructor();
        cc.process(scanner, ps);
	}
}




