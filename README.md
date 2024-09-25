
# Developing Cloud Native Application - Azure

## 🗺️ 설계

### 서비스 시나리오

주제: 도서 기부와 대여가 이루어지는 도서관 서비스 

기능 요구사항
1. 사용자는 책을 기부할 수 있다.
2. 기부한 책은 책 목록에 등록된다.
3. 대여 요청을 통해 등록된 책을 대여할 수 있다.
4. 존재하지 않는 책의 대여 요청은 실패한다.
5. 현재 등록된 책들의 대여 상태를 확인할 수 있다.

### 아키텍처 설계

![image](https://github.com/user-attachments/assets/c574be5a-cebb-4c00-a733-5ebb6aa300bc)


- gateway를 단일 진입점으로 각 서비스 routing
- kafka를 통한 pub/sub (서비스간의 분리)
- Secret 설정을 통한 배포 환경 분리
- HPA를 통한 auto scaling
- Azure PVC 클라우드 스토리지 활용
- Jenkins를 이용한 자동 CI/CD
- Istio를 이용한 service mesh
- grafana, prometheus를 이용한 통합 모니터링

### 이벤트스토밍

![image](https://github.com/user-attachments/assets/311d32d2-6e92-4c62-9e50-23861c197f2a)

- 총 4개의 Bounded Context (기능 기준 분류)
  - donate [bookId]
    - 사용자에 의한 책 기부 이벤트 발생
  - books [bookId, bookStatus]
    - 기부된 책 등록,
    - 등록된 책은 대여 완료 이벤트
    - 등록되지 않은 책은 대여 반려 (request status 조정)
  - request [bookId, requestId, orderStatus)
    - 사용자에 의한 대여 요청 이벤트 발생
  - bookdetail [bookId, status]
    - 책 목록과 각 책의 "등록됨", 혹은 "대여완료됨"의 status 조회 가능

---

## 🖥️ Dev


### Gateway


spring cloud gateway - gateway 서비스의 application.yml에 설정 작성

```yml
server:
  port: 8088

---

spring:
  profiles: default
  cloud:
    gateway:
#<<< API Gateway / Routes
      routes:
        - id: donate
          uri: http://localhost:8082
          predicates:
            - Path=/donates/**, 
        - id: request
          uri: http://localhost:8083
          predicates:
            - Path=/requests/**, 
        - id: bookdetail
          uri: http://localhost:8084
          predicates:
            #- Path=,
            - Path=/bookdetails, 
        - id: books
          uri: http://localhost:8085
          predicates:
            - Path=/books/**, 
        - id: frontend
          uri: http://localhost:8080
          predicates:
            - Path=/**
#>>> API Gateway / Routes
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins:
              - "*"
            allowedMethods:
              - "*"
            allowedHeaders:
              - "*"
            allowCredentials: true


---

spring:
  profiles: docker
  cloud:
    gateway:
      routes:
        - id: donate
          uri: http://donate:8080
          predicates:
            - Path=/donates/**, 
        - id: request
          uri: http://request:8080
          predicates:
            - Path=/requests/**, 
        - id: bookdetail
          uri: http://bookdetail:8080
          predicates:
            - Path=/bookdetails, 
        - id: books
          uri: http://books:8080
          predicates:
            - Path=/books/**, 
        - id: frontend
          uri: http://frontend:8080
          predicates:
            - Path=/**
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins:
              - "*"
            allowedMethods:
              - "*"
            allowedHeaders:
              - "*"
            allowCredentials: true

server:
  port: 8080
```




각각의 microservice들 구동한 후 gateway 포트인 8080으로 request시 정상 작동 확인

- donate 요청(POST)
  
![image](https://github.com/user-attachments/assets/04cdb40f-b7fa-49e0-92fa-060ca0e98f4f)

- donate 요청(GET)
  
![image](https://github.com/user-attachments/assets/c26ed9ea-6730-401c-a94c-b0df64b30aea)

- request 요청(POST)
  
![image](https://github.com/user-attachments/assets/38d80bce-e768-415b-ade8-6ade5df94efe)

- request 요청(GET)
  
![image](https://github.com/user-attachments/assets/95f51ab0-336d-41ba-bd2d-26f5aefb1b93)




### SAGA - KAFKA pub/sub

kafka: pub/sub 모델의 메시지 큐 형태로 동작하는 분산형 데이터 스트리밍 플랫폼


```
📑 시나리오
donate microservice의 BookDonated 이벤트가 publish되면, books microservice의 BookRegistered 이벤트가 subscribe한다.
```


event 발생 이전
- 발생한 donate 없음

![image](https://github.com/user-attachments/assets/fe30e524-e955-49f8-8aa5-3a45aaf34a9f)



- books에 등록된 책 없음


![image](https://github.com/user-attachments/assets/133ab680-eb10-4161-82a8-71ac8be357df)



PolicyHandler.java에 BookDonated 이벤트 발생시 registerBook() 트리거하게 설정되어있음

```java
// PolicyHandler.java

public void wheneverBookDonated_RegisterBook(
        @Payload BookDonated bookDonated
    ) {
        BookDonated event = bookDonated;
        System.out.println(
            "\n\n##### listener RegisterBook : " + bookDonated + "\n\n"
        );

        // Sample Logic //
        Books.registerBook(event);
    }
```



donate 이벤트 발생 (bookId=1)

- bookId=1 등록

![image](https://github.com/user-attachments/assets/8b8eb42b-c897-47c1-bfb2-c3d00713aa34)



event consumer 확인

- bookDonated(pub)에 이어 bookRegistered(sub) 확인


![image](https://github.com/user-attachments/assets/ea84f75a-4ab2-4210-a9d4-9d0ca78b0355)



books에 정상적으로 등록되었는지 확인


![image](https://github.com/user-attachments/assets/13b9c076-d4c8-4be7-a9f5-c989086bfddf)


### Compensation

보상 처리: 유효하지 않은 요청 등이 발생하여 트랜잭션을 취소하는 경우 원복하거나 보상하는 처리

```
📑 시나리오
사용자가 존재하지 않는 책의 bookId로 대여 요청하는 경우 (예외 케이스가 발생하는 경우)
이벤트를 NonexistentBook으로 분기하여 요청의 orderStatus를 "cancelled"로 변경한다.
```


구현

```java
public static void checkOutBook(CheckoutRequested checkoutRequested) {
        repository().findById(Long.valueOf(checkoutRequested.getBookId()))
        .ifPresentOrElse(book -> {
            // 책이 존재하는 경우
            book.setBookStatus("lent");
            repository().save(book);
            BookCheckedOut bookCheckedOut = new BookCheckedOut(book);
            bookCheckedOut.publishAfterCommit();
        }, () -> {
            // 책이 존재하지 않는 경우
            NonexistentBook nonexistentBook = new NonexistentBook();
            nonexistentBook.setBookId(checkoutRequested.getBookId()); // bookId 설정
            nonexistentBook.setRequestId(checkoutRequested.getRequestId()); // requestId 설정
            nonexistentBook.publishAfterCommit();
        });
    }
```

1. book을 등록한다 (bookId=1)
    
    `http localhost:8088/donates bookId=1`
   
    ![image](https://github.com/user-attachments/assets/2366ee77-5456-4bbb-9b94-22586522f08e)

    
3. request(bookId 1 책 대여 요청)를 등록한다 (bookId=1, requestId=1, orderStatus=requested)
    
    `http localhost:8088/requests bookId=1 requestId=1 orderStatus=requested`
    
   ![image](https://github.com/user-attachments/assets/7e0b63ec-73c3-4113-aaeb-a03d3b405345)

    
4. requests의 상태 확인한다
    
    `http localhost:8088/requests/1`
    
   ![image](https://github.com/user-attachments/assets/6d5a5320-dbaf-4c1d-94c8-f4c40f293116)

    
5. books의 상태 확인한다 (bookStatus=lent)
    
    `http localhost:8088/books/1`
    
    ![image](https://github.com/user-attachments/assets/3079090e-01f0-4b4d-b33f-0af73bfc69c9)

    
6. request를 등록한다 (bookId=2, requestId=2, orderStatus=requested)
    
    `http localhost:8088/requests bookId=2 requestId=2 orderStatus=requested`
    
    ![image](https://github.com/user-attachments/assets/e54b6de8-43b9-4aba-b7d0-ea12a01f3897)

    
7. request/2의 상태 확인한다 (cancelled여야 함)
    
    `http localhost:8088/requests/2`
    
    ![image](https://github.com/user-attachments/assets/25bd9ac4-7e1d-444a-844b-900d2466162d)

    
8. books에는 bookId=2가 없음 재확인
    
    ![image](https://github.com/user-attachments/assets/48ce2b4c-130a-4272-ba3b-8866a61348b5)

    
9. consumer 확인 
    
    ![image](https://github.com/user-attachments/assets/42e1c054-efcd-4312-9eed-54c4da28ad80)

    


### CQRS - 분산 데이터 프로젝션

CQRS: 읽기와 업데이트 작업을 분리한다.
읽기 모델을 따로 분리하여 조회 성능을 높이고, 장애에서 격리한다.
해당 서비스에서는 책들의 대여 상태만을 조회하는 ReadModel을 분리하여 테스트한다.

```
📑 시나리오
donate microservice의 BookDonated 이벤트가 publish되면, books microservice의 BookRegistered 이벤트가 subscribe한다.
```

책 기부 이벤트가 발생하면

![image](https://github.com/user-attachments/assets/c53461fd-ca5e-46a1-8b80-ee5d98f15240)

bookdetails에서 "등록됨" 상태로 조회 가능

![image](https://github.com/user-attachments/assets/1305dfb8-49f7-427a-9862-e1a51c232475)

책 여러개 등록 시에는 여러 개 조회 가능

![image](https://github.com/user-attachments/assets/9471bf94-ef0d-4733-882c-fe7a0b229fd2)

대여 요청 정상 처리된 이후에도 확인
`http localhost:8088/requests bookId=1 requestId=1 orderStatus=requested`

![image](https://github.com/user-attachments/assets/c000b90d-026f-4b1d-94f2-bc5ddbd95f94)

해당 bookId의 상태가 "대여완료됨" 으로 바뀐 것 확인

![image](https://github.com/user-attachments/assets/eed932f1-da3c-4eb4-aaee-05cc0ea49392)

ReadModel 관련 서비스 제외 다른 모델 종료 후에도 정상 조회 확인

![image](https://github.com/user-attachments/assets/b10f9549-b4ff-42c6-bc47-a944b756b8ef)


![image](https://github.com/user-attachments/assets/90027d36-1133-496c-b932-c9cf1092702c)





---

## 📊 Ops

Azure 서비스를 이용하여 클라우드 환경에서의 Kubernetes 클러스터에 배포하여 서비스를 운영한다.

Azure portal에서 Kubernetes Service, Conatiner Registry 등 생성

![image](https://github.com/user-attachments/assets/07e6708d-073d-4b11-8072-559b7d864bff)

dockerhub에 이미지들을 업로드하고, 각 서비스의 deployment.yaml이 그 이미지들을 바라보게 하여 클러스터에 서비스들을 배포한다.

![image](https://github.com/user-attachments/assets/a8adcdde-59f5-46ef-bb10-ecf9493e244c)

```
mvn package -B -Dmaven.test.skip=true

(jar 확인)

 docker build -t drsuneamerr/gateway:v1 .     
 docker push drsuneamerr/gateway:v1

(yaml 설정 변경)
kubectl apply -f kubernetes/deployment.yaml
kubectl apply -f kubernetes/service.yaml
```



각 서비스의 클라우드 배포 확인 및 gateway의 IP 확인

![image](https://github.com/user-attachments/assets/49ba7d7f-c2b2-47af-9370-42b00ff503a8)


로컬에서 테스트했던 것과 같이 gateway IP의 get, post 요청이 잘 이루어지는지 확인
`[POST] http 20.249.65.252:8080/donate bookId=1`

![image](https://github.com/user-attachments/assets/2840c236-78cb-4670-8596-d97c4883066a)

`[GET] http 20.249.65.252:8080/books`

![image](https://github.com/user-attachments/assets/891320c7-7179-440b-9b74-4f361a7717d4)


### AutoScaling - HPA

특정 서비스의 CPU 사용률이 일정 기준 이상으로 증가하는 경우 pod의 수를 늘려 scale을 조정한다.

테스트를 위한 siege 사용

![image](https://github.com/user-attachments/assets/27a44dd9-4582-4fbc-83df-525a0510370f)


metric server 적용 확인 : Kubernetes 클러스터에서 kubectl top 명령어를 사용하여 Pod의 리소스 사용량을 확인할 수 있다면, 메트릭 서버가 정상적으로 설치되어 운영되고 있다는 뜻
* metric server: Kubernetes 내에 존재하는 Pod의 메트릭을 실시간으로 수집해 kube-api 서버에 안정적으로 전달하는 역할

books 서비스에 CPU 사용률에 따른 hpa autoscale 설정

![image](https://github.com/user-attachments/assets/2706fd63-d446-47b1-a651-2764ec883c7a)

![image](https://github.com/user-attachments/assets/bc5667fa-ba60-4fd7-a2ca-35baf32ffe2a)


deployment.yaml에 아래와 같은 설정이 추가되어 있어야 정상적으로 HPA 적용 가능
```yaml
      ...
      containers:
          ...
          resources:
            requests:
              cpu: "200m" 
```

수정한 yaml로 배포하여 테스트

![image](https://github.com/user-attachments/assets/edbb96a1-95f5-4873-a246-93399ae242ed)

배포 완료 확인

![image](https://github.com/user-attachments/assets/46b91baa-6135-4f58-9e78-14e49af33c42)


부하테스트
- 테스트 전의 cpu 상태 확인
  
  ![image](https://github.com/user-attachments/assets/618c6b9b-668a-4ef3-81a2-2d7317fcb2d9)

- seige로 부하 후 pod 수 늘어나는 것 확인 `siege -c20 -t40S -v http://20.249.65.252:8080/books`
  
  ![image](https://github.com/user-attachments/assets/25b4bf06-49ee-40c3-b9a1-0e3c497a144c)

- CPU 사용률 향상, replica 수가 maxpods인 3개까지 늘어나는 것 확인
  ![image](https://github.com/user-attachments/assets/33bddc44-f5cc-4ad7-8600-e00519c6eb63)


### Secret을 통한 배포 환경 분리

bookdetail 서비스 private으로 전환

![image](https://github.com/user-attachments/assets/d1bb3268-b1c3-4a49-ba63-2f28a7d39ed4)


private 상태로 재배포 시도 - 실패

![image](https://github.com/user-attachments/assets/25886a87-c054-4422-b525-20488c881c70)


실패 사유: image pull에 실패 

![image](https://github.com/user-attachments/assets/0522a348-c657-48bd-b30b-7a33a043fc31)
![image](https://github.com/user-attachments/assets/41187ccd-d547-4119-a675-809a1e761aba)

kubernetes 환경에 secret 설정
```
kubectl create secret docker-registry my-secret \
--docker-server=https://index.docker.io/v1/ \
--docker-username=drsuneamerr \
--docker-password=[password] \
--docker-email=sunyeong0412@naver.com
```

![image](https://github.com/user-attachments/assets/c2e51d22-a636-4816-8a2c-8c4c365b92a5)

deploy-secret.yaml 수정 후 재배포

```
# 추가된부분
  ...
  imagePullSecrets:
  - name: my-secret
```

![image](https://github.com/user-attachments/assets/bdd5b142-7a84-4142-9c7c-9d86140a98dc)

설정된 secret로 private 상태의 bookdetail 서비스 정상 배포 확인

![image](https://github.com/user-attachments/assets/d8552535-2a34-4097-b4a0-1511ab82b3e8)


### PVC

PVC 생성

![image](https://github.com/user-attachments/assets/69d645fe-2830-4752-8467-ef271576bd7e)


1개의 request 서비스 pod에서 파일 생성

![image](https://github.com/user-attachments/assets/6e8c0df8-4d5c-41b2-a864-a320946fb840)


2개로 scale out

![image](https://github.com/user-attachments/assets/879146b8-34ec-4c72-92a5-d6f285cadad9)

새로 생긴 pod 확인

![image](https://github.com/user-attachments/assets/9192e40f-2d9d-4a70-bc88-c0f741fbf5d4)


다른 pod에서 생성된 파일 확인

![image](https://github.com/user-attachments/assets/fb868a6e-1887-4211-930b-b2a4b1c4edcc)


readwritemany - 새로 생긴 pod에서 생성한 파일도 기존 pod에서 조회되는 것 확인

![image](https://github.com/user-attachments/assets/6d843b6e-293d-462e-9584-2bd789af2604)


### Liveness/Readines - 무정지 배포 

서비스의 안정성을 위해 새로 배포 시에도 서비스가 중단이 없게 함 

deployment.yaml에 관련 설정 작성
```yaml
 # donate/kubernetes/deployment.yaml         
          ...
          readinessProbe:
            httpGet:
              path: '/actuator/health'
              port: 8080
            initialDelaySeconds: 10
            timeoutSeconds: 2
            periodSeconds: 5
            failureThreshold: 10
          livenessProbe:
            httpGet:
              path: '/actuator/health'
              port: 8080
            initialDelaySeconds: 120
            timeoutSeconds: 2
            periodSeconds: 5
            failureThreshold: 5
```

donate replicaset 수를 3개로 늘려본다

![image](https://github.com/user-attachments/assets/5f2f5f46-4a2b-4d81-82a4-d6175cb71de0)


재배포 후 po들의 상태 관찰 - 새로 배포된 pod가 Running 상태가 되고 나서야 기존 pod 하나씩 Terminate 시작

![image](https://github.com/user-attachments/assets/5b21acb8-eb48-409f-b2cb-aff855d2ee53)

새로 생성된 pod들 상태 확인

![image](https://github.com/user-attachments/assets/de39c94e-1986-4562-9633-e3388844b093)



### Istio를 이용한 service mesh

클러스터 환경에 istio 설치

![image](https://github.com/user-attachments/assets/e5875087-40a4-4e58-8cf5-ab20617506ff)


Kiali를 이용한 Monitoring 

injection 이전에는 sidecar가 missing 상태

![image](https://github.com/user-attachments/assets/105d64f4-2477-4726-8277-212697d22aae)


각 서비스의 deployment에 injection 관련 설정 추가 후 재배포 

```
  template:
    metadata:
      labels:
        app: request
        sidecar.istio.io/inject: "true"
```

배포된 서비스들 확인 가능 

![image](https://github.com/user-attachments/assets/8af125fb-41b6-476c-931d-3b0ee1bea820)

![image](https://github.com/user-attachments/assets/bc1d812b-22a9-4a62-bc57-0c3bba281781)


500 에러 발생시킨 후 실제로 모니터링 가능한지 확인

![image](https://github.com/user-attachments/assets/401c0374-3164-411d-883a-7eabd28ddf25)

![image](https://github.com/user-attachments/assets/4498c78d-02f6-4cd0-80bf-45aa1d656fb5)


sidecar 설정 완료 후 Ready 상태 2/2로 모두 변경됨


![image](https://github.com/user-attachments/assets/b700b766-dd5d-4f4d-b03f-13068983d0f6)


### Monitoring

![image](https://github.com/user-attachments/assets/63f4c061-a816-4868-86a1-2e9ac5e8b898)

grafana와 prometheus external ip 확인

![image](https://github.com/user-attachments/assets/71332f0f-f6e2-4b66-b65a-d36804bd8c4f)

조회한 external ip의 9090 포트로 prometheus 접속

![image](https://github.com/user-attachments/assets/45964822-c3c6-4bce-92ba-3fec92a90aaa)


istio_requests_total 메트릭 이용하여 요청 조회

![image](https://github.com/user-attachments/assets/34729696-cefd-4e37-9af2-d8a6e49a003f)


books 서비스에 부하 발생시켜본다. `siege -c20 -t30S -v http://20.249.65.252:8080/books`

![image](https://github.com/user-attachments/assets/31a358e9-690b-41e1-9ac8-8217ac70aa67)


실행시킨 부하 모니터링 확인
`rate(istio_requests_total{app="books",destination_service="books.default.svc.cluster.local",response_code="200"}[5m])`

![image](https://github.com/user-attachments/assets/c7aca82a-cfb3-4dec-8124-a2afebf9232b)


grafana에서도 books 서비스에 적용한 부하 그래프로 모니터링 가능

![image](https://github.com/user-attachments/assets/90db9343-b040-4eaa-80e1-4f4fcefa6b14)


![image](https://github.com/user-attachments/assets/dd82be77-b97f-43a6-b9a0-71ed493040e2)



### Jenkins pipeline

코드 상에 변경 사항이 있는 경우 jenkins에서 git repository의 변화를 감지하여 자동 빌드를 실행할 수 있게 한다.

azure vm 만들고 인바운드 규칙 설정

![image](https://github.com/user-attachments/assets/97399505-6a31-426e-947e-310ddc877171)


Jenkins가 구동될 VM의 외부 IP 확인 후 접속


![image](https://github.com/user-attachments/assets/3e3b9688-76e8-40af-a55a-df573f882774)

사용될 서비스 (books)와 관련하여 Jenkinsfile 생성

```
pipeline {
    agent any

    environment {
        REGISTRY = 'user16.azurecr.io'
        IMAGE_NAME = 'books'
        AKS_CLUSTER = 'user16-aks'
        RESOURCE_GROUP = 'user16-rsrcgrp'
        AKS_NAMESPACE = 'default'
        AZURE_CREDENTIALS_ID = 'Azure-Cred'
        TENANT_ID = 'f46af6a3-e73f-4ab2-a1f7-f33919eda5ac' // Service Principal 등록 후 생성된 ID
    }
 
    stages {
        stage('Clone Repository') {
            steps {
                checkout scm
            }
        }
        
        stage('Maven Build') {
            steps {
                withMaven(maven: 'Maven') {
                    dir('books') {    
                        sh 'mvn package -DskipTests'
                    }
                }
            }
        }
        
        stage('Docker Build') {
            steps {
                script {
                    dir('books') {
                        image = docker.build("${REGISTRY}/${IMAGE_NAME}:v${env.BUILD_NUMBER}")
                    }
                }
            }
        }
        
        stage('Azure Login') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: env.AZURE_CREDENTIALS_ID, usernameVariable: 'AZURE_CLIENT_ID', passwordVariable: 'AZURE_CLIENT_SECRET')]) {
                        sh 'az login --service-principal -u $AZURE_CLIENT_ID -p $AZURE_CLIENT_SECRET --tenant ${TENANT_ID}'
                    }
                }
            }
        }
        
        stage('Push to ACR') {
            steps {
                script {
                    sh "az acr login --name ${REGISTRY.split('\\.')[0]}"
                    sh "docker push ${REGISTRY}/${IMAGE_NAME}:v${env.BUILD_NUMBER}"
                }
            }
        }
        
        stage('CleanUp Images') {
            steps {
                sh """
                docker rmi ${REGISTRY}/${IMAGE_NAME}:v$BUILD_NUMBER
                """
            }
        }
        
        stage('Deploy to AKS') {
            steps {
                script {
                    dir('books') {
                        sh "az aks get-credentials --resource-group ${RESOURCE_GROUP} --name ${AKS_CLUSTER}"
                        sh """
                        sed 's/latest/v${env.BUILD_ID}/g' kubernetes/deployment.yaml > output.yaml
                        cat output.yaml
                        kubectl apply -f output.yaml
                        kubectl apply -f kubernetes/service.yaml
                        rm output.yaml
                        """
                    }
                }
            }
        }
    }
}
```

github repository에 webhook 설정

![image](https://github.com/user-attachments/assets/c132c2e1-1e5e-4a29-a11e-94f957113296)


git에 코드 수정 후 자동 빌드되는지 확인


![image](https://github.com/user-attachments/assets/e5159cf4-7663-4a1a-9b3c-be579979c3b7)


![image](https://github.com/user-attachments/assets/ce86f382-3bb2-48a6-a313-286b44b18bd4)

![image](https://github.com/user-attachments/assets/85ebbaa6-4990-48e2-8b53-3e46e102c3dc)
