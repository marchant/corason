package fr.sophiacom.corason.foundation.appserver;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.foundation.NSArray;

import er.extensions.appserver.ERXDirectAction;
import er.extensions.eof.ERXEC;
import er.extensions.foundation.ERXProperties;

/**
 * DirectAction subclass that inherits from Project Wonder class. Offer useful DA.<p>
 * Should can be subclassed if you nee to return a different content.
 *
 * @see COCoreDirectAction#getResponseForInstanceRunning(boolean)
 */

public class COCoreDirectAction extends ERXDirectAction
{
	public COCoreDirectAction(final WORequest request)
	{
		super(request);
	}

	/**
	 * This DA checks if the current instance is still alive. It fetches a single object just to be sure that there is no
	 * dead lock. It returns a very, very simple content like:<br>
	 * <code>instanceNb YES</code><br>
	 * If an exception is raised, the content is:<br>
	 * <code>instanceNb NOT_BAD</code><br><br>
	 * The entity to fetch is the first one of the first EOModel (almost hazard). A property can be set to change the
	 * behavior.
	 * <code>fr.sophiacom.corason.foundation.appserver.COCoreDirectAction.entityNameForCheckingDeadInstance=MyEntity</code>
	 *
	 * @return a WOActionResults object that generates the content described above
	 * @see getResponseForInstanceRunning method
	 */
	public WOActionResults isInstanceRunningAction()
	{
		boolean status = true;
		String entityName = ERXProperties.stringForKey("fr.sophiacom.corason.foundation.appserver.COCoreDirectAction.entityNameForCheckingDeadInstance");
		if (entityName ==  null || entityName.length() == 0)
		{
			NSArray<EOModel> eoModels = EOModelGroup.defaultGroup().models();
			EOModel anEOModel = eoModels.get(0);
			NSArray<EOEntity> eoEntities = anEOModel.entities();
			EOEntity anEOEntity = eoEntities.get(0);
			entityName = anEOEntity.name();
		}
		if (log.isDebugEnabled())
			log.debug("method: isInstanceRunnningAction: entityName: " + entityName);
		EOFetchSpecification fetchSpecification = new EOFetchSpecification(entityName, null, null);
		fetchSpecification.setFetchLimit(1);
		EOEditingContext ec = ERXEC.newEditingContext();
		ec.lock();
		try
		{
			ec.objectsWithFetchSpecification(fetchSpecification);
		} catch (Exception e)
		{
			log.error("method: isInstanceRunnning", e);
			status = false;
		} finally
		{
			ec.unlock();
			ec.dispose();
		}
		return getResponseForInstanceRunning(status);
	}

	/**
	 * Returns a very simple page based on the status.<p>
	 * This method can be overridden to return something else.
	 *
	 * @param status
	 * @return the content of the web page
	 */
	public WOActionResults getResponseForInstanceRunning(final boolean status)
	{
		final int instanceNumber = this.context().request().applicationNumber();
		return new WOActionResults ()
		{
			@Override
			public WOResponse generateResponse()
			{
				String appName = WOApplication.application().name();
				String head = "<html><head><title>" + appName + " is alive?</title></head>";
				String body = "<body><h1>" + instanceNumber;
				if (status)
					body = body +" YES </h1></body></html>";
				else
					body = body +" NOT_BAD";
				body = body + "</h1></body></html>";
				WOResponse response = new WOResponse();
				response.appendContentString(head + body);
				return response;
			}
		};
	}
}
