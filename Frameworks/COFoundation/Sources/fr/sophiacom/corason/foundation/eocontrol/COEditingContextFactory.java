package fr.sophiacom.corason.foundation.eocontrol;

import org.apache.log4j.Logger;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOObjectStore;

import er.extensions.eof.ERXEC;
import er.extensions.foundation.ERXProperties;

/**
 * EC factory subclass that inherits from Project Wonder class.<p>
 * The purpose is to create COEditingContext. It also provides a static method that returns
 * a non autolock editingContext (necessary when ec is used in a background thread).
 *
 * @see COEditingContext
 *
 */
public class COEditingContextFactory extends ERXEC.DefaultFactory {
	private static Logger log = Logger.getLogger(COEditingContextFactory.class);

	/**
	 * Constructor.
	 *
	 */
	public COEditingContextFactory() {
		super();
        if (log.isDebugEnabled()) {
            log.debug("Constructor: new COEditingContextFactory");
        }
        int timestampLag = ERXProperties.intForKeyWithDefault("fr.sophiacom.corason.foundation.eocontrol.COEditingContextFactory.defaultFetchTimestampLag", 5000);
        EOEditingContext.setDefaultFetchTimestampLag(timestampLag);
	}

	/**
	 * Creation of the EC that Corason needs.
	 *
	 * @param parent
	 * @return new EC
	 * @see http://wiki.objectstyle.org/confluence/display/WONDER/Using+a+custom+EOEditingContext+%28ERXEC%29+Subclass
	 *
	 */
	@Override
	protected EOEditingContext _createEditingContext(final EOObjectStore parent) {
        if (log.isDebugEnabled()) {
            log.debug("_createEditingContext: parent: " + parent);
        }
		return new COEditingContext(parent == null ? EOEditingContext.defaultParentObjectStore() : parent);
	}

	/**
	 * Anonymous ERXEC factory that creates manual locking ec's in an app where safeLocking is on by default
	 */
	private static ERXEC.Factory manualLockingEditingContextFactory = new ERXEC.DefaultFactory() {

		@Override
		protected EOEditingContext _createEditingContext(final EOObjectStore parent)
		{
			return new COEditingContext(parent == null ? EOEditingContext.defaultParentObjectStore() : parent)
			{
				private static final long serialVersionUID = -1933961454177083353L;

				@Override
				public boolean useAutoLock() {return false;}

				@Override
				public boolean coalesceAutoLocks() {return false;}
			};
		}
	};

	/**
	 * @return a regular ERXEC with no auto-locking features
	 */
	public static EOEditingContext newManualLockingEditingContext()
	{
		return manualLockingEditingContextFactory._newEditingContext();
	}

	/**
	 * We do not want autolocking in non-request threads
	 *
	 * @param parent
	 * @return an ERXEC with safeLocking properties turned OFF.
	 */
	public static EOEditingContext newManualLockingEditingContext(final EOObjectStore parent)
	{
		return manualLockingEditingContextFactory._newEditingContext(parent);
	}

}
