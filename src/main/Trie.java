package main;

import java.util.Scanner;
import java.util.Vector;

/********************************************************************
 * Trie.java
 *
 * @author Jack O'Brien
 * @author Megan Maher
 * @author Tyler McCarthy
 * @version Nov 2, 2014
 *******************************************************************/
public class Trie {

	private Node root;
	
	/** Difference of the number of bits between levels. */
	private int strideLen;
	
	/****************************************************************
	 * Default constructor 
	 * 
	 * @param strideLength defines stride length
	 ***************************************************************/
	public Trie(int strideLength){
		
		root = new Node(-1, 0);
		
		this.strideLen = strideLength;
	}
	
	public void add(String prefix, int prefixLength, int pathLength, 
			String nextHop){
		
		int data = convertIPtoInt(prefix, prefixLength);
		
		Node node = new Node(data, pathLength, nextHop, prefixLength);
		
		insertPrefix(node);
	}
	
	public String lookUp(String ipAddr) {
		
		int ipAddressLength = 32;
		
		int data = convertIPtoInt(ipAddr, ipAddressLength);
		
		Node searchingFor = new Node(data, ipAddressLength);
		
		return lookUp(searchingFor, root);
	}
	
	private int convertIPtoInt(String ipAddr, int prefixLength) {
		String[] ipArr = ipAddr.split("\\.");
		
		int data = (Integer.parseInt(ipArr[0]) & 0xFF) << 24;
		data |= (Integer.parseInt(ipArr[1]) & 0xFF) << 16;
		data |= (Integer.parseInt(ipArr[2]) & 0xFF) << 8;
		data |= (Integer.parseInt(ipArr[3]) & 0xFF);
		
		return data >>> (32 - prefixLength);
	}
	
	private String lookUp(Node searchingFor, Node current) {
		
		/* End case */
		if (current.children.isEmpty()) {
			return current.nextHop;
		} else {	
			
			Node next = current.childContainsPrefix(searchingFor);
			
			/* If the current node has a next hop IP */
			if (current.nextHop != null) {
				
				if (next == null) {
					return current.nextHop;
				}
				
				String result = lookUp(searchingFor, next);
				
				if (result == null) {
					return current.nextHop;
				} else {
					return result;
				}
			}
			
			/* If the current node contains no children with the next step */
			if (next == null) {
				return null;
			}		
			
			return lookUp(searchingFor, next);
		}
	}
	
	private void insertPrefix(Node node) {
		insertPrefix(node, root);
	}
	
	private void insertPrefix(Node toInsert, Node current) {
		
		if (current.data == toInsert.data) {
			current.nextHop = toInsert.nextHop;
			current.pathLength = toInsert.pathLength;
		} else {
			
			Node next = current.childContainsPrefix(toInsert);
			
			/* If the current node does not have the child needed, create it */
			if (next == null) {
				int undesiredLength = (toInsert.level - (current.level + 1));
				
				int data = toInsert.data >>> undesiredLength;
				
				next = new Node(data, current.level + 1);
				
				current.addChild(next);
			}
			
			insertPrefix(toInsert, next);
		}
	}
	
	public static void main(String[] args) {
		Trie t = new Trie(1);
		
		Scanner scan = new Scanner(System.in);
		
		System.out.println("Please use commands: " + 
				"add: <prefix> <pathLength> <nextHop>\n" + 
				"                     lookup: <IP address>");
		
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
					int pathLength = Integer.parseInt(arr[1]);
					String nextHop = arr[2];

					t.add(prefix, prefixLength, pathLength, nextHop);
					
					System.out.println("---\tAdded to Trie");
				} else if(input.startsWith("lookup ")) {
					input = input.substring(7);
					String result = t.lookUp(input);
					
					if (result == null) {
						result = "No Match";
					}
							
					System.out.println("---\t" + result);
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
		
		int data;
		
		int pathLength;
		
		String nextHop;
		
		int level;
		
		public Node(int data, int pathLength, String nextHop, int level) {
			this.data = data;
			this.pathLength = pathLength;
			this.nextHop = nextHop;
			this.level = level;
					
			children = new Vector<Node>();
		}
		
		public Node(int data,int level) {
			this.data = data;
			this.level = level;
			
			pathLength = -1;
			nextHop = null;
			
			children = new Vector<Node>();
		}
		
		public void addChild(Node child) {
			children.add(child);
		}
		
		public Node childContainsPrefix(Node prefix) {
			
			int undesiredLength = prefix.level - (level + 1);
			
			int beginningPrefix = prefix.data >>> undesiredLength;
			
			
			for (Node child : children) {
				
				if (child.data == beginningPrefix) {
					return child;
				}
			}
			
			return null;
		}
	}
}
