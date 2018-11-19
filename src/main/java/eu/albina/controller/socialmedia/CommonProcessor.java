package eu.albina.controller.socialmedia;

import com.bedatadriven.jackson.datatype.jts.JtsModule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.albina.exception.AlbinaException;
import eu.albina.model.socialmedia.*;
import eu.albina.util.HibernateUtil;
import org.apache.commons.configuration2.builder.ReloadingFileBasedConfigurationBuilder;
import org.hibernate.HibernateException;
import twitter4j.TwitterException;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class CommonProcessor {
    private ObjectMapper objectMapper=new ObjectMapper();

    public CommonProcessor(){
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
        objectMapper.registerModule(new JtsModule());
    }

    public String toJson(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }

    public <T> T fromJson(String json, Class<T> clazz) throws IOException {
        return objectMapper.readValue(json,clazz);
    }

}
