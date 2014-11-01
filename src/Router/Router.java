package Router;

/********************************************************************
 * Router.java (working title)
 *
 * @author Jack O'Brien
 * @author Megan Maher
 * @author Tyler McCarthy
 * @version Oct 31, 2014
 *******************************************************************/
public class Router {
	
	/** Main trie */
	private Trie trie;
	
	/** Currently hardcoded to 1 for part 1 */
	private int stride_len = 1;
	
	/****************************************************************
	 * Attempts to match a node to a node 
	 * @param m1 match candidate 1
	 * @param m2 match candidate 2
	 ***************************************************************/
	public boolean match (int m1, int m2) {
		return true;
	}
	
	/****************************************************************
	 * Default constructor
	 * @param st defines stride length: values 1-3 inclusive
	 ***************************************************************/
	public Router () {
		
		/** Later to take 2 or 3 as params*/
		this.trie = new Trie(st);
		trie.populate(stride_len);
	}
	
	/****************************************************************
	 * 
	 * @return 
	 ***************************************************************/
	public static void main (String args[]) {
		//populate trie from file
		
		//check sampleips.txt for matches
		
		//check against expected outputs
	}
}
