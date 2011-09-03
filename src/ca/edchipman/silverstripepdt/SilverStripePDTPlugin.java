package ca.edchipman.silverstripepdt;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.php.internal.ui.Logger;
import org.eclipse.php.internal.ui.corext.template.php.CodeTemplateContextType;
import org.eclipse.php.internal.ui.preferences.PHPTemplateStore;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.internal.util.BundleUtility;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

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

	// The shared instance
	private static SilverStripePDTPlugin plugin;
	
	/**
	 * The constructor
	 */
	public SilverStripePDTPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
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
     * Returns the template context type registry for the xml plugin.
     * 
     * @return the template context type registry for the xml plugin
     */
    public ContextTypeRegistry getTemplateContextRegistry() {
        if (fContextTypeRegistry == null) {
            ContributionContextTypeRegistry registry = new ContributionContextTypeRegistry();

            registry.addContextType(new CodeTemplateContextType(NewSilverStripeTemplatesWizardPage.NEW_SS_TEMPLATE_CONTEXTTYPE));
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
            templateStore = new PHPTemplateStore(getTemplateContextRegistry(), getPreferenceStore(), "ca.edchipman.silverstripepdt.SilverStripe.templates");
            
            try {
                templateStore.load();
            } catch (IOException e) {
                Logger.logException(e);
            }
        }
        return templateStore;
    }
}
