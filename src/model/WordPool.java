package model;

import java.util.ArrayList;

public class WordPool {
	int current_period;
	int last_period;
	ArrayList<Word> words;
	
	public WordPool(int current_period, int last_period, ArrayList<Word> words) {
		this.current_period = current_period;
		this.last_period = last_period;
		this.words = words;
	}

	public int getCurrent_period() {
		return current_period;
	}

	public void setCurrent_period(int current_period) {
		this.current_period = current_period;
	}

	public int getLast_period() {
		return last_period;
	}

	public void setNext_period(int last_period) {
		this.last_period = last_period;
	}

	public ArrayList<Word> getWords() {
		return words;
	}

	public void setWords(ArrayList<Word> words) {
		this.words = words;
	}
	
	public ArrayList<Word> getWordsByPeriod(int current_period) {
		ArrayList<Word> wordc = new ArrayList<Word>();
		for(Word w:words) {
			if(w.getPeriod() == current_period) {
				wordc.add(w);
			}
		}
		return wordc;
		
	}
	
	public ArrayList<Word> getCurrentPeriodWords() {
		ArrayList<Word> wordc = new ArrayList<Word>();
		for(Word w:words) {
			if(w.getPeriod() == current_period) {
				wordc.add(w);
			}
		}
		return wordc;
		
	}
	
}