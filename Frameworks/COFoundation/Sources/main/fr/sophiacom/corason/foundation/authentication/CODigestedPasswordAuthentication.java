package fr.sophiacom.corason.foundation.authentication;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import com.webobjects.foundation.NSForwardException;

/**
 * This implementation encrypts password and there is no possibility to return original password.<p>
 * The code has been copied from <b>"Practical WebObjects"</b> (chapter 4) written by Chuck Hill and Sacha Mallai.
 * 
 */
public class CODigestedPasswordAuthentication implements COActionAuthenticates {

	public boolean authenticate(String credential, String password, String login) {
        return digestedString(password).equals(credential);
	}

	public boolean canRetrievePassword() {
		return false;
	}
	
	public String decodeCredential(String credential, String login) {
		throw new UnsupportedOperationException();
	}

	public String encryptPassword(String password, String login) {
        return digestedString(password);
	}

	/**
	 * This method is not used here.
	 * 
	 * @see COActionAuthenticates
	 */
	public void setAdditionalInformation(Map<String, Object> userInfo) {
		// nothing to do.
	}
	
    /**
     * Processes <code>aString</code> through <code>messageDigest()</code> and returns the result encoded in UTF-8.
     * 
     * @param aString the text to digest
     * 
     * @return <code>aString</code> processed by <code>messageDigest()</code> and encoded in UTF-8
     */
    protected String digestedString(String aString) {
        String digestedString;
        
        try {
            MessageDigest md = MessageDigest.getInstance("SHA");
            digestedString =  new sun.misc.BASE64Encoder().encode(md.digest(aString.getBytes()));
        }
        catch (NoSuchAlgorithmException e) {
            throw new NSForwardException(e);
        }
        return digestedString;
    }
    
    /**
     * Returns a small description used for log purpose.
     * 
     * @return name of the class
     * 
     */
    public String toString() {
    	return "Authentication strategy:" + this.getClass().getSimpleName();
    }

	/**
	 * This method returns the current object because we don't need to copy it.
	 * 
	 * @see COActionAuthenticates
	 */
	public COActionAuthenticates copy() {
		return this;
	}
}
