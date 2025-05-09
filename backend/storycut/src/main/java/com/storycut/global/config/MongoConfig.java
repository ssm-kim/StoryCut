package com.storycut.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * MongoDB 설정 클래스
 * <p>
 * MongoDB 연결 및 레포지토리 설정을 담당합니다.
 * </p>
 */
@Configuration
@EnableMongoRepositories(basePackages = "com.storycut.domain.room.repository")
@EnableMongoAuditing
public class MongoConfig {

    /**
     * MongoDB 트랜잭션 매니저를 설정합니다.
     * <p>
     * MongoDB 4.0 이상에서는 트랜잭션을 지원합니다.
     * </p>
     *
     * @param dbFactory MongoDB 데이터베이스 팩토리
     * @return MongoDB 트랜잭션 매니저
     */
    @Bean
    MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
        return new MongoTransactionManager(dbFactory);
    }
}
