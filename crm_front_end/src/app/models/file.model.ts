export interface FileInfo {
  id?: number;
  filename: string;
  originalFilename: string;
  size: number;
  contentType: string;
  uploadDate?: string;
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