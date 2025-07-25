import random
import json
from locust import FastHttpUser, task, between

class FeedLoadTest(FastHttpUser):
    wait_time = between(0.1, 0.5)  # 빠른 요청 간격으로 DB 부하 생성
    
    def on_start(self):
        """테스트 시작 시 사용자 설정"""
        # 다양한 사용자 ID로 테스트 (1-200)
        self.user_id = random.randint(1, 200)
        
    @task(1)  # 단일 태스크 - 피드 첫 페이지 조회 (순수 DB 성능 테스트)
    def get_feed_first_page(self):
        """피드 첫 페이지 조회 - 캐시 없는 순수 DB 성능 측정"""
        # 랜덤한 사용자 ID로 다양한 쿼리 패턴 생성
        selected_user = random.randint(1, 200)
        
        params = {
            "userId": selected_user,
            "size": 20,
            "cursor": ""  # 첫 페이지는 빈 cursor
        }
        
        with self.client.get("/api/v1/feeds", params=params, catch_response=True) as response:
            if response.status_code == 200:
                try:
                    data = response.json()
                    response.success()
                except json.JSONDecodeError:
                    response.failure("Invalid JSON response")
            else:
                response.failure(f"Failed with status {response.status_code}")
