package com.example.kbuddy_backend.docs.config;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import java.lang.reflect.Field;
import java.util.List;

import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.responses.ApiResponses;

@Slf4j
@Configuration
public class SwaggerConfig {

	@Bean
	public OperationCustomizer operationCustomizer() {
		return (operation, handlerMethod) -> {
			this.addResponseBodyWrapperSchemaExample(operation, com.example.kbuddy_backend.common.advice.response.ApiResponse.class, "data");
			this.addStandardResponses(operation);
			return operation;
		};
	}

	/**
	 * 모든 API에 공통 응답 코드 및 설명 추가
	 */
	private void addStandardResponses(Operation operation) {
		ApiResponses responses = operation.getResponses();
		
		// 이미 정의된 응답이 없는 경우 기본 응답 추가
		if (!responses.containsKey("200")) {
			responses.addApiResponse("200", new io.swagger.v3.oas.models.responses.ApiResponse().description("요청이 성공적으로 처리되었습니다."));
		}
		
		// 공통 오류 응답 추가
		responses.addApiResponse("400", new io.swagger.v3.oas.models.responses.ApiResponse().description("잘못된 요청입니다. 요청 파라미터를 확인하세요."));
		responses.addApiResponse("401", new io.swagger.v3.oas.models.responses.ApiResponse().description("인증이 필요합니다. 유효한 JWT 토큰이 필요합니다."));
		responses.addApiResponse("403", new io.swagger.v3.oas.models.responses.ApiResponse().description("접근 권한이 없습니다."));
		responses.addApiResponse("404", new io.swagger.v3.oas.models.responses.ApiResponse().description("요청한 리소스를 찾을 수 없습니다."));
		responses.addApiResponse("500", new io.swagger.v3.oas.models.responses.ApiResponse().description("서버 내부 오류가 발생했습니다."));
	}

	private void addResponseBodyWrapperSchemaExample(Operation operation, Class<?> type, String wrapFieldName) {
		final Content content = operation.getResponses().get("200").getContent();

		if (content != null) {
			content.keySet()
					.forEach(mediaTypeKey -> {
						final MediaType mediaType = content.get(mediaTypeKey);
						mediaType.schema(wrapSchema(mediaType.getSchema(), type, wrapFieldName));
					});
		}
	}

	@SneakyThrows
	private <T> Schema<T> wrapSchema(Schema<?> originalSchema, Class<T> type, String wrapFieldName) {
		final Schema<T> wrapperSchema = new Schema<>();
		Object instance = type.getDeclaredConstructor().newInstance();
		for (Field field : type.getDeclaredFields()) {
			if(field.getName().equals("objectMapper")) continue;
			field.setAccessible(true);
			Object value = field.get(instance);
			wrapperSchema.addProperty(field.getName(), new Schema<>().example(value));
			field.setAccessible(false);
		}
		wrapperSchema.addProperty(wrapFieldName, originalSchema);
		return wrapperSchema;
	}

	@Bean
	public OpenAPI openAPI() {
		SecurityScheme securityScheme = new SecurityScheme()
			.type(SecurityScheme.Type.HTTP)
			.scheme("bearer")
			.bearerFormat("JWT")
			.in(SecurityScheme.In.HEADER)
			.name("Authorization");
		
		SecurityRequirement securityRequirement = new SecurityRequirement().addList("BearerAuth");
		
		return new OpenAPI()
			.info(apiInfo())
			.addSecurityItem(securityRequirement)
			.schemaRequirement("BearerAuth", securityScheme)
			.servers(List.of(
				new Server().url("https://api.k-buddy.kr").description("Production Server"), 
				new Server().url("http://localhost:8080").description("Local Server"), 
				new Server().url("http://localhost:8082").description("Local Server")
			));
	}

	private Info apiInfo() {
		return new Info()
			.title("K-Buddy API 문서")
			.description("K-Buddy 서비스의 API 명세서입니다. 모든 API 응답은 ApiResponse 객체로 래핑되어 제공됩니다.")
			.version("1.0.0");
	}
}
