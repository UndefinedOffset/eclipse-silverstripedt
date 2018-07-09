package ca.edchipman.silverstripedt.ss_4.project;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.dltk.internal.ui.util.CoreUtility;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.php.internal.ui.preferences.PHPTemplateStore;

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
        if(isFrameworkLayout==false) {
            Template pageTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverStripeDT.coreversion.ss_4.newproject.defaultpage");
            PHPTemplateStore.CompiledTemplate pageTemplate=PHPTemplateStore.compileTemplate(templateRegistry, pageTemplateToCompile, project.getName()+"/code", "Page.php");
            new SilverStripeFileCreator().createFile(wizard, project.getName()+"/code", "Page.php", monitor, pageTemplate.string, pageTemplate.offset);
            
            //Generate the PageController.php file
            Template pageControllerTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverStripeDT.coreversion.ss_4.newproject.defaultpagecontroller");
            PHPTemplateStore.CompiledTemplate pageControllerTemplate=PHPTemplateStore.compileTemplate(templateRegistry, pageControllerTemplateToCompile, project.getName()+"/code/control", "PageController.php");
            new SilverStripeFileCreator().createFile(wizard, project.getName()+"/code/control", "PageController.php", monitor, pageControllerTemplate.string, pageControllerTemplate.offset);
        }
        
        
        //Create the config folder
        IPath ymlConfigPath = new Path("_config");
        if (ymlConfigPath.segmentCount() > 0) {
            IFolder folder=project.getFolder(ymlConfigPath);
            CoreUtility.createFolder(folder, true, true, new SubProgressMonitor(monitor, 10));
        } else {
            monitor.worked(10);
        }
        
        
        //Create config.yml
        Template ymlConfigTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverStripeDT.coreversion.ss_4.newproject.ymlconfig");
        PHPTemplateStore.CompiledTemplate ymlConfigTemplate=PHPTemplateStore.compileTemplate(templateRegistry, ymlConfigTemplateToCompile, project.getName()+"/_config", "config.yml");
        new SilverStripeFileCreator().createFile(wizard, project.getName()+"/_config", "config.yml", monitor, ymlConfigTemplate.string, ymlConfigTemplate.offset, true);
        
        
        //Generate the _config.php file
        Template configTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverStripeDT.coreversion.ss_4.newproject.config");
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
        Template editorTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverStripeDT.coreversion.ss_4.templates.newtheme.editor");
        PHPTemplateStore.CompiledTemplate editorTemplate=PHPTemplateStore.compileTemplate(templateRegistry, editorTemplateToCompile, project.getName()+"/css", "editor.css");
        new SilverStripeFileCreator().createFile(wizard, project.getName()+"/css", "editor.css", monitor, editorTemplate.string, editorTemplate.offset);
        
        //Generate the form.css file
        Template formTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverStripeDT.coreversion.ss_4.templates.newtheme.form");
        PHPTemplateStore.CompiledTemplate formTemplate=PHPTemplateStore.compileTemplate(templateRegistry, formTemplateToCompile, project.getName()+"/css", "form.css");
        new SilverStripeFileCreator().createFile(wizard, project.getName()+"/css", "form.css", monitor, formTemplate.string, formTemplate.offset);
        
        //Generate the layout.css file
        Template layoutTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverStripeDT.coreversion.ss_4.templates.newtheme.layout");
        PHPTemplateStore.CompiledTemplate layoutTemplate=PHPTemplateStore.compileTemplate(templateRegistry, layoutTemplateToCompile, project.getName()+"/css", "layout.css");
        new SilverStripeFileCreator().createFile(wizard, project.getName()+"/css", "layout.css", monitor, layoutTemplate.string, layoutTemplate.offset);
        
        //Generate the typography.css file
        Template typographyTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverStripeDT.coreversion.ss_4.templates.newtheme.typography");
        PHPTemplateStore.CompiledTemplate typographyTemplate=PHPTemplateStore.compileTemplate(templateRegistry, typographyTemplateToCompile, project.getName()+"/css", "typography.css");
        new SilverStripeFileCreator().createFile(wizard, project.getName()+"/css", "typography.css", monitor, typographyTemplate.string, typographyTemplate.offset);
        
        //Generate the menu.css file
        Template menuTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverStripeDT.coreversion.ss_4.templates.newtheme.menu");
        PHPTemplateStore.CompiledTemplate menuTemplate=PHPTemplateStore.compileTemplate(templateRegistry, menuTemplateToCompile, project.getName()+"/css", "menu.css");
        new SilverStripeFileCreator().createFile(wizard, project.getName()+"/css", "menu.css", monitor, menuTemplate.string, menuTemplate.offset);
        
        //Generate the responsive.css file
        Template responsiveTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverStripeDT.coreversion.ss_4.templates.newtheme.responsive");
        PHPTemplateStore.CompiledTemplate responsiveTemplate=PHPTemplateStore.compileTemplate(templateRegistry, responsiveTemplateToCompile, project.getName()+"/css", "responsive.css");
        new SilverStripeFileCreator().createFile(wizard, project.getName()+"/css", "responsive.css", monitor, responsiveTemplate.string, responsiveTemplate.offset);
        

        //Generate the _base.scss file
        Template baseSRCTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverStripeDT.coreversion.ss_4.templates.newtheme.base.src");
        PHPTemplateStore.CompiledTemplate baseSRCTemplate=PHPTemplateStore.compileTemplate(templateRegistry, baseSRCTemplateToCompile, project.getName()+"/scss", "_base.scss");
        new SilverStripeFileCreator().createFile(wizard, project.getName()+"/scss", "_base.scss", monitor, baseSRCTemplate.string, baseSRCTemplate.offset);
        
        //Generate the editor.scss file
        Template editorSRCTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverStripeDT.coreversion.ss_4.templates.newtheme.editor.src");
        PHPTemplateStore.CompiledTemplate editorSRCTemplate=PHPTemplateStore.compileTemplate(templateRegistry, editorSRCTemplateToCompile, project.getName()+"/scss", "editor.scss");
        new SilverStripeFileCreator().createFile(wizard, project.getName()+"/scss", "editor.scss", monitor, editorSRCTemplate.string, editorSRCTemplate.offset);
        
        //Generate the form.scss file
        Template formSRCTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverStripeDT.coreversion.ss_4.templates.newtheme.form.src");
        PHPTemplateStore.CompiledTemplate formSRCTemplate=PHPTemplateStore.compileTemplate(templateRegistry, formSRCTemplateToCompile, project.getName()+"/scss", "form.css");
        new SilverStripeFileCreator().createFile(wizard, project.getName()+"/scss", "form.scss", monitor, formSRCTemplate.string, formSRCTemplate.offset);
        
        //Generate the layout.scss file
        Template layoutSRCTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverStripeDT.coreversion.ss_4.templates.newtheme.layout.src");
        PHPTemplateStore.CompiledTemplate layoutSRCTemplate=PHPTemplateStore.compileTemplate(templateRegistry, layoutSRCTemplateToCompile, project.getName()+"/scss", "layout.scss");
        new SilverStripeFileCreator().createFile(wizard, project.getName()+"/scss", "layout.scss", monitor, layoutSRCTemplate.string, layoutSRCTemplate.offset);
        
        //Generate the typography.scss file
        Template typographySRCTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverStripeDT.coreversion.ss_4.templates.newtheme.typography.src");
        PHPTemplateStore.CompiledTemplate typographySRCTemplate=PHPTemplateStore.compileTemplate(templateRegistry, typographySRCTemplateToCompile, project.getName()+"/scss", "typography.scss");
        new SilverStripeFileCreator().createFile(wizard, project.getName()+"/scss", "typography.scss", monitor, typographySRCTemplate.string, typographySRCTemplate.offset);
        
        //Generate the menu.scss file
        Template menuSRCTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverStripeDT.coreversion.ss_4.templates.newtheme.menu");
        PHPTemplateStore.CompiledTemplate menuSRCTemplate=PHPTemplateStore.compileTemplate(templateRegistry, menuSRCTemplateToCompile, project.getName()+"/scss", "menu.scss");
        new SilverStripeFileCreator().createFile(wizard, project.getName()+"/scss", "menu.scss", monitor, menuSRCTemplate.string, menuSRCTemplate.offset);
        
        //Generate the responsive.scss file
        Template responsiveSRCTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverStripeDT.coreversion.ss_4.templates.newtheme.responsive");
        PHPTemplateStore.CompiledTemplate responsiveSRCTemplate=PHPTemplateStore.compileTemplate(templateRegistry, responsiveSRCTemplateToCompile, project.getName()+"/scss", "responsive.scss");
        new SilverStripeFileCreator().createFile(wizard, project.getName()+"/scss", "responsive.scss", monitor, responsiveSRCTemplate.string, responsiveSRCTemplate.offset);
        
        
        //Generate the Page.js file
        Template pageScriptTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverStripeDT.coreversion.ss_4.templates.newtheme.page_script");
        PHPTemplateStore.CompiledTemplate pageScriptTemplate=PHPTemplateStore.compileTemplate(templateRegistry, pageScriptTemplateToCompile, project.getName()+"/javascript", "Page.js");
        new SilverStripeFileCreator().createFile(wizard, project.getName()+"/javascript", "Page.js", monitor, pageScriptTemplate.string, pageScriptTemplate.offset);
        
        
        //Generate the Page.js file
        Template compassConfigTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverStripeDT.coreversion.ss_4.templates.newtheme.config_rb");
        PHPTemplateStore.CompiledTemplate compassConfigTemplate=PHPTemplateStore.compileTemplate(templateRegistry, compassConfigTemplateToCompile, project.getName(), "config.rb");
        new SilverStripeFileCreator().createFile(wizard, project.getName(), "config.rb", monitor, compassConfigTemplate.string, compassConfigTemplate.offset);
        
        
        //Generate the top level Page.ss file
        Template tlPageTemplateToCompile;
        if(isFrameworkLayout) {
            tlPageTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverStripeDT.coreversion.ss_4.templates.newtheme.framework.toplevel");
        } else {
            tlPageTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverStripeDT.coreversion.ss_4.templates.newtheme.toplevel");
        }
        
        PHPTemplateStore.CompiledTemplate tlPageTemplate=PHPTemplateStore.compileTemplate(templateRegistry, tlPageTemplateToCompile, project.getName()+"/templates", "Page.ss");
        if(isFrameworkLayout) {
            new SilverStripeFileCreator().createFile(wizard, project.getName()+"/templates", "Controller.ss", monitor, tlPageTemplate.string, tlPageTemplate.offset);
        }else {
            new SilverStripeFileCreator().createFile(wizard, project.getName()+"/templates", "Page.ss", monitor, tlPageTemplate.string, tlPageTemplate.offset);
        }
        
        
        //Generate the Page.ss file
        Template pageTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverStripeDT.coreversion.ss_4.templates.newtheme.layout");
        PHPTemplateStore.CompiledTemplate pageTemplate=PHPTemplateStore.compileTemplate(templateRegistry, pageTemplateToCompile, project.getName()+"/templates/Layout", "Page.ss");
        
        
        if(isFrameworkLayout) {
            new SilverStripeFileCreator().createFile(wizard, project.getName()+"/templates/Layout", "Controller.ss", monitor, pageTemplate.string, pageTemplate.offset);
        }else {
            new SilverStripeFileCreator().createFile(wizard, project.getName()+"/templates/Layout", "Page.ss", monitor, pageTemplate.string, pageTemplate.offset);
        }
        
        
        //Generate the Navigation.ss file
        Template navigationTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverStripeDT.coreversion.ss_4.templates.newtheme.navigation");
        PHPTemplateStore.CompiledTemplate navigationTemplate=PHPTemplateStore.compileTemplate(templateRegistry, navigationTemplateToCompile, project.getName()+"/templates/Includes", "Navigation.ss");
        new SilverStripeFileCreator().createFile(wizard, project.getName()+"/templates/Includes", "Navigation.ss", monitor, navigationTemplate.string, navigationTemplate.offset);
        
        
        //Generate the Pagination.ss file
        Template paginationTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverStripeDT.coreversion.ss_4.templates.newtheme.pagination");
        PHPTemplateStore.CompiledTemplate paginationTemplate=PHPTemplateStore.compileTemplate(templateRegistry, paginationTemplateToCompile, project.getName()+"/templates/Includes", "Pagination.ss");
        new SilverStripeFileCreator().createFile(wizard, project.getName()+"/templates/Includes", "Pagination.ss", monitor, paginationTemplate.string, paginationTemplate.offset);
    }

}
