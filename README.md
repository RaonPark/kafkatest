## 사용 기술 스택
- Spring Boot
- Docker / Docker-compose
- Apache Kafka / Apache Kafka Streams
- Redis(standalone)
- MySQL
- Kafdrop - for visualizing kafka partitions, topic


## 두 개의 컨슈머 같은 토픽을 사용 (2 Consumers for same topic)
1. 두 개의 서로 다른 컨슈머를 만들고 같은 토픽을 참조하도록 한다.
- 구축 방법
  - Kafka Raft 모드로 3개의 노드를 사용하여 클러스터를 구성한다.
  - 도커를 사용하여 백엔드 이미지를 띄운다.
  - https://blog.naver.com/sumin9278/223638560679

### 두 개의 컨슈머 같은 토픽을 사용 (2 Consumers for same topic in practice)
- 두 개의 컨슈머로 같은 토픽을 참조하여 서로 다른 일을 하도록 한다.
- 하나는 DB, 하나는 컨트롤러에서 값을 반환하도록 한다.
- producer의 처리량 증가 로직을 사용하여 jMeter로 얼마나 달라졌는지 확인한다.
- https://blog.naver.com/sumin9278/223647883850

## Kafka와 Repository를 사용하는 메세징의 속도 차이 (throughputs differ from Kafka ACKS and ISRs)
### Kafka의 ACKS 설정에 의한 속도 차이 및 ISR의 개수에 의한 속도 차이
- https://blog.naver.com/sumin9278/223674974332

## Kafka Streams를 학습해보자. (Let's study Kafka Streams)
### 1. Kafka Streams 개요와 Join (Overview of KafkaStreams and JOIN)
- https://blog.naver.com/sumin9278/223683764086

### 2. Kafka Streams의 Reduce, Aggregation, Windowing (Reduce, Aggregation, Windowing in Kafka Streams)
- https://blog.naver.com/sumin9278/223687561101

### 3. Kafka Streams Support in Spring!
- https://blog.naver.com/sumin9278/223693447181

## Apache Avro를 사용하여 더 빠른 직렬화/역직렬화를 해보자. (Let's fast Serializing and Deserializing using Apache Avro)
- https://blog.naver.com/sumin9278/223689945739
