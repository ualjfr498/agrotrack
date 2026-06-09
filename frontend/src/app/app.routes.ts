import { Routes } from '@angular/router';
import { adminGuard } from './guards/admin.guard';
import { authGuard } from './guards/auth.guard';

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
    path: 'mis-parcelas',
    canActivate: [authGuard],
    loadComponent: () => import('./components/mi-parcela/mi-parcela').then(m => m.MiParcela)
  },
  {
    path: 'perfil',
    canActivate: [authGuard],
    loadComponent: () => import('./components/perfil/perfil').then(m => m.Perfil)
  },
  {
    // Público: un invitado puede usar el asistente para precios/catálogo. Las
    // funciones de parcelas/cultivos solo responden si está autenticado.
    path: 'asistente',
    loadComponent: () => import('./components/asistente/asistente').then(m => m.Asistente)
  },
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
