import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE = 'http://localhost:8080';

export const options = {
    stages: [
        { duration: '20s', target: 30 },
        { duration: '1m',  target: 30 },
        { duration: '10s', target: 0 },
    ],
    thresholds: {
        http_req_failed:   ['rate<0.01'],   // 실패율 1% 미만
        http_req_duration: ['p(95)<500'],   // p95 500ms 미만
    },
};

// 위 mongosh로 뽑은 sessionId들 붙여넣기 (여러 개면 더 현실적)
const SESSION_IDS = [
    '03c49398-b35f-4a89-905e-a1556cd49d60',
    'd76ffa5a-7132-4871-af3c-2861ea4067ef',
    '00f7b9df-f511-4924-8ffd-3de8219baacd',
    'df2c488e-2583-4c2b-94e3-9df29f042aeb',
    '420326f9-62a4-4f4d-b5b9-6130a3417c23',
    'bba3c12f-4bea-4ca7-b5fc-93f5ea1ca83b',
    'c3ec3c65-24f7-479f-bfe1-d6e1582ebfe1',
    '9e818a54-9287-4811-8b0c-f11456cd8600'
];

// 1) 테스트 시작 전 딱 1번 로그인해서 토큰 확보
export function setup() {
    const res = http.post(`${BASE}/api/auth/login`, JSON.stringify({   // ← 실제 로그인 경로로
        email: 'dummy0@test.com',
        password: __ENV.LOGIN_PW,
    }), { headers: { 'Content-Type': 'application/json' } });

    // 응답 쿠키에서 accessToken 추출 (쿠키 이름 실제 거로)
    const accessToken = res.cookies['accessToken'][0].value;
    return { accessToken };
}

// 2) 각 가상유저는 그 토큰을 쿠키로 달고 메세지 조회
export default function (data) {
    const sid = SESSION_IDS[Math.floor(Math.random() * SESSION_IDS.length)];
    const res = http.get(`${BASE}/api/chat/messages/${sid}`, {
        headers: { Cookie: `accessToken=${data.accessToken}` },
        timeout: '10s',
    });
    check(res, { 'status 200': (r) => r.status === 200 });
    sleep(0.5);
}