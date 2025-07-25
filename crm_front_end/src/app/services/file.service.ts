import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { FileInfo, FileUploadResponse } from '../models/file.model';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class FileService {
  private readonly API_URL = 'http://localhost:8089';

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  private getHeaders(): HttpHeaders {
    const token = this.authService.getAuthToken();
    const headers = new HttpHeaders({
      ...(token && { 'Authorization': `Bearer ${token}` })
    });
    console.log('Request headers:', headers.keys());
    return headers;
  }

  getFiles(): Observable<FileInfo[]> {
    console.log('Fetching files from:', `${this.API_URL}/files`);
    return this.http.get<{files: FileInfo[]}>(`${this.API_URL}/files`)
      .pipe(
        map((response: {files: FileInfo[]}) => {
          console.log('Raw response:', response);
          return response.files || [];
        })
      );
  }

  getFileById(id: number): Observable<Blob> {
    return this.http.get(`${this.API_URL}/files?id=${id}`, { 
      headers: this.getHeaders(),
      responseType: 'blob'
    });
  }

  getFileByFilename(filename: string): Observable<Blob> {
    return this.http.get(`${this.API_URL}/files?filename=${filename}`, { 
      headers: this.getHeaders(),
      responseType: 'blob'
    });
  }

  uploadFile(file: File): Observable<FileUploadResponse> {
    const formData = new FormData();
    formData.append('file', file);
    
    console.log('Uploading file:', file.name, 'Size:', file.size);
    
    // Temporarily remove all custom headers to test
    return this.http.post<FileUploadResponse>(`${this.API_URL}/files`, formData);
  }

  deleteFile(id: number): Observable<any> {
    return this.http.delete(`${this.API_URL}/files?id=${id}`, { headers: this.getHeaders() });
  }

  downloadFile(fileInfo: FileInfo): void {
    this.getFileById(fileInfo.id!).subscribe(blob => {
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = fileInfo.originalFilename;
      link.click();
      window.URL.revokeObjectURL(url);
    });
  }
} 