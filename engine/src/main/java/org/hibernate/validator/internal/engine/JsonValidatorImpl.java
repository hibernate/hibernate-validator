/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine;

import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

import java.lang.annotation.ElementType;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

import javax.json.JsonObject;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.ConstraintViolation;
import javax.validation.ElementKind;
import javax.validation.Path;
import javax.validation.TraversableResolver;
import javax.validation.Validator;
import javax.validation.groups.Default;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
import org.hibernate.validator.internal.engine.ValidationContext.ValidationContextBuilder;
import org.hibernate.validator.internal.engine.ValidationContext.ValidatorScopedContext;
import org.hibernate.validator.internal.engine.ValidatorFactoryImpl.ValidatorFactoryScopedContext;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorManager;
import org.hibernate.validator.internal.engine.groups.Group;
import org.hibernate.validator.internal.engine.groups.GroupWithInheritance;
import org.hibernate.validator.internal.engine.groups.Sequence;
import org.hibernate.validator.internal.engine.groups.ValidationOrder;
import org.hibernate.validator.internal.engine.groups.ValidationOrderGenerator;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.engine.resolver.TraversableResolvers;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.BeanMetaDataManager;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;
import org.hibernate.validator.internal.metadata.aggregated.CascadingMetaData;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.facets.Cascadable;
import org.hibernate.validator.internal.metadata.facets.Validatable;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.location.PropertyConstraintLocation;
import org.hibernate.validator.internal.metadata.location.TypeArgumentConstraintLocation;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * The main Bean Validation class. This is the core processing class of Hibernate Validator.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 * @author Guillaume Smet
 * @author Marko Bekhta
 */
public class JsonValidatorImpl {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	/**
	 * The default group array used in case any of the validate methods is called without a group.
	 */
	private static final Collection<Class<?>> DEFAULT_GROUPS = Collections.<Class<?>>singletonList( Default.class );

	/**
	 * Used to resolve the group execution order for a validate call.
	 */
	private final transient ValidationOrderGenerator validationOrderGenerator;

	/**
	 * Reference to shared {@code ConstraintValidatorFactory}.
	 */
	private final ConstraintValidatorFactory constraintValidatorFactory;

	/**
	 * {@link TraversableResolver} as passed to the constructor of this instance.
	 * Never use it directly, always use {@link #getCachingTraversableResolver()} to retrieved the single threaded caching wrapper.
	 */
	private final TraversableResolver traversableResolver;

	/**
	 * Used to get access to the bean meta data. Used to avoid to parsing the constraint configuration for each call
	 * of a given entity.
	 */
	private final BeanMetaDataManager beanMetaDataManager;

	/**
	 * Manages the life cycle of constraint validator instances
	 */
	private final ConstraintValidatorManager constraintValidatorManager;

	private final ValueExtractorManager valueExtractorManager;

	/**
	 * Context containing all {@link Validator} level helpers and configuration properties.
	 */
	private final ValidatorScopedContext validatorScopedContext;

	/**
	 * The constraint initialization context is stored at this level to prevent creating a new instance each time we
	 * initialize a new constraint validator as, for now, it only contains Validator scoped objects.
	 */
	private final HibernateConstraintValidatorInitializationContext constraintValidatorInitializationContext;

	public JsonValidatorImpl(ConstraintValidatorFactory constraintValidatorFactory,
			BeanMetaDataManager beanMetaDataManager,
			ValueExtractorManager valueExtractorManager,
			ConstraintValidatorManager constraintValidatorManager,
			ValidationOrderGenerator validationOrderGenerator,
			ValidatorFactoryScopedContext validatorFactoryScopedContext) {
		this.constraintValidatorFactory = constraintValidatorFactory;
		this.beanMetaDataManager = beanMetaDataManager;
		this.valueExtractorManager = valueExtractorManager;
		this.constraintValidatorManager = constraintValidatorManager;
		this.validationOrderGenerator = validationOrderGenerator;
		this.validatorScopedContext = new ValidatorScopedContext( validatorFactoryScopedContext );
		this.traversableResolver = validatorFactoryScopedContext.getTraversableResolver();
		this.constraintValidatorInitializationContext = validatorFactoryScopedContext.getConstraintValidatorInitializationContext();
	}

	public Set<ConstraintViolation<JsonObject>> validateJson(JsonObject object, Class<?> typeToValidate, Class<?>... groups) {
		Contracts.assertNotNull( object, MESSAGES.validatedObjectMustNotBeNull() );
		sanityCheckGroups( groups );

		ValidationContext<JsonObject> validationContext = getValidationContextBuilder().forValidateJson( object, typeToValidate );

		if ( !validationContext.getRootBeanMetaData().hasConstraints() ) {
			return Collections.emptySet();
		}

		ValidationOrder validationOrder = determineGroupValidationOrder( groups );
		ValueContext<?, Object> valueContext = ValueContext.getLocalExecutionContext(
				validatorScopedContext.getParameterNameProvider(),
				object,
				validationContext.getRootBeanMetaData(),
				PathImpl.createRootPath()
		);

		return validateInContext( validationContext, valueContext, validationOrder );
	}

	private ValidationContextBuilder getValidationContextBuilder() {
		return ValidationContext.getValidationContextBuilder(
				beanMetaDataManager,
				constraintValidatorManager,
				constraintValidatorFactory,
				validatorScopedContext,
				TraversableResolvers.wrapWithCachingForSingleValidation( traversableResolver, validatorScopedContext.isTraversableResolverResultCacheEnabled() ),
				constraintValidatorInitializationContext

		);
	}

	private void sanityCheckGroups(Class<?>[] groups) {
		Contracts.assertNotNull( groups, MESSAGES.groupMustNotBeNull() );
		for ( Class<?> clazz : groups ) {
			if ( clazz == null ) {
				throw new IllegalArgumentException( MESSAGES.groupMustNotBeNull() );
			}
		}
	}

	private ValidationOrder determineGroupValidationOrder(Class<?>[] groups) {
		Collection<Class<?>> resultGroups;
		// if no groups is specified use the default
		if ( groups.length == 0 ) {
			resultGroups = DEFAULT_GROUPS;
		}
		else {
			resultGroups = Arrays.asList( groups );
		}
		return validationOrderGenerator.getValidationOrder( resultGroups );
	}

	/**
	 * Validates the given object using the available context information.
	 *
	 * @param validationContext the global validation context
	 * @param valueContext the current validation context
	 * @param validationOrder Contains the information which and in which order groups have to be executed
	 * @param <T> The root bean type
	 *
	 * @return Set of constraint violations or the empty set if there were no violations.
	 */
	private <T, U> Set<ConstraintViolation<T>> validateInContext(ValidationContext<T> validationContext, ValueContext<U, Object> valueContext,
			ValidationOrder validationOrder) {
		if ( valueContext.getCurrentBean() == null ) {
			return Collections.emptySet();
		}

		BeanMetaData<U> beanMetaData = valueContext.getCurrentBeanMetaData();
		if ( beanMetaData.defaultGroupSequenceIsRedefined() ) {
			validationOrder.assertDefaultGroupSequenceIsExpandable( beanMetaData.getDefaultGroupSequence( valueContext.getCurrentBean() ) );
		}

		// process first single groups. For these we can optimise object traversal by first running all validations on the current bean
		// before traversing the object.
		Iterator<Group> groupIterator = validationOrder.getGroupIterator();
		while ( groupIterator.hasNext() ) {
			Group group = groupIterator.next();
			valueContext.setCurrentGroup( group.getDefiningClass() );
			validateConstraintsForCurrentGroup( validationContext, valueContext );
			if ( shouldFailFast( validationContext ) ) {
				return validationContext.getFailingConstraints();
			}
		}
		groupIterator = validationOrder.getGroupIterator();
		while ( groupIterator.hasNext() ) {
			Group group = groupIterator.next();
			valueContext.setCurrentGroup( group.getDefiningClass() );
			validateCascadedConstraints( validationContext, valueContext );
			if ( shouldFailFast( validationContext ) ) {
				return validationContext.getFailingConstraints();
			}
		}

		// now we process sequences. For sequences I have to traverse the object graph since I have to stop processing when an error occurs.
		Iterator<Sequence> sequenceIterator = validationOrder.getSequenceIterator();
		while ( sequenceIterator.hasNext() ) {
			Sequence sequence = sequenceIterator.next();
			for ( GroupWithInheritance groupOfGroups : sequence ) {
				int numberOfViolations = validationContext.getFailingConstraints().size();

				for ( Group group : groupOfGroups ) {
					valueContext.setCurrentGroup( group.getDefiningClass() );

					validateConstraintsForCurrentGroup( validationContext, valueContext );
					if ( shouldFailFast( validationContext ) ) {
						return validationContext.getFailingConstraints();
					}

					validateCascadedConstraints( validationContext, valueContext );
					if ( shouldFailFast( validationContext ) ) {
						return validationContext.getFailingConstraints();
					}
				}
				if ( validationContext.getFailingConstraints().size() > numberOfViolations ) {
					break;
				}
			}
		}
		return validationContext.getFailingConstraints();
	}

	private void validateConstraintsForCurrentGroup(ValidationContext<?> validationContext, ValueContext<?, Object> valueContext) {
		// we are not validating the default group there is nothing special to consider. If we are validating the default
		// group sequence we have to consider that a class in the hierarchy could redefine the default group sequence.
		if ( !valueContext.validatingDefault() ) {
			validateConstraintsForNonDefaultGroup( validationContext, valueContext );
		}
		else {
			validateConstraintsForDefaultGroup( validationContext, valueContext );
		}
	}

	// TODO: no need to check for class hierarchy ... it's just a json - removed a loop over `beanMetaData.getClassHierarchy()`
	private <U> void validateConstraintsForDefaultGroup(ValidationContext<?> validationContext, ValueContext<U, Object> valueContext) {
		final BeanMetaData<U> beanMetaData = valueContext.getCurrentBeanMetaData();
		Set<MetaConstraint<?>> metaConstraints = beanMetaData.getDirectMetaConstraints();
		validateConstraintsForSingleDefaultGroupElement(
				validationContext, valueContext, metaConstraints,
				Group.DEFAULT_GROUP
		);

		validationContext.markCurrentBeanAsProcessed( valueContext );
	}

	// TODO: removed validatedInterfaces parameter as no interfaces ...
	private <U> boolean validateConstraintsForSingleDefaultGroupElement(ValidationContext<?> validationContext, ValueContext<U, Object> valueContext,
			Set<MetaConstraint<?>> metaConstraints, Group defaultSequenceMember) {
		boolean validationSuccessful = true;

		valueContext.setCurrentGroup( defaultSequenceMember.getDefiningClass() );

		for ( MetaConstraint<?> metaConstraint : metaConstraints ) {
			boolean tmp = validateMetaConstraint( validationContext, valueContext, valueContext.getCurrentBean(), metaConstraint );
			if ( shouldFailFast( validationContext ) ) {
				return false;
			}

			validationSuccessful = validationSuccessful && tmp;
		}
		return validationSuccessful;
	}

	private void validateConstraintsForNonDefaultGroup(ValidationContext<?> validationContext, ValueContext<?, Object> valueContext) {
		validateMetaConstraints( validationContext, valueContext, valueContext.getCurrentBean(), valueContext.getCurrentBeanMetaData().getMetaConstraints() );
		validationContext.markCurrentBeanAsProcessed( valueContext );
	}

	private void validateMetaConstraints(ValidationContext<?> validationContext, ValueContext<?, Object> valueContext, Object parent,
			Iterable<MetaConstraint<?>> constraints) {

		for ( MetaConstraint<?> metaConstraint : constraints ) {
			validateMetaConstraint( validationContext, valueContext, parent, metaConstraint );
			if ( shouldFailFast( validationContext ) ) {
				break;
			}
		}
	}

	private boolean validateMetaConstraint(ValidationContext<?> validationContext, ValueContext<?, Object> valueContext, Object parent, MetaConstraint<?> metaConstraint) {
		ValueContext.ValueState<Object> originalValueState = valueContext.getCurrentValueState();
		valueContext.appendNode( metaConstraint.getLocation() );
		boolean success = true;

		if ( isValidationRequired( validationContext, valueContext, metaConstraint ) ) {

			if ( parent != null ) {
				valueContext.setCurrentValidatedValue( valueContext.getValue( parent, metaConstraint.getLocation() ) );
			}

			success = metaConstraint.validateConstraint( validationContext, valueContext );

			validationContext.markConstraintProcessed( valueContext.getCurrentBean(), valueContext.getPropertyPath(), metaConstraint );
		}

		// reset the value context to the state before this call
		valueContext.resetValueState( originalValueState );

		return success;
	}

	/**
	 * Validates all cascaded constraints for the given bean using the current group set in the execution context.
	 * This method must always be called after validateConstraints for the same context.
	 *
	 * @param validationContext The execution context
	 * @param valueContext Collected information for single validation
	 */
	private void validateCascadedConstraints(ValidationContext<?> validationContext, ValueContext<?, Object> valueContext) {
		Validatable validatable = valueContext.getCurrentValidatable();
		ValueContext.ValueState<Object> originalValueState = valueContext.getCurrentValueState();

		for ( Cascadable cascadable : validatable.getCascadables() ) {
			valueContext.appendNode( cascadable );

			ElementType elementType = cascadable.getElementType();
			if ( isCascadeRequired( validationContext, valueContext.getCurrentBean(), valueContext.getPropertyPath(), elementType ) ) {
				Object value = getCascadableValue( validationContext, valueContext.getCurrentBean(), cascadable );
				CascadingMetaData cascadingMetaData = cascadable.getCascadingMetaData();

				if ( value != null ) {
					CascadingMetaData effectiveCascadingMetaData = cascadingMetaData.addRuntimeContainerSupport( valueExtractorManager, value.getClass() );

					// validate cascading on the annotated object
					if ( effectiveCascadingMetaData.isCascading() ) {
						validateCascadedAnnotatedObjectForCurrentGroup( value, (Class<?>) cascadable.getCascadableType(), validationContext, valueContext, effectiveCascadingMetaData );
					}

					// TODO: for now json arrays will not be validated ....
					//					if ( effectiveCascadingMetaData.isContainer() ) {
					//						ContainerCascadingMetaData containerCascadingMetaData = effectiveCascadingMetaData.as( ContainerCascadingMetaData.class );
					//
					//						if ( containerCascadingMetaData.hasContainerElementsMarkedForCascading() ) {
					//							// validate cascading on the container elements
					//							validateCascadedContainerElementsForCurrentGroup( value, validationContext, valueContext,
					//									containerCascadingMetaData.getContainerElementTypesCascadingMetaData()
					//							);
					//						}
					//					}
				}
			}

			// reset the value context
			valueContext.resetValueState( originalValueState );
		}
	}

	private void validateCascadedAnnotatedObjectForCurrentGroup(Object value, Class<?> typeToValidate, ValidationContext<?> validationContext, ValueContext<?, Object> valueContext,
			CascadingMetaData cascadingMetaData) {
		if ( validationContext.isBeanAlreadyValidated( value, valueContext.getCurrentGroup(), valueContext.getPropertyPath() ) ||
				shouldFailFast( validationContext ) ) {
			return;
		}

		Class<?> originalGroup = valueContext.getCurrentGroup();
		Class<?> currentGroup = cascadingMetaData.convertGroup( originalGroup );

		// expand the group only if was created by group conversion;
		// otherwise we're looping through the right validation order
		// already and need only to pass the current element
		ValidationOrder validationOrder = validationOrderGenerator.getValidationOrder( currentGroup, currentGroup != originalGroup );

		ValueContext<?, Object> cascadedValueContext = buildNewLocalExecutionContext( valueContext, value, typeToValidate );

		validateInContext( validationContext, cascadedValueContext, validationOrder );
	}

	private ValueContext<?, Object> buildNewLocalExecutionContext(ValueContext<?, ?> valueContext, Object value, Class<?> typeToValidate) {
		ValueContext<?, Object> newValueContext;
		if ( value != null ) {
			newValueContext = ValueContext.getLocalExecutionContext(
					validatorScopedContext.getParameterNameProvider(),
					value,
					beanMetaDataManager.getJsonMetaData( typeToValidate ),
					valueContext.getPropertyPath()
			);
			newValueContext.setCurrentValidatedValue( value );
		}
		else {
			newValueContext = ValueContext.getLocalExecutionContext(
					validatorScopedContext.getParameterNameProvider(),
					valueContext.getCurrentBeanType(),
					valueContext.getCurrentBeanMetaData(),
					valueContext.getPropertyPath()
			);
		}

		return newValueContext;
	}

	private boolean isValidationRequired(ValidationContext<?> validationContext,
			ValueContext<?, ?> valueContext,
			MetaConstraint<?> metaConstraint) {
		// validateProperty()/validateValue() call, but this constraint is for another property
		if ( validationContext.getValidatedProperty() != null &&
				!Objects.equals( validationContext.getValidatedProperty(), getPropertyName( metaConstraint.getLocation() ) ) ) {
			return false;
		}
		if ( validationContext.hasMetaConstraintBeenProcessed(
				valueContext.getCurrentBean(),
				valueContext.getPropertyPath(),
				metaConstraint
		) ) {
			return false;
		}

		if ( !metaConstraint.getGroupList().contains( valueContext.getCurrentGroup() ) ) {
			return false;
		}
		return isReachable(
				validationContext,
				valueContext.getCurrentBean(),
				valueContext.getPropertyPath(),
				metaConstraint.getElementType()
		);
	}

	private boolean isReachable(ValidationContext<?> validationContext, Object traversableObject, PathImpl path, ElementType type) {
		if ( needToCallTraversableResolver( path, type ) ) {
			return true;
		}

		Path pathToObject = path.getPathWithoutLeafNode();
		try {
			return validationContext.getTraversableResolver().isReachable(
					traversableObject,
					path.getLeafNode(),
					validationContext.getRootBeanClass(),
					pathToObject,
					type
			);
		}
		catch (RuntimeException e) {
			throw LOG.getErrorDuringCallOfTraversableResolverIsReachableException( e );
		}
	}

	private boolean needToCallTraversableResolver(PathImpl path, ElementType type) {
		// as the TraversableResolver interface is designed right now it does not make sense to call it when
		// there is no traversable object hosting the property to be accessed. For this reason we don't call the resolver
		// for class level constraints (ElementType.TYPE) or top level method parameters or return values.
		// see also BV expert group discussion - http://lists.jboss.org/pipermail/beanvalidation-dev/2013-January/000722.html
		return isClassLevelConstraint( type )
				|| isCrossParameterValidation( path )
				|| isParameterValidation( path )
				|| isReturnValueValidation( path );
	}

	private boolean isCascadeRequired(ValidationContext<?> validationContext, Object traversableObject, PathImpl path, ElementType type) {
		if ( needToCallTraversableResolver( path, type ) ) {
			return true;
		}

		boolean isReachable = isReachable( validationContext, traversableObject, path, type );
		if ( !isReachable ) {
			return false;
		}

		Path pathToObject = path.getPathWithoutLeafNode();
		try {
			return validationContext.getTraversableResolver().isCascadable(
					traversableObject,
					path.getLeafNode(),
					validationContext.getRootBeanClass(),
					pathToObject,
					type
			);
		}
		catch (RuntimeException e) {
			throw LOG.getErrorDuringCallOfTraversableResolverIsCascadableException( e );
		}
	}

	private boolean isClassLevelConstraint(ElementType type) {
		return ElementType.TYPE.equals( type );
	}

	private boolean isCrossParameterValidation(PathImpl path) {
		return path.getLeafNode().getKind() == ElementKind.CROSS_PARAMETER;
	}

	private boolean isParameterValidation(PathImpl path) {
		return path.getLeafNode().getKind() == ElementKind.PARAMETER;
	}

	private boolean isReturnValueValidation(PathImpl path) {
		return path.getLeafNode().getKind() == ElementKind.RETURN_VALUE;
	}

	private boolean shouldFailFast(ValidationContext<?> validationContext) {
		return validationContext.isFailFastModeEnabled() && !validationContext.getFailingConstraints().isEmpty();
	}

	private Object getCascadableValue(ValidationContext<?> validationContext, Object object, Cascadable cascadable) {
		return cascadable.getValue( object );
	}

	private String getPropertyName(ConstraintLocation location) {
		if ( location instanceof TypeArgumentConstraintLocation ) {
			location = ( (TypeArgumentConstraintLocation) location ).getOuterDelegate();
		}

		if ( location instanceof PropertyConstraintLocation ) {
			return ( (PropertyConstraintLocation) location ).getPropertyName();
		}

		return null;
	}
}
