package ca.edchipman.silverstripepdt.language;

import org.eclipse.dltk.core.IScriptProject;

/**
 * Interface used in SilverStripe version plugins to provide the language library path 
 * @author Ed Chipman
 */
public interface ISilverStripeLanguageModelProvider {
    public String getLanguageLibraryPath(IScriptProject project, String ssFrameworkModel);
}
