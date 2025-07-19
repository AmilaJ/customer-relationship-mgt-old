import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FileService } from '../../services/file.service';
import { AuthService } from '../../services/auth.service';
import { FileInfo } from '../../models/file.model';

@Component({
  selector: 'app-files',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './files.component.html',
  styleUrls: ['./files.component.scss']
})
export class FilesComponent implements OnInit {
  files: FileInfo[] = [];
  selectedFile: File | null = null;
  loading = false;
  error = '';

  constructor(
    private fileService: FileService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']);
      return;
    }
    
    this.loadFiles();
  }

  loadFiles(): void {
    this.loading = true;
    this.fileService.getFiles().subscribe({
      next: (files) => {
        this.files = files;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading files:', error);
        this.error = 'Failed to load files';
        this.loading = false;
      }
    });
  }

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
    }
  }

  uploadFile(): void {
    if (!this.selectedFile) {
      this.error = 'Please select a file to upload';
      return;
    }

    this.loading = true;
    this.fileService.uploadFile(this.selectedFile).subscribe({
      next: (response) => {
        if (response.success) {
          this.files.push({
            id: response.fileId,
            filename: response.filename!,
            originalFilename: response.originalFilename!,
            size: response.size!,
            contentType: response.contentType!
          });
          this.selectedFile = null;
          this.error = '';
        } else {
          this.error = response.message;
        }
        this.loading = false;
      },
      error: (error) => {
        console.error('Error uploading file:', error);
        this.error = 'Failed to upload file';
        this.loading = false;
      }
    });
  }

  downloadFile(fileInfo: FileInfo): void {
    this.fileService.downloadFile(fileInfo);
  }

  deleteFile(id: number): void {
    if (confirm('Are you sure you want to delete this file?')) {
      this.fileService.deleteFile(id).subscribe({
        next: () => {
          this.files = this.files.filter(f => f.id !== id);
        },
        error: (error) => {
          console.error('Error deleting file:', error);
          this.error = 'Failed to delete file';
        }
      });
    }
  }

  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }

  goBack(): void {
    this.router.navigate(['/dashboard']);
  }

  triggerFileInput(): void {
    const fileInput = document.getElementById('fileInput') as HTMLInputElement;
    if (fileInput) {
      fileInput.click();
    }
  }
} 