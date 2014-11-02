package main;

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
	 * @param sl defines stride length
	 ***************************************************************/
	public Trie(int sl){
		
		root = new Node(-1, 0);
		
		strideLen = sl;
	}
	
	/****************************************************************
	 * Adds a node to this Trie
	 * 
	 * @param nd represents the node to add 
	 ***************************************************************/
	public void add(int prefix, int prefixLength, int pathLength, 
			String nextHop){
		
		Node node = new Node(prefix, prefixLength);
		
		insertPrefix(node);
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
	
	private class Node {
		
		Vector<Node> children;
		
		int data;
		
		int pathLength;
		
		String nextHop;
		
		int level;
		
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
