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
            String ssVersion=CorePreferencesSupport.getInstance().getProjectSpecificPreferencesValue("silverstripe_version", SilverStripeVersion.SS3_1, project.getProject());
            String ssFrameworkModel=CorePreferencesSupport.getInstance().getProjectSpecificPreferencesValue("silverstripe_framework_model", SilverStripeVersion.FULL_CMS, project.getProject());
            
            return new Path(getLanguageLibraryPath(project, ssVersion, ssFrameworkModel));
        } catch (Exception e) {
            Logger.logException(e);
            return null;
        }
    }

    public String getName() {
        return "Core API";
    }

    private String getLanguageLibraryPath(IScriptProject project, String ssVersion, String ssFrameworkModel) {
        if (ssVersion.equals(SilverStripeVersion.SS2_4)) {
            return LANGUAGE_LIBRARY_PATH + "2.4";
        }else if (ssVersion.equals(SilverStripeVersion.SS2_3)) {
            return LANGUAGE_LIBRARY_PATH + "2.3";
        }
        
        
        if (ssVersion.equals(SilverStripeVersion.SS3_0)) {
            //For framework only 3.0 focus into framework only
            if(ssFrameworkModel.equals(SilverStripeVersion.FRAMEWORK_ONLY)) {
                return LANGUAGE_LIBRARY_PATH + "3.0/framework";
            }
            
            return LANGUAGE_LIBRARY_PATH + "3.0";
        }
        

        //For framework only 3.1 focus into framework only
        if(ssFrameworkModel.equals(SilverStripeVersion.FRAMEWORK_ONLY)) {
            return LANGUAGE_LIBRARY_PATH + "3.1/framework";
        }
        
        return LANGUAGE_LIBRARY_PATH + "3.1";
    }

    public Plugin getPlugin() {
        return SilverStripePDTPlugin.getDefault();
    }
}
