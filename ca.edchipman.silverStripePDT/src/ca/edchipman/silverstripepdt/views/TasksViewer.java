package ca.edchipman.silverstripepdt.views;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.php.internal.core.documentModel.dom.ElementImplForPhp;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.custom.CBanner;

import ca.edchipman.silverstripepdt.SilverStripeNature;
import ca.edchipman.silverstripepdt.SilverStripePluginImages;

public class TasksViewer extends ViewPart {

    public static final String ID = "ca.edchipman.silverstripepdt.views.TasksView"; //$NON-NLS-1$
    private RefreshAction refreshAction;

    public TasksViewer() {
    }

    /**
     * Create contents of the view part.
     * @param parent
     */
    @Override
    public void createPartControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout(2, false));
        
        List list = new List(container, SWT.BORDER | SWT.V_SCROLL);
        GridData gd_list = new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1);
        gd_list.widthHint = 260;
        list.setLayoutData(gd_list);
        
        Composite composite = new Composite(container, SWT.BORDER);
        composite.setLayout(new FillLayout(SWT.HORIZONTAL));
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        
        Browser browser = new Browser(composite, SWT.NONE);

        createActions();
        initializeToolBar();
        
        
        //Listen for the selection of files to change
        getSite().getWorkbenchWindow().getSelectionService().addPostSelectionListener(listener);
    }

    public void dispose() {
        super.dispose();

        getSite().getWorkbenchWindow().getSelectionService().removePostSelectionListener(listener);
    }

    /**
     * Create the actions.
     */
    private void createActions() {
        // Create the actions
    }

    /**
     * Initialize the toolbar.
     */
    private void initializeToolBar() {
        refreshAction = new RefreshAction(this);
        
        IToolBarManager tbm = getViewSite().getActionBars().getToolBarManager();
        tbm.add(refreshAction);
    }

    @Override
    public void setFocus() {
        // Set the focus
    }

    public void refreshTasks() {
        // TODO Auto-generated method stub
        
    }

    protected void handleSelectionChange(IProject _project) {
        // TODO Auto-generated method stub
        
    }
    
    private ISelectionListener listener=new ISelectionListener() {
        public void selectionChanged(IWorkbenchPart sourcepart, ISelection selection) {
            IResource resource=extractSelection(selection);
            if(resource!=null) {
                IProject _project=resource.getProject();
                
                if(_project!=null) {
                    try {
                        if(_project.isOpen() && _project.hasNature(SilverStripeNature.ID)) {
                            handleSelectionChange(_project);
                        }
                    }catch (CoreException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        
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
    };
    
    private class RefreshAction extends Action {
        private TasksViewer tasksViewer;

        public RefreshAction(TasksViewer _tasksViewer) {
            this.tasksViewer=_tasksViewer;
            
            this.setText("Refresh");
            this.setDescription("Refresh Tasks List");
            this.setImageDescriptor(SilverStripePluginImages.IMG_REFRESH);
            this.setEnabled(false);
        }

        @Override 
        public void run() {
            tasksViewer.refreshTasks();
        }
    }

}
