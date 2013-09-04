package ca.edchipman.silverstripepdt.encoding;

import org.eclipse.wst.html.core.internal.encoding.HTMLDocumentLoader;
import org.eclipse.wst.sse.core.internal.ltk.parser.RegionParser;

import ca.edchipman.silverstripepdt.parser.SSSourceParser;

public class SilverStripeDocumentLoader extends HTMLDocumentLoader {
    /*
     * @see IModelLoader#getParser()
     */
    public RegionParser getParser() {
        SSSourceParser parser = new SSSourceParser();
        // for the "static HTML" case, we need to initialize
        // Blocktags here.
        addHTMLishTag(parser, "script"); //$NON-NLS-1$
        addHTMLishTag(parser, "style"); //$NON-NLS-1$
        return parser;
    }
}
