## 두 개의 컨슈머 같은 토픽을 사용
1. 두 개의 서로 다른 컨슈머를 만들고 같은 토픽을 참조하도록 한다.
- 구축 방법
  - Kafka Raft 모드로 3개의 노드를 사용하여 클러스터를 구성한다.
  - 도커를 사용하여 백엔드 이미지를 띄운다.
  - https://blog.naver.com/sumin9278/223638560679

### 두 개의 컨슈머 같은 토픽을 사용
- 두 개의 컨슈머로 같은 토픽을 참조하여 서로 다른 일을 하도록 한다.
- 하나는 DB, 하나는 컨트롤러에서 값을 반환하도록 한다.
- producer의 처리량 증가 로직을 사용하여 jMeter로 얼마나 달라졌는지 확인한다.
- https://blog.naver.com/sumin9278/223647883850