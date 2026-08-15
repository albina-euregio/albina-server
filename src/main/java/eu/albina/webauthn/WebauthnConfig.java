// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.webauthn;

/**
 * @param rpId   The WebAuthn Relying Party ID: a registrable domain suffix of every origin passkeys are used from.
 * @param origin The origin (scheme + host + port) this server instance's frontend is deployed at.
 */
public record WebauthnConfig(String rpId, String rpName, String origin) {
}
