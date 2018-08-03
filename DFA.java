package regex;
import java.util.LinkedList;

public class DFA {
	private LinkedList<State> dfa;
	
	public DFA () {
		this.setDfa(new LinkedList<State> ());
		this.getDfa().clear();
	}

	public LinkedList<State> getDfa() {
		return dfa;
	}

	public void setDfa(LinkedList<State> nfa) {
		this.dfa = nfa;
	}
	public String toString(){
		String ret = "";
		for(State st : dfa){
			ret += st.toString1() +"\n";
		}
		return ret;
	}
}
// This line make it work