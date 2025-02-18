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

package org.eclipse.edc.protocol.dsp.transferprocess.dispatcher;


import org.eclipse.edc.connector.transfer.spi.types.protocol.TransferCompletionMessage;
import org.eclipse.edc.connector.transfer.spi.types.protocol.TransferRequestMessage;
import org.eclipse.edc.connector.transfer.spi.types.protocol.TransferStartMessage;
import org.eclipse.edc.connector.transfer.spi.types.protocol.TransferTerminationMessage;
import org.eclipse.edc.protocol.dsp.dispatcher.PostDspHttpRequestFactory;
import org.eclipse.edc.protocol.dsp.spi.dispatcher.DspHttpRemoteMessageDispatcher;
import org.eclipse.edc.protocol.dsp.spi.serialization.JsonLdRemoteMessageSerializer;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

import static org.eclipse.edc.protocol.dsp.spi.dispatcher.response.DspHttpResponseBodyExtractor.NOOP;
import static org.eclipse.edc.protocol.dsp.transferprocess.dispatcher.TransferProcessApiPaths.BASE_PATH;
import static org.eclipse.edc.protocol.dsp.transferprocess.dispatcher.TransferProcessApiPaths.TRANSFER_COMPLETION;
import static org.eclipse.edc.protocol.dsp.transferprocess.dispatcher.TransferProcessApiPaths.TRANSFER_INITIAL_REQUEST;
import static org.eclipse.edc.protocol.dsp.transferprocess.dispatcher.TransferProcessApiPaths.TRANSFER_START;
import static org.eclipse.edc.protocol.dsp.transferprocess.dispatcher.TransferProcessApiPaths.TRANSFER_TERMINATION;


/**
 * Provides HTTP dispatching for Dataspace Protocol transfer process messages via the {@link DspHttpRemoteMessageDispatcher}.
 */
@Extension(value = DspTransferProcessDispatcherExtension.NAME)
public class DspTransferProcessDispatcherExtension implements ServiceExtension {

    public static final String NAME = "Dataspace Protocol Transfer HTTP Dispatcher Extension";

    @Inject
    private DspHttpRemoteMessageDispatcher messageDispatcher;
    @Inject
    private JsonLdRemoteMessageSerializer remoteMessageSerializer;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        messageDispatcher.registerMessage(
                TransferRequestMessage.class,
                new PostDspHttpRequestFactory<>(remoteMessageSerializer, m -> BASE_PATH + TRANSFER_INITIAL_REQUEST),
                NOOP
        );
        messageDispatcher.registerMessage(
                TransferCompletionMessage.class,
                new PostDspHttpRequestFactory<>(remoteMessageSerializer, m -> BASE_PATH + m.getProcessId() + TRANSFER_COMPLETION),
                NOOP
        );
        messageDispatcher.registerMessage(
                TransferStartMessage.class,
                new PostDspHttpRequestFactory<>(remoteMessageSerializer, m -> BASE_PATH + m.getProcessId() + TRANSFER_START),
                NOOP
        );
        messageDispatcher.registerMessage(
                TransferTerminationMessage.class,
                new PostDspHttpRequestFactory<>(remoteMessageSerializer, m -> BASE_PATH + m.getProcessId() + TRANSFER_TERMINATION),
                NOOP
        );
    }
}
