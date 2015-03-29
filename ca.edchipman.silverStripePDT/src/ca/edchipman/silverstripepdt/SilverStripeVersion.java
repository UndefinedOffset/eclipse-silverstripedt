package ca.edchipman.silverstripepdt;

import java.util.HashMap;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.PlatformUI;

public class SilverStripeVersion {
    public static final String DEFAULT_VERSION="SS3.1";
    
    @Deprecated
    public static final String SS3_1="SS3.1";
    
    @Deprecated
    public static final String SS3_0="SS3.0";
    
    @Deprecated
    public static final String SS2_4="SS2.4";
    
    @Deprecated
    public static final String SS2_3="SS2.3";
    
    public static final String FRAMEWORK_ONLY="framework";
    public static final String FULL_CMS="cms";
    public static final String VERSION_EXTENSION_ID="ca.edchipman.silverStripePDT.ss_version";
    
    private static HashMap<String, IConfigurationElement> lang_registry;
    
    /**
     * Initializes the language registry when the plugin is activated by mapping the configuration elements to the language key
     * @param registry Extension Registry to look for plugins in
     */
    public static void initLanguageRegistry(IExtensionRegistry registry) {
        IConfigurationElement[] extensions=registry.getConfigurationElementsFor(VERSION_EXTENSION_ID);
        
        SilverStripeVersion.lang_registry=new HashMap<String, IConfigurationElement>();
        if(extensions.length>0) {
            for(IConfigurationElement language : extensions) {
                String versionKey="SS"+language.getAttribute("release_chain");
                SilverStripeVersion.lang_registry.put(versionKey, language);
            }
        }
        
        
        //Ensure plugins have been registered
        if(SilverStripeVersion.lang_registry.size()==0) {
            MessageBox dialog=new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.ICON_ERROR | SWT.OK);
            dialog.setText("No Language Plugin");
            dialog.setMessage(
                            "You have not installed any SilverStripe language plugins, you need to install atleast one."+
                            "You can install new language plugins by going to Help > Install New Software then select the SilverStripe DT Update site and install a language plugin."
                        );
            dialog.open();
        }
    }
    
    /**
     * Gets the language configuration element from the language registry
     * @param ssVersion SilverStripe version key to use i.e SS3.1
     * @return Returns the IConfigurationElement representing the SilverStripe version plugin or null if its not found
     */
    public static IConfigurationElement getLanguageDefinition(String ssVersion) {
        if(SilverStripeVersion.lang_registry.containsKey(ssVersion)) {
            return SilverStripeVersion.lang_registry.get(ssVersion);
        }
        
        return null;
    }
}
