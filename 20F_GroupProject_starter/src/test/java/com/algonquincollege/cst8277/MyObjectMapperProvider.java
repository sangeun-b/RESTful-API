/*****************************************************************c******************o*******v******id********
 * File: MyObjectMapperProvider.java
 * Course materials (20W) CST 8277
 * @author Mike Norman
 * @author Sangeun Baek 040953608 
 * @author Hsing-I Wang 040953737
 * @author Qi Wang 040946448
 *
 * Note: students do NOT need to change anything in this class
 *
 */
package com.algonquincollege.cst8277;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Provider
public class MyObjectMapperProvider implements ContextResolver<ObjectMapper> {
    final ObjectMapper defaultObjectMapper;
    public MyObjectMapperProvider() {
        defaultObjectMapper = createDefaultMapper();
    }
    @Override
    public ObjectMapper getContext(Class<?> type) {
            return defaultObjectMapper;
        }
    private static ObjectMapper createDefaultMapper() {
        final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}