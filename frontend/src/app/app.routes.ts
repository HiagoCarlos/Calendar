import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'signup',
    loadComponent: () => import('./pages/signup/signup').then((m) => m.Signup),
    title: 'Criar conta',
  },
  {
    path: 'cadastro',
    redirectTo: 'signup',
    pathMatch: 'full',
  },
  {
    path: 'login',
    loadComponent: () => import('./pages/login/login').then((m) => m.Login),
    title: 'Entrar',
  },
  {
    path: 'resetar-senha',
    loadComponent: () =>
      import('./pages/reset-password/reset-password').then((m) => m.ResetPassword),
    title: 'Resetar senha',
  },
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'signup',
  },
];
