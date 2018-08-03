package regex;
import java.util.LinkedList;

public class NFA {
	private LinkedList<State> nfa;
	
	public NFA () {
		this.setNfa(new LinkedList<State> ());
		this.getNfa().clear();
	}

	public LinkedList<State> getNfa() {
		return nfa;
	}

	public void setNfa(LinkedList<State> nfa) {
		this.nfa = nfa;
	}
	public String toString(){
		String ret = "";
		for(State st : nfa){
			ret += st.toString1() +"\n";
		}
		return ret;
	}
}
// This line make it work
