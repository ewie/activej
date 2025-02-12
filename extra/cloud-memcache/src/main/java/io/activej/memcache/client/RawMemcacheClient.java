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

package io.activej.memcache.client;

import io.activej.common.initializer.WithInitializer;
import io.activej.memcache.protocol.MemcacheRpcMessage.Slice;
import io.activej.rpc.client.IRpcClient;

public class RawMemcacheClient extends AbstractMemcacheClient<byte[], Slice> implements WithInitializer<RawMemcacheClient> {
	private RawMemcacheClient(IRpcClient rpcClient) {
		super(rpcClient);
	}

	public static RawMemcacheClient create(IRpcClient rpcClient) {
		return new RawMemcacheClient(rpcClient);
	}

	@Override
	protected byte[] encodeKey(byte[] key) {
		return key;
	}

	@Override
	protected Slice encodeValue(Slice value) {
		return value;
	}

	@Override
	protected Slice decodeValue(Slice slice) {
		return slice;
	}
}
