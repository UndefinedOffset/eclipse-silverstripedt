package ca.edchipman.silverstripepdt.language;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.jface.wizard.Wizard;

/**
 * Interface used in SilverStripe version plugins to define the project creation handlers 
 * @author Ed Chipman
 */
public interface ISilverStripeNewProjectCreator {
    /**
     * Performs the SilverStripe version specific tasks when creating new project layout project
     * @param project Destination project
     * @param monitor Monitor to update when creating the layout
     * @param templateRegistry Template registry to look through
     * @param isFrameworkLayout If the project is a framework only project this is set to true
     * @throws CoreException 
     */
    public void createProjectLayout(Wizard wizard, IProject project, IProgressMonitor monitor, ContextTypeRegistry templateRegistry, TemplateStore templateStore, boolean isFrameworkLayout) throws CoreException;
    
    /**
     * Performs the SilverStripe version specific tasks when creating new module layout project
     * @param project Destination project
     * @param monitor Monitor to update when creating the layout
     * @param templateRegistry Template registry to look through
     * @param isFrameworkLayout If the project is a framework only project this is set to true
     * @throws CoreException 
     */
    public void createModuleLayout(Wizard wizard, IProject project, IProgressMonitor monitor, ContextTypeRegistry templateRegistry, TemplateStore templateStore, boolean isFrameworkLayout) throws CoreException;
    
    /**
     * Performs the SilverStripe version specific tasks when creating new theme layout project
     * @param project Destination project
     * @param monitor Monitor to update when creating the layout
     * @param templateRegistry Template registry to look through
     * @param isFrameworkLayout If the project is a framework only project this is set to true
     * @throws CoreException 
     */
    public void createThemeLayout(Wizard wizard, IProject project, IProgressMonitor monitor, ContextTypeRegistry templateRegistry, TemplateStore templateStore, boolean isFrameworkLayout) throws CoreException;
}
