package br.com.ottimizza.application.configuration;

import java.lang.reflect.Type;
import java.util.Collections;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.apache.commons.codec.EncoderException;

import feign.RequestTemplate;
import feign.codec.Encoder;

public class JacksonEncoder implements Encoder {
    private final ObjectMapper mapper;

    public JacksonEncoder() {
        this((Iterable)Collections.emptyList());
    }

    public JacksonEncoder(Iterable<Module> modules) {
        this((new ObjectMapper()).setSerializationInclusion(Include.NON_NULL).configure(SerializationFeature.INDENT_OUTPUT, true).registerModules(modules));
    }

    public JacksonEncoder(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public void encode(Object object, Type bodyType, RequestTemplate template) {
        try {
            JavaType javaType = this.mapper.getTypeFactory().constructType(bodyType);
            template.body(this.mapper.writerFor(javaType).writeValueAsString(object));
        } catch (JsonProcessingException var5) {
            try {
                throw new EncoderException(var5.getMessage(), var5);
            } catch (EncoderException e) {
                e.printStackTrace();
            }
        }
    }
}