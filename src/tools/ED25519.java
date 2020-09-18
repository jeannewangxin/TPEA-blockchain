package tools;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;

import net.i2p.crypto.eddsa.EdDSAEngine;
import net.i2p.crypto.eddsa.EdDSASecurityProvider;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveSpec;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable;
import net.i2p.crypto.eddsa.spec.EdDSAParameterSpec;

public class ED25519 {
	final KeyPairGenerator gen;
	
	public ED25519() throws NoSuchAlgorithmException, NoSuchProviderException {
		Security.addProvider(new EdDSASecurityProvider());
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
		gen = KeyPairGenerator.getInstance("EdDSA", "EdDSA");
		sr.setSeed(System.currentTimeMillis());
		gen.initialize(256,sr);
	}
	
	public KeyPair generateKeys() {
		return gen.generateKeyPair();
	}
	public static byte[] sign2 (KeyPair k, byte[] msg) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		Signature sig = Signature.getInstance("ED_25519");
		sig.initSign(k.getPrivate());
		sig.update(msg);
		byte[] s = sig.sign();
		return s;
	}
	
	public static byte[] sign(KeyPair k, byte[] msg) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		EdDSAParameterSpec spec = EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519);
		Signature sig = new EdDSAEngine(MessageDigest.getInstance(spec.getHashAlgorithm()));
		sig.initSign(k.getPrivate());
		sig.update(msg);
		return sig.sign();
	}
	
	
	public static boolean verify(KeyPair k,byte [] msg, byte [] sig) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {
		EdDSAParameterSpec spec = EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519);
		Signature sig2 = new EdDSAEngine(MessageDigest.getInstance(spec.getHashAlgorithm()));
		sig2.initVerify(k.getPublic());
		sig2.update(msg);
		return sig2.verify(sig);
	}
}
