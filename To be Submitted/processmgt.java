/*Programming Assignment 1
*Author: Ang Xuan Yin Joel
*ID: 1001075
*Date: 12/03/16 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class processmgt {
	public static File currentDirectory = new File(System.getProperty("user.dir"));	//setting current working directory
	public static void main(String[] args) {
		
		//initializing arg[0] as filename to parse
		String filename = null;
		if (args.length==1){ 
			filename = args[0];
		}	
		else {		
			System.out.println("Error: Invalid number of arguments.");
		}
		try {	
			System.out.println("Parsing contents of file into a process graph...");
			BufferedReader in = new BufferedReader(new FileReader(filename));		//bufferedreader for inputs of file
			ArrayList<ProcessGraphNode> list = new ArrayList<>();				//list of nodes
			String line;									//for reading line input from file
			String[] lineD;									//for delimited line
			int nodeNumber = 0;								//equals to line number in the file
			ProcessGraph processGraph = null;
			while ((line=in.readLine())!=null){
				lineD = line.split(":");										//(command, children, input, output)
				list.add(new ProcessGraphNode(nodeNumber, lineD[0], lineD[2], lineD[3], lineD[1], null, 1));		//adding list of nodes to list
				nodeNumber++;												//incrementing node number to next line
			}
			in.close();
			
			//build a process graph
			System.out.println("Creating a Process Graph...");
			processGraph = new ProcessGraph(list);
			
			//linking each proces graph node to the main process graph
			for (int i=0;i<processGraph.getList().size();i++){
				processGraph.getList().get(i).setProcessGraph(processGraph);
			}
			
			//identify root nodes by setting child nodes to 0 (not ready) so only root nodes will be ready to run
			//check for invalid nodes by seeing if the node numbers are > size-1 of list of noeds and whether it is <0
			System.out.println("Identifying and differentiating root nodes and child nodes. Also checking for invalid nodes...");
			processGraph.identifyRootNodes();											
			
			//run processes
			System.out.println("Start Execution:");
			processGraph.execute();
			
			
		}catch (IOException e){
			System.out.println("Error: File '"+filename+"' not found! Please run the program again.");	//filename arg[0] is an invalid file (does not exist)

		}catch (IndexOutOfBoundsException e){		//catches error if lines in file do not follow the standard format
			System.out.println("Error: Content of file containing process graph has a formatting error! Program has ended.");
			
		}catch (Exception e){
			System.out.println("Error detected! Program has ended.");
			e.printStackTrace();
		}
	}
}

class ProcessGraph {
	private ArrayList<ProcessGraphNode> list;
	
	public ProcessGraph(ArrayList<ProcessGraphNode> list){
		this.list = list;
	}
	
	public ArrayList<ProcessGraphNode> getList(){
		return this.list;
	}
	
	//identify root nodes by checking for children nodes of all nodes, and setting status of children nodes to 0 (= not ready)
	public void identifyRootNodes(){
		String[] childrenList = null;
		for (ProcessGraphNode node:list){
			if (!node.getChildren().equals("none")){
				childrenList=node.getChildren().split(" ");
				for (int i=0; i< childrenList.length; i++){
					if (Integer.valueOf(childrenList[i])>list.size()-1 || Integer.valueOf(childrenList[i]) <0){		//check that child node number is a valid process
						System.out.println("Child node "+childrenList[i]+" is an invalid process node! It will be ignored.");
					}
					else{
						this.list.get(Integer.valueOf(childrenList[i])).setStatus(0);						//mark children nodes as not ready
					}
				}
			}
		}
	}
	
	//check and return nodes that have a ready status (status=1)
	public ArrayList<ProcessGraphNode> getRunnableNodes(){
		ArrayList<ProcessGraphNode> runnableNodesList = new ArrayList<>();
		for (ProcessGraphNode node:list){
			if (node.getStatus()==1){
				runnableNodesList.add(node);
			}
		}
		return runnableNodesList;
	}
	
	public void execute(){
		boolean finished = false;	//used to check if all runnable process have completed
		boolean cycle;			//used to identify processes in a dependency cycle (infinite loop)
		while(!finished){			
			finished=true;
			cycle=true;
			boolean checkParent=true;									//boolean to check if parents have terminated (false if parents have not completed)
			for (ProcessGraphNode node:getRunnableNodes()){							//perform checking of parents for each runnable node
				ArrayList<Integer> parents=node.getParent();
				if (parents==null){									//if it is a root node, checkParent remains true
					System.out.println("Process node "+node.getNodeNumber()+" is a root node.");
				}
				else{
					for (int parentnode:parents){							//else, check that parents of runnable process are completed
						if(list.get(parentnode).getStatus()!=3){		
							checkParent=false;						//if parents are not completed, don't run
						}
					}
				}
							
				if (node.getStatus()==1&&checkParent){							//if process is ready to run and parent processes are completed, run it		
					try{
						node.setStatus(2);
						System.out.println("Starting process node "+node.getNodeNumber()+"...");
						node.start();
					}catch(Exception e){
						System.out.println("Error: Error when running process node thread "+node.getNodeNumber());;
					}
				}
			}
			//check if all processes have finished running (terminated and status=3), if they have, then end the program
			for (ProcessGraphNode node:this.list){
				if (node.getStatus()!=3){
					finished=false;
					if (node.getStatus()!=0){
						cycle = false;		//as long as one process is either ready to run or running, there is no cycle;
					}
				}
			}
			//end program if cycle detected
			if (cycle&&!finished){
				String nodesInCycle="";
				for (ProcessGraphNode node: list){
					if (node.getStatus() ==0){
						nodesInCycle+=" "+node.getNodeNumber();
					}
				}
				System.out.println("Error: Dependency cycle detected amongst processes:"+nodesInCycle+". Stopping infinite loop.");
				break;
			}
		}
		System.out.println("All runnable processes have ran. Ending Program.");
	}
}
	

//data structure for nodes
class ProcessGraphNode extends Thread {
	private int nodeNumber;				//node number which is line number in file
	private String command;				//first argument of delimited line
	private String input;				//third argument of delimited line
	private String output;				//fourth argument of delimited line
	private String children;			//second argument of delimited line
	private ArrayList<Integer> parent;		//parent(s) of process, null if root node
	private int status;				//initially 1 = ready, 0= not ready, 2= running, 3= terminated
	private ProcessGraph processGraph;		//links node to main process graph
	
	public ProcessGraphNode(int nodeNumber, String command, String input, String output, String children, ArrayList<Integer> parent, int status){
		this.nodeNumber=nodeNumber;
		this.command=command;
		this.input=input;
		this.output=output;
		this.children=children;
		this.parent=parent;
		this.status=status;
	}

	//defining get and set methods
	public int getNodeNumber(){
		return this.nodeNumber;
	}
	
	public String getCommand(){
		return this.command;
	}
	
	public String getInput(){
		return this.input;
	}
	
	public String getOutput(){
		return this.output;
	}
	
	public String getChildren(){
		return this.children;
	}
	
	public ArrayList<Integer> getParent(){
		return this.parent;
	}
	
	public int getStatus(){
		return this.status;
	}

	public ProcessGraph getProcessGraph(){
		return this.processGraph;
	}
	
	public void addParent(int newParent){
		//create new arraylist if current process has no parents
		if(this.parent==null){
			this.parent = new ArrayList<>();
			this.parent.add(newParent);
		}
		//adds on to the current arraylist of parents if process has more than 1 parent
		else{
			this.parent.add(newParent);
		}
	}

	public void setStatus(int newStatus){
		this.status = newStatus;
	}
	public void setProcessGraph(ProcessGraph processGraph){
		this.processGraph=processGraph;
	}

	public void processError(ProcessGraphNode node){
		if (!node.getChildren().equals("none")){	
			String[] childrenList = node.getChildren().split(" ");
			for (int i=0; i<childrenList.length;i++){
				if (Integer.valueOf(childrenList[i])>node.getProcessGraph().getList().size()-1&&Integer.valueOf(childrenList[i])>0){	//check that child node is valid
				}
				else{
					ProcessGraphNode child = node.getProcessGraph().getList().get(Integer.valueOf(childrenList[i]));
					if (child.getStatus()==0){	
						child.setStatus(3);									//set children nodes to terminate if child isn't already running
						System.out.println("Process node "+child.getNodeNumber()+" has been prematurely terminated because its parent node(s) did not run successfully!");
					}
					child.addParent(this.nodeNumber);	//set this node to parent of children nodes
					processError(child);			//recurse to end ALL child processes
				}
			}
		}
		node.setStatus(3);	//terminate process
	}
	
	public void run(){
		String[] commandnew = this.command.split(" ");
		ProcessBuilder pb = new ProcessBuilder(commandnew);
		pb.directory(processmgt.currentDirectory);
		try {
			//redirecting input
			if (!this.input.equals("stdin")){
				pb.redirectInput(ProcessBuilder.Redirect.from(new File(this.input)));
			}
			//redirecting output
			if (!this.output.equals("stdout")){
				pb.redirectOutput(ProcessBuilder.Redirect.to(new File(this.output)));
			}
			Process p = pb.start();
			//wait for end of process and print success message
			int exitValue = p.waitFor();		// if exitValue = 0, process has run successfully without errors
			System.out.println("Process node "+this.nodeNumber+" completed with an exit value of "+exitValue+".");
			if (exitValue!=0){
				System.out.println("There may possibly be an error in running process node "+this.nodeNumber+". Please check input/output values properly.");
			}
			
			//set process status to terminated
			this.status=3;	
			
			//setting child processes to ready
			if (!this.getChildren().equals("none")){	
				String[] childrenList = this.getChildren().split(" ");
				for (int i=0; i<childrenList.length;i++){
					if (Integer.valueOf(childrenList[i])>this.processGraph.getList().size()-1 || Integer.valueOf(childrenList[i])<0){	//ignore if child node is invalid
					}
					else {
						ProcessGraphNode child = this.processGraph.getList().get(Integer.valueOf(childrenList[i]));
						if (child.getStatus()==0){
							child.setStatus(1);							//set children nodes to ready if child isn't already running
						}
						this.processGraph.getList().get(Integer.valueOf(childrenList[i])).addParent(this.nodeNumber);		//set this node to parent of children nodes
					}
				}
			}

		//error catching
		} catch (IOException e) {
			System.out.println("Error: Process node "+this.nodeNumber+" did not run successfully! (Due to I/O errors or invalid command)");			
			processError(this);		//run function to terminate all child processes of failed process
		} catch (InterruptedException e) {
			System.out.println("Error: Process node "+this.nodeNumber+" has been interrupted!");
		} catch (Exception e){
			System.out.println("Error: Process node "+this.nodeNumber+" did not run successfully! (Due to other errors");
			e.printStackTrace();
		}
	}
}

