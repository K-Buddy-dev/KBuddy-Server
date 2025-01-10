package com.example.kbuddy_backend.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.lang.Nullable;


/**
 * ResponseBodyAdvice로 Controller에서 String 반환은 조작할 수 없어 MappingJackson2HttpMessageConverter를 상속받아 직접 커스텀
 * ModelAndView를 반환할 때 String으로 반환되는 것을 막기 위해 String.class에 대한 canWrite를 false로 설정하는 것이 추후에 로직수정이 필요함.
 */
public class MyHttpMessageConverter extends MappingJackson2HttpMessageConverter {

    public MyHttpMessageConverter(ObjectMapper objectMapper) {
        super(objectMapper);
        objectMapper.registerModule(new JavaTimeModule());
        setObjectMapper(objectMapper);
    }

    @Override
    public boolean canWrite(Class<?> clazz, @Nullable MediaType mediaType) {
        return String.class == clazz && super.canWrite(mediaType);
    }
}
