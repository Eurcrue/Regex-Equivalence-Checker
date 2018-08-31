package regex;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

public class RegularExpression {
	private static int stateID = 0;
	
	private static Stack<NFA> stackNfa = new Stack<NFA> ();
	private static Stack<Character> operator = new Stack<Character> ();	

	private static Set<State> set1 = new HashSet <State> ();
	private static Set<State> set2 = new HashSet <State> ();
	
	// Set of inputs
	private static Set <Character> input = new HashSet <Character> ();
	private static Set<Character> removed = new HashSet<Character>();	
	private static DFA comb = new DFA();
	
	public static void addChar(char a){input.add(a); removed.remove(a);}
	public static void removeChar(char a){input.remove(a); removed.add(a);}
	public static Set<Character> getInputSet(){return input;}
	
	// Generates NFA using the regular expression
	public static NFA generateNFA(String regular) {
		// Generate regular expression with the concatenation
		regular = AddConcat (regular);
		
		// Only inputs available
		input.add('a');
		input.add('b');
		
		for(char a: removed) input.remove(a);
		
		// Cleaning stacks
		stackNfa.clear();
		operator.clear();

		for (int i = 0 ; i < regular.length(); i++) {	

			if (isInputCharacter (regular.charAt(i))) {
				pushStack(regular.charAt(i));
				
			} else if (operator.isEmpty()) {
				operator.push(regular.charAt(i));
				
			} else if (regular.charAt(i) == '(') {
				operator.push(regular.charAt(i));
				
			} else if (regular.charAt(i) == ')') {
				while (operator.get(operator.size()-1) != '(') {
					doOperation();
				}				
		
				// Pop the '(' left parenthesis
				operator.pop();
				
			} else {
				while (!operator.isEmpty() && 
						Priority (regular.charAt(i), operator.get(operator.size() - 1)) ){
					doOperation ();
				}
				operator.push(regular.charAt(i));
			}		
		}		
		
		// Clean the remaining elements in the stack
		while (!operator.isEmpty()) {	doOperation(); }
		
		// Get the complete nfa
		NFA completeNfa = stackNfa.pop();
		
		// add the accpeting state to the end of NFA
		completeNfa.getNfa().get(completeNfa.getNfa().size() - 1).setAcceptState(true);
		
		// return the nfa
		return completeNfa;
	}
	
	/**
	 * Checks if two regex are equal
	 * 
	 * @param regex1 first one
	 * @param regex2 second one
	 * @return whether or not they are equal
	 * 
	 * @author Reynald Oliveria
	 */
	public static boolean isEqual(String regex1, String regex2){
		stackNfa.clear();
		
		/*
		 * This block is for creating DFA for regex1 - regex2 = regex1 interesect (not regex2)
		 */
		//make regex2 complement
		DFA reg2 = generateDFA(generateNFA(regex2));
		for(int i = 0; i < reg2.getDfa().size(); i++){
			reg2.getDfa().get(i).setAcceptState(!reg2.getDfa().get(i).isAcceptState());
		}
		NFA compReg2 = new NFA();
		compReg2.setNfa(reg2.getDfa());
		//create the intersection by demorgans law
		NFA reg1 = generateNFA(regex1);
		stackNfa.push(reg1);
		stackNfa.push(compReg2);
		union();
		DFA combined = generateDFA(stackNfa.pop());
		for(int i = 0; i < combined.getDfa().size(); i++){
			combined.getDfa().get(i).setAcceptState(!combined.getDfa().get(i).isAcceptState());
		}
		comb = combined;
		
		
		/*
		 * This block checks if the accept states are reachable
		 * They should not be, if the two are equal
		 */
		List<State> reachable = new ArrayList<State>();
		int numAdded = 1;
		int size = 0;
		int index = 0;
		reachable.add(combined.getDfa().get(0));
		while(numAdded > 0){
			numAdded = 0;
			size = reachable.size();
			for(index = index+1-1; index < size; index++){
				for(char c : reachable.get(index).getNextState().keySet()){
					for(int j = 0; j < reachable.get(index).getAllTransitions(c).size(); j++){
						if(!reachable.contains(reachable.get(index).getAllTransitions(c).get(j))){
							reachable.add(reachable.get(index).getAllTransitions(c).get(j));
							numAdded++;
						}
					}
				}
			}
		}
		
		List<State> accepts = new ArrayList<State>();
		for(int i = 0; i < combined.getDfa().size(); i++){
			if(combined.getDfa().get(i).isAcceptState()) accepts.add(combined.getDfa().get(i));
		}
		for(int i = 0; i < accepts.size(); i++){
			if(reachable.contains(accepts.get(i))) return false;//if accept state is reachable, then not equal
		}
		
		return isEqual1(regex2, regex1);//check regex2-regex1 this time
	}
	
	//Literally just a repeat
	private static boolean isEqual1(String regex1, String regex2){
		stackNfa.clear();
		
		DFA reg2 = generateDFA(generateNFA(regex2));
		for(int i = 0; i < reg2.getDfa().size(); i++){
			reg2.getDfa().get(i).setAcceptState(!reg2.getDfa().get(i).isAcceptState());
		}
		
		NFA compReg2 = new NFA();
		compReg2.setNfa(reg2.getDfa());
		
		NFA reg1 = generateNFA(regex1);
		
		stackNfa.push(reg1);
		stackNfa.push(compReg2);
		union();
		DFA combined = generateDFA(stackNfa.pop());
		for(int i = 0; i < combined.getDfa().size(); i++){
			combined.getDfa().get(i).setAcceptState(!combined.getDfa().get(i).isAcceptState());
		}
		
		List<State> reachable = new ArrayList<State>();
		int numAdded = 1;
		int size = 0;
		int index = 0;
		reachable.add(combined.getDfa().get(0));
		while(numAdded > 0){
			numAdded = 0;
			size = reachable.size();
			for(index = index+1-1; index < size; index++){
				for(char c : reachable.get(index).getNextState().keySet()){
					for(int j = 0; j < reachable.get(index).getAllTransitions(c).size(); j++){
						if(!reachable.contains(reachable.get(index).getAllTransitions(c).get(j))){
							reachable.add(reachable.get(index).getAllTransitions(c).get(j));
							numAdded++;
						}
					}
				}
			}
		}
		
		List<State> accepts = new ArrayList<State>();
		for(int i = 0; i < combined.getDfa().size(); i++){
			if(combined.getDfa().get(i).isAcceptState()) accepts.add(combined.getDfa().get(i));
		}
		for(int i = 0; i < accepts.size(); i++){
			if(reachable.contains(accepts.get(i))) return false;
		}
		
		return true;
	}
	
	/**
	 * Finds a string that one regex accepts and the other doesnt
	 * 
	 * @param regex1 regex to be tested
	 * @param regex2 regex to be tested
	 * @return a string that one regex accepts and the other doesnt
	 */
	public static String causeOfInequality(String regex1, String regex2){
		//checks for which regex to check
		if(isEqual(regex1,regex2)) return "";//checks if two are equal first
		
		//which regex is the algorithm going to search for
		int match = 0;
		if(isEqual1(regex1,regex2)){
			match = 1;
			isEqual(regex2,regex1);
		}else{
			match = 2;
			isEqual(regex1,regex2);
		}
		//DFA test = comb;
		
		if(comb.getDfa().get(0).isAcceptState()) return match +": e";//shortcut if one regex accepts null string
		
		/*
		 * This is just BFS at this point
		 */
		List<ArrayList<Integer>> adList = new ArrayList<ArrayList<Integer>>();//creates adjacency list
		List<Integer> visited = new ArrayList<Integer>();
		for(int i = 0; i < comb.getDfa().size(); i++) adList.add(new ArrayList<Integer>());
		for(int i = 0; i < comb.getDfa().size(); i++){
			for(char c : input){
				adList.get(i).add(comb.getDfa().indexOf(comb.getDfa().get(i).getAllTransitions(c).get(0)));
			}
		}
		
		List<ArrayList<Integer>> reverse = new ArrayList<ArrayList<Integer>>();//reversed adjacency list to traceback
		for(int i = 0; i < comb.getDfa().size(); i++) reverse.add(new ArrayList<Integer>());
		for(int i = 0; i < adList.size(); i++){
			for(int j = 0; j < adList.get(i).size(); j++){
				if(!reverse.get(adList.get(i).get(j)).contains(i)) reverse.get(adList.get(i).get(j)).add(i);
			}
		}
		
		/*
		 * Make a BFS Tree
		 */
		visited.clear();
		List<ArrayList<Integer>> tree = new ArrayList<ArrayList<Integer>>();
		tree.add(new ArrayList<Integer>());
		tree.get(0).add(0);
		visited.add(0);
		int dist = 0;
		int index = 0;
		outer:
		while(visited.size() < comb.getDfa().size()){
			tree.add(new ArrayList<Integer>());
			for(int i : tree.get(tree.size()-2)){
				for(int j : adList.get(i)){
					if(!visited.contains(j)){
						visited.add(j);
						tree.get(tree.size()-1).add(j);
					}
					if(comb.getDfa().get(j).isAcceptState()){
						dist = tree.size();
						index = tree.get(tree.size()-1).size()-1;
						break outer;//stop when you find an accept state
					}
				}
			}
		}
		
		/*
		 * Backtrack to find a path
		 */
		adList = reverse;
		int[] path = new int[dist];
		path[dist - 1] = tree.get(dist-1).get(index);
		for(int i = dist - 2; i > -1; i--){
			for(int j = 0; j < adList.get(path[i+1]).size(); j++){
				if(tree.get(i).contains(adList.get(path[i+1]).get(j))){
					path[i] = adList.get(path[i+1]).get(j);
					break;
				}
			}
		}
		
		/*
		 * Track forward to create the string
		 */
		String ret = "only Regex "+match+" matches: \"";
		for(int i = 0; i < dist - 1; i++){
			for(char c : input){
				if(comb.getDfa().get(path[i]).getAllTransitions(c).contains(comb.getDfa().get(path[i+1]))){
					ret += c;
					break;
				}
			}
		}
		
		return ret+"\"";
		
	}
	
	/**
	 * Checks if a regex matches a string
	 * 
	 * @param regex the regular expression
	 * @param testString the string to test
	 * @return whether or not the regex matches the testString
	 */
	public static boolean match (String regex, String testString){
		DFA dfa = generateDFA(generateNFA(regex));
		State current = dfa.getDfa().getFirst();
		
		//runs through the DFA
		for(int i = 0; i < testString.length(); i++){
			if(testString.charAt(i) == '~') continue;
			if(!isInputCharacter(testString.charAt(i))){
				System.out.println("bad");
				return false;
			}
			current = current.getAllTransitions(testString.charAt(i)).get(0);
		}
		return current.isAcceptState();
	}
	
	public static String DFAToRegex(DFA dfa){
		String regex = "";
		List<ArrayList<Integer>> adList = new ArrayList<ArrayList<Integer>>();//creates adjacency list
		List<Integer> visited = new ArrayList<Integer>();
		for(int i = 0; i < dfa.getDfa().size(); i++) adList.add(new ArrayList<Integer>());
		for(int i = 0; i < dfa.getDfa().size(); i++){
			for(char c : input){
				adList.get(i).add(dfa.getDfa().indexOf(dfa.getDfa().get(i).getAllTransitions(c).get(0)));
			}
		}
		
		List<ArrayList<Integer>> reverse = new ArrayList<ArrayList<Integer>>();//reversed adjacency list to traceback
		for(int i = 0; i < dfa.getDfa().size(); i++) reverse.add(new ArrayList<Integer>());
		for(int i = 0; i < adList.size(); i++){
			for(int j = 0; j < adList.get(i).size(); j++){
				if(!reverse.get(adList.get(i).get(j)).contains(i)) reverse.get(adList.get(i).get(j)).add(i);
			}
		}
		return regex;
	}
	
	// Priority of operands
	private static boolean Priority (char first, Character second) {
		if(first == second) {	return true;	}
		if(first == '*') 	{	return false;	}
		if(second == '*')  	{	return true;	}
		if(first == '.') 	{	return false;	}
		if(second == '.') 	{	return true;	}
		if(first == '|') 	{	return false;	} 
		else 				{	return true;	}
	}

	// Do the desired operation based on the top of stackNfa
	private static void doOperation () {
		if (RegularExpression.operator.size() > 0) {
			char charAt = operator.pop();

			switch (charAt) {
				case ('|'):
					union ();
					break;
	
				case ('.'):
					concatenation ();
					break;
	
				case ('*'):
					star ();
					break;
	
				default :
					System.out.println("Unkown Symbol !");
					//System.exit(1);
					throw new RuntimeException();			
			}
		}
	}
	
	/**
	 * Rewrites a regex as to remove "+" and "?"
	 * @param regex the regex to be translated
	 * @return an equivalent regex without "+" and "?"
	 */
	public static String reWrite(String regex) {
		if(!regex.contains("?") && !regex.contains("+")) return regex;
		String[] exceptions = { "(?", "|?", ".?", "(+", "|+", ".+" };
		for (String s : exceptions) {
			if (regex.contains(s))
				throw new RuntimeException();
		}
		String test = "";
		for (int i = 0; i < regex.length(); i++) {
			if (regex.charAt(i) != '?' && regex.charAt(i) != '+')
				test += regex.charAt(i);
		}
		RegularExpression.generateNFA(test);

		String subregex = "";
		int parenCount = 0;
		int length = 0;
		String ret = regex;
		char current = ' ';
		for (int i = 1; i < ret.length(); i++) {
			if (ret.charAt(i) == '?' || ret.charAt(i) == '+') {
				subregex = "";
				parenCount = 0;
				length = 0;
				if (ret.charAt(i - 1) == '*') {
					subregex = "*";
					length++;
				}
				do {
					current = ret.charAt(i - ++length);
					if (current == ')')
						parenCount++;
					if (current == '(')
						parenCount--;
					subregex = current + subregex;
				} while (parenCount != 0);

				if (ret.charAt(i) == '?') {
					subregex = "((" + subregex + ")|~)";
				} else {
					subregex = subregex + subregex + "*";
				}

				ret = ret.substring(0, i - length) + subregex + ret.substring(i + 1);
			}
		}
		return ret;
	}
		
	// Do the star operation
	private static void star() {
		// Retrieve top NFA from Stack
		NFA nfa = stackNfa.pop();
		
		// Create states for star operation
		State start = new State (stateID++);
		State end	= new State (stateID++);
		
		// Add transition to start and end state
		start.addTransition(end, '~');
		start.addTransition(nfa.getNfa().getFirst(), '~');
		
		nfa.getNfa().getLast().addTransition(end, '~');
		nfa.getNfa().getLast().addTransition(nfa.getNfa().getFirst(), '~');
		
		nfa.getNfa().addFirst(start);
		nfa.getNfa().addLast(end);
		
		// Put nfa back in the stackNfa
		stackNfa.push(nfa);
	}

	// Do the concatenation operation
	private static void concatenation() {
		// retrieve nfa 1 and 2 from stackNfa
		NFA nfa2 = stackNfa.pop();
		NFA nfa1 = stackNfa.pop();
		
		// Add transition to the end of nfa 1 to the begin of nfa 2
		// the transition uses empty string
		nfa1.getNfa().getLast().addTransition(nfa2.getNfa().getFirst(), '~');
		
		// Add all states in nfa2 to the end of nfa1
		for (State s : nfa2.getNfa()) {	nfa1.getNfa().addLast(s); }

		// Put nfa back to stackNfa
		stackNfa.push (nfa1);
	}
	
	// Makes union of sub NFA 1 with sub NFA 2
	private static void union() {
		// Load two NFA in stack into variables
		NFA nfa2 = stackNfa.pop();
		NFA nfa1 = stackNfa.pop();
		
		// Create states for union operation
		State start = new State (stateID++);
		State end	= new State (stateID++);

		// Set transition to the begin of each subNFA with empty string
		start.addTransition(nfa1.getNfa().getFirst(), '~');
		start.addTransition(nfa2.getNfa().getFirst(), '~');

		// Set transition to the end of each subNfa with empty string
		nfa1.getNfa().getLast().addTransition(end, '~');
		nfa2.getNfa().getLast().addTransition(end, '~');

		// Add start to the end of each nfa
		nfa1.getNfa().addFirst(start);
		nfa2.getNfa().addLast(end);
		
		// Add all states in nfa2 to the end of nfa1
		// in order	
		for (State s : nfa2.getNfa()) {
			nfa1.getNfa().addLast(s);
		}
		// Put NFA back to stack
		stackNfa.push(nfa1);		
	}
	
	// Push input symbol into stackNfa
	private static void pushStack(char symbol) {
		State s0 = new State (stateID++);
		State s1 = new State (stateID++);
		
		// add transition from 0 to 1 with the symbol
		s0.addTransition(s1, symbol);
		
		// new temporary NFA
		NFA nfa = new NFA ();
		
		// Add states to NFA
		nfa.getNfa().addLast(s0);
		nfa.getNfa().addLast(s1);		
		
		// Put NFA back to stackNfa
		stackNfa.push(nfa);
	}

	// add "." when is concatenation between to symbols that
	// concatenates to each other
	private static String AddConcat(String regular) {
		String newRegular = new String ("");

		for (int i = 0; i < regular.length() - 1; i++) {
			if ( isInputCharacter(regular.charAt(i))  && isInputCharacter(regular.charAt(i+1)) ) {
				newRegular += regular.charAt(i) + ".";
				
			} else if ( isInputCharacter(regular.charAt(i)) && regular.charAt(i+1) == '(' ) {
				newRegular += regular.charAt(i) + ".";
				
			} else if ( regular.charAt(i) == ')' && isInputCharacter(regular.charAt(i+1)) ) {
				newRegular += regular.charAt(i) + ".";
				
			} else if (regular.charAt(i) == '*'  && isInputCharacter(regular.charAt(i+1)) ) {
				newRegular += regular.charAt(i) + ".";
				
			} else if ( regular.charAt(i) == '*' && regular.charAt(i+1) == '(' ) {
				newRegular += regular.charAt(i) + ".";
				
			} else if ( regular.charAt(i) == ')' && regular.charAt(i+1) == '(') {
				newRegular += regular.charAt(i) + ".";			
				
			} else {
				newRegular += regular.charAt(i);
			}
		}
		newRegular += regular.charAt(regular.length() - 1);
		return newRegular;
	}

	// Return true if is part of the automata Language else is false
	public static boolean isInputCharacter(char charAt) {
		return input.contains(charAt) || charAt == '~';
	}

	
	// Using the NFA, generates the DFA
	public static DFA generateDFA(NFA nfa) {
		// Creating the DFA
		DFA dfa = new DFA ();

		// Clearing all the states ID for the DFA
		stateID = 0;

		// Create an arrayList of unprocessed States
		LinkedList <State> unprocessed = new LinkedList<State> ();
		
		// Create sets
		set1 = new HashSet <State> ();
		set2 = new HashSet <State> ();

		// Add first state to the set1
		set1.add(nfa.getNfa().getFirst());

		// Run the first remove Epsilon the get states that
		// run with epsilon
		removeEpsilonTransition ();

		// Create the start state of DFA and add to the stack
		State dfaStart = new State (set2, stateID++);
		
		dfa.getDfa().addLast(dfaStart);
		unprocessed.addLast(dfaStart);
		
		// While there is elements in the stack
		while (!unprocessed.isEmpty()) {
			// Process and remove last state in stack
			State state = unprocessed.removeLast();

			// Check if input symbol
			for (Character symbol : input) {
				set1 = new HashSet<State> ();
				set2 = new HashSet<State> ();

				moveStates (symbol, state.getStates(), set1);
				removeEpsilonTransition ();

				boolean found = false;
				State st = null;

				for (int i = 0 ; i < dfa.getDfa().size(); i++) {
					st = dfa.getDfa().get(i);

					if (st.getStates().equals(set2)) {
						found = true;
						break;
					}
				}

				// Not in the DFA set, add it
				if (!found) {
					State p = new State (set2, stateID++);
					unprocessed.addLast(p);
					dfa.getDfa().addLast(p);
					state.addTransition(p, symbol);

				// Already in the DFA set
				} else {
					state.addTransition(st, symbol);
				}
			}			
		}
		// Return the complete DFA
		return dfa;
	}

	// Remove the epsilon transition from states
	private static void removeEpsilonTransition() {
		Stack <State> stack = new Stack <State> ();
		set2 = set1;

		for (State st : set1) { stack.push(st);	}

		while (!stack.isEmpty()) {
			State st = stack.pop();

			ArrayList <State> epsilonStates = st.getAllTransitions ('~');

			for (State p : epsilonStates) {
				// Check p is in the set otherwise Add
				if (!set2.contains(p)) {
					set2.add(p);
					stack.push(p);
				}				
			}
		}		
	}

	// Move states based on input symbol
	private static void moveStates(Character c, Set<State> states,	Set<State> set) {
		ArrayList <State> temp = new ArrayList<State> ();

		for (State st : states) {	temp.add(st);	}
		for (State st : temp) {			
			ArrayList<State> allStates = st.getAllTransitions(c);

			for (State p : allStates) {	set.add(p);	}
		}
	}	
}
// This line make it work