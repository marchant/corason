package fr.sophiacom.corason.foundation.authentication;

import java.util.Map;

import er.extensions.crypting.BCrypt;

public class COBCryptAuthentication implements COActionAuthenticates {

	@Override
	public boolean authenticate(String credential, String password, String login) {
		return BCrypt.checkpw(password, credential);
	}

	@Override
	public boolean canRetrievePassword() {
		return false;
	}

	@Override
	public String decodeCredential(String credential, String login) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String encryptPassword(String password, String login) {
		return BCrypt.hashpw(password, BCrypt.gensalt());
	}

	/**
	 * This method is not used here.
	 *
	 * @see COActionAuthenticates
	 */
	@Override
	public void setAdditionalInformation(Map<String, Object> userInfo) {
		// nothing to do.
	}

	/**
	 * Returns a small description used for log purpose.
	 *
	 * @return name of the class
	 */
	@Override
	public String toString() {
		return "Authentication strategy:" + this.getClass().getSimpleName();
	}

	/**
	 * This method returns the current object because we don't need to copy it.
	 *
	 * @see COActionAuthenticates
	 */
	@Override
	public COActionAuthenticates copy() {
		return this;
	}
}
