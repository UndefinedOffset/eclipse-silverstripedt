package ca.edchipman.silverstripepdt.templates;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.persistence.TemplatePersistenceData;
import org.eclipse.jface.text.templates.persistence.TemplateReaderWriter;
import org.eclipse.php.internal.ui.preferences.PHPTemplateStore;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.internal.editors.text.NLSUtility;
import org.osgi.framework.Bundle;

@SuppressWarnings("restriction")
public class SilverStripeTemplateStore extends PHPTemplateStore {
    /* extension point string literals */
    private static final String TEMPLATES_EXTENSION_POINT= "org.eclipse.ui.editors.templates"; //$NON-NLS-1$

    private static final String ID= "id"; //$NON-NLS-1$
    private static final String NAME= "name"; //$NON-NLS-1$

    private static final String CONTEXT_TYPE_ID= "contextTypeId"; //$NON-NLS-1$
    private static final String DESCRIPTION= "description"; //$NON-NLS-1$
    private static final String AUTO_INSERT= "autoinsert"; //$NON-NLS-1$
    private static final String SS_VERSIONS="ss-versions"; //$NON-NLS-1$ 

    private static final String TEMPLATE= "template"; //$NON-NLS-1$
    private static final String PATTERN= "pattern"; //$NON-NLS-1$

    private static final String INCLUDE= "include"; //$NON-NLS-1$
    private static final String FILE= "file"; //$NON-NLS-1$
    private static final String TRANSLATIONS= "translations"; //$NON-NLS-1$

	private IPersistentPreferenceStore fPreferenceStore;

	private String fKey;

    public SilverStripeTemplateStore(ContextTypeRegistry registry, IPreferenceStore store, String key) {
        super(registry, store, key);
        
        fPreferenceStore=(IPersistentPreferenceStore) store;
        fKey=key;
    }
    
    /**
	 * Loads the templates from contributions and preferences.
	 *
	 * @throws IOException if loading fails.
	 */
	public void load() throws IOException {
		super.load();
		
		loadCustomTemplates();
	}

	private void loadCustomTemplates() throws IOException {
		String pref= fPreferenceStore.getString(fKey);
		if (pref != null && pref.trim().length() > 0) {
			Reader input= new StringReader(pref);
			TemplateReaderWriter reader= new SilverStripeTemplateReaderWriter();
			TemplatePersistenceData[] datas= reader.read(input);
			for (int i= 0; i < datas.length; i++) {
				TemplatePersistenceData data= datas[i];
				add(data);
			}
		}
	}

    /**
     * Loads the templates contributed via the templates extension point.
     *
     * @throws IOException {@inheritDoc}
     */
    protected void loadContributedTemplates() throws IOException {
        IConfigurationElement[] extensions= getTemplateExtensions();
        Collection contributed= readContributedTemplates(extensions);
        for (Iterator it= contributed.iterator(); it.hasNext();) {
            TemplatePersistenceData data= (TemplatePersistenceData) it.next();
            internalAdd(data);
        }
    }

    private static IConfigurationElement[] getTemplateExtensions() {
        return Platform.getExtensionRegistry().getConfigurationElementsFor(TEMPLATES_EXTENSION_POINT);
    }

    private Collection readContributedTemplates(IConfigurationElement[] extensions) throws IOException {
        Collection templates= new ArrayList();
        for (int i= 0; i < extensions.length; i++) {
            if (extensions[i].getName().equals(TEMPLATE))
                createTemplate(templates, extensions[i]);
            else if (extensions[i].getName().equals(INCLUDE)) {
                readIncludedTemplates(templates, extensions[i]);
            }
        }

        return templates;
    }

    private void createTemplate(Collection map, IConfigurationElement element) {
        String contextTypeId= element.getAttribute(CONTEXT_TYPE_ID);
        // log failures since extension point id and name are mandatory
        if (contextExists(contextTypeId)) {
            String id= element.getAttribute(ID);
            if (isValidTemplateId(id)) {

                String name= element.getAttribute(NAME);
                if (name != null) {

                    String pattern= element.getChildren(PATTERN)[0].getValue();
                    if (pattern != null) {

                        String desc= element.getAttribute(DESCRIPTION);
                        if (desc == null)
                            desc= ""; //$NON-NLS-1$

                        String autoInsert= element.getAttribute(AUTO_INSERT);
                        boolean bAutoInsert;
                        if (autoInsert == null)
                            bAutoInsert= true;
                        else
                            bAutoInsert= Boolean.valueOf(autoInsert).booleanValue();
                        

                        String ssVersions=element.getAttribute(SS_VERSIONS);
                        String[] ssVersionsArray;
                        if(ssVersions == null) {
                            ssVersionsArray=new String[0];
                        }else {
                            ssVersionsArray=ssVersions.split(",");
                        }
                        

                        SilverStripeTemplate template= new SilverStripeTemplate(name, desc, contextTypeId, pattern, bAutoInsert, ssVersionsArray);
                        TemplatePersistenceData data= new TemplatePersistenceData(template, true, id);
                        if (validateTemplate(template))
                            map.add(data);
                    }
                }
            }
        }
    }

    /**
     * Returns <code>true</code> if a context type id specifies a valid context type
     * or if no context type registry is present.
     *
     * @param contextTypeId the context type id to look for
     * @return <code>true</code> if the context type specified by the id
     *         is present in the context type registry, or if no registry is
     *         specified
     */
    private boolean contextExists(String contextTypeId) {
        return contextTypeId != null && (getRegistry() == null || getRegistry().getContextType(contextTypeId) != null);
    }

    /**
     * Validates a template against the context type registered in the context
     * type registry. Returns always <code>true</code> if no registry is
     * present.
     *
     * @param template the template to validate
     * @return <code>true</code> if validation is successful or no context
     *         type registry is specified, <code>false</code> if validation
     *         fails
     */
    private boolean validateTemplate(Template template) {
        String contextTypeId= template.getContextTypeId();
        if (!contextExists(contextTypeId))
            return false;

        if (getRegistry() != null) {
            try {
                getRegistry().getContextType(contextTypeId).validate(template.getPattern());
            } catch (TemplateException e) {
                EditorsPlugin.log(NLSUtility.format("Ignoring template \"{0}\" since validation failed.", template.getName()), e);
                return false;
            }
        }
        return true;
    }

    private static boolean isValidTemplateId(String id) {
        return id != null && id.trim().length() != 0; // TODO test validity?
    }

    private void readIncludedTemplates(Collection templates, IConfigurationElement element) throws IOException {
        String file= element.getAttribute(FILE);
        if (file != null) {
            Bundle plugin = Platform.getBundle(element.getContributor().getName());
            URL url= FileLocator.find(plugin, Path.fromOSString(file), null);
            if (url != null) {
                ResourceBundle bundle= null;
                InputStream bundleStream= null;
                InputStream stream= null;
                try {
                    String translations= element.getAttribute(TRANSLATIONS);
                    if (translations != null) {
                        URL bundleURL= FileLocator.find(plugin, Path.fromOSString(translations), null);
                        if (bundleURL != null) {
                            bundleStream= bundleURL.openStream();
                            bundle= new PropertyResourceBundle(bundleStream);
                        }
                    }

                    stream= new BufferedInputStream(url.openStream());
                    TemplateReaderWriter reader= new SilverStripeTemplateReaderWriter();
                    TemplatePersistenceData[] datas= reader.read(stream, bundle);
                    for (int i= 0; i < datas.length; i++) {
                        TemplatePersistenceData data= datas[i];
                        if (data.isCustom()) {
                            if (data.getId() == null)
                                EditorsPlugin.logErrorMessage(NLSUtility.format("Ignoring template \"{0}\" since it has no id.", data.getTemplate().getName()));
                            else
                                EditorsPlugin.logErrorMessage(NLSUtility.format("Ignoring template \"{0}\" since it is deleted.", data.getTemplate().getName()));
                        } else if (validateTemplate(data.getTemplate())) {
                            templates.add(data);
                        }
                    }
                } finally {
                    try {
                        if (bundleStream != null)
                            bundleStream.close();
                    } catch (IOException x) {
                    } finally {
                        try {
                            if (stream != null)
                                stream.close();
                        } catch (IOException x) {
                        }
                    }
                }
            }
        }
    }

	/**
	 * Saves the templates to the preferences.
	 *
	 * @throws IOException if the templates cannot be written
	 */
	public void save() throws IOException {
		ArrayList custom= new ArrayList();
		List<TemplatePersistenceData> fTemplates=Arrays.asList(this.getTemplateData(true));
		for (Iterator it= fTemplates.iterator(); it.hasNext();) {
			TemplatePersistenceData data= (TemplatePersistenceData) it.next();
			if (data.isCustom() && !(data.isUserAdded() && data.isDeleted())) // don't save deleted user-added templates
				custom.add(data);
		}

		StringWriter output= new StringWriter();
		TemplateReaderWriter writer= new SilverStripeTemplateReaderWriter();
		writer.save((TemplatePersistenceData[]) custom.toArray(new TemplatePersistenceData[custom.size()]), output);

		this.stopListeningForPreferenceChanges();
		try {
			fPreferenceStore.setValue(fKey, output.toString());
			if (fPreferenceStore instanceof IPersistentPreferenceStore)
				((IPersistentPreferenceStore)fPreferenceStore).save();
		} finally {
			this.startListeningForPreferenceChanges();
		}
	}
}
