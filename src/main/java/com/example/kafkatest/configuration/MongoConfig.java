package com.example.kafkatest.configuration;

import com.example.kafkatest.configuration.properties.MongoProperties;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoClientFactoryBean;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoConfig {
    @Bean
    public MongoOperations mongoTemplate(MongoClient mongoClient, MongoProperties.MongoDBProperties properties) {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, properties.database);
        // Write Concern 은 Journal 을 사용하여 조금 더 신뢰성있는 프로그램을 만든다.
        // Journal 은 디스크에 먼저 한 번 쓰이고, 그 다음에 DB 에 저장된다.
        // 디스크에 한 번 쓰이기 때문에 공간복잡도는 올라가지만 만약 DB 가 크래시가 나는 경우
        // 이전 기록이 있기 때문에 journal 에서 처리되지 않은 데이터들을 가져오게된다.
        mongoTemplate.setWriteConcern(WriteConcern.JOURNALED);
        mongoTemplate.setReadPreference(ReadPreference.primary());
        // lifecycle event 는 false 로 해둔다.
        // listener 가 없다면 이 밸류는 false 가 될 수 있다.(can be false)
        // 만약 EntityCallbacks 가 존재하면 이는 listener 이므로 true 가 될 수 있다.
        mongoTemplate.setEntityLifecycleEventsEnabled(false);

        return mongoTemplate;
    }
}
