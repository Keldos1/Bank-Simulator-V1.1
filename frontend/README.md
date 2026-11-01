# Bank Simulator — React frontend

Frontend для учебного банковского приложения Spring Boot.

## Запуск

1. Установите Node.js (LTS).
2. В каталоге `bank-simulator-frontend` выполните `npm install`.
3. Запустите Spring Boot backend и PostgreSQL.
4. Выполните `npm run dev`.
5. Откройте http://localhost:5173.

Vite перенаправляет запросы `/api/*` на `http://localhost:8081`. Если Spring Boot слушает порт 8080, измените `target` в `vite.config.ts` на `http://localhost:8080` и перезапустите Vite.

## Авторизация

Используется HTTP Basic Auth. Логин и пароль хранятся только в React state (в памяти вкладки), не в localStorage. После обновления страницы требуется повторный вход. Для реального банка необходимы HTTPS и более подходящая схема сессий/токенов.

`/api/auth/me` в текущем SecurityConfig разрешён только CLIENT. Для ADMIN интерфейс проверяет доступ через `GET /api/admin/users?page=0&size=1`; при успехе открывается панель администратора. Для EMPLOYEE отдельного интерфейса пока нет.

## Важно

- `WithdrawCreateDto` использует `accountID` с заглавным `ID` — сохранено в запросах.
- Для перевода нужен **ID счёта получателя**, поскольку backend принимает `destinationAccountId`.
- `UserProfileDto` не содержит `status`, поэтому статус пользователя не показан в таблице администратора.
- Для блокировки счетов ожидаются URL `/api/admin/accounts/{accountId}/block` и `/unblock`. Если backend использует старые `/api/admin/{accountId}/block`, обновите пути в `src/App.tsx` либо исправьте контроллер.
- Вход ADMIN проверяется через endpoint списка пользователей; после обновления страницы нужно войти снова.
- Приложение не проверялось против запущенного backend: API и обработчики следует проверить после подключения.
- Для `npm run build` требуется установка зависимостей (`npm install`).
