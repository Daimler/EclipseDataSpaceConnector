/*
 *  Copyright (c) 2023 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */

package org.eclipse.edc.iam.identitytrust.core.scope;

import org.eclipse.edc.identitytrust.scope.ScopeExtractor;
import org.eclipse.edc.identitytrust.scope.ScopeExtractorRegistry;
import org.eclipse.edc.jwt.spi.JwtRegisteredClaimNames;
import org.eclipse.edc.policy.engine.spi.PolicyContext;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.iam.TokenParameters;
import org.eclipse.edc.spi.monitor.Monitor;

import java.util.function.BiFunction;

import static java.lang.String.format;

/**
 * IATP pre-validator function for extracting scopes from a {@link Policy} using the registered {@link ScopeExtractor}
 * in the {@link ScopeExtractorRegistry}.
 */
public class IatpScopeExtractorFunction implements BiFunction<Policy, PolicyContext, Boolean> {

    private final ScopeExtractorRegistry registry;
    private final Monitor monitor;

    public IatpScopeExtractorFunction(ScopeExtractorRegistry registry, Monitor monitor) {
        this.registry = registry;
        this.monitor = monitor;
    }

    @Override
    public Boolean apply(Policy policy, PolicyContext context) {
        var params = context.getContextData(TokenParameters.Builder.class);
        if (params == null) {
            throw new EdcException(format("%s not set in policy context", TokenParameters.Builder.class.getName()));
        }
        var results = registry.extractScopes(policy, context).map(scopes -> String.join(" ", scopes));

        if (results.succeeded()) {
            params.claims(JwtRegisteredClaimNames.SCOPE, results.getContent());
            return true;
        } else {
            monitor.warning("Failed to extract scopes from a policy");
            return false;
        }
    }
}
