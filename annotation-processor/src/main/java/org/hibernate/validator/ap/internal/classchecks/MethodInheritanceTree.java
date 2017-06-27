/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.internal.classchecks;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.ExecutableElement;

import org.hibernate.validator.ap.internal.util.CollectionHelper;

/**
 * Represents an inheritance tree of overridden methods. In the head of the tree a node from which we start looking for
 * overridden methods is located. Also contains some useful methods to walk around overridden methods.
 *
 * @author Marko Bekhta
 * @author Guillaume Smet
 */
public class MethodInheritanceTree {

	private final MethodNode rootMethodNode;

	private final Map<ExecutableElement, MethodNode> methodNodeMapping;

	private final Set<ExecutableElement> topLevelMethods;

	private final Set<ExecutableElement> overriddenMethods;

	/**
	 * Initializes a {@link MethodInheritanceTree}.
	 *
	 * @param rootMethodNode the root method {@link MethodNode}
	 * @param nodeMapping the mapping between the method represented by an {@link ExecutableElement} and the corresponding {@link MethodNode}
	 */
	private MethodInheritanceTree(MethodNode rootMethodNode, Map<ExecutableElement, MethodNode> nodeMapping) {
		this.rootMethodNode = rootMethodNode;
		this.methodNodeMapping = Collections.unmodifiableMap( nodeMapping );
		this.topLevelMethods = buildTopLevelMethodSet();
		this.overriddenMethods = buildOverriddenMethodSet();
	}

	/**
	 * Checks if there are any overridden methods in the hierarchy.
	 *
	 * @return {@code true} if there are any overridden methods found, {@code false} otherwise
	 */
	public boolean hasOverriddenMethods() {
		return overriddenMethods.size() > 0;
	}

	/**
	 * Returns a set containing all the methods of the hierarchy.
	 *
	 * @return a set containing all the methods of the hierarchy
	 */
	public Set<ExecutableElement> getAllMethods() {
		return Collections.unmodifiableSet( methodNodeMapping.keySet() );
	}

	/**
	 * Returns a set containing all the overridden methods.
	 *
	 * @return a set containing all the overridden methods
	 */
	public Set<ExecutableElement> getOverriddenMethods() {
		return overriddenMethods;
	}

	/**
	 * Checks if there are any parallel definitions of the method in the hierarchy.
	 *
	 * @return {@code true} if there are any parallel definitions of the method in the hierarchy, {@code false} otherwise
	 */
	public boolean hasParallelDefinitions() {
		return topLevelMethods.size() > 1;
	}

	/**
	 * Returns a set containing all the top level overridden methods.
	 *
	 * @return a set containing all the top level overridden methods
	 */
	public Set<ExecutableElement> getTopLevelMethods() {
		return topLevelMethods;
	}

	private Set<ExecutableElement> buildOverriddenMethodSet() {
		Set<ExecutableElement> overriddenMethods = CollectionHelper.newHashSet();
		for ( ExecutableElement method : methodNodeMapping.keySet() ) {
			if ( !rootMethodNode.getMethod().equals( method ) ) {
				overriddenMethods.add( method );
			}
		}
		return Collections.unmodifiableSet( overriddenMethods );
	}

	private Set<ExecutableElement> buildTopLevelMethodSet() {
		Set<ExecutableElement> methods = CollectionHelper.newHashSet();
		for ( MethodNode methodNode : methodNodeMapping.values() ) {
			if ( !methodNode.isOverriding() ) {
				methods.add( methodNode.getMethod() );
			}
		}
		return Collections.unmodifiableSet( methods );
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder()
				.append( "MethodInheritanceTree [" )
				.append( "rootMethodNode=" ).append( rootMethodNode )
				.append( "]" );

		return sb.toString();
	}

	public static class Builder {

		private final MethodNode rootMethodNode;

		private final Map<ExecutableElement, MethodNode> nodeMapping = CollectionHelper.newHashMap();

		public Builder(ExecutableElement rootMethod) {
			rootMethodNode = new MethodNode( rootMethod );
			nodeMapping.put( rootMethod, rootMethodNode );
		}

		public void addOverriddenMethod(ExecutableElement overridingMethod, ExecutableElement overriddenMethod) {
			MethodNode overriddenMethodNode = new MethodNode( overriddenMethod );
			nodeMapping.get( overridingMethod ).addOverriddenMethodNode( overriddenMethodNode );
			nodeMapping.put( overriddenMethod, overriddenMethodNode );
		}

		public MethodInheritanceTree build() {
			return new MethodInheritanceTree( rootMethodNode, nodeMapping );
		}
	}

	/**
	 * Node of the tree that contains information about the method, its enclosing type and its overridden methods.
	 */
	private static class MethodNode {
		private ExecutableElement method;
		private Set<MethodNode> overriddenMethodNodes;

		private MethodNode(ExecutableElement method) {
			this.method = method;
			this.overriddenMethodNodes = CollectionHelper.newHashSet();
		}

		private boolean isOverriding() {
			return !overriddenMethodNodes.isEmpty();
		}

		private ExecutableElement getMethod() {
			return method;
		}

		private void addOverriddenMethodNode(MethodNode overriddenMethodNode) {
			overriddenMethodNodes.add( overriddenMethodNode );
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder()
					.append( "MethodNode [" )
					.append( "method=" ).append( method )
					.append( "]" );

			return sb.toString();
		}
	}
}
