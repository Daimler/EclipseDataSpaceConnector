/*
 *  Copyright (c) 2023 Fraunhofer Institute for Software and Systems Engineering
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Fraunhofer Institute for Software and Systems Engineering - initial API and implementation
 *
 */

package org.eclipse.edc.protocol.dsp.negotiation.transform.from;

import jakarta.json.Json;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;
import org.eclipse.edc.connector.contract.spi.types.negotiation.ContractRequestMessage;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.spi.types.domain.offer.ContractOffer;
import org.eclipse.edc.transform.spi.ProblemBuilder;
import org.eclipse.edc.transform.spi.TransformerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.ID;
import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.TYPE;
import static org.eclipse.edc.protocol.dsp.type.DspNegotiationPropertyAndTypeNames.DSPACE_PROPERTY_DATASET;
import static org.eclipse.edc.protocol.dsp.type.DspNegotiationPropertyAndTypeNames.DSPACE_PROPERTY_OFFER;
import static org.eclipse.edc.protocol.dsp.type.DspNegotiationPropertyAndTypeNames.DSPACE_PROPERTY_OFFER_ID;
import static org.eclipse.edc.protocol.dsp.type.DspNegotiationPropertyAndTypeNames.DSPACE_TYPE_CONTRACT_REQUEST_MESSAGE;
import static org.eclipse.edc.protocol.dsp.type.DspPropertyAndTypeNames.DSPACE_PROPERTY_CALLBACK_ADDRESS;
import static org.eclipse.edc.protocol.dsp.type.DspPropertyAndTypeNames.DSPACE_PROPERTY_CONSUMER_PID;
import static org.eclipse.edc.protocol.dsp.type.DspPropertyAndTypeNames.DSPACE_PROPERTY_PROCESS_ID;
import static org.eclipse.edc.protocol.dsp.type.DspPropertyAndTypeNames.DSPACE_PROPERTY_PROVIDER_PID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JsonObjectFromContractRequestMessageTransformerTest {

    private static final String CALLBACK_ADDRESS = "https://test.com";
    private static final String CONSUMER_PID = "consumerPid";
    private static final String PROTOCOL = "DSP";
    private static final String DATASET_ID = "datasetId";
    private static final String CONTRACT_OFFER_ID = "contractOffer1";

    private final JsonBuilderFactory jsonFactory = Json.createBuilderFactory(Map.of());
    private final TransformerContext context = mock();

    private final JsonObjectFromContractRequestMessageTransformer transformer =
            new JsonObjectFromContractRequestMessageTransformer(jsonFactory);

    @BeforeEach
    void setUp() {
        when(context.problem()).thenReturn(new ProblemBuilder(context));
    }

    @Test
    void verify_contractOffer() {
        var message = contractRequestMessageBuilder()
                .processId("processId")
                .consumerPid("consumerPid")
                .providerPid("providerPid")
                .contractOffer(contractOffer())
                .build();
        var obj = jsonFactory.createObjectBuilder().build();
        when(context.transform(any(Policy.class), eq(JsonObject.class))).thenReturn(obj);

        var result = transformer.transform(message, context);

        assertThat(result).isNotNull();
        assertThat(result.getJsonString(ID).getString()).isNotEmpty();
        assertThat(result.getJsonString(TYPE).getString()).isEqualTo(DSPACE_TYPE_CONTRACT_REQUEST_MESSAGE);
        assertThat(result.getJsonString(DSPACE_PROPERTY_DATASET).getString()).isEqualTo(DATASET_ID);
        assertThat(result.getJsonString(DSPACE_PROPERTY_CALLBACK_ADDRESS).getString()).isEqualTo(CALLBACK_ADDRESS);
        assertThat(result.getJsonObject(DSPACE_PROPERTY_OFFER)).isNotNull();
        assertThat(result.getJsonObject(DSPACE_PROPERTY_OFFER).getString(ID)).isEqualTo(CONTRACT_OFFER_ID);
        assertThat(result.getJsonString(DSPACE_PROPERTY_CONSUMER_PID).getString()).isEqualTo("consumerPid");
        assertThat(result.getJsonString(DSPACE_PROPERTY_PROVIDER_PID).getString()).isEqualTo("providerPid");
        assertThat(result.getJsonString(DSPACE_PROPERTY_PROCESS_ID).getString()).isEqualTo("processId");

        verify(context, never()).reportProblem(anyString());
    }

    @Test
    void verify_contractOfferId() {
        var message = contractRequestMessageBuilder()
                .processId("processId")
                .consumerPid(CONSUMER_PID)
                .contractOfferId(CONTRACT_OFFER_ID)
                .build();

        var result = transformer.transform(message, context);

        assertThat(result).isNotNull();
        assertThat(result.getJsonString(ID).getString()).isNotEmpty();
        assertThat(result.getJsonString(TYPE).getString()).isEqualTo(DSPACE_TYPE_CONTRACT_REQUEST_MESSAGE);
        assertThat(result.getString(DSPACE_PROPERTY_OFFER_ID)).isEqualTo(CONTRACT_OFFER_ID);

        verify(context, never()).reportProblem(anyString());
    }

    @Test
    void verify_nullPolicyFails() {
        var message = contractRequestMessageBuilder()
                .processId("processId")
                .consumerPid(CONSUMER_PID)
                .contractOffer(contractOffer())
                .build();
        when(context.transform(any(Policy.class), eq(JsonObject.class))).thenReturn(null);

        var result = transformer.transform(message, context);

        assertThat(result).isNull();
        verify(context, times(1)).reportProblem(anyString());
    }

    private ContractRequestMessage.Builder contractRequestMessageBuilder() {
        return ContractRequestMessage.Builder.newInstance()
                .protocol(PROTOCOL)
                .callbackAddress(CALLBACK_ADDRESS)
                .dataset(DATASET_ID);
    }

    private ContractOffer contractOffer() {
        return ContractOffer.Builder.newInstance()
                .id(CONTRACT_OFFER_ID)
                .assetId(DATASET_ID)
                .policy(Policy.Builder.newInstance().build())
                .build();
    }

}
