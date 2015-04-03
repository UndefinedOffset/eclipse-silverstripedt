package ca.edchipman.silverstripedt.ss_3_1.project;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.php.internal.ui.preferences.PHPTemplateStore;

import ca.edchipman.silverstripepdt.language.ISilverStripeNewProjectCreator;
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
            Template pageTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverstripepdt.SilverStripe.templates.newssproject.ss31.defaultpage");
            PHPTemplateStore.CompiledTemplate pageTemplate=PHPTemplateStore.compileTemplate(templateRegistry, pageTemplateToCompile, project.getName()+"/code", "Page.php");
            new SilverStripeFileCreator().createFile(wizard, project.getName()+"/code", "Page.php", monitor, pageTemplate.string, pageTemplate.offset);
        }
        
        
        //Create config.yml
        Template ymlConfigTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverstripepdt.SilverStripe.templates.newssproject.ss31.ymlconfig");
        PHPTemplateStore.CompiledTemplate ymlConfigTemplate=PHPTemplateStore.compileTemplate(templateRegistry, ymlConfigTemplateToCompile, project.getName()+"/_config", "config.yml");
        new SilverStripeFileCreator().createFile(wizard, project.getName()+"/_config", "config.yml", monitor, ymlConfigTemplate.string, ymlConfigTemplate.offset, true);
        
        
        //Generate the _config.php file
        Template configTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverstripepdt.SilverStripe.templates.newssproject.ss31.config");
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
        //Generate the layout.css file
        Template layoutTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverstripepdt.SilverStripe.templates.newsstheme.ss30.layout");
        PHPTemplateStore.CompiledTemplate layoutTemplate=PHPTemplateStore.compileTemplate(templateRegistry, layoutTemplateToCompile, project.getName()+"/css", "layout.css");
        new SilverStripeFileCreator().createFile(wizard, project.getName()+"/css", "layout.css", monitor, layoutTemplate.string, layoutTemplate.offset);
        
        
        //Generate the typography.css file
        Template typographyTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverstripepdt.SilverStripe.templates.newsstheme.ss30.typography");
        PHPTemplateStore.CompiledTemplate typographyTemplate=PHPTemplateStore.compileTemplate(templateRegistry, typographyTemplateToCompile, project.getName()+"/css", "typography.css");
        new SilverStripeFileCreator().createFile(wizard, project.getName()+"/css", "typography.css", monitor, typographyTemplate.string, typographyTemplate.offset);
        
        
        //Generate the top level Page.ss file
        Template tlPageTemplateToCompile;
        if(isFrameworkLayout) {
            tlPageTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverstripepdt.SilverStripe.templates.newss.ss30.framework.toplevel");
        } else {
            tlPageTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverstripepdt.SilverStripe.templates.newss.ss30.toplevel");
        }
        
        PHPTemplateStore.CompiledTemplate tlPageTemplate=PHPTemplateStore.compileTemplate(templateRegistry, tlPageTemplateToCompile, project.getName()+"/templates", "Page.ss");
        if(isFrameworkLayout) {
            new SilverStripeFileCreator().createFile(wizard, project.getName()+"/templates", "Controller.ss", monitor, tlPageTemplate.string, tlPageTemplate.offset);
        }else {
            new SilverStripeFileCreator().createFile(wizard, project.getName()+"/templates", "Page.ss", monitor, tlPageTemplate.string, tlPageTemplate.offset);
        }
        
        
        //Generate the Page.ss file
        Template pageTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverstripepdt.SilverStripe.templates.newss.ss30.template");
        PHPTemplateStore.CompiledTemplate pageTemplate=PHPTemplateStore.compileTemplate(templateRegistry, pageTemplateToCompile, project.getName()+"/templates/Layout", "Page.ss");
        
        
        if(isFrameworkLayout) {
            new SilverStripeFileCreator().createFile(wizard, project.getName()+"/templates/Layout", "Controller.ss", monitor, pageTemplate.string, pageTemplate.offset);
        }
        
        
        //Generate the Navigation.ss file
        Template navigationTemplateToCompile=templateStore.findTemplateById("ca.edchipman.silverstripepdt.SilverStripe.templates.newsstheme.ss30.navigation");
        PHPTemplateStore.CompiledTemplate navigationTemplate=PHPTemplateStore.compileTemplate(templateRegistry, navigationTemplateToCompile, project.getName()+"/templates/Includes", "Navigation.ss");
        new SilverStripeFileCreator().createFile(wizard, project.getName()+"/templates/Includes", "Navigation.ss", monitor, navigationTemplate.string, navigationTemplate.offset);
    }

}
