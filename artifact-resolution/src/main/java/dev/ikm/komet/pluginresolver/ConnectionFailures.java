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
package dev.ikm.komet.pluginresolver;

import java.security.cert.CertPathBuilderException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.util.Objects;

/**
 * Turns a connection failure into a sentence a user can act on. Raw JSSE text —
 * {@code "(certificate_unknown) PKIX path building failed: sun.security.provider..."} — names
 * neither the actual condition (this network re-signs TLS traffic and this computer doesn't
 * trust its CA) nor a next step, so the two certificate cases that have a specific remedy are
 * recognized from the cause chain and explained; everything else keeps the behavior of showing
 * the failure's own message (IKE-Network/ike-issues#956).
 */
public final class ConnectionFailures {

    /** Cause-chain walk limit — guards against pathological or cyclic cause chains. */
    private static final int MAX_CAUSE_DEPTH = 16;

    private ConnectionFailures() {
    }

    /**
     * A user-facing description of {@code failure}.
     *
     * <ul>
     *   <li>A certificate <em>validity</em> failure (expired / not yet valid) is described as
     *       such, with the computer's clock as the thing to check.</li>
     *   <li>Any other certificate <em>trust</em> failure is described as TLS interception:
     *       the network's CA isn't in the operating-system trust store. The underlying detail
     *       (which, via {@link PlatformTrust}, names the served chain's subject and issuer) is
     *       appended in parentheses.</li>
     *   <li>Anything else: the deepest non-blank message in the cause chain — the closest
     *       thing to the actual reason — falling back to the failure's own {@code toString}.</li>
     * </ul>
     *
     * @param failure the failure to describe
     * @return the description, never blank
     * @throws NullPointerException if {@code failure} is {@code null}
     */
    public static String describe(Throwable failure) {
        Objects.requireNonNull(failure, "failure");
        boolean trustFailure = false;
        boolean validityFailure = false;
        String deepestMessage = null;
        Throwable current = failure;
        for (int depth = 0; current != null && depth < MAX_CAUSE_DEPTH; depth++) {
            if (current instanceof CertificateExpiredException || current instanceof CertificateNotYetValidException) {
                validityFailure = true;
            } else if (current instanceof CertificateException
                    || current instanceof CertPathBuilderException
                    || current instanceof CertPathValidatorException) {
                trustFailure = true;
            }
            String message = current.getMessage();
            if (message != null && !message.isBlank()) {
                deepestMessage = message;
            }
            current = current.getCause() == current ? null : current.getCause();
        }
        if (deepestMessage == null) {
            deepestMessage = failure.toString();
        }
        if (validityFailure) {
            return "the repository's TLS certificate is outside its validity period (" + deepestMessage
                    + ") — check this computer's date and time";
        }
        if (trustFailure) {
            return "the repository's TLS certificate is not trusted by this computer — on networks that"
                    + " inspect secure traffic (corporate proxy or firewall), the network's CA must be"
                    + " installed in the operating-system trust store (" + deepestMessage + ")";
        }
        return deepestMessage;
    }
}
