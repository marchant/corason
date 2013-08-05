package fr.sophiacom.corason.foundation.appserver;

import org.apache.log4j.Logger;

import com.webobjects.eocontrol.EOEventCenter;

import er.extensions.appserver.ERXApplication;

/**
 * Application subclass that inherits from Project Wonder class. 
 * This is the good place to put some interesting patterns like registering instances in the database, records some
 * metrics or be shutdown the instance, driven by the database.
 * 
 */

public class COCoreApplication extends ERXApplication 
{
	protected static final Logger log = Logger.getLogger(COCoreApplication.class);

	public COCoreApplication()
	{
    	super();
    	if (log.isDebugEnabled())
    		log.debug("constructor: setting the EOEventLoggingPassword.");
		EOEventCenter.setPassword(System.getProperty("EOEventLoggingPassword"));
	}
	
    /**
     * Just log that the instance has finished its initialization.  
     *
     */
    @Override
	public void didFinishLaunching() 
    {
        log.info("method: didFinishLaunching: ENTER");
        super.didFinishLaunching();
        log.info("method: didFinishLaunching: DONE");
    }
}
