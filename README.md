# feed-postgresql-query-tuning
하이라이팅되는 내용을 피드의 형태로 유저에게 보여주는 간단한 api 형태 입니다. 피드 내 페이지는 유저가 해당 페이지에 하이라이트했던 내용을 포함합니다.

# 프로젝트 목표
- PostgreSQL 환경에서 대용량 트래픽을 가정하여 쿼리 성능을 최적화하는 것을 목표로 합니다.

# 가상 시나리오
- 사용자 피드 및 하이라이트 기능을 제공하는 서비스
- Feed : 특정 컬럼 기준 내림차순 정렬, 대용량 데이터
- Highlight : 생성 시간 기준으로 내림차순 정렬, 높은 트래픽 발생

# 사용 기술
- PostgreSQL

# 핵심 튜닝 포인트
- 쿼리 플랜을 통해서 인덱스 분석, 정렬 방식을 의도치 않은 정렬을 사용하진 않는지, 적절한 상황에 맞는 조인을 사용하는지 등등
    - 인덱스 : 가장 비효율적은 Full Scan(seq scan)을 하진 않는지 확인 후 인덱스 scan으로 개선 과정
    - 정렬 : 기존 메모리양은 4MB인데 이를 넘어서게 되면 외부 정렬을 하게 된다 따라서 외부 정렬을 하진 않는지 확인하고 개선
    - 조인 : 특정 상황에 맞는 조인이 있는데 nested loop, hash, merge 등이 존재하고 각각에 대해서 적절히 사용하는지 확인 후 왜 쿼리 플랜이 이러한 조인을 선택하게 되었는지 쿼리와 연관 지어서 설명

# 튜닝 전/후 성능 비교
- 각 쿼리 별로 튜닝 이전의 쿼리 플랜과 실행시간 분석 후 원인이 뭐였는지 확인 및 개선이 이 연습 프로젝트의 최종 목표입니다. 

# 첫 번째 쿼리 시도
![데이터 삽입 스크린샷](./image/insert-data.png "데이터 삽입")
- 테스트를 하기 위한 데이터 삽입
삽입 후 데이터에 대한 쿼리를 실행 및 통계를 확인 하였습니다.

## 초기 쿼리 성능 분석

### 실행 통계 분석
![쿼리 통계 분석](./image/statistics_sc.png "쿼리 통계 분석")

```
첫 번째 쿼리 (피드 목록 조회): 15ms, 21 rows
두 번째 쿼리 (하이라이트 조회): 2ms, 20 rows
총 JDBC 실행 시간: 1,767,662,300 nanoseconds (약 1.77초)
```

### 성능 문제점 식별

1. **과도한 실행 시간**
   - 단순한 피드 목록 조회에 1.77초는 매우 느림
   - 대용량 트래픽 환경에서는 허용 불가능한 수준

2. **복잡한 조인 구조**
   ```sql
   FROM feed_items fi1_0
   JOIN users u1_0 ON u1_0.id=fi1_0.user_id
   JOIN pages p1_0 ON p1_0.id=fi1_0.page_id
   LEFT JOIN highlights h1_0 ON h1_0.page_id=fi1_0.page_id AND h1_0.user_id=fi1_0.user_id
   LEFT JOIN mentions m1_0 ON m1_0.highlight_id=h1_0.id
   ```
   - 4개 테이블 조인으로 인한 복잡도가 증가하였고 이로 인해 카테시안 곱을 유발할 수 있음
   - LEFT JOIN으로 인한 데이터 증폭이 될 수 있음
   - join으로 인한 복잡도로 index를 안탈 수 도 있다는 것을 고려해야 됨

3. **WHERE 절 조건 복잡성**
   ```sql
   WHERE fi1_0.visibility=? OR fi1_0.visibility=? AND u1_0.id=? OR fi1_0.visibility=? AND m1_0.mentioned_user_id=?
   ```
   - 복합 OR 조건으로 인한 인덱스 활용 제한
   - 멘션 조건 검사를 위한 추가 조인 필요
   - 의도대로 된 쿼리가 아님

4. **GROUP BY 필요성**
   - LEFT JOIN으로 인한 중복 데이터 제거 필요
   - 9개 컬럼에 대한 그룹핑으로 인한 추가 오버헤드

### 예상 원인
- **비효율적 조인**: 멘션 조건 확인을 위한 불필요한 LEFT JOIN
- **정렬 오버헤드**: 데이터가 많다보니까 메모리에서 정렬이 해결이 안되서 외부 정렬을 사용할 수도 있음
- **비 인덱스** : 복잡한 join 조건으로 인해 인덱스가 안탈 수도 있음

### 쿼리 플랜 분석
![쿼리 플랜 피드 조회 쿼리](./image/query_plan_1(feedItem).png "쿼리 플랜 피드 조회 쿼리")
- 첫 번째 쿼리 : FeedItem 조회하는 쿼리 플랜 분석
쿼리 플랜을 보면 인덱스를 안타고 전부 seq scan 즉 테이블을 전부 탐색하는 모습을 볼 수 있었다. 이는 즉 조인으로 인한 복잡도 증가로 인해 인덱스를 타는 것 보다 전체를 탐색하는게 더 효율적이라고 옵티마이저가 판단한 것이다.

![쿼리 플랜 피드 조회 쿼리](./image/query_plan_1(feedItem2).png "쿼리 플랜 피드 조회 쿼리")
- 외부 정렬 사용 : 한번에 너무 많은 데이터를 가져와서 정렬 시행시 기본 메모리를 초과하여 정렬을 외부 정렬을 사용한 모습을 볼 수 있다. 기본적으로는 메모리에서 정렬이 가능하다면 퀵정렬을 사용하여 더 빠르지만 이는 더 느리게 동작한 모습이다.

### 개선 초점
조건에 대한 부분을 서브 쿼리로 따로 분리하여 조회를 하여 부하를 줄이고 인덱스를 타게 하도록 개선하자.

# 두 번째 쿼리 시도

## 최적화된 쿼리 성능 분석

### 실행 통계 분석 (최적화 후)
![쿼리 통계 분석](./image/statistics_sc_2.png "쿼리 통계 분석")
```
첫 번째 쿼리 (ID 조회): 0ms, 21 rows
두 번째 쿼리 (피드 목록 조회): 1ms, 21 rows  
세 번째 쿼리 (하이라이트 조회): 1ms, 20 rows
총 JDBC 실행 시간: 215,610,000 nanoseconds (약 0.216초)
```

### 최적화 성과
![1번째 쿼리 플랜](./image/query_plan_2(feedItem).png "쿼리 플랜")
![2번째 쿼리 플랜](./image/query_plan_2(feedItem_2).png "쿼리 플랜")
![3번째 쿼리 플랜](./image/query_plan_2(feedItem_3).png "쿼리 플랜")
쿼리 플랜 분석 : 각 쿼리 별로 쿼리가 인덱스를 잘 타고 있는 모습을 확인할 수 있었고 

![4번째 쿼리 플랜](./image/query_plan_2(feedItem_4).png "쿼리 플랜")
쿼리에서 정렬이 외부 정렬이 아닌 내부 정렬 즉 퀵 정렬을 사용하는 모습을 볼 수 있었다.

1. **극적인 성능 개선**
   - **실행 시간**: 1.77초 → 0.216초 (약 **8.2배 개선**)
   - **쿼리 개수**: 2개 → 3개 (단계별 최적화)
   - **각 쿼리 실행 시간**: 15ms → 0-1ms

2. **쿼리 구조 최적화**
   ```sql
   -- 첫 번째 쿼리: EXISTS 서브쿼리 활용
   SELECT fi1_0.id
   FROM feed_items fi1_0
   WHERE (
       fi1_0.visibility = 'PUBLIC'
       OR (fi1_0.visibility = 'PRIVATE' AND fi1_0.user_id = ?)
       OR (fi1_0.visibility = 'MENTIONED' AND EXISTS(
           SELECT 1 FROM mentions m1_0
           JOIN highlights h1_0 ON h1_0.id = m1_0.highlight_id
           WHERE h1_0.page_id = fi1_0.page_id AND m1_0.mentioned_user_id = ?
       ))
   )
   AND fi1_0.highlight_count > ?
   ORDER BY fi1_0.first_highlight_at DESC, fi1_0.id DESC
   LIMIT 20;
   ```
   - **LEFT JOIN 제거**: EXISTS 서브쿼리로 대체
   - **GROUP BY 제거**: 중복 데이터 문제 해결
   - **조건 추가**: `highlight_count > 0` 필터링

3. **단계별 쿼리 분리**
   ```sql
   -- 두 번째 쿼리: IN 절 활용
   SELECT fi1_0.id, u1_0.id, u1_0.username, ...
   FROM feed_items fi1_0
   JOIN users u1_0 ON u1_0.id = fi1_0.user_id
   JOIN pages p1_0 ON p1_0.id = fi1_0.page_id
   WHERE fi1_0.id IN (?, ?, ?, ...)
   ORDER BY fi1_0.first_highlight_at DESC, fi1_0.id DESC;
   ```
   - **단순한 INNER JOIN**: 복잡한 조건 제거
   - **IN 절 활용**: 첫 번째 쿼리 결과 활용

4. 하이라이트 조회
    - 변경없습니다.

### 최적화 기법 분석

1. **쿼리 분리 전략**
   - 복잡한 단일 쿼리를 3개의 단순한 쿼리로 분리
   - 각 쿼리의 목적과 역할 명확화
   - 네트워크 오버헤드보다 쿼리 복잡도 감소 효과가 더 큼

2. **EXISTS vs LEFT JOIN**
   - EXISTS: 존재 여부만 확인, 데이터 증폭 없음
   - LEFT JOIN: 실제 데이터 조인, GROUP BY 필요
   - 멘션 조건 확인에는 EXISTS가 더 효율적임임

3. **인덱스 활용도 개선**
   - 단순한 WHERE 조건으로 인덱스 활용도 증가
