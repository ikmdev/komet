/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.tinkar.provider.websocket.server;

import dev.ikm.tinkar.common.service.PluggableService;
import dev.ikm.tinkar.common.service.PrimitiveDataService;
import io.activej.bytebuf.ByteBuf;
import io.activej.bytebuf.ByteBufPool;
import io.activej.http.AsyncServlet;
import io.activej.http.RoutingServlet;
import io.activej.http.WebSocket.Message;
import io.activej.inject.annotation.Provides;
import io.activej.launchers.http.MultithreadedHttpServerLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicInteger;

public class DataProviderWebsocketServer extends MultithreadedHttpServerLauncher {
    private static final Logger LOG = LoggerFactory.getLogger(DataProviderWebsocketServer.class);
    private final ServiceLoader<PrimitiveDataService> serviceLoader;
    private final PrimitiveDataService dataService;

    public DataProviderWebsocketServer() {
        this.serviceLoader = PluggableService.load(PrimitiveDataService.class);
        this.dataService = this.serviceLoader.findFirst().get();
    }

    public static void main(String[] args) throws Exception {
        DataProviderWebsocketServer server = new DataProviderWebsocketServer();
        server.launch(args);
    }

    @Provides
    AsyncServlet servlet() {
        AtomicInteger nid = new AtomicInteger();
        return RoutingServlet.create()
                .mapWebSocket("/", webSocket -> webSocket.readMessage()
                        .whenResult(message -> {
                            ByteBuf buf = message.getBuf();
                            PrimitiveDataService.RemoteOperations operation = PrimitiveDataService.RemoteOperations.fromToken(buf.readByte());
                            nid.set(buf.readInt());
                            LOG.info("Received: " + operation + " for: " + nid);
                        })
                        .then(() -> {

                            byte[] data = dataService.getBytes(nid.get());
                            ByteBuf buf = ByteBufPool.allocate(data.length);
                            buf.writeInt(data.length);
                            buf.write(data);
                            return webSocket.writeMessage(Message.binary(buf));
                        }));
    }
}
