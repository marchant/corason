/**
 *
 */
package fr.sophiacom.corason.foundation.eo;

import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSValidation;

import er.corebusinesslogic.ERCStampedEnterpriseObject;
import er.extensions.eof.ERXKey;

/**
 * EO subclass that has some useful methods and that inherits from Project Wonder class.
 *
 */
@SuppressWarnings("serial")
public class COStampedEnterpriseObject extends ERCStampedEnterpriseObject implements COCopyable, COCheckDelete
{
	public interface Keys extends ERCStampedEnterpriseObject.Keys 
	{
		// Adding ERKey for CREATED and LAST_MODIFIED attributes
		public static final ERXKey<NSTimestamp> CREATED_KEY = new ERXKey<NSTimestamp>(CREATED);
		public static final ERXKey<NSTimestamp> LAST_MODIFIED_KEY = new ERXKey<NSTimestamp>(LAST_MODIFIED);
	}

	@Override
	public void willInsert()
	{
		super.willInsert();
		willSave();
	}
	
	/**
	 * Overridden to call willSave
	 * 
     * {@link #willSave()}.
	 */
	@Override
	public void willUpdate()
	{
		super.willUpdate();
		willSave();
    }

	 /**
     * Called as part of the augmented transaction process.
     * This method is called after saveChanges is called on
     * the editing context, but before the object is actually
     * saved in the database. This method is also called
     * before <code>validateForSave</code> is called on this
     * object. This method is called by willInsert and willUpdate so you can
     * write your code that needs to be executed when the object is inserted 
     * and updated.<br>
     * By default, this method does nothing.
     * 
     * {@link #willInsert()}.
     * {@link #willUpdate()}.
     */
	public void willSave()
	{
		//NOP
	}
	
    /**
     * Checks if the value of the attribute has changed but not yet saved to the database.<p>
     * We use now the wonder code but we keep the method name because it's more readable and compatible with
     * isAnyValueChangedForAttributes() which doesn't exist in Wonder.
     *
     * @param attributeName
     * @return <code>true</code> if the value changed or if it's a new object
     */
	public boolean isValueChangedForAttribute(final String attributeName)
	{
		return this.hasKeyChangedFromCommittedSnapshot(attributeName);
	}

    /**
     * Checks if any value of the attributes has changed but not yet saved to the database.
     *
     * @param attributeNames array of keys to check
     * @return <code>true</code> if one value changed or if it's a new object
     */
    public boolean isAnyValueChangedForAttributes(final String... attributeNames) {
        for(String key : attributeNames) {
            if (isValueChangedForAttribute(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the class name without the package if any. Mainly for debugging.
     *
     * @return class name
     */
    public final String shortClassName() 
    {
        final String name = getClass().getName();
        return name.substring( name.lastIndexOf(".") + 1 );
    }

    /**
     * Returns a copy of this object, copying related objects as well.  The actual copy mechanism (by reference, shallow, deep, or custom) for each object 
     * is up to the object being copied. If a copy already exists in <code>copiedObjects</code>, then that is returned instead of making a new copy.  
     * This allows complex graphs of objects, including those with cycles, to be copied without producing duplicate objects.  
     * The graph of copied objects will be the same regardless of where copy is started with two exceptions: it is started on a reference 
     * copied object or a reference copied object is the only path between two disconnected parts of the graph.  In these cases the 
     * reference copied object prevents the copy from following the graph further.
     *
     * @param copiedObjects - the copied objects keyed on the EOGlobalID of the object the copy was made from.
 	 * @param copyContext a context that can be passed to all objects involved by the copy process
     * @return a copy of this object
     */
    public EOEnterpriseObject copy(final NSMutableDictionary<Object, EOEnterpriseObject> copiedObjects, final String copyContext)
    {
       return COCopyable.DefaultImplementation.copy(copiedObjects, this, copyContext);
    }

    /**
     * Convenience cover method for copy(NSMutableDictionary) that creates the dictionary internally.
     * You can use this to start the copying of a graph if you have no need to reference the dictionary.
     *
  	 * @param copyContext a context that can be passed to all objects involved by the copy process
     * @return a copy of this object
     */
    public EOEnterpriseObject copy(final String copyContext)
    {
        return copy(new NSMutableDictionary<Object, EOEnterpriseObject>(), copyContext);
    }

    /**
     * Returns a copy of this object.  Each EOEnterpriseObject can override this this to produce the actual copy by an appropriate mechanism (reference, shallow, deep, or custom).  The default is a deep copy.
     *
     * @param copiedObjects - the copied objects keyed on the EOGlobalID of the object the copy was made from.
 	 * @param copyContext a context that can be passed to all objects involved by the copy process
     * @return a copy of this object
     */
    public EOEnterpriseObject duplicate(final NSMutableDictionary<Object, EOEnterpriseObject> copiedObjects, final String copyContext)
    {
    	return COCopyable.DefaultImplementation.duplicate(copiedObjects, this, copyContext);
    }

	/**
	 * Implementation of COCheckDelete interface.
	 *
	 * @exception NSValidation.ValidationException
	 */
	public void checkDelete() throws NSValidation.ValidationException
	{
		if (ERCStampedEnterpriseObject.log.isDebugEnabled())
			ERCStampedEnterpriseObject.log.debug("method: checkDelete: hasToCheckDelete: " + hasToCheckDelete());
		if (hasToCheckDelete())
		{
			COCheckDelete.DefaultImplementation.checkDelete(this);
		}
	}

	/**
	 * Implementation of COCheckDelete interface.
	 *
	 * @return <code>true</code> if the eo must be checked.
	 */
	public boolean hasToCheckDelete()
	{
		return COCheckDelete.DefaultImplementation.hasToCheckDelete(this);
	}

	/**
	 * Raises a NSValidation.ValidationException if the object can't be deleted.<p>
	 * Calls checkDelete() method.
	 *
	 * @exception NSValidation.ValidationException
	 */
	@Override
	public void validateForDelete() throws NSValidation.ValidationException
	{
		super.validateForDelete();
		checkDelete();
	}
}
