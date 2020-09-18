package model;

import java.util.ArrayList;

public class LetterPool {
	private int current_period;
	private int next_period;
	private ArrayList<Letter> letters;
	
	public LetterPool(int current_period, int next_period, ArrayList<Letter> letters) {
		this.current_period = current_period;
		this.next_period = next_period;
		this.letters = letters;
	}
	
	public int getCurrent_period() {
		return current_period;
	}

	public void setCurrent_period(int current_period) {
		this.current_period = current_period;
	}

	public int getNext_period() {
		return next_period;
	}

	public void setNext_period(int next_period) {
		this.next_period = next_period;
	}

	public ArrayList<Letter> getLetters() {
		return letters;
	}
	
	public ArrayList<Letter> getCurrentLetters() {
		ArrayList<Letter> res = new ArrayList<Letter>();
		for(Letter l : letters) {
			if(l.getPeriod()==this.current_period) {
				res.add(l);
			}
		}
		return res;
	}

	public void setLetters(ArrayList<Letter> letters) {
		this.letters = letters;
	}
//	class Letter {
//		private char letter;
//		private int period;
//		private String head;
//		private String author;
//		private String signature;
//		
//		public Letter(char letter, int period, String hash, String author, String signature) {
//			super();
//			this.letter = letter;
//			this.period = period;
//			this.head = hash;
//			this.author = author;
//			this.signature = signature;
//		}
//		
//		public char getLetter() {
//			return letter;
//		}
//		public void setLetter(char letter) {
//			this.letter = letter;
//		}
//		public int getPeriod() {
//			return period;
//		}
//		public void setPeriod(int period) {
//			this.period = period;
//		}
//		public String getHash() {
//			return head;
//		}
//		public void setHash(String hash) {
//			this.head = hash;
//		}
//		public String getAuthor() {
//			return author;
//		}
//		public void setAuthor(String author) {
//			this.author = author;
//		}
//		public String getSignature() {
//			return signature;
//		}
//		public void setSignature(String signature) {
//			this.signature = signature;
//		}
//		
//	}
	
	
}
