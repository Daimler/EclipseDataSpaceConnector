/*
 *  Copyright (c) 2021 Daimler TSS GmbH
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Daimler TSS GmbH - Initial API and Implementation
 *
 */

package org.eclipse.dataspaceconnector.ids.api.multipart.handler.description;

import org.eclipse.dataspaceconnector.ids.core.configuration.IllegalSettingException;
import org.eclipse.dataspaceconnector.ids.core.configuration.SettingResolver;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ResourceDescriptionRequestHandlerSettingsFactory {
    private final SettingResolver settingResolver;

    public ResourceDescriptionRequestHandlerSettingsFactory(@NotNull SettingResolver settingResolver) {
        this.settingResolver = Objects.requireNonNull(settingResolver);
    }

    @NotNull
    public ResourceDescriptionRequestHandlerSettingsFactoryResult createResourceDescriptionRequestHandlerSettings() {
        List<String> errors = new ArrayList<>();

        String id = null;

        try {
            id = settingResolver.resolveId();
        } catch (IllegalSettingException e) {
            errors.add(e.getMessage());
        }

        var settings = ResourceDescriptionRequestHandlerSettings.Builder.newInstance().id(id).build();

        return ResourceDescriptionRequestHandlerSettingsFactoryResult.Builder.newInstance()
                .resourceDescriptionRequestHandlerSettings(settings)
                .errors(errors)
                .build();
    }
}
