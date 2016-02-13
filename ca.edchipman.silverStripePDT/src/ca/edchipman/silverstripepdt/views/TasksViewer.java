package ca.edchipman.silverstripepdt.views;

import java.lang.reflect.Array;
import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.IField;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.IType;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.dltk.core.index2.search.ISearchEngine.MatchRule;
import org.eclipse.dltk.core.search.IDLTKSearchScope;
import org.eclipse.dltk.core.search.SearchEngine;
import org.eclipse.dltk.internal.ui.typehierarchy.SubTypeHierarchyViewer.SubTypeHierarchyContentProvider;
import org.eclipse.dltk.internal.ui.typehierarchy.TypeHierarchyLifeCycle;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.php.internal.core.documentModel.dom.ElementImplForPhp;
import org.eclipse.php.internal.core.model.PhpModelAccess;
import org.eclipse.php.internal.core.preferences.CorePreferencesSupport;
import org.eclipse.php.internal.core.typeinference.PHPModelUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.custom.ScrolledComposite;

import ca.edchipman.silverstripepdt.SilverStripeNature;
import ca.edchipman.silverstripepdt.SilverStripePluginImages;

@SuppressWarnings("restriction")
public class TasksViewer extends ViewPart {
    public static final String ID = "ca.edchipman.silverstripepdt.views.TasksViewer"; //$NON-NLS-1$
    
    private RefreshAction refreshAction;
    private Composite fViewStack;
    private StackLayout fViewStackLayout;
    private Composite fTasksView;
    private Label fErrorLabel;
    private Browser fTasksBrowser;
    private Composite fErrorView;
    private Composite fTasksList;
    private ProgressBar progressBar;
    private ArrayList<SilverStripeTask> projectTasks;
    
    private TaskProgressListener progressListener;

    private IProject fLastProject;

    private boolean projectTasksLoading;

    public TasksViewer() {
    }

    /**
     * Create contents of the view part.
     * @param parent
     */
    @Override
    public void createPartControl(Composite parent) {
        fViewStack = new Composite(parent, SWT.NONE);
        fViewStackLayout=new StackLayout();
        fViewStack.setLayout(fViewStackLayout);
        
        
        fTasksView = new Composite(fViewStack, SWT.NONE);
        fTasksView.setLayout(new GridLayout(2, false));
        
        ScrolledComposite scrolledComposite = new ScrolledComposite(fTasksView, SWT.BORDER | SWT.V_SCROLL);
        scrolledComposite.setBackground(parent.getBackground());
        GridData gd_scrolledComposite = new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1);
        gd_scrolledComposite.widthHint = 260;
        scrolledComposite.setLayoutData(gd_scrolledComposite);
        
        fTasksList = new Composite(scrolledComposite, SWT.NONE);
        GridLayout gl_fTasksList = new GridLayout(1, false);
        gl_fTasksList.marginHeight = 10;
        gl_fTasksList.marginRight = 5;
        fTasksList.setLayout(gl_fTasksList);
        fTasksList.setLayoutData(new GridData(GridData.FILL_BOTH));
        fTasksList.setBackground(parent.getBackground());
        scrolledComposite.setContent(fTasksList);
        
        Composite rightComp = new Composite(fTasksView, SWT.NONE);
        GridLayout gl_rightComp = new GridLayout(1, false);
        gl_rightComp.marginWidth = 0;
        gl_rightComp.marginHeight = 0;
        rightComp.setLayout(gl_rightComp);
        rightComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        
        
        progressBar = new ProgressBar(rightComp, SWT.SMOOTH | SWT.HORIZONTAL);
        progressBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        progressBar.setVisible(false);
        progressBar.setMinimum(0);
        
        boolean browserInitError=false;
        
        try {
            fTasksBrowser = new Browser(rightComp, SWT.NONE);
            fTasksBrowser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
            progressListener=new TaskProgressListener(fTasksBrowser, progressBar);
            fTasksBrowser.addProgressListener(progressListener);
        } catch (SWTError error) {
            if(progressListener!=null) {
                fTasksBrowser.removeProgressListener(progressListener);
            }
            
            fTasksBrowser = null;
            browserInitError=true;
        }
        
        fErrorView = new Composite(fViewStack, SWT.NONE);
        fErrorView.setLayout(new GridLayout(1, false));
        
        fErrorLabel = new Label(fErrorView, SWT.CENTER);
        fErrorLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
        fErrorLabel.setAlignment(SWT.CENTER);
        
        if(browserInitError) {
            fErrorLabel.setText("SWT Browser control is not available. Please refer to: http://www.eclipse.org/swt/faq.php#whatisbrowser for more information.");
        }else {
            fErrorLabel.setText("No project is selected");
        }
        
        
        ControlDecoration controlDecoration = new ControlDecoration(fErrorLabel, SWT.LEFT | SWT.TOP);
        controlDecoration.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
        
        createActions();
        initializeToolBar();
        
        //Find the current project
        IResource currentSelection=extractSelection(getSite().getWorkbenchWindow().getSelectionService().getSelection());
        if(currentSelection!=null) {
            IProject project=currentSelection.getProject();
            try {
                if(project.hasNature(SilverStripeNature.ID)) {
                    String siteBase=CorePreferencesSupport.getInstance().getProjectSpecificPreferencesValue("silverstripe_site_base", null, project);
                    if(siteBase!=null && siteBase.isEmpty()==false) {
                        refreshAction.setEnabled(true);
                        fLastProject=project;
                        
                        this.getSite().getPage().addPartListener(viewPartListener);
                    }else {
                        refreshAction.setEnabled(false);
                        
                        fErrorLabel.setText("You have not set the site base for this project, you can set this on the project's preferences for SilverStripe");
                        if(fViewStackLayout.topControl!=fErrorView) {
                            fViewStackLayout.topControl=fErrorView;
                            fViewStack.layout();
                        }
                    }
                }else {
                    refreshAction.setEnabled(false);
                    
                    fErrorLabel.setText("You must add SilverStripe Support to your project to use this view");
                    if(fViewStackLayout.topControl!=fErrorView) {
                        fViewStackLayout.topControl=fErrorView;
                        fViewStack.layout();
                    }
                }
            } catch (CoreException e) {
                e.printStackTrace();
            }
        }else {
            fViewStackLayout.topControl=fErrorView;
            fViewStack.layout();
        }
        
        
        //Listen for the selection of files to change, if the browser is working
        if(browserInitError==false) {
            getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(listener);
        }
    }

    public void dispose() {
        getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(listener);
        
        if(projectTasks!=null && projectTasks.isEmpty()==false) {
            for(SilverStripeTask task : projectTasks) {
                task.dispose();
            }
            
            projectTasks.removeAll(projectTasks);
            projectTasks=null;
        }
        
        super.dispose();
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
    
    /**
     * Refreshes the list of tasks based on the current project
     */
    public void refreshTasks() {
        if(projectTasksLoading) {
            return;
        }
        
        projectTasksLoading=true;
        
        if(projectTasks!=null && projectTasks.isEmpty()==false) {
            for(SilverStripeTask task : projectTasks) {
                task.dispose();
            }
            
            projectTasks.removeAll(projectTasks);
            
            fTasksList.setSize(fTasksList.computeSize(fTasksList.getParent().getClientArea().width, SWT.FILL));
            fTasksList.layout(true);
        }else {
            projectTasks=new ArrayList<SilverStripeTask>();
        }
        
        String siteBase=CorePreferencesSupport.getInstance().getProjectSpecificPreferencesValue("silverstripe_site_base", null, fLastProject);
        if(siteBase!=null && siteBase.isEmpty()==false) {
            String finalURL=siteBase;
            if(finalURL.substring(finalURL.length()-1).equals("/")==false) {
                finalURL=finalURL.concat("/");
            }
            
            finalURL=finalURL.concat("dev/tasks");
            
            IScriptProject project=DLTKCore.create(fLastProject);
            IModelElement[] elements=new IModelElement[] { project };
            IDLTKSearchScope scope=SearchEngine.createSearchScope(elements, IDLTKSearchScope.SYSTEM_LIBRARIES, project.getLanguageToolkit());
            IType[] buildTypes=PhpModelAccess.getDefault().findTypes("BuildTask", MatchRule.EXACT, 0, 0, scope, new NullProgressMonitor());
            buildTypes=this.concatTypeArray(buildTypes, PhpModelAccess.getDefault().findTypes("MigrationTask", MatchRule.EXACT, 0, 0, scope, new NullProgressMonitor()));
            if(buildTypes.length>1) {
                IType buildTaskType=buildTypes[0];
                IType migrationTaskType=buildTypes[1];
                
                try {
                    TypeHierarchyLifeCycle lifecycle=new TypeHierarchyLifeCycle();
                    lifecycle.doHierarchyRefresh(buildTaskType, null);
                    
                    SubTypeHierarchyContentProvider provider=new SubTypeHierarchyContentProvider(lifecycle);
                    Object[] decendents=provider.getChildren(buildTaskType);
                    decendents=this.concatObjectArray(decendents, provider.getChildren(migrationTaskType));
                    
                    if(decendents.length>0) {
                        for(Object taskObj : decendents) {
                            if(taskObj instanceof IType) {
                                IType task=(IType) taskObj;
                                
                                //Skip the MigrationTask class
                                if(task.getFullyQualifiedName().equals("MigrationTask")) {
                                    continue;
                                }
                                
                                
                                String taskTitle=PHPModelUtils.extractElementName(task.getFullyQualifiedName());
                                String taskURL=finalURL.concat("/"+taskTitle);
                                String taskDesc="No description";
                                
                                IField taskTitleField=task.getField("$title");
                                if(taskTitleField!=null && taskTitleField.exists()) {
                                    taskTitle=taskTitleField.getSource().replaceFirst("(?s)^(.*)(\\s*)=(\\s*)(\"|')(.*)(\"|')$", "$5").trim().replaceAll("(\n|\r)", " ").replaceAll("(\\s+|\t+)", " ");
                                }
                                
                                IField taskDescField=task.getField("$description");
                                if(taskDescField!=null && taskDescField.exists()) {
                                    taskDesc=taskDescField.getSource().replaceFirst("(?s)^(.*)(\\s*)=(\\s*)(\"|')(.*)(\"|')$", "$5").trim().replaceAll("(\n|\r)", " ").replaceAll("(\\s+|\t+)", " ");
                                }
                                
                                if(task.getSuperClasses()[0].equals("MigrationTask")) {
                                    projectTasks.add(new SilverStripeTask(fTasksList, taskTitle.concat(" (up)"), taskURL, taskDesc));
                                    
                                    projectTasks.add(new SilverStripeTask(fTasksList, taskTitle.concat(" (down)"), taskURL.concat("?Direction=down"), taskDesc));
                                }else {
                                    projectTasks.add(new SilverStripeTask(fTasksList, taskTitle, taskURL, taskDesc));
                                }
                            }
                        }
                        
                        
                        //Clean up
                        lifecycle.freeHierarchy();
                        provider.dispose();
                        
                        
                        if(fViewStackLayout.topControl!=fTasksView) {
                            fViewStackLayout.topControl=fTasksView;
                            fViewStack.layout();
                        }
                        
                        fTasksList.setSize(fTasksList.computeSize(fTasksList.getParent().getClientArea().width, SWT.DEFAULT));
                        fTasksList.layout(true, true);
                    }else {
                        fErrorLabel.setText("Could not find any build tasks");
                        if(fViewStackLayout.topControl!=fErrorView) {
                            fViewStackLayout.topControl=fErrorView;
                            fViewStack.layout();
                        }
                    }
                } catch (ModelException e) {
                    fErrorLabel.setText("Error loading build tasks");
                    if(fViewStackLayout.topControl!=fErrorView) {
                        fViewStackLayout.topControl=fErrorView;
                        fViewStack.layout();
                    }
                    
                    e.printStackTrace();
                }
            }
        }
        
        projectTasksLoading=false;
    }
    
    /**
     * Concatenates two IType arrays into one
     * @param leftArray First array to concatenate
     * @param rightArray Second array to concatenate
     * @return Resulting array
     */
    private IType[] concatTypeArray(IType[] leftArray, IType[] rightArray) {
        int leftLen = leftArray.length;
        int rightLen = rightArray.length;

        IType[] result = (IType[]) Array.newInstance(leftArray.getClass().getComponentType(), leftLen+rightLen);
        System.arraycopy(leftArray, 0, result, 0, leftLen);
        System.arraycopy(rightArray, 0, result, leftLen, rightLen);
        
        return result;
    }
    
    /**
     * Concatenates two Object arrays into one
     * @param leftArray First array to concatenate
     * @param rightArray Second array to concatenate
     * @return Resulting array
     */
    private Object[] concatObjectArray(Object[] leftArray, Object[] rightArray) {
        int leftLen = leftArray.length;
        int rightLen = rightArray.length;

        Object[] result = (Object[]) Array.newInstance(leftArray.getClass().getComponentType(), leftLen+rightLen);
        System.arraycopy(leftArray, 0, result, 0, leftLen);
        System.arraycopy(rightArray, 0, result, leftLen, rightLen);
        
        return result;
    }
    
    /**
     * Runs the task on the webserver
     * @param taskURL
     */
    protected void runTask(String taskURL) {
        progressBar.setVisible(true);
        progressBar.setMaximum(1);
        progressBar.setSelection(0);
        
        fTasksBrowser.setUrl(taskURL);
    }
    
    /**
     * Handles when the selection changes in the platform
     * @param _project Project the selection belongs to
     */
    protected void handleSelectionChange(IProject _project) {
        try {
            if(_project.hasNature(SilverStripeNature.ID)) {
                String siteBase=CorePreferencesSupport.getInstance().getProjectSpecificPreferencesValue("silverstripe_site_base", null, _project);
                if(siteBase!=null && siteBase.isEmpty()==false) {
                    refreshAction.setEnabled(true);
                    
                    if(fLastProject==null || _project.getName().equals(fLastProject.getName())==false) {
                        fTasksBrowser.setUrl("about:blank");
                        fLastProject=_project;
                        
                        if(fViewStackLayout.topControl!=fTasksView) {
                            fViewStackLayout.topControl=fTasksView;
                            fViewStack.layout();
                        }
                        
                        this.refreshTasks();
                    }
                }else {
                    refreshAction.setEnabled(false);
                    
                    fErrorLabel.setText("You have not set the site base for this project, you can set this on the project's preferences for SilverStripe");
                    if(fViewStackLayout.topControl!=fErrorView) {
                        fViewStackLayout.topControl=fErrorView;
                        fViewStack.layout();
                    }
                }
            }else {
                refreshAction.setEnabled(false);
                
                fErrorLabel.setText("You must add SilverStripe Support to your project to use this view");
                if(fViewStackLayout.topControl!=fErrorView) {
                    fViewStackLayout.topControl=fErrorView;
                    fViewStack.layout();
                }
            }
        } catch (CoreException e) {
            e.printStackTrace();
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
     * Extracts the resource from the active editor part
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
    
    /**
     * Selection listener
     */
    private ISelectionListener listener=new ISelectionListener() {
        public void selectionChanged(IWorkbenchPart sourcepart, ISelection selection) {
            if(sourcepart.getSite().getId().equals(TasksViewer.ID)) {
                return;
            }
            
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
    };
    
    /**
     * View part listener
     */
    private IPartListener2 viewPartListener=new IPartListener2() {
        @Override
        public void partVisible(IWorkbenchPartReference partRef) {
            if(partRef.getId().equals(TasksViewer.ID)) {
                Display.getCurrent().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        TasksViewer.this.refreshTasks();
                    }
                });
                
                TasksViewer.this.getSite().getPage().removePartListener(viewPartListener);
                viewPartListener=null;
            }
        }
        
        @Override
        public void partOpened(IWorkbenchPartReference partRef) {}
        
        @Override
        public void partInputChanged(IWorkbenchPartReference partRef) {}
        
        @Override
        public void partHidden(IWorkbenchPartReference partRef) {}
        
        @Override
        public void partDeactivated(IWorkbenchPartReference partRef) {}
        
        @Override
        public void partClosed(IWorkbenchPartReference partRef) {}
        
        @Override
        public void partBroughtToTop(IWorkbenchPartReference partRef) {}
        
        @Override
        public void partActivated(IWorkbenchPartReference partRef) {}
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
    
    private class TaskProgressListener implements ProgressListener {
        private ProgressBar progress;
        private Browser browser;

        /**
         * @param bar Progress bar to update
         */
        public TaskProgressListener(Browser browser, ProgressBar bar) {
            this.browser=browser;
            progress = bar;
        }
        
        /**
         * Handles when the progress changes
         * @param ProgressEvent Progress event data
         */
        public void changed(ProgressEvent event) {
            if(progressBar.isDisposed()==false && event.total>0) {
                if(event.total>progress.getMaximum()) {
                    progressBar.setMaximum(event.total);
                }
                
                if(event.current<progress.getMaximum()) {
                    progress.setVisible(true);
                    progress.setSelection(event.current);
                }else {
                    progress.setSelection(event.current);
                }
            }
        }
        
        /**
         * Handles when the progress completes
         * @param ProgressEvent Progress event data
         */
        public void completed(ProgressEvent event) {
            progress.setVisible(false);
            progress.setMaximum(1);
            progress.setSelection(0);
            browser.evaluate("var links=document.getElementsByTagName('a');" +
                            "for(var i=0;i<links.length;i++) { "+
                                "links[i].href='#'; "+
                            "}");
        }
    }
    
    private class SilverStripeTask extends Composite {
        private Label fTaskTitleLbl;
        private Label fTaskDescLbl;
        private String taskURL;
        private Color defaultBackground;
        private Color hoverBackgroundColor;
        private Listener mouseClickListener;
        private Listener mouseExitListener;
        private Listener mouseEnterListener;

        public SilverStripeTask(Composite parent, String title, String url, String description) {
            super(parent, SWT.NONE);
            
            defaultBackground=parent.getBackground();
            hoverBackgroundColor=Display.getCurrent().getSystemColor(SWT.COLOR_LIST_SELECTION);
            
            
            this.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
            this.setLayout(new GridLayout(1, false));
            this.setBackground(defaultBackground);
            
            taskURL=url;
            
            fTaskTitleLbl=new Label(this, SWT.WRAP | SWT.NO_BACKGROUND);
            fTaskTitleLbl.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
            fTaskTitleLbl.setText(title);
            FontDescriptor boldDescriptor=FontDescriptor.createFrom(fTaskTitleLbl.getFont()).setStyle(SWT.BOLD);
            Font boldFont = boldDescriptor.createFont(fTaskTitleLbl.getDisplay());
            fTaskTitleLbl.setFont(boldFont);
            fTaskTitleLbl.setBackground(defaultBackground);
            
            fTaskDescLbl=new Label(this, SWT.WRAP);
            fTaskDescLbl.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
            fTaskDescLbl.setText(description);
            fTaskDescLbl.setBackground(defaultBackground);
            
            //Bind listeners
            mouseClickListener=new Listener() {
                @Override
                public void handleEvent(Event e) {
                    TasksViewer.this.runTask(taskURL);
                }
            };
            
            mouseEnterListener=new Listener() {
                @Override
                public void handleEvent(Event e) {
                    SilverStripeTask.this.setBackground(hoverBackgroundColor);
                    fTaskTitleLbl.setBackground(hoverBackgroundColor);
                    fTaskDescLbl.setBackground(hoverBackgroundColor);
                }
            };
            
            mouseExitListener=new Listener() {
                @Override
                public void handleEvent(Event e) {
                    SilverStripeTask.this.setBackground(defaultBackground);
                    fTaskTitleLbl.setBackground(defaultBackground);
                    fTaskDescLbl.setBackground(defaultBackground);
                }
            };
            
            
            //Bind Listeners
            this.addListener(SWT.MouseUp, mouseClickListener);
            this.addListener(SWT.MouseEnter, mouseEnterListener);
            this.addListener(SWT.MouseExit, mouseExitListener);
            
            fTaskTitleLbl.addListener(SWT.MouseUp, mouseClickListener);
            fTaskTitleLbl.addListener(SWT.MouseEnter, mouseEnterListener);
            fTaskTitleLbl.addListener(SWT.MouseExit, mouseExitListener);
            
            fTaskDescLbl.addListener(SWT.MouseUp, mouseClickListener);
            fTaskDescLbl.addListener(SWT.MouseEnter, mouseEnterListener);
            fTaskDescLbl.addListener(SWT.MouseExit, mouseExitListener);
        }
        
        public void dispose() {
            mouseClickListener=null;
            mouseEnterListener=null;
            mouseExitListener=null;
            
            
            //Cleanup colors
            defaultBackground.dispose();
            hoverBackgroundColor.dispose();
            
            
            super.dispose();
        }
    }
}
