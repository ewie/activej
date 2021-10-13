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
import io.activej.codegen.expression.Variable;
import io.activej.serializer.CompatibilityLevel;
import io.activej.serializer.SerializerDef;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

import static io.activej.codegen.expression.Expressions.*;

public final class SerializerDefGenericMap extends AbstractSerializerDefMap {
	public SerializerDefGenericMap(SerializerDef keySerializer, SerializerDef valueSerializer) {
		this(keySerializer, valueSerializer, false);
	}

	private SerializerDefGenericMap(SerializerDef keySerializer, SerializerDef valueSerializer, boolean nullable) {
		super(keySerializer, valueSerializer, Map.class, Map.class, Object.class, Object.class, nullable);
	}

	@Override
	protected Expression createConstructor(Expression length) {
		Class<?> rawType = keySerializer.getDecodeType();
		if (rawType.isEnum()) {
			return constructor(EnumMap.class, value(rawType));
		}
		return constructor(HashMap.class, initialSize(length));
	}

	@Override
	protected @NotNull Expression doDecode(StaticDecoders staticDecoders, Expression in, int version, CompatibilityLevel compatibilityLevel, Variable length) {
		return ifThenElse(cmpEq(length, value(0)),
				staticCall(Collections.class, "emptyMap"),
				ifThenElse(cmpEq(length, value(1)),
						staticCall(Collections.class, "singletonMap",
								keySerializer.defineDecoder(staticDecoders, in, version, compatibilityLevel),
								valueSerializer.defineDecoder(staticDecoders, in, version, compatibilityLevel)),
						super.doDecode(staticDecoders, in, version, compatibilityLevel, length)));
	}

	@Override
	public Expression mapForEach(Expression collection, UnaryOperator<Expression> forEachKey, UnaryOperator<Expression> forEachValue) {
		return forEach(collection, forEachKey, forEachValue);
	}

	@Override
	protected SerializerDef doEnsureNullable(CompatibilityLevel compatibilityLevel) {
		return new SerializerDefGenericMap(keySerializer, valueSerializer, true);
	}
}