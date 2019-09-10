/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.metadata.bytebuddy;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.hibernate.validator.engine.HibernateValidatorEnhancedBean;
import org.hibernate.validator.internal.util.StringHelper;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.loading.ByteArrayClassLoader;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.implementation.bytecode.assign.primitive.PrimitiveBoxingDelegate;
import net.bytebuddy.implementation.bytecode.assign.reference.ReferenceTypeAwareAssigner;
import net.bytebuddy.jar.asm.Label;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.jar.asm.Type;
import net.bytebuddy.matcher.ElementMatchers;
import org.testng.annotations.Test;

/**
 * Note that this implementation is not complete and is only for testing purposes.
 * <p>
 * It typically doesn't implement the support for instrumented parent classes.
 *
 * @author Marko Bekhta
 */
public class ByteBuddyWrapperTest {

	@Test
	public void testByteBuddy() throws Exception {
		Class<?> clazz = Foo.class;

		ClassLoader classLoader = new ByteArrayClassLoader(
				ClassLoadingStrategy.BOOTSTRAP_LOADER,
				ClassFileLocator.ForClassLoader.readToNames( Foo.class, HibernateValidatorEnhancedBean.class,
						MyContracts.class, StringHelper.class ) );

		Class<?> aClass = new ByteBuddy().rebase( clazz )
				.implement( HibernateValidatorEnhancedBean.class )
				.method(
						named( HibernateValidatorEnhancedBean.GET_FIELD_VALUE_METHOD_NAME )
								.and( ElementMatchers.takesArguments( String.class ) )
								.and( ElementMatchers.returns( Object.class ) )
				)
				.intercept( new Implementation.Simple( new GetFieldValue( clazz ) ) )
				.method(
						named( HibernateValidatorEnhancedBean.GET_GETTER_VALUE_METHOD_NAME )
								.and( ElementMatchers.takesArguments( String.class ) )
								.and( ElementMatchers.returns( Object.class ) )
				)
				.intercept( new Implementation.Simple( new GetGetterValue( clazz ) ) )
				.make()
				.load( classLoader, ClassLoadingStrategy.Default.INJECTION )
				.getLoaded();

		Object object = aClass.newInstance();

		Method getFieldValue = aClass.getMethod( HibernateValidatorEnhancedBean.GET_FIELD_VALUE_METHOD_NAME, String.class );

		assertThat( getFieldValue.invoke( object, "num" ) ).isEqualTo( -1 );
		assertThat( getFieldValue.invoke( object, "string" ) ).isEqualTo( "test" );
		assertThat( getFieldValue.invoke( object, "looooong" ) ).isEqualTo( 100L );

		Method getGetterValue = aClass.getMethod( HibernateValidatorEnhancedBean.GET_GETTER_VALUE_METHOD_NAME, String.class );

		assertThat( getGetterValue.invoke( object, "getMessage" ) ).isEqualTo( "messssssage" );
		assertThat( getGetterValue.invoke( object, "getKey" ) ).isEqualTo( false );
	}

	private static class GetFieldValue implements ByteCodeAppender {

		@SuppressWarnings("rawtypes")
		private final Class clazz;

		private final Field[] fields;

		@SuppressWarnings("rawtypes")
		public GetFieldValue(Class clazz) {
			this.clazz = clazz;
			this.fields = clazz.getDeclaredFields();
		}

		@Override
		public Size apply(MethodVisitor methodVisitor, Implementation.Context implementationContext, MethodDescription instrumentedMethod) {
			try {
				// Contracts.assertNotEmpty(propertyName, "Property cannot be blank");
				Label contractsPropertyNameCheckLabel = new Label();
				methodVisitor.visitLabel( contractsPropertyNameCheckLabel );
				methodVisitor.visitVarInsn( Opcodes.ALOAD, 1 );
				methodVisitor.visitLdcInsn( "Property cannot be blank" );
				methodVisitor.visitMethodInsn(
						Opcodes.INVOKESTATIC,
						Type.getType( MyContracts.class ).getInternalName(),
						"assertNotEmpty",
						Type.getType( MyContracts.class.getDeclaredMethod( "assertNotEmpty", String.class, String.class ) ).getDescriptor(),
						false
				);

				Label l1 = new Label();
				methodVisitor.visitLabel( l1 );
				int index = 0;
				for ( Field field : fields ) {
					String fieldName = field.getName();

					if ( index > 0 ) {
						methodVisitor.visitFrame( Opcodes.F_SAME, 0, null, 0, null );
					}

					//		if (propertyName.equals(field_name_goes_here)) {
					//			return field;
					//		}
					methodVisitor.visitVarInsn( Opcodes.ALOAD, 1 );
					methodVisitor.visitLdcInsn( fieldName );
					methodVisitor.visitMethodInsn(
							Opcodes.INVOKEVIRTUAL,
							Type.getType( String.class ).getInternalName(),
							"equals",
							Type.getType( String.class.getDeclaredMethod( "equals", Object.class ) ).getDescriptor(),
							false
					);

					Label ifCheckLabel = new Label();
					methodVisitor.visitJumpInsn( Opcodes.IFEQ, ifCheckLabel );

					Label returnFieldLabel = new Label();
					methodVisitor.visitLabel( returnFieldLabel );
					methodVisitor.visitVarInsn( Opcodes.ALOAD, 0 );
					methodVisitor.visitFieldInsn(
							Opcodes.GETFIELD,
							Type.getInternalName( clazz ),
							fieldName,
							Type.getDescriptor( field.getType() )
					);
					if ( field.getType().isPrimitive() ) {
						PrimitiveBoxingDelegate.forPrimitive( new TypeDescription.ForLoadedType( field.getType() ) )
								.assignBoxedTo(
										TypeDescription.Generic.OBJECT,
										ReferenceTypeAwareAssigner.INSTANCE,
										Assigner.Typing.STATIC
								)
								.apply( methodVisitor, implementationContext );
					}
					methodVisitor.visitInsn( Opcodes.ARETURN );
					methodVisitor.visitLabel( ifCheckLabel );

					index++;
				}

				// throw new IllegalArgumentException("No property was found for a given name");

				methodVisitor.visitFrame( Opcodes.F_SAME, 0, null, 0, null );
				methodVisitor.visitTypeInsn( Opcodes.NEW, Type.getInternalName( IllegalArgumentException.class ) );
				methodVisitor.visitInsn( Opcodes.DUP );
				methodVisitor.visitLdcInsn( "No property was found for a given name" );
				methodVisitor.visitMethodInsn(
						Opcodes.INVOKESPECIAL,
						Type.getInternalName( IllegalArgumentException.class ),
						"<init>",
						Type.getType( IllegalArgumentException.class.getDeclaredConstructor( String.class ) ).getDescriptor(),
						false
				);
				methodVisitor.visitInsn( Opcodes.ATHROW );

				Label label = new Label();
				methodVisitor.visitLabel( label );
				methodVisitor.visitLocalVariable(
						"this",
						Type.getDescriptor( clazz ),
						null,
						contractsPropertyNameCheckLabel,
						label,
						0
				);
				methodVisitor.visitLocalVariable(
						"name",
						Type.getDescriptor( String.class ),
						null,
						contractsPropertyNameCheckLabel,
						label,
						1
				);
				methodVisitor.visitMaxs( 3, 2 );

				return new Size( 6, instrumentedMethod.getStackSize() );
			}
			catch (NoSuchMethodException e) {
				throw new IllegalArgumentException( e );
			}
		}
	}

	private static class GetGetterValue implements ByteCodeAppender {

		@SuppressWarnings("rawtypes")
		private final Class clazz;

		private final Method[] methods;

		@SuppressWarnings("rawtypes")
		public GetGetterValue(Class clazz) {
			this.clazz = clazz;
			this.methods = clazz.getDeclaredMethods();
		}

		@Override
		public Size apply(MethodVisitor methodVisitor, Implementation.Context implementationContext, MethodDescription instrumentedMethod) {
			try {
				// Contracts.assertNotEmpty(propertyName, "Property cannot be blank");
				Label contractsPropertyNameCheckLabel = new Label();
				methodVisitor.visitLabel( contractsPropertyNameCheckLabel );
				methodVisitor.visitVarInsn( Opcodes.ALOAD, 1 );
				methodVisitor.visitLdcInsn( "Property cannot be blank" );
				methodVisitor.visitMethodInsn(
						Opcodes.INVOKESTATIC,
						Type.getType( MyContracts.class ).getInternalName(),
						"assertNotEmpty",
						Type.getType( MyContracts.class.getDeclaredMethod( "assertNotEmpty", String.class, String.class ) ).getDescriptor(),
						false
				);

				Label l1 = new Label();
				methodVisitor.visitLabel( l1 );
				int index = 0;
				for ( Method method : methods ) {
					String fieldName = method.getName();

					if ( index > 0 ) {
						methodVisitor.visitFrame( Opcodes.F_SAME, 0, null, 0, null );
					}

					//		if (propertyName.equals(field_name_goes_here)) {
					//			return field;
					//		}
					methodVisitor.visitVarInsn( Opcodes.ALOAD, 1 );
					methodVisitor.visitLdcInsn( fieldName );
					methodVisitor.visitMethodInsn(
							Opcodes.INVOKEVIRTUAL,
							Type.getType( String.class ).getInternalName(),
							"equals",
							Type.getType( String.class.getDeclaredMethod( "equals", Object.class ) ).getDescriptor(),
							false
					);

					Label ifCheckLabel = new Label();
					methodVisitor.visitJumpInsn( Opcodes.IFEQ, ifCheckLabel );

					Label returnFieldLabel = new Label();
					methodVisitor.visitLabel( returnFieldLabel );
					methodVisitor.visitVarInsn( Opcodes.ALOAD, 0 );
					methodVisitor.visitMethodInsn(
							Opcodes.INVOKEVIRTUAL,
							Type.getInternalName( clazz ),
							method.getName(),
							Type.getMethodDescriptor( method ),
							method.getDeclaringClass().isInterface()
					);
					if ( method.getReturnType().isPrimitive() ) {
						PrimitiveBoxingDelegate.forPrimitive( new TypeDescription.ForLoadedType( method.getReturnType() ) )
								.assignBoxedTo(
										TypeDescription.Generic.OBJECT,
										ReferenceTypeAwareAssigner.INSTANCE,
										Assigner.Typing.STATIC
								)
								.apply( methodVisitor, implementationContext );
					}
					methodVisitor.visitInsn( Opcodes.ARETURN );
					methodVisitor.visitLabel( ifCheckLabel );

					index++;
				}

				// throw new IllegalArgumentException("No property was found for a given name");

				methodVisitor.visitFrame( Opcodes.F_SAME, 0, null, 0, null );
				methodVisitor.visitTypeInsn( Opcodes.NEW, Type.getInternalName( IllegalArgumentException.class ) );
				methodVisitor.visitInsn( Opcodes.DUP );
				methodVisitor.visitLdcInsn( "No property was found for a given name" );
				methodVisitor.visitMethodInsn(
						Opcodes.INVOKESPECIAL,
						Type.getInternalName( IllegalArgumentException.class ),
						"<init>",
						Type.getType( IllegalArgumentException.class.getDeclaredConstructor( String.class ) ).getDescriptor(),
						false
				);
				methodVisitor.visitInsn( Opcodes.ATHROW );

				Label label = new Label();
				methodVisitor.visitLabel( label );
				methodVisitor.visitLocalVariable(
						"this",
						Type.getDescriptor( clazz ),
						null,
						contractsPropertyNameCheckLabel,
						label,
						0
				);
				methodVisitor.visitLocalVariable(
						"name",
						Type.getDescriptor( String.class ),
						null,
						contractsPropertyNameCheckLabel,
						label,
						1
				);
				methodVisitor.visitMaxs( 3, 2 );

				return new Size( 6, instrumentedMethod.getStackSize() );
			}
			catch (NoSuchMethodException e) {
				throw new IllegalArgumentException( e );
			}
		}
	}


	@SuppressWarnings("unused")
	public static class Foo {
		private String string;
		private Integer num;
		private long looooong;

		public Foo() {
			this( "test", -1 );
			this.looooong = 100L;
		}

		public Foo(String string, Integer num) {
			this.string = string;
			this.num = num;
		}

		public String getMessage() {
			return "messssssage";
		}

		public boolean getKey() {
			return false;
		}
	}

	@SuppressWarnings("unused")
	public static class Bar extends Foo {
		private final List<String> strings;

		public Bar() {
			super();
			this.strings = Collections.emptyList();
		}
	}

	public static final class MyContracts {
		public static void assertNotEmpty(String s, String message) {
			if ( StringHelper.isNullOrEmptyString( s ) ) {
				throw new IllegalArgumentException( message );
			}
		}
	}

}
