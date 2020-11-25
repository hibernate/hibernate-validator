/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.logging;

import static org.jboss.logging.Logger.Level.DEBUG;
import static org.jboss.logging.Logger.Level.ERROR;
import static org.jboss.logging.Logger.Level.INFO;
import static org.jboss.logging.Logger.Level.WARN;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Executable;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.ServiceConfigurationError;
import java.util.Set;
import java.util.regex.PatternSyntaxException;

import jakarta.validation.ClockProvider;
import jakarta.validation.ConstraintDeclarationException;
import jakarta.validation.ConstraintDefinitionException;
import jakarta.validation.ConstraintTarget;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.ElementKind;
import jakarta.validation.GroupDefinitionException;
import jakarta.validation.MessageInterpolator;
import jakarta.validation.ParameterNameProvider;
import jakarta.validation.Path;
import jakarta.validation.TraversableResolver;
import jakarta.validation.UnexpectedTypeException;
import jakarta.validation.ValidationException;
import jakarta.validation.spi.ValidationProvider;
import jakarta.validation.valueextraction.ValueExtractor;
import jakarta.validation.valueextraction.ValueExtractorDeclarationException;
import jakarta.validation.valueextraction.ValueExtractorDefinitionException;
import javax.xml.stream.XMLStreamException;

import org.hibernate.validator.internal.engine.messageinterpolation.parser.MessageDescriptorFormatException;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl.ConstraintType;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.properties.Callable;
import org.hibernate.validator.internal.properties.Constrainable;
import org.hibernate.validator.internal.properties.javabean.JavaBeanConstructor;
import org.hibernate.validator.internal.properties.javabean.JavaBeanMethod;
import org.hibernate.validator.internal.util.logging.formatter.ArrayOfClassesObjectFormatter;
import org.hibernate.validator.internal.util.logging.formatter.ClassObjectFormatter;
import org.hibernate.validator.internal.util.logging.formatter.CollectionOfClassesObjectFormatter;
import org.hibernate.validator.internal.util.logging.formatter.CollectionOfObjectsToStringFormatter;
import org.hibernate.validator.internal.util.logging.formatter.DurationFormatter;
import org.hibernate.validator.internal.util.logging.formatter.ExecutableFormatter;
import org.hibernate.validator.internal.util.logging.formatter.ObjectArrayFormatter;
import org.hibernate.validator.internal.util.logging.formatter.TypeFormatter;
import org.hibernate.validator.internal.xml.mapping.ContainerElementTypePath;
import org.hibernate.validator.messageinterpolation.ExpressionLanguageFeatureLevel;
import org.hibernate.validator.spi.messageinterpolation.LocaleResolver;
import org.hibernate.validator.spi.nodenameprovider.PropertyNodeNameProvider;
import org.hibernate.validator.spi.properties.GetterPropertySelectionStrategy;
import org.hibernate.validator.spi.scripting.ScriptEvaluationException;
import org.hibernate.validator.spi.scripting.ScriptEvaluatorFactory;
import org.hibernate.validator.spi.scripting.ScriptEvaluatorNotFoundException;
import org.jboss.logging.BasicLogger;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.FormatWith;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

/**
 * The Hibernate Validator logger interface for JBoss Logging.
 * <p>
 * <b>Note</b>:<br>
 * New log messages must always use a new (incremented) message id. Don't re-use of existing message ids, even
 * if a given log method is not used anymore. Unused messages can be deleted.
 * </p>
 *
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2012 SERLI
 */
@MessageLogger(projectCode = "HV")
public interface Log extends BasicLogger {

	@LogMessage(level = INFO)
	@Message(id = 1, value = "Hibernate Validator %s")
	void version(String version);

	@LogMessage(level = DEBUG)
	@Message(id = 2, value = "Ignoring XML configuration.")
	void ignoringXmlConfiguration();

	@LogMessage(level = DEBUG)
	@Message(id = 3, value = "Using %s as constraint validator factory.")
	void usingConstraintValidatorFactory(@FormatWith(ClassObjectFormatter.class) Class<? extends ConstraintValidatorFactory> constraintValidatorFactoryClass);

	@LogMessage(level = DEBUG)
	@Message(id = 4, value = "Using %s as message interpolator.")
	void usingMessageInterpolator(@FormatWith(ClassObjectFormatter.class) Class<? extends MessageInterpolator> messageInterpolatorClass);

	@LogMessage(level = DEBUG)
	@Message(id = 5, value = "Using %s as traversable resolver.")
	void usingTraversableResolver(@FormatWith(ClassObjectFormatter.class) Class<? extends TraversableResolver> traversableResolverClass);

	@LogMessage(level = DEBUG)
	@Message(id = 6, value = "Using %s as validation provider.")
	void usingValidationProvider(@FormatWith(ClassObjectFormatter.class) Class<? extends ValidationProvider<?>> validationProviderClass);

	@LogMessage(level = DEBUG)
	@Message(id = 7, value = "%s found. Parsing XML based configuration.")
	void parsingXMLFile(String fileName);

	@LogMessage(level = WARN)
	@Message(id = 8, value = "Unable to close input stream.")
	void unableToCloseInputStream();

	@LogMessage(level = WARN)
	@Message(id = 10, value = "Unable to close input stream for %s.")
	void unableToCloseXMLFileInputStream(String fileName);

	@LogMessage(level = WARN)
	@Message(id = 11, value = "Unable to create schema for %1$s: %2$s")
	void unableToCreateSchema(String fileName, String message);

	@Message(id = 12, value = "Unable to create annotation for configured constraint")
	ValidationException getUnableToCreateAnnotationForConfiguredConstraintException(@Cause RuntimeException e);

	@Message(id = 13, value = "The class %1$s does not have a property '%2$s' with access %3$s.")
	ValidationException getUnableToFindPropertyWithAccessException(@FormatWith(ClassObjectFormatter.class) Class<?> beanClass, String property, ElementType elementType);

	@Message(id = 16, value = "%s does not represent a valid BigDecimal format.")
	IllegalArgumentException getInvalidBigDecimalFormatException(String value, @Cause NumberFormatException e);

	@Message(id = 17, value = "The length of the integer part cannot be negative.")
	IllegalArgumentException getInvalidLengthForIntegerPartException();

	@Message(id = 18, value = "The length of the fraction part cannot be negative.")
	IllegalArgumentException getInvalidLengthForFractionPartException();

	@Message(id = 19, value = "The min parameter cannot be negative.")
	IllegalArgumentException getMinCannotBeNegativeException();

	@Message(id = 20, value = "The max parameter cannot be negative.")
	IllegalArgumentException getMaxCannotBeNegativeException();

	@Message(id = 21, value = "The length cannot be negative.")
	IllegalArgumentException getLengthCannotBeNegativeException();

	@Message(id = 22, value = "Invalid regular expression.")
	IllegalArgumentException getInvalidRegularExpressionException(@Cause PatternSyntaxException e);

	@Message(id = 23, value = "Error during execution of script \"%s\" occurred.")
	ConstraintDeclarationException getErrorDuringScriptExecutionException(String script, @Cause Exception e);

	@Message(id = 24, value = "Script \"%s\" returned null, but must return either true or false.")
	ConstraintDeclarationException getScriptMustReturnTrueOrFalseException(String script);

	@Message(id = 25, value = "Script \"%1$s\" returned %2$s (of type %3$s), but must return either true or false.")
	ConstraintDeclarationException getScriptMustReturnTrueOrFalseException(String script, Object executionResult, String type);

	@Message(id = 26, value = "Assertion error: inconsistent ConfigurationImpl construction.")
	ValidationException getInconsistentConfigurationException();

	@Message(id = 27, value = "Unable to find provider: %s.")
	ValidationException getUnableToFindProviderException(@FormatWith(ClassObjectFormatter.class) Class<?> providerClass);

	@Message(id = 28, value = "Unexpected exception during isValid call.")
	ValidationException getExceptionDuringIsValidCallException(@Cause RuntimeException e);

	@Message(id = 29, value = "Constraint factory returned null when trying to create instance of %s.")
	ValidationException getConstraintValidatorFactoryMustNotReturnNullException(@FormatWith(ClassObjectFormatter.class) Class<? extends ConstraintValidator<?, ?>> validatorClass);

	@Message(id = 30,
			value = "No validator could be found for constraint '%s' validating type '%s'. Check configuration for '%s'")
	UnexpectedTypeException getNoValidatorFoundForTypeException(@FormatWith(ClassObjectFormatter.class) Class<? extends Annotation> constraintType,
			String validatedValueType,
			String path);

	@Message(id = 31,
			value = "There are multiple validator classes which could validate the type %1$s. The validator classes are: %2$s.")
	UnexpectedTypeException getMoreThanOneValidatorFoundForTypeException(Type type,
			@FormatWith(CollectionOfObjectsToStringFormatter.class) Collection<Type> validatorClasses);

	@SuppressWarnings("rawtypes")
	@Message(id = 32, value = "Unable to initialize %s.")
	ValidationException getUnableToInitializeConstraintValidatorException(@FormatWith(ClassObjectFormatter.class) Class<? extends ConstraintValidator> validatorClass,
			@Cause RuntimeException e);

	@Message(id = 33, value = "At least one custom message must be created if the default error message gets disabled.")
	ValidationException getAtLeastOneCustomMessageMustBeCreatedException();

	@Message(id = 34, value = "%s is not a valid Java Identifier.")
	IllegalArgumentException getInvalidJavaIdentifierException(String identifier);

	@Message(id = 35, value = "Unable to parse property path %s.")
	IllegalArgumentException getUnableToParsePropertyPathException(String propertyPath);

	@Message(id = 36, value = "Type %s not supported for unwrapping.")
	ValidationException getTypeNotSupportedForUnwrappingException(@FormatWith(ClassObjectFormatter.class) Class<?> type);

	@Message(id = 37,
			value = "Inconsistent fail fast configuration. Fail fast enabled via programmatic API, but explicitly disabled via properties.")
	ValidationException getInconsistentFailFastConfigurationException();

	@Message(id = 38, value = "Invalid property path.")
	IllegalArgumentException getInvalidPropertyPathException();

	@Message(id = 39, value = "Invalid property path. Either there is no property %2$s in entity %1$s or it is not possible to cascade to the property.")
	IllegalArgumentException getInvalidPropertyPathException(@FormatWith(ClassObjectFormatter.class) Class<?> beanClass, String propertyName);

	@Message(id = 40, value = "Property path must provide index or map key.")
	IllegalArgumentException getPropertyPathMustProvideIndexOrMapKeyException();

	@Message(id = 41, value = "Call to TraversableResolver.isReachable() threw an exception.")
	ValidationException getErrorDuringCallOfTraversableResolverIsReachableException(@Cause RuntimeException e);

	@Message(id = 42, value = "Call to TraversableResolver.isCascadable() threw an exception.")
	ValidationException getErrorDuringCallOfTraversableResolverIsCascadableException(@Cause RuntimeException e);

	@Message(id = 43, value = "Unable to expand default group list %1$s into sequence %2$s.")
	GroupDefinitionException getUnableToExpandDefaultGroupListException(@FormatWith(CollectionOfObjectsToStringFormatter.class) List<?> defaultGroupList,
			@FormatWith(CollectionOfObjectsToStringFormatter.class) List<?> groupList);

	@Message(id = 44, value = "At least one group has to be specified.")
	IllegalArgumentException getAtLeastOneGroupHasToBeSpecifiedException();

	@Message(id = 45, value = "A group has to be an interface. %s is not.")
	ValidationException getGroupHasToBeAnInterfaceException(@FormatWith(ClassObjectFormatter.class) Class<?> clazz);

	@Message(id = 46, value = "Sequence definitions are not allowed as composing parts of a sequence.")
	GroupDefinitionException getSequenceDefinitionsNotAllowedException();

	@Message(id = 47, value = "Cyclic dependency in groups definition")
	GroupDefinitionException getCyclicDependencyInGroupsDefinitionException();

	@Message(id = 48, value = "Unable to expand group sequence.")
	GroupDefinitionException getUnableToExpandGroupSequenceException();

	@Message(id = 52,
			value = "Default group sequence and default group sequence provider cannot be defined at the same time.")
	GroupDefinitionException getInvalidDefaultGroupSequenceDefinitionException();

	@Message(id = 53, value = "'Default.class' cannot appear in default group sequence list.")
	GroupDefinitionException getNoDefaultGroupInGroupSequenceException();

	@Message(id = 54, value = "%s must be part of the redefined default group sequence.")
	GroupDefinitionException getBeanClassMustBePartOfRedefinedDefaultGroupSequenceException(@FormatWith(ClassObjectFormatter.class) Class<?> beanClass);

	@Message(id = 55, value = "The default group sequence provider defined for %s has the wrong type")
	GroupDefinitionException getWrongDefaultGroupSequenceProviderTypeException(@FormatWith(ClassObjectFormatter.class) Class<?> beanClass);

	@Message(id = 56, value = "Method or constructor %1$s doesn't have a parameter with index %2$d.")
	IllegalArgumentException getInvalidExecutableParameterIndexException(Callable callable, int index);

	@Message(id = 59, value = "Unable to retrieve annotation parameter value.")
	ValidationException getUnableToRetrieveAnnotationParameterValueException(@Cause Exception e);

	@Message(id = 62, value = "Method or constructor %1$s has %2$s parameters, but the passed list of parameter meta data has a size of %3$s.")
	IllegalArgumentException getInvalidLengthOfParameterMetaDataListException(Callable callable, int nbParameters, int listSize);

	@Message(id = 63, value = "Unable to instantiate %s.")
	ValidationException getUnableToInstantiateException(@FormatWith(ClassObjectFormatter.class) Class<?> clazz, @Cause Exception e);

	@Message(id = 64, value = "Unable to instantiate %1$s: %2$s.")
	ValidationException getUnableToInstantiateException(String message, @FormatWith(ClassObjectFormatter.class) Class<?> clazz, @Cause Exception e);

	@Message(id = 65, value = "Unable to load class: %s from %s.")
	ValidationException getUnableToLoadClassException(String className, ClassLoader loader, @Cause Exception e);

	@Message(id = 68, value = "Start index cannot be negative: %d.")
	IllegalArgumentException getStartIndexCannotBeNegativeException(int startIndex);

	@Message(id = 69, value = "End index cannot be negative: %d.")
	IllegalArgumentException getEndIndexCannotBeNegativeException(int endIndex);

	@Message(id = 70, value = "Invalid Range: %1$d > %2$d.")
	IllegalArgumentException getInvalidRangeException(int startIndex, int endIndex);

	@Message(id = 71, value = "A explicitly specified check digit must lie outside the interval: [%1$d, %2$d].")
	IllegalArgumentException getInvalidCheckDigitException(int startIndex, int endIndex);

	@Message(id = 72, value = "'%c' is not a digit.")
	NumberFormatException getCharacterIsNotADigitException(char c);

	@Message(id = 73, value = "Parameters starting with 'valid' are not allowed in a constraint.")
	ConstraintDefinitionException getConstraintParametersCannotStartWithValidException();

	@Message(id = 74, value = "%2$s contains Constraint annotation, but does not contain a %1$s parameter.")
	ConstraintDefinitionException getConstraintWithoutMandatoryParameterException(String parameterName, String constraintName);

	@Message(id = 75,
			value = "%s contains Constraint annotation, but the payload parameter default value is not the empty array.")
	ConstraintDefinitionException getWrongDefaultValueForPayloadParameterException(String constraintName);

	@Message(id = 76, value = "%s contains Constraint annotation, but the payload parameter is of wrong type.")
	ConstraintDefinitionException getWrongTypeForPayloadParameterException(String constraintName, @Cause ClassCastException e);

	@Message(id = 77,
			value = "%s contains Constraint annotation, but the groups parameter default value is not the empty array.")
	ConstraintDefinitionException getWrongDefaultValueForGroupsParameterException(String constraintName);

	@Message(id = 78, value = "%s contains Constraint annotation, but the groups parameter is of wrong type.")
	ConstraintDefinitionException getWrongTypeForGroupsParameterException(String constraintName, @Cause ClassCastException e);

	@Message(id = 79,
			value = "%s contains Constraint annotation, but the message parameter is not of type java.lang.String.")
	ConstraintDefinitionException getWrongTypeForMessageParameterException(String constraintName);

	@Message(id = 80, value = "Overridden constraint does not define an attribute with name %s.")
	ConstraintDefinitionException getOverriddenConstraintAttributeNotFoundException(String attributeName);

	@Message(id = 81,
			value = "The overriding type of a composite constraint must be identical to the overridden one. Expected %1$s found %2$s.")
	ConstraintDefinitionException getWrongAttributeTypeForOverriddenConstraintException(@FormatWith(ClassObjectFormatter.class) Class<?> expectedReturnType,
			@FormatWith(ClassObjectFormatter.class) Class<?> currentReturnType);

	@Message(id = 82, value = "Wrong type for attribute '%2$s' of annotation %1$s. Expected: %3$s. Actual: %4$s.")
	ValidationException getWrongAnnotationAttributeTypeException(@FormatWith(ClassObjectFormatter.class) Class<? extends Annotation> annotationClass,
			String attributeName, @FormatWith(ClassObjectFormatter.class) Class<?> expectedType,
			@FormatWith(ClassObjectFormatter.class) Class<?> currentType);

	@Message(id = 83, value = "The specified annotation %1$s defines no attribute '%2$s'.")
	ValidationException getUnableToFindAnnotationAttributeException(@FormatWith(ClassObjectFormatter.class) Class<? extends Annotation> annotationClass,
			String parameterName, @Cause NoSuchMethodException e);

	@Message(id = 84, value = "Unable to get attribute '%2$s' from annotation %1$s.")
	ValidationException getUnableToGetAnnotationAttributeException(@FormatWith(ClassObjectFormatter.class) Class<? extends Annotation> annotationClass,
			String parameterName, @Cause Exception e);

	@Message(id = 85, value = "No value provided for attribute '%1$s' of annotation @%2$s.")
	IllegalArgumentException getNoValueProvidedForAnnotationAttributeException(String parameterName,
			@FormatWith(ClassObjectFormatter.class) Class<? extends Annotation> annotation);

	@Message(id = 86, value = "Trying to instantiate annotation %1$s with unknown attribute(s): %2$s.")
	RuntimeException getTryingToInstantiateAnnotationWithUnknownAttributesException(@FormatWith(ClassObjectFormatter.class) Class<? extends Annotation> annotationType,
			Set<String> unknownParameters);

	@Message(id = 87, value = "Property name cannot be null or empty.")
	IllegalArgumentException getPropertyNameCannotBeNullOrEmptyException();

	@Message(id = 88, value = "Element type has to be FIELD or METHOD.")
	IllegalArgumentException getElementTypeHasToBeFieldOrMethodException();

	@Message(id = 89, value = "Member %s is neither a field nor a method.")
	IllegalArgumentException getMemberIsNeitherAFieldNorAMethodException(Member member);

	@Message(id = 90, value = "Unable to access %s.")
	ValidationException getUnableToAccessMemberException(String memberName, @Cause Exception e);

	@Message(id = 91, value = "%s has to be a primitive type.")
	IllegalArgumentException getHasToBeAPrimitiveTypeException(@FormatWith(ClassObjectFormatter.class) Class<?> clazz);

	@Message(id = 93, value = "null is an invalid type for a constraint validator.")
	ValidationException getNullIsAnInvalidTypeForAConstraintValidatorException();

	@Message(id = 94, value = "Missing actual type argument for type parameter: %s.")
	IllegalArgumentException getMissingActualTypeArgumentForTypeParameterException(TypeVariable<?> typeParameter);

	@Message(id = 95, value = "Unable to instantiate constraint factory class %s.")
	ValidationException getUnableToInstantiateConstraintValidatorFactoryClassException(String constraintValidatorFactoryClassName, @Cause ValidationException e);

	@Message(id = 96, value = "Unable to open input stream for mapping file %s.")
	ValidationException getUnableToOpenInputStreamForMappingFileException(String mappingFileName);

	@Message(id = 97, value = "Unable to instantiate message interpolator class %s.")
	ValidationException getUnableToInstantiateMessageInterpolatorClassException(String messageInterpolatorClassName, @Cause Exception e);

	@Message(id = 98, value = "Unable to instantiate traversable resolver class %s.")
	ValidationException getUnableToInstantiateTraversableResolverClassException(String traversableResolverClassName, @Cause Exception e);

	@Message(id = 99, value = "Unable to instantiate validation provider class %s.")
	ValidationException getUnableToInstantiateValidationProviderClassException(String providerClassName, @Cause Exception e);

	@Message(id = 100, value = "Unable to parse %s.")
	ValidationException getUnableToParseValidationXmlFileException(String file, @Cause Exception e);

	@Message(id = 101, value = "%s is not an annotation.")
	ValidationException getIsNotAnAnnotationException(@FormatWith(ClassObjectFormatter.class) Class<?> annotationClass);

	@Message(id = 102, value = "%s is not a constraint validator class.")
	ValidationException getIsNotAConstraintValidatorClassException(@FormatWith(ClassObjectFormatter.class) Class<?> validatorClass);

	@Message(id = 103, value = "%s is configured at least twice in xml.")
	ValidationException getBeanClassHasAlreadyBeenConfiguredInXmlException(@FormatWith(ClassObjectFormatter.class) Class<?> beanClass);

	@Message(id = 104, value = "%1$s is defined twice in mapping xml for bean %2$s.")
	ValidationException getIsDefinedTwiceInMappingXmlForBeanException(String name, @FormatWith(ClassObjectFormatter.class) Class<?> beanClass);

	@Message(id = 105, value = "%1$s does not contain the fieldType %2$s.")
	ValidationException getBeanDoesNotContainTheFieldException(@FormatWith(ClassObjectFormatter.class) Class<?> beanClass, String fieldName);

	@Message(id = 106, value = "%1$s does not contain the property %2$s.")
	ValidationException getBeanDoesNotContainThePropertyException(@FormatWith(ClassObjectFormatter.class) Class<?> beanClass, String getterName);

	@Message(id = 107, value = "Annotation of type %1$s does not contain a parameter %2$s.")
	ValidationException getAnnotationDoesNotContainAParameterException(@FormatWith(ClassObjectFormatter.class) Class<? extends Annotation> annotationClass,
			String parameterName);

	@Message(id = 108, value = "Attempt to specify an array where single value is expected.")
	ValidationException getAttemptToSpecifyAnArrayWhereSingleValueIsExpectedException();

	@Message(id = 109, value = "Unexpected parameter value.")
	ValidationException getUnexpectedParameterValueException();

	ValidationException getUnexpectedParameterValueException(@Cause ClassCastException e);

	@Message(id = 110, value = "Invalid %s format.")
	ValidationException getInvalidNumberFormatException(String formatName, @Cause NumberFormatException e);

	@Message(id = 111, value = "Invalid char value: %s.")
	ValidationException getInvalidCharValueException(String value);

	@Message(id = 112, value = "Invalid return type: %s. Should be a enumeration type.")
	ValidationException getInvalidReturnTypeException(@FormatWith(ClassObjectFormatter.class) Class<?> returnType, @Cause ClassCastException e);

	@Message(id = 113, value = "%s, %s, %s are reserved parameter names.")
	ValidationException getReservedParameterNamesException(String messageParameterName, String groupsParameterName, String payloadParameterName);

	@Message(id = 114, value = "Specified payload class %s does not implement jakarta.validation.Payload")
	ValidationException getWrongPayloadClassException(@FormatWith(ClassObjectFormatter.class) Class<?> payloadClass);

	@Message(id = 115, value = "Error parsing mapping file.")
	ValidationException getErrorParsingMappingFileException(@Cause Exception e);

	@Message(id = 116, value = "%s")
	IllegalArgumentException getIllegalArgumentException(String message);

	@Message(id = 118, value = "Unable to cast %s (with element kind %s) to %s")
	ClassCastException getUnableToNarrowNodeTypeException(@FormatWith(ClassObjectFormatter.class) Class<?> actualDescriptorType, ElementKind kind,
			@FormatWith(ClassObjectFormatter.class) Class<?> expectedDescriptorType);

	@LogMessage(level = DEBUG)
	@Message(id = 119, value = "Using %s as parameter name provider.")
	void usingParameterNameProvider(@FormatWith(ClassObjectFormatter.class) Class<? extends ParameterNameProvider> parameterNameProviderClass);

	@Message(id = 120, value = "Unable to instantiate parameter name provider class %s.")
	ValidationException getUnableToInstantiateParameterNameProviderClassException(String parameterNameProviderClassName, @Cause ValidationException e);

	@Message(id = 121, value = "Unable to parse %s.")
	ValidationException getUnableToDetermineSchemaVersionException(String file, @Cause XMLStreamException e);

	@Message(id = 122, value = "Unsupported schema version for %s: %s.")
	ValidationException getUnsupportedSchemaVersionException(String file, String version);

	@Message(id = 124, value = "Found multiple group conversions for source group %s: %s.")
	ConstraintDeclarationException getMultipleGroupConversionsForSameSourceException(@FormatWith(ClassObjectFormatter.class) Class<?> from,
			@FormatWith(CollectionOfClassesObjectFormatter.class) Collection<Class<?>> tos);

	@Message(id = 125, value = "Found group conversions for non-cascading element at: %s.")
	ConstraintDeclarationException getGroupConversionOnNonCascadingElementException(Object context);

	@Message(id = 127, value = "Found group conversion using a group sequence as source at: %s.")
	ConstraintDeclarationException getGroupConversionForSequenceException(@FormatWith(ClassObjectFormatter.class) Class<?> from);

	@LogMessage(level = WARN)
	@Message(id = 129, value = "EL expression '%s' references an unknown property")
	void unknownPropertyInExpressionLanguage(String expression, @Cause Exception e);

	@LogMessage(level = WARN)
	@Message(id = 130, value = "Error in EL expression '%s'")
	void errorInExpressionLanguage(String expression, @Cause Exception e);

	@Message(id = 131,
			value = "A method return value must not be marked for cascaded validation more than once in a class hierarchy, but the following two methods are marked as such: %s, %s.")
	ConstraintDeclarationException getMethodReturnValueMustNotBeMarkedMoreThanOnceForCascadedValidationException(Callable callable1, Callable callable2);

	@Message(id = 132,
			value = "Void methods must not be constrained or marked for cascaded validation, but method %s is.")
	ConstraintDeclarationException getVoidMethodsMustNotBeConstrainedException(Callable callable);

	@Message(id = 133, value = "%1$s does not contain a constructor with the parameter types %2$s.")
	ValidationException getBeanDoesNotContainConstructorException(@FormatWith(ClassObjectFormatter.class) Class<?> beanClass,
			@FormatWith(ArrayOfClassesObjectFormatter.class) Class<?>[] parameterTypes);

	@Message(id = 134, value = "Unable to load parameter of type '%1$s' in %2$s.")
	ValidationException getInvalidParameterTypeException(String type, @FormatWith(ClassObjectFormatter.class) Class<?> beanClass);

	@Message(id = 135, value = "%1$s does not contain a method with the name '%2$s' and parameter types %3$s.")
	ValidationException getBeanDoesNotContainMethodException(@FormatWith(ClassObjectFormatter.class) Class<?> beanClass, String methodName,
			@FormatWith(ArrayOfClassesObjectFormatter.class) Class<?>[] parameterTypes);

	@Message(id = 136, value = "The specified constraint annotation class %1$s cannot be loaded.")
	ValidationException getUnableToLoadConstraintAnnotationClassException(String constraintAnnotationClassName, @Cause Exception e);

	@Message(id = 137, value = "The method '%1$s' is defined twice in the mapping xml for bean %2$s.")
	ValidationException getMethodIsDefinedTwiceInMappingXmlForBeanException(JavaBeanMethod javaBeanMethod, @FormatWith(ClassObjectFormatter.class) Class<?> beanClass);

	@Message(id = 138, value = "The constructor '%1$s' is defined twice in the mapping xml for bean %2$s.")
	ValidationException getConstructorIsDefinedTwiceInMappingXmlForBeanException(JavaBeanConstructor javaBeanConstructor, @FormatWith(ClassObjectFormatter.class) Class<?> beanClass);

	@Message(id = 139,
			value = "The constraint '%1$s' defines multiple cross parameter validators. Only one is allowed.")
	ConstraintDefinitionException getMultipleCrossParameterValidatorClassesException(
			@FormatWith(ClassObjectFormatter.class) Class<? extends Annotation> constraint);

	@Message(id = 141,
			value = "The constraint %1$s used ConstraintTarget#IMPLICIT where the target cannot be inferred.")
	ConstraintDeclarationException getImplicitConstraintTargetInAmbiguousConfigurationException(
			@FormatWith(ClassObjectFormatter.class) Class<? extends Annotation> constraint);

	@Message(id = 142,
			value = "Cross parameter constraint %1$s is illegally placed on a parameterless method or constructor '%2$s'.")
	ConstraintDeclarationException getCrossParameterConstraintOnMethodWithoutParametersException(
			@FormatWith(ClassObjectFormatter.class) Class<? extends Annotation> constraint, Constrainable executable);

	@Message(id = 143,
			value = "Cross parameter constraint %1$s is illegally placed on class level.")
	ConstraintDeclarationException getCrossParameterConstraintOnClassException(@FormatWith(ClassObjectFormatter.class) Class<? extends Annotation> constraint);

	@Message(id = 144,
			value = "Cross parameter constraint %1$s is illegally placed on field '%2$s'.")
	ConstraintDeclarationException getCrossParameterConstraintOnFieldException(@FormatWith(ClassObjectFormatter.class) Class<? extends Annotation> constraint,
			Constrainable field);

	@Message(id = 146,
			value = "No parameter nodes may be added since path %s doesn't refer to a cross-parameter constraint.")
	IllegalStateException getParameterNodeAddedForNonCrossParameterConstraintException(Path path);

	@Message(id = 147,
			value = "%1$s is configured multiple times (note, <getter> and <method> nodes for the same method are not allowed)")
	ValidationException getConstrainedElementConfiguredMultipleTimesException(String location);

	@LogMessage(level = WARN)
	@Message(id = 148, value = "An exception occurred during evaluation of EL expression '%s'")
	void evaluatingExpressionLanguageExpressionCausedException(String expression, @Cause Exception e);

	@Message(id = 149, value = "An exception occurred during message interpolation")
	ValidationException getExceptionOccurredDuringMessageInterpolationException(@Cause Exception e);

	@Message(id = 150,
			value = "The constraint %1$s defines multiple validators for the type %2$s: %3$s, %4$s. Only one is allowed.")
	UnexpectedTypeException getMultipleValidatorsForSameTypeException(@FormatWith(ClassObjectFormatter.class) Class<? extends Annotation> constraint,
			@FormatWith(TypeFormatter.class) Type type,
			@FormatWith(ClassObjectFormatter.class) Class<? extends ConstraintValidator<?, ?>> validatorClass1,
			@FormatWith(ClassObjectFormatter.class) Class<? extends ConstraintValidator<?, ?>> validatorClass2);

	@Message(id = 151,
			value = "A method overriding another method must not redefine the parameter constraint configuration, but method %2$s redefines the configuration of %1$s.")
	ConstraintDeclarationException getParameterConfigurationAlteredInSubTypeException(Callable superMethod, Callable subMethod);

	@Message(id = 152,
			value = "Two methods defined in parallel types must not declare parameter constraints, if they are overridden by the same method, but methods %s and %s both define parameter constraints.")
	ConstraintDeclarationException getParameterConstraintsDefinedInMethodsFromParallelTypesException(Callable method1, Callable method2);

	@Message(id = 153,
			value = "The constraint %1$s used ConstraintTarget#%2$s but is not specified on a method or constructor.")
	ConstraintDeclarationException getParametersOrReturnValueConstraintTargetGivenAtNonExecutableException(
			@FormatWith(ClassObjectFormatter.class) Class<? extends Annotation> constraint, ConstraintTarget target);

	@Message(id = 154, value = "Cross parameter constraint %1$s has no cross-parameter validator.")
	ConstraintDefinitionException getCrossParameterConstraintHasNoValidatorException(@FormatWith(ClassObjectFormatter.class) Class<? extends Annotation> constraint);

	@Message(id = 155,
			value = "Composed and composing constraints must have the same constraint type, but composed constraint %1$s has type %3$s, while composing constraint %2$s has type %4$s.")
	ConstraintDefinitionException getComposedAndComposingConstraintsHaveDifferentTypesException(@FormatWith(ClassObjectFormatter.class) Class<? extends Annotation> composedConstraintClass,
			@FormatWith(ClassObjectFormatter.class) Class<? extends Annotation> composingConstraintClass, ConstraintType composedConstraintType, ConstraintType composingConstraintType);

	@Message(id = 156,
			value = "Constraints with generic as well as cross-parameter validators must define an attribute validationAppliesTo(), but constraint %s doesn't.")
	ConstraintDefinitionException getGenericAndCrossParameterConstraintDoesNotDefineValidationAppliesToParameterException(
			@FormatWith(ClassObjectFormatter.class) Class<? extends Annotation> constraint);

	@Message(id = 157,
			value = "Return type of the attribute validationAppliesTo() of the constraint %s must be jakarta.validation.ConstraintTarget.")
	ConstraintDefinitionException getValidationAppliesToParameterMustHaveReturnTypeConstraintTargetException(
			@FormatWith(ClassObjectFormatter.class) Class<? extends Annotation> constraint);

	@Message(id = 158,
			value = "Default value of the attribute validationAppliesTo() of the constraint %s must be ConstraintTarget#IMPLICIT.")
	ConstraintDefinitionException getValidationAppliesToParameterMustHaveDefaultValueImplicitException(
			@FormatWith(ClassObjectFormatter.class) Class<? extends Annotation> constraint);

	@Message(id = 159,
			value = "Only constraints with generic as well as cross-parameter validators must define an attribute validationAppliesTo(), but constraint %s does.")
	ConstraintDefinitionException getValidationAppliesToParameterMustNotBeDefinedForNonGenericAndCrossParameterConstraintException(
			@FormatWith(ClassObjectFormatter.class) Class<? extends Annotation> constraint);

	@Message(id = 160,
			value = "Validator for cross-parameter constraint %s does not validate Object nor Object[].")
	ConstraintDefinitionException getValidatorForCrossParameterConstraintMustEitherValidateObjectOrObjectArrayException(
			@FormatWith(ClassObjectFormatter.class) Class<? extends Annotation> constraint);

	@Message(id = 161,
			value = "Two methods defined in parallel types must not define group conversions for a cascaded method return value, if they are overridden by the same method, but methods %s and %s both define parameter constraints.")
	ConstraintDeclarationException getMethodsFromParallelTypesMustNotDefineGroupConversionsForCascadedReturnValueException(Callable method1, Callable method2);

	@Message(id = 162,
			value = "The validated type %1$s does not specify the constructor/method: %2$s")
	IllegalArgumentException getMethodOrConstructorNotDefinedByValidatedTypeException(@FormatWith(ClassObjectFormatter.class) Class<?> validatedType, @FormatWith(ExecutableFormatter.class) Executable executable);

	@Message(id = 163,
			value = "The actual parameter type '%1$s' is not assignable to the expected one '%2$s' for parameter %3$d of '%4$s'")
	IllegalArgumentException getParameterTypesDoNotMatchException(@FormatWith(ClassObjectFormatter.class) Class<?> actualType, Type expectedType, int index, @FormatWith(ExecutableFormatter.class) Executable executable);

	@Message(id = 164, value = "%s has to be a auto-boxed type.")
	IllegalArgumentException getHasToBeABoxedTypeException(@FormatWith(ClassObjectFormatter.class) Class<?> clazz);

	@Message(id = 165, value = "Mixing IMPLICIT and other executable types is not allowed.")
	IllegalArgumentException getMixingImplicitWithOtherExecutableTypesException();

	@Message(id = 166,
			value = "@ValidateOnExecution is not allowed on methods overriding a superclass method or implementing an interface. Check configuration for %1$s")
	ValidationException getValidateOnExecutionOnOverriddenOrInterfaceMethodException(@FormatWith(ExecutableFormatter.class) Executable executable);

	@Message(id = 167,
			value = "A given constraint definition can only be overridden in one mapping file. %1$s is overridden in multiple files")
	ValidationException getOverridingConstraintDefinitionsInMultipleMappingFilesException(String constraintClassName);

	@Message(id = 168,
			value = "The message descriptor '%1$s' contains an unbalanced meta character '%2$c'.")
	MessageDescriptorFormatException getUnbalancedBeginEndParameterException(String messageDescriptor, char character);

	@Message(id = 169,
			value = "The message descriptor '%1$s' has nested parameters.")
	MessageDescriptorFormatException getNestedParameterException(String messageDescriptor);

	@Message(id = 170, value = "No JSR-223 scripting engine could be bootstrapped for language \"%s\".")
	ConstraintDeclarationException getCreationOfScriptExecutorFailedException(String languageName, @Cause Exception e);

	@Message(id = 171, value = "%s is configured more than once via the programmatic constraint declaration API.")
	ValidationException getBeanClassHasAlreadyBeConfiguredViaProgrammaticApiException(@FormatWith(ClassObjectFormatter.class) Class<?> beanClass);

	@Message(id = 172,
			value = "Property \"%2$s\" of type %1$s is configured more than once via the programmatic constraint declaration API.")
	ValidationException getPropertyHasAlreadyBeConfiguredViaProgrammaticApiException(@FormatWith(ClassObjectFormatter.class) Class<?> beanClass, String propertyName);

	@Message(id = 173,
			value = "Method %2$s of type %1$s is configured more than once via the programmatic constraint declaration API.")
	ValidationException getMethodHasAlreadyBeenConfiguredViaProgrammaticApiException(@FormatWith(ClassObjectFormatter.class) Class<?> beanClass, String method);

	@Message(id = 174,
			value = "Parameter %3$s of method or constructor %2$s of type %1$s is configured more than once via the programmatic constraint declaration API.")
	ValidationException getParameterHasAlreadyBeConfiguredViaProgrammaticApiException(@FormatWith(ClassObjectFormatter.class) Class<?> beanClass, Callable callable, int parameterIndex);

	@Message(id = 175,
			value = "The return value of method or constructor %2$s of type %1$s is configured more than once via the programmatic constraint declaration API.")
	ValidationException getReturnValueHasAlreadyBeConfiguredViaProgrammaticApiException(@FormatWith(ClassObjectFormatter.class) Class<?> beanClass, Callable callable);

	@Message(id = 176,
			value = "Constructor %2$s of type %1$s is configured more than once via the programmatic constraint declaration API.")
	ValidationException getConstructorHasAlreadyBeConfiguredViaProgrammaticApiException(@FormatWith(ClassObjectFormatter.class) Class<?> beanClass, String constructor);

	@Message(id = 177,
			value = "Cross-parameter constraints for the method or constructor %2$s of type %1$s are declared more than once via the programmatic constraint declaration API.")
	ValidationException getCrossParameterElementHasAlreadyBeConfiguredViaProgrammaticApiException(@FormatWith(ClassObjectFormatter.class) Class<?> beanClass, Callable callable);

	@Message(id = 178, value = "Multiplier cannot be negative: %d.")
	IllegalArgumentException getMultiplierCannotBeNegativeException(int multiplier);

	@Message(id = 179, value = "Weight cannot be negative: %d.")
	IllegalArgumentException getWeightCannotBeNegativeException(int weight);

	@Message(id = 180, value = "'%c' is not a digit nor a letter.")
	IllegalArgumentException getTreatCheckAsIsNotADigitNorALetterException(int weight);

	@Message(id = 181,
			value = "Wrong number of parameters. Method or constructor %1$s expects %2$d parameters, but got %3$d.")
	IllegalArgumentException getInvalidParameterCountForExecutableException(String executable, int expectedParameterCount, int actualParameterCount);

	@Message(id = 182, value = "No validation value unwrapper is registered for type '%1$s'.")
	ValidationException getNoUnwrapperFoundForTypeException(Type type);

	@Message(id = 183,
			value = "Unable to initialize 'jakarta.el.ExpressionFactory'. Check that you have the EL dependencies on the classpath, or use ParameterMessageInterpolator instead")
	ValidationException getUnableToInitializeELExpressionFactoryException(@Cause Throwable e);

	@LogMessage(level = WARN)
	@Message(id = 185, value = "Message contains EL expression: %1s, which is not supported by the selected message interpolator")
	void warnElIsUnsupported(String expression);

	@Message(id = 189,
			value = "The configuration of value unwrapping for property '%s' of bean '%s' is inconsistent between the field and its getter.")
	ConstraintDeclarationException getInconsistentValueUnwrappingConfigurationBetweenFieldAndItsGetterException(String property,
			@FormatWith(ClassObjectFormatter.class) Class<?> beanClass);

	@Message(id = 190, value = "Unable to parse %s.")
	ValidationException getUnableToCreateXMLEventReader(String file, @Cause Exception e);

	@LogMessage(level = WARN)
	@Message(id = 192, value = "Couldn't determine Java version from value %1s; Not enabling features requiring Java 8")
	void unknownJvmVersion(String vmVersionStr);

	@Message(id = 193, value = "%s is configured more than once via the programmatic constraint definition API.")
	ValidationException getConstraintHasAlreadyBeenConfiguredViaProgrammaticApiException(@FormatWith(ClassObjectFormatter.class) Class<? extends Annotation> annotationClass);

	@Message(id = 194, value = "An empty element is only supported when a CharSequence is expected.")
	ValidationException getEmptyElementOnlySupportedWhenCharSequenceIsExpectedExpection();

	@Message(id = 195, value = "Unable to reach the property to validate for the bean %s and the property path %s. A property is null along the way.")
	ValidationException getUnableToReachPropertyToValidateException(Object bean, Path path);

	@Message(id = 196, value = "Unable to convert the Type %s to a Class.")
	ValidationException getUnableToConvertTypeToClassException(Type type);

	@Message(id = 197, value = "No value extractor found for type parameter '%2$s' of type %1$s.")
	ConstraintDeclarationException getNoValueExtractorFoundForTypeException(@FormatWith(ClassObjectFormatter.class) Class<?> type, TypeVariable<?> typeParameter);

	@Message(id = 198, value = "No suitable value extractor found for type %1$s.")
	ConstraintDeclarationException getNoValueExtractorFoundForUnwrapException(Type type);

	@LogMessage(level = DEBUG)
	@Message(id = 200, value = "Using %s as clock provider.")
	void usingClockProvider(@FormatWith(ClassObjectFormatter.class) Class<? extends ClockProvider> clockProviderClass);

	@Message(id = 201, value = "Unable to instantiate clock provider class %s.")
	ValidationException getUnableToInstantiateClockProviderClassException(String clockProviderClassName, @Cause ValidationException e);

	@Message(id = 202, value = "Unable to get the current time from the clock provider")
	ValidationException getUnableToGetCurrentTimeFromClockProvider(@Cause Exception e);

	@Message(id = 203, value = "Value extractor type %1s fails to declare the extracted type parameter using @ExtractedValue.")
	ValueExtractorDefinitionException getValueExtractorFailsToDeclareExtractedValueException(@FormatWith(ClassObjectFormatter.class) Class<?> extractorType);

	@Message(id = 204, value = "Only one type parameter must be marked with @ExtractedValue for value extractor type %1s.")
	ValueExtractorDefinitionException getValueExtractorDeclaresExtractedValueMultipleTimesException(@FormatWith(ClassObjectFormatter.class) Class<?> extractorType);

	@Message(id = 205, value = "Invalid unwrapping configuration for constraint %2$s on %1$s. You can only define one of 'Unwrapping.Skip' or 'Unwrapping.Unwrap'.")
	ConstraintDeclarationException getInvalidUnwrappingConfigurationForConstraintException(Constrainable constrainable, @FormatWith(ClassObjectFormatter.class) Class<? extends Annotation> constraint);

	@Message(id = 206, value = "Unable to instantiate value extractor class %s.")
	ValidationException getUnableToInstantiateValueExtractorClassException(String valueExtractorClassName, @Cause ValidationException e);

	@LogMessage(level = DEBUG)
	@Message(id = 207, value = "Adding value extractor %s.")
	void addingValueExtractor(@FormatWith(ClassObjectFormatter.class) Class<? extends ValueExtractor<?>> valueExtractorClass);

	@Message(id = 208, value = "Given value extractor %2$s handles the same type and type use as previously given value extractor %1$s.")
	ValueExtractorDeclarationException getValueExtractorForTypeAndTypeUseAlreadyPresentException(ValueExtractor<?> first, ValueExtractor<?> second);

	@Message(id = 209, value = "A composing constraint (%2$s) must not be given directly on the composed constraint (%1$s) and using the corresponding List annotation at the same time.")
	ConstraintDeclarationException getCannotMixDirectAnnotationAndListContainerOnComposedConstraintException(@FormatWith(ClassObjectFormatter.class) Class<? extends Annotation> composedConstraint,
			@FormatWith(ClassObjectFormatter.class) Class<? extends Annotation> composingConstraint);

	@Message(id = 210, value = "Unable to find the type parameter %2$s in class %1$s.")
	IllegalArgumentException getUnableToFindTypeParameterInClass(@FormatWith(ClassObjectFormatter.class) Class<?> clazz, Object typeParameterReference);

	@Message(id = 211, value = "Given type is neither a parameterized nor an array type: %s.")
	ValidationException getTypeIsNotAParameterizedNorArrayTypeException(@FormatWith(TypeFormatter.class) Type type);

	@Message(id = 212, value = "Given type has no type argument with index %2$s: %1$s.")
	ValidationException getInvalidTypeArgumentIndexException(@FormatWith(TypeFormatter.class) Type type, int index);

	@Message(id = 213, value = "Given type has more than one type argument, hence an argument index must be specified: %s.")
	ValidationException getNoTypeArgumentIndexIsGivenForTypeWithMultipleTypeArgumentsException(@FormatWith(TypeFormatter.class) Type type);

	@Message(id = 214, value = "The same container element type of type %1$s is configured more than once via the programmatic constraint declaration API.")
	ValidationException getContainerElementTypeHasAlreadyBeenConfiguredViaProgrammaticApiException(@FormatWith(TypeFormatter.class) Type type);

	@Message(id = 215, value = "Calling parameter() is not allowed for the current element.")
	ValidationException getParameterIsNotAValidCallException();

	@Message(id = 216, value = "Calling returnValue() is not allowed for the current element.")
	ValidationException getReturnValueIsNotAValidCallException();

	@Message(id = 217, value = "The same container element type %2$s is configured more than once for location %1$s via the XML mapping configuration.")
	ValidationException getContainerElementTypeHasAlreadyBeenConfiguredViaXmlMappingConfigurationException(ConstraintLocation rootConstraintLocation,
			ContainerElementTypePath path);

	@Message(id = 218, value = "Having parallel definitions of value extractors on a given class is not allowed: %s.")
	ValueExtractorDefinitionException getParallelDefinitionsOfValueExtractorsException(@FormatWith(ClassObjectFormatter.class) Class<?> extractorImplementationType);

	@SuppressWarnings("rawtypes")
	@Message(id = 219, value = "Unable to get the most specific value extractor for type %1$s as several most specific value extractors are declared: %2$s.")
	ConstraintDeclarationException getUnableToGetMostSpecificValueExtractorDueToSeveralMaximallySpecificValueExtractorsDeclaredException(
			@FormatWith(ClassObjectFormatter.class) Class<?> valueType,
			@FormatWith(CollectionOfClassesObjectFormatter.class) Collection<Class<? extends ValueExtractor>> valueExtractors);

	@SuppressWarnings("rawtypes")
	@Message(id = 220, value = "When @ExtractedValue is defined on a type parameter of a container type, the type attribute may not be set: %1$s.")
	ValueExtractorDefinitionException getExtractedValueOnTypeParameterOfContainerTypeMayNotDefineTypeAttributeException(
			@FormatWith(ClassObjectFormatter.class) Class<? extends ValueExtractor> extractorImplementationType);

	@SuppressWarnings("rawtypes")
	@Message(id = 221, value = "An error occurred while extracting values in value extractor %1$s.")
	ValidationException getErrorWhileExtractingValuesInValueExtractorException(
			@FormatWith(ClassObjectFormatter.class) Class<? extends ValueExtractor> extractorImplementationType,
			@Cause Exception e);

	@Message(id = 222, value = "The same value extractor %s is added more than once via the XML configuration.")
	ValueExtractorDeclarationException getDuplicateDefinitionsOfValueExtractorException(String className);

	@SuppressWarnings("rawtypes")
	@Message(id = 223, value = "Implicit unwrapping is not allowed for type %1$s as several maximally specific value extractors marked with @UnwrapByDefault are declared: %2$s.")
	ConstraintDeclarationException getImplicitUnwrappingNotAllowedWhenSeveralMaximallySpecificValueExtractorsMarkedWithUnwrapByDefaultDeclaredException(
			@FormatWith(ClassObjectFormatter.class) Class<?> valueType,
			@FormatWith(CollectionOfClassesObjectFormatter.class) Collection<Class<? extends ValueExtractor>> valueExtractors);

	@Message(id = 224, value = "Unwrapping of ConstraintDescriptor is not supported yet.")
	ValidationException getUnwrappingOfConstraintDescriptorNotSupportedYetException();

	@SuppressWarnings("rawtypes")
	@Message(id = 225, value = "Only unbound wildcard type arguments are supported for the container type of the value extractor: %1$s.")
	ValueExtractorDefinitionException getOnlyUnboundWildcardTypeArgumentsSupportedForContainerTypeOfValueExtractorException(
			@FormatWith(ClassObjectFormatter.class) Class<? extends ValueExtractor> valueExtractorClass);

	@Message(id = 226, value = "Container element constraints and cascading validation are not supported on arrays: %1$s")
	ValidationException getContainerElementConstraintsAndCascadedValidationNotSupportedOnArraysException(@FormatWith(TypeFormatter.class) Type type);

	@Message(id = 227, value = "The validated type %1$s does not specify the property: %2$s")
	IllegalArgumentException getPropertyNotDefinedByValidatedTypeException(@FormatWith(ClassObjectFormatter.class) Class<?> validatedType, String propertyName);

	@Message(id = 228, value = "No value extractor found when narrowing down to the runtime type %3$s among the value extractors for type parameter '%2$s' of type %1$s.")
	ConstraintDeclarationException getNoValueExtractorFoundForTypeException(@FormatWith(TypeFormatter.class) Type declaredType,
			TypeVariable<?> declaredTypeParameter, @FormatWith(ClassObjectFormatter.class) Class<?> valueType);

	@Message(id = 229, value = "Unable to cast %1$s to %2$s.")
	ClassCastException getUnableToCastException(Object object, @FormatWith(ClassObjectFormatter.class) Class<?> clazz);

	@LogMessage(level = DEBUG)
	@Message(id = 230, value = "Using %s as script evaluator factory.")
	void usingScriptEvaluatorFactory(@FormatWith(ClassObjectFormatter.class) Class<? extends ScriptEvaluatorFactory> scriptEvaluatorFactoryClass);

	@Message(id = 231, value = "Unable to instantiate script evaluator factory class %s.")
	ValidationException getUnableToInstantiateScriptEvaluatorFactoryClassException(String scriptEvaluatorFactoryClassName, @Cause Exception e);

	@Message(id = 232, value = "No JSR 223 script engine found for language \"%s\".")
	ScriptEvaluatorNotFoundException getUnableToFindScriptEngineException(String languageName);

	@Message(id = 233, value = "An error occurred while executing the script: \"%s\".")
	ScriptEvaluationException getErrorExecutingScriptException(String script, @Cause Exception e);

	@LogMessage(level = DEBUG)
	@Message(id = 234, value = "Using %1$s as ValidatorFactory-scoped %2$s.")
	void logValidatorFactoryScopedConfiguration(@FormatWith(ClassObjectFormatter.class) Class<?> configuredClass, String configuredElement);

	@Message(id = 235, value = "Unable to create an annotation descriptor for %1$s.")
	ValidationException getUnableToCreateAnnotationDescriptor(@FormatWith(ClassObjectFormatter.class) Class<?> configuredClass, @Cause Throwable e);

	@Message(id = 236, value = "Unable to find the method required to create the constraint annotation descriptor.")
	ValidationException getUnableToFindAnnotationDefDeclaredMethods(@Cause Exception e);

	@Message(id = 237, value = "Unable to access method %3$s of class %2$s with parameters %4$s using lookup %1$s.")
	ValidationException getUnableToAccessMethodException(Lookup lookup, @FormatWith(ClassObjectFormatter.class) Class<?> clazz, String methodName,
			@FormatWith(ObjectArrayFormatter.class) Object[] parameterTypes, @Cause Throwable e);

	@LogMessage(level = DEBUG)
	@Message(id = 238, value = "Temporal validation tolerance set to %1$s.")
	void logTemporalValidationTolerance(@FormatWith(DurationFormatter.class) Duration tolerance);

	@Message(id = 239, value = "Unable to parse the temporal validation tolerance property %s. It should be a duration represented in milliseconds.")
	ValidationException getUnableToParseTemporalValidationToleranceException(String toleranceProperty, @Cause Exception e);

	@LogMessage(level = DEBUG)
	@Message(id = 240, value = "Constraint validator payload set to %1$s.")
	void logConstraintValidatorPayload(Object payload);

	@Message(id = 241, value = "Encountered unsupported element %1$s while parsing the XML configuration.")
	ValidationException logUnknownElementInXmlConfiguration(String tag);

	@LogMessage(level = WARN)
	@Message(id = 242, value = "Unable to load or instantiate JPA aware resolver %1$s. All properties will per default be traversable.")
	void logUnableToLoadOrInstantiateJPAAwareResolver(String traversableResolverClassName);

	@Message(id = 243, value = "Constraint %2$s references constraint validator type %1$s, but this validator is defined for constraint type %3$s.")
	ConstraintDefinitionException getConstraintValidatorDefinitionConstraintMismatchException(
			@FormatWith(ClassObjectFormatter.class) Class<? extends ConstraintValidator<?, ?>> constraintValidatorImplementationType,
			@FormatWith(ClassObjectFormatter.class) Class<? extends Annotation> registeredConstraintAnnotationType,
			@FormatWith(TypeFormatter.class) Type declaredConstraintAnnotationType);

	@Message(id = 244, value = "ConstrainedElement expected class was %1$s, but instead received %2$s.")
	AssertionError getUnexpectedConstraintElementType(@FormatWith(ClassObjectFormatter.class) Class<?> expecting, @FormatWith(ClassObjectFormatter.class) Class<?> got);

	@Message(id = 245, value = "Allowed constraint element types are FIELD and GETTER, but instead received %1$s.")
	AssertionError getUnsupportedConstraintElementType(ConstrainedElement.ConstrainedElementKind kind);

	@LogMessage(level = DEBUG)
	@Message(id = 246, value = "Using %s as getter property selection strategy.")
	void usingGetterPropertySelectionStrategy(@FormatWith(ClassObjectFormatter.class) Class<? extends GetterPropertySelectionStrategy> getterPropertySelectionStrategyClass);

	@Message(id = 247, value = "Unable to instantiate getter property selection strategy class %s.")
	ValidationException getUnableToInstantiateGetterPropertySelectionStrategyClassException(String getterPropertySelectionStrategyClassName, @Cause Exception e);

	@Message(id = 248, value = "Unable to get an XML schema named %s.")
	ValidationException unableToGetXmlSchema(String schemaResourceName);

	@Message(id = 250, value = "Uninitialized locale: %s. Please register your locale as a locale to initialize when initializing your ValidatorFactory.")
	ValidationException uninitializedLocale(Locale locale);

	@LogMessage(level = ERROR)
	@Message(id = 251, value = "An error occurred while loading an instance of service %s.")
	void unableToLoadInstanceOfService(String serviceName, @Cause ServiceConfigurationError e);

	@LogMessage(level = DEBUG)
	@Message(id = 252, value = "Using %s as property node name provider.")
	void usingPropertyNodeNameProvider(@FormatWith(ClassObjectFormatter.class) Class<? extends PropertyNodeNameProvider> propertyNodeNameProviderClass);

	@Message(id = 253, value = "Unable to instantiate property node name provider class %s.")
	ValidationException getUnableToInstantiatePropertyNodeNameProviderClassException(String propertyNodeNameProviderClassName, @Cause Exception e);

	@LogMessage(level = WARN)
	@Message(id = 254, value = "Missing parameter metadata for %s, which declares implicit or synthetic parameters."
			+ " Automatic resolution of generic type information for method parameters"
			+ " may yield incorrect results if multiple parameters have the same erasure."
			+ " To solve this, compile your code with the '-parameters' flag."
	)
	void missingParameterMetadataWithSyntheticOrImplicitParameters(@FormatWith(ExecutableFormatter.class) Executable executable);

	@LogMessage(level = DEBUG)
	@Message(id = 255, value = "Using %s as locale resolver.")
	void usingLocaleResolver(@FormatWith(ClassObjectFormatter.class) Class<? extends LocaleResolver> localeResolverClass);

	@Message(id = 256, value = "Unable to instantiate locale resolver class %s.")
	ValidationException getUnableToInstantiateLocaleResolverClassException(String localeResolverClassName, @Cause Exception e);

	@LogMessage(level = WARN)
	@Message(id = 257, value = "Expression variables have been defined for constraint %1$s while Expression Language is not enabled.")
	void expressionVariablesDefinedWithExpressionLanguageNotEnabled(Class<? extends Annotation> constraintAnnotation);

	@Message(id = 258, value = "Expressions should not be resolved when Expression Language features are disabled.")
	IllegalStateException expressionsNotResolvedWhenExpressionLanguageFeaturesDisabled();

	@Message(id = 259, value = "Provided Expression Language feature level is not supported.")
	IllegalStateException expressionsLanguageFeatureLevelNotSupported();

	@LogMessage(level = DEBUG)
	@Message(id = 260, value = "Expression Language feature level for constraints set to %1$s.")
	void logConstraintExpressionLanguageFeatureLevel(ExpressionLanguageFeatureLevel expressionLanguageFeatureLevel);

	@LogMessage(level = DEBUG)
	@Message(id = 261, value = "Expression Language feature level for custom violations set to %1$s.")
	void logCustomViolationExpressionLanguageFeatureLevel(ExpressionLanguageFeatureLevel expressionLanguageFeatureLevel);

	@Message(id = 262, value = "Unable to find an expression language feature level for value %s.")
	ValidationException invalidExpressionLanguageFeatureLevelValue(String expressionLanguageFeatureLevelName, @Cause IllegalArgumentException e);

	@LogMessage(level = WARN)
	@Message(id = 263, value = "EL expression '%s' references an unknown method.")
	void unknownMethodInExpressionLanguage(String expression, @Cause Exception e);

	@LogMessage(level = ERROR)
	@Message(id = 264, value = "Unable to interpolate EL expression '%s' as it uses a disabled feature.")
	void disabledFeatureInExpressionLanguage(String expression, @Cause Exception e);
}
