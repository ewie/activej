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

package io.activej.common.initializer;

import java.util.function.Consumer;

/**
 * An interface that marks a class as initializable
 *
 * @param <T> a type of initializable object (Self type)
 */
@SuppressWarnings("unchecked")
public interface WithInitializer<T extends WithInitializer<T>> {

	/**
	 * Initializes an object by applying an initializing consumer
	 *
	 * @param initializer an initializing consumer
	 * @return this same object
	 */
	default T withInitializer(Consumer<? super T> initializer) {
		initializer.accept((T) this);
		return (T) this;
	}

}
