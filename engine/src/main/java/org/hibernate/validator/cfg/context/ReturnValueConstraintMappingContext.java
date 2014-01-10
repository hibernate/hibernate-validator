/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hibernate.validator.cfg.context;

/**
 * Constraint mapping creational context representing a method return value. Allows
 * to place constraints on the return value, mark it as cascadable and to
 * navigate to other constraint targets.
 *
 * @author Gunnar Morling
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
 */
public interface ReturnValueConstraintMappingContext
		extends TypeTarget, ParameterTarget, CrossParameterTarget, ConstructorTarget, MethodTarget, Constrainable<ReturnValueConstraintMappingContext>, Cascadable<ReturnValueConstraintMappingContext>, Unwrapable<ReturnValueConstraintMappingContext> {

}
