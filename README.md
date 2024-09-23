
## KAFKA pub/sub

```
📑 시나리오
donate microservice의 BookDonated 이벤트가 publish되면, books microservice의 BookRegistered 이벤트가 subscribe한다.
```

event 발생 이전
- 발생한 donate 없음

![image](https://github.com/user-attachments/assets/97012543-d3f5-454f-9ad9-f0bf2d03790b)




- books에 등록된 책 없음


![image](https://github.com/user-attachments/assets/0334b722-07c6-4cef-9ef1-f8b6c2621a71)


donate 이벤트 발생 (bookId=1)

- bookId=1 등록 확인

![image](https://github.com/user-attachments/assets/30c890e0-a1a7-40eb-a26c-97f0c72a8ee7)


event consumer 확인

- bookDonated(pub)에 이어 bookRegistered(sub) 확인

![image](https://github.com/user-attachments/assets/4c5ae699-9e2b-4483-aa87-cdd55999a285)


books에 정상적으로 등록되었는지 확인

![image](https://github.com/user-attachments/assets/bdb2f370-f730-4069-9802-7124eb28806f)





