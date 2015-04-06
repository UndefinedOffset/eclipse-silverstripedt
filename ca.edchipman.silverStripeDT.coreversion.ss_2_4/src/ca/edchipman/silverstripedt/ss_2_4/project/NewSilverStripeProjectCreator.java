package ca.edchipman.silverstripedt.ss_2_4.project;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.dltk.internal.ui.util.CoreUtility;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.php.internal.ui.preferences.PHPTemplateStore;
import org.osgi.framework.Bundle;

import ca.edchipman.silverstripepdt.SilverStripePDTPlugin;
import ca.edchipman.silverstripepdt.SilverStripeVersion;
import ca.edchipman.silverstripepdt.versioninterfaces.ISilverStripeNewProjectCreator;
import ca.edchipman.silverstripepdt.wizards.SilverStripeProjectWizardSecondPage.SilverStripeFileCreator;

@SuppressWarnings("restriction")
public class NewSilverStripeProjectCreator implements ISilverStripeNewProjectCreator {
    /**
     * Performs the SilverStripe version specific tasks when creating new project layout project
     * @param project Destination project
     * @param monitor Monitor to update when creating the layout
     * @param templateRegistry Template registry to look through
     * @param isFrameworkLayout If the project is a framework only project this is set to true
     * @throws CoreException 
     */
    public void createProjectLayout(Wizard wizard, IProject project, IProgressMonitor monitor, ContextTypeRegistry templateRegistry, TemplateStore templateStore, boolean isFrameworkLayout) throws CoreException {
        //Generate the Page.php file
        Template pageTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverstripepdt.SilverStripe.templates.newssproject.defaultpage");
        PHPTemplateStore.CompiledTemplate pageTemplate=PHPTemplateStore.compileTemplate(templateRegistry, pageTemplateToCompile, project.getName()+"/code", "Page.php");
        new SilverStripeFileCreator().createFile(wizard, project.getName()+"/code", "Page.php", monitor, pageTemplate.string, pageTemplate.offset);
        
        
        //Generate the _config.php file
        Template configTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverstripepdt.SilverStripe.templates.newssproject.config");
        PHPTemplateStore.CompiledTemplate configTemplate=PHPTemplateStore.compileTemplate(templateRegistry, configTemplateToCompile, project.getName(), "_config.php");
        new SilverStripeFileCreator().createFile(wizard, project.getName(), "_config.php", monitor, configTemplate.string, configTemplate.offset, true);
    }

    /**
     * Performs the SilverStripe version specific tasks when creating new module layout project
     * @param project Destination project
     * @param monitor Monitor to update when creating the layout
     * @param templateRegistry Template registry to look through
     * @param isFrameworkLayout If the project is a framework only project this is set to true
     * @throws CoreException 
     */
    public void createModuleLayout(Wizard wizard, IProject project, IProgressMonitor monitor, ContextTypeRegistry templateRegistry, TemplateStore templateStore, boolean isFrameworkLayout) throws CoreException {
        //Do nothing
    }

    /**
     * Performs the SilverStripe version specific tasks when creating new theme layout project
     * @param project Destination project
     * @param monitor Monitor to update when creating the layout
     * @param templateRegistry Template registry to look through
     * @param isFrameworkLayout If the project is a framework only project this is set to true
     * @throws CoreException 
     */
    public void createThemeLayout(Wizard wizard, IProject project, IProgressMonitor monitor, ContextTypeRegistry templateRegistry, TemplateStore templateStore, boolean isFrameworkLayout) throws CoreException {
        //Generate the editor.css file
        Template editorTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverstripepdt.SilverStripe.templates.newsstheme.editor");
        PHPTemplateStore.CompiledTemplate editorTemplate=PHPTemplateStore.compileTemplate(templateRegistry, editorTemplateToCompile, project.getName()+"/css", "editor.css");
        new SilverStripeFileCreator().createFile(wizard, project.getName()+"/css", "editor.css", monitor, editorTemplate.string, editorTemplate.offset);
        
        
        //Generate the form.css file
        Template formTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverstripepdt.SilverStripe.templates.newsstheme.form");
        PHPTemplateStore.CompiledTemplate formTemplate=PHPTemplateStore.compileTemplate(templateRegistry, formTemplateToCompile, project.getName()+"/css", "form.css");
        new SilverStripeFileCreator().createFile(wizard, project.getName()+"/css", "form.css", monitor, formTemplate.string, formTemplate.offset);
        
        
        //Generate the layout.css file
        Template layoutTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverstripepdt.SilverStripe.templates.newsstheme.layout");
        PHPTemplateStore.CompiledTemplate layoutTemplate=PHPTemplateStore.compileTemplate(templateRegistry, layoutTemplateToCompile, project.getName()+"/css", "layout.css");
        new SilverStripeFileCreator().createFile(wizard, project.getName()+"/css", "layout.css", monitor, layoutTemplate.string, layoutTemplate.offset);
        
        
        //Generate the typography.css file
        Template typographyTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverstripepdt.SilverStripe.templates.newsstheme.typography");
        PHPTemplateStore.CompiledTemplate typographyTemplate=PHPTemplateStore.compileTemplate(templateRegistry, typographyTemplateToCompile, project.getName()+"/css", "typography.css");
        new SilverStripeFileCreator().createFile(wizard, project.getName()+"/css", "typography.css", monitor, typographyTemplate.string, typographyTemplate.offset);
        
        
        //Generate the top level Page.ss file
        Template tlPageTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverstripepdt.SilverStripe.templates.newss.toplevel");
        PHPTemplateStore.CompiledTemplate tlPageTemplate=PHPTemplateStore.compileTemplate(templateRegistry, tlPageTemplateToCompile, project.getName()+"/templates", "Page.ss");
        new SilverStripeFileCreator().createFile(wizard, project.getName()+"/templates", "Page.ss", monitor, tlPageTemplate.string, tlPageTemplate.offset);
        
        
        //Generate the Page.ss file
        Template pageTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverstripepdt.SilverStripe.templates.newss.template");
        PHPTemplateStore.CompiledTemplate pageTemplate=PHPTemplateStore.compileTemplate(templateRegistry, pageTemplateToCompile, project.getName()+"/templates/Layout", "Page.ss");
        new SilverStripeFileCreator().createFile(wizard, project.getName()+"/templates/Layout", "Page.ss", monitor, pageTemplate.string, pageTemplate.offset);
        
        
        //Generate the Page_results.ss file
        Template pageResultsTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverstripepdt.SilverStripe.templates.newsstheme.pageresults");
        PHPTemplateStore.CompiledTemplate pageResultsTemplate=PHPTemplateStore.compileTemplate(templateRegistry, pageResultsTemplateToCompile, project.getName()+"/templates/Layout", "Page_results.ss");
        new SilverStripeFileCreator().createFile(wizard, project.getName()+"/templates/Layout", "Page_results.ss", monitor, pageResultsTemplate.string, pageResultsTemplate.offset);
        
        
        //Generate the Navigation.ss file
        Template navigationTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverstripepdt.SilverStripe.templates.newsstheme.navigation");
        PHPTemplateStore.CompiledTemplate navigationTemplate=PHPTemplateStore.compileTemplate(templateRegistry, navigationTemplateToCompile, project.getName()+"/templates/Includes", "Navigation.ss");
        new SilverStripeFileCreator().createFile(wizard, project.getName()+"/templates/Includes", "Navigation.ss", monitor, navigationTemplate.string, navigationTemplate.offset);
        
        
        IPath treeIconsPath = new Path("images/treeicons");
        if (treeIconsPath.segmentCount() > 0) {
            IFolder folder = project.getFolder(treeIconsPath);
            CoreUtility.createFolder(folder, true, true, new SubProgressMonitor(monitor, 10));
        } else {
            monitor.worked(10);
        }
        
        //Copy the Home Icon
        try {
            Bundle bundle = Platform.getBundle(SilverStripePDTPlugin.PLUGIN_ID);
            InputStream stream;
            
            stream = FileLocator.openStream(bundle, new Path("resources/theme/images/treeicons/home-file.gif"), false);
            
            IFile file = project.getFile("images/treeicons/home-file.gif");
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
            
            IFile file = project.getFile("images/treeicons/news-file.gif");
            file.create(stream, true, null);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
