import {
  useCallback,
  useEffect,
  useState,
  type FormEvent
} from 'react';

import {
  ArrowDownLeft,
  ArrowLeftRight,
  ArrowUpRight,
  Building2,
  ChevronLeft,
  ChevronRight,
  CircleUserRound,
  CreditCard,
  History,
  Landmark,
  LogOut,
  Plus,
  Shield,
  Wallet
} from 'lucide-react';

import { api, patch, post, loginRequest } from './api';
import type { Account, Auth, Page, Transaction, User } from './types';

type View = 'dashboard' | 'history' | 'admin';
type Operation = 'deposit' | 'withdraw' | 'transfer';

const money = (value: number | string, currency = 'KZT') =>
  `${Number(value).toLocaleString('ru-RU', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })} ${currency === 'KZT' ? '₸' : currency}`;

const msg = (e: unknown) =>
  e instanceof Error ? e.message : 'Неизвестная ошибка';

const dt = (s: string) => new Date(s).toLocaleString('ru-RU');

export default function App() {
  const [auth, setAuth] = useState<Auth | null>(null);
  const [checkingSession, setCheckingSession] = useState(true);
  const [profile, setProfile] = useState<User | null>(null);
  const [loginMode, setLoginMode] = useState<'login' | 'register'>('login');
  const [view, setView] = useState<View>('dashboard');
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [selected, setSelected] = useState<number | null>(null);
  const [history, setHistory] = useState<Page<Transaction> | null>(null);
  const [historyPage, setHistoryPage] = useState(0);
  const [operation, setOperation] = useState<Operation | null>(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');

  const refreshAccounts = useCallback(async (a: Auth) => {
    const result = await api<Account[]>('/account/my', a);
    setAccounts(result);
    setSelected(prev =>
      prev !== null && result.some(x => x.id === prev)
        ? prev
        : (result[0]?.id ?? null)
    );
  }, []);

  const applyUser = useCallback(async (user: User) => {
    const role = user.role as Auth['role'];

    const session: Auth = {
      phone: user.phoneNumber,
      password: '',
      role
    };

    if (role === 'CLIENT') {
      const result = await api<Account[]>('/account/my');
      setAccounts(result);
      setSelected(result[0]?.id ?? null);
      setView('dashboard');
    } else if (role === 'ADMIN') {
      setAccounts([]);
      setView('admin');
    } else {
      throw new Error('Роль пользователя не поддерживается интерфейсом');
    }

    setProfile(user);
    setAuth(session);
  }, []);

  const login = async (phone: string, password: string) => {
    await loginRequest(phone, password);
    const user = await api<User>('/auth/me');
    await applyUser(user);
  };

  const logout = async () => {
    try {
      await post<void>('/auth/logout', undefined);
    } catch (e) {
      setError(msg(e));
      return;
    }

    setAuth(null);
    setProfile(null);
    setAccounts([]);
    setHistory(null);
    setSelected(null);
    setOperation(null);
    setError('');
    setNotice('');
    setView('dashboard');
  };

  useEffect(() => {
    let active = true;

    const restoreSession = async () => {
      try {
        const user = await api<User>('/auth/me');
        if (active) await applyUser(user);
      } catch {
        // Нет действующей сессии.
      } finally {
        if (active) setCheckingSession(false);
      }
    };

    void restoreSession();

    return () => {
      active = false;
    };
  }, [applyUser]);

  useEffect(() => {
    if (
      !auth ||
      auth.role !== 'CLIENT' ||
      selected === null ||
      view !== 'history'
    ) return;

    let active = true;

    api<Page<Transaction>>(
      `/transactions/${selected}/history?page=${historyPage}&size=10&sort=createdAt,desc`,
      auth
    )
      .then(p => {
        if (active) setHistory(p);
      })
      .catch(e => {
        if (active) setError(msg(e));
      });

    return () => {
      active = false;
    };
  }, [auth, selected, historyPage, view]);

  const run = async (
    action: () => Promise<void>,
    success?: string
  ) => {
    setBusy(true);
    setError('');
    setNotice('');

    try {
      await action();
      if (success) setNotice(success);
    } catch (e) {
      setError(msg(e));
    } finally {
      setBusy(false);
    }
  };

  if (checkingSession) {
    return (
      <div className="auth-page">
        <div className="auth-form-area">
          <h2>Проверяем сессию...</h2>
        </div>
      </div>
    );
  }

  if (!auth) {
    return (
      <AuthScreen
        mode={loginMode}
        setMode={setLoginMode}
        onLogin={(phone, pass) => run(() => login(phone, pass))}
        busy={busy}
        error={error}
        onRegister={data =>
          run(async () => {
            await post('/auth/register', undefined, data);
            setLoginMode('login');
          }, 'Регистрация выполнена. Теперь войдите.')
        }
        notice={notice}
      />
    );
  }

  const admin = auth.role === 'ADMIN';

  return (
    <div className="shell">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-icon"><Landmark size={24} /></div>
          <div>
            <strong>Bank Simulator</strong>
            <small>DIGITAL BANKING</small>
          </div>
        </div>

        <div className="side-label">МЕНЮ</div>

        {!admin && (
          <>
            <button
              className={'nav ' + (view === 'dashboard' ? 'active' : '')}
              onClick={() => setView('dashboard')}
            >
              <Wallet size={19} /> Главная
            </button>
            <button
              className={'nav ' + (view === 'history' ? 'active' : '')}
              onClick={() => {
                setHistoryPage(0);
                setView('history');
              }}
            >
              <History size={19} /> История операций
            </button>
          </>
        )}

        {admin && (
          <button
            className={'nav ' + (view === 'admin' ? 'active' : '')}
            onClick={() => setView('admin')}
          >
            <Shield size={19} /> Администрирование
          </button>
        )}

        <div className="side-bottom">
          <div className="side-user">
            <CircleUserRound size={26} />
            <div>
              <strong>{profile?.firstName} {profile?.lastName}</strong>
              <small>{auth.phone}</small>
            </div>
          </div>
          <button className="nav" onClick={() => void logout()}>
            <LogOut size={19} /> Выйти
          </button>
        </div>
      </aside>

      <main className="main">
        <header className="topbar">
          <div>
            <span className="eyebrow">
              BANK SIMULATOR / {admin ? 'ADMIN' : 'CLIENT'}
            </span>
            <h1>
              {admin
                ? 'Панель администратора'
                : view === 'history'
                  ? 'История операций'
                  : 'Мой банк'}
            </h1>
          </div>
          <div className="avatar">
            {(profile?.firstName || 'A').slice(0, 1).toUpperCase()}
          </div>
        </header>

        {error && (
          <div className="alert error" role="alert">
            {error}
            <button onClick={() => setError('')}>×</button>
          </div>
        )}

        {notice && (
          <div className="alert success" role="status">
            {notice}
            <button onClick={() => setNotice('')}>×</button>
          </div>
        )}

        {admin ? (
          <AdminPanel auth={auth} run={run} busy={busy} />
        ) : view === 'history' ? (
          <section className="panel">
            <div className="section-head">
              <h2>История операций</h2>
              <select
                value={selected ?? ''}
                onChange={e => {
                  setSelected(Number(e.target.value));
                  setHistoryPage(0);
                  setHistory(null);
                }}
              >
                {accounts.map(a => (
                  <option key={a.id} value={a.id}>
                    {a.accountNumber}
                  </option>
                ))}
              </select>
            </div>

            {selected === null ? (
              <p className="muted">Сначала создайте счёт.</p>
            ) : (
              <>
                <TransactionTable items={history?.content ?? []} />
                <div className="pagination">
                  <span>
                    Всего операций: {history?.totalElements ?? '…'}
                  </span>
                  <div>
                    <button
                      disabled={historyPage === 0}
                      onClick={() => setHistoryPage(p => p - 1)}
                    >
                      <ChevronLeft size={17} />
                    </button>
                    <span>
                      {historyPage + 1} / {Math.max(history?.totalPages ?? 1, 1)}
                    </span>
                    <button
                      disabled={!history || history.last}
                      onClick={() => setHistoryPage(p => p + 1)}
                    >
                      <ChevronRight size={17} />
                    </button>
                  </div>
                </div>
              </>
            )}
          </section>
        ) : (
          <>
            <section className="hero">
              <div>
                <span className="hero-label">
                  Добро пожаловать, {profile?.firstName}!
                </span>
                <p>Общий баланс ваших счетов</p>
                <div className="hero-balance">
                  {money(
                    accounts
                      .filter(a => a.currency === 'KZT')
                      .reduce((sum, a) => sum + Number(a.balance), 0)
                  )}
                </div>
                <small>Сумма по счетам в KZT</small>
              </div>
              <div className="hero-art">
                <Building2 size={110} strokeWidth={1} />
              </div>
            </section>

            <div className="actions">
              <button onClick={() => setOperation('deposit')}>
                <span><ArrowDownLeft size={24} /></span>
                Пополнить
              </button>
              <button onClick={() => setOperation('withdraw')}>
                <span><ArrowUpRight size={24} /></span>
                Снять
              </button>
              <button onClick={() => setOperation('transfer')}>
                <span><ArrowLeftRight size={24} /></span>
                Перевести
              </button>
            </div>

            <section className="panel">
              <div className="section-head">
                <div>
                  <h2>Мои счета</h2>
                  <p className="muted">
                    Управление банковскими счетами
                  </p>
                </div>
                <button
                  className="btn primary"
                  disabled={busy}
                  onClick={() =>
                    run(async () => {
                      await post('/account/create', auth);
                      await refreshAccounts(auth);
                    }, 'Счёт создан')
                  }
                >
                  <Plus size={17} /> Новый счёт
                </button>
              </div>

              <div className="account-grid">
                {accounts.map(a => (
                  <div key={a.id} className="account-card">
                    <div className="account-top">
                      <CreditCard size={25} />
                      <span
                        className={
                          'pill ' +
                          (a.accountStatus === 'ACTIVE' ? 'ok' : 'warn')
                        }
                      >
                        {a.accountStatus}
                      </span>
                    </div>
                    <span className="muted">{a.accountNumber}</span>
                    <div className="account-balance">
                      {money(a.balance, a.currency)}
                    </div>
                    <div className="account-bottom">
                      <span>{a.currency}</span>
                      {a.accountStatus === 'ACTIVE' && (
                        <button
                          disabled={busy || Number(a.balance) !== 0}
                          title={
                            Number(a.balance) !== 0
                              ? 'Сначала обнулите баланс'
                              : ''
                          }
                          onClick={() => {
                            if (window.confirm('Закрыть счёт?')) {
                              void run(async () => {
                                await patch(`/account/${a.id}/close`, auth);
                                await refreshAccounts(auth);
                              }, 'Счёт закрыт');
                            }
                          }}
                        >
                          Закрыть счёт
                        </button>
                      )}
                    </div>
                  </div>
                ))}

                {accounts.length === 0 && (
                  <p className="muted">
                    Счетов пока нет. Создайте первый счёт.
                  </p>
                )}
              </div>
            </section>
          </>
        )}
      </main>

      {operation && (
        <OperationModal
          type={operation}
          accounts={accounts}
          busy={busy}
          close={() => setOperation(null)}
          submit={async payload => {
            setBusy(true);
            setError('');
            setNotice('');

            try {
              if (operation === 'deposit') {
                await post('/transactions/deposit', auth, payload);
              } else if (operation === 'withdraw') {
                await post('/transactions/withdraw', auth, payload);
              } else {
                await post('/transactions/transfer', auth, payload);
              }

              await refreshAccounts(auth);
              setOperation(null);
              setNotice('Операция выполнена');
            } finally {
              setBusy(false);
            }
          }}
        />
      )}
    </div>
  );
}

function AuthScreen({
  mode, setMode, onLogin, onRegister, busy, error, notice
}: {
  mode: 'login' | 'register';
  setMode: (m: 'login' | 'register') => void;
  onLogin: (p: string, pw: string) => void;
  onRegister: (data: unknown) => void;
  busy: boolean;
  error: string;
  notice: string;
}) {
  const [phone, setPhone] = useState('');
  const [password, setPassword] = useState('');
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [birthDate, setBirthDate] = useState('');

  const submit = (e: FormEvent) => {
    e.preventDefault();
    if (mode === 'login') {
      onLogin(phone, password);
    } else {
      onRegister({
        phoneNumber: phone,
        password,
        firstName,
        lastName,
        birthDate
      });
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-intro">
        <div className="brand">
          <div className="brand-icon"><Landmark size={25} /></div>
          <div>
            <strong>Bank Simulator</strong>
            <small>DIGITAL BANKING</small>
          </div>
        </div>
        <div className="auth-slogan">
          <span className="eyebrow">YOUR MONEY. YOUR CONTROL.</span>
          <h1>
            Банкинг,<br />
            <em>который всегда</em><br />
            с тобой.
          </h1>
          <p>
            Управляйте счетами, переводите деньги и отслеживайте
            каждую операцию в одном месте.
          </p>
        </div>
        <div className="auth-footer">
          Учебный банковский симулятор · Spring Boot + React
        </div>
      </div>

      <div className="auth-form-area">
        <form className="auth-form" onSubmit={submit}>
          <div className="form-icon"><Landmark size={26} /></div>
          <h2>
            {mode === 'login' ? 'С возвращением!' : 'Создать аккаунт'}
          </h2>
          <p className="muted">
            {mode === 'login'
              ? 'Войдите в личный кабинет'
              : 'Зарегистрируйтесь в Bank Simulator'}
          </p>

          {error && <div className="alert error">{error}</div>}
          {notice && <div className="alert success">{notice}</div>}

          {mode === 'register' && (
            <div className="form-row">
              <label>
                Имя
                <input
                  required
                  value={firstName}
                  onChange={e => setFirstName(e.target.value)}
                  placeholder="Имя"
                />
              </label>
              <label>
                Фамилия
                <input
                  required
                  value={lastName}
                  onChange={e => setLastName(e.target.value)}
                  placeholder="Фамилия"
                />
              </label>
            </div>
          )}

          <label>
            Номер телефона
            <input
              required
              value={phone}
              onChange={e => setPhone(e.target.value)}
              placeholder="Номер телефона"
              autoComplete="username"
            />
          </label>

          {mode === 'register' && (
            <label>
              Дата рождения
              <input
                type="date"
                required
                value={birthDate}
                onChange={e => setBirthDate(e.target.value)}
              />
            </label>
          )}

          <label>
            Пароль
            <input
              type="password"
              required
              minLength={mode === 'register' ? 8 : undefined}
              value={password}
              onChange={e => setPassword(e.target.value)}
              placeholder="Введите пароль"
              autoComplete={
                mode === 'register'
                  ? 'new-password'
                  : 'current-password'
              }
            />
          </label>

          <button className="btn primary full" disabled={busy}>
            {busy
              ? 'Подождите…'
              : mode === 'login'
                ? 'Войти'
                : 'Зарегистрироваться'}
          </button>

          <p className="switch">
            {mode === 'login' ? 'Нет аккаунта?' : 'Уже есть аккаунт?'}{' '}
            <button
              type="button"
              onClick={() =>
                setMode(mode === 'login' ? 'register' : 'login')
              }
            >
              {mode === 'login' ? 'Регистрация' : 'Войти'}
            </button>
          </p>
        </form>
      </div>
    </div>
  );
}

function OperationModal({
  type, accounts, busy, close, submit
}: {
  type: Operation;
  accounts: Account[];
  busy: boolean;
  close: () => void;
  submit: (data: unknown) => Promise<void>;
}) {
  const active = accounts.filter(a => a.accountStatus === 'ACTIVE');
  const [source, setSource] = useState(String(active[0]?.id ?? ''));
  const [dest, setDest] = useState('');
  const [amount, setAmount] = useState('');
  const [modalError, setModalError] = useState('');

  const selectedAccount = active.find(a => a.id === Number(source));
  const balance = Number(selectedAccount?.balance ?? 0);
  const value = Number(amount);

  const validAmount =
    amount.trim() !== '' &&
    Number.isFinite(value) &&
    value > 0;

  const insufficientFunds =
    (type === 'transfer' || type === 'withdraw') &&
    validAmount &&
    value > balance;

  const invalidDestination = type === 'transfer' && !dest.trim();

  const sameAccount =
    type === 'transfer' &&
    !!selectedAccount &&
    dest.trim() === selectedAccount.accountNumber;

  const invalidForm =
    !selectedAccount ||
    !validAmount ||
    insufficientFunds ||
    invalidDestination ||
    sameAccount;

  const title = {
    deposit: 'Пополнение счёта',
    withdraw: 'Снятие средств',
    transfer: 'Перевод денег'
  }[type];

  const send = async (e: FormEvent) => {
    e.preventDefault();
    if (busy || invalidForm) return;

    setModalError('');

    const data = type === 'deposit'
      ? { accountId: Number(source), depositAmount: value }
      : type === 'withdraw'
        ? { accountId: Number(source), withdrawAmount: value }
        : {
            sourceAccountId: Number(source),
            destinationAccountNumber: dest.trim(),
            transferAmount: value
          };

    try {
      await submit(data);
    } catch (error) {
      setModalError(
        error instanceof Error
          ? error.message
          : 'Не удалось выполнить операцию'
      );
    }
  };

  return (
    <div
      className="overlay"
      onMouseDown={e => {
        if (e.target === e.currentTarget && !busy) close();
      }}
    >
      <form className="modal" onSubmit={send}>
        <div className="section-head">
          <h2>{title}</h2>
          <button
            type="button"
            className="close"
            disabled={busy}
            onClick={close}
          >
            ×
          </button>
        </div>

        <label>
          {type === 'transfer' ? 'Счёт отправителя' : 'Выберите счёт'}
          <select
            required
            value={source}
            disabled={busy}
            onChange={e => {
              setSource(e.target.value);
              setModalError('');
            }}
          >
            {active.map(a => (
              <option key={a.id} value={a.id}>
                {a.accountNumber} — {money(a.balance, a.currency)}
              </option>
            ))}
          </select>
        </label>

        {(type === 'transfer' || type === 'withdraw') && (
          <p className="muted">
            Доступно: {money(balance, selectedAccount?.currency)}
          </p>
        )}

        {type === 'transfer' && (
          <label>
            Номер счёта получателя
            <input
              required
              type="text"
              value={dest}
              disabled={busy}
              onChange={e => {
                setDest(e.target.value);
                setModalError('');
              }}
              placeholder="Введите номер счёта"
            />
          </label>
        )}

        <label>
          Сумма (KZT)
          <input
            required
            type="number"
            min="0.01"
            step="0.01"
            value={amount}
            disabled={busy}
            onChange={e => {
              setAmount(e.target.value);
              setModalError('');
            }}
            placeholder="0.00"
          />
        </label>

        {insufficientFunds && (
          <div className="alert error" role="alert">
            Недостаточно средств на счёте.
            Доступно: {money(balance, selectedAccount?.currency)}
          </div>
        )}

        {sameAccount && (
          <div className="alert error" role="alert">
            Нельзя переводить деньги на тот же счёт.
          </div>
        )}

        {modalError && !insufficientFunds && !sameAccount && (
          <div className="alert error" role="alert">
            {modalError}
          </div>
        )}

        <button
          type="submit"
          className="btn primary full"
          disabled={busy || invalidForm}
        >
          {busy ? 'Выполняется…' : 'Подтвердить'}
        </button>
      </form>
    </div>
  );
}

function TransactionTable({ items }: { items: Transaction[] }) {
  const names: Record<string, string> = {
    DEPOSIT: 'Пополнение',
    WITHDRAWAL: 'Снятие',
    TRANSFER: 'Перевод'
  };

  return (
    <div className="table-wrap">
      <table>
        <thead>
          <tr>
            <th>Операция</th>
            <th>Дата</th>
            <th>Направление</th>
            <th>Статус</th>
            <th className="right">Сумма</th>
          </tr>
        </thead>
        <tbody>
          {items.map(t => (
            <tr key={t.transactionId}>
              <td>
                <strong>{names[t.type] ?? t.type}</strong>
                <small>#{t.transactionId}</small>
              </td>
              <td>{dt(t.createdAt)}</td>
              <td>
                {t.direction === 'INCOMING' ? 'Входящий' : 'Исходящий'}
              </td>
              <td><span className="pill ok">{t.status}</span></td>
              <td
                className={
                  'right ' +
                  (t.direction === 'INCOMING' ? 'positive' : '')
                }
              >
                {t.direction === 'INCOMING' ? '+' : '−'}
                {money(t.amount)}
              </td>
            </tr>
          ))}
          {items.length === 0 && (
            <tr>
              <td colSpan={5} className="empty">
                Операций пока нет
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}

function AdminPanel({
  auth, run, busy
}: {
  auth: Auth;
  run: (action: () => Promise<void>, success?: string) => Promise<void>;
  busy: boolean;
}) {
  const [users, setUsers] = useState<Page<User> | null>(null);
  const [page, setPage] = useState(0);
  const [userAccounts, setUserAccounts] = useState<Account[] | null>(null);
  const [chosen, setChosen] = useState<User | null>(null);

  const load = useCallback(async () => {
    setUsers(
      await api<Page<User>>(`/admin/users?page=${page}&size=10`, auth)
    );
  }, [auth, page]);

  useEffect(() => {
    void load().catch(() => {});
  }, [load]);

  const selectUser = (u: User) =>
    run(async () => {
      setChosen(u);
      setUserAccounts(
        await api<Account[]>(`/admin/users/${u.id}/accounts`, auth)
      );
    });

  const changeUser = (u: User, op: 'block' | 'unblock') => {
    if (!window.confirm(
      `${op === 'block' ? 'Заблокировать' : 'Разблокировать'} пользователя ${u.firstName}?`
    )) return;

    void run(async () => {
      await patch(`/admin/users/${u.id}/${op}`, auth);
      await load();
    }, 'Статус пользователя обновлён');
  };

  const changeAccount = (a: Account, op: 'block' | 'unblock') => {
    void run(async () => {
      await patch(`/admin/accounts/${a.id}/${op}`, auth);
      if (chosen) {
        setUserAccounts(
          await api<Account[]>(`/admin/users/${chosen.id}/accounts`, auth)
        );
      }
    }, 'Статус счёта обновлён');
  };

  return (
    <>
      <section className="panel">
        <div className="section-head">
          <div>
            <h2>Пользователи</h2>
            <p className="muted">Просмотр и управление клиентами</p>
          </div>
          <span className="pill">
            Всего: {users?.totalElements ?? '…'}
          </span>
        </div>

        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Пользователь</th>
                <th>Телефон</th>
                <th>Роль</th>
                <th>Действия</th>
              </tr>
            </thead>
            <tbody>
              {users?.content.map(u => (
                <tr key={u.id}>
                  <td>
                    <strong>{u.firstName} {u.lastName}</strong>
                    <small>ID: {u.id}</small>
                  </td>
                  <td>{u.phoneNumber}</td>
                  <td><span className="pill">{u.role}</span></td>
                  <td>
                    <div className="table-actions">
                      <button
                        onClick={() => void selectUser(u)}
                        disabled={busy}
                      >
                        Счета
                      </button>
                      {u.role === 'CLIENT' && (
                        <>
                          <button
                            disabled={busy}
                            onClick={() => changeUser(u, 'block')}
                          >
                            Блокировать
                          </button>
                          <button
                            disabled={busy}
                            onClick={() => changeUser(u, 'unblock')}
                          >
                            Разблокировать
                          </button>
                        </>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="pagination">
          <span>Страница {page + 1}</span>
          <div>
            <button
              disabled={page === 0}
              onClick={() => setPage(p => p - 1)}
            >
              <ChevronLeft size={17} />
            </button>
            <button
              disabled={!users || users.last}
              onClick={() => setPage(p => p + 1)}
            >
              <ChevronRight size={17} />
            </button>
          </div>
        </div>
      </section>

      {chosen && (
        <section className="panel">
          <div className="section-head">
            <h2>
              Счета: {chosen.firstName} {chosen.lastName}
            </h2>
            <button
              onClick={() => {
                setChosen(null);
                setUserAccounts(null);
              }}
            >
              Закрыть ×
            </button>
          </div>

          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Номер счёта</th>
                  <th>Баланс</th>
                  <th>Статус</th>
                  <th>Действия</th>
                </tr>
              </thead>
              <tbody>
                {userAccounts?.map(a => (
                  <tr key={a.id}>
                    <td>{a.accountNumber}</td>
                    <td>{money(a.balance, a.currency)}</td>
                    <td><span className="pill">{a.accountStatus}</span></td>
                    <td>
                      <div className="table-actions">
                        {a.accountStatus === 'ACTIVE' && (
                          <button
                            disabled={busy}
                            onClick={() => changeAccount(a, 'block')}
                          >
                            Блокировать
                          </button>
                        )}
                        {a.accountStatus === 'BLOCKED' && (
                          <button
                            disabled={busy}
                            onClick={() => changeAccount(a, 'unblock')}
                          >
                            Разблокировать
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      )}
    </>
  );
}
