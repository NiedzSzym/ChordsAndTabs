import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs'; // <--- Ważny import z biblioteki RxJS
import { SongService, SongChordsCreateDto, InstrumentDto, KeyDto, TuningDto } from '../../services/song';

@Component({
  selector: 'app-add-song-chords',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './add-song-chords.html',
  styleUrl: './add-song-chords.scss'
})
export class AddSongChordsComponent implements OnInit {
  instruments = signal<InstrumentDto[]>([]);
  notations = signal<string[]>([]);
  tunings = signal<TuningDto[]>([]);
  keys = signal<KeyDto[]>([]);

  formData: SongChordsCreateDto = {
    keyId: null,
    tuningId: null,
    instrumentTypeId: null,
    notationType: '',
    status: 'PUBLIC',
    strummingPattern: '',
    timeSignature: '4/4',
    tempo: null,
    capoFret: null,
    songBody: '',
    chordIds: []
  };

  songId: number = 0;
  chordsId: number = 0;
  isEditMode = signal(false);

  isLoading = signal(false);
  errorMessage = signal('');

  private readonly songService = inject(SongService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  ngOnInit(): void {
    const songIdParam = this.route.snapshot.paramMap.get('songId');
    const chordsIdParam = this.route.snapshot.paramMap.get('chordsId');

    if (songIdParam) this.songId = +songIdParam;

    if (chordsIdParam) {
      this.isEditMode.set(true);
      this.chordsId = +chordsIdParam;
    }

    this.loadInitialData();
  }

  loadInitialData(): void {
    this.isLoading.set(true);

    // Pobieramy wszystkie główne słowniki równolegle
    forkJoin({
      instruments: this.songService.getInstruments(),
      notations: this.songService.getNotationTypes(),
      keys: this.songService.getKeys()
    }).subscribe({
      next: (data) => {
        this.instruments.set(data.instruments);
        this.notations.set(data.notations);
        this.keys.set(data.keys);

        if (this.isEditMode()) {
          this.loadChordsForEditing();
        } else {
          // Ustawianie domyślnych wartości dla trybu tworzenia nowej tabulatury
          if (data.instruments.length > 0) {
            this.formData.instrumentTypeId = data.instruments[0].instrumentTypeId;
            this.loadTunings(data.instruments[0].instrumentTypeId, true);
          }
          if (data.notations.length > 0) this.formData.notationType = data.notations[0];
          if (data.keys.length > 0) this.formData.keyId = data.keys[0].keyId;
          this.isLoading.set(false);
        }
      },
      error: (err) => {
        this.errorMessage.set('Failed to load dictionary data.');
        this.isLoading.set(false);
        console.error(err);
      }
    });
  }

  loadChordsForEditing(): void {
    this.songService.getSongChords(this.songId, this.chordsId).subscribe({
      next: (tab) => {
        // Zmapowanie otrzymanych nazw (String) na identyfikatory (ID) formularza
        const instrument = this.instruments().find(i => i.name === tab.instrumentTypeName);
        const key = this.keys().find(k => k.name === tab.keyName);

        this.formData = {
          keyId: key ? key.keyId : null,
          tuningId: null, // Ustawimy to dopiero, jak pobierzemy strojenia
          instrumentTypeId: instrument ? instrument.instrumentTypeId : null,
          notationType: tab.notationType,
          status: tab.status || 'PUBLIC',
          strummingPattern: tab.strummingPattern || '',
          timeSignature: tab.timeSignature || '4/4',
          tempo: tab.tempo,
          capoFret: tab.capoFret,
          songBody: tab.songBody,
          chordIds: tab.chords ? tab.chords.map(c => c.chordId) : []
        };

        if (this.formData.instrumentTypeId) {
          // Jeśli odnaleziono instrument, pobierzmy jego strojenia i ustawmy odpowiednie ID
          this.songService.getTunings(this.formData.instrumentTypeId).subscribe(tunings => {
            this.tunings.set(tunings);
            const tuning = tunings.find(t => t.tuning === tab.tuningName);
            this.formData.tuningId = tuning ? tuning.tuningId : null;
            this.isLoading.set(false);
          });
        } else {
          this.isLoading.set(false);
        }
      },
      error: (err) => {
        this.errorMessage.set('Failed to load tab details for editing.');
        this.isLoading.set(false);
        console.error(err);
      }
    });
  }

  loadTunings(instrumentId: number, setFirstAsDefault: boolean = false): void {
    this.songService.getTunings(instrumentId).subscribe(res => {
      this.tunings.set(res);
      if (setFirstAsDefault && res.length > 0) {
        this.formData.tuningId = res[0].tuningId;
      } else if (res.length === 0) {
        this.formData.tuningId = null;
      }
    });
  }

  onInstrumentChange(): void {
    if (this.formData.instrumentTypeId) {
      this.loadTunings(this.formData.instrumentTypeId, true);
    } else {
      this.tunings.set([]);
      this.formData.tuningId = null;
    }
  }

  onSubmit(): void {
    this.isLoading.set(true);
    this.errorMessage.set('');

    const observer = {
      next: () => {
        this.isLoading.set(false);
        this.router.navigate(['/songs', this.songId, 'tabs']);
      },
      error: (err: any) => {
        this.isLoading.set(false);
        this.errorMessage.set(`Failed to ${this.isEditMode() ? 'update' : 'save'} the chords.`);
        console.error('Submission error:', err);
      }
    };

    if (this.isEditMode()) {
      this.songService.updateSongChords(this.songId, this.chordsId, this.formData).subscribe(observer);
    } else {
      this.songService.createSongChords(this.songId, this.formData).subscribe(observer);
    }
  }
}
