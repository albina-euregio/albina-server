// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller;

import eu.albina.model.Passkey;
import eu.albina.model.User;
import io.micronaut.data.annotation.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PasskeyRepository extends CrudRepository<Passkey, String> {
	List<Passkey> findByOwner(User owner);

	Optional<Passkey> findByCredentialId(String credentialId);
}
