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

package io.activej.common.exception;

import io.activej.common.ApplicationSettings;
import org.jetbrains.annotations.NotNull;

/**
 * A wrapper exception that wraps checked exceptions into a runtime exception,
 * so that it could be thrown from a non-throwing call sites.
 * <p>
 * If this exception is thrown, it should be properly handled later on,
 * propagating a cause of {@link UncheckedException} based on application logic
 */
public final class UncheckedException extends RuntimeException {
	public static final boolean WITH_STACK_TRACE = ApplicationSettings.getBoolean(UncheckedException.class, "withStackTrace", false);

	private UncheckedException(@NotNull Exception cause) {
		super(cause);
	}

	public static UncheckedException of(@NotNull Exception cause) {
		if (cause.getClass() == UncheckedException.class) {
			return (UncheckedException) cause;
		}
		return new UncheckedException(cause);
	}

	@Override
	public Throwable fillInStackTrace() {
		return WITH_STACK_TRACE ? super.fillInStackTrace() : this;
	}

	@Override
	public Exception getCause() {
		return (Exception) super.getCause();
	}
}
