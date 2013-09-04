package ca.edchipman.silverstripepdt.parser;

import org.eclipse.wst.sse.core.internal.ltk.parser.BlockTokenizer;
import org.eclipse.wst.xml.core.internal.parser.XMLSourceParser;

public class SSSourceParser extends XMLSourceParser {
    protected BlockTokenizer getTokenizer() {
        if (fTokenizer == null) {
            fTokenizer = new SSTokenizer();
        }
        return fTokenizer;
    }
}
