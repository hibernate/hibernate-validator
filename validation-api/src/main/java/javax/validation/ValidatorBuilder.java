package javax.validation;

/**
 * Return a Validator corresponding to the initialized state.
 * 
 * @author Emmanuel Bernard
 */
public interface ValidatorBuilder {
	/**
	 * Defines the message resolver implementation used by the Validator.
	 * If unset, the message resolver of the ValidatorFactory is used.
	 *
	 * @return self following the chaining method pattern
	 */
	ValidatorBuilder messageResolver(MessageResolver messageResolver);

	/**
	 * Defines the traversable resolver implementation used by the Validator.
	 * If unset, the traversable resolver of the ValidatorFactory is used.
	 *
	 * @return self following the chaining method pattern
	 */
	ValidatorBuilder traversableResolver(TraversableResolver traversableResolver);

	/**
	 * return an initialized Validator instance respecting the defined state
	 * Validator instances can be pooled and shared by the implementation
	 */
	Validator getValidator();

}
