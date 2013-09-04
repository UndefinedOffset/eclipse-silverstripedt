package ca.edchipman.silverstripepdt.modelhandler;

import org.eclipse.wst.html.core.internal.modelhandler.ModelHandlerForHTML;
import org.eclipse.wst.sse.core.internal.document.IDocumentLoader;

import ca.edchipman.silverstripepdt.encoding.SilverStripeDocumentLoader;

public class ModelHandlerForSS extends ModelHandlerForHTML {
    /** 
     * Needs to match what's in plugin registry. 
     * In fact, can be overwritten at run time with 
     * what's in registry! (so should never be 'final')
     */
    static String AssociatedContentTypeID = "ca.edchipman.silverstripepdt.SilverStripeTemplateSource"; //$NON-NLS-1$
    
    /**
     * Needs to match what's in plugin registry. 
     * In fact, can be overwritten at run time with 
     * what's in registry! (so should never be 'final')
     */
    private static String ModelHandlerID_SS = "ca.edchipman.silverstripepdt.modelhandler"; //$NON-NLS-1$
    
    public ModelHandlerForSS() {
        super();
        setId(ModelHandlerID_SS);
        setAssociatedContentTypeId(AssociatedContentTypeID);
    }
    
    public IDocumentLoader getDocumentLoader() {
        return new SilverStripeDocumentLoader();
    }
}
