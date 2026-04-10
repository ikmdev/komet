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

/**
 * gRPC client provider for Komet. Provides {@link dev.ikm.komet.grpc.GrpcSearchClient}
 * which connects to a running tinkar-core service and delegates concept search over gRPC.
 *
 * <p>The gRPC runtime libraries (grpc-api, grpc-stub, grpc-protobuf, guava) are shaded
 * into this jar by maven-shade-plugin so that jlink sees a single named module rather
 * than a mix of automatic modules.
 *
 * <p>grpc-netty-shaded (the transport) remains on the classpath as a runtime-only
 * automatic module and is discovered at runtime via ServiceLoader.
 */
module dev.ikm.komet.grpc.provider {

    exports dev.ikm.komet.grpc;
    // Generated proto/gRPC stub classes (used from App.java to build requests)
    exports dev.ikm.tinkar.service.proto;

    // Protobuf runtime — JPMS-wrapped; provides com.google.protobuf.* packages
    requires dev.ikm.jpms.protobuf;

    // Generated schema message classes from Tinkar.proto
    requires dev.ikm.tinkar.schema;

    // javax.annotation — JPMS-wrapped jsr305 (module: dev.ikm.jpms.javax.annotation)
    requires dev.ikm.jpms.javax.annotation;

    // SearchService contract from tinkar-core search-provider
    requires dev.ikm.tinkar.provider.search;
    // PrimitiveDataSearchResult lives in tinkar-core common
    requires dev.ikm.tinkar.common;

    requires org.slf4j;
}
