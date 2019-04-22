import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main{
	private static int numNodes;
	private static int numLinks;
	private static String[] nodes;
	private static String[][] links;
	private static String[][] distances;
	private final static int MAX_NUMBER = 9999999;		//used to expressed infinity
	private static List<String> outputLines = new ArrayList<String>();
	
	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		System.out.print("Please enter the path to the input file: ");
		String fileName = in.nextLine();
		in.close();	//close the scanner
		
		//Building output file name
		String[] hold = fileName.split("\\.");	//split input file name on the "."
		String outputFileName = hold[0] + ".out";
		//System.out.println("outputFileName: " + outputFileName);
		
		try {
			//Outputs the current working directory
			//String dir = System.getProperty("user.dir");
			//System.out.println("current dir: " + dir);
			
			File file = new File(fileName);
			//System.out.println(file.exists());
			Scanner fileScanner = new Scanner(file);
			String line[];
			
			//1st line is numNodes and numLinks
			line = fileScanner.nextLine().split(" ");
			numNodes = Integer.parseInt(line[0]);
			numLinks = Integer.parseInt(line[1]);
			
			//2nd line is name of nodes
			nodes = new String[numNodes];
			nodes = fileScanner.nextLine().split(" ");
			
			//Rest of the lines are links and their distances
			links = new String[numLinks][3];
			for(int i = 0; i < numLinks; i++) {
				line = fileScanner.nextLine().split(" ");
				links[i][0] = line[0];
				links[i][1] = line[1];
				links[i][2] = line[2];
			}
			
			fileScanner.close();	//close file reading scanner
		}
		catch(IOException e) {
			System.out.println("Error reading the file.");
			return;
		}
		
		dijkstra();
		bellmanFord();
		
		Path file = Paths.get(outputFileName);
		try{
			Files.write(file, outputLines, Charset.forName("UTF-8"));
			System.out.println("OUTPUT ALSO PRINTED TO FILE: " + outputFileName);
			return;
		}
		catch(IOException e) {
			System.out.println("Error writing to file.");
			return;
		}
	}
	
	//Debugging
	private static void readInputFromConsole() {
		Scanner in = new Scanner(System.in);
		String hold[];
		
		System.out.print("Number of nodes and links in the network: ");
		hold = in.nextLine().split(" ");	//split the next input on the space
		numNodes = Integer.parseInt(hold[0]);
		numLinks = Integer.parseInt(hold[1]);
		
		System.out.print("Names of the nodes with spaces in between: ");
		nodes = new String[numNodes];
		nodes = in.nextLine().split(" ");

		System.out.println("Links and their Distances: ");
		links = new String[numLinks][3];
		for(int i = 0; i < numLinks; i++) {
			hold = in.nextLine().split(" ");
			links[i][0] = hold[0];
			links[i][1] = hold[1];
			links[i][2] = hold[2];
		}
		
		in.close();	//close the scanner
	}
	
	
	//Link State Algorithm (Dijkstra's)
	private static void dijkstra() {		
		//set of nodes whose least cost path is definitively known
		List<String> solvedNodes = new ArrayList<String>();
		String startingNode = nodes[0];		//first node is the starting node
		solvedNodes.add(startingNode);
		
		//Initialize distances
		distances = new String[nodes.length - 1][3];
		for(int i = 0; i < distances.length; i++) {
			//Node Names (ignoring starting node)
			distances[i][0] = nodes[i+1];
			//Cost to reach said node (arbitrary large number representing infinity)
			distances[i][1] = Integer.toString(MAX_NUMBER);
			//Predecessor node along path from source to node
			distances[i][2] = "Not reachable";
		}
		
		//Find all neighbors of the starting node
		for(int i = 0; i < links.length; i++) {
			if(links[i][0].equals(startingNode)) {
				int nodeIndex = distanceIndex(links[i][1]);
				distances[nodeIndex][1] = links[i][2];
				distances[nodeIndex][2] = startingNode;
			}
			else if (links[i][1].equals(startingNode)) {
				int nodeIndex = distanceIndex(links[i][0]);
				distances[nodeIndex][1] = links[i][2];
				distances[nodeIndex][2] = startingNode;
			}
		}
		
		//---------------MAIN LOOP-------------//
		while (solvedNodes.size() != nodes.length) {
			//Find the closest node that isn't already solved
			String closestNode = null;
			int closestDistance = MAX_NUMBER;
			for(int i = 0; i < distances.length; i++) {
				String currentNode = distances[i][0];
				int currentDistance = Integer.parseInt(distances[i][1]);
				if (currentDistance < closestDistance && !solvedNodes.contains(currentNode)) {
					closestNode = distances[i][0];
					closestDistance = currentDistance;
				}
			}
			//Add closest node to solved node list
			solvedNodes.add(closestNode);
			//System.out.println("New solved node: " + closestNode);
			
			
			//Update distances while taking into account the new node
			int newNodeSolvedIndex = solvedNodes.size() - 1;
			//System.out.println("checking: " + solvedNodes.get(newNodeSolvedIndex));
			//int newNodeDistancesIndex = distanceIndex(closestNode);
			for(int i = 0; i < links.length; i++) {
				//Neighbors of the new node that aren't already solved
				if (solvedNodes.get(newNodeSolvedIndex).equals(links[i][0]) && !solvedNodes.contains(links[i][1])) {
					//Distance to new node + cost of neighbor's link
					int possibleShorterDistance = closestDistance + Integer.parseInt(links[i][2]);
					int neighborDistancesIndex = distanceIndex(links[i][1]);
					int currentDistance = Integer.parseInt(distances[neighborDistancesIndex][1]);
					if (currentDistance > possibleShorterDistance) {
						distances[neighborDistancesIndex][1] = Integer.toString(possibleShorterDistance);
						distances[neighborDistancesIndex][2] = closestNode;
					}
				}
				else if (solvedNodes.get(newNodeSolvedIndex).equals(links[i][1]) && !solvedNodes.contains(links[i][0])) {
					//Distance to new node + cost of neighbor's link
					int possibleShorterDistance = closestDistance + Integer.parseInt(links[i][2]);
					int neighborDistancesIndex = distanceIndex(links[i][0]);
					int currentDistance = Integer.parseInt(distances[neighborDistancesIndex][1]);
					if (currentDistance > possibleShorterDistance) {
						distances[neighborDistancesIndex][1] = Integer.toString(possibleShorterDistance);
						distances[neighborDistancesIndex][2] = closestNode;
					}
				}
			}
		}
		
		
		//Print out results
		System.out.println("Final Dijkstra Results: ");
		for(int i = 0; i < distances.length; i++) {
			//Destination
			String node = distances[i][0];
			
			//Dijkstra's path to destination
			List<String> path = new ArrayList<String>();
			path.add(distances[i][2]);
			while(!path.contains(startingNode)) {
				String predecessorNode = path.get(path.size() - 1);
				int predecessorNodeDistancesIndex = distanceIndex(predecessorNode);
				path.add(distances[predecessorNodeDistancesIndex][2]);
			}
			String[] pathArray = path.toArray(new String[0]);
			String pathString = "";
			for(int j = pathArray.length-1; j > -1; j--) {
				pathString = pathString + pathArray[j];
				pathString = pathString + "-";
			}
			pathString = pathString + node;
			
			//Distance from origin to destination
			String distance = distances[i][1];
			
			String output = node + ": " + pathString + " " + distance;
			System.out.println(output);		//output to console
			outputLines.add(output);		//output to file
		}
		System.out.println("-----------"); 	//Splits up the Dijkstra and bellmanFord results
	}

	//Helper Function for Dijkstra()
	//Returns the index of the given node in the distances array
	private static int distanceIndex(String node) {
		//System.out.println("Comparing Node: " + node);
		for(int i = 0; i < distances.length; i++) {
			if (node.equals(distances[i][0])) {
				return i;
			}
		}
		System.out.println("Node does NOT exist in the distances array!");
		return -1;
	}
	
	//Prints out the distances array
	//Used for debugging Dijkstra()
	private static void printDistances() {
		System.out.println("--------------------------------");
		System.out.println("Distances Table: ");
		for(int i = 0; i < distances.length; i++) {
			System.out.println(distances[i][0] + " " + distances[i][1] + " " + distances[i][2]);
		}
		System.out.println("--------------------------------");
	}
	
	
	
	
	
	//Distance Vector Algorithm (Bellman-Ford)
	private static void bellmanFord() {
		//A "numNodes by numNodes" table for each of the numNodes nodes
		//Ex: 3 nodes = 3 node tables each of which is 3x3
		//Order = table #, row #, column #
		int[][][] nodeTables = new int[numNodes][numNodes][numNodes];
		
		//Initialize the node tables
		int numRounds = 2;
		for(int table = 0; table < numNodes; table++) {
			for(int row = 0; row < numNodes; row++) {
				//Only need to update your own row (IE table Y only updates row Y)
				if (table == row) {
					String node = nodes[table];
					for(int col = 0; col < numNodes; col++) {
						String neighbor = nodes[col];
						nodeTables[table][row][col] = neighborDistance(node, neighbor);
					}
				}
				//Init everyother row as 
				else {
					for(int col = 0; col < numNodes; col++) {
						nodeTables[table][row][col] = MAX_NUMBER;
					}
				}
			}
		}
		//System.out.println("init: ");
		//printCompleteBellmanFordResults(nodeTables);
		
		//Init Changes table
		//True on changes[i] means that row "table[i][i]" has been updated
		boolean[] changes = new boolean[numNodes];
		for (int i = 0; i < numNodes; i++) {
			changes[i] = true;
		}
		boolean notComplete = true;
		
		while(notComplete) {
			
			//Updates
			for(int table = 0; table < numNodes; table++) {
				for(int row = 0; row < numNodes; row++) {
					//Transfer the other node's rows to this table
					if (table != row && changes[row] == true) {
						for(int col = 0; col < numNodes; col++) {
							nodeTables[table][row][col] = nodeTables[row][row][col];
						}
					}
				}
			}
			//System.out.println("Update changes: ");
			//printCompleteBellmanFordResults(nodeTables);
			
			//Return all changes table to false
			for(int i = 0; i < numNodes; i++) {
				changes[i] = false;
			}
			
			//Update the minimum distances
			for(int table = 0; table < numNodes; table++) {
				int row = table;
				for(int col = 0; col < numNodes; col++) {
					int originalValue = nodeTables[table][row][col];
					int minDistance = MAX_NUMBER;
					for(int minIndex = 0; minIndex < numNodes; minIndex++) {
						//if (minIndex != col) {
							int costToNeighbor = nodeTables[table][row][minIndex];
							int costFromNeighborToDest = nodeTables[table][minIndex][col];
							int distance = costToNeighbor + costFromNeighborToDest;
							if (distance < minDistance) {
								minDistance = distance;					
							}
						//}
					}
					if(minDistance != originalValue) {
						nodeTables[table][row][col] = minDistance;
						changes[table] = true;
					}
				}
			}
			//System.out.println("Min Distance Update: ");
			//printCompleteBellmanFordResults(nodeTables);
			
			boolean confirm = false;
			for(int i = 0; i < numNodes; i++) {
				if (changes[i] == true) {
					confirm = true;
					break;
				}
			}
			if (!confirm) {
				notComplete = false;
			}
			else {
				numRounds++;
			}
		}
		
		//Print final node table (only one needed)
		System.out.println("Final BellmanFord Results: ");
		printBellmanFordResults(nodeTables);
		
		System.out.println(numRounds);
		outputLines.add(Integer.toString(numRounds));
	}
	
	
	//Returns the distance from a node to a potential neighbor
	//If they aren't neighbors, returns infinity
	private static int neighborDistance(String node, String neighbor) {
		if (node.equals(neighbor)) {
			return 0;
		}
		
		for(int i = 0; i < links.length; i++) {
			if (node.equals(links[i][0]) && neighbor.equals(links[i][1])) {
				return Integer.parseInt(links[i][2]);
			}
			else if (neighbor.equals(links[i][0]) && node.equals(links[i][1])) {
				return Integer.parseInt(links[i][2]);
			}
		}
		//node and neighbor aren't neighbors
		return MAX_NUMBER;
	}
	
	//Helper / Debugger Function for BellmanFord
	//Outputs to console AND to file
	private static void printBellmanFordResults(int[][][] nodeTables) {
		for(int i = 0; i < numNodes; i++) {
			String output = "";
			for(int j = 0; j < numNodes-1; j++) {
				output = output + Integer.toString(nodeTables[0][i][j]) + " ";
			}
			output = output + Integer.toString(nodeTables[0][i][numNodes-1]);
			
			System.out.println(output);
			outputLines.add(output);
		}
	}
	
	//Debugger Function for BellmanFord
	//Prints out the tables for ALL of the nodes
	private static void printCompleteBellmanFordResults(int[][][] nodeTables){
		for(int table = 0; table < numNodes; table++) {
			for(int i = 0; i < numNodes; i++) {
				String output = "";
				for(int j = 0; j < numNodes-1; j++) {
					output = output + Integer.toString(nodeTables[table][i][j]) + " ";
				}
				output = output + Integer.toString(nodeTables[table][i][numNodes-1]);
				System.out.println(output);
			}
			System.out.println("---");
		}
		System.out.println("_*_*_*_*_*__*");
	}
	
}