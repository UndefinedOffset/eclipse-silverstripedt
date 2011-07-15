package ca.edchipman.silverstripepdt.wizards;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.DLTKLanguageManager;
import org.eclipse.dltk.core.IBuildpathEntry;
import org.eclipse.dltk.core.IDLTKLanguageToolkit;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.internal.ui.util.CoreUtility;
import org.eclipse.dltk.internal.ui.wizards.BuildpathDetector;
import org.eclipse.dltk.internal.ui.wizards.NewWizardMessages;
import org.eclipse.dltk.ui.wizards.BuildpathsBlock;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.php.internal.core.includepath.IncludePath;
import org.eclipse.php.internal.core.includepath.IncludePathManager;
import org.eclipse.php.internal.core.language.LanguageModelInitializer;
import org.eclipse.php.internal.ui.PHPUiPlugin;
import org.eclipse.php.internal.ui.preferences.PHPTemplateStore;
import org.eclipse.php.internal.ui.viewsupport.ProjectTemplateStore;
import org.eclipse.php.internal.ui.wizards.PHPFileCreationWizard;
import org.eclipse.php.internal.ui.wizards.PHPProjectWizardFirstPage;
import org.eclipse.php.internal.ui.wizards.PHPProjectWizardSecondPage;
import org.eclipse.php.internal.ui.wizards.PHPFileCreationWizard.FileCreator;

import ca.edchipman.silverstripepdt.SilverStripeNature;

@SuppressWarnings("restriction")
public class SilverStripeProjectWizardSecondPage extends PHPProjectWizardSecondPage {
    public SilverStripeProjectWizardSecondPage(PHPProjectWizardFirstPage mainPage) {
        super(mainPage);
    }
    
    protected void updateProject(IProgressMonitor monitor)
            throws CoreException, InterruptedException {
        
        ProjectTemplateStore templateStore=new ProjectTemplateStore(null);
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
            ContextTypeRegistry templateRegistry=PHPUiPlugin.getDefault().getCodeTemplateContextRegistry();

            if (fFirstPage.getDetect()) {
                includepathEntries = setProjectBaseIncludepath();
                if (!getProject().getFile(FILENAME_BUILDPATH).exists()) {

                    IDLTKLanguageToolkit toolkit = DLTKLanguageManager
                            .getLanguageToolkit(getScriptNature());
                    final BuildpathDetector detector = createBuildpathDetector(
                            monitor, toolkit);
                    buildpathEntries = detector.getBuildpath();

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
                new FileCreator().createFile(((Wizard)this.getWizard()), getProject().getName()+"/code", "Page.php", monitor, pageTemplate.string, pageTemplate.offset);
                
                
                //Generate the _config.php file
                Template configTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverstripepdt.SilverStripe.templates.newssproject.config");
                PHPTemplateStore.CompiledTemplate configTemplate=PHPTemplateStore.compileTemplate(templateRegistry, configTemplateToCompile, getProject().getName(), "_config.php");
                new FileCreator().createFile(((Wizard)this.getWizard()), getProject().getName(), "_config.php", monitor, configTemplate.string, configTemplate.offset);
                
                
                // configure the buildpath entries, including the default
                // InterpreterEnvironment library.
                final IPath projectPath = getProject().getFullPath();
                List cpEntries = new ArrayList();
                cpEntries.add(DLTKCore.newSourceEntry(projectPath));

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
                Template[] temp=templateStore.getTemplates("ca.edchipman.silverstripepdt.SilverStripeTemplate");
                Template configTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverstripepdt.SilverStripe.templates.newssmodule.config");
                PHPTemplateStore.CompiledTemplate configTemplate=PHPTemplateStore.compileTemplate(templateRegistry, configTemplateToCompile, getProject().getName(), "_config.php");
                new FileCreator().createFile(((Wizard)this.getWizard()), getProject().getName(), "_config.php", monitor, configTemplate.string, configTemplate.offset);
                
                
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
}
