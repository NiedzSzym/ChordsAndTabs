import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { SongService, ArtistCreateDto } from '../../services/song';

@Component({
  selector: 'app-add-artist',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './add-artist.html',
  styleUrl: './add-artist.scss'
})
export class AddArtistComponent {
  formData: ArtistCreateDto = {
    name: ''
  };

  isLoading = signal(false);
  errorMessage = signal('');

  private readonly songService = inject(SongService);
  private readonly router = inject(Router);

  onSubmit(): void {
    this.isLoading.set(true);
    this.errorMessage.set('');

    this.songService.createArtist(this.formData).subscribe({
      next: () => {
        this.isLoading.set(false);
        // Po pomyślnym utworzeniu artysty wracamy do formularza dodawania piosenki
        this.router.navigate(['/add-song']);
      },
      error: (err: any) => {
        this.isLoading.set(false);
        this.errorMessage.set('Failed to save the artist. Please try again.');
        console.error('Artist creation error:', err);
      }
    });
  }
}
