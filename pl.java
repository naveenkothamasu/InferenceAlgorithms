import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Stack;

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
					System.out
							.println(forwardChaining(kb, localCount, q, facts));
				}
			} else if (taskNumber == 2) {
				for (String q : queries) {
					HashMap<String, Integer> localCount = new HashMap<String, Integer>();
					copy(localCount, count);
					System.out.println(backwardChaining(kb, localCount, q,
							facts));
				}
			} else if(taskNumber == 3){
				
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

	public static String backwardChaining(HashMap<String, String> kb,
			HashMap<String, Integer> count, String q, ArrayList<String> facts) {

		Stack<String> s1 = new Stack<String>();
		s1.add(q);
		ArrayList<String> processed = new ArrayList<String>();
		return bchelper(s1, kb, facts, processed);
	}

	public static String bchelper(Stack<String> s1, HashMap<String, String> kb,
			ArrayList<String> facts, ArrayList<String> processed) {

		String p = null;
		String[] prems = null;
		while (!s1.isEmpty()) {
			p = s1.pop();
			if (facts.contains(p)) {
				continue;
			}
			ArrayList<String> rules = getRules(kb, p);
			if (rules.isEmpty()) {
				return "No";
			} else {
				for (String rule : getRules(kb, p)) {
					Stack<String> s2 = new Stack<String>();
					fillStack(s2, s1);
					prems = rule.split(",");
					for (String s : prems) {
						s2.push(s.trim());
					}
					if (processed.contains(p)) {
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
							str += "#" + val + " :- " + key + "# " + val;
							log.write(str + "\n");
							agenda.add(val);
							facts.add(val);
							log.write("-------------------------------------------------------------\n");
						}
					}
				}
			}
		}
		return "NO";
	}
}
