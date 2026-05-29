import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { SongService, SongChordsListDto, SongChordsDto } from '../../services/song';

@Component({
  selector: 'app-song-tabs',
  standalone: true,
  imports: [
    RouterLink
  ],
  templateUrl: './song-tabs.html',
  styleUrl: './song-tabs.scss'
})
export class SongTabsComponent implements OnInit {
  versions = signal<SongChordsListDto[]>([]);
  activeTab = signal<SongChordsDto | null>(null);
  isLoading = signal(true);
  errorMessage = signal('');

  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly songService = inject(SongService);
  songId = 0;

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('songId');
    if (idParam) {
      this.songId = +idParam;
      this.fetchVersions();
    }
  }

  fetchVersions(): void {
    this.songService.getSongChordsList(this.songId).subscribe({
      next: (list) => {
        this.versions.set(list);
        if (list.length > 0) {
          this.loadTabDetail(list[0].songChordsId);
        } else {
          this.isLoading.set(false);
        }
      },
      error: () => {
        this.isLoading.set(false);
        this.errorMessage.set('Failed to load tabs list.');
      }
    });
  }

  loadTabDetail(chordsId: number): void {
    this.isLoading.set(true);
    this.songService.getSongChordsDetail(this.songId, chordsId).subscribe({
      next: (detail) => {
        this.activeTab.set(detail);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.errorMessage.set('Failed to load tab details.');
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/songs']);
  }
}
