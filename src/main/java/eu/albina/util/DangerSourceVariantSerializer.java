package eu.albina.util;

import java.io.IOException;
import java.io.StringWriter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import eu.albina.model.DangerSourceVariant;

public class DangerSourceVariantSerializer extends JsonSerializer<DangerSourceVariant> {

    @Override
    public void serialize(DangerSourceVariant value, 
      JsonGenerator gen,
      SerializerProvider serializers) 
      throws IOException, JsonProcessingException {
 
        StringWriter writer = new StringWriter();
        JsonUtil.ALBINA_OBJECT_MAPPER.writeValue(writer, value);
        gen.writeFieldName(writer.toString());
    }
}