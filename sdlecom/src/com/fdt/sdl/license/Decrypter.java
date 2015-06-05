package com.fdt.sdl.license;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.Cipher;

import sun.misc.BASE64Decoder;

public class Decrypter {

	private PublicKey readKeyFromFile(String keyFileName) throws Exception {
		ObjectInputStream oin = null;
		try {
			InputStream in = Decrypter.class.getResourceAsStream(keyFileName);
			oin = new ObjectInputStream(new BufferedInputStream(in));
			BigInteger m = (BigInteger) oin.readObject();
			BigInteger e = (BigInteger) oin.readObject();
			RSAPublicKeySpec keySpec = new RSAPublicKeySpec(m, e);
			KeyFactory fact = KeyFactory.getInstance("RSA");
			PublicKey pubKey = fact.generatePublic(keySpec);
			return pubKey;
		} finally {
			oin.close();
		}
	}

	public String deCrypt(String encodedData) throws Exception {
		String decodedData = null;
		byte[] rawData = null;
		PublicKey publicKey = readKeyFromFile("public.key");
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.DECRYPT_MODE, publicKey);
		rawData = cipher.doFinal(this.decodeBASE64(encodedData));
		decodedData = new String(rawData, "UTF8");
		return decodedData;
	}

	private byte[] decodeBASE64(String text) throws Exception {
		BASE64Decoder b64 = new BASE64Decoder();
		return b64.decodeBuffer(text);
	}

}