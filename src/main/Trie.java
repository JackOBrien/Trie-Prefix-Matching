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
	
	/** Difference of the number of bits between levels. 
	 * TODO: Unused for release 1 */
	private int strideLength;
	
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
	 * associated with it. Returns "No Match" if there is no 
	 * matching prefix.
	 * 
	 * @param ipAddr the IPv4 address to lookup
	 * @return the next hop associated with the given IP if any.
	 ***************************************************************/
	public String lookUp(String ipAddr) {
		
		int ipAddressLength = 32;
		
		int data = convertIPtoInt(ipAddr, ipAddressLength);
		
		String result = lookUp(new Prefix(data, ipAddressLength, null), root);
		
		if (result == null) result = "No Match";
		
		return result;
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
			return current.getBestHop(toLookup.bits);
		} else {	
			
			Node next = current.getNextStep(toLookup);
			
			/* If the current node has a next hop IP */
			if (current.getBestHop(toLookup.bits) != null) {
				
				if (next == null) {
					return current.getBestHop(toLookup.bits);
				}
				
				String result = lookUp(toLookup, next);
				
				if (result == null) {
					return current.getBestHop(toLookup.bits);
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
		
		int len = toInsert.length;
		
		while (len % strideLength != 0) {
			len ++;
		}
		
		int desiredData = toInsert.bits << (len - toInsert.length);
		
		if (current.data == desiredData && 
				current.level * strideLength == len) {
			current.addPrefix(toInsert);
		} else {
			
			Node next = current.getNextStep(toInsert);
			
			/* If the current node does not have the child needed, create it */
			if (next == null) {
				
				int data = shiftPrefix(toInsert, current.level);
				
				next = new Node(data, current.level + 1);
				
				current.addChild(next);
			}
			
			insertPrefix(toInsert, next);
		}
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

	private int shiftPrefix(Prefix prefix, int level) {
		int undesiredLength = prefix.length - ((level + 1) * strideLength);
		int beginningPrefix;
		
		if (undesiredLength < 0) {
			beginningPrefix = prefix.bits << (-1) * undesiredLength;
		} else {
			beginningPrefix = prefix.bits >>> undesiredLength;
		}
		
		return beginningPrefix; 
	}
	
	
	public static void main(String[] args) {
		Trie t = new Trie(2);
		
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
		
		Vector<Prefix> prefixes;
		Vector<Node> children;
		
		int data;
		int level;
		
		public Node(int data,int level) {
			this.data = data;
			this.level = level;
			
			prefixes = new Vector<Prefix>();
			children = new Vector<Node>();
		}
		
		public void addPrefix(Prefix prefix) {
			prefixes.add(prefix);
		}
		
		public void addChild(Node child) {
			children.add(child);
		}
		
		public String getBestHop(int ipBits) {			
			Prefix longestPrefix = new Prefix(-1, 0, null);
			
			for (Prefix p : prefixes) {
				if (p.length > longestPrefix.length) {
					if (p.matches(ipBits)) {
						longestPrefix = p;
					}
				}
			}
			
			return longestPrefix.nextHop;
		}
		
		public Node getNextStep(Prefix prefix) {
			
			int beginningPrefix = shiftPrefix(prefix, level);
			
			for (Node child : children) {
				
				if (child.data == beginningPrefix) {
					return child;
				}
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

		public boolean matches(int ipBits) {
			int undesiredLength = 32 - length;
			
			return bits == ipBits >>> undesiredLength;
		}
	}
}
