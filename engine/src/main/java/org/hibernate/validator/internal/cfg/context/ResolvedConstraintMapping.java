/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.cfg.context;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.validator.internal.engine.ConstraintCreationContext;
import org.hibernate.validator.internal.engine.constraintdefinition.ConstraintDefinitionContribution;
import org.hibernate.validator.internal.metadata.aggregated.CascadingMetaDataBuilder;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.core.MetaConstraints;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl.ConstraintType;
import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedField;
import org.hibernate.validator.internal.metadata.raw.ConstrainedParameter;
import org.hibernate.validator.internal.metadata.raw.ConstrainedType;
import org.hibernate.validator.internal.properties.Callable;
import org.hibernate.validator.internal.properties.javabean.JavaBeanField;
import org.hibernate.validator.internal.util.actions.NewInstance;
import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;

/**
 * Factory-owned programmatic metadata. Locations and cascading structures are resolved at factory
 * construction; MetaConstraints are built later, after all constraint validators are registered.
 * This state does not reference the reusable fluent declaration contexts.
 */
public final class ResolvedConstraintMapping {

	private final Set<ResolvedType<?>> types;
	private final AnnotationProcessingOptionsImpl annotationProcessingOptions;
	private final Set<ConstraintDefinitionContribution<?>> constraintDefinitions;

	ResolvedConstraintMapping(Set<ResolvedType<?>> types, AnnotationProcessingOptionsImpl annotationProcessingOptions,
			Set<ConstraintDefinitionContribution<?>> constraintDefinitions) {
		this.types = types;
		this.annotationProcessingOptions = annotationProcessingOptions;
		this.constraintDefinitions = constraintDefinitions;
	}

	public AnnotationProcessingOptionsImpl getAnnotationProcessingOptions() {
		return annotationProcessingOptions;
	}

	public Set<ResolvedType<?>> getTypes() {
		return types;
	}

	public Set<BeanConfiguration<?>> getBeanConfigurations(ConstraintCreationContext context) {
		Set<BeanConfiguration<?>> configurations = new HashSet<>();
		for ( ResolvedType<?> type : types ) {
			configurations.add( type.build( context ) );
		}
		return configurations;
	}

	public Set<ConstraintDefinitionContribution<?>> getConstraintDefinitionContributions() {
		return constraintDefinitions;
	}

	static Set<MetaConstraint<?>> buildConstraints(Set<ConfiguredConstraint<?>> constraints, ConstraintType type, ConstraintCreationContext context) {
		Set<MetaConstraint<?>> result = new HashSet<>();
		for ( ConfiguredConstraint<?> constraint : constraints ) {
			result.add( buildConstraint( constraint, type, context ) );
		}
		return result;
	}

	private static <A extends Annotation> MetaConstraint<A> buildConstraint(ConfiguredConstraint<A> constraint, ConstraintType type,
			ConstraintCreationContext context) {
		ConstraintDescriptorImpl<A> descriptor = new ConstraintDescriptorImpl<>( context.getConstraintHelper(),
				constraint.getLocation().getConstrainable(), constraint.createAnnotationDescriptor(), constraint.getLocation().getKind(), type );
		return MetaConstraints.create( context.getTypeResolutionHelper(), context.getValueExtractorManager(),
				context.getConstraintValidatorManager(), descriptor, constraint.getLocation() );
	}

	interface ResolvedElement {
		ConstrainedElement build(ConstraintCreationContext context);
	}

	public static final class ResolvedType<C> {
		private final Class<C> beanClass;
		private final Set<ConfiguredConstraint<?>> constraints;
		private final List<ResolvedElement> members;
		private final List<Class<?>> defaultGroupSequence;
		private final Class<? extends DefaultGroupSequenceProvider<? super C>> defaultGroupSequenceProviderClass;

		ResolvedType(Class<C> beanClass, Set<ConfiguredConstraint<?>> constraints, List<ResolvedElement> members,
				List<Class<?>> defaultGroupSequence, Class<? extends DefaultGroupSequenceProvider<? super C>> defaultGroupSequenceProviderClass) {
			this.beanClass = beanClass;
			this.constraints = constraints;
			this.members = members;
			this.defaultGroupSequence = defaultGroupSequence == null ? null : new ArrayList<>( defaultGroupSequence );
			this.defaultGroupSequenceProviderClass = defaultGroupSequenceProviderClass;
		}

		public Class<C> getBeanClass() {
			return beanClass;
		}

		BeanConfiguration<C> build(ConstraintCreationContext context) {
			Set<ConstrainedElement> elements = new HashSet<>();
			elements.add( new ConstrainedType( ConfigurationSource.API, beanClass, buildConstraints( constraints, ConstraintType.GENERIC, context ) ) );
			for ( ResolvedElement member : members ) {
				elements.add( member.build( context ) );
			}
			return new BeanConfiguration<>( ConfigurationSource.API, beanClass, elements, defaultGroupSequence,
					defaultGroupSequenceProviderClass == null ? null : NewInstance.action( defaultGroupSequenceProviderClass, "default group sequence provider" ) );
		}
	}

	static final class ResolvedCascading {
		final Set<ConfiguredConstraint<?>> containerConstraints;
		final CascadingMetaDataBuilder cascading;

		ResolvedCascading(Set<ConfiguredConstraint<?>> containerConstraints, CascadingMetaDataBuilder cascading) {
			this.containerConstraints = containerConstraints;
			this.cascading = cascading;
		}

		static ResolvedCascading empty() {
			return new ResolvedCascading( Set.of(), CascadingMetaDataBuilder.nonCascading() );
		}

		Set<MetaConstraint<?>> buildConstraints(ConstraintCreationContext context) {
			return ResolvedConstraintMapping.buildConstraints( containerConstraints, ConstraintType.GENERIC, context );
		}
	}

	static final class ResolvedField implements ResolvedElement {
		private final JavaBeanField field;
		private final Set<ConfiguredConstraint<?>> constraints;
		private final ResolvedCascading cascading;

		ResolvedField(JavaBeanField field, Set<ConfiguredConstraint<?>> constraints, ResolvedCascading cascading) {
			this.field = field;
			this.constraints = constraints;
			this.cascading = cascading;
		}

		@Override
		public ConstrainedElement build(ConstraintCreationContext context) {
			return new ConstrainedField( ConfigurationSource.API, field, buildConstraints( constraints, ConstraintType.GENERIC, context ),
					cascading.buildConstraints( context ), cascading.cascading );
		}
	}

	static final class ResolvedParameter {
		private final Callable callable;
		private final int index;
		private final Type type;
		private final Set<ConfiguredConstraint<?>> constraints;
		private final ResolvedCascading cascading;

		ResolvedParameter(Callable callable, int index, Set<ConfiguredConstraint<?>> constraints, ResolvedCascading cascading) {
			this.callable = callable;
			this.index = index;
			this.type = callable.getParameterGenericType( index );
			this.constraints = constraints;
			this.cascading = cascading;
		}

		ConstrainedParameter build(ConstraintCreationContext context) {
			return new ConstrainedParameter( ConfigurationSource.API, callable, type, index,
					buildConstraints( constraints, ConstraintType.GENERIC, context ), cascading.buildConstraints( context ), cascading.cascading );
		}
	}

	static final class ResolvedExecutable implements ResolvedElement {
		private final Callable callable;
		private final List<ResolvedParameter> parameters;
		private final Set<ConfiguredConstraint<?>> crossParameterConstraints;
		private final Set<ConfiguredConstraint<?>> returnValueConstraints;
		private final ResolvedCascading cascading;

		ResolvedExecutable(Callable callable, List<ResolvedParameter> parameters, Set<ConfiguredConstraint<?>> crossParameterConstraints,
				Set<ConfiguredConstraint<?>> returnValueConstraints, ResolvedCascading cascading) {
			this.callable = callable;
			this.parameters = parameters;
			this.crossParameterConstraints = crossParameterConstraints;
			this.returnValueConstraints = returnValueConstraints;
			this.cascading = cascading;
		}

		@Override
		public ConstrainedElement build(ConstraintCreationContext context) {
			List<ConstrainedParameter> resolvedParameters = new ArrayList<>();
			for ( ResolvedParameter parameter : parameters ) {
				resolvedParameters.add( parameter.build( context ) );
			}
			return new ConstrainedExecutable( ConfigurationSource.API, callable, resolvedParameters,
					buildConstraints( crossParameterConstraints, ConstraintType.CROSS_PARAMETER, context ),
					buildConstraints( returnValueConstraints, ConstraintType.GENERIC, context ), cascading.buildConstraints( context ), cascading.cascading );
		}
	}
}
