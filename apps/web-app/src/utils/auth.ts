export async function refreshAccessToken(): Promise<string | null> {
    try {
        console.debug('[auth] refreshAccessToken: POST /api/v1/auth/refresh');
        // Call the BFF's web refresh endpoint which handles cookie-based refresh
        const res = await fetch('http://localhost:8080/api/v1/auth/refresh', {
            method: 'POST',
            credentials: 'include', // send cookies (HTTP-only refresh token)
        });

        console.debug('[auth] refreshAccessToken: status', res.status);

        if (!res.ok) {
            // Try to read response for debugging
            let text: string | null = null;
            try {
                text = await res.text();
            } catch (e) {
                /* ignore */
            }
            console.warn('[auth] refreshAccessToken failed', res.status, text);
            return null;
        }

        const data = await res.json();
        if (data && data.accessToken) {
            console.debug('[auth] refreshAccessToken: received new access token');
            localStorage.setItem('accessToken', data.accessToken);
            return data.accessToken;
        }

        console.warn('[auth] refreshAccessToken: no accessToken in response', data);
        return null;
    } catch (err) {
        console.error('refreshAccessToken error:', err);
        return null;
    }
}

export async function fetchWithAuth(input: RequestInfo, init: RequestInit = {}): Promise<Response> {
    const token = localStorage.getItem('accessToken');
    const headers = new Headers(init.headers || undefined);
    if (token) headers.set('Authorization', `Bearer ${token}`);

    // Ensure cookies are sent to endpoints that rely on the HTTP-only refresh cookie
    const opts: RequestInit = { ...init, headers, credentials: 'include' };

    console.debug('[auth] fetchWithAuth:', input);

    let res: Response;
    try {
        res = await fetch(input, opts);
    } catch (err) {
        console.error('[auth] fetch error:', err);
        throw err;
    }

    // If the access token expired, attempt to refresh and retry once
    if (res.status === 401) {
        console.info('[auth] fetchWithAuth: received 401, attempting refresh');
        const newToken = await refreshAccessToken();
        if (!newToken) {
            // Refresh failed, clear local state and force login
            console.warn('[auth] fetchWithAuth: refresh failed, routing to login');
            localStorage.removeItem('accessToken');
            window.location.replace('/login');
            throw new Error('Session expired');
        }

        console.debug('[auth] fetchWithAuth: retrying original request with new token');
        headers.set('Authorization', `Bearer ${newToken}`);
        const retryOpts: RequestInit = { ...opts, headers };
        try {
            res = await fetch(input, retryOpts);
        } catch (err) {
            console.error('[auth] fetch retry error:', err);
            throw err;
        }
    }

    return res;
}

export default { refreshAccessToken, fetchWithAuth };
