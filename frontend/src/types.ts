export type Role = 'CLIENT' | 'ADMIN' | 'EMPLOYEE';
export type User = { id: number; firstName: string; lastName: string; phoneNumber: string; role: Role; status?: string };
export type Account = { id: number; accountNumber: string; balance: number; currency: string; accountStatus: string };
export type Transaction = { transactionId: number; type: string; amount: number; direction: string; status: string; createdAt: string };
export type Page<T> = { content: T[]; totalPages: number; totalElements: number; number: number; last: boolean };
export type Auth = { phone: string; password: string; role: 'CLIENT' | 'ADMIN' };
