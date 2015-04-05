package ca.edchipman.silverstripepdt.versioninterfaces;

import org.eclipse.dltk.core.IScriptProject;

/**
 * Interface used in SilverStripe version plugins to provide the language library path 
 * @author Ed Chipman
 */
public interface ISilverStripeLanguageModelProvider {
    /**
     * Gets the path to the language provider
     * @param project Project to look for the language library path for
     * @param ssFrameworkModel Framework model to load
     * @return String path to the language library
     */
    public String getLanguageLibraryPath(IScriptProject project, String ssFrameworkModel);
    
    /**
     * Gets the template context to be used
     * @return Gets the constant used for the templates
     */
    public String getTemplateContext();
}
