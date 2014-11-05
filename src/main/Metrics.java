package main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class Metrics {
	
	static PrintStream dummyStream = new PrintStream(new OutputStream(){
	    public void write(int b) {}
	});
	
	static PrintStream originalStream;
	
	public static void mute() {
		System.setOut(dummyStream);
	}
	
	public static void unmute() {
		System.setOut(originalStream);
	}
	
	public static void main(String[] args) {
		
		originalStream = System.out;
		
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
		
		mute();
		
		long[] totalTime = {0l, 0l, 0l};
		
		for (int sL = 1; sL <= 3; sL ++) {
			unmute();
			System.out.println("Stride length " + sL);
			mute();
			
			for (int i = 0; i < numberOfLoops; i++) {
				Router router = new Router(sL);

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

				totalTime[sL-1] += (System.currentTimeMillis() - start);
				unmute();
				System.out.println("-- Run " + (i + 1) + " complete");
				mute();
			}
			unmute();
			System.out.println();
			mute();
		}
		
//		int averageTime = (int) (totalTime / numberOfLoops);
//		long totalMemory = (Runtime.getRuntime().totalMemory() - 
//				Runtime.getRuntime().freeMemory());
//		int averageMemory = (int) (totalMemory / numberOfLoops);
//		averageMemory /= 1048576;
		
		unmute();
		
		System.out.println("\n");
		String header = String.format("%14s| %7s %7s %7s", "Stride Length",
				"1", "2", "3");
		System.out.println(header);
		System.out.println(new String(new char[39]).replace("\0", "-"));
		
		String build = String.format("%14s| %7s %7s %7s", "Build Time",
				"---", "---", "---");
		String lookup = String.format("%14s| %7s %7s %7s", "Look Up Time",
				"---", "---", "---");
		String memory = String.format("%14s| %7s %7s %7s", "Memory Usage",
				"---", "---", "---");
		
		System.out.println(build);
		System.out.println(lookup);
		System.out.println(memory);
	}	
}
