package fr.sophiacom.corason.foundation.authentication;

import java.util.Map;

import org.apache.log4j.Logger;

import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * This factory instantiates authentication strategy objects. It uses the pattern Flyweight because objects are instantiated
 * once and are reused during the life of the application.<p>
 * Each object must be identified by a unique key. The objects can be reused because they don't store external informations. All
 * informations needed by an authentication object is passed as parameters to the different methods.<br>
 * This class doesn't implement an interface because it's very simple so the only way to change the behavior is to create
 * a inherited class and derive the method <code>strategyForType(AuthenticationStrategyType type, Map<String, Object> userInfo)</code>.
 *
 */
public class COAuthenticationFactory {

    private static final Logger log = Logger.getLogger(COAuthenticationFactory.class);
    protected volatile static COAuthenticationFactory factory = null;
    private final NSMutableDictionary<String, COActionAuthenticates> _strategyObjects = new NSMutableDictionary<String, COActionAuthenticates>();

	public enum AuthenticationStrategyType {
		SIMPLE(new COSimpleAuthentication()),
		DIGESTED(new CODigestedPasswordAuthentication()),
		ENCRYPTED(new COEncryptedPasswordAuthentication()),
		LDAP(new COLDAPAuthentication());

		private final COActionAuthenticates _strategyObject;

		AuthenticationStrategyType(final COActionAuthenticates v) {
			_strategyObject = v;
		}
		public COActionAuthenticates strategyObject() {
			return _strategyObject;
		}
	}

	/**
	 * Returns the instance of COAuthenticationFactory.<br>
	 *
	 * @return the unique instance
	 * @see COAuthenticationFactory
	 */
    public static COAuthenticationFactory getInstance() {
    	if (factory == null) {
    		synchronized (COAuthenticationFactory.class) {
    	        log.debug("COAuthenticationFactory: getInstance: initializer is null.");
    			if (factory == null)
    				factory = new COAuthenticationFactory();
			}    	}
    	return factory;
    }

	/**
	 * Setter for the singleton.<p>
	 * You should use this setter for setting your own factory when your application finished its initialization
	 *
	 * @param initializer the initializer to set
	 */
	public static void setFactory(final COAuthenticationFactory in_factory) {
        log.debug("COAuthenticationFactory: setFactory: in_factory:" + in_factory);
        COAuthenticationFactory.factory = in_factory;
	}

	/**
	 * Returns an authentication strategy.<p>
	 * If no object already exists for the key parameter, this method invokes strategyForType().
	 *
	 * @param key unique identifier
	 * @param type type of strategy
	 * @param userInfo additional informations that can be used after the creation of the authentication object. If the object
	 * exists already, this parameter is ignored.
	 *
	 * @return strategy object
	 */
	public COActionAuthenticates strategy(final String key, final AuthenticationStrategyType type, final NSDictionary<String, Object> userInfo) {
		if (_strategyObjects.objectForKey(key) == null) {
			_strategyObjects.setObjectForKey(strategyForType(type, userInfo), key);
		}
		return _strategyObjects.objectForKey(key);
	}

	/**
	 * Returns a new authentication strategy object.<p>
	 * The default implementation uses the AuthenticationStrategyType enum to know which object should be returned.
	 *
	 * @param type type of strategy
	 * @param userInfo additional informations that can be used after the creation of the authentication object. If the object
	 * exists already, this parameter is ignored.
	 *
	 * @return strategy object
	 */
	protected COActionAuthenticates strategyForType(final AuthenticationStrategyType type, final Map<String, Object> userInfo) {
		COActionAuthenticates v = type.strategyObject().copy();
		v.setAdditionalInformation(userInfo);
		return v;
	}
}
