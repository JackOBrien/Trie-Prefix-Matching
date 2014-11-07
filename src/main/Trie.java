package main;

import java.util.Scanner;
import java.util.Vector;

/********************************************************************
 * Trie.java
 *
 * @author Jack O'Brien
 * @author Megan Maher
 * @author Tyler McCarthy
 * @version Nov 4, 2014
 *******************************************************************/
public class Trie {

	/** The root of the Trie */
	private Node root;
	
	/** Difference of the number of bits between levels. */
	private int strideLength;
	
	private int numNodes;
	private int numPrefixes;
	
	/****************************************************************
	 * Default constructor 
	 * 
	 * @param strideLength defines stride length
	 ***************************************************************/
	public Trie(int strideLength){
		
		root = new Node(-1, 0);
		
		this.strideLength = strideLength;
	}
	
	/****************************************************************
	 * Adds a prefix into the Trie.
	 * 
	 * @param prefix the IP address of the prefix.
	 * @param prefixLength the number of bits in the prefix.
	 * @param pathLength the length of the AS path.
	 * @param nextHop the IP address of the next hop.
	 ***************************************************************/
	public void add(String prefix, int prefixLength, String nextHop){
		
		int prefBits = convertIPtoInt(prefix, prefixLength);
		
		Prefix p = new Prefix(prefBits, prefixLength, nextHop);
		
		insertPrefix(p);
	}
	
	/****************************************************************
	 * Looks up the given IP address and returns the next hop 
	 * associated with it. Returns "NoMatch" if there is no 
	 * matching prefix.
	 * 
	 * @param ipAddr the IPv4 address to lookup
	 * @return the next hop associated with the given IP if any.
	 ***************************************************************/
	public String lookUp(String ipAddr) {
		
		int ipAddressLength = 32;
		
		int data = convertIPtoInt(ipAddr, ipAddressLength);
		
		String result = lookUp(new Prefix(data, ipAddressLength, null), root);
		
		if (result == null) result = "NoMatch";
		
		return result;
	}
	
	public int getNumNodes() {
		return numNodes;
	}
	
	public int getNumPrefixes() {
		return numPrefixes;
	}
	
	/****************************************************************
	 * Looks up the given IP address and returns the next hop 
	 * associated with it. Returns "No Match" if there is no 
	 * matching prefix.
	 * 
	 * @param ipBits representation of the IPv4 address to lookup
	 * @param current the node currently being compared.
	 * @return the next hop associated with the given IP if any.
	 ***************************************************************/
	private String lookUp(Prefix toLookup, Node current) {
		
		/* End case */
		if (current.children.isEmpty()) {
			if (current.prefix == null) return null;
			return current.prefix.nextHop;
		} else {	
			int nodeLength = strideLength * (current.level + 1);
			int bits = toLookup.bits;
			int pLength = toLookup.length;
			
			if (pLength < nodeLength) {
				pLength = nodeLength;
				bits <<= nodeLength - toLookup.length;
			}
			
			int undesiredLength = pLength - nodeLength;
			bits >>>= undesiredLength;
				
			Node next = current.getNextStep(bits);

			/* If the current node has a next hop IP */
			if (current.prefix != null) {
				
				if (next == null) {
					return current.prefix.nextHop;
				}
				
				String result = lookUp(toLookup, next);
				
				if (result == null) {
					return current.prefix.nextHop;
				} else {
					return result;
				}
			}
			
			/* If the current node contains no children with the next step */
			if (next == null) {
				return null;
			}		
			
			return lookUp(toLookup, next);
		}
	}
	
	/****************************************************************
	 * Inserts the given prefix into the Trie.
	 * 
	 * @param prefix the Prefix object representing what's to be added.
	 ***************************************************************/
	private void insertPrefix(Prefix prefix) {
		insertPrefix(prefix, root);
	}
	
	/****************************************************************
	 * Inserts the given prefix into the Trie.
	 * 
	 * @param prefix the Prefix object representing what's to be added.
	 * @param current the node currently being compared.
	 ***************************************************************/
	private void insertPrefix(Prefix toInsert, Node current) {
		
		/* End Case. We've reached the desired level in the Trie. */
		if (strideLength * current.level >= toInsert.length) {
			
			/* Doesn't change prefix if a better one is already there. */
			if (current.prefix != null) {
				if (current.prefix.length > toInsert.length) {
					return;
				}
			}
			if (current.prefix == null) numPrefixes ++;
			current.setPrefix(toInsert);
		} else {
			int[] destArr = destinationData(toInsert, current.level + 1);
			
			/* Loops through the array of destination data */
			for (int i = 0; i < destArr.length; i++) {
				Node next = current.getNextStep(destArr[i]);
				
				if (next == null) {
					next = new Node(destArr[i], current.level + 1);
					current.addChild(next);
					numNodes++;
				}

				insertPrefix(toInsert, next);
			}
		}
	}
	
	private int[] destinationData(Prefix p, int level) {
		
		// Number of significant bits in the node's data field
		int nodeLen = strideLength * level;
		int numDests;
		
		if (p.length >= nodeLen) numDests = 1;
		else {
			double power = (double) ((nodeLen) - p.length);
			numDests = (int) Math.pow(2.0, power);
		}
		
		int[] destinations = new int[numDests];
		
		// Difference from prefix length to next multiple of stride length
		int len = 0; 
		while ((len + p.length) % strideLength != 0) len ++;
		int data = p.bits << len;
		
		int unwantedLen = ((p.length + len) - nodeLen);
		data >>>= unwantedLen;
		
		for (int i = 0; numDests > 0; i++, numDests--) {
			destinations[i] = data | i;
		}
		
		return destinations;
	}
	
	/****************************************************************
	 * Converts a prefix given as a string to an integer representing
	 * the bits of the string.
	 * 
	 * @param ipAddr the IP of the prefix to convert.
	 * @param prefixLength the number of relevant bits in the prefix.
	 * @return the prefix as an integer representing the bits.
	 ***************************************************************/
	private int convertIPtoInt(String ipAddr, int prefixLength) {
		String[] ipArr = ipAddr.split("\\.");
		
		int data = (Integer.parseInt(ipArr[0]) & 0xFF) << 24;
		data |= (Integer.parseInt(ipArr[1]) & 0xFF) << 16;
		data |= (Integer.parseInt(ipArr[2]) & 0xFF) << 8;
		data |= (Integer.parseInt(ipArr[3]) & 0xFF);
		
		return data >>> (32 - prefixLength);
	}	
	
	public static void main(String[] args) {
		Trie t = new Trie(3);
		
		Scanner scan = new Scanner(System.in);
		
		System.out.println("Please use commands: " + 
				"add <prefix> <pathLength> <nextHop>\n" + 
				"                     lookup <IP address>");
		
		while(true) {
			System.out.print("> ");
			String input = scan.nextLine();
			
			try {
		
				if (input.startsWith("add ")) {
					input = input.substring(4);

					String[] arr = input.split(" ");

					String prefix = arr[0];
					
					String[] prefixArr = prefix.split("/");
					prefix = prefixArr[0];
					
					int prefixLength = Integer.parseInt(prefixArr[1]);
					String nextHop = arr[1];

					t.add(prefix, prefixLength, nextHop);
					
 					System.out.println("---\tAdded to Trie");
				
				} else if(input.startsWith("lookup ")) {
					input = input.substring(7);
					String result = t.lookUp(input);
							
					System.out.println("---\t" + result);
				
				} else if(input.startsWith("exit")) {
					scan.close();
					System.exit(0);
				
				} else {
					System.err.println("~ Invalid Command: " + input + " ~");
				}
			} catch (Exception e) {
				System.out.println();
				e.printStackTrace();
			}
		}
	}
	
	private class Node {
		
		Vector<Node> children;
		
		Prefix prefix;
		
		int data;
		int level;
		
		public Node(int data,int level) {
			this.data = data;
			this.level = level;
			
			prefix = null;
			children = new Vector<Node>();
		}
		
		public void setPrefix(Prefix prefix) {
			this.prefix = prefix;
		}
		
		public void addChild(Node child) {
			children.add(child);
		}

		public Node getNextStep(int data) {
			for (Node child : children) {
				if (child.data == data)
					return child;
			}
			
			return null;
		}
	}
	
	private class Prefix {
		int bits;
		int length;
		String nextHop;
		
		public Prefix(int bits, int length, String nextHop) {
			this.bits = bits;
			this.length = length;
			this.nextHop = nextHop;
		}
	}
}
