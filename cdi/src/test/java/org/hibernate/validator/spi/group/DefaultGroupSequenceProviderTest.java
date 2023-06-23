/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.spi.group;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.group.GroupSequenceProvider;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * DefaultGroupSequenceProviderTest.
 *
 * @author ilikly
 */
public class DefaultGroupSequenceProviderTest {

    @Test
    public void withoutClassParam () {
        Assert.assertThrows (() -> {
            final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory ();
            final Validator validator = validatorFactory.getValidator ();
            validator.validate (new A1 ());
        });
    }

    @Test
    public void withClassParam () {
        final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory ();
        final Validator validator = validatorFactory.getValidator ();
        validator.validate (new A2 ());
    }


    public static class DefaultGroupSequenceProvider1 implements DefaultGroupSequenceProvider<Object> {

        @Override
        public List<Class<?>> getValidationGroups (Object object) {
            List<Class<?>> groups = new ArrayList<> ();
            if (Objects.nonNull (object)) {
                groups.add (object.getClass ());
            }
            return groups;
        }
    }


    public static class DefaultGroupSequenceProvider2 implements DefaultGroupSequenceProvider<Object> {

        @Override
        public List<Class<?>> getValidationGroups (Class<?> clazz, Object object) {
            List<Class<?>> groups = new ArrayList<> ();
            if (Objects.nonNull (clazz)) {
                groups.add (clazz);
            }
            return groups;
        }

        @Override
        public List<Class<?>> getValidationGroups (Object object) {
            throw new IllegalArgumentException ("");
        }
    }


    @GroupSequenceProvider(DefaultGroupSequenceProvider1.class)
    public static class A1 {
        @NotNull
        private String name;

        public String getName () {
            return name;
        }

        public void setName (String name) {
            this.name = name;
        }
    }

    @GroupSequenceProvider(DefaultGroupSequenceProvider1.class)
    public static class B1 {
        @NotNull
        private String name;

        public String getName () {
            return name;
        }

        public void setName (String name) {
            this.name = name;
        }
    }

    @GroupSequenceProvider(DefaultGroupSequenceProvider2.class)
    public static class A2 {
        @NotNull
        private String name;

        public String getName () {
            return name;
        }

        public void setName (String name) {
            this.name = name;
        }
    }

    @GroupSequenceProvider(DefaultGroupSequenceProvider2.class)
    public static class B2 {
        @NotNull
        private String name;

        public String getName () {
            return name;
        }

        public void setName (String name) {
            this.name = name;
        }
    }

}
