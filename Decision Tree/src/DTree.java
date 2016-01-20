import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

/**
 * @name: DTree
 * @description:  The Decision Tree class. Builds a decision tree from a training
 * 				  set and tests the tree using the testing set provided.
 * @author: Awelemdy Orakwue
 */

public class DTree {

	private static Scanner in;
	private static double percent;
	private ArrayList<String[]> trainingSet = new ArrayList<String[]>(); // Holds the training set
	private ArrayList<String[]> testingSet = new ArrayList<String[]>(); // Holds the test set
	private HashMap<String, String> decisions = new HashMap<String, String>(); // Associates t to true and f to false.
	private ArrayList<String> decisions_key = new ArrayList<String>(); // Holds the decision key
	private HashMap<String, String> attributesMap = new HashMap<String, String>(); // Holds each attribute's sub group.
	private HashMap<String, String> attributeIndex = new HashMap<String, String>(); // Maps attribute to table index
	private ArrayList<String> attributes = new ArrayList<String>(); // Holds the attributes
	private int correct = 0;

	/**
	 * Main function that starts the decision tree program
	 * Sample: DTree <file> - <file contents percentage split btw training & test data>  => DTree mushroom - 100 where 
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 3)
			usage("Usage: java DTree inputfile outputfile percent");

		// Validate arguments
		try {
			in = new Scanner(new File(args[0]));
		} catch (FileNotFoundException e) {
			usage("File not found: " + args[0]);
		}
		try {
			percent = Double.parseDouble(args[2]);
			if (percent < 1 || percent > 100) {
				usage("Value for percent must be between 1 and 100");
			}
		} catch (NumberFormatException nfe) {
			usage("Value for percent must be between 1 and 100");
		}

		if (!args[1].equals("-")){
			try {
				System.setOut(new PrintStream(new File(args[1])));
			} catch (FileNotFoundException e) {
			}
		}
		DTree obj = new DTree();
		obj.reader(); // read in file
	}

	/**
	 * Reads in data from file user passed in
	 */
	public void reader() {
		// Read in training and testing samples
		@SuppressWarnings("unused")
		int num_features = Integer.parseInt(in.nextLine());
		int file_length = Integer.parseInt(in.nextLine());
		double n = ((percent / (double) 100) * (double) file_length);
		int limit = (int) Math.floor(n);
		int count = 1;
		int index = 1;
		while (in.hasNextLine()) {
			String line = in.nextLine();
			if (line.equals("")) // Empty line reached
				continue;
			else {
				String[] split = line.split(":");
				if(split.length == 1 && percent == 100){
					this.trainingSet.add(line.split(","));
					this.testingSet.add(line.split(","));
				}else if (split.length == 1 && count <= limit) { // data sets reached
					this.trainingSet.add(line.split(","));
					count++;
				} else if ( split.length == 1 && count > limit) {
					this.testingSet.add(line.split(","));
					count++;
				} else {
					if (split[0].equals("classes")) {
						String[] second_split = split[1].split(",");
						for (String l : second_split) {
							String[] third_split = l.split("=");
							this.decisions.put(third_split[0], third_split[1]);
							this.decisions_key.add(third_split[0]);
						}
					} else { // Parse Attributes
						this.attributesMap.put(split[0], split[1]);
						this.attributes.add(split[0]);
						this.attributeIndex.put(split[0], (index+""));
						this.attributeIndex.put((index+""), split[0]);
						String[] temp = split[1].split(",");
						for(String p :  temp){
							this.attributesMap.put(p.split("=")[0], split[0]);
						}
						index += 1;
					}
				}
			}
		}
		in.close();
		System.out.println("\nThe Decision Tree Built With "+this.trainingSet.size()+" Training Samples:\n");
		Tree root = buildTree(this.trainingSet, this.attributes, 0, null);
		classify(root, this.testingSet);
	}

	/**
	 * Performs tests on the decision tree
	 * @param root - root node of the tree
	 * @param testingSet2 - the test dataset
	 */
	private void classify(Tree root, ArrayList<String[]> testingSet2) {
		System.out.println("\nClassification Results On " + testingSet2.size() + " Test Samples:\n");
		for(String[] p : testingSet2){
			String classification = p[0];
			testDataSet(Arrays.copyOfRange(p, 1, p.length), classification, root, 1, p.length);
		}
		System.out.println("Number of correct classifications: " + this.correct);
		System.out.println("Number of incorrect classifications: " + (testingSet2.size() - this.correct));
	}

	/**
	 * Recursively performs the test on each test set
	 * @param path - test set
	 * @param classification - true classification
	 * @param root - root node of tree
	 * @param index - index used to retrieve attribute
	 * @param length - length of the test set
	 */
	private void testDataSet(String[] path, String classification, Tree root, int index, int length) {
		String edge = null;
		String attribute = null;
		if(path.length != 0 && index < length){
			edge = path[0];
			attribute = this.attributeIndex.get((index+""));
		}
		if(root.getData().equals(classification) && root.getChildren().size() == 0){
			this.correct += 1;
			//return;
		}else if(!root.getData().equals(classification) && root.getChildren().size() == 0){
			return;
		}else if(attribute!= null && attribute.equals(root.getData())){
			ArrayList<Tree> l = root.getChildren();
			for(Tree t : l){
				if(t.getEdgeLabel().equals(edge)){
					testDataSet(Arrays.copyOfRange(path, 1, path.length), classification, t, index+1,length);
					break;
				}
			}
			
		}else if(root.getData().equals("undetermined") || path.length == 0){
			this.correct += 1;
			return;
		}else{
			testDataSet(Arrays.copyOfRange(path, 1, path.length), classification, root, index+1,length);
		}
		return;
	}

	/**
	 * Builds the ID3 tree for the training data passed in
	 * 
	 * @param trainingSet2 - the training data
	 * @param attributes2 - the attributes
	 * @param level 
	 */
	public Tree buildTree(ArrayList<String[]> trainingSet2, ArrayList<String> attributes2, int level, String branch) {
		Tree root = new Tree(null);
		String check = sameClass(trainingSet2);
		if (!check.equals("")) { // Same number of classes
			root.setData(check);
		} else if (trainingSet2.size() == 0) { // return default class
			root.setData("undetermined");
		} else {
			double maxGain = Double.NEGATIVE_INFINITY;
			String attribute = "";
			double dataEntropy = computeDataEntropy(trainingSet2); // compute the gain for each attribute
			for (String l : attributes2) {
				double gain = computeGain(trainingSet2, l, dataEntropy);
				if (gain > maxGain) {
					attribute = l;
					maxGain = gain;
				}
			}
			root.setData(attribute);
			System.out.println("Level " + level + " Attribute: " + attribute);
			ArrayList<String> new_attribute = new ArrayList<String>();
			for (String p : attributes2) {
				if (!p.equals(attribute))
					new_attribute.add(p);
			}
			String[] leaves = this.attributesMap.get(attribute).split(",");
			for (String l : leaves) {
				String[] split = l.split("=");
				String code = split[0];
				ArrayList<String[]> new_data = getData(code, attribute, trainingSet2);
				if(new_data.size() == 0){
					System.out.println("At level " +  (level+1) + ", " + l + ", decision: undetermined");
				}else{
					String check2 = sameClass(new_data);
					if (!check2.equals("")) // Same number of classes
						System.out.println("At level " +  (level+1) + ", " + l + ", decision: " +  check2 + "=" + this.decisions.get(check2));
					else
						System.out.println("Split tree on " + l);
				}
				Tree new_tree = buildTree(new_data, new_attribute, level+1, l);
				new_tree.setEdgeLabel(split[0]);
				root.addChildren(new_tree);
			}
		}
		return root;
	}
	/**
	 * Computes the data entropy of the training set 
	 * @param trainingSet2
	 * @return - entropy
	 */
	public double computeDataEntropy(ArrayList<String[]> trainingSet2) {
		int length = trainingSet2.size();
		int trues = 0;
		int falses = 0;
		for (String[] l : trainingSet2) {
			if (l[0].equals(this.decisions_key.get(0)))
				trues++;
			else if (l[0].equals(this.decisions_key.get(1)))
				falses++;
		}
		return entropy((double)trues, (double)falses, (double)length);
	}

	/**
	 * Computes the entropy using arguments
	 * @param trues - number of true classification
	 * @param falses - number of false classification
	 * @param length - size of the data
	 * @return - entropy calculated
	 */
	public double entropy(double trues, double falses, double length) {
		double x = 0.0;
		double y = 0.0;
		if (falses == 0) {
			y = 0;
		} else {
			y = Math.log( (falses / length));
		}
		if (trues == 0) {
			x = 0;
		} else {
			x = Math.log( (trues / length) );
		}
		if(length == 0)
			length = 1; // To avoid division by zero
		double entropy = -1 * ( ( (trues / length) * (x / Math.log(2) ) ) +  ( (falses / length) * (y / Math.log(2)))  );
		return entropy;
	}

	/**
	 * Retrieves the new training data from the passed in data based on the code
	 * point
	 * 
	 * @param code - the sub attribute within an attribute to retrieve all data
	 *            	 relating to
	 * @param attribute 
	 * @param main_data - the training set
	 * @return  a new training set data
	 */
	public ArrayList<String[]> getData(String code, String attribute, ArrayList<String[]> main_data) { // change
		ArrayList<String[]> result = new ArrayList<String[]>();
		int index = Integer.parseInt(this.attributeIndex.get(attribute));
		for (String[] l : main_data) {
			if (l[index].equals(code)){
				l[index] = "";
				result.add(l);
			}
		}
		return result;
	}

	/**
	 * Computes the gain of an attribute given the overall data's entropy.
	 * @param trainingSet2 - the data to compute from
	 * @param attribute - the attribute whose gain is being computed
	 * @param dataEntropy - the overall entropy of the data
	 * @return - the gain of the attribute
	 */
	public double computeGain(ArrayList<String[]> trainingSet2, String attribute, double dataEntropy) { // change
		// Get Entropy of the target attribute, l.
		String[] leaves = this.attributesMap.get(attribute).split(",");
		int index = Integer.parseInt(this.attributeIndex.get(attribute));
		double attribute_entropy = 0.0;
		int length = trainingSet2.size();
		for (String p : leaves) {
			int occurrence = 0;
			int trues = 0;
			int falses = 0;
			String[] split = p.split("=");
			String code = split[0];
			for (String[] l : trainingSet2) {
				if (l[index].equals(code) && this.decisions_key.get(0).equals(l[0])) {
					occurrence++;
					trues++;
				} else if (l[index].equals(code) && this.decisions_key.get(1).equals(l[0])) {
					occurrence++;
					falses++;
				}
			}
			// calculate the entropy of the sub branch
			double entropy = entropy((double)trues, (double)falses, (double)occurrence);
			attribute_entropy += ((double) occurrence / (double) length)
					* entropy;
		}
		return (dataEntropy - attribute_entropy);
	}

	/**
	 * Checks if the data contains the same class.
	 * 
	 * @param trainingSet2
	 *            - the data to check
	 * @return string representation of class
	 */
	public String sameClass(ArrayList<String[]> trainingSet2) {
		
		if(trainingSet2.size() == 0)
			return "";
		int i = 0;
		int j = 0;
		for (String[] l : trainingSet2) {
			if (this.decisions_key.get(0).equals(l[0])) {
				i++;
			} else if (this.decisions_key.get(1).equals(l[0])) {
				j++;
			}
		}
		if (i == trainingSet2.size())
			return this.decisions_key.get(0);
		else if (j == trainingSet2.size())
			return this.decisions_key.get(1);
		else
			return "";
	}

	/**
	 * Prints a usage message if user starts program with incorrect arguments.
	 */
	private static void usage(String str) {
		System.out.println(str);
		System.exit(0);
	}

}
