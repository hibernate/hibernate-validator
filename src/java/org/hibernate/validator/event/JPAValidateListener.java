//$Id: $
package org.hibernate.validator.event;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.Set;
import java.util.HashSet;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import org.hibernate.validator.ClassValidator;

/**
 * Java Persistence Entity Listener that validates entities on inserts or updates
 * This listener needs ot be placed on each validatable entities
 *
 * <code>
 * @Entity
 * @EntityListeners(JPAValidateListener.class)
 * public class Submarine {
 *     @NotEmpty private String name;
 *     ...
 * }
 * </code>
 *
 * @author Emmanuel Bernard
 */
public class JPAValidateListener {
	//No need for weakReference if the event listener is loaded by the same CL than
	//the entities, but in a shared env this is not the case
	//TODO check that this can be hot redeployable
	private static final Map<Class, WeakReference<ClassValidator>> validators;
	private static final ClassValidator<JPAValidateListener> NO_VALIDATOR;
	static {
		validators = new WeakHashMap<Class, WeakReference<ClassValidator>>();
		NO_VALIDATOR = new ClassValidator<JPAValidateListener>( JPAValidateListener.class);
	}

	//keep hardref at the instance level
	private final Set<ClassValidator> currentValidators = new HashSet<ClassValidator>();

	@PrePersist
	@PreUpdate
	@SuppressWarnings( "unchecked" )
	public void onChange(Object object) {
		if (object == null) return;
		Class entity = object.getClass();
		WeakReference<ClassValidator> weakValidator = validators.get(entity);
		ClassValidator validator = weakValidator != null ? weakValidator.get() : null;
		if ( validator == null ) {
			//initialize
			//TODO reuse the same reflection manager?
			validator = new ClassValidator(entity);
			if ( ! validator.hasValidationRules() ) {
				validator = NO_VALIDATOR;
			}
			validators.put( entity, new WeakReference<ClassValidator>(validator) );
			currentValidators.add( validator );
		}
		if ( validator != NO_VALIDATOR ) {
			validator.assertValid( object );
		}
	}
}
