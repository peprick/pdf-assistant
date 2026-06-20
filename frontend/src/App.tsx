import { FormEvent, useEffect, useMemo, useState } from 'react';
import { askQuestion, listDocuments, listMessages, uploadDocument } from './api';
import type { AskResponse, ChatMessage, PdfDocument } from './types';

function App() {
  const [documents, setDocuments] = useState<PdfDocument[]>([]);
  const [selectedId, setSelectedId] = useState<string>('');
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [question, setQuestion] = useState('');
  const [latestSources, setLatestSources] = useState<AskResponse['sources']>([]);
  const [sourcesOpen, setSourcesOpen] = useState(false);
  const [busy, setBusy] = useState(false);
  const [notice, setNotice] = useState('');

  const selectedDocument = useMemo(
    () => documents.find((document) => document.id === selectedId) ?? null,
    [documents, selectedId]
  );

  useEffect(() => {
    refreshDocuments();
  }, []);

  useEffect(() => {
    if (!selectedId) {
      setMessages([]);
      return;
    }
    listMessages(selectedId)
      .then(setMessages)
      .catch((error) => setNotice(error.message));
  }, [selectedId]);

  async function refreshDocuments() {
    try {
      const nextDocuments = await listDocuments();
      setDocuments(nextDocuments);
      setSelectedId((current) => current || nextDocuments[0]?.id || '');
    } catch (error) {
      setNotice(error instanceof Error ? error.message : 'Could not load documents');
    }
  }

  async function handleUpload(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = event.currentTarget;
    const input = form.elements.namedItem('pdf') as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      setNotice('Choose a PDF first.');
      return;
    }

    setBusy(true);
    setNotice('Uploading and indexing PDF...');
    try {
      const uploaded = await uploadDocument(file);
      await refreshDocuments();
      setSelectedId(uploaded.id);
      setNotice('PDF is ready.');
      form.reset();
    } catch (error) {
      setNotice(error instanceof Error ? error.message : 'Upload failed');
    } finally {
      setBusy(false);
    }
  }

  async function handleAsk(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!selectedDocument || !question.trim()) {
      return;
    }

    const userQuestion = question.trim();
    const shouldOpenSources = /\b(source|sources|citation|citations|reference|references|page|pages|evidence)\b/i
      .test(userQuestion);
    setQuestion('');
    setLatestSources([]);
    setSourcesOpen(false);
    setBusy(true);
    setNotice('Thinking...');
    setMessages((current) => [
      ...current,
      {
        id: crypto.randomUUID(),
        role: 'user',
        content: userQuestion,
        createdAt: new Date().toISOString()
      }
    ]);

    try {
      const response = await askQuestion(selectedDocument.id, userQuestion);
      setLatestSources(response.sources);
      setSourcesOpen(shouldOpenSources);
      setMessages((current) => [
        ...current,
        {
          id: crypto.randomUUID(),
          role: 'assistant',
          content: response.answer,
          createdAt: new Date().toISOString()
        }
      ]);
      setNotice('');
    } catch (error) {
      setNotice(error instanceof Error ? error.message : 'Question failed');
    } finally {
      setBusy(false);
    }
  }

  return (
    <main className="app-shell">
      {notice === 'Thinking...' && (
        <div className="thinking-overlay" role="status" aria-live="polite">
          <div className="thinking-box">
            <div className="thinking-ring" />
            <div>
              <strong>Thinking</strong>
              <span>Finding the closest PDF context and asking the local model.</span>
            </div>
            <div className="thinking-dots" aria-hidden="true">
              <i />
              <i />
              <i />
            </div>
          </div>
        </div>
      )}

      <aside className="sidebar">
        <div className="brand">
          <span className="brand-mark">PDF</span>
          <div>
            <h1>Assistant</h1>
            <p>Local RAG workspace</p>
          </div>
        </div>

        <form className="upload-form" onSubmit={handleUpload}>
          <input name="pdf" type="file" accept="application/pdf" disabled={busy} />
          <button type="submit" disabled={busy}>Upload</button>
        </form>

        <div className="document-list">
          {documents.map((document) => (
            <button
              className={document.id === selectedId ? 'document-item active' : 'document-item'}
              key={document.id}
              type="button"
              onClick={() => setSelectedId(document.id)}
            >
              <span>{document.fileName}</span>
              <small>{document.status}{document.pageCount ? ` | ${document.pageCount} pages` : ''}</small>
            </button>
          ))}
          {documents.length === 0 && <p className="empty-state">No PDFs uploaded yet.</p>}
        </div>
      </aside>

      <section className="workspace">
        <header className="workspace-header">
          <div>
            <h2>{selectedDocument?.fileName ?? 'Upload a PDF to begin'}</h2>
            <p>{selectedDocument ? selectedDocument.status : 'Ask grounded questions with page citations.'}</p>
          </div>
        </header>

        <section className="chat-panel">
          <div className="messages">
            {messages.map((message) => (
              <article className={`message ${message.role}`} key={message.id}>
                <span>{message.role}</span>
                <p>{message.content}</p>
              </article>
            ))}
            {messages.length === 0 && (
              <div className="welcome">
                <h3>Ready when your PDF is.</h3>
                <p>Upload a document, then ask about definitions, summaries, obligations, dates, or confusing sections.</p>
              </div>
            )}
          </div>

          {latestSources.length > 0 && (
            <section className="source-area">
              <button
                className="source-toggle"
                type="button"
                onClick={() => setSourcesOpen((current) => !current)}
              >
                {sourcesOpen ? 'Hide sources' : `Show sources (${latestSources.length})`}
              </button>

              {sourcesOpen && (
                <div className="sources">
                  {latestSources.map((source) => (
                    <article className="source" key={source.chunkId}>
                      <header>
                        <strong>Page {source.pageNumber}</strong>
                        <span>Relevance {Math.round(source.score * 100)}%</span>
                      </header>
                      <p>{cleanSnippet(source.snippet)}</p>
                    </article>
                  ))}
                </div>
              )}
            </section>
          )}

          <form className="question-form" onSubmit={handleAsk}>
            <input
              value={question}
              onChange={(event) => setQuestion(event.target.value)}
              placeholder="Ask a question about the selected PDF"
              disabled={!selectedDocument || selectedDocument.status !== 'READY' || busy}
            />
            <button disabled={!selectedDocument || selectedDocument.status !== 'READY' || busy} type="submit">
              Ask
            </button>
          </form>
          {notice && <p className="notice">{notice}</p>}
        </section>
      </section>
    </main>
  );
}

function cleanSnippet(snippet: string) {
  return snippet
    .replace(/\s+/g, ' ')
    .replace(/\b([A-Z])\s+(?=[A-Z]\b)/g, '$1')
    .replace(/\b([a-z])\s+(?=[a-z]\b)/g, '$1')
    .replace(/\s+([.,;:!?])/g, '$1')
    .trim();
}

export default App;
