package com.example.kbuddy_backend.common.advice.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApiResponse<T> {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private final LocalDateTime timestamp = LocalDateTime.now();
    private int status;
    private String code;
    private String path;
    private T message;


    public static <T> ApiResponse<T> ok(T data,String path){
        return new ApiResponse<>(200,path,data, CustomCode.HTTP_200);
    }

    public static <T> ApiResponse<T> created(T data,String path){
        return new ApiResponse<>(201,path,data, CustomCode.HTTP_201);
    }

    public static <T> ApiResponse<T> noContent(T data,String path){
        return new ApiResponse<>(204,path,data, CustomCode.HTTP_204);
    }

    public static ApiResponse<?> error(String message,String path,int status, String code){
        return new ApiResponse<>(status,path,message,code);
    }
    public ApiResponse(int status, String path, T message, CustomCode code) {
        this.status = status;
        this.path = path;
        this.message = message;
        this.code = code.getCode();
    }

    public ApiResponse(int status, String path, T message, String code) {
        this.status = status;
        this.path = path;
        this.message = message;
        this.code = code;
    }

    public String toJson() {
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert ApiResponse to JSON", e);
        }
    }

}
