// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest.filter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.ws.rs.NameBinding;

import eu.albina.model.enumerations.Role;

@NameBinding
@Retention(value = RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface Secured {
	Role[] value() default {};
}
