import { Component, OnInit, inject, signal } from '@angular/core';
import { SongService, SongDto } from '../../services/song';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-song-list',
  standalone: true,
  imports: [RouterLink, FormsModule],
  templateUrl: './song-list.html',
  styleUrl: './song-list.scss'
})
export class SongListComponent implements OnInit {
  songs = signal<SongDto[]>([]);
  errorMessage = signal('');
  isLoading = signal(true);

  // Stan paginacji
  currentPage = signal(0);
  totalPages = signal(0);
  private readonly pageSize = 12; // Zoptymalizowana wartość pod siatkę CSS Grid/Flexbox

  // Stan filtrów
  searchName = signal('');
  searchArtist = signal('');
  searchYear = signal<number | null>(null);
  onlyMySongs = signal(false);
  currentUser = signal<string>('');

  private readonly songService = inject(SongService);
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);


  ngOnInit(): void {
    this.currentUser.set(this.authService.getLoggedUser());
    this.fetchSongs();
  }

  fetchSongs(): void {
    this.isLoading.set(true);

    this.songService.getSongs(
      this.currentPage(),
      this.pageSize,
      this.searchName(),
      this.searchArtist(),
      this.searchYear() || undefined,
      this.onlyMySongs()
    ).subscribe({
      next: (response: any) => {
        this.songs.set(response.content || []);
        this.totalPages.set(response.totalPages || 0);
        this.isLoading.set(false);
      },
      error: (err: any) => {
        this.errorMessage.set('Failed to load the song library.');
        this.isLoading.set(false);
        console.error(err);
      }
    });
  }

  deleteSong(songId: number): void {
    const isConfirmed = window.confirm('Are you sure you want to delete this song? This action cannot be undone.');

    if (isConfirmed) {
      this.isLoading.set(true);
      this.songService.deleteSong(songId).subscribe({
        next: () => {
          this.fetchSongs();
        },
        error: (err: any) => {
          this.isLoading.set(false);
          if (err.status === 403 || err.status === 401) {
            this.errorMessage.set('You do not have permission to delete this song.');
          } else {
            this.errorMessage.set('An error occurred while trying to delete the song.');
          }
          console.error('Delete error details:', err);
        }
      });
    }
  }

  onSearch(): void {
    this.currentPage.set(0);
    this.fetchSongs();
  }

  clearFilters(): void {
    this.searchName.set('');
    this.searchArtist.set('');
    this.searchYear.set(null);
    this.onlyMySongs.set(false);
    this.currentPage.set(0);
    this.fetchSongs();
  }

  viewTabs(songId: number): void {
    this.router.navigate(['/songs', songId, 'tabs']);
  }

  nextPage(): void {
    if (this.currentPage() < this.totalPages() - 1) {
      this.currentPage.update(page => page + 1);
      this.fetchSongs();
    }
  }

  previousPage(): void {
    if (this.currentPage() > 0) {
      this.currentPage.update(page => page - 1);
      this.fetchSongs();
    }
  }

  firstPage(): void {
    if (this.currentPage() > 0) {
      this.currentPage.set(0);
      this.fetchSongs();
    }
  }

  lastPage(): void {
    if (this.currentPage() < this.totalPages() - 1) {
      this.currentPage.set(this.totalPages() - 1);
      this.fetchSongs();
    }
  }
}
