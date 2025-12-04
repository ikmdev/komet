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

import dev.ikm.tinkar.common.service.DataServiceController;
import dev.ikm.tinkar.common.service.DataServiceProperty;
import dev.ikm.tinkar.common.service.DataUriOption;
import dev.ikm.tinkar.common.service.PrimitiveDataService;
import dev.ikm.tinkar.common.validation.ValidationRecord;
import dev.ikm.tinkar.common.validation.ValidationSeverity;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;

public class WebsocketServiceController implements DataServiceController<PrimitiveDataService> {
    public static final String CONTROLLER_NAME = "Websocket";
    private static final DataServiceProperty passwordProperty = new DataServiceProperty("password", true, true);
    MutableMap<DataServiceProperty, String> providerProperties = Maps.mutable.empty();
    {
        providerProperties.put(new DataServiceProperty("username", false, false), null);
        providerProperties.put(passwordProperty, null);
    }

    @Override
    public List<DataUriOption> providerOptions() {
        try {
            return List.of(new DataUriOption("localhost websocket", new URI("ws://127.0.0.1:8080/")));
        } catch (URISyntaxException e) {
           throw new RuntimeException(e);
        }
    }

    @Override
    public ValidationRecord[] validate(DataServiceProperty dataServiceProperty, Object value, Object target) {
        if (passwordProperty.equals(dataServiceProperty)) {
            if (value instanceof String password)  {
                if (password.isBlank()) {
                    return new ValidationRecord[] { new ValidationRecord(ValidationSeverity.ERROR,
                            "Password cannot be blank", target)};
                } else if (password.length() < 5)  {
                    return new ValidationRecord[] { new ValidationRecord(ValidationSeverity.ERROR,
                            "Password cannot be less than 5 characters", target)};
                } else if (password.length() < 8) {
                    return new ValidationRecord[] { new ValidationRecord(ValidationSeverity.WARNING,
                            "Password recommended to be 8 or more characters", target),
                            new ValidationRecord(ValidationSeverity.INFO,
                                    "Password is " + password.length() +
                                            " characters long", target),
                    };
                } else {
                    return new ValidationRecord[] { new ValidationRecord(ValidationSeverity.OK,
                            "Password OK", target)};
                }
            }
        }
        return new ValidationRecord[]{};
    }
    DataUriOption dataUriOption;
    @Override
    public String controllerName() {
        return CONTROLLER_NAME;
    }

    @Override
    public ImmutableMap<DataServiceProperty, String> providerProperties() {
        return providerProperties.toImmutable();
    }

    @Override
    public void setDataServiceProperty(DataServiceProperty key, String value) {
        providerProperties.put(key, value);
    }

    @Override
    public boolean isValidDataLocation(String name) {
        return name.toLowerCase(Locale.ROOT).startsWith("ws://");
    }

    @Override
    public void setDataUriOption(DataUriOption dataUriOption) {
        this.dataUriOption = dataUriOption;
    }

    @Override
    public Class<? extends PrimitiveDataService> serviceClass() {
        return DataProviderWebsocketClient.class;
    }

    @Override
    public boolean running() {
        return false;
    }

    @Override
    public void start() {
        try {
            DataProviderWebsocketClient client = new DataProviderWebsocketClient(dataUriOption.uri());
            client.launch(new String[]{});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public void save() {

    }

    @Override
    public void reload() {

    }

    @Override
    public PrimitiveDataService provider() {
        return null;
    }

    @Override
    public String toString() {
        return "Websocket";
    }
}
