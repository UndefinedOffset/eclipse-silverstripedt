package ca.edchipman.silverstripepdt.views;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.dltk.core.DLTKCore;
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
import org.eclipse.php.internal.core.documentModel.dom.ElementImplForPHP;
import org.eclipse.php.internal.core.model.PHPModelAccess;
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
import ca.edchipman.silverstripepdt.SilverStripePreferences;
import ca.edchipman.silverstripepdt.SilverStripeVersion;

@SuppressWarnings("restriction")
public class TestsViewer extends ViewPart {
    public static final String ID = "ca.edchipman.silverstripepdt.views.TestsViewer"; //$NON-NLS-1$
    
    private RefreshAction refreshAction;
    private Composite fViewStack;
    private StackLayout fViewStackLayout;
    private Composite fTestsView;
    private Label fErrorLabel;
    private Browser fTestsBrowser;
    private Composite fErrorView;
    private Composite fTestsList;
    private ProgressBar progressBar;
    private ArrayList<SilverStripeTest> projectTests;
    
    private TestProgressListener progressListener;

    private IProject fLastProject;

    private boolean projectTestsLoading;

    public TestsViewer() {
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
        
        
        fTestsView = new Composite(fViewStack, SWT.NONE);
        fTestsView.setLayout(new GridLayout(2, false));
        
        ScrolledComposite scrolledComposite = new ScrolledComposite(fTestsView, SWT.BORDER | SWT.V_SCROLL);
        scrolledComposite.setBackground(parent.getBackground());
        GridData gd_scrolledComposite = new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1);
        gd_scrolledComposite.widthHint = 260;
        scrolledComposite.setLayoutData(gd_scrolledComposite);
        
        fTestsList = new Composite(scrolledComposite, SWT.NONE);
        GridLayout gl_fTestsList = new GridLayout(1, false);
        gl_fTestsList.marginHeight = 10;
        gl_fTestsList.marginRight = 5;
        fTestsList.setLayout(gl_fTestsList);
        fTestsList.setLayoutData(new GridData(GridData.FILL_BOTH));
        fTestsList.setBackground(parent.getBackground());
        scrolledComposite.setContent(fTestsList);
        
        Composite rightComp = new Composite(fTestsView, SWT.NONE);
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
            fTestsBrowser = new Browser(rightComp, SWT.NONE);
            fTestsBrowser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
            progressListener=new TestProgressListener(fTestsBrowser, progressBar);
            fTestsBrowser.addProgressListener(progressListener);
        } catch (SWTError error) {
            if(progressListener!=null) {
                fTestsBrowser.removeProgressListener(progressListener);
            }
            
            fTestsBrowser = null;
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
        fErrorView.layout();
        
        
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
                    String ssVersion=CorePreferencesSupport.getInstance().getProjectSpecificPreferencesValue(SilverStripePreferences.SILVERSTRIPE_VERSION, SilverStripeVersion.getDefaultVersion(), project);
                    IConfigurationElement versionDef=SilverStripeVersion.getLanguageDefinition(ssVersion);
                    
                    if(versionDef.getAttribute("supports_tests")!=null && versionDef.getAttribute("supports_tests").toLowerCase().equals("false")) {
                        refreshAction.setEnabled(false);
                        
                        fErrorLabel.setText("The project's version of SilverStripe does not support the tests view");
                        fErrorView.layout();
                        if(fViewStackLayout.topControl!=fErrorView) {
                            fViewStackLayout.topControl=fErrorView;
                            fViewStack.layout();
                        }
                    }else {
                        String siteBase=CorePreferencesSupport.getInstance().getProjectSpecificPreferencesValue("silverstripe_site_base", null, project);
                        if(siteBase!=null && siteBase.isEmpty()==false) {
                            refreshAction.setEnabled(true);
                            fLastProject=project;
                            
                            this.getSite().getPage().addPartListener(viewPartListener);
                        }else {
                            refreshAction.setEnabled(false);
                            
                            fErrorLabel.setText("You have not set the site base for this project, you can set this on the project's preferences for SilverStripe");
                            fErrorView.layout();
                            if(fViewStackLayout.topControl!=fErrorView) {
                                fViewStackLayout.topControl=fErrorView;
                                fViewStack.layout();
                            }
                        }
                    }
                }else {
                    refreshAction.setEnabled(false);
                    
                    fErrorLabel.setText("You must add SilverStripe Support to your project to use this view");
                    fErrorView.layout();
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
        
        if(projectTests!=null && projectTests.isEmpty()==false) {
            for(SilverStripeTest test : projectTests) {
                test.dispose();
            }
            
            projectTests.removeAll(projectTests);
            projectTests=null;
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
     * Refreshes the list of tests based on the current project
     */
    public void refreshTests() {
        if(projectTestsLoading) {
            return;
        }
        
        projectTestsLoading=true;
        
        if(projectTests!=null && projectTests.isEmpty()==false) {
            for(SilverStripeTest test : projectTests) {
                test.dispose();
            }
            
            projectTests.removeAll(projectTests);
            
            fTestsList.setSize(fTestsList.computeSize(fTestsList.getParent().getClientArea().width, SWT.FILL));
            fTestsList.layout(true);
        }else {
            projectTests=new ArrayList<SilverStripeTest>();
        }
        
        String siteBase=CorePreferencesSupport.getInstance().getProjectSpecificPreferencesValue("silverstripe_site_base", null, fLastProject);
        if(siteBase!=null && siteBase.isEmpty()==false) {
            String finalURL=siteBase;
            if(finalURL.substring(finalURL.length()-1).equals("/")==false) {
                finalURL=finalURL.concat("/");
            }
            
            finalURL=finalURL.concat("dev/tests");
            
            IScriptProject project=DLTKCore.create(fLastProject);
            IModelElement[] elements=new IModelElement[] { project };
            IDLTKSearchScope scope=SearchEngine.createSearchScope(elements, IDLTKSearchScope.SYSTEM_LIBRARIES, project.getLanguageToolkit());
            IType[] unitTests=PHPModelAccess.getDefault().findTypes("SapphireTest", MatchRule.EXACT, 0, 0, scope, new NullProgressMonitor());
            unitTests=this.concatTypeArray(unitTests, PHPModelAccess.getDefault().findTypes("FunctionalTest", MatchRule.EXACT, 0, 0, scope, new NullProgressMonitor()));
            
            if(unitTests.length>0) {
                IType sapphireTest=unitTests[0];
                IType functionalTest=unitTests[1];
                
                try {
                    TypeHierarchyLifeCycle lifecycle=new TypeHierarchyLifeCycle();
                    lifecycle.doHierarchyRefresh(sapphireTest, null);
                    
                    SubTypeHierarchyContentProvider provider=new SubTypeHierarchyContentProvider(lifecycle);
                    Object[] decendents=provider.getChildren(sapphireTest);
                    decendents=this.concatObjectArray(decendents, provider.getChildren(functionalTest));
                    
                    
                    if(decendents.length>1) {
                        ArrayList<SSTest> foundTests=new ArrayList<SSTest>();
                        for(Object testObj : decendents) {
                            if(testObj instanceof IType) {
                                IType test=(IType) testObj;
                                
                                //Skip FunctionalTest
                                if(test.getFullyQualifiedName().equals("FunctionalTest")) {
                                    continue;
                                }
                                
                                String testTitle=PHPModelUtils.extractElementName(test.getFullyQualifiedName());
                                String testURL=finalURL.concat("/"+testTitle);
                                
                                foundTests.add(new SSTest(testTitle, testURL));
                            }
                        }
                        
                        
                        //Sort the tests by name
                        Collections.sort(foundTests);
                        
                        
                        //Add to the display
                        for(SSTest test : foundTests) {
                            projectTests.add(new SilverStripeTest(fTestsList, test.getTestTitle(), test.getTestURL()));
                        }
                        
                        
                        //Clean up
                        lifecycle.freeHierarchy();
                        provider.dispose();
                        
                        
                        if(fViewStackLayout.topControl!=fTestsView) {
                            fViewStackLayout.topControl=fTestsView;
                            fViewStack.layout();
                        }
                        
                        fTestsList.setSize(fTestsList.computeSize(fTestsList.getParent().getClientArea().width, SWT.DEFAULT));
                        fTestsList.layout(true, true);
                    }else {
                        fErrorLabel.setText("Could not find any unit tests");
                        fErrorView.layout();
                        if(fViewStackLayout.topControl!=fErrorView) {
                            fViewStackLayout.topControl=fErrorView;
                            fViewStack.layout();
                        }
                    }
                } catch (ModelException e) {
                    fErrorLabel.setText("Error loading unit tests");
                    fErrorView.layout();
                    if(fViewStackLayout.topControl!=fErrorView) {
                        fViewStackLayout.topControl=fErrorView;
                        fViewStack.layout();
                    }
                    
                    e.printStackTrace();
                }
            }
        }
        
        projectTestsLoading=false;
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
     * Runs the test on the webserver
     * @param testURL
     */
    protected void runTest(String testURL) {
        progressBar.setVisible(true);
        progressBar.setMaximum(1);
        progressBar.setSelection(0);
        
        fTestsBrowser.setUrl(testURL+"?flush=1");
    }
    
    /**
     * Handles when the selection changes in the platform
     * @param _project Project the selection belongs to
     */
    protected void handleSelectionChange(IProject _project) {
        try {
            if(_project.hasNature(SilverStripeNature.ID)) {
                String ssVersion=CorePreferencesSupport.getInstance().getProjectSpecificPreferencesValue(SilverStripePreferences.SILVERSTRIPE_VERSION, SilverStripeVersion.getDefaultVersion(), _project.getProject());
                IConfigurationElement versionDef=SilverStripeVersion.getLanguageDefinition(ssVersion);
                
                if(versionDef.getAttribute("supports_tests")!=null && versionDef.getAttribute("supports_tests").toLowerCase().equals("false")) {
                    refreshAction.setEnabled(false);
                    
                    fErrorLabel.setText("The project's version of SilverStripe does not support the tests view");
                    fErrorView.layout();
                    if(fViewStackLayout.topControl!=fErrorView) {
                        fViewStackLayout.topControl=fErrorView;
                        fViewStack.layout();
                    }
                }else {
                    String siteBase=CorePreferencesSupport.getInstance().getProjectSpecificPreferencesValue("silverstripe_site_base", null, _project);
                    if(siteBase!=null && siteBase.isEmpty()==false) {
                        refreshAction.setEnabled(true);
                        
                        if(fLastProject==null || _project.getName().equals(fLastProject.getName())==false) {
                            fTestsBrowser.setUrl("about:blank");
                            fLastProject=_project;
                            
                            if(fViewStackLayout.topControl!=fTestsView) {
                                fViewStackLayout.topControl=fTestsView;
                                fViewStack.layout();
                            }
                            
                            this.refreshTests();
                        }
                    }else {
                        refreshAction.setEnabled(false);
                        
                        fErrorLabel.setText("You have not set the site base for this project, you can set this on the project's preferences for SilverStripe");
                        fErrorView.layout();
                        if(fViewStackLayout.topControl!=fErrorView) {
                            fViewStackLayout.topControl=fErrorView;
                            fViewStack.layout();
                        }
                    }
                }
            }else {
                refreshAction.setEnabled(false);
                
                fErrorLabel.setText("You must add SilverStripe Support to your project to use this view");
                fErrorView.layout();
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
        if(!(element instanceof IAdaptable) || (element instanceof ElementImplForPHP)) {
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
            if(sourcepart.getSite().getId().equals(TestsViewer.ID)) {
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
            if(partRef.getId().equals(TestsViewer.ID)) {
                Display.getCurrent().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        TestsViewer.this.refreshTests();
                    }
                });
                
                TestsViewer.this.getSite().getPage().removePartListener(viewPartListener);
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
        private TestsViewer testsViewer;

        public RefreshAction(TestsViewer _testsViewer) {
            this.testsViewer=_testsViewer;
            
            this.setText("Refresh");
            this.setDescription("Refresh Tests List");
            this.setImageDescriptor(SilverStripePluginImages.IMG_REFRESH);
            this.setEnabled(false);
        }

        @Override 
        public void run() {
            testsViewer.refreshTests();
        }
    }
    
    private class TestProgressListener implements ProgressListener {
        private ProgressBar progress;
        private Browser browser;

        /**
         * @param bar Progress bar to update
         */
        public TestProgressListener(Browser browser, ProgressBar bar) {
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
    
    private class SSTest implements Comparable<SSTest> {
        private String testName;
        private String testURL;
        
        public SSTest(String name, String url) {
            this.testName=name;
            this.testURL=url;
        }
        
        private String getTestTitle() {
            return this.testName;
        }

        private String getTestURL() {
            return this.testURL;
        }
        
        /**
         * Compares two tests by title
         * @return Negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
         */
        @Override
        public int compareTo(SSTest otherTest) {
            return this.getTestTitle().compareTo(otherTest.getTestTitle());
        }
    }
    
    private class SilverStripeTest extends Composite {
        private Label fTestTitleLbl;
        private String testURL;
        private Color defaultBackground;
        private Color hoverBackgroundColor;
        private Listener mouseClickListener;
        private Listener mouseExitListener;
        private Listener mouseEnterListener;

        public SilverStripeTest(Composite parent, String title, String url) {
            super(parent, SWT.NONE);
            
            defaultBackground=parent.getBackground();
            hoverBackgroundColor=Display.getCurrent().getSystemColor(SWT.COLOR_LIST_SELECTION);
            
            
            this.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
            this.setLayout(new GridLayout(1, false));
            this.setBackground(defaultBackground);
            
            testURL=url;
            
            fTestTitleLbl=new Label(this, SWT.WRAP | SWT.NO_BACKGROUND);
            fTestTitleLbl.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
            fTestTitleLbl.setText(title);
            FontDescriptor boldDescriptor=FontDescriptor.createFrom(fTestTitleLbl.getFont()).setStyle(SWT.BOLD);
            Font boldFont = boldDescriptor.createFont(fTestTitleLbl.getDisplay());
            fTestTitleLbl.setFont(boldFont);
            fTestTitleLbl.setBackground(defaultBackground);
            
            //Bind listeners
            mouseClickListener=new Listener() {
                @Override
                public void handleEvent(Event e) {
                    TestsViewer.this.runTest(testURL);
                }
            };
            
            mouseEnterListener=new Listener() {
                @Override
                public void handleEvent(Event e) {
                    SilverStripeTest.this.setBackground(hoverBackgroundColor);
                    fTestTitleLbl.setBackground(hoverBackgroundColor);
                }
            };
            
            mouseExitListener=new Listener() {
                @Override
                public void handleEvent(Event e) {
                    SilverStripeTest.this.setBackground(defaultBackground);
                    fTestTitleLbl.setBackground(defaultBackground);
                }
            };
            
            
            //Bind Listeners
            this.addListener(SWT.MouseUp, mouseClickListener);
            this.addListener(SWT.MouseEnter, mouseEnterListener);
            this.addListener(SWT.MouseExit, mouseExitListener);
            
            fTestTitleLbl.addListener(SWT.MouseUp, mouseClickListener);
            fTestTitleLbl.addListener(SWT.MouseEnter, mouseEnterListener);
            fTestTitleLbl.addListener(SWT.MouseExit, mouseExitListener);
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
