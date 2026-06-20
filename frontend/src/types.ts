export type DocumentStatus = 'PROCESSING' | 'READY' | 'FAILED';

export type PdfDocument = {
  id: string;
  fileName: string;
  status: DocumentStatus;
  pageCount: number | null;
  errorMessage: string | null;
  createdAt: string;
};

export type ChatMessage = {
  id: string;
  role: 'user' | 'assistant' | string;
  content: string;
  createdAt: string;
};

export type Source = {
  chunkId: string;
  pageNumber: number;
  chunkIndex: number;
  score: number;
  snippet: string;
};

export type AskResponse = {
  answer: string;
  sources: Source[];
};
