package javax.validation;

import java.lang.annotation.ElementType;

/**
 * Contract determining if a property can be accessed by the Bean Validation provider
 * This contract is called for each property either validated or traversed
 *
 * A traversable resolver implementation must me thread-safe
 *
 * @author Emmanuel Bernard
 */
public interface TraversableResolver {
	/**
	 * Determine if a property can be traversed by Bean Validation.
	 *
	 * @param traversableObject object hosting traversableProperty
	 * @param traversableProperty name of the traqversable property
	 * @param rootBeanType type of the root object passed to the Validator.
	 * @param pathToTraversableObject path from the root object to the traversableObject
	 *        (using the path specification defined by Bean Validator)	 
	 * @param elementType either FIELD or METHOD
	 *
	 * @return true if the property is traversable by Bean Validation
	 */
	boolean isTraversable(Object traversableObject,
						  String traversableProperty,
						  Class<?> rootBeanType,
						  String pathToTraversableObject,
						  ElementType elementType);
}
