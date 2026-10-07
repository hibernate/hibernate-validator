/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.cdi.internal.injection;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.enterprise.inject.spi.InjectionTargetFactory;

import org.hibernate.validator.cdi.internal.DestructibleBeanInstance;

import org.testng.annotations.Test;

public class DestructibleBeanInstanceTest {

	@Test
	public void testReleaseAfterDestructionFailures() {
		Fixture fixture = new Fixture();
		IllegalStateException preDestroyFailure = new IllegalStateException( "preDestroy" );
		IllegalStateException disposeFailure = new IllegalStateException( "dispose" );
		expect( fixture.target.produce( fixture.context ) ).andReturn( fixture.instance );
		fixture.target.inject( fixture.instance, fixture.context );
		fixture.target.postConstruct( fixture.instance );
		fixture.target.preDestroy( fixture.instance );
		expectLastCall().andThrow( preDestroyFailure );
		fixture.target.dispose( fixture.instance );
		expectLastCall().andThrow( disposeFailure );
		fixture.context.release();
		fixture.replay();
		DestructibleBeanInstance<Object> bean = new DestructibleBeanInstance<>( fixture.manager, Object.class );
		assertThatThrownBy( bean::destroy ).isSameAs( preDestroyFailure ).hasSuppressedException( disposeFailure );
		fixture.verify();
	}

	@Test
	public void testReleaseAfterProductionFailure() {
		Fixture fixture = new Fixture();
		IllegalStateException failure = new IllegalStateException( "produce" );
		expect( fixture.target.produce( fixture.context ) ).andThrow( failure );
		fixture.context.release();
		fixture.replay();
		assertThatThrownBy( () -> new DestructibleBeanInstance<>( fixture.manager, Object.class ) ).isSameAs( failure );
		fixture.verify();
	}

	@Test
	public void testReleaseAfterInjectionFailure() {
		assertInjectionFailureCleanup( false );
	}

	@Test
	public void testReleaseAfterExistingInstanceInjectionFailure() {
		assertInjectionFailureCleanup( true );
	}

	private void assertInjectionFailureCleanup(boolean existingInstance) {
		Fixture fixture = new Fixture();
		IllegalStateException failure = new IllegalStateException( "inject" );
		if ( !existingInstance ) {
			expect( fixture.target.produce( fixture.context ) ).andReturn( fixture.instance );
		}
		fixture.target.inject( fixture.instance, fixture.context );
		expectLastCall().andThrow( failure );
		if ( !existingInstance ) {
			fixture.target.dispose( fixture.instance );
		}
		fixture.context.release();
		fixture.replay();
		assertThatThrownBy( () -> {
			if ( existingInstance ) {
				new DestructibleBeanInstance<>( fixture.manager, fixture.instance );
			}
			else {
				new DestructibleBeanInstance<>( fixture.manager, Object.class );
			}
		} ).isSameAs( failure );
		fixture.verify();
	}

	@SuppressWarnings("unchecked")
	private static class Fixture {
		final Object instance = new Object();
		final BeanManager manager = createMock( BeanManager.class );
		final AnnotatedType<Object> type = createMock( AnnotatedType.class );
		final InjectionTargetFactory<Object> factory = createMock( InjectionTargetFactory.class );
		final InjectionTarget<Object> target = createMock( InjectionTarget.class );
		final CreationalContext<Object> context = createMock( CreationalContext.class );

		Fixture() {
			expect( manager.createAnnotatedType( Object.class ) ).andReturn( type );
			expect( manager.getInjectionTargetFactory( type ) ).andReturn( factory );
			expect( factory.createInjectionTarget( null ) ).andReturn( target );
			expect( manager.createCreationalContext( null ) ).andReturn( context );
		}

		void replay() {
			org.easymock.EasyMock.replay( manager, type, factory, target, context );
		}

		void verify() {
			org.easymock.EasyMock.verify( manager, type, factory, target, context );
		}
	}
}
