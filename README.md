## 사용 기술 스택
- Spring Boot
- Docker / Docker-compose
- Apache Kafka / Apache Kafka Streams
- Redis(standalone)
- MySQL
- Kafdrop - for visualizing kafka partitions, topic

## 카프카를 왜 사용할까? (Why Kafka?)
제가 카프카를 사용한 이유는 유저 알고리즘이 무엇보다 중요해진 시점에 대용량 데이터들을 어떻게 하면 빠르게 처리하며 의미있는 데이터로 만들지 궁금했기 때문입니다.<br>
예를 들어, 미적분 문제를 풀 때, 문제를 푸는 사람이 1분 내로 문제를 꾸준히 푼다고 해보겠습니다. 그러면 매우 쉬운 문제라고 판단할 수 있겠죠? 그러면 문제의 난이도를 높일 수 있습니다.<br>
만약 위와 같은 상황에서 문제의 난이도를 높이지 못한다면 문제가 너무 시시하다고 생각하여 앱을 종료하거나 사용하지 않을 수 있습니다.<br>
따라서, 유저의 데이터를 실시간으로 다루고, 빠르게 처리하는데 카프카가 매우 좋은 메소드라고 생각했습니다. 그래서 이번 프로젝트에서는 카프카와 카프카 스트림즈를 학습하여 추후에 실제로 사용해볼 수 있도록 하였습니다.<br>

I have been curious about how to process massive data and make it meaningful to own user. Because nowadays, improving algorithm for users is most significant challenge.<br>
For example, there is user solves a problem one minutes continuously. Then, You could think the problems should be easy for that user and give him harder problems.<br>
If you do not give him more challengable problems, the user might lose his interest and away from your app.<br>
So, with this thought, I think that Kafka is well suited for dealing with user data in real-time. Therefore, in this project, I'm now studying Kafka and Kafka Streams and programming various kinds of circumstances.<br>


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

## Apache Avro를 사용하여 더 빠른 직렬화/역직렬화를 해보자. (Let's fast Serializing and Deserializing with Apache Avro)
- https://blog.naver.com/sumin9278/223689945739
