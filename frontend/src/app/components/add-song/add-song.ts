import { Component, OnInit, inject, signal } from '@angular/core';
import { Router, ActivatedRoute, RouterLink } from '@angular/router'; // <-- Dodano ActivatedRoute
import { FormsModule } from '@angular/forms';
import { SongService, SongCreateDto, ArtistDto } from '../../services/song';

@Component({
  selector: 'app-add-song',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './add-song.html',
  styleUrl: './add-song.scss'
})
export class AddSongComponent implements OnInit {
  artists = signal<ArtistDto[]>([]);

  formData: SongCreateDto = {
    name: '',
    releaseYear: null,
    artistIds: []
  };

  isLoading = signal(false);
  errorMessage = signal('');

  // Zmienne obsługujące tryb edycji
  isEditMode = signal(false);
  songId = 0;

  private readonly songService = inject(SongService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute); // <-- Wstrzyknięto do odczytu URL

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.isEditMode.set(true);
      this.songId = +idParam;
    }

    this.isLoading.set(true);

    // Najpierw pobieramy pełną listę artystów
    this.songService.getArtists().subscribe({
      next: (artistsResponse) => {
        this.artists.set(artistsResponse);

        // Jeśli jesteśmy w trybie edycji, pobieramy dane utworu i mapujemy
        if (this.isEditMode()) {
          this.loadSongForEditing();
        } else {
          this.isLoading.set(false);
        }
      },
      error: (err) => {
        this.isLoading.set(false);
        this.errorMessage.set('Failed to load required data.');
        console.error(err);
      }
    });
  }

  loadSongForEditing(): void {
    this.songService.getSong(this.songId).subscribe({
      next: (song) => {
        // Mapowanie nazw artystów (z backendu) na numery ID (dla selektora formularza)
        const matchedArtistIds = this.artists()
          .filter(artist => song.artistNames.includes(artist.name))
          .map(artist => artist.artistId);

        this.formData = {
          name: song.name,
          releaseYear: song.year, // Backend zwraca pole 'year' w SongDto
          artistIds: matchedArtistIds
        };
        this.isLoading.set(false);
      },
      error: (err) => {
        this.isLoading.set(false);
        this.errorMessage.set('Failed to load song details for editing.');
        console.error(err);
      }
    });
  }

  onSubmit(): void {
    this.isLoading.set(true);
    this.errorMessage.set('');

    const observer = {
      next: () => {
        this.isLoading.set(false);
        this.router.navigate(['/songs']);
      },
      error: (err: any) => {
        this.isLoading.set(false);
        this.errorMessage.set(`Failed to ${this.isEditMode() ? 'update' : 'save'} the song.`);
        console.error('Submission error:', err);
      }
    };

    if (this.isEditMode()) {
      this.songService.updateSong(this.songId, this.formData).subscribe(observer);
    } else {
      this.songService.createSong(this.formData).subscribe(observer);
    }
  }
}
