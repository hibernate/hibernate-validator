/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.cfg.json;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;
import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

import java.lang.invoke.MethodHandles;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.json.JsonObject;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.cfg.json.PropertyConstraintMappingContext;
import org.hibernate.validator.cfg.json.TypeConstraintMappingContext;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl.ConstraintType;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedType;
import org.hibernate.validator.internal.metadata.raw.JsonConfiguration;
import org.hibernate.validator.internal.properties.Constrainable;
import org.hibernate.validator.internal.properties.Property;
import org.hibernate.validator.internal.properties.json.JsonProperty;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.NewInstance;
import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;

/**
 * Constraint mapping creational context which allows to configure the class-level constraints for one bean.
 *
 * @param <C> The type represented by this creational context.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 * @author Marko Bekhta
 */
public final class JsonTypeConstraintMappingContextImpl<C> extends JsonConstraintMappingContextImplBase
		implements TypeConstraintMappingContext<C> {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final Class<C> objectType;

	private final Set<PropertyJsonConstraintMappingContextImpl> propertyContexts = newHashSet();
	private final Set<Constrainable> configuredMembers = newHashSet();

	private List<Class<?>> defaultGroupSequence;
	private Class<? extends DefaultGroupSequenceProvider<JsonObject>> defaultGroupSequenceProviderClass;

	JsonTypeConstraintMappingContextImpl(JsonConstraintMappingImpl mapping, Class<C> objectType) {
		super( mapping );
		this.objectType = objectType;
	}

	@Override
	public TypeConstraintMappingContext<C> constraint(ConstraintDef<?, ?> definition) {
		addConstraint( ConfiguredConstraint.forType( definition, objectType ) );
		return this;
	}

	@Override
	public TypeConstraintMappingContext<C> defaultGroupSequence(Class<?>... defaultGroupSequence) {
		this.defaultGroupSequence = Arrays.asList( defaultGroupSequence );
		return this;
	}

	@Override
	public TypeConstraintMappingContext<C> defaultGroupSequenceProviderClass(Class<? extends DefaultGroupSequenceProvider<JsonObject>> defaultGroupSequenceProviderClass) {
		this.defaultGroupSequenceProviderClass = defaultGroupSequenceProviderClass;
		return this;
	}

	@Override
	public PropertyConstraintMappingContext property(String property) {
		return property( property, String.class );
	}

	@Override
	public PropertyConstraintMappingContext property(String property, Class<?> propertyType) {
		Contracts.assertNotNull( property, "The property name must not be null." );
		Contracts.assertNotEmpty( property, MESSAGES.propertyNameMustNotBeEmpty() );

		Property constrainable = new JsonProperty( property, propertyType );
		if ( configuredMembers.contains( constrainable ) ) {
			throw LOG.getPropertyHasAlreadyBeConfiguredViaProgrammaticApiException( objectType, property );
		}

		PropertyJsonConstraintMappingContextImpl context = new PropertyJsonConstraintMappingContextImpl(
				this,
				constrainable
		);

		configuredMembers.add( constrainable );
		propertyContexts.add( context );
		return context;
	}

	JsonConfiguration<C> build(ConstraintHelper constraintHelper, TypeResolutionHelper typeResolutionHelper,
			ValueExtractorManager valueExtractorManager) {
		return new JsonConfiguration<>(
				ConfigurationSource.API,
				objectType,
				buildConstraintElements( constraintHelper, typeResolutionHelper, valueExtractorManager ),
				defaultGroupSequence,
				getDefaultGroupSequenceProvider()
		);
	}

	private Set<ConstrainedElement> buildConstraintElements(ConstraintHelper constraintHelper, TypeResolutionHelper typeResolutionHelper,
			ValueExtractorManager valueExtractorManager) {
		Set<ConstrainedElement> elements = newHashSet();

		//class-level configuration
		elements.add(
				new ConstrainedType(
						ConfigurationSource.API,
						objectType,
						getConstraints( constraintHelper, typeResolutionHelper, valueExtractorManager )
				)
		);

		//properties
		for ( PropertyJsonConstraintMappingContextImpl propertyContext : propertyContexts ) {
			elements.add( propertyContext.build( constraintHelper, typeResolutionHelper, valueExtractorManager ) );
		}

		return elements;
	}

	private DefaultGroupSequenceProvider<JsonObject> getDefaultGroupSequenceProvider() {
		return defaultGroupSequenceProviderClass != null ? run(
				NewInstance.action(
						defaultGroupSequenceProviderClass,
						"default group sequence provider"
				)
		) : null;
	}

	Class<?> getObjectType() {
		return objectType;
	}

	@Override
	protected ConstraintType getConstraintType() {
		return ConstraintType.GENERIC;
	}

	/**
	 * Runs the given privileged action, using a privileged block if required.
	 *
	 * <b>NOTE:</b> This must never be changed into a publicly available method to avoid execution of arbitrary
	 * privileged actions within HV's protection domain.
	 */
	private <T> T run(PrivilegedAction<T> action) {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
	}
}
