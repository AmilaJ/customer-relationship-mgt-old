export interface FileInfo {
  id?: number;
  filename: string;
  originalFilename: string;
  size: number;
  contentType: string;
  uploadedAt?: string; // Changed from uploadDate to match backend
}

export interface FileUploadResponse {
  success: boolean;
  message: string;
  fileId?: number;
  filename?: string;
  originalFilename?: string;
  size?: number;
  contentType?: string;
} 