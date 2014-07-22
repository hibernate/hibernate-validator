/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hibernate.validator.internal.util.logging;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Set;
import java.util.regex.PatternSyntaxException;
import javax.validation.ConstraintDeclarationException;
import javax.validation.ConstraintDefinitionException;
import javax.validation.ConstraintTarget;
import javax.validation.ElementKind;
import javax.validation.GroupDefinitionException;
import javax.validation.Path;
import javax.validation.UnexpectedTypeException;
import javax.validation.ValidationException;
import javax.xml.stream.XMLStreamException;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Cause;
import org.jboss.logging.LogMessage;
import org.jboss.logging.Message;
import org.jboss.logging.MessageLogger;

import org.hibernate.validator.internal.engine.messageinterpolation.parser.MessageDescriptorFormatException;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl.ConstraintType;

import static org.jboss.logging.Logger.Level.INFO;
import static org.jboss.logging.Logger.Level.WARN;

/**
 * The logger interface for JBoss Logging.
 * <p>
 * <b>Note</b>:<br/>
 * New log messages must always use a new (incremented) message id. Don't re-use of existing message ids, even
 * if a given log method is not used anymore. Unused messages can be deleted.
 * </p>
 *
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2012 SERLI
 */
@SuppressWarnings("deprecation")
@MessageLogger(projectCode = "HV")
public interface Log extends BasicLogger {

	@LogMessage(level = INFO)
	@Message(id = 1, value = "Hibernate Validator %s")
	void version(String version);

	@LogMessage(level = INFO)
	@Message(id = 2, value = "Ignoring XML configuration.")
	void ignoringXmlConfiguration();

	@LogMessage(level = INFO)
	@Message(id = 3, value = "Using %s as constraint factory.")
	void usingConstraintFactory(String constraintFactoryClassName);

	@LogMessage(level = INFO)
	@Message(id = 4, value = "Using %s as message interpolator.")
	void usingMessageInterpolator(String messageInterpolatorClassName);

	@LogMessage(level = INFO)
	@Message(id = 5, value = "Using %s as traversable resolver.")
	void usingTraversableResolver(String traversableResolverClassName);

	@LogMessage(level = INFO)
	@Message(id = 6, value = "Using %s as validation provider.")
	void usingValidationProvider(String validationProviderClassName);

	@LogMessage(level = INFO)
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
	ValidationException getUnableToFindPropertyWithAccessException(Class<?> beanClass, String property, ElementType elementType);

	@Message(id = 14, value = "Type %1$s doesn't have a method %2$s.")
	IllegalArgumentException getUnableToFindMethodException(Class<?> beanClass, String method);

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
	ValidationException getUnableToFindProviderException(Class<?> providerClass);

	@Message(id = 28, value = "Unexpected exception during isValid call.")
	ValidationException getExceptionDuringIsValidCallException(@Cause RuntimeException e);

	@Message(id = 29, value = "Constraint factory returned null when trying to create instance of %s.")
	ValidationException getConstraintFactoryMustNotReturnNullException(String validatorClassName);

	@Message(id = 30, value = "No validator could be found for type: %s.")
	UnexpectedTypeException getNoValidatorFoundForTypeException(String className);

	@Message(id = 31,
			value = "There are multiple validator classes which could validate the type %1$s. The validator classes are: %2$s.")
	UnexpectedTypeException getMoreThanOneValidatorFoundForTypeException(Type type, String validatorClasses);

	@Message(id = 32, value = "Unable to initialize %s.")
	ValidationException getUnableToInitializeConstraintValidatorException(String validatorClassName, @Cause RuntimeException e);

	@Message(id = 33, value = "At least one custom message must be created if the default error message gets disabled.")
	ValidationException getAtLeastOneCustomMessageMustBeCreatedException();

	@Message(id = 34, value = "%s is not a valid Java Identifier.")
	IllegalArgumentException getInvalidJavaIdentifierException(String identifier);

	@Message(id = 35, value = "Unable to parse property path %s.")
	IllegalArgumentException getUnableToParsePropertyPathException(String propertyPath);

	@Message(id = 36, value = "Type %s not supported for unwrapping.")
	ValidationException getTypeNotSupportedForUnwrappingException(Class<?> type);

	@Message(id = 37,
			value = "Inconsistent fail fast configuration. Fail fast enabled via programmatic API, but explicitly disabled via properties.")
	ValidationException getInconsistentFailFastConfigurationException();

	@Message(id = 38, value = "Invalid property path.")
	IllegalArgumentException getInvalidPropertyPathException();

	@Message(id = 39, value = "Invalid property path. There is no property %1$s in entity %2$s.")
	IllegalArgumentException getInvalidPropertyPathException(String propertyName, String beanClassName);

	@Message(id = 40, value = "Property path must provide index or map key.")
	IllegalArgumentException getPropertyPathMustProvideIndexOrMapKeyException();

	@Message(id = 41, value = "Call to TraversableResolver.isReachable() threw an exception.")
	ValidationException getErrorDuringCallOfTraversableResolverIsReachableException(@Cause RuntimeException e);

	@Message(id = 42, value = "Call to TraversableResolver.isCascadable() threw an exception.")
	ValidationException getErrorDuringCallOfTraversableResolverIsCascadableException(@Cause RuntimeException e);

	@Message(id = 43, value = "Unable to expand default group list %1$s into sequence %2$s.")
	GroupDefinitionException getUnableToExpandDefaultGroupListException(List<?> defaultGroupList, List<?> groupList);

	@Message(id = 44, value = "At least one group has to be specified.")
	IllegalArgumentException getAtLeastOneGroupHasToBeSpecifiedException();

	@Message(id = 45, value = "A group has to be an interface. %s is not.")
	ValidationException getGroupHasToBeAnInterfaceException(String className);

	@Message(id = 46, value = "Sequence definitions are not allowed as composing parts of a sequence.")
	GroupDefinitionException getSequenceDefinitionsNotAllowedException();

	@Message(id = 47, value = "Cyclic dependency in groups definition")
	GroupDefinitionException getCyclicDependencyInGroupsDefinitionException();

	@Message(id = 48, value = "Unable to expand group sequence.")
	GroupDefinitionException getUnableToExpandGroupSequenceException();

	@Message(id = 49, value = "The given index must be between %1$s and %2$s.")
	IndexOutOfBoundsException getInvalidIndexException(String lowerBound, String upperBound);

	@Message(id = 50, value = "Missing format string in template: %s.")
	ValidationException getMissingFormatStringInTemplateException(String expression);

	@Message(id = 51, value = "Invalid format: %s.")
	ValidationException getInvalidFormatException(String message, @Cause IllegalFormatException e);

	@Message(id = 52,
			value = "Default group sequence and default group sequence provider cannot be defined at the same time.")
	GroupDefinitionException getInvalidDefaultGroupSequenceDefinitionException();

	@Message(id = 53, value = "'Default.class' cannot appear in default group sequence list.")
	GroupDefinitionException getNoDefaultGroupInGroupSequenceException();

	@Message(id = 54, value = "%s must be part of the redefined default group sequence.")
	GroupDefinitionException getBeanClassMustBePartOfRedefinedDefaultGroupSequenceException(String beanClassName);

	@Message(id = 55, value = "The default group sequence provider defined for %s has the wrong type")
	GroupDefinitionException getWrongDefaultGroupSequenceProviderTypeException(String beanClassName);

	@Message(id = 56, value = "Method or constructor %1$s doesn't have a parameter with index %2$d.")
	IllegalArgumentException getInvalidExecutableParameterIndexException(String executable, int index);

	@Message(id = 57, value = "Unable to find constraints for  %s.")
	ValidationException getUnableToFindAnnotationConstraintsException(Class<? extends Annotation> annotationClass);

	@Message(id = 59, value = "Unable to retrieve annotation parameter value.")
	ValidationException getUnableToRetrieveAnnotationParameterValueException(@Cause Exception e);

	@Message(id = 62,
			value = "Method or constructor %1$s has %2$s parameters, but the passed list of parameter meta data has a size of %3$s.")
	IllegalArgumentException getInvalidLengthOfParameterMetaDataListException(String executableName, int nbParameters, int listSize);

	@Message(id = 63, value = "Unable to instantiate %s.")
	ValidationException getUnableToInstantiateException(String className, @Cause Exception e);

	ValidationException getUnableToInstantiateException(Class<?> clazz, @Cause Exception e);

	@Message(id = 64, value = "Unable to instantiate %1$s: %2$s.")
	ValidationException getUnableToInstantiateException(String message, Class<?> clazz, @Cause Exception e);

	@Message(id = 65, value = "Unable to load class: %s.")
	ValidationException getUnableToLoadClassException(String className);

	ValidationException getUnableToLoadClassException(String className, @Cause Exception e);

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
	ConstraintDefinitionException getWrongAttributeTypeForOverriddenConstraintException(String expectedReturnType, Class<?> currentReturnType);

	@Message(id = 82, value = "Wrong parameter type. Expected: %1$s Actual: %2$s.")
	ValidationException getWrongParameterTypeException(String expectedType, String currentType);

	@Message(id = 83, value = "The specified annotation defines no parameter '%s'.")
	ValidationException getUnableToFindAnnotationParameterException(String parameterName, @Cause NoSuchMethodException e);

	@Message(id = 84, value = "Unable to get '%1$s' from %2$s.")
	ValidationException getUnableToGetAnnotationParameterException(String parameterName, String annotationName, @Cause Exception e);

	@Message(id = 85, value = "No value provided for parameter '%1$s' of annotation @%2$s.")
	IllegalArgumentException getNoValueProvidedForAnnotationParameterException(String parameterName, String annotation);

	@Message(id = 86, value = "Trying to instantiate %1$s with unknown parameter(s): %2$s.")
	RuntimeException getTryingToInstantiateAnnotationWithUnknownParametersException(Class<?> annotationType, Set<String> unknownParameters);

	@Message(id = 87, value = "Property name cannot be null or empty.")
	IllegalArgumentException getPropertyNameCannotBeNullOrEmptyException();

	@Message(id = 88, value = "Element type has to be FIELD or METHOD.")
	IllegalArgumentException getElementTypeHasToBeFieldOrMethodException();

	@Message(id = 89, value = "Member %s is neither a field nor a method.")
	IllegalArgumentException getMemberIsNeitherAFieldNorAMethodException(Member member);

	@Message(id = 90, value = "Unable to access %s.")
	ValidationException getUnableToAccessMemberException(String memberName, @Cause Exception e);

	@Message(id = 91, value = "%s has to be a primitive type.")
	IllegalArgumentException getHasToBeAPrimitiveTypeException(Class<?> clazz);

	@Message(id = 93, value = "null is an invalid type for a constraint validator.")
	ValidationException getNullIsAnInvalidTypeForAConstraintValidatorException();

	@Message(id = 94, value = "Missing actual type argument for type parameter: %s.")
	IllegalArgumentException getMissingActualTypeArgumentForTypeParameterException(TypeVariable<?> typeParameter);

	@Message(id = 95, value = "Unable to instantiate constraint factory class %s.")
	ValidationException getUnableToInstantiateConstraintFactoryClassException(String constraintFactoryClassName, @Cause ValidationException e);

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
	ValidationException getIsNotAnAnnotationException(String annotationClassName);

	@Message(id = 102, value = "%s is not a constraint validator class.")
	ValidationException getIsNotAConstraintValidatorClassException(Class<?> validatorClass);

	@Message(id = 103, value = "%s is configured at least twice in xml.")
	ValidationException getBeanClassHasAlreadyBeConfiguredInXmlException(String beanClassName);

	@Message(id = 104, value = "%1$s is defined twice in mapping xml for bean %2$s.")
	ValidationException getIsDefinedTwiceInMappingXmlForBeanException(String name, String beanClassName);

	@Message(id = 105, value = "%1$s does not contain the fieldType %2$s.")
	ValidationException getBeanDoesNotContainTheFieldException(String beanClassName, String fieldName);

	@Message(id = 106, value = "%1$s does not contain the property %2$s.")
	ValidationException getBeanDoesNotContainThePropertyException(String beanClassName, String getterName);

	@Message(id = 107, value = "Annotation of type %1$s does not contain a parameter %2$s.")
	ValidationException getAnnotationDoesNotContainAParameterException(String annotationClassName, String parameterName);

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
	ValidationException getInvalidReturnTypeException(Class<?> returnType, @Cause ClassCastException e);

	@Message(id = 113, value = "%s, %s, %s are reserved parameter names.")
	ValidationException getReservedParameterNamesException(String messageParameterName, String groupsParameterName, String payloadParameterName);

	@Message(id = 114, value = "Specified payload class %s does not implement javax.validation.Payload")
	ValidationException getWrongPayloadClassException(String payloadClassName);

	@Message(id = 115, value = "Error parsing mapping file.")
	ValidationException getErrorParsingMappingFileException(@Cause Exception e);

	@Message(id = 116, value = "%s")
	IllegalArgumentException getIllegalArgumentException(String message);

	@Message(id = 118, value = "Unable to cast %s (with element kind %s) to %s")
	ClassCastException getUnableToNarrowNodeTypeException(String actualDescriptorType, ElementKind kind, String expectedDescriptorType);

	@LogMessage(level = INFO)
	@Message(id = 119, value = "Using %s as parameter name provider.")
	void usingParameterNameProvider(String parameterNameProviderClassName);

	@Message(id = 120, value = "Unable to instantiate parameter name provider class %s.")
	ValidationException getUnableToInstantiateParameterNameProviderClassException(String parameterNameProviderClassName, @Cause ValidationException e);

	@Message(id = 121, value = "Unable to parse %s.")
	ValidationException getUnableToDetermineSchemaVersionException(String file, @Cause XMLStreamException e);

	@Message(id = 122, value = "Unsupported schema version for %s: %s.")
	ValidationException getUnsupportedSchemaVersionException(String file, String version);

	@Message(id = 123, value = "Unable to parse %s.")
	ValidationException getUnableToResetXmlInputStreamException(String file, @Cause IOException e);

	@Message(id = 124, value = "Found multiple group conversions for source group %s: %s.")
	ConstraintDeclarationException getMultipleGroupConversionsForSameSourceException(Class<?> from, Set<Class<?>> tos);

	@Message(id = 125, value = "Found group conversions for non-cascading element: %s.")
	ConstraintDeclarationException getGroupConversionOnNonCascadingElementException(String location);

	@Message(id = 127, value = "Found group conversion using a group sequence as source: %s.")
	ConstraintDeclarationException getGroupConversionForSequenceException(Class<?> from);

	@LogMessage(level = WARN)
	@Message(id = 129, value = "EL expression '%s' references an unknown property")
	void unknownPropertyInExpressionLanguage(String expression, @Cause Exception e);

	@LogMessage(level = WARN)
	@Message(id = 130, value = "Error in EL expression '%s'")
	void errorInExpressionLanguage(String expression, @Cause Exception e);

	@Message(id = 131,
			value = "A method return value must not be marked for cascaded validation more than once in a class hierarchy, but the following two methods are marked as such: %s, %s.")
	ConstraintDeclarationException getMethodReturnValueMustNotBeMarkedMoreThanOnceForCascadedValidationException(Member member1, Member member2);

	@Message(id = 132,
			value = "Void methods must not be constrained or marked for cascaded validation, but method %s is.")
	ConstraintDeclarationException getVoidMethodsMustNotBeConstrainedException(Member member);

	@Message(id = 133, value = "%1$s does not contain a constructor with the parameter types %2$s.")
	ValidationException getBeanDoesNotContainConstructorException(String beanClassName, String parameterTypes);

	@Message(id = 134, value = "Unable to load parameter of type '%1$s' in %2$s.")
	ValidationException getInvalidParameterTypeException(String type, String beanClassName);

	@Message(id = 135, value = "%1$s does not contain a method with the name '%2$s' and parameter types %3$s.")
	ValidationException getBeanDoesNotContainMethodException(String beanClassName, String methodName, List<Class<?>> parameterTypes);

	@Message(id = 136, value = "The specified constraint annotation class %1$s cannot be loaded.")
	ValidationException getUnableToLoadConstraintAnnotationClassException(String constraintAnnotationClass, @Cause Exception e);

	@Message(id = 137, value = "The method '%1$s' is defined twice in the mapping xml for bean %2$s.")
	ValidationException getMethodIsDefinedTwiceInMappingXmlForBeanException(String name, String beanClassName);

	@Message(id = 138, value = "The constructor '%1$s' is defined twice in the mapping xml for bean %2$s.")
	ValidationException getConstructorIsDefinedTwiceInMappingXmlForBeanException(String name, String beanClassName);

	@Message(id = 139,
			value = "The constraint '%1$s' defines multiple cross parameter validators. Only one is allowed.")
	ConstraintDefinitionException getMultipleCrossParameterValidatorClassesException(String constraint);

	@Message(id = 141,
			value = "The constraint %1$s used ConstraintTarget#IMPLICIT where the target cannot be inferred.")
	ConstraintDeclarationException getImplicitConstraintTargetInAmbiguousConfigurationException(String constraint);

	@Message(id = 142,
			value = "Cross parameter constraint %1$s is illegally placed on a parameterless method or constructor '%2$s'.")
	ConstraintDeclarationException getCrossParameterConstraintOnMethodWithoutParametersException(String constraint, String member);

	@Message(id = 143,
			value = "Cross parameter constraint %1$s is illegally placed on class level.")
	ConstraintDeclarationException getCrossParameterConstraintOnClassException(String constraint);

	@Message(id = 144,
			value = "Cross parameter constraint %1$s is illegally placed on field '%2$s'.")
	ConstraintDeclarationException getCrossParameterConstraintOnFieldException(String constraint, String field);

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
			value = "The constraint '%s' defines multiple validators for the type '%s'. Only one is allowed.")
	UnexpectedTypeException getMultipleValidatorsForSameTypeException(String constraint, String type);

	@Message(id = 151,
			value = "A method overriding another method must not alter the parameter constraint configuration, but method %2$s changes the configuration of %1$s.")
	ConstraintDeclarationException getParameterConfigurationAlteredInSubTypeException(Member superMethod, Member subMethod);

	@Message(id = 152,
			value = "Two methods defined in parallel types must not declare parameter constraints, if they are overridden by the same method, but methods %s and %s both define parameter constraints.")
	ConstraintDeclarationException getParameterConstraintsDefinedInMethodsFromParallelTypesException(Member method1, Member method2);

	@Message(id = 153,
			value = "The constraint %1$s used ConstraintTarget#%2$s but is not specified on a method or constructor.")
	ConstraintDeclarationException getParametersOrReturnValueConstraintTargetGivenAtNonExecutableException(String constraint, ConstraintTarget target);

	@Message(id = 154, value = "Cross parameter constraint %1$s has no cross-parameter validator.")
	ConstraintDefinitionException getCrossParameterConstraintHasNoValidatorException(String constraint);

	@Message(id = 155,
			value = "Composed and composing constraints must have the same constraint type, but composed constraint %1$s has type %3$s, while composing constraint %2$s has type %4$s.")
	ConstraintDefinitionException getComposedAndComposingConstraintsHaveDifferentTypesException(String composedConstraintTypeName, String composingConstraintTypeName, ConstraintType composedConstraintType, ConstraintType composingConstraintType);

	@Message(id = 156,
			value = "Constraints with generic as well as cross-parameter validators must define an attribute validationAppliesTo(), but constraint %s doesn't.")
	ConstraintDefinitionException getGenericAndCrossParameterConstraintDoesNotDefineValidationAppliesToParameterException(String constraint);

	@Message(id = 157,
			value = "Return type of the attribute validationAppliesTo() of the constraint %s must be javax.validation.ConstraintTarget.")
	ConstraintDefinitionException getValidationAppliesToParameterMustHaveReturnTypeConstraintTargetException(String constraint);

	@Message(id = 158,
			value = "Default value of the attribute validationAppliesTo() of the constraint %s must be ConstraintTarget#IMPLICIT.")
	ConstraintDefinitionException getValidationAppliesToParameterMustHaveDefaultValueImplicitException(String constraint);

	@Message(id = 159,
			value = "Only constraints with generic as well as cross-parameter validators must define an attribute validationAppliesTo(), but constraint %s does.")
	ConstraintDefinitionException getValidationAppliesToParameterMustNotBeDefinedForNonGenericAndCrossParameterConstraintException(String constraint);

	@Message(id = 160,
			value = "Validator for cross-parameter constraint %s does not validate Object nor Object[].")
	ConstraintDefinitionException getValidatorForCrossParameterConstraintMustEitherValidateObjectOrObjectArrayException(String constraint);

	@Message(id = 161,
			value = "Two methods defined in parallel types must not define group conversions for a cascaded method return value, if they are overridden by the same method, but methods %s and %s both define parameter constraints.")
	ConstraintDeclarationException getMethodsFromParallelTypesMustNotDefineGroupConversionsForCascadedReturnValueException(Member method1, Member method2);

	@Message(id = 162,
			value = "The validated type %1$s does not specify the constructor/method: %2$s")
	IllegalArgumentException getMethodOrConstructorNotDefinedByValidatedTypeException(String validatedTypeName, Member member);

	@Message(id = 163,
			value = "The actual parameter type '%1$s' is not assignable to the expected one '%2$s' for parameter %3$d of '%4$s'")
	IllegalArgumentException getParameterTypesDoNotMatchException(String actualType, String expectedType, int index, Member member);

	@Message(id = 164, value = "%s has to be a auto-boxed type.")
	IllegalArgumentException getHasToBeABoxedTypeException(Class<?> clazz);

	@Message(id = 165, value = "Mixing IMPLICIT and other executable types is not allowed.")
	IllegalArgumentException getMixingImplicitWithOtherExecutableTypesException();

	@Message(id = 166,
			value = "@ValidateOnExecution is not allowed on methods overriding a superclass method or implementing an interface. Check configuration for %1$s")
	ValidationException getValidateOnExecutionOnOverriddenOrInterfaceMethodException(Method m);

	@Message(id = 167,
			value = "A given constraint definition can only be overridden in one mapping file. %1$s is overridden in multiple files")
	ValidationException getOverridingConstraintDefinitionsInMultipleMappingFilesException(String constraintClass);

	@Message(id = 168,
			value = "The message descriptor '%1$s' contains an unbalanced meta character '%2$c' parameter.")
	MessageDescriptorFormatException getNonTerminatedParameterException(String messageDescriptor, char character);

	@Message(id = 169,
			value = "The message descriptor '%1$s' has nested parameters.")
	MessageDescriptorFormatException getNestedParameterException(String messageDescriptor);

	@Message(id = 170, value = "No JSR-223 scripting engine could be bootstrapped for language \"%s\".")
	ConstraintDeclarationException getCreationOfScriptExecutorFailedException(String languageName, @Cause Exception e);

	@Message(id = 171, value = "%s is configured more than once via the programmatic constraint declaration API.")
	ValidationException getBeanClassHasAlreadyBeConfiguredViaProgrammaticApiException(String beanClassName);

	@Message(id = 172,
			value = "Property \"%2$s\" of type %1$s is configured more than once via the programmatic constraint declaration API.")
	ValidationException getPropertyHasAlreadyBeConfiguredViaProgrammaticApiException(String beanClassName, String propertyName);

	@Message(id = 173,
			value = "Method %2$s of type %1$s is configured more than once via the programmatic constraint declaration API.")
	ValidationException getMethodHasAlreadyBeConfiguredViaProgrammaticApiException(String beanClassName, String method);

	@Message(id = 174,
			value = "Parameter %3$s of method or constructor %2$s of type %1$s is configured more than once via the programmatic constraint declaration API.")
	ValidationException getParameterHasAlreadyBeConfiguredViaProgrammaticApiException(String beanClassName, String executable, int parameterIndex);

	@Message(id = 175,
			value = "The return value of method or constructor %2$s of type %1$s is configured more than once via the programmatic constraint declaration API.")
	ValidationException getReturnValueHasAlreadyBeConfiguredViaProgrammaticApiException(String beanClassName, String executable);

	@Message(id = 176,
			value = "Constructor %2$s of type %1$s is configured more than once via the programmatic constraint declaration API.")
	ValidationException getConstructorHasAlreadyBeConfiguredViaProgrammaticApiException(String beanClassName, String constructor);

	@Message(id = 177,
			value = "Cross-parameter constraints for the method or constructor %2$s of type %1$s are declared more than once via the programmatic constraint declaration API.")
	ValidationException getCrossParameterElementHasAlreadyBeConfiguredViaProgrammaticApiException(String beanClassName, String executable);

	@Message(id = 178, value = "Multiplier cannot be negative: %d.")
	IllegalArgumentException getMultiplierCannotBeNegativeException(int multiplier);

	@Message(id = 179, value = "Weight cannot be negative: %d.")
	IllegalArgumentException getWeightCannotBeNegativeException(int weight);

	@Message(id = 180, value = "'%c' is not a digit nor a letter.")
	IllegalArgumentException getTreatCheckAsIsNotADigitNorALetterException(int weight);

	@Message(id = 181,
			value = "Wrong number of parameters. Method or constructor %1$s expects %2$d parameters, but got %3$d.")
	IllegalArgumentException getInvalidParameterCountForExecutableException(String executable, int expectedParameterCount, int actualParameterCount);

	@Message(id = 182, value = "No validation value unwrapper is registered for type %1$s.")
	ValidationException getNoUnwrapperFoundForTypeException(String typeName);

	@Message(id = 183, value = "Unable to load 'javax.el.ExpressionFactory'. Check that you have the EL dependencies on the classpath")
	ValidationException getMissingELDependenciesException();
}
