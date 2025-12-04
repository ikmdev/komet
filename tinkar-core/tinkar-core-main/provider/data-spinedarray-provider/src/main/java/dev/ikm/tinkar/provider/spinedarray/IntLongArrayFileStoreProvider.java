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
package dev.ikm.tinkar.provider.spinedarray;


import dev.ikm.tinkar.collection.SpineFileUtil;
import dev.ikm.tinkar.collection.store.IntLongArrayStore;
import dev.ikm.tinkar.collection.store.IntLongArrayStoreProvider;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;

import java.io.File;

public class IntLongArrayFileStoreProvider implements IntLongArrayStoreProvider {
    public IntLongArrayStore get(String storeName) {
        File folderPath = ServiceProperties.get(ServiceKeys.DATA_STORE_ROOT, SpinedArrayProvider.defaultDataDirectory);
        IntLongArrayStore intLongArrayFileStore = new IntLongArrayFileStore(
                new File(new File(folderPath, "namedStores"), storeName));
        return intLongArrayFileStore;
    }
    public IntLongArrayStore get(int patternNid) {
        File folderPath = ServiceProperties.get(ServiceKeys.DATA_STORE_ROOT, SpinedArrayProvider.defaultDataDirectory);
        IntLongArrayStore intLongArrayFileStore = new IntLongArrayFileStore(
                SpineFileUtil.getSpineDirectory(new File(folderPath, "longArrayPatternStores"), patternNid));
        return intLongArrayFileStore;
    }
}
