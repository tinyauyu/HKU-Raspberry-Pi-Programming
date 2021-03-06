package lab2_capacity;

import java.net.*;
import java.util.Arrays;
import java.io.*;

public class Master extends Thread {
	
	private Socket[] workers;
	private BigArray array;
	private int boundary[][];
	
	public Master(String ip[], int port, int arraysize){
		workers = new Socket[ip.length];
		for (int i=0; i<ip.length; i++){
			try {
				workers[i] = new Socket(ip[i], port);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		array = new BigArray(arraysize);
		array.isSorted();
		boundary = array.split_boundary(ip.length+1);
	}
	
	@Override
	public void run(){
		ObjectOutputStream out[] = new ObjectOutputStream[workers.length];
		ObjectInputStream in[] = new ObjectInputStream[workers.length];
		
		try {
			
			for(int i=0; i<workers.length; i++){
				out[i] = new ObjectOutputStream(workers[i].getOutputStream());
				in[i] = new ObjectInputStream(workers[i].getInputStream());
			}
			
			// send out partitions
			for(int i=0; i<workers.length; i++){
				System.out.println("Sending partition " + i+1 + "...");
				out[i].writeUnshared(array.size());
				array.set_boundary(boundary[i+1][0], boundary[i+1][1]);
				//out[i].writeObject(array);
				
				array.setRemoteBoundary(out[i], boundary[i+1][0], boundary[i+1][1]);
				array.outputToStream(out[i], boundary[i+1][0], boundary[i+1][1]);
			}
			
			// sort own partition
			System.out.println("Start sorting a part of the array...");
			array.set_boundary(boundary[0][0], boundary[0][1]);
			array.mergesort();
			System.out.println("Done!");
			
			// collect partitions
			for(int i=0; i<workers.length; i++){
				try {
					System.out.println("Sending the partition(s) to Worker " + i+1);
					array.outputToStream(out[i], boundary[0][0], boundary[i][1]);
					//array.inputFromStream(in[i], boundary[i+1][0], boundary[i+1][1]);
					System.out.println("Geting back the merged array...");
					array.inputFromStream(in[i], boundary[0][0], boundary[i+1][1]);
					System.out.println("OK!");
					
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}	
			}
			
			array.isSorted();
			//System.out.println(array.toString());
		} catch (IOException | ClassNotFoundException e){
			e.printStackTrace();
		}
	}
	
	
	
	public static void main(String args[]){
		int size = 16000000;
		int port = 12345;
		String ip[] = {"127.0.0.1"};
		
		if (args.length>0){
			size = Integer.parseInt(args[0]);
		}
		if (args.length>1){
			port = Integer.parseInt(args[1]);
		}
		if (args.length>2){
			String assign_ip[] = Arrays.copyOfRange(args, 2, args.length);
			ip = assign_ip;
		}
		
//		String assign_ip[] = {"192.168.1.103","192.168.1.104"};
//		ip = assign_ip;
		
		
		Master master = new Master(ip, port, size);
		master.start();
	}
	

}
