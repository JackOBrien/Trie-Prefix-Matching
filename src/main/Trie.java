package main;

/********************************************************************
 * Trie.java
 *
 * @author Jack O'Brien
 * @author Megan Maher
 * @author Tyler McCarthy
 * @version Oct 31, 2014
 *******************************************************************/
public class Trie {

	/** Maximum number of children per parent node */
	private int stride_len;
	
	/****************************************************************
	 * Adds a node to this Trie
	 * Do we need to make a node class? -probably not.
	 * 
	 * We can determine parent node of a given child node of length
	 * n from the most significant n-1 bits of the child.
	 * 
	 * 
	 * @param nd represents the node to add 
	 ***************************************************************/
	public boolean add(int nd){
		return true;
	}
	
	/****************************************************************
	 * Removes a node from this Trie
	 * Will we need this ? 
	 ***************************************************************/
	public boolean remove(){
		return true;
	}
	
	/****************************************************************
	 * No clue how this one is going to work, or if it is needed.
	 * @return ??
	 ***************************************************************/
	public int find(){
		return 0;
	}
	
	/****************************************************************
	 * populate main trie by reading input file
	 * calls add() repeatedly for each entry in the grand list
	 * 
	 * -I am actually still not sure of the actual structure of the
	 * trie. Research and clarification are my next step.
	 ***************************************************************/
	public void populate () {
		
	}
	
	/****************************************************************
	 * Default constructor 
	 * @param st defines stride length
	 ***************************************************************/
	public Trie(int st){
		
		this.stride_len = st;
		populate();
		
	}
}
