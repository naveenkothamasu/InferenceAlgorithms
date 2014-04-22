import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Scanner;



public class pl {

	public static void main(String[] args){
		
		int taskNumber = 0;
		String kb_input_file_path = null;
		String query_input_file_path = null;
		String output_entail = null;
		String output_log = null;
		
		for(int i=0; i< args.length; i++){
			if(args[i].equals("-t")){
				taskNumber = Integer.parseInt(args[i+1]);
			}else if(args[i].equals("-kb")){
				kb_input_file_path = args[i+1];
			}else if(args[i].equals("-q")){
				query_input_file_path = args[i+1];
			}else if(args[i].equals("-oe")){
				output_entail = args[i+1];
			}else if(args[i].equals("-ol")){
				output_log = args[i+1];
			}
		}
		ArrayList<String> queries = new ArrayList<String>();
		HashMap<String, String> kb = new HashMap<String, String>();
		ArrayList<String> facts = new ArrayList<String>();
		HashMap<String, Integer> count = new HashMap<String, Integer>();
		try {
			Scanner s = new Scanner(new File(kb_input_file_path));
			String[] str = null;
			String line = null;
			String key = null;
			String val = null;
			while(s.hasNext()){
				line = s.nextLine();
				if(line.contains(":-")){
					str = line.split(":-");
					key = str[1].trim();
					val = str[0].trim();
					kb.put(key, val);
					count.put(key, key.split(",").length);
				}else{
					facts.add(line.trim());
				}
			}
			
			s = new Scanner(new File(query_input_file_path));
			
			while(s.hasNext()){
				queries.add(s.nextLine());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		if(taskNumber == 1){
			for(String q : queries){
				HashMap<String, Integer> localCount = new HashMap<String, Integer>();
				copy(localCount, count);
				System.out.println(forwardChaining(kb, localCount, q, facts));
			}
		}
	}
	public static void copy(HashMap<String, Integer> localCount, HashMap<String, Integer> count){
		for(Entry<String, Integer> entry : count.entrySet()){
			localCount.put(entry.getKey(), entry.getValue());
		}
	}
	public static String forwardChaining(HashMap<String, String> kb, HashMap<String, Integer> count, String q, ArrayList<String> facts){
		
		LinkedList<String> agenda = new LinkedList<String>();
		ArrayList<String> inferred = new ArrayList<String>();
		/*
		for(Entry<String, String> entry : kb.entrySet()){
			if(entry.getValue().equals("-1")){
				agenda.add(entry.getKey());
			}
		}
		*/
		for(String fact : facts){
			agenda.add(fact);
		}
		String key = null;
		String val = null;
		String p = null;
		while(!agenda.isEmpty()){
			p = agenda.pop();
			if(!inferred.contains(p)){
				inferred.add(p);
				for(Entry<String, String> entry : kb.entrySet()){
					key = entry.getKey();
					val = entry.getValue();
					if(key.contains(p)){
						count.put(key, count.get(key)-1);
					}
					if(count.get(key) == 0){
						if(val.equals(q)){
							return "YES";
						}else{
							agenda.add(val);
						}
					}
				}
			}
		}
		return "NO";
	}
}
