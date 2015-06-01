package fr.sophiacom.corason.foundation.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class COSecurityUtilities
{
	private static final SecureRandom random = new SecureRandom();


	// This is a delegate used for unit test to test id collisions
	public static interface NextIdDelegate {
		public String nextId();
	}
	private static NextIdDelegate nextIdDelegate = null;
	public static void setDelegate(NextIdDelegate delegate){
		nextIdDelegate = delegate;
	}

	public static String getMD5(final String in_string) {
		String lc_stringMD5 = null;

		if (in_string != null) {
			try {
				MessageDigest md = MessageDigest.getInstance("MD5");
				md.reset();
				md.update(in_string.getBytes());
				byte messageDigest[] = md.digest();
				lc_stringMD5 = convertToHex(messageDigest);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		}
		return lc_stringMD5;
	}

	public static String convertToHex(final byte[] data) {
		StringBuffer buf = new StringBuffer();
		for (byte element : data)
		{
			int halfbyte = (element >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				if ((0 <= halfbyte) && (halfbyte <= 9)) {
					buf.append((char) ('0' + halfbyte));
				} else {
					buf.append((char) ('a' + (halfbyte - 10)));
				}
				halfbyte = element & 0x0F;
			} while(two_halfs++ < 1);
		}
		return buf.toString();
	}

	public static String nextId(final int length) {
		String res = (new BigInteger(length * 5, random)).toString(32);
		int missing = length - res.length();
		for(int i = 0; i < missing; i ++) {
			res = '0' + res;
		}

		return res;
	}

	public static String nextId() {
		if (nextIdDelegate != null){
			return nextIdDelegate.nextId();
		}
		return nextId(16);
	}
}
