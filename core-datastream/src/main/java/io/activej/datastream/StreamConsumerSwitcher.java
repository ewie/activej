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

package io.activej.datastream;

import io.activej.common.initializer.WithInitializer;
import io.activej.promise.Promise;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

import static io.activej.common.Checks.checkState;

/**
 * A consumer that wraps around another consumer that can be hot swapped with some other consumer.
 * <p>
 * It sets its acknowledgement on supplier end of stream, and acts as if suspended when current consumer stops and acknowledges.
 */
public final class StreamConsumerSwitcher<T> extends AbstractStreamConsumer<T> implements WithInitializer<StreamConsumerSwitcher<T>> {
	private InternalSupplier internalSupplier = new InternalSupplier();
	private final Set<InternalSupplier> pendingAcknowledgements = new HashSet<>();

	private StreamConsumerSwitcher() {
	}

	/**
	 * Creates a new instance of this consumer.
	 */
	public static <T> StreamConsumerSwitcher<T> create() {
		return new StreamConsumerSwitcher<>();
	}

	public Promise<Void> switchTo(@NotNull StreamConsumer<T> consumer) {
		checkState(!isComplete());
		checkState(!isEndOfStream());
		assert this.internalSupplier != null;

		InternalSupplier internalSupplierOld = this.internalSupplier;
		InternalSupplier internalSupplierNew = new InternalSupplier();

		this.internalSupplier = internalSupplierNew;
		internalSupplierNew.streamTo(consumer);

		internalSupplierOld.sendEndOfStream();

		return internalSupplierNew.getAcknowledgement();
	}

	public int getPendingAcknowledgements() {
		return pendingAcknowledgements.size();
	}

	@Override
	protected void onStarted() {
		resume(internalSupplier.getDataAcceptor());
	}

	@Override
	protected void onEndOfStream() {
		internalSupplier.sendEndOfStream();
	}

	@Override
	protected void onError(Exception e) {
		internalSupplier.closeEx(e);
		for (InternalSupplier pendingAcknowledgement : pendingAcknowledgements) {
			pendingAcknowledgement.getConsumer().closeEx(e);
		}
	}

	@Override
	protected void onCleanup() {
		internalSupplier = null;
		pendingAcknowledgements.clear();
	}

	private class InternalSupplier extends AbstractStreamSupplier<T> {
		@Override
		protected void onStarted() {
			pendingAcknowledgements.add(this);
		}

		@Override
		protected void onResumed() {
			if (StreamConsumerSwitcher.this.internalSupplier == this) {
				StreamConsumerSwitcher.this.resume(getDataAcceptor());
			}
		}

		@Override
		protected void onSuspended() {
			if (StreamConsumerSwitcher.this.internalSupplier == this) {
				StreamConsumerSwitcher.this.suspend();
			}
		}

		@Override
		protected void onAcknowledge() {
			pendingAcknowledgements.remove(this);
			if (pendingAcknowledgements.isEmpty()) {
				StreamConsumerSwitcher.this.acknowledge();
			}
		}

		@Override
		protected void onError(Exception e) {
			StreamConsumerSwitcher.this.closeEx(e);
		}
	}
}
