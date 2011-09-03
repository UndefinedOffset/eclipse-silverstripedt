package ca.edchipman.silverstripepdt.wizards;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.DLTKLanguageManager;
import org.eclipse.dltk.core.IBuildpathEntry;
import org.eclipse.dltk.core.IDLTKLanguageToolkit;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.internal.core.ScriptProject;
import org.eclipse.dltk.internal.ui.util.CoreUtility;
import org.eclipse.dltk.internal.ui.wizards.BuildpathDetector;
import org.eclipse.dltk.internal.ui.wizards.NewWizardMessages;
import org.eclipse.dltk.ui.wizards.BuildpathsBlock;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.php.internal.core.buildpath.BuildPathUtils;
import org.eclipse.php.internal.core.includepath.IncludePath;
import org.eclipse.php.internal.core.includepath.IncludePathManager;
import org.eclipse.php.internal.core.language.LanguageModelInitializer;
import org.eclipse.php.internal.core.preferences.CorePreferencesSupport;
import org.eclipse.php.internal.core.preferences.ProjectPreferencesPropagator;
import org.eclipse.php.internal.core.preferences.CorePreferenceConstants.Keys;
import org.eclipse.php.internal.core.project.ProjectOptions;
import org.eclipse.php.internal.ui.Logger;
import org.eclipse.php.internal.ui.PHPUIMessages;
import org.eclipse.php.internal.ui.editor.PHPStructuredEditor;
import org.eclipse.php.internal.ui.preferences.PHPTemplateStore;
import org.eclipse.php.internal.ui.wizards.PHPProjectWizardFirstPage;
import org.eclipse.php.internal.ui.wizards.PHPProjectWizardSecondPage;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.wst.sse.ui.internal.StructuredTextViewer;
import org.eclipse.core.internal.resources.ProjectPreferences;
import org.osgi.framework.Bundle;

import ca.edchipman.silverstripepdt.SilverStripeNature;
import ca.edchipman.silverstripepdt.SilverStripePDTPlugin;

@SuppressWarnings("restriction")
public class SilverStripeProjectWizardSecondPage extends PHPProjectWizardSecondPage {
    public SilverStripeProjectWizardSecondPage(PHPProjectWizardFirstPage mainPage) {
        super(mainPage);
    }
    
    protected void updateProject(IProgressMonitor monitor)
            throws CoreException, InterruptedException {
        
        TemplateStore templateStore=SilverStripePDTPlugin.getDefault().getTemplateStore();
        try {
            templateStore.load();
        } catch (IOException e) {
            // Ignore the error.
        }
        
        IProject projectHandle = fFirstPage.getProjectHandle();
        IScriptProject create = DLTKCore.create(projectHandle);
        super.init(create, null, false);
        fCurrProjectLocation = getProjectLocationURI();
        SilverStripeProjectWizardFirstPage fFirstPage=(SilverStripeProjectWizardFirstPage) this.fFirstPage;
        
        String ssVersion="SS2.4";
        
        if(((SilverStripeProjectWizardFirstPage)fFirstPage).IsSS23Project()) {
            ssVersion="SS2.3";
        }
        
        Path silverStripeContainer=new Path(SilverStripePDTPlugin.NATURE_ID);
        
        
        
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        try {
            monitor.beginTask(
                    NewWizardMessages.ScriptProjectWizardSecondPage_operation_initialize,
                    70);
            if (monitor.isCanceled()) {
                throw new OperationCanceledException();
            }

            URI realLocation = fCurrProjectLocation;
            if (fCurrProjectLocation == null) { // inside workspace
                try {
                    URI rootLocation = ResourcesPlugin.getWorkspace().getRoot()
                            .getLocationURI();
                    realLocation = new URI(rootLocation.getScheme(), null, Path
                            .fromPortableString(rootLocation.getPath())
                            .append(getProject().getName()).toString(), null);
                } catch (URISyntaxException e) {
                    Assert.isTrue(false, "Can't happen"); //$NON-NLS-1$
                }
            }

            rememberExistingFiles(realLocation);

            createProject(getProject(), fCurrProjectLocation,
                    new SubProgressMonitor(monitor, 20));

            IBuildpathEntry[] buildpathEntries = null;
            IncludePath[] includepathEntries = null;
            ContextTypeRegistry templateRegistry=SilverStripePDTPlugin.getDefault().getTemplateContextRegistry();

            if (fFirstPage.getDetect()) {
                includepathEntries = setProjectBaseIncludepath();
                if (!getProject().getFile(FILENAME_BUILDPATH).exists()) {

                	final IPath projectPath = getProject().getFullPath();
                    List cpEntries = new ArrayList();
                    cpEntries.add(DLTKCore.newSourceEntry(projectPath));
                    cpEntries.add(DLTKCore.newContainerEntry(silverStripeContainer));
                    
                    buildpathEntries = (IBuildpathEntry[]) cpEntries.toArray(new IBuildpathEntry[cpEntries.size()]);
                    includepathEntries = setProjectBaseIncludepath();
                } else {
                    monitor.worked(20);
                }
            } else if (fFirstPage.IsProjectLayout()) {
                // need to create sub-folders and set special build/include
                // paths
                IPath codePath = new Path("code");
                IPath javascriptPath = new Path("javascript");

                if (codePath.segmentCount() > 0) {
                    IFolder folder = getProject().getFolder(codePath);
                    CoreUtility.createFolder(folder, true, true, new SubProgressMonitor(monitor, 10));
                }else {
                    monitor.worked(10);
                }

                if (javascriptPath.segmentCount() > 0) {
                    IFolder folder = getProject().getFolder(javascriptPath);
                    CoreUtility.createFolder(folder, true, true, new SubProgressMonitor(monitor, 10));
                } else {
                    monitor.worked(10);
                }
                
                
                //Generate the Page.php file
                Template pageTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverstripepdt.SilverStripe.templates.newssproject.defaultpage");
                PHPTemplateStore.CompiledTemplate pageTemplate=PHPTemplateStore.compileTemplate(templateRegistry, pageTemplateToCompile, getProject().getName()+"/code", "Page.php");
                new SilverStripeFileCreator().createFile(((Wizard)this.getWizard()), getProject().getName()+"/code", "Page.php", monitor, pageTemplate.string, pageTemplate.offset);
                
                
                //Generate the _config.php file
                Template configTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverstripepdt.SilverStripe.templates.newssproject.config");
                PHPTemplateStore.CompiledTemplate configTemplate=PHPTemplateStore.compileTemplate(templateRegistry, configTemplateToCompile, getProject().getName(), "_config.php");
                new SilverStripeFileCreator().createFile(((Wizard)this.getWizard()), getProject().getName(), "_config.php", monitor, configTemplate.string, configTemplate.offset, true);
                
                
                // configure the buildpath entries, including the default
                // InterpreterEnvironment library.
                final IPath projectPath = getProject().getFullPath();
                List cpEntries = new ArrayList();
                cpEntries.add(DLTKCore.newSourceEntry(projectPath));
                cpEntries.add(DLTKCore.newContainerEntry(silverStripeContainer));
                
                buildpathEntries = (IBuildpathEntry[]) cpEntries.toArray(new IBuildpathEntry[cpEntries.size()]);
                includepathEntries = setProjectBaseIncludepath();
            } else if (fFirstPage.IsModuleLayout()) {
                // need to create sub-folders and set special build/include
                // paths
                IPath codePath = new Path("code");
                IPath cssPath = new Path("css");
                IPath imagesPath = new Path("images");
                IPath javascriptPath = new Path("javascript");
                IPath layoutPath = new Path("templates/Layout");
                IPath includePath = new Path("templates/Includes");
                
                if (codePath.segmentCount() > 0) {
                    IFolder folder = getProject().getFolder(codePath);
                    CoreUtility.createFolder(folder, true, true, new SubProgressMonitor(monitor, 10));
                }else {
                    monitor.worked(10);
                }
                
                if (cssPath.segmentCount() > 0) {
                    IFolder folder = getProject().getFolder(cssPath);
                    CoreUtility.createFolder(folder, true, true, new SubProgressMonitor(monitor, 10));
                } else {
                    monitor.worked(10);
                }
                
                if (imagesPath.segmentCount() > 0) {
                    IFolder folder = getProject().getFolder(imagesPath);
                    CoreUtility.createFolder(folder, true, true, new SubProgressMonitor(monitor, 10));
                } else {
                    monitor.worked(10);
                }
                
                if (javascriptPath.segmentCount() > 0) {
                    IFolder folder = getProject().getFolder(javascriptPath);
                    CoreUtility.createFolder(folder, true, true, new SubProgressMonitor(monitor, 10));
                } else {
                    monitor.worked(10);
                }
                
                if (layoutPath.segmentCount() > 0) {
                    IFolder folder = getProject().getFolder(layoutPath);
                    CoreUtility.createFolder(folder, true, true, new SubProgressMonitor(monitor, 10));
                } else {
                    monitor.worked(10);
                }
                
                if (includePath.segmentCount() > 0) {
                    IFolder folder = getProject().getFolder(includePath);
                    CoreUtility.createFolder(folder, true, true, new SubProgressMonitor(monitor, 10));
                } else {
                    monitor.worked(10);
                }
                
                
                //Generate the _config.php file
                Template configTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverstripepdt.SilverStripe.templates.newssmodule.config");
                PHPTemplateStore.CompiledTemplate configTemplate=PHPTemplateStore.compileTemplate(templateRegistry, configTemplateToCompile, getProject().getName(), "_config.php");
                new SilverStripeFileCreator().createFile(((Wizard)this.getWizard()), getProject().getName(), "_config.php", monitor, configTemplate.string, configTemplate.offset, true);
                
                
                // configure the buildpath entries, including the default
                // InterpreterEnvironment library.
                final IPath projectPath = getProject().getFullPath();
                List cpEntries = new ArrayList();
                cpEntries.add(DLTKCore.newSourceEntry(projectPath));
                cpEntries.add(DLTKCore.newContainerEntry(silverStripeContainer));

                buildpathEntries = (IBuildpathEntry[]) cpEntries.toArray(new IBuildpathEntry[cpEntries.size()]);
                includepathEntries = setProjectBaseIncludepath();
            } else if (fFirstPage.IsThemeLayout()) {
                // need to create sub-folders and set special build/include
                // paths
                IPath cssPath = new Path("css");
                IPath treeIconsPath = new Path("images/treeicons");
                IPath javascriptPath = new Path("javascript");
                IPath layoutPath = new Path("templates/Layout");
                IPath includePath = new Path("templates/Includes");
                
                if (cssPath.segmentCount() > 0) {
                    IFolder folder = getProject().getFolder(cssPath);
                    CoreUtility.createFolder(folder, true, true, new SubProgressMonitor(monitor, 10));
                } else {
                    monitor.worked(10);
                }
                
                if (treeIconsPath.segmentCount() > 0) {
                    IFolder folder = getProject().getFolder(treeIconsPath);
                    CoreUtility.createFolder(folder, true, true, new SubProgressMonitor(monitor, 10));
                } else {
                    monitor.worked(10);
                }
                
                if (javascriptPath.segmentCount() > 0) {
                    IFolder folder = getProject().getFolder(javascriptPath);
                    CoreUtility.createFolder(folder, true, true, new SubProgressMonitor(monitor, 10));
                } else {
                    monitor.worked(10);
                }
                
                if (layoutPath.segmentCount() > 0) {
                    IFolder folder = getProject().getFolder(layoutPath);
                    CoreUtility.createFolder(folder, true, true, new SubProgressMonitor(monitor, 10));
                } else {
                    monitor.worked(10);
                }
                
                if (includePath.segmentCount() > 0) {
                    IFolder folder = getProject().getFolder(includePath);
                    CoreUtility.createFolder(folder, true, true, new SubProgressMonitor(monitor, 10));
                } else {
                    monitor.worked(10);
                }
                
                
                //Generate the editor.css file
                Template editorTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverstripepdt.SilverStripe.templates.newsstheme.editor");
                PHPTemplateStore.CompiledTemplate editorTemplate=PHPTemplateStore.compileTemplate(templateRegistry, editorTemplateToCompile, getProject().getName()+"/css", "editor.css");
                new SilverStripeFileCreator().createFile(((Wizard)this.getWizard()), getProject().getName()+"/css", "editor.css", monitor, editorTemplate.string, editorTemplate.offset);

                //Generate the form.css file
                Template formTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverstripepdt.SilverStripe.templates.newsstheme.form");
                PHPTemplateStore.CompiledTemplate formTemplate=PHPTemplateStore.compileTemplate(templateRegistry, formTemplateToCompile, getProject().getName()+"/css", "form.css");
                new SilverStripeFileCreator().createFile(((Wizard)this.getWizard()), getProject().getName()+"/css", "form.css", monitor, formTemplate.string, formTemplate.offset);
                
                //Generate the layout.css file
                Template layoutTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverstripepdt.SilverStripe.templates.newsstheme.layout");
                PHPTemplateStore.CompiledTemplate layoutTemplate=PHPTemplateStore.compileTemplate(templateRegistry, layoutTemplateToCompile, getProject().getName()+"/css", "layout.css");
                new SilverStripeFileCreator().createFile(((Wizard)this.getWizard()), getProject().getName()+"/css", "layout.css", monitor, layoutTemplate.string, layoutTemplate.offset);
                
                //Generate the typography.css file
                Template typographyTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverstripepdt.SilverStripe.templates.newsstheme.typography");
                PHPTemplateStore.CompiledTemplate typographyTemplate=PHPTemplateStore.compileTemplate(templateRegistry, typographyTemplateToCompile, getProject().getName()+"/css", "typography.css");
                new SilverStripeFileCreator().createFile(((Wizard)this.getWizard()), getProject().getName()+"/css", "typography.css", monitor, typographyTemplate.string, typographyTemplate.offset);
                
                //Generate the top level Page.ss file
                Template tlPageTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverstripepdt.SilverStripe.templates.newss.toplevel");
                PHPTemplateStore.CompiledTemplate tlPageTemplate=PHPTemplateStore.compileTemplate(templateRegistry, tlPageTemplateToCompile, getProject().getName()+"/templates", "Page.ss");
                new SilverStripeFileCreator().createFile(((Wizard)this.getWizard()), getProject().getName()+"/templates", "Page.ss", monitor, tlPageTemplate.string, tlPageTemplate.offset, true);
                
                //Generate the Page.ss file
                Template pageTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverstripepdt.SilverStripe.templates.newss.template");
                PHPTemplateStore.CompiledTemplate pageTemplate=PHPTemplateStore.compileTemplate(templateRegistry, pageTemplateToCompile, getProject().getName()+"/templates/Layout", "Page.ss");
                new SilverStripeFileCreator().createFile(((Wizard)this.getWizard()), getProject().getName()+"/templates/Layout", "Page.ss", monitor, pageTemplate.string, pageTemplate.offset);
                
                //Generate the Page.ss file
                Template pageResultsTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverstripepdt.SilverStripe.templates.newsstheme.pageresults");
                PHPTemplateStore.CompiledTemplate pageResultsTemplate=PHPTemplateStore.compileTemplate(templateRegistry, pageResultsTemplateToCompile, getProject().getName()+"/templates/Layout", "Page_results.ss");
                new SilverStripeFileCreator().createFile(((Wizard)this.getWizard()), getProject().getName()+"/templates/Layout", "Page_results.ss", monitor, pageResultsTemplate.string, pageResultsTemplate.offset);
                
                
                //Copy the Home Icon
                try {
                    Bundle bundle = Platform.getBundle(SilverStripePDTPlugin.PLUGIN_ID);
                    InputStream stream;
                    
                    stream = FileLocator.openStream(bundle, new Path("resources/theme/images/treeicons/home-file.gif"), false);
                    
                    IFile file = getProject().getFile("images/treeicons/home-file.gif");
                    file.create(stream, true, null);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                
                //Copy the News Icon
                try {
                    Bundle bundle = Platform.getBundle(SilverStripePDTPlugin.PLUGIN_ID);
                    InputStream stream;
                    
                    stream = FileLocator.openStream(bundle, new Path("resources/theme/images/treeicons/news-file.gif"), false);
                    
                    IFile file = getProject().getFile("images/treeicons/news-file.gif");
                    file.create(stream, true, null);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
                
                // configure the buildpath entries, including the default
                // InterpreterEnvironment library.
                final IPath projectPath = getProject().getFullPath();
                List cpEntries = new ArrayList();
                cpEntries.add(DLTKCore.newSourceEntry(projectPath));

                buildpathEntries = (IBuildpathEntry[]) cpEntries.toArray(new IBuildpathEntry[cpEntries.size()]);
                includepathEntries = setProjectBaseIncludepath();
            } else {
                //Shouldn't happen
                throw new OperationCanceledException();
            }
            
            if (monitor.isCanceled()) {
                throw new OperationCanceledException();
            }

            init(DLTKCore.create(getProject()), buildpathEntries, false);

            // setting PHP4/5 and ASP-Tags :
            setPhpLangOptions();
            
            //Disable asp tags
            if(CorePreferencesSupport.getInstance().getPreferencesValue(Keys.EDITOR_USE_ASP_TAGS, null, null)=="true") {
                ProjectOptions.setSupportingAspTags(false, getProject());
            }
            
            //Disable short tags
            if(CorePreferencesSupport.getInstance().getPreferencesValue(Keys.EDITOR_USE_SHORT_TAGS, null, null)=="true") {
                CorePreferencesSupport.getInstance().setProjectSpecificPreferencesValue(Keys.EDITOR_USE_SHORT_TAGS, "false", getProject());
            }
            

            configureScriptProject(new SubProgressMonitor(monitor, 30));

            // checking and adding JS nature,libs, include path if needed
            if (fFirstPage.shouldSupportJavaScript()) {
                addJavaScriptNature(monitor);
            }

            // adding build paths, and language-Container:
            getScriptProject().setRawBuildpath(buildpathEntries,
                    new NullProgressMonitor());
            LanguageModelInitializer.enableLanguageModelFor(getScriptProject());

            // init, and adding include paths:
            getBuildPathsBlock().init(getScriptProject(),
                    new IBuildpathEntry[] {});
            IncludePathManager.getInstance().setIncludePath(getProject(),
                    includepathEntries);
            
            
            CorePreferencesSupport.getInstance().setProjectSpecificPreferencesValue("silverstripe_version", ssVersion, getProject());
        } finally {
            monitor.done();
        }
    }

    protected String getScriptNature() {
        return SilverStripeNature.ID;
    }
    
    protected String getPHPScriptNature() {
        return super.getScriptNature();
    }
    
    public void configureScriptProject(IProgressMonitor monitor)
            throws CoreException, InterruptedException {
        String scriptNature = getScriptNature();
        String phpScriptNature = getPHPScriptNature();
        setScriptNature(monitor, scriptNature, phpScriptNature);
    }
    
    /**
    * @param monitor
    * @param scriptNature
    * @throws CoreException
    * @throws InterruptedException
    */
    @SuppressWarnings("deprecation")
    protected void setScriptNature(IProgressMonitor monitor, String scriptNature, String phpScriptNature)
            throws CoreException, InterruptedException {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }

        int nSteps = 6;
        monitor.beginTask(
                NewWizardMessages.ScriptCapabilityConfigurationPage_op_desc_Script,
                nSteps);

        try {
            IProject project = getProject();
            BuildpathsBlock.addScriptNature(project, new SubProgressMonitor(
                    monitor, 1), scriptNature);
            BuildpathsBlock.addScriptNature(project, new SubProgressMonitor(
                    monitor, 1), phpScriptNature);
        } catch (OperationCanceledException e) {
            throw new InterruptedException();
        } finally {
            monitor.done();
        }
    }
    
    private IProject getProject() {
        IScriptProject scriptProject = getScriptProject();
        if (scriptProject != null) {
            return scriptProject.getProject();
        }
        
        return null;
    }
    
    /**
     * Creates a folder resource handle for the folder with the given name.
     * The folder handle is created relative to the container specified during
     * object creation.
     *
     * @param folderName the name of the folder resource to create a handle for
     * @return the new folder resource handle
     */
    private IFolder createFolderHandle(IPath folderPath) {
        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        IFolder folderHandle = workspaceRoot.getFolder(folderPath);
        
        return folderHandle;
    }
    
    /**
     * A static nested class for the creation of a new PHP File.
     * 
     * @author yaronm
     * 
     */
    public static class SilverStripeFileCreator {

        /**
         * The worker method. It will find the container, create the file if
         * missing or just replace its contents, and open the editor on the
         * newly created file. This method does not take an editor id to use
         * when opening the file.
         * 
         * @param wizard
         * @param containerName
         * @param fileName
         * @param monitor
         * @param contents
         * @throws CoreException
         * @see {@link #createFile(Wizard, String, String, IProgressMonitor, String, String)}
         */
        public IFile createFile(Wizard wizard, String containerName, String fileName, IProgressMonitor monitor, String contents, boolean openFileOnCreate) throws CoreException {
            return createFile(wizard, containerName, fileName, monitor, contents, 0, null, openFileOnCreate);
        }

        public IFile createFile(Wizard wizard, String containerName, String fileName, IProgressMonitor monitor, String contents, int offset) throws CoreException {
            return createFile(wizard, containerName, fileName, monitor, contents, offset, null, false);
        }

        public IFile createFile(Wizard wizard, String containerName, String fileName, IProgressMonitor monitor, String contents, int offset, boolean openFileOnCreate) throws CoreException {
            return createFile(wizard, containerName, fileName, monitor, contents, offset, null, openFileOnCreate);
        }

        /**
         * The worker method. It will find the container, create the file if
         * missing or just replace its contents, and open the editor on the
         * newly created file.
         * 
         * @param wizard
         * @param containerName
         * @param fileName
         * @param monitor
         * @param contents
         * @param editorID
         *            An optional editor ID to use when opening the file (can be
         *            null).
         * @throws CoreException
         */
        public IFile createFile(Wizard wizard, String containerName, String fileName, IProgressMonitor monitor, String contents, final int offset, final String editorID, final boolean openFileOnCreate) throws CoreException {
            // create a sample file
            monitor.beginTask(
                    NLS.bind(PHPUIMessages.newPhpFile_create, fileName), 2);
            IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
            IResource resource = root.findMember(new Path(containerName));
            if (!resource.exists() || !(resource instanceof IContainer)) {
                throwCoreException(PHPUIMessages.PHPFileCreationWizard_1
                        + containerName + PHPUIMessages.PHPFileCreationWizard_2); //$NON-NLS-1$ //$NON-NLS-2$
            }
            IContainer container = (IContainer) resource;
            final IFile file = container.getFile(new Path(fileName));

            // adopt project's/workspace's line delimiter (separator)
            String lineSeparator = Platform.getPreferencesService().getString(
                    Platform.PI_RUNTIME,
                    Platform.PREF_LINE_SEPARATOR,
                    null,
                    new IScopeContext[] { new ProjectScope(container
                            .getProject()) });
            if (lineSeparator == null)
                lineSeparator = Platform.getPreferencesService().getString(
                        Platform.PI_RUNTIME, Platform.PREF_LINE_SEPARATOR,
                        null, new IScopeContext[] { new InstanceScope() });
            if (lineSeparator == null)
                lineSeparator = System
                        .getProperty(Platform.PREF_LINE_SEPARATOR);
            if (contents != null) {
                contents = contents.replaceAll("(\n\r?|\r\n?)", lineSeparator); //$NON-NLS-1$
            }

            try {
                InputStream stream = openContentStream(contents);
                if (file.exists()) {
                    file.setContents(stream, true, true, monitor);
                } else {
                    file.create(stream, true, monitor);
                }
                stream.close();
            } catch (IOException e) {
                Logger.logException(e);
                return null;
            }
            
            if(openFileOnCreate) {
                monitor.worked(1);
                monitor.setTaskName(NLS.bind(PHPUIMessages.newPhpFile_openning,
                        fileName));
                wizard.getShell().getDisplay().asyncExec(new Runnable() {
                    public void run() {
                        IWorkbenchPage page = PlatformUI.getWorkbench()
                                .getActiveWorkbenchWindow().getActivePage();
                        try {
                            normalizeFile(file);
                            IEditorPart editor;
                            if (editorID == null) {
                                editor = IDE.openEditor(page, file, true);
                            } else {
                                editor = IDE.openEditor(page, file, editorID, true);
                            }
                            if (editor instanceof PHPStructuredEditor) {
                                StructuredTextViewer textViewer=((PHPStructuredEditor) editor).getTextViewer();
                                textViewer.setSelectedRange(offset, 0);
                            }
                        } catch (PartInitException e) {}
                    }
                });
                monitor.worked(1);
            }
            
            return file;
        }

        /**
         * We will initialize file contents with a sample text.
         */
        private static InputStream openContentStream(String contents) {
            if (contents == null) {
                contents = ""; //$NON-NLS-1$
            }

            return new ByteArrayInputStream(contents.getBytes());
        }

        private static void throwCoreException(String message)
                throws CoreException {
            IStatus status = new Status(IStatus.ERROR,
                    PHPUIMessages.PHPFileCreationWizard_4, IStatus.OK, message,
                    null); //$NON-NLS-1$
            throw new CoreException(status);
        }

        /**
         * @param file
         */
        protected void normalizeFile(IFile file) {
        }
    }
}
