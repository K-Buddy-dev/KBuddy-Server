package com.example.kbuddy_backend.docs.config;

import com.example.kbuddy_backend.common.advice.response.ApiResponse;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Slf4j
@Configuration
public class SwaggerConfig {

	@Bean
	public OperationCustomizer operationCustomizer() {
		return (operation, handlerMethod) -> {
			this.addResponseBodyWrapperSchemaExample(operation, ApiResponse.class, "data");
			return operation;
		};
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
		return new OpenAPI()
			.components(new Components())
			.info(apiInfo());
	}

	private Info apiInfo() {
		return new Info()
			.title("K-Buddy Swagger")
			.description("Springdoc을 사용한 Swagger UI")
			.version("1.0.0");
	}
}
