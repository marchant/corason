package fr.sophiacom.corason.foundation.authentication;

import java.util.Map;

/**
 * This interface aims to define a set of methods that allows to authenticate users.<p>
 * The goal behind this interface is to facilitate the creation of classes that use the strategy pattern <br>
 * This has been highly inspired by <b>"Practical WebObjects"</b> book written by Chuck Hill and Sacha Mallai. Thanks to them.
 * 
 */
public interface COActionAuthenticates {

    /**
     * Return <code>true</code> if the credential equals a possible combination of login + password.<p>
     * The concrete implementation can use both or only password, encrypted or not.
     *  
     * @param aCredential the credential to compare
     * @param aPassword the password to attempt authentication with 
     * @param aLogin the login that can be used to authenticate
     * 
     * @return <code>true</code> if credential equals the password/login
     */
	public boolean authenticate(String aCredential, String aPassword, String aLogin);
	
    /**
     * Return the encrypted password that can be a combination of the password/login.<p>
     * The concrete implementation determines which algorithm is used to encryption.
     * 
     * @param aPassword the password to attempt authentication with 
     * @param aLogin the login that can be used to authenticate
     *  
     * @return encrypted password
     */
	public String encryptPassword(String aPassword, String aLogin);
	
    /**
     * Return <code>true</code> if the algorithm used to encrypt the password or the authentication method
     * allows to return a password in clear.<p>
     * 
     * @return <code>true</code> if the password can be returned by decodePassword()
     */
	public boolean canRetrievePassword();
	
    /**
     * Returns password in clear.
     * 
     * @param aCredential credential to decode
     * @param aLogin login used to encrypt password (depends on the algorithm)
     * 
     * @return password to be returned to the user
     * @throws UnsupportedOperationException if the concrete implementation doesn't return the password.
     */
	public String decodeCredential(String aCredential, String aLogin);
	
    /**
     * This method allow to give informations that can be used by certain implementations.<p>
     * Look up the keys like USER_INFO_XXX to know what kind of information is needed by the involved classes.
     * 
     * @param userInfo
     * 
     */
	public void setAdditionalInformation(Map<String, Object> userInfo);
	
	/**
     * This method returns a copy of the current object.<p>
     * 
     * @return new COActionAuthenticates object
     * 
     */
	public COActionAuthenticates copy();
}
