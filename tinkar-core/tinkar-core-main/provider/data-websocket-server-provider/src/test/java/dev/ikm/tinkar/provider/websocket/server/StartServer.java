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

import dev.ikm.tinkar.entity.load.LoadEntitiesFromProtobufFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class StartServer {
    private static final Logger LOG = LoggerFactory.getLogger(StartServer.class);

    public static void main(String[] args) {
        try {
            File file = new File("/Users/kec/Solor/tinkar-export.zip");
            LoadEntitiesFromProtobufFile loadTink =  new LoadEntitiesFromProtobufFile(file);
            loadTink.compute();
            LOG.info("Loaded. " + loadTink.summarize());
            DataProviderWebsocketServer server = new DataProviderWebsocketServer();
            server.launch(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
