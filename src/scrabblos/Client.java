package scrabblos;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Collections;

import model.Letter;
import model.LetterPool;
import model.Word;
import net.i2p.crypto.eddsa.EdDSAPublicKey;
import tools.ED25519;
import tools.Utils;

public class Client implements Runnable {
	private volatile String pk;
	private volatile KeyPair kp;
	private volatile ArrayList<String> LetterBag = new ArrayList<String>();
	private volatile int current_period = -1;
	private char[] bags = { 'b', 'd', 'u', 'q', 's', 'y', 'o', 'r', 'r', 'p', 'm', 'e', 'p', 'y', 's', 'l', 't', 'h',
			'u', 'i', 'n', 'p', 'w', 't', 'w', 'a', 'e', 's', 'r', 'c', 'y', 'c', 'u', 'j', 'x', 't', 'i', 'o', 'k',
			'k', 'c', 'c', 'l', 'w', 'y', 'c', 'w', 'o', 'y', 'x', 'g', 'c', 'u', 'y', 'g', 's', 's', 'c', 'q', 'q',
			'a', 'x', 'd', 'm', 'j', 'e', 'l', 'f', 'f', 'g', 'k', 'x', 'p', 'm', 'j', 'x', 'a', 'y', 'g', 'p', 'd',
			'g', 'g', 'i', 'j', 'o', 'g', 'w', 'r', 'a', 'd', 'b', 'p', 'm', 'o', 'e', 'p', 'v', 't', 'h', 'a', 'm',
			'v', 'm', 'f', 'f', 'e', 's', 'v', 'r', 'o', 'v', 'h', 'o', 'v', 'q', 'a', 'm', 'c', 'b', 'e', 'q', 'g',
			'd', 'p', 'd', 'x', 'q', 'k', 'f', 'p', 'k', 'f', 'a', 's', 'k', 'c', 'x', 'q', 'h', 'i', 'r', 'w', 's',
			's', 'r', 'r', 'q', 'u', 'g', 's', 'n', 'x', 'f', 't', 'q', 'a', 'v', 'r', 'p', 't', 'n', 'h', 'p', 's',
			'j', 'w', 'p', 'y', 'x', 'b', 't', 'v', 'v', 'g', 'a', 'y', 'q', 'm', 'k', 'm', 'x', 'c', 't', 'u', 'n',
			'g', 'e', 'o', 'w', 'o', 'l', 'l', 'f', 'o', 'd', 'l', 'b', 'p', 'x' };

	/**
	 * Principle thread 
	 * @param 
	 * @return 
	 */	
	@Override
	public void run() {
		try {
			//create key pair
			creatKey();
			//register client
			rigister(pk);
			//initialiser letter bag
			for (char c : bags) {
				LetterBag.add(c + "");
			}
			// round start
			while (getCurrentPeriod() <= MotorA.MAX_ROUND) {
				//every client throw only one letter per period
				if (current_period < getCurrentPeriod()) {
					updateLetterPool();
					current_period++;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Client register with public key
	 * @param 
	 * @return 
	 */		
	private void rigister(String public_key) {
		synchronized (MotorA.getMotorA()) {
			MotorA motor = MotorA.getMotorA();
			motor.registerClient(public_key);
		}
	}
	
	/**
	 * get Period
	 * @param 
	 * @return Period
	 */	
	protected int getPeriod() {
		synchronized (MotorA.getMotorA()) {
			MotorA motor = MotorA.getMotorA();
			LetterPool letterpool = motor.getLetter_pool();
			int periode = 0;
			for (Letter letter : letterpool.getLetters()) {
				if (letter.getPeriod() > periode)
					periode = letter.getPeriod();
			}
			return periode;
		}
	}

	
	/**
	 * Client choose a word that he want put in blockchaine  
	 * @param 
	 * @return hash value (signature of a word )
	 */
	private String getWordSignature() {
		ArrayList<Word> wordPool = readLastWordPool();
		
		if (wordPool.size() > 0) {
			int maxSize = 0;
			int maxSizeIndex = 0;
			for (Word w : wordPool) {
				String str = w.getHash();
				int size = getPreSize(str);
				size += w.getWord().size();
				if (size > maxSize) {
					maxSize = size;
					maxSizeIndex = wordPool.indexOf(w);
				}
			}
			return wordPool.get(maxSizeIndex).getSignature();
		}else {
			MessageDigest digest;
			try {
				digest = MessageDigest.getInstance("SHA-256");
				return Utils.bytesToHex(digest.digest(("").getBytes()));
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return "";
	}

	/**
	 * Get the total size of all precedents of a word  
	 * @param hash value (signature of a word )
	 * @return int
	 */
	private int getPreSize(String hash) {
		ArrayList<Word> wordPool = (ArrayList<Word>)readWordPool().clone();
		MessageDigest digest;
		try {
			for (Word w : wordPool) {
				digest = MessageDigest.getInstance("SHA-256");
				String sighash = w.getSignature();//Utils.bytesToHex(digest.digest((w.getSignature()).getBytes()));
				if (sighash == hash) {
					int size = w.getWord().size();
					if (w.getPeriod() > 0) {
						hash = w.getHash();
						return size + getPreSize(hash);
					}
					if (w.getPeriod() == 0) {
						return size;
					}
				}
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return 0;

	}

	/**
	 * Read the full word pool 
	 * @param 
	 * @return ArrayList<Word>
	 */
	private ArrayList<Word> readWordPool() {
		synchronized (MotorA.getMotorA()) {
			MotorA motor = MotorA.getMotorA();
			return motor.getWord_pool().getWords();
		}
	}
	
	/**
	 * Read the current word pool 
	 * @param 
	 * @return ArrayList<Word>
	 */
	private ArrayList<Word> readCurrentWordPool() {
		synchronized (MotorA.getMotorA()) {
			MotorA motor = MotorA.getMotorA();
			return motor.getCurrentWord_pool();
		}
	}
	
	/**
	 * 
	 * Read the words proposed by politician in the last period
	 * @param 
	 * @return ArrayList<Word>
	 */
	private ArrayList<Word> readLastWordPool() {
		synchronized (MotorA.getMotorA()) {
			MotorA motor = MotorA.getMotorA();
			return motor.getLastWord_pool();
		}
	}

	/**
	 * 
	 * Creat key pair
	 * @param 
	 * @return 
	 */
	private void creatKey() {
		ED25519 ed;
		try {
			ed = new ED25519();
			kp = ed.generateKeys();
			EdDSAPublicKey public_k = (EdDSAPublicKey) kp.getPublic();
			pk = Utils.bytesToHex(public_k.getAbyte());
		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * Client choose a letter randomly then sign with his signature and the hash of last word he choosed
	 * @param Full letter bag and hash value of last word choosed
	 * @return Letter
	 */
	private Letter chooseLetter(ArrayList<String> LetterBag, String hash) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-256");
			Collections.shuffle(LetterBag);
			String letter = LetterBag.get(0);
			String signature = Utils.bytesToHex(Utils.signature2(letter, digest.digest((hash).getBytes()), 0, kp));
			String head = hash;//Utils.bytesToHex(digest.digest((hash).getBytes()));
			return new Letter(letter, getLetterPool().getCurrent_period(), head, pk, signature);
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Update letter pool, client throw letter
	 * Every client choose firstly a letter randomly then sign with his signature 
	 * In the letter, ther is a pointeur who point to last word that client choose 
	 * @param 
	 * @return 
	 */
	
	protected void updateLetterPool() {		
		Letter l = chooseLetter(LetterBag, getWordSignature());
		System.out.println("Client :"+pk+" update letter pool. Letter added : "+l.getLetter());
		synchronized (MotorA.getMotorA()) {
			MotorA motor = MotorA.getMotorA();
			motor.addLetter(l);
			//motor.showLetterPool();
		}
	}
	/**
	 * get full letter pool 
	 * 
	 * @param 
	 * @return LetterPool.
	 */
	protected LetterPool getLetterPool() {
		synchronized (MotorA.getMotorA()) {
			MotorA motor = MotorA.getMotorA();
			return motor.getLetter_pool();
		}

	}
	/**
	 * get current period number
	 * 
	 * @param 
	 * @return current period.
	 */
	protected int getCurrentPeriod() {
		synchronized (MotorA.getMotorA()) {
			MotorA motor = MotorA.getMotorA();
			return motor.getCurrentPeriod();
		}

	}

	

}