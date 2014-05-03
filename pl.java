import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Stack;
import java.util.TreeSet;

public class pl {

	private static BufferedWriter log = null;
	private static BufferedWriter output = null;

	public static void main(String[] args) throws IOException {

		int taskNumber = 0;
		String kb_input_file_path = null;
		String query_input_file_path = null;
		String output_entail = null;
		String output_log = null;

		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-t")) {
				taskNumber = Integer.parseInt(args[i + 1]);
			} else if (args[i].equals("-kb")) {
				kb_input_file_path = args[i + 1];
			} else if (args[i].equals("-q")) {
				query_input_file_path = args[i + 1];
			} else if (args[i].equals("-oe")) {
				output_entail = args[i + 1];
			} else if (args[i].equals("-ol")) {
				output_log = args[i + 1];
			}
		}

		ArrayList<String> queries = new ArrayList<String>();
		HashMap<String, String> kb = new HashMap<String, String>();
		ArrayList<String> facts = new ArrayList<String>();
		HashMap<String, Integer> count = new HashMap<String, Integer>();
		try {
			log = new BufferedWriter(new FileWriter(output_log));
			output = new BufferedWriter(new FileWriter(output_entail));
			Scanner s = new Scanner(new File(kb_input_file_path));
			String[] str = null;
			String line = null;
			String key = null;
			String val = null;
			while (s.hasNext()) {
				line = s.nextLine();
				if (line.contains(":-")) {
					str = line.split(":-");
					key = str[1].trim();
					val = str[0].trim();
					kb.put(key, val);
					count.put(key, key.split(",").length);
				} else {
					facts.add(line.trim());
				}
			}

			s = new Scanner(new File(query_input_file_path));

			while (s.hasNext()) {
				queries.add(s.nextLine());
			}

			if (taskNumber == 1) {
				log.write("<Known/Deducted facts>#Rules Fires#NewlyEntailedFacts\n");
				for (String q : queries) {
					HashMap<String, Integer> localCount = new HashMap<String, Integer>();
					copy(localCount, count);
					output.write(forwardChaining(kb, localCount, q, facts)+"\n");
				}
			} else if (taskNumber == 2) {
				log.write("<Queue of Goals>#Relevant Rules/Fact#New Goal Introduced\n");
				for (String q : queries) {
					HashMap<String, Integer> localCount = new HashMap<String, Integer>();
					copy(localCount, count);
					output.write(backwardChaining(kb, localCount, q, facts)+"\n");
					log.write("-------------------------------------------------------------\n");
				}
			} else if (taskNumber == 3) {
				log.write("Resolving clause 1#Resolving clause 2#Added clause \n");
				for (String q : queries) {
					HashMap<String, Integer> localCount = new HashMap<String, Integer>();
					copy(localCount, count);
					//System.out.println(resolution(kb, localCount, q, facts));
					output.write(resolution(kb, localCount, q, facts)+"\n");
					log.write("-------------------------------------------------------------\n");
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			log.close();
			output.close();
		}
	}

	public static void copy(HashMap<String, Integer> localCount,
			HashMap<String, Integer> count) {
		for (Entry<String, Integer> entry : count.entrySet()) {
			localCount.put(entry.getKey(), entry.getValue());
		}
	}

	public static boolean doNotAdd = true;

	public static String resolution(HashMap<String, String> kb,
			HashMap<String, Integer> count, String q, ArrayList<String> facts)
			throws IOException {

		TreeSet<String> query = new TreeSet<String>();
		query.add("-" + q);
		
		HashSet<TreeSet<String>> newList = new HashSet<TreeSet<String>>();
		ArrayList<TreeSet<String>> clauses = new ArrayList<TreeSet<String>>();

		TreeSet<String> cnf = null;
		for (Entry<String, String> entry : kb.entrySet()) {
			cnf = convertToCNF(entry);
			clauses.add(cnf);
		}
		clauses.add(query);
		for(String f : facts){
			TreeSet<String> fact =  new TreeSet<String>();
			fact.add(f);
			clauses.add(fact);
		}
		
		TreeSet<String> resolvent = new TreeSet<String>();
		int itr = 1;
		do {
			log.write("ITERATION = " + itr++);
			log.write("\n");
			for (int i = 0; i < clauses.size(); i++) {
				for (int j = i + 1; j < clauses.size(); j++) {
					doNotAdd = true;
					resolvent = resolve(clauses.get(i), clauses.get(j));
					if (resolvent.isEmpty()) {
						return "Yes";
					}
					if (!doNotAdd && !clauses.contains(resolvent)
							&& !newList.contains(resolvent)) {
						newList.add(resolvent);
						String str = clauses.get(i) + " # " + clauses.get(j)
								+ " # " + resolvent;
						str = str.replaceAll(",", " OR");
						//System.out.println(str);
						log.write(str);
					}
				}
				if (clauses.containsAll(newList)) {
					return "No";
				}
				clauses.addAll(newList);
			}
		} while (true);
	}

	public static TreeSet<String> resolve(TreeSet<String> treeSet,
			TreeSet<String> treeSet2) {
		TreeSet<String> resolvent = new TreeSet<String>();
		for (String s1 : treeSet) {
			if (s1.contains("-")) {
				if (!treeSet2.contains("" + s1.charAt(1))) {
					if (!resolvent.contains(s1)) {
						resolvent.add(s1);
					}
				} else {
					doNotAdd = false;
				}
			} else {
				if (!treeSet2.contains("-" + s1)) {
					if (!resolvent.contains(s1)) {
						resolvent.add(s1);
					}
				} else {
					doNotAdd = false;
				}
			}
		}
		for (String s2 : treeSet2) {
			if (s2.contains("-")) {
				if (!treeSet.contains("" + s2.charAt(1))) {
					if (!resolvent.contains(s2)) {
						resolvent.add(s2);
					}
				} else {
					doNotAdd = false;
				}
			} else {
				if (!treeSet.contains("-" + s2)) {
					if (!resolvent.contains(s2)) {
						resolvent.add(s2);
					}
				} else {
					doNotAdd = false;
				}
			}
		}

		return resolvent;
	}

	public static TreeSet<String> convertToCNF(Entry<String, String> rule) {
		TreeSet<String> newRule = new TreeSet<String>();
		String key = rule.getKey().trim();
		String val = rule.getValue().trim();
		if (key.contains(",")) {
			for (String s : key.split(",")) {
				newRule.add("-" + s.trim());
			}
			newRule.add(val.trim());
		} else {
			newRule.add("-" + key);
			newRule.add(val);
		}
		return newRule;

	}

	public static String backwardChaining(HashMap<String, String> kb,
			HashMap<String, Integer> count, String q, ArrayList<String> facts)
			throws IOException {

		Stack<String> s1 = new Stack<String>();
		s1.add(q);
		ArrayList<String> processed = new ArrayList<String>();
		return bchelper(s1, kb, facts, processed);
	}

	public static String bchelper(Stack<String> s1, HashMap<String, String> kb,
			ArrayList<String> facts, ArrayList<String> processed)
			throws IOException {

		String p = null;
		String[] prems = null;
		while (!s1.isEmpty()) {
			p = s1.pop();
			log.write(p + " # ");
			if (facts.contains(p)) {
				log.write(p + " # N/A \n");
				continue;
			}
			ArrayList<String> rules = getRules(kb, p);
			if (rules.isEmpty()) {
				log.write("# N/A # N/A\n");
				return "No";
			} else {
				for (String rule : getRules(kb, p)) {
					log.write(p + " :- " + rule + " # " + rule + "\n");
					Stack<String> s2 = new Stack<String>();
					fillStack(s2, s1);
					prems = rule.split(",");
					for (String s : prems) {
						s2.push(s.trim());
					}
					if (processed.contains(p)) {
						log.write(p + " # CYCLE DETECTED # N/A \n");
						return "No";
					} else {
						processed.add(p);
					}
					return bchelper(s2, kb, facts, processed);
				}
			}
		}
		return "Yes";
	}

	public static ArrayList<String> getRules(HashMap<String, String> kb,
			String q) {

		ArrayList<String> rules = new ArrayList<String>();
		String key = null;
		String val = null;
		for (Entry<String, String> entry : kb.entrySet()) {
			key = entry.getKey();
			val = entry.getValue();
			if (val.equals(q)) {
				rules.add(key);
			}
		}
		return rules;

	}

	public static void fillStack(Stack<String> s2, Stack<String> s1) {

		for (int i = 0; i < s1.size(); i++) {
			s2.add((String) s1.get(i));
		}
	}

	public static String forwardChaining(HashMap<String, String> kb,
			HashMap<String, Integer> count, String q, ArrayList<String> facts)
			throws IOException {

		LinkedList<String> agenda = new LinkedList<String>();
		ArrayList<String> inferred = new ArrayList<String>();
		ArrayList<String> logEntries = new ArrayList<String>();

		for (String fact : facts) {
			agenda.add(fact);
		}
		String key = null;
		String val = null;
		String p = null;
		while (!agenda.isEmpty()) {
			p = agenda.pop();
			if (!inferred.contains(p)) {
				inferred.add(p);
				for (Entry<String, String> entry : kb.entrySet()) {
					key = entry.getKey();
					val = entry.getValue();
					if (key.contains(p)) {
						count.put(key, count.get(key) - 1);
					}
					if (count.get(key) == 0) {
						if (val.equals(q)) {
							return "YES";
						} else {
							String str = "";
							for (String s : facts) {
								str += s + ", ";
							}
							if(!facts.contains(val)){
								facts.add(val);
								str += "#" + val + " :- " + key + "# " + val;
								logEntries.add(str);
								agenda.add(val);
							}
							for(String s : logEntries){
								log.write(s + "\n");
							}
							log.write("-------------------------------------------------------------\n");
						}
					}
				}
			}
		}
		return "NO";
	}
}
