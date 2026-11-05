// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.controller.publication.blog;

import io.micronaut.serde.annotation.Serdeable;

/**
 * A blog category; {@link #id()} is accepted as {@code searchCategory} by {@link AbstractBlog#searchBlogPosts}.
 */
@Serdeable
public record BlogCategory(
	long id,
	String name
	) {
}
