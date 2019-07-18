package org.checkerframework.lsp;

import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.services.TextDocumentService;

public class CFTextDocumentService implements TextDocumentService {
    private final CFLanguageServer server;

    CFTextDocumentService(CFLanguageServer server) {
        this.server = server;
    }

    /**
     * The document open notification is sent from the client to the server to
     * signal newly opened text documents. The document's truth is now managed
     * by the client and the server must not try to read the document's truth
     * using the document's uri.
     * <p>
     * Registration Options: TextDocumentRegistrationOptions
     *
     * @param params
     */
    @Override
    public void didOpen(DidOpenTextDocumentParams params) {

    }

    /**
     * The document change notification is sent from the client to the server to
     * signal changes to a text document.
     * <p>
     * Registration Options: TextDocumentChangeRegistrationOptions
     *
     * @param params
     */
    @Override
    public void didChange(DidChangeTextDocumentParams params) {

    }

    /**
     * The document close notification is sent from the client to the server
     * when the document got closed in the client. The document's truth now
     * exists where the document's uri points to (e.g. if the document's uri is
     * a file uri the truth now exists on disk).
     * <p>
     * Registration Options: TextDocumentRegistrationOptions
     *
     * @param params
     */
    @Override
    public void didClose(DidCloseTextDocumentParams params) {

    }

    /**
     * The document save notification is sent from the client to the server when
     * the document for saved in the client.
     * <p>
     * Registration Options: TextDocumentSaveRegistrationOptions
     *
     * @param params
     */
    @Override
    public void didSave(DidSaveTextDocumentParams params) {

    }
}
