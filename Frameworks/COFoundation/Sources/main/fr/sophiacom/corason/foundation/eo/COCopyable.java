package fr.sophiacom.corason.foundation.eo;

import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSSet;
import com.webobjects.foundation.NSTimestamp;

import er.corebusinesslogic.ERCStampedEnterpriseObject;
import er.extensions.foundation.ERXProperties;


/**
 * This class provides an interface for flexible copying of EOEnterpriseObjects
 * and a default implementation for doing the actual copying.  This default
 * implementation would be most easily used by creating a sub-class of
 * EOCustomObject or EOGenericRecord and using that as the super class of your
 * EOEnterpriseObjects.
 *
 * This has been highly inspired by <b>"Practical WebObjects"</b> book written by Chuck Hill and Sacha Mallai. Thanks to them.
 */
public interface COCopyable
{
	public static Logger log = Logger.getLogger(COCopyable.class);

	public static final String isAttributeCopyableKey = "isAttributeCopyable";

	public enum COCopyMode
	{
		SHALLOW,
		DEEP;
	}

	/**
	 * Returns a copy of this object, copying related objects as well.  The
	 * actual copy mechanism (by reference, shallow, deep, or custom) for each
	 * object is up to the object being copied.  If a copy already exists in
	 * <code>copiedObjects</code>, then that is returned instead of making a new
	 * copy.  This allows complex graphs of objects, including those with cycles,
	 * to be copied without producing duplicate objects.  The graph of copied
	 * objects will be the same regardless of where copy is started with two
	 * exceptions: if it is started on a reference copied object or if a reference
	 * copied object is the only path between two disconnected parts of the graph.
	 * In these cases the reference copied object prevents the copy from following
	 * the graph further.
	 *
	 * @param copiedObjects the copied objects keyed on the EOGlobalID of the object the copy was made from.
	 * @param copyContext a context that can be passed to all objects involved by the copy process
	 * @return a copy of this object
	 */
	public EOEnterpriseObject copy(NSMutableDictionary<Object, EOEnterpriseObject> copiedObjects, String copyContext);

	/**
	 * Returns a copy of this object. Each EOEnterpriseObject should implement this to produce the actual
	 * copy by an appropriate mechanism (reference, shallow, deep, or custom). The copyContext information can
	 * help you to do some custom code depending on the context.
	 *
	 * @param copiedObjects the copied objects keyed on the EOGlobalID of the object the copy was made from.
	 * @param copyContext a context that can be passed to all objects involved by the copy process
	 * @return a copy of this object
	 */
	public EOEnterpriseObject duplicate(NSMutableDictionary<Object, EOEnterpriseObject> copiedObjects, String copyContext);

	/**
	 * This class provides a default implementation of COCopyable that handles
	 * the most common situations encountered copying EO objects.  This default
	 * implementation would be most easily used by creating a sub-class of
	 * EOCustomObject or EOGenericRecord and using that as the super class of
	 * your EOEnterpriseObjects.For example:<br>
	 * <pre>
	 * public class CopyableGenericRecord extends EOGenericRecord
	 * {
	 *     public CopyableGenericRecord()
	 *     {
	 *         super();
	 *     }
	 *
	 *     public EOEnterpriseObject copy(NSMutableDictionary copiedObjects)
	 *     {
	 *         return COCopyable.DefaultImplementation.copy(copiedObjects, this, null);
	 *     }
	 *
	 *     // Sub-classes can override this to copy via a different mechanism.
	 *     // This can be anything from COCopyable.Utility.referenceCopy(this)
	 *     // to a fully customized copy.
	 *     public EOEnterpriseObject duplicate(NSMutableDictionary copiedObjects, String copyContext)
	 *     {
	 *         return COCopyable.DefaultImplementation.duplicate(copiedObjects, this, copyContext);
	 *     }
	 * }
	 * </pre>
	 * <b>Notes</b><br>
	 * Debugging information can be turned on with the DEBUG level of the log4j logger
	 * <code>fr.sophiacom.corason.foundation.eo.COCopyable</code>.<br><br>
	 * If you implement your own deep copy of relationships you should register the new object before copying its relationships
	 * to so that circular relationships will be copied correctly. For example:<br>
	 * <pre>
	 * EOGlobalID globalID = editingContext().globalIDForObject(this);
	 * copiedObjects.setObjectForKey(copy, globalID);
	 * </pre>
	 */
	public static class DefaultImplementation
	{
		public static COCopyMode copyMode;

		/**
		 * Returns a copy of this object.  The actual copy mechanism (by
		 * reference, shallow, deep, or custom) is up to the object being
		 * copied.  If a copy already exists in <code>copiedObjects</code>,
		 * then that is returned instead of making a new copy.  This allows
		 * complex graphs of objects, including those with cycles, to be
		 * copied without producing duplicate objects.
		 *
		 * @param copiedObjects the copied objects keyed on the EOGlobalID of the object the copy was made from.
		 * @param source the EOEnterpriseObject to copy
		 * @param copyContext a context that can be passed to all objects involved by the copy process
		 * @return a copy of this object
		 */
		public static EOEnterpriseObject copy(final NSMutableDictionary<Object, EOEnterpriseObject> copiedObjects, final EOEnterpriseObject source, final String copyContext)
		{
			EOGlobalID globalID = source.editingContext().globalIDForObject(source);
			if (log.isDebugEnabled())
				log.debug("Copying object: " + globalID);

			EOEnterpriseObject copy = copiedObjects.objectForKey(globalID);

			if (copy == null)
			{
				if (log.isDebugEnabled())
					log.debug("Creating duplicate of object: " + globalID);
				copy = ((COCopyable)source).duplicate(copiedObjects, copyContext);
				copiedObjects.setObjectForKey(copy, globalID);
				// If there is a real copy, the created and lastModified attributes are updated.
				if (copy != source && source instanceof COStampedEnterpriseObject)
				{
					NSTimestamp now = new NSTimestamp();
					copy.takeValueForKey(now, ERCStampedEnterpriseObject.Keys.CREATED);
					copy.takeValueForKey(now, ERCStampedEnterpriseObject.Keys.LAST_MODIFIED);
				}
			}
			return copy;
		}

		/**
		 * Returns a copy of this object. Depending of the property <code>fr.sophiacom.corason.foundation.eo.COCopyable.defaultCopyMode</code>,
		 * the copy is a deep or shallow one (@see COCopyMode enum).
		 *
		 * @param copiedObjects the copied objects keyed on the EOGlobalID of the object the copy was made from.
		 * @param copyContext a context that can be passed to all objects involved by the copy process
		 * @return a copy of this object
		 */
		public static EOEnterpriseObject duplicate(final NSMutableDictionary<Object, EOEnterpriseObject> copiedObjects,
				final EOEnterpriseObject source,
				final String copyContext)
		{
			EOEnterpriseObject duplicate;
			if (copyMode == null)
				synchronized (DefaultImplementation.class)
				{
					String aString = ERXProperties.stringForKeyWithDefault("fr.sophiacom.corason.foundation.eo.COCopyable.defaultCopyMode", COCopyMode.SHALLOW.toString());
					copyMode = COCopyMode.valueOf(aString.toUpperCase());
				}

			if (COCopyMode.SHALLOW.equals(copyMode))
				duplicate = Utility.shallowCopy(copiedObjects, source);
			else
				duplicate = Utility.deepCopy(copiedObjects, source, copyContext);
			return duplicate;
		}
	}

	/**
	 * This class provides utility methods for use implementing COCopyable.  They
	 * handle the most common situations encountered copying EO objects.  The
	 * DefaultImplementation uses them internally.  The implementations of
	 * referenceCopy, shallowCopy(), and deepCopy() should be suitable for most
	 * eo objects to use for their duplicate(NSMutableDictionary) method.
	 * However there are some situations that can not be handled with this
	 * generic code:<br>
	 * <ol>
	 * <li>An attribute or relationship must not be copied (e.g. order numbers).</li>
	 * <li>An attribute or relationship needs special handling (e.g. dateModified
	 * should reflect when the copy was made, not when the original object was
	 * created).</li>
	 * <li>An EO object should not be copied the same way in all situations (e.g.
	 * the relationship from one object should be copied deeply, but from
	 * another object should be a reference copy).</li>
	 * <li>The relationships must be copied in a certain order (e.g. due to side
	 * effects in the methods setting the relationships).</li>
	 * </ol>
	 * In this situations you will need to write a custom implementation of the
	 * duplicate(NSMutableDictionary) method.  This can be as simple as invoking
	 * the default implementation and then cleaning up the result to as complex
	 * as doing it all by hand.  Utility also provides lower-level methods that
	 * you can use when creating a custom duplicate(NSMutableDictionary) method.
	 * These are: newInstance(), copyAttributes(), exposedKeyAttributeNames(),
	 * shallowCopyRelatedObjects(), and deepCopyRelatedObjects(),
	 * cleanRelationships(), and deepCopyRelationship.  Debugging information can
	 * be turned on with the DEBUG level of the log4j logger
	 * <code>fr.sophiacom.corason.foundation.eo.COCopyable</code>.
	 */
	public static class Utility
	{
		protected static NSMutableDictionary<String, NSArray<String>> exposedKeyAttributeDictionary = null;


		/**
		 * Returns a copy of this object by reference.  This is equivalent to
		 * <code>return this;</code> on an EOEnterpriseObject.  This method of
		 * copying is suitable for lookup list items and other objects which
		 * should never be duplicated.
		 *
		 * @param source the EOEnterpriseObject to copy
		 * @return a copy of this object
		 */
		public static EOEnterpriseObject referenceCopy(final EOEnterpriseObject source)
		{
			if (log.isDebugEnabled())
				log.debug("Reference copying: " + globalIDForObject(source));
			return source;
		}

		/**
		 * Returns a shallow copy of this object, the attribute values are
		 * copied and the relationships are copied by reference.  This method
		 * of copying is suitable for things like an order item where
		 * duplication of the product is not wanted and where the order will
		 * not be changed (the copied order item will be on the original order,
		 * not a copy of it).
		 *
		 * @param source the EOEnterpriseObject to copy
		 * @return a copy of this object
		 */
		public static EOEnterpriseObject shallowCopy(final NSMutableDictionary<Object, EOEnterpriseObject> copiedObjects, final EOEnterpriseObject source)
		{
			if (log.isDebugEnabled())
				log.debug("Making shallow copy of: " + globalIDForObject(source));

			EOEnterpriseObject copy = newInstance(source);

			// Register this object right away to handle circular relationships
			copiedObjects.setObjectForKey(copy, globalIDForObject(source));
			copyAttributes(source, copy);
			shallowCopyRelatedObjects(copiedObjects, source, copy);

			return copy;
		}

		/**
		 * Returns a deep copy of this object, the attribute values are copied
		 * and the relationships are copied by calling copy(NSMutableDictionary)
		 * on them.  Thus each related will be copied by its own reference,
		 * shallow, deep, or custom duplicate(NSMutableDictionary) method.
		 * The copy is registered with copiedObjects as soon as it is created
		 * so that circular relationships can be accommodated.  This method of
		 * copying is suitable for duplicating complex graphs of objects.
		 *
		 * @param copiedObjects the copied objects keyed on the EOGlobalID of the object the copy was made from.
		 * @param source the EOEnterpriseObject to copy
		 * @param copyContext a context that can be passed to all objects involved by the copy process
		 * @return a copy of this object
		 */
		public static EOEnterpriseObject deepCopy(final NSMutableDictionary<Object, EOEnterpriseObject> copiedObjects, final EOEnterpriseObject source, final String copyContext)
		{
			if (log.isDebugEnabled())
				log.debug("Making deep copy of: " + globalIDForObject(source));

			EOEnterpriseObject copy = newInstance(source);

			// Register this object right away to handle circular relationships
			copiedObjects.setObjectForKey(copy, globalIDForObject(source));

			copyAttributes(source, copy);
			deepCopyRelatedObjects(copiedObjects, source, copy, copyContext);

			return copy;
		}

		/**
		 * This creates and returns a new instance of the same Entity as source.
		 * When an EO object is created it can already have some relationships
		 * and attributes set.  These can come from to one relationships that
		 * are marked as 'owns destination' and also from the effects of
		 * awakeFromInsertion().  Preset attributes should be overwritten when
		 * all attributes are copied, but the relationships need some special
		 * handling.  See the method cleanRelationships(NSMutableDictionary,
		 * EOEnterpriseObject, EOEnterpriseObject) for details on what is done.
		 * This method can be used when creating custom implementations of the
		 * duplicate() method in COCopyable.
		 *
		 * @param source the EOEnterpriseObject to copy
		 * @return a new instance of the same Entity as source
		 */
		public static EOEnterpriseObject newInstance(final EOEnterpriseObject source)
		{
			if (log.isDebugEnabled())
				log.debug("Making new instance of: " + globalIDForObject(source));

			EOEnterpriseObject newInstance = EOUtilities.createAndInsertInstance(source.editingContext(), source.entityName());
			cleanRelationships(source, newInstance);

			return newInstance;
		}

		/**
		 * When an EO object is created it can already have some relationships
		 * set.  This can come from to one relationships that are marked as 'owns
		 * destination' and also from the effects of awakeFromInsertion() and
		 * need some special handling prior to making a copy.
		 * <ol>
		 * <li>All objects are disconnected from the relationship.</li>
		 * <li>If a disconnected object has a temporary EOGlobalID it is deleted.</li>
		 * </ol>
		 *
		 * @param source the EOEnterpriseObject that copy was created from
		 * @param copy the newly instantiated copy of source that needs to have its relationships cleaned
		 */
		public static void cleanRelationships(final EOEnterpriseObject source, final EOEnterpriseObject copy)
		{
			if (log.isDebugEnabled())
				log.debug("Cleaning related objects in copy of: " + globalIDForObject(source));

			EOEditingContext ec = source.editingContext();
			// To-Many relationships
			Enumeration<String> relationshipEnumerator = copy.toManyRelationshipKeys().objectEnumerator();

			while (relationshipEnumerator.hasMoreElements())
			{
				String relationshipName = relationshipEnumerator.nextElement();
				@SuppressWarnings("unchecked") // relationship are always EOEnterprise objects
				NSArray<EOEnterpriseObject> relatedObjects = (NSArray<EOEnterpriseObject>) copy.valueForKey(relationshipName);

				if (relatedObjects.count() > 0) {

					if (log.isDebugEnabled())
						log.debug("Removing objects in to-many relationship: " + relationshipName);

					Enumeration<EOEnterpriseObject> relatedObjectEnumerator = new NSArray<EOEnterpriseObject>(relatedObjects).objectEnumerator();

					while (relatedObjectEnumerator.hasMoreElements())
					{
						EOEnterpriseObject relatedObject = relatedObjectEnumerator.nextElement();
						copy.removeObjectFromBothSidesOfRelationshipWithKey(relatedObject, relationshipName);
						if (globalIDForObject(relatedObject).isTemporary())
							ec.deleteObject(relatedObject);
					}
				}
			}

			// To-one relationships
			relationshipEnumerator = copy.toOneRelationshipKeys().objectEnumerator();

			while (relationshipEnumerator.hasMoreElements())
			{
				String relationshipName = relationshipEnumerator.nextElement();
				EOEnterpriseObject relatedObject = (EOEnterpriseObject) copy.valueForKey(relationshipName);
				if (relatedObject != null)
				{
					if (log.isDebugEnabled())
						log.debug("Removing object in to-one relationship: " + relationshipName);

					copy.removeObjectFromBothSidesOfRelationshipWithKey(relatedObject, relationshipName);

					if (globalIDForObject(relatedObject).isTemporary())
						source.editingContext().deleteObject(relatedObject);
				}
			}
		}

		/**
		 * This copies the attributes from the source EOEnterpriseObject to the
		 * destination.  Only attributes which are class properties are copied.
		 * However if an attribute is a class property and also used in a
		 * relationship it is assumed to be an exposed primary or forign key and
		 * not copied.  Such attributes are set to null.  See
		 * exposedKeyAttributeNames for details on how this is determined.  It
		 * can be used when creating custom implementations of the duplicate()
		 * method in COCopyable.
		 *
		 * @param source the EOEnterpriseObject to copy attribute values from
		 * @param destination the EOEnterpriseObject to copy attribute values to
		 */
		public static void copyAttributes(final EOEnterpriseObject source, final EOEnterpriseObject destination)
		{
			if (log.isDebugEnabled())
				log.debug("Copying attributes for: " + globalIDForObject(source));

			NSArray<String> exposedKeyAttributeNames = exposedKeyAttributeNames(source);

			EOEntity entity = EOUtilities.entityForObject(source.editingContext(), source);

			Enumeration<String> attributeNameEnumerator = source.attributeKeys().objectEnumerator();

			while (attributeNameEnumerator.hasMoreElements())
			{
				String attributeName = attributeNameEnumerator.nextElement();

				if (exposedKeyAttributeNames.containsObject(attributeName))
				{
					if (log.isDebugEnabled())
						log.debug("Nulling exposed key: " + attributeName);
					destination.takeStoredValueForKey(null, attributeName);
				}
				else
				{
					if (isAttributeCopyable(entity.attributeNamed(attributeName))){
						if (log.isDebugEnabled())
							log.debug("Copying attribute: " + attributeName + ", value: " + source.valueForKey(attributeName));
						destination.takeStoredValueForKey(source.storedValueForKey(attributeName), attributeName);
					}
				}
			}
		}

		/**
		 * This returns true if the attribute can be copied or not. It reads the userInfo of the attribute entity
		 * and looks for the key "isAttributeCopyable".<p>
		 * It's often use for information like order id or invoice id that must not be copied and must be regenrated.
		 * 
		 * @param attribute
		 * @return <code>true> if the attribute can be copied.
		 */
		private static boolean isAttributeCopyable(final EOAttribute attribute){
			if (attribute.userInfo() == null)
				return true;
			if (!attribute.userInfo().containsKey(isAttributeCopyableKey))
				return true;
			String isAttributeCopyable = (String) attribute.userInfo().valueForKey(isAttributeCopyableKey);
			return !isAttributeCopyable.equals("false");
		}

		/**
		 * Returns an array of attribute names from the EOEntity of source that
		 * are used in the primary key, or in forming relationships.  These can
		 * be presumed to be exposed primary or foreign keys and handled
		 * accordingly when copying an object.
		 *
		 * @param source the EOEnterpriseObject to copy attribute values from
		 * @return an array of attribute names from the EOEntity of source that
		 * are used in forming relationships.
		 *
		 **/
		public static NSArray<String> exposedKeyAttributeNames(final EOEnterpriseObject source)
		{
			if (exposedKeyAttributeDictionary == null)
			{
				exposedKeyAttributeDictionary = new NSMutableDictionary<String, NSArray<String>>();
			}

			EOEntity entity = EOUtilities.entityForObject(source.editingContext(), source);
			NSArray<String> exposedKeyAttributeNames = exposedKeyAttributeDictionary.objectForKey(entity.name());

			if (exposedKeyAttributeNames == null)
			{
				if (log.isDebugEnabled())
					log.debug("Checking entity: " + entity.name() + " for exposed keys");
				NSMutableSet<String> keyNames = new NSMutableSet<String>();
				keyNames.addObjectsFromArray(entity.primaryKeyAttributeNames());

				Enumeration<EORelationship> relationshipEnumerator = entity.relationships().objectEnumerator();

				while (relationshipEnumerator.hasMoreElements())
				{
					EORelationship relationship = relationshipEnumerator.nextElement();
					@SuppressWarnings("unchecked")
					NSArray<String> keys = (NSArray<String>)relationship.sourceAttributes().valueForKey("name");
					keyNames.addObjectsFromArray(keys);
				}

				NSSet<String> publicAttributeNames = new NSSet<String>(source.attributeKeys());
				exposedKeyAttributeNames = publicAttributeNames.setByIntersectingSet(keyNames).allObjects();
				if (log.isDebugEnabled())
					log.debug("--> Attributes: " + exposedKeyAttributeNames + " are exposed, including...");
				exposedKeyAttributeDictionary.setObjectForKey(exposedKeyAttributeNames, entity.name());
			}

			return exposedKeyAttributeNames;
		}

		/**
		 * This copies related objects from the source EOEnterpriseObject to the destination by reference.  Only relationships which are class
		 * properties are copied.  It can be used when creating custom implementations of the duplicate() method in COCopyable.
		 * @param copiedObjects
		 *
		 * @param copiedObjects
		 * @param source the EOEnterpriseObject to copy attribute values from
		 * @param destination the EOEnterpriseObject to copy attribute values to
		 */
		public static void shallowCopyRelatedObjects(final NSMutableDictionary<Object, EOEnterpriseObject> copiedObjects,
				final EOEnterpriseObject source,
				final EOEnterpriseObject destination)
		{
			if (log.isDebugEnabled())
				log.debug("Shallow copying relationships for: " + globalIDForObject(source));

			shallowCopyRelatedToManyObjects(copiedObjects, source, destination);
			shallowCopyRelatedToOneObjects(copiedObjects, source, destination);
		}

		/**
		 * This copies toMany related objects from the source EOEnterpriseObject to the destination by reference.  Only relationships which are class
		 * properties are copied.  It can be used when creating custom implementations of the duplicate() method in COCopyable.<p>
		 * The main exception is when the relationship "owns" property is set to true, this method calls shallowCopy on objects belonging to
		 * relationship of the source object because new related objects must be created.
		 *
		 * @param copiedObjects
		 * @param source the EOEnterpriseObject to copy attribute values from
		 * @param destination the EOEnterpriseObject to copy attribute values to
		 */
		@SuppressWarnings("unchecked")
		public static void shallowCopyRelatedToManyObjects(final NSMutableDictionary<Object, EOEnterpriseObject> copiedObjects,
				final EOEnterpriseObject source,
				final EOEnterpriseObject destination)
		{
			Enumeration<String> relationshipEnumerator =  source.toManyRelationshipKeys().objectEnumerator();
			EOClassDescription sourceClassDescription = source.classDescription();

			while (relationshipEnumerator.hasMoreElements())
			{
				String relationshipName = relationshipEnumerator.nextElement();
				boolean relatedObjectOwned = sourceClassDescription.ownsDestinationObjectsForRelationshipKey(relationshipName);

				NSArray<EOEnterpriseObject> originalObjects = (NSArray<EOEnterpriseObject>)source.valueForKey(relationshipName);

				if (log.isDebugEnabled())
					log.debug("Method: shallowCopyRelatedToManyObjects: copying " + originalObjects.count() + " originalObjects for relationship: " + relationshipName);

				for (int i = 0, count = originalObjects.count(); i < count; i++)
				{
					EOEnterpriseObject copyRelated;
					EOEnterpriseObject originalRelated =  originalObjects.objectAtIndex(i);
					if (relatedObjectOwned)
						copyRelated = shallowCopy(copiedObjects, originalRelated);
					else
						copyRelated = referenceCopy(originalRelated);
					destination.addObjectToBothSidesOfRelationshipWithKey(copyRelated, relationshipName);
				}
			}
		}

		/**
		 * This copies toOne related objects from the source EOEnterpriseObject to the destination by reference.  Only relationships which are class
		 * properties are copied.  It can be used when creating custom implementations of the duplicate() method in COCopyable.<p>
		 * The main exception is when the relationship "owns" property is set to true, this method calls shallowCopy because a new related object
		 * must be created.
		 *
		 * @param copiedObjects
		 * @param source the EOEnterpriseObject to copy attribute values from
		 * @param destination the EOEnterpriseObject to copy attribute values to
		 */
		public static void shallowCopyRelatedToOneObjects(final NSMutableDictionary<Object, EOEnterpriseObject> copiedObjects,
				final EOEnterpriseObject source,
				final EOEnterpriseObject destination)
		{
			Enumeration<String> relationshipEnumerator = source.toOneRelationshipKeys().objectEnumerator();
			EOClassDescription sourceClassDescription = source.classDescription();

			while (relationshipEnumerator.hasMoreElements())
			{
				String relationshipName = relationshipEnumerator.nextElement();
				EOEnterpriseObject originalRelated = (EOEnterpriseObject)source.valueForKey(relationshipName);
				if (originalRelated != null)
				{
					boolean relatedObjectOwned = sourceClassDescription.ownsDestinationObjectsForRelationshipKey(relationshipName);
					EOEnterpriseObject copyRelated;
					if (log.isDebugEnabled())
						log.debug("Method: shallowCopyRelatedToOneObject: copying object: " + source + "for relationship: " + relationshipName + " source owns destination: " + relatedObjectOwned);
					if (relatedObjectOwned)
						copyRelated = shallowCopy(copiedObjects, originalRelated);
					else
					{
						copyRelated = copiedObjects.objectForKey(globalIDForObject(originalRelated));
						copyRelated = copyRelated != null ? copyRelated: referenceCopy(originalRelated);
					}
					destination.addObjectToBothSidesOfRelationshipWithKey(copyRelated, relationshipName);
				}
			}
		}

		/**
		 * This copies related objects from the source EOEnterpriseObject to the
		 * destination by calling deepCopyRelationship on them. It can be used
		 * when creating custom implementations of the duplicate() method in
		 * COCopyable. Only relationships which are class properties are copied.
		 *
		 * @param copiedObjects the copied objects keyed on the EOGlobalID of the object the copy was made from
		 * @param source the EOEnterpriseObject to copy attribute values from
		 * @param destination the EOEnterpriseObject to copy attribute values to
		 */
		public static void deepCopyRelatedObjects(final NSMutableDictionary<Object, EOEnterpriseObject> copiedObjects,
				final EOEnterpriseObject source,
				final EOEnterpriseObject destination,
				final String copyContext)
		{
			if (log.isDebugEnabled())
				log.debug("Shallow copying relationships for: " + globalIDForObject(source));
			EOEntity entity = EOUtilities.entityForObject(source.editingContext(),source);

			Enumeration<EORelationship> relationshipEnumerator = entity.relationships().objectEnumerator();
			while (relationshipEnumerator.hasMoreElements())
			{
				EORelationship relationship = relationshipEnumerator.nextElement();
				if (entity.classProperties().containsObject(relationship))
				{
					deepCopyRelationship(copiedObjects, source, destination, relationship, copyContext);
				}
			}
		}

		/**
		 * This copies the object(s) for the named relationship from the source
		 * EOEnterpriseObject to the destination by calling
		 * copy(NSMutableDictionary) on them.  Thus each related object will be
		 * copied by its own reference, shallow, deep, or custom
		 * duplicate(NSMutableDictionary) method.  It can be used when creating
		 * custom implementations of the duplicate() method in COCopyable.
		 *
		 * @param copiedObjects the copied objects keyed on the EOGlobalID of the
		 * object the copy was made from
		 * @param source the EOEnterpriseObject to copy attribute values from
		 * @param destination the EOEnterpriseObject to copy attribute values to
		 * @param relationship the EORelationship to copy
		 * @param copyContext a context that can be passed to all objects involved by the copy process
		 */
		public static void deepCopyRelationship(final NSMutableDictionary<Object, EOEnterpriseObject> copiedObjects,
				final EOEnterpriseObject source,
				final EOEnterpriseObject destination,
				final EORelationship relationship,
				final String copyContext)
		{
			String relationshipName = relationship.name();

			if (relationship.isToMany())
			{
				if (log.isDebugEnabled())
					log.debug("Copying 2-M relationship: " + relationshipName + " from object source: " + globalIDForObject(source));

				@SuppressWarnings("unchecked")
				NSArray<EOEnterpriseObject> originalObjects = (NSArray<EOEnterpriseObject>)source.valueForKey(relationshipName);
				@SuppressWarnings("unchecked")
				NSArray<EOEnterpriseObject> destinationObjects = (NSArray<EOEnterpriseObject>)destination.valueForKey(relationshipName);

				if (log.isDebugEnabled())
					log.debug("Copying " + originalObjects.count() + " originalObjects for relationship: " + relationshipName);

				for (int i = 0, count = originalObjects.count(); i < count; i++)
				{
					EOEnterpriseObject original = originalObjects.objectAtIndex(i);

					EOEnterpriseObject originalCopy = ((COCopyable)original).copy(copiedObjects, copyContext);

					// This is a tricky part.  Making the copy in the previous
					// copiedObjects line can set the relationship that we are
					// about to set.  We need to check for this so that we do not
					// create duplicated relationships.
					if (!destinationObjects.containsObject(originalCopy))
					{
						destination.addObjectToBothSidesOfRelationshipWithKey(originalCopy, relationshipName);
					}
				}
			}
			else
			{
				EOEnterpriseObject original = (EOEnterpriseObject)source.valueForKey(relationshipName);
				if (log.isDebugEnabled())
					log.debug("Copying 2-1 relationship: " + relationshipName + " from object: " + globalIDForObject(source));

				if (original != null)
				{
					if (log.isDebugEnabled())
						log.debug("Copying object for relationship: " + relationshipName);
					destination.addObjectToBothSidesOfRelationshipWithKey(((COCopyable)original).copy(copiedObjects, copyContext), relationshipName);
				}
			}
		}

		/**
		 * Convenience method to get EOGlobalID from an EOEnterpriseObject.
		 *
		 * @param the EOEnterpriseObject to return the EOGlobalID for
		 * @return the EOGlobalID of the eo parameter
		 */
		public static EOGlobalID globalIDForObject(final EOEnterpriseObject eo)
		{
			return eo.editingContext().globalIDForObject(eo);
		}
	}
}
