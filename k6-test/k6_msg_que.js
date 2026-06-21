import http from 'k6/http';
import { check } from 'k6';

// 측정 대상을 환경변수로 전환 (async/sync 번갈아 테스트)
const MODE = __ENV.MODE || 'async';  // 'async' 또는 'sync'
const URL = `http://localhost:8080/api/test/measure/${MODE}`;

export const options = {
    stages: [
        { duration: '30s', target: 50 },   // 30초 동안 동시 사용자 50명까지 증가
        { duration: '1m', target: 50 },    // 1분간 50명 유지
        { duration: '30s', target: 0 },    // 30초 동안 0명으로 감소
    ],
};

export default function () {
    const payload = JSON.stringify({
        userId: '6a23bfaeabf907a01157a34e',   // 측정용 더미 유저
        sessionId: '00f7b9df-f511-4924-8ffd-3de8219baacd',
    });

    const params = { headers: { 'Content-Type': 'application/json' } };

    const res = http.post(URL, payload, params);

    check(res, {
        'status is 200': (r) => r.status === 200,
    });
}