/*
 *  Copyright (c) 2022 Amadeus
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Amadeus - initial API and implementation
 *
 */

package org.eclipse.edc.connector.transfer.dataplane.flow;

import org.eclipse.edc.connector.dataplane.selector.spi.DataPlaneSelectorService;
import org.eclipse.edc.connector.dataplane.selector.spi.client.DataPlaneClientFactory;
import org.eclipse.edc.connector.dataplane.selector.spi.instance.DataPlaneInstance;
import org.eclipse.edc.connector.transfer.spi.callback.ControlApiUrl;
import org.eclipse.edc.connector.transfer.spi.flow.DataFlowController;
import org.eclipse.edc.connector.transfer.spi.types.DataFlowResponse;
import org.eclipse.edc.connector.transfer.spi.types.TransferProcess;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.spi.response.StatusResult;
import org.eclipse.edc.spi.types.domain.asset.Asset;
import org.eclipse.edc.spi.types.domain.transfer.DataFlowRequest;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.stream.Collectors.toSet;
import static org.eclipse.edc.connector.transfer.dataplane.spi.TransferDataPlaneConstants.HTTP_PROXY;
import static org.eclipse.edc.connector.transfer.spi.flow.FlowType.PULL;
import static org.eclipse.edc.connector.transfer.spi.flow.FlowType.PUSH;

public class ProviderPushTransferDataFlowController implements DataFlowController {

    private final ControlApiUrl callbackUrl;
    private final DataPlaneSelectorService selectorClient;
    private final DataPlaneClientFactory clientFactory;

    private final Set<String> transferTypes = Set.of("%s-%s".formatted("HttpData", PULL));

    public ProviderPushTransferDataFlowController(ControlApiUrl callbackUrl, DataPlaneSelectorService selectorClient, DataPlaneClientFactory clientFactory) {
        this.callbackUrl = callbackUrl;
        this.selectorClient = selectorClient;
        this.clientFactory = clientFactory;
    }

    @Override
    public boolean canHandle(TransferProcess transferProcess) {
        // Backward compatibility: adds check if a transfer type is provided, it should not be Http-PULL
        return !HTTP_PROXY.equals(transferProcess.getDestinationType()) &&
                (Optional.ofNullable(transferProcess.getTransferType()).map(type -> !transferTypes.contains(type)).orElse(true));

    }

    @Override
    public @NotNull StatusResult<DataFlowResponse> initiateFlow(TransferProcess transferProcess, Policy policy) {
        var dataFlowRequest = DataFlowRequest.Builder.newInstance()
                .id(UUID.randomUUID().toString())
                .processId(transferProcess.getId())
                .trackable(true)
                .sourceDataAddress(transferProcess.getContentDataAddress())
                .destinationDataAddress(transferProcess.getDataDestination())
                .transferType(transferProcess.getTransferType())
                .callbackAddress(callbackUrl != null ? callbackUrl.get() : null)
                .build();

        var dataPlaneInstance = selectorClient.select(transferProcess.getContentDataAddress(), transferProcess.getDataDestination());
        return clientFactory.createClient(dataPlaneInstance)
                .transfer(dataFlowRequest)
                .map(it -> DataFlowResponse.Builder.newInstance().build());
    }

    @Override
    public StatusResult<Void> terminate(TransferProcess transferProcess) {
        return selectorClient.getAll().stream().map(clientFactory::createClient)
                .map(client -> client.terminate(transferProcess.getId()))
                .reduce(StatusResult::merge)
                .orElse(StatusResult.success());
    }

    @Override
    public Set<String> transferTypesFor(Asset asset) {
        return selectorClient.getAll().stream()
                .filter(it -> it.getAllowedSourceTypes().contains(asset.getDataAddress().getType()))
                .map(DataPlaneInstance::getAllowedDestTypes)
                .flatMap(Collection::stream)
                .map(it -> "%s-%s".formatted(it, PUSH))
                .collect(toSet());
    }

}
