package fr.sophiacom.corason.foundation.eocontrol;

import org.apache.log4j.Logger;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOObjectStore;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSNotificationCenter;

import er.extensions.eof.ERXEC;

/**
 * EC subclass that inherits from Project Wonder class.
 * This is the good place to put some interesting patterns like recording some metrics.
 *
 */

@SuppressWarnings("serial")
public class COEditingContext extends ERXEC
{

	public static final String EditingContextWillFetchObjectsNotification = "EditingContextWillFetchObjects";
	public static final String FETCH_SPEC_KEY = "fetchSpecificationKey";

	private static final Logger log = Logger.getLogger(COEditingContext.class);

	/**
	 * Constructor.
	 *
	 */
	public COEditingContext(final EOObjectStore objectStore)
	{
		super(objectStore);
        if (log.isDebugEnabled())
            log.debug("Constructor: new COEditingContext: "+this);
	}

	/**
	 * objectsWithFetchSpecification posts a notification <code>EditingContextWillFetchObjectsNotification</code> before fetching objects.<p>
	 * That gives you the opportunity to change the fetchSpecification on the fly.
	 *
	 * @param fetchSpecification
	 * @param editingContext
	 * @return array of eos
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public NSArray objectsWithFetchSpecification(final EOFetchSpecification fetchSpecification, final EOEditingContext editingContext)
	{
		NSDictionary<String, EOFetchSpecification> userInfo = new NSDictionary<String, EOFetchSpecification>(fetchSpecification, FETCH_SPEC_KEY);
		NSNotificationCenter.defaultCenter().postNotification(EditingContextWillFetchObjectsNotification, this, userInfo);
		return super.objectsWithFetchSpecification(fetchSpecification, editingContext);
	}

	/**
	 * This method is overridden for memory optimization.
	 *
	 * @see http://wiki.objectstyle.org/confluence/display/WO/EOF-Using+EOF-Memory+Management
	 */
	@Override
	public void saveChanges()
	{
		if (log.isDebugEnabled())
			log.debug("saveChanges: "+this);

		super.saveChanges();
		if (undoManager() != null)
			undoManager().removeAllActions();
	}
}
