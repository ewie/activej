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

package io.activej.http.session;

import io.activej.promise.Promise;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

/**
 * Basic session storage for the {@link SessionServlet}.
 */
public interface SessionStore<T> {
	Promise<Void> save(String sessionId, T sessionObject);

	Promise<@Nullable T> get(String sessionId);

	Promise<Void> remove(String sessionId);

	default @Nullable Duration getSessionLifetimeHint() {
		return null;
	}
}
