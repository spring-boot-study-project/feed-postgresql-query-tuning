import random
import json
from locust import FastHttpUser, task, between

class FeedLoadTest(FastHttpUser):
    wait_time = between(1, 3)  # 1-3초 대기
    
    def on_start(self):
        """테스트 시작 시 사용자 설정"""
        # 다양한 사용자로 테스트 (전역 캐시 효과 확인)
        self.user_id = random.randint(1, 100)
        self.cursor_cache = {}  # 사용자별 cursor 저장용
        
    @task(10)  # 가중치 10 - 첫 페이지 조회 (전역 캐시 효과 측정)
    def get_feed_first_page(self):
        """피드 첫 페이지 조회 - 전역 PUBLIC 캐시 효과 측정"""
        # 다양한 사용자로 테스트하여 전역 캐시 효과 확인
        selected_user = random.randint(1, 1000)  # 넓은 범위의 사용자
        
        params = {
            "userId": selected_user,
            "size": 20,
            "cursor": ""  # 첫 페이지는 빈 cursor
        }
        with self.client.get("/api/v1/feeds", params=params, catch_response=True) as response:
            if response.status_code == 200:
                try:
                    # cursor 정보 저장
                    data = response.json()
                    if 'nextCursor' in data and data['nextCursor']:
                        self.cursor_cache[selected_user] = data['nextCursor']
                    response.success()
                except json.JSONDecodeError:
                    response.failure("Invalid JSON response")
            else:
                response.failure(f"Failed with status {response.status_code}")
    
    @task(3)  # 가중치 3 - 다음 페이지 조회 (페이지네이션 테스트)
    def get_feed_next_page(self):
        """피드 다음 페이지 조회 (페이지네이션) - DB 성능 측정"""
        # cursor가 있는 사용자 중에서 다음 페이지 조회
        available_users = list(self.cursor_cache.keys())
        
        if available_users:
            selected_user = random.choice(available_users)
            cursor = self.cursor_cache[selected_user]
            
            params = {
                "userId": selected_user,
                "size": 20,
                "cursor": cursor
            }
            with self.client.get("/api/v1/feeds", params=params, catch_response=True) as response:
                if response.status_code == 200:
                    try:
                        # 다음 cursor 업데이트
                        data = response.json()
                        if 'nextCursor' in data and data['nextCursor']:
                            self.cursor_cache[selected_user] = data['nextCursor']
                        response.success()
                    except json.JSONDecodeError:
                        response.failure("Invalid JSON response")
                else:
                    response.failure(f"Failed with status {response.status_code}")
        else:
            # cursor가 없으면 첫 페이지 조회로 대체
            self.get_feed_first_page()
    

    
    @task(2)  # 가중치 2 - 다양한 사용자 피드 조회 (캐시 효과 검증)
    def get_various_user_feed(self):
        """다양한 사용자의 피드 조회 - 전역 캐시 효과 검증"""
        # 넓은 범위의 사용자로 테스트하여 전역 캐시 효과 확인
        various_user_id = random.randint(1, 2000)  # 매우 넓은 범위
        
        params = {
            "userId": various_user_id,
            "size": 20,
            "cursor": ""  # 첫 페이지 조회
        }
        with self.client.get("/api/v1/feeds", params=params, catch_response=True) as response:
            if response.status_code == 200:
                try:
                    # cursor 정보 저장 (필요시 다음 페이지 조회용)
                    data = response.json()
                    if 'nextCursor' in data and data['nextCursor']:
                        self.cursor_cache[various_user_id] = data['nextCursor']
                    response.success()
                except json.JSONDecodeError:
                    response.failure("Invalid JSON response")
            else:
                response.failure(f"Failed with status {response.status_code}")
