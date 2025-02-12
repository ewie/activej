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

package io.activej.crdt.wal;

import io.activej.async.service.EventloopService;
import io.activej.common.initializer.WithInitializer;
import io.activej.crdt.CrdtData;
import io.activej.crdt.function.CrdtFunction;
import io.activej.crdt.primitives.CrdtType;
import io.activej.crdt.storage.CrdtStorage;
import io.activej.datastream.StreamSupplier;
import io.activej.eventloop.Eventloop;
import io.activej.promise.Promise;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.TreeMap;

import static io.activej.async.util.LogUtils.Level.INFO;
import static io.activej.async.util.LogUtils.toLogger;

public class InMemoryWriteAheadLog<K extends Comparable<K>, S> implements WriteAheadLog<K, S>, EventloopService,
		WithInitializer<InMemoryWriteAheadLog<K, S>> {
	private static final Logger logger = LoggerFactory.getLogger(InMemoryWriteAheadLog.class);

	private Map<K, S> map = new TreeMap<>();

	private final Eventloop eventloop;
	private final CrdtFunction<S> function;
	private final CrdtStorage<K, S> storage;

	private InMemoryWriteAheadLog(Eventloop eventloop, CrdtFunction<S> function, CrdtStorage<K, S> storage) {
		this.eventloop = eventloop;
		this.function = function;
		this.storage = storage;
	}

	public static <K extends Comparable<K>, S> InMemoryWriteAheadLog<K, S> create(Eventloop eventloop, CrdtFunction<S> function, CrdtStorage<K, S> storage) {
		return new InMemoryWriteAheadLog<>(eventloop, function, storage);
	}

	public static <K extends Comparable<K>, S extends CrdtType<S>> InMemoryWriteAheadLog<K, S> create(Eventloop eventloop, CrdtStorage<K, S> storage) {
		return new InMemoryWriteAheadLog<>(eventloop, CrdtFunction.ofCrdtType(), storage);
	}

	@Override
	public Promise<Void> put(K key, S value) {
		if (logger.isTraceEnabled()) {
			logger.trace("{} value for key {}", map.containsKey(key) ? "Merging" : "Putting new", key);
		}
		map.merge(key, value, function::merge);
		return Promise.complete();
	}

	@Override
	public Promise<Void> flush() {
		if (map.isEmpty()) {
			logger.info("Nothing to flush");
			return Promise.complete();
		}

		Map<K, S> map = this.map;
		this.map = new TreeMap<>();

		return storage.upload()
				.then(consumer -> StreamSupplier.ofStream(map.entrySet().stream()
								.map(entry -> new CrdtData<>(entry.getKey(), entry.getValue())))
						.streamTo(consumer))
				.whenException(e -> map.forEach(this::put))
				.whenComplete(toLogger(logger, INFO, INFO, "flush", map.size()));
	}

	@Override
	public @NotNull Eventloop getEventloop() {
		return eventloop;
	}

	@Override
	public @NotNull Promise<?> start() {
		return Promise.complete();
	}

	@Override
	public @NotNull Promise<?> stop() {
		return flush();
	}
}
