package fr.sophiacom.corason.foundation.authentication;

import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;

/**
 * This implementation encrypts password and allows to return original password.<p>
 * The code has been copied from <b>"Practical WebObjects"</b> (chapter 4) written by Chuck Hill and Sacha Mallai.
 *
 */
public class COEncryptedPasswordAuthentication implements COActionAuthenticates {

	/**
	 * To encrypt password, this class needs additional informations like a pass phrase.<p>
	 * The pass phrase is passed through the userInfo map. The key to use is USER_INFO_SECRET_KEY.
	 *
	 */
	public final static String USER_INFO_SECRET_KEY = "secretKey";
	protected PBEParameterSpec obfuscator;
	protected SecretKey secretKey;
	protected boolean hasInitializedCipherSupport = false;
	protected NSDictionary<String, Object> userInfo;

	public boolean authenticate(final String credential, final String password, final String login) {
		return transformString(password, Cipher.ENCRYPT_MODE).equals(credential);
	}

	public boolean canRetrievePassword() {
		return true;
	}

	public String decodeCredential(final String credential, final String login) {
		return transformString(credential, Cipher.DECRYPT_MODE);
	}

	public String encryptPassword(final String password, final String login) {
		return transformString(password, Cipher.ENCRYPT_MODE);
	}

	public void setAdditionalInformation(final Map<String, Object> userInfo) {
		// This object must contain "final" objects. That's the reason why we create our own dictionary.
		this.userInfo = new NSDictionary<String, Object>(userInfo);
	}

	/**
	 * Encrypts or decrypts (determined by mode) inputString.  Results of encryption
	 * are encoded in Base64.  Strings to be decrypted are assumed to have been
	 * encoded in Base64 and are decoded before being decrypted.
	 *
	 * @param inputString the String ot encrypt or decrypt
	 * @param mode either Ciper.ENCRYPT_MODE or Cipher.DECRYPT_MODE
	 *
	 * @return the transformed String
	 */
	public String transformString(final String inputString, final int mode) {
		String transformedString;

		initializeCipherSupport();

		try {
			// Create a Password Based Encryption cipher and initialize with mode, key, and obfuscator
			Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
			pbeCipher.init(mode, secretKey, obfuscator);

			//FIXME: Replace sun.misc.BASE64Encoder by http://www.source-code.biz/snippets/java/2.htm for example
			if (mode == Cipher.ENCRYPT_MODE) {
				byte[] processedBytes = pbeCipher.doFinal(inputString.getBytes());
				transformedString = new sun.misc.BASE64Encoder().encode(processedBytes);
			}
			else {
				byte[] bytesToProcess = new sun.misc.BASE64Decoder().decodeBuffer(inputString);
				transformedString = new String(pbeCipher.doFinal(bytesToProcess));
			}
		}
		catch (Exception e) {
			throw new NSForwardException(e);
		}

		return transformedString;
	}

	/**
	 * initializer blocks.
	 */
	protected void initializeCipherSupport() {
		if ( ! hasInitializedCipherSupport) {
			try {
				// Create parameters for Password Based Encryption.  If someone can
				// get this information they can decrypt anything we encrypt!
				obfuscator = new PBEParameterSpec(
						new byte[] { (byte)0xc8, (byte)0xee, (byte)0xc7, (byte)0x73,
								(byte)0x7e,  (byte)0x99, (byte)0x21, (byte)0x8c},
								100);
				String secretKeyString = (String) userInfo.valueForKey(USER_INFO_SECRET_KEY);
				secretKey = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(new PBEKeySpec(secretKeyString.toCharArray()));
				hasInitializedCipherSupport = true;
			}
			catch (Exception e) {
				throw new NSForwardException(e);
			}
		}
	}

    /**
     * Returns a small description used for log purpose.
     *
     * @return name of the class
     *
     */
    @Override
	public String toString() {
    	return "Authentication strategy:" + this.getClass().getSimpleName();
    }

	public COActionAuthenticates copy() {
		return new COEncryptedPasswordAuthentication();
	}
}
