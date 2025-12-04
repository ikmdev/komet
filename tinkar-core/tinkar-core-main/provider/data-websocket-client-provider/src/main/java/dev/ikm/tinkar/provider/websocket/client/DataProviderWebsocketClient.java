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
package dev.ikm.tinkar.provider.websocket.client;

import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.service.DataActivity;
import dev.ikm.tinkar.common.service.PrimitiveDataSearchResult;
import dev.ikm.tinkar.common.service.PrimitiveDataService;
import dev.ikm.tinkar.common.util.uuid.UuidUtil;
import dev.ikm.tinkar.entity.EntityService;
import io.activej.bytebuf.ByteBuf;
import io.activej.bytebuf.ByteBufPool;
import io.activej.eventloop.Eventloop;
import io.activej.http.AsyncHttpClient;
import io.activej.http.HttpRequest;
import io.activej.http.WebSocket;
import io.activej.http.WebSocket.Message;
import io.activej.inject.annotation.Inject;
import io.activej.inject.annotation.Provides;
import io.activej.inject.module.Module;
import io.activej.launcher.Launcher;
import io.activej.service.ServiceGraphModule;
import org.eclipse.collections.api.block.procedure.primitive.IntProcedure;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.ObjIntConsumer;

public class DataProviderWebsocketClient
        extends Launcher
        implements PrimitiveDataService {
    private static final Logger LOG = LoggerFactory.getLogger(DataProviderWebsocketClient.class);
    private static final Integer wsKey = Integer.valueOf(1);
    private final URI uri;
    @Inject
    AsyncHttpClient httpClient;
    @Inject
    Eventloop eventloop;
    ConcurrentHashMap<Integer, WebSocket> wsMap = new ConcurrentHashMap<>();

    public DataProviderWebsocketClient(URI uri) {
        this.uri = uri;
    }

    public static void main(String[] args) throws Exception {
        DataProviderWebsocketClient client = new DataProviderWebsocketClient(new URI("ws://127.0.0.1:8080/"));
        client.launch(args);
    }

    @Provides
    Eventloop eventloop() {
        return Eventloop.create();
    }

    @Provides
    AsyncHttpClient client(Eventloop eventloop) {
        return AsyncHttpClient.create(eventloop);
    }

    @Override
    protected Module getModule() {
        return ServiceGraphModule.create();
    }

    @Override
    protected void run() throws ExecutionException, InterruptedException {
        String url = args.length != 0 ? args[0] : "ws://127.0.0.1:8080/";
        LOG.info("\nWeb Socket request: " + url);
        CompletableFuture<?> future = eventloop.submit(() -> {
            getEntity(url, PrimitiveDataService.FIRST_NID);
        });
        future.get();
        future = eventloop.submit(() -> {
            getEntity(url, PrimitiveDataService.FIRST_NID + 1);
        });
        future.get();
    }

    private void getEntity(String url, int nid) {
        LOG.info("Sending nid: " + nid);
        ByteBuf buf = ByteBufPool.allocate(32);
        buf.writeByte(RemoteOperations.GET_BYTES.token);
        buf.writeInt(nid);
        httpClient.webSocketRequest(HttpRequest.get(url))
                .then(webSocket -> webSocket.writeMessage(Message.binary(buf))
                        .then(webSocket::readMessage)
                        .whenResult(message -> {
                            ByteBuf readBuf = message.getBuf();
                            int length = readBuf.readInt();
                            byte[] readData = new byte[length];
                            readBuf.read(readData);
                            LOG.info("Received: " + EntityService.get().unmarshalChronology(readData));
                        })
                        .whenComplete(webSocket::close));
    }

    @Override
    public long writeSequence() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        WebSocket ws = wsMap.remove(wsKey);
        if (ws != null) {
            ws.close();
        }
    }

    @Override
    public int nidForUuids(UUID... uuids) {
        try {
            return nidForLongArray(UuidUtil.asArray(uuids));
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int nidForUuids(ImmutableList<UUID> uuidList) {
        try {
            return nidForLongArray(UuidUtil.asArray(uuidList));
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean hasUuid(UUID uuid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasPublicId(PublicId publicId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forEach(ObjIntConsumer<byte[]> action) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forEachParallel(ObjIntConsumer<byte[]> action) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forEachParallel(ImmutableIntList nids, ObjIntConsumer<byte[]> action) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] getBytes(int nid) {

        ByteBuf buf = ByteBufPool.allocate(32);
        buf.writeByte(RemoteOperations.GET_BYTES.token);
        buf.writeInt(nid);
        AtomicReference<byte[]> readDataReference = new AtomicReference<>();
        final WebSocket ws = webSocket();

        CompletableFuture<?> future = eventloop.submit(() -> {
            ws.writeMessage(Message.binary(buf))
                    .then(ws::readMessage)
                    .whenResult(message -> {
                        ByteBuf readBuf = message.getBuf();
                        int length = readBuf.readInt();
                        byte[] readData = new byte[length];
                        readBuf.read(readData);
                        readDataReference.set(readData);
                        LOG.info("Received: " + EntityService.get().unmarshalChronology(readData));

                    });
        });

        try {
            future.get();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return readDataReference.get();
    }

    @Override
    public byte[] merge(int nid, int patternNid, int referencedComponentNid, byte[] value, Object sourceObject, DataActivity activity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PrimitiveDataSearchResult[] search(String query, int maxResultSize) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public CompletableFuture<Void> recreateLuceneIndex() throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forEachSemanticNidOfPattern(int patternNid, IntProcedure procedure) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forEachPatternNid(IntProcedure procedure) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forEachConceptNid(IntProcedure procedure) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forEachStampNid(IntProcedure procedure) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forEachSemanticNid(IntProcedure procedure) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forEachSemanticNidForComponent(int componentNid, IntProcedure procedure) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forEachSemanticNidForComponentOfPattern(int componentNid, int patternNid, IntProcedure procedure) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String name() {
        return uri.toString();
    }

    private int nidForLongArray(long[] uuidParts) throws ExecutionException, InterruptedException {
        ByteBuf buf = ByteBufPool.allocate(32);
        buf.writeByte(RemoteOperations.NID_FOR_UUIDS.token);
        buf.writeInt(uuidParts.length);
        for (long part : uuidParts) {
            buf.writeLong(part);
        }
        AtomicInteger nid = new AtomicInteger();
        final WebSocket ws = webSocket();
        CompletableFuture<?> future = eventloop.submit(() -> {
            ws.writeMessage(Message.binary(buf))
                    .then(ws::readMessage)
                    .whenResult(message -> {
                        ByteBuf readBuf = message.getBuf();
                        nid.set(readBuf.readInt());
                    });
        });
        future.get();
        return nid.get();
    }

    WebSocket webSocket() {
        return wsMap.computeIfAbsent(wsKey, (Integer key) ->
                {
                    try {
                        return httpClient.webSocketRequest(HttpRequest.get(uri.toString())).toCompletableFuture().get();
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }
        );
    }
}
