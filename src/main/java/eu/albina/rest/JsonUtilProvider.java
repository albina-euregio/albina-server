// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;

import eu.albina.util.JsonUtil;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.Headers;
import io.micronaut.core.type.MutableHeaders;
import io.micronaut.http.MediaType;
import io.micronaut.http.body.MessageBodyReader;
import io.micronaut.http.body.MessageBodyWriter;
import io.micronaut.http.codec.CodecException;

public class JsonUtilProvider<T> implements MessageBodyReader<T>, MessageBodyWriter<T> {

	@Override
	public @Nullable T read(@NonNull Argument<T> type, @Nullable MediaType mediaType, @NonNull Headers httpHeaders, @NonNull InputStream inputStream) throws CodecException {
		try {
			return JsonUtil.parseUsingJackson(inputStream, type.getType());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public void writeTo(@NonNull Argument<T> type, @NonNull MediaType mediaType, T object, @NonNull MutableHeaders outgoingHeaders, @NonNull OutputStream outputStream) throws CodecException {
		try {
			JsonUtil.writeValueUsingJackson(outputStream, object);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
