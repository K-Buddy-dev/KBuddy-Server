package com.example.kbuddy_backend.common.advice.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class ApiResponse<T> {

    public static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Schema(description = "응답 시간")
    public LocalDateTime timestamp = LocalDateTime.now();

    @Schema(description = "HTTP 상태 코드", example = "200")
    public int status;

    @Schema(description = "응답 코드", example = "KB-HTTP-201")
    public String code;

    @Schema(description = "요청 경로", example = "/kbuddy/v1/user/auth/register")
    public String path;

    @Schema(description = "실제 응답 데이터")
    public T data;


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
        this.data = message;
        this.code = code.getCode();
    }

    public ApiResponse(int status, String path, T message, String code) {
        this.status = status;
        this.path = path;
        this.data = message;
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
