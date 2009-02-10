package javax.validation;

/**
 * Provide contextual data and operation when applying a given constraint validator implementation
 *
 * @author Emmanuel Bernard
 */
public interface ConstraintValidatorContext {
	/**
	 * Disable default error message and default ConstraintViolation object generation.
	 * Useful to set a different error message or generate an ConstraintViolation based on
	 * a different property
	 *
	 * @see #addError(String)
	 * @see #addError(String, String)
	 */
	void disableDefaultError();

	/**
	 * @return the current unexpanded default message
	 */
	String getDefaultErrorMessage();

	/**
	 * Add a new error message. This error message will be interpolated.
	 * <p/>
	 * If isValid returns false, a ConstraintViolation object will be built per error message
	 * including the default one unless #disableDefaultError() has been called.
	 * <p/>
	 * Aside from the error message, ConstraintViolation objects generated from such a call
	 * contains the same contextual information (root bean, path and so on)
	 * <p/>
	 * This method can be called multiple time. One ConstraintViolation instance per
	 * call is created.
	 *
	 * @param message new unexpanded error message
	 */
	void addError(String message);

	/**
	 * Add a new error message to a given sub property <code>property</code>.
	 * This error message will be interpolated.
	 * <p/>
	 * If isValid returns false, a ConstraintViolation object will be built
	 * per error message including the default one unless #disableDefaultError()
	 * has been called.
	 * <p/>
	 *
	 * @param message new unexpanded error message
	 * @param property property name the ConstraintViolation is targeting
	 */
	void addError(String message, String property);
}
