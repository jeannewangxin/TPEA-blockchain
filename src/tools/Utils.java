package tools;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.ArrayList;

import model.Letter;
import net.i2p.crypto.eddsa.EdDSAPublicKey;

public class Utils {

	public static String bytesToHex(byte[] hashInBytes) {

        StringBuilder sb = new StringBuilder();
        for (byte b : hashInBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
	
	public static byte[] longToBytes(long x) {
	    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
	    buffer.putLong(x);
	    return buffer.array();
	}
	
	public static byte[] signature2(String lettre,byte[] hash,long p,KeyPair kp) throws NoSuchAlgorithmException, IOException, InvalidKeyException, SignatureException {
		//Faut rajouter une condition comme quoi si le wordpool est vide on passe ici
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		
		outputStream.write(lettre.getBytes());
		outputStream.write(Utils.longToBytes(p));
		outputStream.write(hash);
		
		EdDSAPublicKey public_k = (EdDSAPublicKey) kp.getPublic();
		outputStream.write(public_k.getAbyte());
		byte[] hashf = digest.digest(outputStream.toByteArray());
		
		return ED25519.sign(kp,hashf);
	}
	
	public static byte[] signature2Poli(ArrayList<Letter> w,byte[] hash,KeyPair kp) throws NoSuchAlgorithmException, IOException, InvalidKeyException, SignatureException {
		//Faut rajouter une condition comme quoi si le wordpool est vide on passe ici
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		for(int i = 0; i<w.size(); i++) {
			outputStream.write(w.get(i).getLetter().getBytes());
		}
		outputStream.write(hash);
		
		EdDSAPublicKey public_k = (EdDSAPublicKey) kp.getPublic();
		outputStream.write(public_k.getAbyte());
		byte[] hashf = digest.digest(outputStream.toByteArray());
		
		return ED25519.sign(kp,hashf);
	}
	
	
	public static byte[] intToBigEndian(int numero) {
		ByteBuffer bb = ByteBuffer.allocate(8);
		bb.rewind();
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.putInt((int) numero);
		//System.out.println(bb);
		return bb.array();
		
	}
	
	public static ArrayList<String> makeDictionnary(String fileName) {
		
		String file = "src/dict_dict_100000_1_10.txt";
		try {
			String line = null;
			FileReader fileReader = new FileReader(fileName);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			ArrayList<String> strings = new ArrayList<String>();

			while ((line = bufferedReader.readLine()) != null) {
				strings.add(line);
			}
			return strings;

		} catch (Exception e) {
			return null;
		}
	}
	
	public static String readAnswer(DataInputStream is) {
		byte size_of_msg_bytes[] = new byte[8];
		String msgreadtostr = "";
		try {
			is.read(size_of_msg_bytes);
			ByteBuffer wrapped = ByteBuffer.wrap(size_of_msg_bytes);
			long size_of_msg = wrapped.getLong();
			byte msgread[] = new byte[(int) size_of_msg];
			is.read(msgread);
			msgreadtostr = new String(msgread);
		}catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return msgreadtostr;
	}
}
