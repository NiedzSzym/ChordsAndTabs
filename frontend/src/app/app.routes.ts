import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login';
import { RegisterComponent } from './components/register/register';
import { SongListComponent } from './components/song-list/song-list';
import { SongTabsComponent } from './components/song-tabs/song-tabs';
import { AddSongComponent } from './components/add-song/add-song';
import { MainLayoutComponent } from './layouts/main-layout/main-layout';
import { AddSongChordsComponent } from './components/add-song-chords/add-song-chords';
import { AddArtistComponent } from './components/add-artist/add-artist';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  {
    path: '',
    component: MainLayoutComponent,
    children: [
      { path: 'songs', component: SongListComponent },
      { path: 'add-song', component: AddSongComponent },
      { path: 'songs/:songId/tabs', component: SongTabsComponent },
      { path: 'songs/:songId/add-chords', component: AddSongChordsComponent },
      { path: 'add-artist', component: AddArtistComponent },
      { path: 'add-song', component: AddSongComponent },
      { path: 'edit-song/:id', component: AddSongComponent },
      { path: 'songs/:songId/add-chords', component: AddSongChordsComponent },
      { path: 'songs/:songId/edit-chords/:chordsId', component: AddSongChordsComponent },
      { path: '', redirectTo: 'songs', pathMatch: 'full' }
    ]
  },
  { path: '**', redirectTo: '/login' }
];
