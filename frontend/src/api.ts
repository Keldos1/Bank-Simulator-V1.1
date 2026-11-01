
import type { Auth } from './types';

export class ApiError extends Error {
  constructor(
    public status: number,
    message: string
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

type CsrfResponse = {
  token: string;
  headerName: string;
};

async function getCsrf(): Promise<CsrfResponse> {
  let response: Response;

  try {
    response = await fetch('/api/auth/csrf', {
      credentials: 'include'
    });
  } catch {
    throw new ApiError(
      0,
      'Не удалось получить CSRF-токен. Проверь Spring Boot.'
    );
  }

  if (!response.ok) {
    throw new ApiError(
      response.status,
      `Ошибка получения CSRF: ${response.status}`
    );
  }

  return response.json();
}

export async function api<T>(
  path: string,
  _auth?: Auth,
  init: RequestInit = {}
): Promise<T> {
  const method = (init.method ?? 'GET').toUpperCase();
  const headers = new Headers(init.headers);

  // Не перезаписываем Content-Type, если он уже задан.
  // Это важно для formLogin в Spring Security.
  if (init.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }

  if (!['GET', 'HEAD', 'OPTIONS'].includes(method)) {
    const csrf = await getCsrf();
    headers.set(csrf.headerName, csrf.token);
  }

  let response: Response;

  try {
    response = await fetch('/api' + path, {
      ...init,
      method,
      headers,
      credentials: 'include',
      redirect: 'error'
    });
  } catch {
    throw new ApiError(
      0,
      'Не удалось выполнить запрос. Проверь Spring Boot и настройки авторизации.'
    );
  }

  if (!response.ok) {
    const error = await response.json().catch(() => ({}));

    const message =
      typeof error.message === 'string'
        ? error.message
        : response.status === 401
          ? 'Неверные данные или сессия истекла'
          : response.status === 403
            ? 'Доступ запрещён или CSRF-токен недействителен'
            : `Ошибка HTTP ${response.status}`;

    throw new ApiError(response.status, message);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  const text = await response.text();

  if (!text) {
    return undefined as T;
  }

  try {
    return JSON.parse(text) as T;
  } catch {
    throw new ApiError(
      response.status,
      'Сервер вернул неожиданный формат ответа.'
    );
  }
}

export async function loginRequest(
  phone: string,
  password: string
): Promise<void> {
  const body = new URLSearchParams({
    username: phone,
    password
  });

  await api<void>('/auth/login', undefined, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    },
    body
  });
}

export const post = <T>(
  path: string,
  auth: Auth | undefined,
  data?: unknown
): Promise<T> => {
  return api<T>(path, auth, {
    method: 'POST',
    ...(data !== undefined
      ? { body: JSON.stringify(data) }
      : {})
  });
};

export const patch = <T>(
  path: string,
  auth: Auth
): Promise<T> => {
  return api<T>(path, auth, {
    method: 'PATCH'
  });
};
