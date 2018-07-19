/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.provider.proeprtyholder;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import javax.validation.ValidationException;

import org.hibernate.validator.cfg.AnnotationDef;
import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.cfg.defs.EmailDef;
import org.hibernate.validator.cfg.defs.MinDef;
import org.hibernate.validator.cfg.defs.NotNullDef;
import org.hibernate.validator.cfg.defs.SizeDef;
import org.hibernate.validator.internal.metadata.aggregated.CascadingMetaDataBuilder;
import org.hibernate.validator.internal.metadata.core.MetaConstraintBuilder;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.provider.PropertyHolderMetaDataProvider;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.propertyholder.ConstrainedPropertyHolderElementBuilder;
import org.hibernate.validator.internal.metadata.raw.propertyholder.PropertyHolderConfiguration;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.annotation.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredMethodHandle;

/**
 * A dummy metadata provider just for testing purposes. To be removed compeletely later.
 *
 * @author Marko Bekhta
 */
public class DummyPropertyHolderMetaDataProvider implements PropertyHolderMetaDataProvider {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private static final MethodHandle CREATE_ANNOTATION_DESCRIPTOR_METHOD_HANDLE =
			run( GetDeclaredMethodHandle.andMakeAccessible( MethodHandles.lookup(), AnnotationDef.class, "createAnnotationDescriptor" ) );

	public static final String USER_MAPPING_NAME = "user";
	public static final String ADDRESS_MAPPING_NAME = "address";

	@Override
	public Optional<PropertyHolderConfiguration> getBeanConfiguration(String mappingName) {
		switch ( mappingName ) {
			case USER_MAPPING_NAME:
				return Optional.of( user() );
			case ADDRESS_MAPPING_NAME:
				return Optional.of( address() );
			default:
				return Optional.empty();
		}
	}

	private static PropertyHolderConfiguration user() {
		return new PropertyHolderConfiguration(
				ConfigurationSource.API,
				USER_MAPPING_NAME,
				CollectionHelper.asSet(
						new ConstrainedPropertyHolderElementBuilder(
								ConfigurationSource.API,
								"name",
								String.class,
								CollectionHelper.asSet(
										new MetaConstraintBuilder(
												createAnnotationDescriptor( new NotNullDef() ),
												ConstraintLocation.Builder.forPropertyHolderProperty()
										),
										new MetaConstraintBuilder(
												createAnnotationDescriptor( new SizeDef().min( 5 ).max( 10 ) ),
												ConstraintLocation.Builder.forPropertyHolderProperty()
										)
								),
								Collections.emptySet(),
								CascadingMetaDataBuilder.nonCascading()
						),
						new ConstrainedPropertyHolderElementBuilder(
								ConfigurationSource.API,
								"email",
								String.class,
								CollectionHelper.asSet(
										new MetaConstraintBuilder(
												createAnnotationDescriptor( new NotNullDef() ),
												ConstraintLocation.Builder.forPropertyHolderProperty()
										),
										new MetaConstraintBuilder(
												createAnnotationDescriptor( new EmailDef() ),
												ConstraintLocation.Builder.forPropertyHolderProperty()
										)
								),
								Collections.emptySet(),
								CascadingMetaDataBuilder.nonCascading()
						),
						new ConstrainedPropertyHolderElementBuilder(
								ConfigurationSource.API,
								"address",
								Map.class, // TODO: note this won't work need to handle cascading differently
								Collections.emptySet(),
								Collections.emptySet(),
								// TODO: might require a builder over it to collect info and then when we know the property hodler class we
								// could build the CascadingMetaDataBuilder from it.
								CascadingMetaDataBuilder.propertyHolder(
										USER_MAPPING_NAME,
										true,
										Collections.emptyMap(),
										Collections.emptyMap()
								)
						)
				),
				Collections.emptyList()
		);
	}

	private static PropertyHolderConfiguration address() {
		return new PropertyHolderConfiguration(
				ConfigurationSource.API,
				USER_MAPPING_NAME,
				CollectionHelper.asSet(
						new ConstrainedPropertyHolderElementBuilder(
								ConfigurationSource.API,
								"street",
								String.class,
								CollectionHelper.asSet(
										new MetaConstraintBuilder(
												createAnnotationDescriptor( new NotNullDef() ),
												ConstraintLocation.Builder.forPropertyHolderProperty()
										),
										new MetaConstraintBuilder(
												createAnnotationDescriptor( new SizeDef().min( 5 ).max( 10 ) ),
												ConstraintLocation.Builder.forPropertyHolderProperty()
										)
								),
								Collections.emptySet(),
								CascadingMetaDataBuilder.nonCascading()
						),
						new ConstrainedPropertyHolderElementBuilder(
								ConfigurationSource.API,
								"buildingNumber",
								Long.class,
								CollectionHelper.asSet(
										new MetaConstraintBuilder(
												createAnnotationDescriptor( new NotNullDef() ),
												ConstraintLocation.Builder.forPropertyHolderProperty()
										),
										new MetaConstraintBuilder(
												createAnnotationDescriptor( new MinDef().value( 0 ) ),
												ConstraintLocation.Builder.forPropertyHolderProperty()
										)
								),
								Collections.emptySet(),
								CascadingMetaDataBuilder.nonCascading()
						)
				),
				Collections.emptyList()
		);
	}

	private static <A extends Annotation> ConstraintAnnotationDescriptor<A> createAnnotationDescriptor(ConstraintDef<?, A> constraint) {
		try {
			@SuppressWarnings("unchecked")
			AnnotationDescriptor<A> annotationDescriptor = (AnnotationDescriptor<A>) CREATE_ANNOTATION_DESCRIPTOR_METHOD_HANDLE.invoke( constraint );
			return new ConstraintAnnotationDescriptor<>( annotationDescriptor );
		}
		catch (Throwable e) {
			if ( e instanceof ValidationException ) {
				throw (ValidationException) e;
			}
			throw LOG.getUnableToCreateAnnotationDescriptor( constraint.getClass(), e );
		}
	}

	/**
	 * Runs the given privileged action, using a privileged block if required.
	 * <b>NOTE:</b> This must never be changed into a publicly available method to avoid execution of arbitrary
	 * privileged actions within HV's protection domain.
	 */
	private static <V> V run(PrivilegedAction<V> action) {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
	}
}
