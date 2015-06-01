package fr.sophiacom.corason.foundation.eo;


import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSValidation;
import com.webobjects.foundation.NSValidation.ValidationException;

/**
 * COCheckDelete defines a way for an enterprise object to check if it can be removed or not even if the relationships are not
 * declared in the EOModel. That's often the case for "parameters" entities where inverse to many relationships are not in the EOModel
 * for performance reason.<p>
 *
 * @author Jean Marc Azer, Philippe Rabier
 *
 */
public interface COCheckDelete
{

	/**
	 * Return <code>true</code> if we want that the enterprise object must check if it can be removed or not.
	 * Return <code>false</code> otherwise.<p>
	 * Be careful that the check can take a long time especially when there are several relationships to check.
	 *
	 * @return boolean
	 */
	public boolean hasToCheckDelete();

	/**
	 * Check if the eo can be removed.<p>
	 * If not, a NSValidation.ValidationException is raised.
	 *
	 */
	public void checkDelete() throws NSValidation.ValidationException;

	/**
	 * Default implementation of the interface.<p>
	 * Example:<br>
	 * <code>
	 * public void checkDelete() { <br>
	 * if(hasToCheckDelete()) {<br>
	 * SFCheckDelete.DefaultImplementation.checkDelete(this);<br>
	 * }<br>
	 * }<br>
	 * </code>
	 *
	 */
	public class DefaultImplementation {
		public static final Object CHECK_DELETE = "checkDelete";
		public static final String ERROR_DEPENDANCES = "Unable to delete, there is a connection with the entity ";

		private static final Logger log = Logger.getLogger(COStampedEnterpriseObject.class);
		private static NSDictionary<String, NSArray<EORelationship>> checkDeleteDic;

		/**
		 * Return list of entities.
		 *
		 * @return array of EOntities
		 */
		private static NSArray<EOEntity> entitiesList()
		{
			NSMutableArray<EOEntity> entitiesArray = new NSMutableArray<EOEntity>();
			EOModelGroup modelGroup = EOModelGroup.defaultGroup();

			NSArray<EOModel> models = modelGroup.models();
			for (EOModel model : models)
			{
				entitiesArray.addObjectsFromArray(model.entities());
			}
			if (log.isDebugEnabled())
				log.debug("method: entitiesList: entitiesArray.size" + entitiesArray.size());
			return entitiesArray.immutableClone();
		}

		/**
		 * Create a dictionary where the keys are the entity names and the objects, an array of to one relationships
		 * with no inverse relationships.<p>
		 * This dictionary contains the relationships where the destination entity is the key of the dictionary.
		 *
		 * @return NSDictionary
		 */
		private static NSDictionary<String, NSArray<EORelationship>> buildRelationshipDictionary(){
			NSMutableDictionary<String, NSArray<EORelationship>> dictionary = new NSMutableDictionary<String, NSArray<EORelationship>>();
			NSArray<EOEntity> entities = entitiesList();

			for (EOEntity entity : entities)
			{
				NSArray<EORelationship> relationships = entity.relationships();
				for (EORelationship relationship : relationships)
				{
					if((!relationship.isToMany()) && (relationship.inverseRelationship()==null))
					{
						if(dictionary.objectForKey(relationship.destinationEntity().name())==null)
						{
							dictionary.setObjectForKey(new NSMutableArray<EORelationship>(relationship), relationship.destinationEntity().name());
						} else
						{
							NSArray<EORelationship> array = dictionary.objectForKey(relationship.destinationEntity().name());
							((NSMutableArray<EORelationship>)array).addObject(relationship);
						}
					}
				}

			}
			if (log.isDebugEnabled())
				log.debug("method: buildRelationshipDictionary: dictionary: " + dictionary);
			return dictionary.immutableClone();
		}

		/**
		 * Check if the eo can be removed.<p>
		 * If not, a NSValidation.ValidationException is raised.<br>
		 * The first time, the EOModels are analyzed and the checkDeleteDic dictionary is created.
		 *
		 * @param eo to check
		 * @exception NSValidation.ValidationException
		 */
		public static void checkDelete(final EOEnterpriseObject eo) throws NSValidation.ValidationException
		{
			if (eo == null)
				throw new IllegalStateException("The enterprise object to check can't be null.");

			if (checkDeleteDic == null)
				checkDeleteDic = buildRelationshipDictionary();
			// checkDeleteDic is not null!
			String entityName = eo.entityName();
			NSArray<EORelationship> relationships = checkDeleteDic.objectForKey(entityName);

			for (EORelationship relationship : relationships)
			{
				EOQualifier qual = new EOKeyValueQualifier(relationship.name(),EOKeyValueQualifier.QualifierOperatorEqual,eo);
				EOFetchSpecification fetchSpec = new EOFetchSpecification(relationship.entity().name(),qual,null);
				fetchSpec.setFetchLimit(1);
				fetchSpec.setFetchesRawRows(true);
				fetchSpec.setRawRowKeyPaths(relationship.entity().primaryKeyAttributeNames());
				@SuppressWarnings("rawtypes") // we don't care about the type, we just check array size
				NSArray results = eo.editingContext().objectsWithFetchSpecification(fetchSpec);
				if (log.isDebugEnabled())
					log.debug("method: checkDelete: relationships: " + relationships);

				if((results != null) && (results.count()>0))
					throw new ValidationException(ERROR_DEPENDANCES + relationship.entity().name());
			}
		}

		/**
		 * Return <code>true</code> if the eo can be deleted.<p>
		 * This method analyzes the EOEntity userInfo to find the key "checkDelete".<br>
		 * Return <code>false</code> if the key is missing or if FALSE has been set. Otherwise return <code>true</code>
		 *
		 * @param eo object to check
		 * @return boolean
		 */
		public static boolean hasToCheckDelete(final EOEnterpriseObject eo)
		{
			EOEntity entity = EOModelGroup.defaultGroup().entityForObject(eo);
			String value = (String)entity.userInfo().objectForKey(CHECK_DELETE);
			return value == null ? false : Boolean.valueOf(value).booleanValue();
		}
	}
}

