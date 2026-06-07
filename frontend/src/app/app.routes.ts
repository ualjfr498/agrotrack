import { Routes } from '@angular/router';
import { adminGuard } from './guards/admin.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./components/home/home').then(m => m.Home)
  },
  {
    path: 'login',
    loadComponent: () => import('./auth/login/login').then(m => m.Login)
  },
  {
    path: 'register',
    loadComponent: () => import('./auth/register/register').then(m => m.Register)
  },
  {
    path: 'analizador',
    loadComponent: () => import('./components/catalogo/catalogo').then(m => m.Catalogo)
  },
  {
    path: 'analizador/:id',
    loadComponent: () => import('./components/catalogo-detalle/catalogo-detalle').then(m => m.CatalogoDetalle)
  },
  // Compatibilidad con rutas antiguas: catálogo y precios se unificaron en el analizador.
  { path: 'catalogo', redirectTo: 'analizador', pathMatch: 'full' },
  { path: 'catalogo/:id', redirectTo: 'analizador/:id' },
  { path: 'precios', redirectTo: 'analizador', pathMatch: 'full' },
  {
    path: 'admin',
    canActivate: [adminGuard],
    loadComponent: () => import('./components/admin/admin').then(m => m.Admin)
  },
  {
    path: '**',
    redirectTo: ''
  }
];
