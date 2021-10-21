/*
 * Copyright (C) 2020 ActiveJ LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.activej.serializer.impl;

import io.activej.codegen.expression.Expression;
import io.activej.serializer.CompatibilityLevel;
import io.activej.serializer.SerializerDef;
import org.jetbrains.annotations.NotNull;

import java.util.function.UnaryOperator;

import static io.activej.codegen.expression.Expressions.*;

public class SerializerDefRegularCollection extends AbstractSerializerDefCollection {

	public SerializerDefRegularCollection(SerializerDef valueSerializer, Class<?> encodeType, Class<?> decodeType) {
		this(valueSerializer, encodeType, decodeType, Object.class, false);
	}

	protected SerializerDefRegularCollection(SerializerDef valueSerializer, Class<?> encodeType, Class<?> decodeType, @NotNull Class<?> elementType, boolean nullable) {
		super(valueSerializer, encodeType, decodeType, elementType, nullable);
	}

	@Override
	protected @NotNull SerializerDef doEnsureNullable(CompatibilityLevel compatibilityLevel) {
		return new SerializerDefRegularCollection(valueSerializer, encodeType, decodeType, elementType, true);
	}

	@Override
	protected @NotNull Expression doIterate(Expression collection, UnaryOperator<Expression> action) {
		return iterateIterable(collection, action);
	}

	@Override
	protected @NotNull Expression createBuilder(Expression length) {
		return constructor(decodeType, length);
	}

	@Override
	protected @NotNull Expression addToBuilder(Expression builder, Expression index, Expression element) {
		return call(builder, "add", element);
	}

	@Override
	protected @NotNull Expression build(Expression builder) {
		return builder;
	}
}