/**
 * 
 */
package fr.sophiacom.corason.foundation.authentication;

import java.util.Map;

/**
 * This is a very simple implementation because the password and credential are identical.<p>
 * The code has been copied from <b>"Practical WebObjects"</b> (chapter 4) written by Chuck Hill and Sacha Mallai.
 * 
 */
public class COSimpleAuthentication implements COActionAuthenticates {

	public boolean authenticate(String aCredential, String aPassword, String login) {
		return aCredential.equals(aPassword);
	}

	public boolean canRetrievePassword() {
		return true;
	}

	public String decodeCredential(String password, String login) {
		return password;
	}

	public String encryptPassword(String credential, String login) {
		return credential;
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
