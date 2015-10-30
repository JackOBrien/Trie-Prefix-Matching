package main;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Scanner;

/********************************************************************
 * Calculates various metrics for each stride length 1-3
 *
 * @author Jack O'Brien
 * @author Megan Maher
 * @author Tyler McCarthy
 * @version Nov 5, 2014
 *******************************************************************/
public class Metrics {
	
	static PrintStream dummyStream = new PrintStream(new OutputStream(){
	    public void write(int b) {}
	});
	
	static PrintStream originalStream;
	
	private static void mute() {
		System.setOut(dummyStream);
	}
	
	private static void unmute() {
		System.setOut(originalStream);
	}
	
	public static void print(String msg) {
		unmute();
		System.out.println(msg);
		mute();
	}
	
	private int strideLength;
	private final double LOOKUP_RUNS = 3000000.0;
	
	private String routes;
	private String ips;
	
	private Router lastUsed;
	
	private double avgBuild;
	private double avgLookup;
	private int avgMemory;
	
	private int numRuns;
	
	private long[] buildTimes;
	
	public static int numLinesLookup;
	
	public Metrics(int strideLength, int runs, String routes, String ips) {
		this.strideLength = strideLength;
		numRuns = runs;
		this.routes = routes;
		this.ips = ips;
		
		buildTimes = new long[runs];
		
		System.gc();
		
		long memoryBefore =  (Runtime.getRuntime().totalMemory() - 
				Runtime.getRuntime().freeMemory()); 
		
		avgBuild = avgBuildTime(runs);
		
		long memoryAfter = (Runtime.getRuntime().totalMemory() - 
				Runtime.getRuntime().freeMemory());
		memoryAfter -= memoryBefore;
		avgMemory = (int) (memoryAfter / runs);
		avgMemory /= 1048576;
		
		avgLookup = avgLookupTime();
	}

	/****************************************************************
	 * Unused in this build. Calls are commented.
	 ***************************************************************/
	public double getStdDevBuildTime() {
		long sum = 0;
		for (int i = 0; i < buildTimes.length; i++) {
			sum += ((buildTimes[i] - avgBuild) * (buildTimes[i] - avgBuild));
		}
		
		double stdDev = Math.sqrt((1.0/((double)numRuns))*((double)sum));
		return stdDev;
	}
	
	/****************************************************************
	 * @return average build time in seconds
	 ***************************************************************/
	public double getAvgBuildTime() {
		return avgBuild / 1000.0;
	}
	
	/****************************************************************
	 * @return the average look up time in nano seconds per IP.
	 ***************************************************************/
	public double getAvgLookupTime() {
		double avgLk = avgLookup * 1000000.0; // time in nano seconds
		avgLk /= (double) numLinesLookup; // time in nano per IP
		return avgLk;
	}
	
	public int getAvgMemory() {
		return avgMemory;
	}
	
	public int getNumNodes() {
		return lastUsed.getNumNodes();
	}
	
	public int getNumPrefixes() {
		return lastUsed.getNumPrefixes();
	}
	
	private double avgBuildTime(int numRuns) {
		long totalTime = 0;
		Router router = null;
		
		for (int i = 0; i < numRuns; i++) {
			router = new Router(strideLength);

			try {
				router.setRoutesFile(routes);
			} catch (FileNotFoundException e) {
				unmute();
				System.err.println("File \"" + routes + "\" not found");
				System.exit(0);
			}

			try {
				router.setIpListFile(ips);
			} catch (FileNotFoundException e) {
				unmute();
				System.err.println("File \"" + ips + "\" not found");
				System.exit(0);
			}

			long start = System.currentTimeMillis();

			try {
				router.populateTrie();
			} catch (IOException e) {
				unmute();
				e.printStackTrace();
			}
			long elapsedTime = (System.currentTimeMillis() - start);
			totalTime += elapsedTime;
			
			buildTimes[i] = elapsedTime;
			
			print("-- Build " + (i + 1) + " complete");
		}
		lastUsed = router;
		
		return  (totalTime / (double) numRuns);
	}
	
	/****************************************************************
	 * @return the average time in milliseconds of look up per file
	 ***************************************************************/
	private double avgLookupTime() {
		
		long totalTime = 0;
		print("-- Calculating Lookup Time (" + (int) LOOKUP_RUNS + " runs)");
		
		for (int i = 0; i < LOOKUP_RUNS; i++) {
			long start = System.currentTimeMillis();
			try {
				lastUsed.lookupIPs();
			} catch (IOException e) {
				unmute();
				e.printStackTrace();
			}
			
			totalTime += (System.currentTimeMillis() - start);
		}
		print("-- Lookup runs complete");

		return (totalTime / LOOKUP_RUNS);
	}
	
	/****************************************************************
	 * Method writen by StackOverflow.com user Martinus.
	 * URL: http://stackoverflow.com/a/453067/2233026
	 * Date: March 28, 2014
	 * 
	 * Will count the number of lines in a file very quickly.
	 * 
	 * @param filename name of file to count the lines of.
	 * @return the number of lines in the file
	 * @throws IOException if something goes wrong reading the file
	 ***************************************************************/
	public static int countLines(String filename) throws IOException {
	    InputStream is = new BufferedInputStream(new FileInputStream(filename));
	    try {
	        byte[] c = new byte[1024];
	        int count = 0;
	        int readChars = 0;
	        boolean empty = true;
	        while ((readChars = is.read(c)) != -1) {
	            empty = false;
	            for (int i = 0; i < readChars; ++i) {
	                if (c[i] == '\n') {
	                    ++count;
	                }
	            }
	        }
	        return (count == 0 && !empty) ? 1 : count;
	    } finally {
	        is.close();
	    }
	}
	
	public static void main(String[] args) {
		
		originalStream = System.out;
		
		Scanner scan = new Scanner(System.in);
		System.out.print("Number of times to populate the Trie: ");
		int numRuns = Integer.parseInt(scan.nextLine());
		
		System.out.print("\nInput router file path:");
		System.out.println();
		
		String routerFile = scan.nextLine();
		
		System.out.print("\nInput IP list file path:");
		System.out.println();
		
		String ipFile = scan.nextLine();
		scan.close();
		
		/* Get size of Lookup file in number of lines */
		try {
			numLinesLookup = countLines(ipFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		mute();
		
		print("\nStarting Stride Length 1 Calculations");
		Metrics m1 = new Metrics(1, numRuns, routerFile, ipFile);
		double build1 = m1.getAvgBuildTime();
		double lookup1 = m1.getAvgLookupTime();
		int nodes1 = m1.getNumNodes();
		int prefixes1 = m1.getNumPrefixes();
		int mem1 = m1.avgMemory;
//		print("Std dev: " + Double.toString(m1.getStdDevBuildTime()));
		
		print("\nStarting Stride Length 2 Calculations");
		Metrics m2 = new Metrics(2, numRuns, routerFile, ipFile);
		double build2 = m2.getAvgBuildTime();
		double lookup2 = m2.getAvgLookupTime();
		int nodes2 = m2.getNumNodes();
		int prefixes2 = m2.getNumPrefixes();
		int mem2 = m2.avgMemory;
//		print("Std dev: " + Double.toString(m2.getStdDevBuildTime()));
		
		print("\nStarting Stride Length 3 Calculations");
		Metrics m3 = new Metrics(3, numRuns, routerFile, ipFile);
		double build3 = m3.getAvgBuildTime();
		double lookup3 = m3.getAvgLookupTime();
		int nodes3 = m3.getNumNodes();
		int prefixes3 = m3.getNumPrefixes();
		int mem3 = m3.avgMemory;
//		print("Std dev: " + Double.toString(m3.getStdDevBuildTime()));
		
		unmute();
		
		System.out.println("\n");
		String header = String.format("%16s | %7s %7s %7s %-7s", 
				"Stride Length", "1", "2", "3", "Units");
		System.out.println(header);
		System.out.println(new String(new char[48]).replace("\0", "-"));
		
		String build = String.format("%16s | %7.2f %7.2f %7.2f %-7s", 
				"Build Time", build1, build2, build3, "sec");
		String lookup = String.format("%16s | %7.4f %7.4f %7.4f %-7s", 
				"Search Time", lookup1, lookup2, lookup3, "ns");
		String nodes = String.format("%16s | %7s %7s %7s %-7s", 
				"Num Nodes", nodes1, nodes2, nodes3, "nodes");
		String prefixes = String.format("%16s | %7s %7s %7s %-7s", 
				"Num Prefixes", prefixes1, prefixes2, prefixes3, "prefixes");
		String memory = String.format("%16s | %7s %7s %7s %-7s", 
				"Memory Usage",	mem1, mem2, mem3, "Mb");
		
		System.out.println(build);
		System.out.println(lookup);
		System.out.println(nodes);
		System.out.println(prefixes);
		System.out.println(memory);
	}	
}
