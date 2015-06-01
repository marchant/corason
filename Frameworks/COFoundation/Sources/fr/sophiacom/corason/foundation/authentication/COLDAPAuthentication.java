package fr.sophiacom.corason.foundation.authentication;

import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import com.webobjects.foundation.NSForwardException;

/**
 * This implementation authenticates a password against a LDAP server. So there no encryption here.<p>
 * The code has been copied from <b>"Practical WebObjects"</b> (chapter 4) written by Chuck Hill and Sacha Mallai.
 *
 * The userInfo map is mandatory because it provides LDAP informations (see USER_INFO_LDAP_URL_KEY and
 * USER_INFO_BASE_DN_KEY keys).
 *
 */
public class COLDAPAuthentication implements COActionAuthenticates {
	public final static String USER_INFO_LDAP_URL_KEY = "ldapUrlKey";
	public final static String USER_INFO_BASE_DN_KEY = "baseDNKey";
	private String LDAP_URL; // Example: "ldap://localhost:389";
	private String BASE_DN; // Example: ",dc=practicalwebobjects,dc=apress,dc=com";

	/**
	 * Return <code>true</code> if this user can be authenticated with password.
	 * This is implemented by attempting an LDAP login with the credential.
	 *
	 * @param password the password to attempt to authentication with
	 * @return <code>true</code> if this user can be authenticated with password
	 */
	@Override
	public boolean authenticate(final String credential, final String password, final String login) {
		boolean canAuthenticateWithPassword = false;

		Hashtable<String, String> ldapEnvironment = ldapEnvironment();
		ldapEnvironment.put(Context.SECURITY_PRINCIPAL, "userid=" + login + BASE_DN);
		ldapEnvironment.put(Context.SECURITY_CREDENTIALS, password);

		try {
			DirContext ctx = new InitialDirContext(ldapEnvironment);
			canAuthenticateWithPassword = true;
			ctx.close();
		}
		catch (javax.naming.AuthenticationException authException) {
			// Nothing to do, they fail.
		}
		catch (NamingException e) {
			if (e.getRootCause() instanceof java.net.ConnectException) {
				throw new NSForwardException(e, "Failed to contact LDAP server.");
			}
			else {
				throw new NSForwardException(e);
			}
		}

		return canAuthenticateWithPassword;
	}

	@Override
	public boolean canRetrievePassword() {
		return false;
	}

	@Override
	public String decodeCredential(final String credential, final String login) {
		throw new UnsupportedOperationException();
	}

    /**
     * This method is useless for LDAP authentication. But for security reason and to avoid
     * to store a real password, this method returns empty string.
     *
     * @param password the text to process for authentication
     */
	@Override
	public String encryptPassword(final String password, final String login) {
		return new String();
	}

    /**
     * The userInfo gives informations about the connexion to the LDAP server.
     *
     * @param userInfo informations about the url and base.
     *
     */
	@Override
	public void setAdditionalInformation(final Map<String, Object> userInfo) {
		LDAP_URL = (String) userInfo.get(USER_INFO_LDAP_URL_KEY);
		BASE_DN = (String) userInfo.get(USER_INFO_BASE_DN_KEY);
	}

	/**
	 * Returns <code>Hashtable</code> of LDAP environment settings which are not
	 * user specific: INITIAL_CONTEXT_FACTORY, PROVIDER_URL, and
	 * SECURITY_AUTHENTICATION.
	 *
	 * @return <code>Hasttable</code> of LDAP environment settings which are not user specific
	 */
	protected Hashtable<String, String> ldapEnvironment() {
		Hashtable<String, String> ldapEnvironment = new Hashtable<String, String>();
		ldapEnvironment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		ldapEnvironment.put(Context.PROVIDER_URL, LDAP_URL);

		// This is only appropriate when the communications channel is secure.
		// In practice some form of SASL should be used.
		ldapEnvironment.put(Context.SECURITY_AUTHENTICATION, "simple");

		return ldapEnvironment;
	}

    /**
     * Returns a small description used for log purpose.
     *
     * @return name of the class
     *
     */
    @Override
	public String toString() {
    	return "Authentication strategy:" + this.getClass().getSimpleName() + " /LDAP infos:" + LDAP_URL + " /" + LDAP_URL;
    }

	@Override
	public COActionAuthenticates copy() {
		return new COLDAPAuthentication();
	}
}
