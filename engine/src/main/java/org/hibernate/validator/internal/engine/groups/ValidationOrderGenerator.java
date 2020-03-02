/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.groups;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import jakarta.validation.GroupSequence;
import jakarta.validation.groups.Default;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Helper class used to order groups and sequences into the right order for validation.
 *
 * @author Hardy Ferentschik
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
public class ValidationOrderGenerator {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final ConcurrentMap<Class<?>, Sequence> resolvedSequences = new ConcurrentHashMap<Class<?>, Sequence>();

	/**
	 * Creates a {@link ValidationOrder} for the given validation group.
	 *
	 * @param group the group to get as order
	 * @param expand whether the given group should be expanded (i.e. flattened it
	 * to its members if it is a sequence or group extending another
	 * group) or not
	 *
	 * @return a {@link ValidationOrder} for the given validation group
	 */
	public ValidationOrder getValidationOrder(Class<?> group, boolean expand) {
		if ( Default.class.equals( group ) ) {
			return ValidationOrder.DEFAULT_GROUP;
		}

		if ( expand ) {
			return getValidationOrder( Collections.<Class<?>>singletonList( group ) );
		}
		else {
			DefaultValidationOrder validationOrder = new DefaultValidationOrder();
			validationOrder.insertGroup( new Group( group ) );
			return validationOrder;
		}
	}

	/**
	 * Generates a order of groups and sequences for the specified validation groups.
	 *
	 * @param groups the groups specified at the validation call
	 *
	 * @return an instance of {@code ValidationOrder} defining the order in which validation has to occur
	 */
	public ValidationOrder getValidationOrder(Collection<Class<?>> groups) {
		if ( groups == null || groups.size() == 0 ) {
			throw LOG.getAtLeastOneGroupHasToBeSpecifiedException();
		}

		// HV-621 - if we deal with the Default group we return the default ValidationOrder. No need to
		// process Default as other groups which saves several reflection calls (HF)
		if ( groups.size() == 1 && groups.contains( Default.class ) ) {
			return ValidationOrder.DEFAULT_GROUP;
		}

		for ( Class<?> clazz : groups ) {
			if ( !clazz.isInterface() ) {
				throw LOG.getGroupHasToBeAnInterfaceException( clazz );
			}
		}

		DefaultValidationOrder validationOrder = new DefaultValidationOrder();
		for ( Class<?> clazz : groups ) {
			if ( Default.class.equals( clazz ) ) { // HV-621
				validationOrder.insertGroup( Group.DEFAULT_GROUP );
			}
			else if ( isGroupSequence( clazz ) ) {
				insertSequence( clazz, clazz.getAnnotation( GroupSequence.class ).value(), true, validationOrder );
			}
			else {
				Group group = new Group( clazz );
				validationOrder.insertGroup( group );
				insertInheritedGroups( clazz, validationOrder );
			}
		}

		return validationOrder;
	}

	public ValidationOrder getDefaultValidationOrder(Class<?> clazz, List<Class<?>> defaultGroupSequence) {
		DefaultValidationOrder validationOrder = new DefaultValidationOrder();
		insertSequence( clazz, defaultGroupSequence.toArray( new Class<?>[defaultGroupSequence.size()] ), false, validationOrder );
		return validationOrder;
	}

	private boolean isGroupSequence(Class<?> clazz) {
		return clazz.getAnnotation( GroupSequence.class ) != null;
	}

	/**
	 * Recursively add inherited groups into the group chain.
	 *
	 * @param clazz the group interface
	 * @param chain the group chain we are currently building
	 */
	private void insertInheritedGroups(Class<?> clazz, DefaultValidationOrder chain) {
		for ( Class<?> inheritedGroup : clazz.getInterfaces() ) {
			Group group = new Group( inheritedGroup );
			chain.insertGroup( group );
			insertInheritedGroups( inheritedGroup, chain );
		}
	}

	private void insertSequence(Class<?> sequenceClass, Class<?>[] sequenceElements, boolean cache, DefaultValidationOrder validationOrder) {
		Sequence sequence = cache ? resolvedSequences.get( sequenceClass ) : null;
		if ( sequence == null ) {
			sequence = resolveSequence( sequenceClass, sequenceElements, new ArrayList<Class<?>>() );
			// we expand the inherited groups only after we determined whether the sequence is expandable
			sequence.expandInheritedGroups();

			// cache already resolved sequences
			if ( cache ) {
				final Sequence cachedResolvedSequence = resolvedSequences.putIfAbsent( sequenceClass, sequence );
				if ( cachedResolvedSequence != null ) {
					sequence = cachedResolvedSequence;
				}
			}
		}
		validationOrder.insertSequence( sequence );
	}

	private Sequence resolveSequence(Class<?> sequenceClass, Class<?>[] sequenceElements, List<Class<?>> processedSequences) {
		if ( processedSequences.contains( sequenceClass ) ) {
			throw LOG.getCyclicDependencyInGroupsDefinitionException();
		}
		else {
			processedSequences.add( sequenceClass );
		}
		List<Group> resolvedSequenceGroups = new ArrayList<Group>();
		for ( Class<?> clazz : sequenceElements ) {
			if ( isGroupSequence( clazz ) ) {
				Sequence tmpSequence = resolveSequence( clazz, clazz.getAnnotation( GroupSequence.class ).value(), processedSequences );
				addGroups( resolvedSequenceGroups, tmpSequence.getComposingGroups() );
			}
			else {
				List<Group> list = new ArrayList<Group>();
				list.add( new Group( clazz ) );
				addGroups( resolvedSequenceGroups, list );
			}
		}
		return new Sequence( sequenceClass, resolvedSequenceGroups );
	}

	private void addGroups(List<Group> resolvedGroupSequence, List<Group> groups) {
		for ( Group tmpGroup : groups ) {
			if ( resolvedGroupSequence.contains( tmpGroup ) && resolvedGroupSequence.indexOf( tmpGroup ) < resolvedGroupSequence
					.size() - 1 ) {
				throw LOG.getUnableToExpandGroupSequenceException();
			}
			resolvedGroupSequence.add( tmpGroup );
		}
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "ValidationOrderGenerator" );
		sb.append( "{resolvedSequences=" ).append( resolvedSequences );
		sb.append( '}' );
		return sb.toString();
	}
}
