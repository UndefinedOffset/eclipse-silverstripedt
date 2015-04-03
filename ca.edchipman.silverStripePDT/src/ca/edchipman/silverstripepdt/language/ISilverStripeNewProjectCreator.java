package ca.edchipman.silverstripepdt.language;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.templates.ContextTypeRegistry;

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
     */
    public void createProjectLayout(IProject project, IProgressMonitor monitor, ContextTypeRegistry templateRegistry, boolean isFrameworkLayout);
    
    /**
     * Performs the SilverStripe version specific tasks when creating new module layout project
     * @param project Destination project
     * @param monitor Monitor to update when creating the layout
     * @param templateRegistry Template registry to look through
     * @param isFrameworkLayout If the project is a framework only project this is set to true
     */
    public void createModuleLayout(IProject project, IProgressMonitor monitor, ContextTypeRegistry templateRegistry, boolean isFrameworkLayout);
    
    /**
     * Performs the SilverStripe version specific tasks when creating new theme layout project
     * @param project Destination project
     * @param monitor Monitor to update when creating the layout
     * @param templateRegistry Template registry to look through
     * @param isFrameworkLayout If the project is a framework only project this is set to true
     */
    public void createThemeLayout(IProject project, IProgressMonitor monitor, ContextTypeRegistry templateRegistry, boolean isFrameworkLayout);
}
