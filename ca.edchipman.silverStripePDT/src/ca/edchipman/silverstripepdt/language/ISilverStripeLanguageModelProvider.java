package ca.edchipman.silverstripepdt.language;

import org.eclipse.dltk.core.IScriptProject;

import ca.edchipman.silverstripepdt.SilverStripeVersion;

public interface ISilverStripeLanguageModelProvider {
    public String getLanguageLibraryPath(IScriptProject project, String ssFrameworkModel);
}
