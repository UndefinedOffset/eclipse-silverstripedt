package ca.edchipman.silverstripepdt.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.php.internal.core.documentModel.dom.ElementImplForPhp;
import org.eclipse.php.internal.core.preferences.CorePreferencesSupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import ca.edchipman.silverstripepdt.SilverStripeNature;
import ca.edchipman.silverstripepdt.views.DevBuildViewer;

@SuppressWarnings("restriction")
public class SilverStripeDevBuildAction implements IWorkbenchWindowActionDelegate {
    IProject _project=null;
    
    /**
     * Initializes the action
     */
    public SilverStripeDevBuildAction() {}
    
    /**
     * Initializes the action
     * @param project Project to bind to
     */
    public SilverStripeDevBuildAction(IProject project) {
        this._project=project;
    }

    @Override
    public void run(IAction action) {
        if(_project!=null) {
            String siteBase=CorePreferencesSupport.getInstance().getProjectSpecificPreferencesValue("silverstripe_site_base", null, _project);
            if(siteBase!=null && siteBase.isEmpty()==false) {
                try {
                    String finalURL=siteBase;
                    
                    if(finalURL.substring(finalURL.length()-1).equals("/")==false) {
                        finalURL=finalURL.concat("/");
                    }
                    
                    finalURL=finalURL.concat("dev/build?flush=all");
                    DevBuildViewer browser=((DevBuildViewer) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(DevBuildViewer.ID));
                    
                    if(browser==null) {
                        try {
                            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(DevBuildViewer.ID);
                        } catch (PartInitException e) {
                            MessageBox messageBox = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.ICON_ERROR | SWT.OK);
                            messageBox.setMessage("Browser cannot be initialized.");
                            messageBox.setText("Exit");
                            messageBox.open();
                        }
                        browser=((DevBuildViewer) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(DevBuildViewer.ID));
                    }
                    
                    browser.setTargetURL(finalURL);
                    
                    //Force view to show
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().activate(browser);
                }catch (SWTError e) {
                    MessageBox messageBox = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.ICON_ERROR | SWT.OK);
                    messageBox.setMessage("Browser cannot be initialized.");
                    messageBox.setText("Exit");
                    messageBox.open();
                }
            }else {
                MessageBox dialog=new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.ICON_ERROR | SWT.OK);
                dialog.setText("Site base not set");
                dialog.setMessage("You have not set the site base for this project, you can set this on the project's preferences for SilverStripe");
                dialog.open();
            }
        }
    }

    /**
     * Handles when the selection changes, checks to see if the project is a SilverStripe PDT Project
     * @param IAction Action to enable/disable
     * @param ISelection selection Selection to check
     */
    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        IResource resource=extractSelection(selection);
        if(resource!=null) {
            _project=resource.getProject();
            
            if(_project!=null) {
                try {
                    if(_project.isOpen() && _project.hasNature(SilverStripeNature.ID)) {
                        action.setEnabled(true);
                        return;
                    }
                }catch (CoreException e) {
                    e.printStackTrace();
                }
            }
        }else {
            _project=null;            
        }
        
        action.setEnabled(false);
    }
    
    /**
     * Cleanup on dispose
     */
    @Override
    public void dispose() {
        _project=null;
    }

    @Override
    public void init(IWorkbenchWindow window) {}
    
    /**
     * Gets the resource from the selection
     * @param sel Selection to find resource from
     * @return Returns the selected resource
     */
    protected final IResource extractSelection(ISelection sel) {
        if(!(sel instanceof IStructuredSelection) || (sel instanceof TextSelection)) {
        	IWorkbench iworkbench=PlatformUI.getWorkbench();
            if (iworkbench==null) {
                return null;
            }
            
            IWorkbenchWindow iworkbenchwindow=iworkbench.getActiveWorkbenchWindow();
            if (iworkbenchwindow==null) {
                return null;
            }
            
            IWorkbenchPage iworkbenchpage=iworkbenchwindow.getActivePage();
            if(iworkbenchpage==null) {
                return null;
            }
            
            IEditorPart ieditorpart=iworkbenchpage.getActiveEditor();
            if(ieditorpart==null) {
                return null;
            }
            
            return extractResource(ieditorpart);
        }
        
        
        IStructuredSelection ss=(IStructuredSelection) sel;
        Object element=ss.getFirstElement();
        
        if(element instanceof IResource) {
            return (IResource) element;
        }
        
        //If not IAdabptable get resource from the active editor
        if(!(element instanceof IAdaptable) || (element instanceof ElementImplForPhp)) {
            IWorkbench iworkbench=PlatformUI.getWorkbench();
            if (iworkbench==null) {
                return null;
            }
            
            IWorkbenchWindow iworkbenchwindow=iworkbench.getActiveWorkbenchWindow();
            if (iworkbenchwindow==null) {
                return null;
            }
            
            IWorkbenchPage iworkbenchpage=iworkbenchwindow.getActivePage();
            if(iworkbenchpage==null) {
                return null;
            }
            
            IEditorPart ieditorpart=iworkbenchpage.getActiveEditor();
            if(ieditorpart==null) {
                return null;
            }
            
            return extractResource(ieditorpart);
        }
        
        
        IAdaptable adaptable=(IAdaptable) element;
        Object adapter=adaptable.getAdapter(IResource.class);
        return (IResource) adapter;
    }
    
    /**
     * Extracts the reosurce from the active editor part
     * @param editor Editor part
     * @return Returns the current editor parts resource
     */
    private IResource extractResource(IEditorPart editor) {
        IEditorInput input=editor.getEditorInput();
        if(!(input instanceof IFileEditorInput)) {
            return null;
        }
        
        return ((IFileEditorInput) input).getFile();
    }
}