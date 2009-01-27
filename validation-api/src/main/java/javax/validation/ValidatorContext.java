package javax.validation;

/**
 * Return a Validator corresponding to the initialized state.
 * 
 * @author Emmanuel Bernard
 */
public interface ValidatorContext {
	/**
	 * Defines the message interpolator implementation used by the Validator.
	 * If unset, the message interpolator of the ValidatorFactory is used.
	 *
	 * @return self following the chaining method pattern
	 */
	ValidatorContext messageInterpolator(MessageInterpolator messageInterpolator);

	/**
	 * Defines the traversable resolver implementation used by the Validator.
	 * If unset, the traversable resolver of the ValidatorFactory is used.
	 *
	 * @return self following the chaining method pattern
	 */
	ValidatorContext traversableResolver(TraversableResolver traversableResolver);

	/**
	 * @return an initialized <code>Validator</code> instance respecting the defined state.
	 * Validator instances can be pooled and shared by the implementation.
	 */
	Validator getValidator();
}
