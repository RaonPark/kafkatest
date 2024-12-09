package com.example.kafkatest.kstreams;

import com.example.kafkatest.entity.Articles;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.kstream.internals.TimeWindow;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.WindowStore;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.time.Duration;

public class KStreamsTestMain {
    private final static ObjectMapper objectMapper = new ObjectMapper();
    public static void main(String[] args) {
        StreamsBuilder builder = new StreamsBuilder();

        /*
         * 예를 들어, {"articleId": "123", "liked": 123}이 있고,
         * 이벤트 스트림으로 {"articleId": "123", "liked": 34}, {"articleId": "123", "liked": 92}가 들어왔다고 해보자.
         * 그러면 우선 Aggregator 부터 살펴보면, Aggregator 의 키는 <K, V, VAgg>이다.
         * 즉, 차례로, Key, Value, Value Aggregate(aggregate 할 값)
         * 우리는 liked 가 long 값이므로 VAgg 는 Long type 이 될 것이다.
         */
        Aggregator<String, String, Long> aggregator = (key, value, aggregate) -> {
            try {
                Articles article = objectMapper.readValue(value, Articles.class);
                return article.getLiked() + aggregate;
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        };

        JsonDeserializer<Articles> jsonDeserializer = new JsonDeserializer<>(Articles.class, false);
        jsonDeserializer.addTrustedPackages("*");
        Serde<Articles> articlesSerde = Serdes.serdeFrom(new JsonSerializer<>(), jsonDeserializer);

        KStream<String, String> stream = builder.stream("liked-article-topic");

        /*
         * 그리고 여길 보자, 우리는 aggregate 를 할 것인데,
         * 우선은 aggregate 의 반환값은 KTable<VR> 이고, 스트림은 <String, String> 타입이다.
         * 우리는 Long type 의 aggregate 을 할 것이므로 Initializer<VR>은 Long type 의 0L을 반환한다.
         * aggregator 는 <K, V, VR> 이다.
         * Materialized 는 기본적으로 설정해주지 않으면 Kafka 에서는 unnamed internal state stores 를 생성한다.
         * 따라서 Materialized 를 하면 store 의 이름을 부여할 수 있게 해준다.
         * 만약 Materialized 를 하지 않으면 store 의 이름이 없으므로 query 를 할 수 없다.
         * Materialized 의 타입은 <V, VR, KeyValueStore<Bytes, byte[]>> 타입이다.
         */
        KTable<String, Long> likedArticles = stream.groupByKey().aggregate(() -> 0L,
                aggregator,
                Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as("LIKED_ARTICLES")
                        .withCachingDisabled());

        likedArticles.toStream().to("liked-articles-topic", Produced.with(Serdes.String(), Serdes.Long()));

        // 그러면 이제 Windowing 을 사용해보자. 1시간마다 가장 좋아요를 많이 받은 게시물을 갱신하고 싶다.
        StreamsBuilder secondBuilder = new StreamsBuilder();

        KStream<String, String> likedStream = secondBuilder.stream("liked-topic");
        Aggregator<String, String, Long> secondAggregator = (key, value, aggregate) -> {
            try {
                Articles articles = objectMapper.readValue(value, Articles.class);
                return articles.getLiked() + aggregate;
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        };

        ForeachAction<String, Long> peekAction = (article, likedAgg) -> {
            try {
                Articles likedArticle = objectMapper.readValue(article, Articles.class);
                System.out.println("article title: " + likedArticle.getTitle() + " liked : " + likedAgg);
            } catch(JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        };

        likedStream.groupByKey()
                .windowedBy(TimeWindows.ofSizeAndGrace(Duration.ofHours(1), Duration.ofMinutes(5)))
                .aggregate(() -> 0L,
                        secondAggregator,
                        Materialized.<String, Long, WindowStore<Bytes, byte[]>>as("LIKED_STORE")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(Serdes.Long()))
                .suppress(Suppressed.untilWindowCloses(Suppressed.BufferConfig.unbounded()))
                // 여기까지는 반환 값이 KTable 이다.
                .toStream()
                // KTable 의 반환 값은 KTable<Windowed<? super String>, ? super Long> 이다.
                // 따라서 Windowed Key 를 하나의 String key 로 반환하기 위해 map() 함수를 사용하여 키를 변환시켜줘야 한다.
                .map((wk, value) -> KeyValue.pair(wk.key(), value))
                .peek(peekAction)
                .to("liked-hour-topic", Produced.with(Serdes.String(), Serdes.Long()));
    }
}
