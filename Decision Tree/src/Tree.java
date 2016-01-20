/**
 * A simple class that holds the tree for the DTree algorithm
 */
import java.util.ArrayList;

public class Tree {
	private String data;
	private String edge_label;
	private ArrayList<Tree> children;

	/**
	 * A constructor for the tree class
	 * 
	 * @param rootData
	 *            - the value of the node
	 */
	public Tree(String rootData) {
		data = rootData;
		edge_label = null;
		children = new ArrayList<Tree>();
	}

	/**
	 * Sets the data field of the Tree object
	 * @param data - data could be attribute or classification
	 */
	public void setData(String data) {
		this.data = data;
	}

	/**
	 * Sets the edge field of the Tree object
	 * @param label - code point for the edge that led to the Tree object
	 */
	public void setEdgeLabel(String label) {
		this.edge_label = label;
	}
	/**
	 * Returns the edge field of the Tree object
	 * @return label - code point for the edge that led to the Tree object
	 */
	public String getEdgeLabel() {
		return this.edge_label;
	}
	/**
	 * Returns the data field of the Tree object
	 * @return data - data could be attribute or classification
	 */
	public String getData() {
		return this.data;
	}

	/**
	 * Add a child(Tree object) to this Tree's children
	 * @param child - Tree object that's a child of this Tree object
	 */
	public void addChildren(Tree child) {
		this.children.add(child);
	}
	/**
	 * Returns the children of this Tree object
	 * @return children of Tree object
	 */
	public ArrayList<Tree> getChildren() {
		return this.children;
	}
}
