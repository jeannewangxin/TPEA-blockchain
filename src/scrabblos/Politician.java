package scrabblos;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;

import model.Letter;
import model.LetterPool;
import model.Word;
import net.i2p.crypto.eddsa.EdDSAPublicKey;
import tools.ED25519;
import tools.Utils;

public class Politician implements Runnable {

	private volatile String pk; // public key
	private volatile KeyPair kp;// Signature
	private volatile Word wordAct;
	private volatile ArrayList<String> wordDes;
	private volatile String hash = "";
	private volatile ArrayList<String> dictionary;
	private volatile int MILI_PER_SEC = 1000;

	/**
	 * Principle thread 
	 * @param 
	 * @return 
	 */	
	@Override
	public void run() {
		try {
			dictionary = Utils.makeDictionnary("src/dict_dict_100000_1_10.txt");
			createKey();
			rigister(pk);
			while (getCurrentPeriod()<= MotorA.MAX_ROUND) {
				wordAct = new Word(new ArrayList<Letter>(), "", "", "");
				wordDes = new ArrayList<String>();
				long startTime = System.currentTimeMillis();
				//System.out.println("getFlagPassNextRound "+getFlagPassNextRound());
				if (getFlagPassNextRound()) {
					while ((System.currentTimeMillis() - startTime) < MotorA.TIME_UNIT_PER_ROUND * MILI_PER_SEC) {
						Word word = makeWord();
						if (word != null) {
							if (!inWordPool(word)) {
								if (inDictionary(word)) {
									updateWordPool(word);
								}
							}
						}
					}
					updateState(pk, true);
					//showPoliticiansState();
				}
				//showWordPool();
			}
			
			showWordPool();
			judge();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	/**
	 * Call judge process 
	 * @param 
	 * @return 
	 */	
	private void judge(){
		synchronized (MotorA.getMotorA()) {
			MotorA motor = MotorA.getMotorA();
			motor.judge();
		}

	}
	
	/**
	 * get current period number
	 * 
	 * @param 
	 * @return current period.
	 */
	private int getCurrentPeriod() {
		synchronized (MotorA.getMotorA()) {
			MotorA motor = MotorA.getMotorA();
			return motor.getCurrentPeriod();
		}

	}

	/**
	 * Check if all politicians have finished making word
	 * If all politician finish, then return ture, otherwise return false 
	 * 
	 * @param 
	 * @return boolean.
	 */
	private boolean getFlagPassNextRound() {
		synchronized (MotorA.getMotorA()) {
			MotorA motor = MotorA.getMotorA();
			return motor.flagPassNextRound();
		}

	}

	/**
	 * Show politicians' state
	 * Show if they have finished making word
	 * @param 
	 * @return 
	 */
	private void showPoliticiansState() {
		synchronized (MotorA.getMotorA()) {
			MotorA motor = MotorA.getMotorA();
			motor.showPoliticiansState();
		}

	}

	/**
	 * Politician register
	 * 
	 * @param 
	 * @return
	 */
	private void rigister(String public_key) {
		synchronized (MotorA.getMotorA()) {
			MotorA motor = MotorA.getMotorA();
			motor.registerPolitician(public_key);
		}
	}

	/**
	 * Politician update his state when he finish making word
	 * 
	 * @param 
	 * @return
	 */
	private void updateState(String public_key, boolean state) {
		synchronized (MotorA.getMotorA()) {
			MotorA motor = MotorA.getMotorA();
			motor.updatePoliticianState(pk, state);
		}
	}

	/**
	 * Get politician's state
	 * 
	 * @param 
	 * @return boolean
	 */
	private boolean getState(String public_key) {

		synchronized (MotorA.getMotorA()) {
			MotorA motor = MotorA.getMotorA();
			return motor.getPoliticians_states(pk);
		}
	}

	/**
	 * Check if a word is already in word pool
	 * 
	 * @param 
	 * @return boolean
	 */
	private boolean inWordPool(Word word) {
		synchronized (MotorA.getMotorA()) {
			MotorA motor = MotorA.getMotorA();
			ArrayList<Word> wordpool = motor.getWord_pool().getWords();
			for (Word w : wordpool) {
				if (w.getPoliticien() == word.getPoliticien() && w.getWord().size() == word.getWord().size()) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * Check if a word is in dictionary
	 * 
	 * @param 
	 * @return boolean
	 */
	private boolean inDictionary(Word word) {
		boolean flag = true;
		for (String s : wordDes) {
			for (int i = 0; i < word.getWord().size() - 1; i++) {
				if (s.length() <= i)
					continue;
				if (word.getWord().get(i).getLetter().charAt(0) != s.charAt(i)) {
					flag = false;
					break;
				}
			}
			if (flag)
				return true;
		}
		return false;
	}

	/**
	 * Check if a word is valid
	 * That means there is no more than one letter who belong to the same client in a word
	 * 
	 * @param 
	 * @return boolean
	 */
	private boolean isValid(Letter l) {
		for (Letter letterAct : this.wordAct.getWord()) {
			if (letterAct.getAuthor().equals(l.getAuthor())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Create key pair
	 * 
	 * @param 
	 * @return
	 */
	private void createKey() {
		ED25519 ed;
		try {
			ed = new ED25519();
			kp = ed.generateKeys();
			EdDSAPublicKey public_k = (EdDSAPublicKey) kp.getPublic();
			pk = Utils.bytesToHex(public_k.getAbyte());
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Read the full letter pool
	 * 
	 * @param 
	 * @return 
	 */
	private LetterPool readLetterPool() {
		synchronized (MotorA.getMotorA()) {
			MotorA motor = MotorA.getMotorA();
			// motor.showLetterPool();
			return motor.getLetter_pool();
		}
	}

	/**
	 * Read the current period's letter pool
	 * 
	 * @param 
	 * @return 
	 */
	private ArrayList<Letter> readCurrentLetterPool() {
		synchronized (MotorA.getMotorA()) {
			MotorA motor = MotorA.getMotorA();
			return motor.getCurrentLetter_pool();
		}
	}

	/**
	 * Find all words who start with prefix given in dictionary
	 * 
	 * @param prefix ArrayList<Letter> letters
	 * @return ArrayList<String> words
	 */
	private ArrayList<String> findWordDestinaire(ArrayList<Letter> letters) {
		ArrayList<String> dest = new ArrayList<String>();
		for (String s : dictionary) {
			int i = 0;
			if (s.length() <= letters.size()) {
				continue;
			} else {
				for (Letter letter : letters) {
					if (letter.getLetter().charAt(0) == s.charAt(i)) {
						i++;
						continue;
					} else {
						break;
					}
				}
				if (i == letters.size())
					dest.add(s);
			}
		}
		return dest;
	}

	/**
	 * Show target word
	 * 
	 * @param 
	 * @return
	 */
	private void showWordDes() {
		System.out.println("show WORD DES+++++++++++++++++++++++++++++++++++++++++++++++++++++");
		if (wordDes.size() > 0) {
			for (String s : wordDes) {
				System.out.println(s);
			}
		}
		System.out.println("finish show  WORD DES+++++++++++++++++++++++++++++++++++++++++++++++");
	}

	/**
	 * Politician choose the precedent for his new word
	 * 
	 * @param  letters available for making new word
	 * @return signature of the precedent
	 */
	private String choosePrecedent(ArrayList<Letter> ll) {		
		Map<String,Integer> frequency = new HashMap<String,Integer>();
		for(Letter l : ll) {
			if(frequency.containsKey(l.getHash()))
				frequency.put(l.getHash(), (frequency.get(l.getHash())+1));
			else 
				frequency.put(l.getHash(),1);
		}
		
		frequency = U.sortByValue(frequency, false);
		Map.Entry<String,Integer> entry = frequency.entrySet().iterator().next();
		return entry.getKey();
	}
	
	/**
	 * Filter letters list by specific head
	 * 
	 * @param  head
	 * @return letters list
	 */
	private ArrayList<Letter> filterLPByHead(ArrayList<Letter> ll, String head) {		
		ArrayList<Letter> res = new ArrayList<Letter>();
		for(Letter l : ll) {
			if(l.getHash()==head)
				res.add(l);
				
		}
		return res;
	}
	
	/**
	 * make word
	 * 
	 * @param  
	 * @return Word valid
	 */
	private Word makeWord()
			throws IOException, JSONException, InvalidKeyException, NoSuchAlgorithmException, SignatureException {
		LetterPool letterPool = readLetterPool();
		ArrayList<Letter> lp = readCurrentLetterPool();
		if (lp.isEmpty())
			return null;
		String precedent = choosePrecedent(lp);
		lp = filterLPByHead(lp,precedent);
		ArrayList<Letter> word = new ArrayList<Letter>();
		boolean flag = false;
		if (wordAct.getWord().size() == 0) {
			Letter l = lp.get((int) (Math.random() * (lp.size() - 1)));
			word.add(l);
			wordDes = findWordDestinaire(word);
			// showWordDes();
			flag = true;
		} else {
			word = (ArrayList<Letter>) this.wordAct.getWord().clone();
			int i = word.size();
			for (String d : wordDes) {
				for (Letter l : lp) {
					if (d.length() <= i)
						continue;
					if (l.getLetter().charAt(0) == d.charAt(i)) {
						if (isValid(l)) {
							word.add(l);
							wordDes = findWordDestinaire(word);
							flag = true;
							break;
						}
					}
				}
				if (flag)
					break;
			}
		}

		if (flag) {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			String signture = Utils.bytesToHex(Utils.signature2Poli(word, digest.digest((precedent).getBytes()), kp));
			this.wordAct.setWord(word);
			this.wordAct.setPeriod(letterPool.getCurrent_period());
			this.wordAct.setSignature(signture);
			this.wordAct.setHash(precedent);
			this.wordAct.setPoliticien(pk);
			return this.wordAct;
		} else {
			return null;
		}

	}

	/**
	 * Politician throw word in word pool
	 * 
	 * @param  Word w
	 * @return 
	 */
	private void updateWordPool(Word w) {
		System.out.println("update word pool " + Thread.currentThread().getId() + " " + pk);
		synchronized (MotorA.getMotorA()) {
			MotorA motor = MotorA.getMotorA();
			motor.addWord(w);
			 motor.showWordPool();
			// motor.showPoliticiansState();
		}

	}
	/**
	 * Show the full word pool
	 * 
	 * @param 
	 * @return
	 */
	protected void showWordPool() {
		System.out.println("show word pool");
		synchronized (MotorA.getMotorA()) {
			MotorA motor = MotorA.getMotorA();
			motor.showWordPool();
		}

	}

}