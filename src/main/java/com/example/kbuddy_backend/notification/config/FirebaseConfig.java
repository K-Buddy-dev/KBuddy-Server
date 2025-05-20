package com.example.kbuddy_backend.notification.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class FirebaseConfig {

    private final Environment env;

    public FirebaseConfig(Environment env) {
        this.env = env;
    }

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            // 환경 변수에서 Firebase 설정 정보를 가져와서 Map으로 구성
            Map<String, String> firebaseConfig = new HashMap<>();
            firebaseConfig.put("type", env.getProperty("FIREBASE_TYPE"));
            firebaseConfig.put("project_id", env.getProperty("FIREBASE_PROJECT_ID"));
            firebaseConfig.put("private_key_id", env.getProperty("FIREBASE_PRIVATE_KEY_ID"));
            
            // 개인 키 처리
            String privateKey = env.getProperty("FIREBASE_PRIVATE_KEY");
            if (privateKey != null) {
                // 개인 키에서 불필요한 이스케이프 문자 제거
                privateKey = privateKey.replace("\\n", "\n");
            }
            firebaseConfig.put("private_key", privateKey);
            
            firebaseConfig.put("client_email", env.getProperty("FIREBASE_CLIENT_EMAIL"));
            firebaseConfig.put("client_id", env.getProperty("FIREBASE_CLIENT_ID"));
            firebaseConfig.put("auth_uri", env.getProperty("FIREBASE_AUTH_URI"));
            firebaseConfig.put("token_uri", env.getProperty("FIREBASE_TOKEN_URI"));
            firebaseConfig.put("auth_provider_x509_cert_url", env.getProperty("FIREBASE_AUTH_PROVIDER_X509_CERT_URL"));
            firebaseConfig.put("client_x509_cert_url", env.getProperty("FIREBASE_CLIENT_X509_CERT_URL"));
            firebaseConfig.put("universe_domain", env.getProperty("FIREBASE_UNIVERSE_DOMAIN"));

            // Map을 JSON 문자열로 변환
            String jsonConfig = new com.fasterxml.jackson.databind.ObjectMapper()
                    .writeValueAsString(firebaseConfig);

            // JSON 문자열을 InputStream으로 변환
            GoogleCredentials credentials = GoogleCredentials.fromStream(
                    new ByteArrayInputStream(jsonConfig.getBytes())
            );

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();

            return FirebaseApp.initializeApp(options);
        }
        return FirebaseApp.getInstance();
    }
}