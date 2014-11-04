package main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class Metrics {
	
	public static void main(String[] args) {
		
		PrintStream originalStream = System.out;
		PrintStream dummyStream    = new PrintStream(new OutputStream(){
		    public void write(int b) {
		        //NO-OP
		    }
		});
		
		Scanner scan = new Scanner(System.in);
		System.out.print("Number of times to populate the Trie: ");
		int numberOfLoops = Integer.parseInt(scan.nextLine());
		
		System.out.print("\nInput router file path:");
		System.out.println();
		
		String routerFile = scan.nextLine();
		
		System.out.print("\nInput IP list file path:");
		System.out.println();
		
		String ipFile = scan.nextLine();
		scan.close();
		
		System.out.println("...Running...");
		
		System.setOut(dummyStream);
		
		long totalTime = 0;
		
		for (int i = 0; i < numberOfLoops; i++) {
			Router router = new Router();
			
			try {
				router.setRoutesFile(routerFile);
			} catch (FileNotFoundException e) {
				System.err.println("File \"" + routerFile + "\" not found");
				System.exit(0);
			}
			
			try {
				router.setIpListFile(ipFile);
			} catch (FileNotFoundException e) {
				System.err.println("File \"" + ipFile + "\" not found");
				System.exit(0);
			}
			
			long start = System.currentTimeMillis();
			
			try {
				router.populateTrie();
				router.lookupIPs();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			totalTime += (System.currentTimeMillis() - start);
		}
		
		int averageTime = (int) (totalTime / numberOfLoops);
		
		System.setOut(originalStream);
		System.out.println("Average time: " + averageTime + "ms");
	}	
}
