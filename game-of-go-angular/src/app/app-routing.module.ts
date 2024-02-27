import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginPageComponent } from './components/login-page/login-page.component';
import { PlayMenuComponent } from './components/play-menu/play-menu.component';
import { RegisterPageComponent } from './components/register-page/register-page.component';
import { GameComponent } from './components/game/game.component';

const routes: Routes = [
  { path: 'play', component: PlayMenuComponent },
  { path: 'login', component: LoginPageComponent },
  { path: 'register', component: RegisterPageComponent },
  { path: 'game/:id', component: GameComponent, canDeactivate: [(component: GameComponent) => component.confirmLeaving()] },
  { path: '', component: PlayMenuComponent, pathMatch: 'full' },
  { path: '**', component: PlayMenuComponent, pathMatch: 'full' },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
