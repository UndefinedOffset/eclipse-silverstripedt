package ca.edchipman.silverstripepdt;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.core.di.annotations.Execute;

public class SilverStripeVersion {
    public static final String SS3_1="SS3.1";
    public static final String SS3_0="SS3.0";
    public static final String SS2_4="SS2.4";
    public static final String SS2_3="SS2.3";
    public static final String FRAMEWORK_ONLY="framework";
    public static final String FULL_CMS="cms";
    public static final String VERSION_EXTENSION_ID="ca.edchipman.silverStripePDT.ss_version";
    
    private static IConfigurationElement[] lang_registry;
    
    public static void initLanguageRegistry(IExtensionRegistry registry) {
        SilverStripeVersion.lang_registry=registry.getConfigurationElementsFor(VERSION_EXTENSION_ID);
    }
    
    public static IConfigurationElement getLanguageDefinition(String ssVersion) {
        //TODO Improve the performance of this, we should try not to use a loop here maybe we can pre-process the extensions into some sort of Dictionary like object?
        for(IConfigurationElement language : SilverStripeVersion.lang_registry) {
            String versionKey="SS"+language.getAttribute("release_chain");
            if(versionKey.equals(ssVersion)) {
                return language;
            }
        }
        
        return null;
    }
}
