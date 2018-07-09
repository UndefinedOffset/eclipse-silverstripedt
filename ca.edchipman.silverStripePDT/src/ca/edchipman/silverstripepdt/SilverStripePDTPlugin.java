package ca.edchipman.silverstripepdt;

import java.io.IOException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.php.internal.ui.Logger;
import org.eclipse.php.internal.ui.corext.template.php.CodeTemplateContextType;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import ca.edchipman.silverstripepdt.contentassist.SSTemplateCompletionProcessor;
import ca.edchipman.silverstripepdt.templates.SilverStripeTemplateStore;
import ca.edchipman.silverstripepdt.wizards.NewSilverStripeClassWizardTemplatePage;
import ca.edchipman.silverstripepdt.wizards.NewSilverStripeProjectWizard;
import ca.edchipman.silverstripepdt.wizards.NewSilverStripeTemplatesWizardPage;

/**
 * The activator class controls the plug-in life cycle
 */
@SuppressWarnings("restriction")
public class SilverStripePDTPlugin extends AbstractUIPlugin {
    // The plug-in ID
    public static final String PLUGIN_ID = "ca.edchipman.silverstripepdt"; //$NON-NLS-1$
    public static final String NATURE_ID = "ca.edchipman.silverstripepdt.LANGUAGE"; //$NON-NLS-1$
    protected TemplateStore templateStore = null;
    protected ContextTypeRegistry fContextTypeRegistry = null;
    
    protected TemplateStore caTemplateStore = null;
    protected ContextTypeRegistry caContextTypeRegistry = null;
    protected ContextTypeRegistry fClassContextTypeRegistry;

    // The shared instance
    private static SilverStripePDTPlugin plugin;
    
    /**
     * The constructor
     */
    public SilverStripePDTPlugin() {}

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        
        SilverStripeVersion.initLanguageRegistry(Platform.getExtensionRegistry());
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static SilverStripePDTPlugin getDefault() {
        return plugin;
    }
    
    /**
     * Returns an image descriptor for the image file at the given
     * plug-in relative path
     *
     * @param path the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

    /**
     * Returns the template context type registry for creating SilverStripe classes.
     * @return the template context type registry for creating SilverStripe classes
     */
    public ContextTypeRegistry getNewClassContextRegistry() {
        if (fClassContextTypeRegistry == null) {
            ContributionContextTypeRegistry registry = new ContributionContextTypeRegistry();

            registry.addContextType(new CodeTemplateContextType(NewSilverStripeClassWizardTemplatePage.NEW_CLASS_CONTEXTTYPE));

            fClassContextTypeRegistry = registry;
        }

        return fClassContextTypeRegistry;
    }

    /**
     * Returns the template context type registry for SilverStripe template files.
     * @return the template context type registry for SilverStripe template files
     */
    public ContextTypeRegistry getTemplateContextRegistry() {
        if (fContextTypeRegistry == null) {
            ContributionContextTypeRegistry registry = new ContributionContextTypeRegistry();

            registry.addContextType(new CodeTemplateContextType(NewSilverStripeTemplatesWizardPage.NEW_SS_TEMPLATE_CONTEXTTYPE));
            registry.addContextType(new CodeTemplateContextType(NewSilverStripeTemplatesWizardPage.NEW_SS_30_TEMPLATE_CONTEXTTYPE));
            registry.addContextType(new CodeTemplateContextType(NewSilverStripeTemplatesWizardPage.NEW_SS_40_TEMPLATE_CONTEXTTYPE));
            registry.addContextType(new CodeTemplateContextType(NewSilverStripeProjectWizard.NEW_SS_PROJECT_TEMPLATE_CONTEXTTYPE));

            fContextTypeRegistry = registry;
        }

        return fContextTypeRegistry;
    }

    /**
     * Returns the template store for the xml editor templates.
     * 
     * @return the template store for the xml editor templates
     */
    public TemplateStore getTemplateStore() {
        if (templateStore == null) {
            templateStore = new SilverStripeTemplateStore(getTemplateContextRegistry(), getPreferenceStore(), "ca.edchipman.silverstripepdt.SilverStripe.templates");
            
            try {
                templateStore.load();
            } catch (IOException e) {
                Logger.logException(e);
            }
        }
        return templateStore;
    }

    /**
     * Returns the content assist template context type registry for the xml plugin.
     * 
     * @return the content assist template context type registry for the xml plugin
     */
    public ContextTypeRegistry getCATemplateContextRegistry() {
        if (caContextTypeRegistry == null) {
            ContributionContextTypeRegistry registry = new ContributionContextTypeRegistry();

            registry.addContextType(new CodeTemplateContextType(SSTemplateCompletionProcessor.TEMPLATE_CONTEXT_ID));
            registry.addContextType(new CodeTemplateContextType(NewSilverStripeTemplatesWizardPage.NEW_SS_TEMPLATE_CONTEXTTYPE));
            registry.addContextType(new CodeTemplateContextType(NewSilverStripeTemplatesWizardPage.NEW_SS_30_TEMPLATE_CONTEXTTYPE));
            registry.addContextType(new CodeTemplateContextType(NewSilverStripeTemplatesWizardPage.NEW_SS_40_TEMPLATE_CONTEXTTYPE));

            caContextTypeRegistry = registry;
        }

        return caContextTypeRegistry;
    }

    /**
     * Returns the template store for the xml editor templates.
     * 
     * @return the template store for the xml editor templates
     */
    public TemplateStore getCATemplateStore() {
        if (caTemplateStore == null) {
            caTemplateStore = new SilverStripeTemplateStore(getCATemplateContextRegistry(), getPreferenceStore(), "ca.edchipman.silverstripepdt.contentassist.templates");
            
            try {
                caTemplateStore.load();
            } catch (IOException e) {
                Logger.logException(e);
            }
        }
        return caTemplateStore;
    }
}
