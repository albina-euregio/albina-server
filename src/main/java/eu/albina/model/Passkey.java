// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import eu.albina.util.JsonUtil;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * A WebAuthn passkey registered for a {@link User}. The credential itself (public key, sign
 * counter, transports, …) is treated as an opaque blob in {@link #publicKeyCose}, parsed only by
 * {@link eu.albina.webauthn.CoseKey} — never decomposed into its own columns, the same way a
 * password hash column doesn't get its own columns per hash parameter.
 */
@Entity
@Table(name = "passkeys")
@Serdeable
@JsonView(JsonUtil.Views.Internal.class)
public class Passkey extends AbstractPersistentObject {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "OWNER_EMAIL", nullable = false)
	@JsonIgnore
	private User owner;

	/** Base64url-encoded WebAuthn credential ID, unique across all accounts. */
	@Column(name = "CREDENTIAL_ID", length = 191, unique = true, nullable = false)
	@JsonIgnore
	private String credentialId;

	/** The raw COSE_Key (CBOR), base64-encoded. */
	@Column(name = "PUBLIC_KEY_COSE", length = 1024, nullable = false)
	@JsonIgnore
	private String publicKeyCose;

	@Column(name = "SIGN_COUNT", nullable = false)
	@JsonIgnore
	private long signCount;

	/** User-editable label (e.g. "iPhone", "YubiKey"), shown in the passkey management UI. */
	@Column(name = "NAME", length = 191)
	private String name;

	@Column(name = "CREATED_AT", nullable = false)
	private Instant createdAt;

	@Column(name = "LAST_USED_AT")
	private Instant lastUsedAt;

	@PrePersist
	void prePersist() {
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public String getCredentialId() {
		return credentialId;
	}

	public void setCredentialId(String credentialId) {
		this.credentialId = credentialId;
	}

	public String getPublicKeyCose() {
		return publicKeyCose;
	}

	public void setPublicKeyCose(String publicKeyCose) {
		this.publicKeyCose = publicKeyCose;
	}

	public long getSignCount() {
		return signCount;
	}

	public void setSignCount(long signCount) {
		this.signCount = signCount;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getLastUsedAt() {
		return lastUsedAt;
	}

	public void setLastUsedAt(Instant lastUsedAt) {
		this.lastUsedAt = lastUsedAt;
	}
}
