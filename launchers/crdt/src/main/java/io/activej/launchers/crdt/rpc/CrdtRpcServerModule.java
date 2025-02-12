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

package io.activej.launchers.crdt.rpc;

import io.activej.async.service.EventloopTaskScheduler;
import io.activej.config.Config;
import io.activej.crdt.wal.WriteAheadLog;
import io.activej.eventloop.Eventloop;
import io.activej.inject.annotation.Eager;
import io.activej.inject.annotation.Provides;
import io.activej.inject.module.AbstractModule;
import io.activej.rpc.server.RpcRequestHandler;
import io.activej.rpc.server.RpcServer;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static io.activej.async.service.EventloopTaskScheduler.Schedule.ofInterval;
import static io.activej.config.Config.ofClassPathProperties;
import static io.activej.config.Config.ofSystemProperties;
import static io.activej.config.converter.ConfigConverters.ofEventloopTaskSchedule;
import static io.activej.config.converter.ConfigConverters.ofInetSocketAddress;

public abstract class CrdtRpcServerModule<K extends Comparable<K>, S> extends AbstractModule {
	public static final int DEFAULT_PORT = 9000;
	public static final String PROPERTIES_FILE = "crdt-rpc-server.properties";

	protected abstract List<Class<?>> getMessageTypes();

	@Provides
	Eventloop eventloop() {
		return Eventloop.create();
	}

	@Provides
	Config config() {
		return Config.create()
				.with("listenAddresses", Config.ofValue(ofInetSocketAddress(), new InetSocketAddress(DEFAULT_PORT)))
				.overrideWith(ofClassPathProperties(PROPERTIES_FILE, true))
				.overrideWith(ofSystemProperties("config"));
	}

	@Provides
	@Eager
	@SuppressWarnings("unchecked")
	RpcServer server(Eventloop eventloop, Map<Class<?>, RpcRequestHandler<?, ?>> handlers, Config config) {
		RpcServer server = RpcServer.create(eventloop)
				.withListenAddress(config.get(ofInetSocketAddress(), "listenAddresses"))
				.withMessageTypes(getMessageTypes());
		for (Map.Entry<Class<?>, RpcRequestHandler<?, ?>> entry : handlers.entrySet()) {
			server.withHandler((Class<Object>) entry.getKey(), (RpcRequestHandler<Object, Object>) entry.getValue());
		}
		return server;
	}

	@Provides
	@Eager
	EventloopTaskScheduler walFlushScheduler(Eventloop eventloop, WriteAheadLog<K, S> wal, Config config) {
		return EventloopTaskScheduler.create(eventloop, wal::flush)
				.withSchedule(config.get(ofEventloopTaskSchedule(), "flush.schedule", ofInterval(Duration.ofMinutes(1))))
				.withInitialDelay(Duration.ofMinutes(1));
	}
}
