package ca.edchipman.silverstripepdt.templates;

import org.eclipse.jface.text.templates.*;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.dltk.ui.templates.ScriptTemplateContext;

public class FileNameTemplateResolver extends SimpleTemplateVariableResolver {
    /** Name of the line selection variable, value= {@value} */
    public static final String NAME="file_name";

    /**
     * Creates a new line selection variable
     */
    public FileNameTemplateResolver() {
        super(NAME, "File Name without extension");
    }
    
    /**
     * {@inheritDoc}
     */
    protected String resolve(TemplateContext context) {
        String fileName=((ScriptTemplateContext)context).getSourceModule().getElementName();
        if(fileName==null) {
            fileName="";
        }else if(fileName!="") {
            int pos=fileName.lastIndexOf('.');
            fileName=fileName.substring(0,pos);
            
            return fileName.replace(".", "_");
        }
        
        IWorkbenchWindow win = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = win.getActivePage();
        
        if(page != null) {
            IEditorPart editor = page.getActiveEditor();
            if (editor != null) {
                IEditorInput input = editor.getEditorInput();
                if (input instanceof IFileEditorInput) {
                    fileName = ((IFileEditorInput)input).getFile().getName();
                }
            }
        }
        
        if(fileName!="") {
            int pos=fileName.lastIndexOf('.');
            fileName=fileName.substring(0,pos);
            
            return fileName.replace(".", "_");
        }
        
        return "";
    }
}
