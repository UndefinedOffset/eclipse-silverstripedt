package ca.edchipman.silverstripepdt.editor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.php.internal.core.preferences.CorePreferencesSupport;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.wst.sse.ui.StructuredTextEditor;

import ca.edchipman.silverstripepdt.SilverStripeVersion;
import ca.edchipman.silverstripepdt.preferences.SilverStripePreferences;

@SuppressWarnings("restriction")
public class SilverStripeTemplateStructuredEditor extends StructuredTextEditor {
    protected String ssVersion;
    
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IEditorPart#init(org.eclipse.ui.IEditorSite,
     * org.eclipse.ui.IEditorInput)
     */
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);
        
        if (input instanceof IFileEditorInput) {
            IFile file = ((IFileEditorInput)input).getFile();
            IProject project = file.getProject();

            this.ssVersion=CorePreferencesSupport.getInstance().getProjectSpecificPreferencesValue(SilverStripePreferences.SILVERSTRIPE_VERSION, SilverStripeVersion.DEFAULT_VERSION, project);
        }
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Use StructuredTextViewerConfiguration if a viewerconfiguration has not
     * already been set. Also initialize StructuredTextViewer.
     * </p>
     * 
     * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        
        if(getSourceViewerConfiguration() instanceof StructuredTextViewerConfigurationSS) {
            ((StructuredTextViewerConfigurationSS) getSourceViewerConfiguration()).setSSVersion(this.ssVersion);
        }
    }
    
    public void update() {
        super.update();
        
        if(getSourceViewerConfiguration() instanceof StructuredTextViewerConfigurationSS && this.ssVersion!=null) {
            ((StructuredTextViewerConfigurationSS) getSourceViewerConfiguration()).setSSVersion(this.ssVersion);
        }
    }
    
    public void setSSVersion(String version) {
        this.ssVersion=version;
        
        if(getSourceViewerConfiguration() instanceof StructuredTextViewerConfigurationSS && this.ssVersion!=null) {
            ((StructuredTextViewerConfigurationSS) getSourceViewerConfiguration()).setSSVersion(this.ssVersion);
        }
    }
}
