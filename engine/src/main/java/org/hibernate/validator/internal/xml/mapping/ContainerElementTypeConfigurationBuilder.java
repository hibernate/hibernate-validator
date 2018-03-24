/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.xml.mapping;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.validator.internal.metadata.aggregated.CascadingMetaDataBuilder;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;

/**
 * Builds the aggregated cascading and type argument constraints configuration from the {@link ContainerElementTypeStaxBuilder} elements.
 *
 * @author Guillaume Smet
 * @author Marko Bekhta
 */
class ContainerElementTypeConfigurationBuilder {

	private final List<ContainerElementTypeStaxBuilder> containerElementTypeStaxBuilders;
	private final Set<ContainerElementTypePath> configuredPaths;

	ContainerElementTypeConfigurationBuilder() {
		this.containerElementTypeStaxBuilders = new ArrayList<>();
		this.configuredPaths = new HashSet<>();
	}

	public void add(ContainerElementTypeStaxBuilder containerElementTypeStaxBuilder) {
		containerElementTypeStaxBuilders.add( containerElementTypeStaxBuilder );
	}

	ContainerElementTypeConfiguration build(ConstraintLocation parentConstraintLocation, Type enclosingType) {
		return build( ContainerElementTypePath.root(), parentConstraintLocation, enclosingType );
	}

	private ContainerElementTypeConfiguration build(ContainerElementTypePath parentConstraintElementTypePath,
			ConstraintLocation parentConstraintLocation, Type enclosingType) {
		return containerElementTypeStaxBuilders.stream()
				.map( builder -> builder.build( configuredPaths, parentConstraintElementTypePath, parentConstraintLocation, enclosingType ) )
				.reduce( ContainerElementTypeConfiguration.EMPTY_CONFIGURATION, ContainerElementTypeConfiguration::merge );
	}

	static class ContainerElementTypeConfiguration {

		public static final ContainerElementTypeConfiguration EMPTY_CONFIGURATION = new ContainerElementTypeConfiguration( Collections.emptySet(), Collections.emptyMap() );

		private final Set<MetaConstraint<?>> metaConstraints;

		private final Map<TypeVariable<?>, CascadingMetaDataBuilder> containerElementTypesCascadingMetaDataBuilder;

		ContainerElementTypeConfiguration(Set<MetaConstraint<?>> metaConstraints, Map<TypeVariable<?>, CascadingMetaDataBuilder> containerElementTypesCascadingMetaData) {
			this.metaConstraints = metaConstraints;
			this.containerElementTypesCascadingMetaDataBuilder = containerElementTypesCascadingMetaData;
		}

		public Set<MetaConstraint<?>> getMetaConstraints() {
			return metaConstraints;
		}

		public Map<TypeVariable<?>, CascadingMetaDataBuilder> getTypeParametersCascadingMetaData() {
			return containerElementTypesCascadingMetaDataBuilder;
		}

		public static ContainerElementTypeConfiguration merge(ContainerElementTypeConfiguration l, ContainerElementTypeConfiguration r) {
			return new ContainerElementTypeConfiguration(
					Stream.concat( l.metaConstraints.stream(), r.metaConstraints.stream() ).collect( Collectors.toSet() ),
					Stream.concat( l.containerElementTypesCascadingMetaDataBuilder.entrySet().stream(), r.containerElementTypesCascadingMetaDataBuilder.entrySet().stream() )
							.collect( Collectors.toMap( Map.Entry::getKey, Map.Entry::getValue ) )
			);
		}
	}
}
