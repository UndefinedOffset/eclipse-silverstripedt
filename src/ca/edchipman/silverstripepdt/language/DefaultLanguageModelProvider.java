package ca.edchipman.silverstripepdt.language;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.php.core.language.ILanguageModelProvider;
import org.eclipse.php.internal.core.Logger;
import org.eclipse.php.internal.core.preferences.CorePreferencesSupport;

import ca.edchipman.silverstripepdt.SilverStripePDTPlugin;
import ca.edchipman.silverstripepdt.SilverStripeVersion;

@SuppressWarnings("restriction")
public class DefaultLanguageModelProvider implements ILanguageModelProvider {
    private static final String LANGUAGE_LIBRARY_PATH = "$nl$/resources/SS"; //$NON-NLS-1$

    public IPath getPath(IScriptProject project) {
        try {
            return new Path(getLanguageLibraryPath(project, CorePreferencesSupport.getInstance().getProjectSpecificPreferencesValue("silverstripe_version", SilverStripeVersion.SS3_0, project.getProject())));
        } catch (Exception e) {
            Logger.logException(e);
            return null;
        }
    }

    public String getName() {
        return "Core API";
    }

    private String getLanguageLibraryPath(IScriptProject project, String ssVersion) {
        if (ssVersion.equals(SilverStripeVersion.SS2_4)) {
            return LANGUAGE_LIBRARY_PATH + "2.4";
        }else if (ssVersion.equals(SilverStripeVersion.SS2_3)) {
            return LANGUAGE_LIBRARY_PATH + "2.3";
        }
        
        return LANGUAGE_LIBRARY_PATH + "3.0";
    }

    public Plugin getPlugin() {
        return SilverStripePDTPlugin.getDefault();
    }
}
