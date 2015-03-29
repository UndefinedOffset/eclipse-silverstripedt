package ca.edchipman.silverstripepdt.editor;

import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.wst.css.core.text.ICSSPartitions;
import org.eclipse.wst.css.ui.internal.style.LineStyleProviderForEmbeddedCSS;
import org.eclipse.wst.html.core.text.IHTMLPartitions;
import org.eclipse.wst.html.ui.StructuredTextViewerConfigurationHTML;
import org.eclipse.wst.html.ui.internal.contentassist.HTMLStructuredContentAssistProcessor;
import org.eclipse.wst.sse.ui.internal.provisional.style.LineStyleProvider;
import org.eclipse.wst.xml.core.text.IXMLPartitions;

import ca.edchipman.silverstripepdt.contentassist.SSTemplateCompletionProcessor;
import ca.edchipman.silverstripepdt.style.LineStyleProviderForSS;

@SuppressWarnings("restriction")
public class StructuredTextViewerConfigurationSS extends StructuredTextViewerConfigurationHTML {
    /*
     * One instance per configuration
     */
    private LineStyleProvider fLineStyleProviderForEmbeddedCSS;
    
    /*
     * One instance per configuration
     */
    private LineStyleProviderForSS fLineStyleProviderForSS;
    
    protected String ssVersion;
    
    public LineStyleProvider[] getLineStyleProviders(ISourceViewer sourceViewer, String partitionType) {
        LineStyleProvider[] providers = null;

        // workaround IXMLPartitions.XML_PI
        if (partitionType == IHTMLPartitions.HTML_DEFAULT || partitionType == IHTMLPartitions.HTML_COMMENT || partitionType == IHTMLPartitions.HTML_DECLARATION || partitionType == IXMLPartitions.XML_PI) {
            providers = new LineStyleProvider[]{getLineStyleProviderForSS()};
        }else if (partitionType == ICSSPartitions.STYLE || partitionType == ICSSPartitions.COMMENT) {
            providers = new LineStyleProvider[]{getLineStyleProviderForEmbeddedCSS()};
        }

        return providers;
    }
    
    private LineStyleProvider getLineStyleProviderForSS() {
        if (fLineStyleProviderForSS == null) {
            fLineStyleProviderForSS = new LineStyleProviderForSS();
        }
        return fLineStyleProviderForSS;
    }
    
    private LineStyleProvider getLineStyleProviderForEmbeddedCSS() {
        if (fLineStyleProviderForEmbeddedCSS == null) {
            fLineStyleProviderForEmbeddedCSS = new LineStyleProviderForEmbeddedCSS();
        }
        return fLineStyleProviderForEmbeddedCSS;
    }
    
    protected IContentAssistProcessor[] getContentAssistProcessors(
            ISourceViewer sourceViewer, String partitionType) {
        
        IContentAssistProcessor htmlProcessor = new HTMLStructuredContentAssistProcessor(this.getContentAssistant(), partitionType, sourceViewer);
        IContentAssistProcessor ssProcessor = new SSTemplateCompletionProcessor();
        
        return new IContentAssistProcessor[]{htmlProcessor, ssProcessor};
    }
    
    public void setSSVersion(String version) {
        this.ssVersion=version;
        fLineStyleProviderForSS.setSSVersion(version);
    }
}
