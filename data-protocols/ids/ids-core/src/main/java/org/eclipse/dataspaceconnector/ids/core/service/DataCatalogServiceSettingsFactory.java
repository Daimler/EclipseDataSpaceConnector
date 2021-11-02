package org.eclipse.dataspaceconnector.ids.core.service;

import org.eclipse.dataspaceconnector.ids.core.configuration.IllegalSettingException;
import org.eclipse.dataspaceconnector.ids.core.configuration.SettingResolver;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DataCatalogServiceSettingsFactory {
    private final SettingResolver settingResolver;

    public DataCatalogServiceSettingsFactory(@NotNull SettingResolver settingResolver) {
        this.settingResolver = Objects.requireNonNull(settingResolver);
    }

    @NotNull
    public DataCatalogServiceSettingsFactoryResult createResourceCatalogFactorySettings() {
        List<String> errors = new ArrayList<>();

        String catalogId = null;

        try {
            catalogId = settingResolver.resolveCatalogId();
        } catch (IllegalSettingException e) {
            errors.add(e.getMessage());
        }

        var settings = DataCatalogServiceSettings.Builder.newInstance().catalogId(catalogId).build();

        return DataCatalogServiceSettingsFactoryResult.Builder.newInstance()
                .dataCatalogServiceSettings(settings)
                .errors(errors)
                .build();
    }
}
