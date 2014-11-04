package main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

/********************************************************************
 * Router.java
 *
 * @author Jack O'Brien
 * @author Megan Maher
 * @author Tyler McCarthy
 * @version Nov 3, 2014
 *******************************************************************/
public class Router {
	
	// TODO: Make Dynamic for second release
	private final int STRIDE_LENGTH = 1; 
	
	private BufferedReader routesReader;
	
	private BufferedReader ipListReader;
	
	private Trie trie;
	
	public Router() {
		trie = new Trie(STRIDE_LENGTH);
	}

	public void setRoutesFile(String path) throws FileNotFoundException {
		routesReader = new BufferedReader(new FileReader(path));
	}
	
	public void setIpListFile(String path) throws FileNotFoundException {
		ipListReader = new BufferedReader(new FileReader(path));
	}
	
	public void populateTrie() throws IOException {
		String line;
		
		/* Reads the routes file line-by-line */
		while ((line = routesReader.readLine()) != null) {
			String[] pipes = line.split("\\|");
			
			if (pipes.length != 3) {
				System.out.println(pipes.length);
				throw new IOException("Invalid Routes File");
			}
			
			String currentPrefix = pipes[0];
			
			String shortestPrefix = pipes[0];
			String shortestHop = pipes[2];
			
			// Number of the current line's path length
			int shortestPath = pipes[1].split(" ").length;
			
			/* Loops through alike prefixes */
			while ((line = routesReader.readLine()) != null) {
				pipes = line.split("\\|");
				
				if (!currentPrefix.equals(pipes[0])) break;
				
				int currentPath = pipes[1].split(" ").length;
				
				/* Checks for a new shortest path */
				if (currentPath < shortestPath) {
					shortestPrefix = pipes[0];
					shortestHop = pipes[2];
					
					shortestPath = currentPath;
					
					/* Aborts early when path of length 1 or less found. */
					if (shortestPath <= 1) {
						break;
					}
				}
			}
			
			String[] prefixArr = shortestPrefix.split("/");
			
			String prefix = prefixArr[0];
			int prefixLength = Integer.parseInt(prefixArr[1]);
			
			trie.add(prefix, prefixLength, shortestPath, shortestHop);
		}
		
		routesReader.close();
		
	}
	
	public void lookupIPs() throws IOException {
		String line;
		
		/* Reads the routes file line-by-line */
		while ((line = ipListReader.readLine()) != null) {
			String result = trie.lookUp(line);
			
			String output = String.format("%-15s %-15s", line, result);
			
			System.out.println(output);
		}
		
		ipListReader.close();
	}
	
	public static void main(String[] args) {
		//TODO: Take stride length, pass to constructor
		
		Router router = new Router();
		
		Scanner scan = new Scanner(System.in);
		
		System.out.println("Input router file path:");
		System.out.print("\t> ");
		
		String fileName = "";
		
		try {
			fileName = scan.nextLine();
			router.setRoutesFile(fileName);
		} catch (FileNotFoundException e) {
			System.err.println("File \"" + fileName + "\" not found");
			System.exit(0);
		}
		
		System.out.println("\nInput IP list file path:");
		System.out.print("\t> ");
		
		try {
			fileName = scan.nextLine();
			router.setIpListFile(fileName);
		} catch (FileNotFoundException e) {
			System.err.println("File \"" + fileName + "\" not found");
			System.exit(0);
		}
		
		System.out.println("\nPress enter to populate Trie");
		scan.nextLine();
		System.out.print("...Populating... ");
		
		try {
			long start = System.currentTimeMillis();
			
			router.populateTrie();
			
			long elapsed = System.currentTimeMillis() - start;
			
			System.out.println("Done");
			System.out.println("Elapsed time: " + elapsed + "ms");
			
			System.gc();
			
			int memUsage = (int) (Runtime.getRuntime().totalMemory() - 
					Runtime.getRuntime().freeMemory());
			memUsage /= 1048576;
			
			System.out.println("Memory usage: " + memUsage + "Mb");
			
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		System.out.println("\nPress enter to lookup IPs");
		scan.nextLine();
		
		try {
			router.lookupIPs();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		scan.close();
	}
}
