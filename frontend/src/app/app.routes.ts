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
    path: '',
    pathMatch: 'full',
    redirectTo: 'signup',
  },
];
