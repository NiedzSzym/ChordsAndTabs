import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface SongDto {
  id: number;
  name: string;
  year: number;
  artistNames: string[];
  createdBy?: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface SongChordsListDto {
  songChordsId: number;
  songName: string;
  keyName: string;
  tuningName: string;
  instrumentTypeName: string;
  notationType: string;
  authorNickname: string;
}

export interface SongChordsDto extends SongChordsListDto {
  status: string;
  strummingPattern: string;
  timeSignature: string;
  tempo: number;
  capoFret: number;
  songBody: string;
  chords: any[];
}

export interface SongCreateDto {
  name: string;
  releaseYear: number | null;
  artistIds: number[];
}

export interface SongChordsCreateDto {
  keyId: number | null;
  tuningId: number | null;
  instrumentTypeId: number | null;
  notationType: string;
  status: string;
  strummingPattern: string;
  timeSignature: string;
  tempo: number | null;
  capoFret: number | null;
  songBody: string;
  chordIds?: number[];
}

export interface ChordSelectDto {
  chordId: number;
  name: string;
  chordFingering: string;
}

export interface SongChordsDetailsDto {
  songChordsId: number;
  songName: string;
  keyName: string;
  tuningName: string;
  instrumentTypeName: string;
  status: string;
  notationType: string;
  authorNickname: string;
  createdAt: string;
  updatedAt: string;
  strummingPattern: string;
  timeSignature: string;
  tempo: number | null;
  capoFret: number | null;
  songBody: string;
  chords: ChordSelectDto[];
}

export interface InstrumentDto {
  instrumentTypeId: number;
  name: string;
}

export interface KeyDto {
  keyId: number;
  name: string;
  mode: string;
}

export interface TuningDto {
  tuningId: number;
  tuning: string;
}

export interface ArtistDto {
  artistId: number;
  name: string;
}

export interface ArtistCreateDto {
  name: string;
}

export interface SongDto {
  id: number;
  name: string;
  releaseYear: number | null;
  artistNames: string[];
  createdBy?: string;
}


@Injectable({
  providedIn: 'root'
})



export class SongService {
  private readonly apiUrl = 'http://localhost:8080/api/songs';
  private readonly http = inject(HttpClient);

  getSongs(
    page: number = 0,
    size: number = 12,
    name?: string,
    artist?: string,
    year?: number,
    mySongs?: boolean
  ): Observable<Page<SongDto>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (name) {
      params = params.set('name', name);
    }
    if (artist) {
      params = params.set('artist', artist);
    }
    if (year) {
      params = params.set('year', year.toString());
    }
    if (mySongs) {
      params = params.set('mySongs', 'true');
    }

    return this.http.get<Page<SongDto>>(this.apiUrl, { params });
  }

  createSongChords(songId: number, chordsData: SongChordsCreateDto): Observable<any> {
    return this.http.post(`${this.apiUrl}/${songId}/chords`, chordsData);
  }

  createSong(song: SongCreateDto): Observable<SongDto> {
    return this.http.post<SongDto>(this.apiUrl, song);
  }

  getSongChordsList(songId: number): Observable<SongChordsListDto[]> {
    return this.http.get<SongChordsListDto[]>(`${this.apiUrl}/${songId}/chords`);
  }

  getSongChordsDetail(songId: number, chordsId: number): Observable<SongChordsDto> {
    return this.http.get<SongChordsDto>(`${this.apiUrl}/${songId}/chords/${chordsId}`);
  }

  deleteSong(songId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${songId}`);
  }

  getInstruments(): Observable<InstrumentDto[]> {
    return this.http.get<InstrumentDto[]>('http://localhost:8080/api/instruments');
  }

  getNotationTypes(): Observable<string[]> {
    return this.http.get<string[]>('http://localhost:8080/api/notation-types');
  }

  getKeys(): Observable<KeyDto[]> {
    return this.http.get<KeyDto[]>('http://localhost:8080/api/keys');
  }

  getArtists(): Observable<ArtistDto[]> {
    return this.http.get<ArtistDto[]>('http://localhost:8080/api/artists');
  }

  createArtist(payload: ArtistCreateDto): Observable<void> {
    return this.http.post<void>('http://localhost:8080/api/artists', payload);
  }

  getTunings(instrumentTypeId?: number): Observable<TuningDto[]> {
    let params = new HttpParams();
    if (instrumentTypeId) {
      params = params.set('instrumentTypeId', instrumentTypeId.toString());
    }
    return this.http.get<TuningDto[]>('http://localhost:8080/api/tunings', { params });
  }

  getSong(id: number): Observable<SongDto> {
    return this.http.get<SongDto>(`http://localhost:8080/api/songs/${id}`);
  }

  updateSong(id: number, payload: SongCreateDto): Observable<void> {
    return this.http.put<void>(`http://localhost:8080/api/songs/${id}`, payload);
  }

  getSongChords(songId: number, chordsId: number): Observable<SongChordsDetailsDto> {
    return this.http.get<SongChordsDetailsDto>(`http://localhost:8080/api/songs/${songId}/chords/${chordsId}`);
  }

  updateSongChords(songId: number, chordsId: number, payload: SongChordsCreateDto): Observable<void> {
    return this.http.put<void>(`http://localhost:8080/api/songs/${songId}/chords/${chordsId}`, payload);
  }

}
