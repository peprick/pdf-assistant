import type { AskResponse, ChatMessage, PdfDocument } from './types';

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? '/api';

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, options);
  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || `Request failed with ${response.status}`);
  }
  return response.json() as Promise<T>;
}

export function listDocuments(): Promise<PdfDocument[]> {
  return request<PdfDocument[]>('/documents');
}

export function listMessages(documentId: string): Promise<ChatMessage[]> {
  return request<ChatMessage[]>(`/documents/${documentId}/messages`);
}

export function uploadDocument(file: File): Promise<PdfDocument> {
  const formData = new FormData();
  formData.append('file', file);
  return request<PdfDocument>('/documents', {
    method: 'POST',
    body: formData
  });
}

export function askQuestion(documentId: string, question: string): Promise<AskResponse> {
  return request<AskResponse>(`/documents/${documentId}/questions`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ question })
  });
}
