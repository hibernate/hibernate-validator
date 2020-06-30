/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.messageinterpolation;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMock;
import org.hibernate.validator.internal.engine.messageinterpolation.ParameterTermResolver;
import org.hibernate.validator.messageinterpolation.HibernateMessageInterpolatorContext;
import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import jakarta.validation.MessageInterpolator;
import jakarta.validation.metadata.ConstraintDescriptor;

/**
 * Test for {@link org.hibernate.validator.internal.engine.messageinterpolation.ParameterTermResolver}
 *
 * @author Alexander Gatsenko
 */
public class ParameterTermResolverTest {

	private final ParameterTermResolver resolver = new ParameterTermResolver();

	@DataProvider(name = "interpolateByNotArrayValueArgs")
	public static Object[][] interpolateByNotArrayValueArgs() {
		// lines of (String variableName, Object variableValue, String expectedResolvedExpression)
		return new Object[][] {
				{ "value", null, "{value}" },
				{ "value", true, "true" },
				{ "value", false, "false" },
				{ "value", 'a', "a" },
				{ "value", (byte) 10, "10" },
				{ "value", (short) 10, "10" },
				{ "value", 10, "10" },
				{ "value", 10L, "10" },
				{ "value", 10.1, "10.1" },
				{ "value", 10.1f, "10.1" },
				{ "value", "string value", "string value" },
		};
	}

	@DataProvider(name = "interpolateByArrayValueArgs")
	public static Object[][] interpolateByArrayValueArgs() {
		// lines of (String variableName, <Array as Object> variableValueArray, String expectedResolvedExpression)
		return new Object[][] {
				{ "value", new boolean[] { true, false }, Arrays.toString( new boolean[] { true, false } ) },
				{ "value", new char[] { 'a', 'b' }, Arrays.toString( new char[] { 'a', 'b' } ) },
				{ "value", new byte[] { 1, 2 }, Arrays.toString( new byte[] { 1, 2 } ) },
				{ "value", new short[] { 1, 2 }, Arrays.toString( new short[] { 1, 2 } ) },
				{ "value", new int[] { 1, 2 }, Arrays.toString( new int[] { 1, 2 } ) },
				{ "value", new long[] { 1, 2 }, Arrays.toString( new long[] { 1, 2 } ) },
				{ "value", new double[] { 1.2, 3.4 }, Arrays.toString( new double[] { 1.2, 3.4 } ) },
				{ "value", new float[] { 1.2F, 3.4F }, Arrays.toString( new float[] { 1.2F, 3.4F } ) },
				{ "value", new String[] { "one", "two" }, Arrays.toString( new String[] { "one", "two" } ) },
		};
	}

	@Test(dataProvider = "interpolateByNotArrayValueArgs")
	public void testInterpolateShouldResolveExpressionByNotArrayValue(
			String variableName,
			Object variableValue,
			String expectedResolvedExpression) {
		final MessageInterpolator.Context context = createHibernateContextWithConstraintDescriptorAttr(
				variableName,
				variableValue
		);
		final String srcExpression = createVariableExpression( variableName );

		final String actualResolvedExpression = resolver.interpolate( context, srcExpression );
		assertEquals( actualResolvedExpression, expectedResolvedExpression );
	}

	@Test(dataProvider = "interpolateByArrayValueArgs")
	@TestForIssue(jiraKey = "HV-1761")
	public void testInterpolateShouldResolveExpressionByArrayValue(
			String variableName,
			Object variableValueArray,
			String expectedResolvedExpression) {
		final MessageInterpolator.Context context = createHibernateContextWithConstraintDescriptorAttr(
				variableName,
				variableValueArray
		);
		final String srcExpression = createVariableExpression( variableName );

		final String actualResolvedExpression = resolver.interpolate( context, srcExpression );
		assertEquals( actualResolvedExpression, expectedResolvedExpression );
	}

	@Test(dataProvider = "interpolateByNotArrayValueArgs")
	public void testInterpolateShouldAllowUseNotHibernateContext(
			String variableName,
			Object variableValue,
			String expectedResolvedExpression) {
		final MessageInterpolator.Context context = createNotHibernateContextWithConstraintDescriptorAttr(
				variableName,
				variableValue
		);
		final String srcExpression = createVariableExpression( variableName );

		final String actualResolvedExpression = resolver.interpolate( context, srcExpression );
		assertEquals( actualResolvedExpression, expectedResolvedExpression );
	}

	private static String createVariableExpression(String variableName) {
		return String.format( "{%s}", variableName );
	}

	private static Map<String, Object> createConstraintDescriptorAttr(String attrName, Object attrValue) {
		Map<String, Object> map = new HashMap<>( 1 );
		map.put( attrName, attrValue );
		return map;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static MessageInterpolator.Context createNotHibernateContextWithConstraintDescriptorAttr(
			String attrName,
			Object attrValue) {
		final Map<String, Object> attrs = createConstraintDescriptorAttr( attrName, attrValue );

		final MessageInterpolator.Context context = EasyMock.mock( MessageInterpolator.Context.class );
		final ConstraintDescriptor constraintDescriptor = EasyMock.mock( ConstraintDescriptor.class );

		EasyMock.expect( context.getConstraintDescriptor() ).andStubReturn( constraintDescriptor );
		EasyMock.expect( constraintDescriptor.getAttributes() ).andStubReturn( attrs );

		EasyMock.replay( context );
		EasyMock.replay( constraintDescriptor );

		return context;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static MessageInterpolator.Context createHibernateContextWithConstraintDescriptorAttr(
			String attrName,
			Object attrValue) {
		final Map<String, Object> attrs = createConstraintDescriptorAttr( attrName, attrValue );

		final HibernateMessageInterpolatorContext context = EasyMock.mock( HibernateMessageInterpolatorContext.class );
		final ConstraintDescriptor constraintDescriptor = EasyMock.mock( ConstraintDescriptor.class );

		EasyMock.expect( context.getMessageParameters() ).andStubReturn( attrs );
		EasyMock.expect( context.getConstraintDescriptor() ).andStubReturn( constraintDescriptor );
		EasyMock.expect( constraintDescriptor.getAttributes() ).andStubReturn( attrs );

		EasyMock.replay( context );
		EasyMock.replay( constraintDescriptor );

		return context;
	}
}
