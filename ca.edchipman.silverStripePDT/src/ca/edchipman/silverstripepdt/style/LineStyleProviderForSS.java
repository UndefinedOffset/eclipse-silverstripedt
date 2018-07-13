package ca.edchipman.silverstripepdt.style;


import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.wst.html.ui.internal.HTMLUIPlugin;
import org.eclipse.wst.html.ui.internal.style.LineStyleProviderForHTML;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.xml.ui.internal.style.IStyleConstantsXML;

import ca.edchipman.silverstripepdt.SilverStripePDTPlugin;
import ca.edchipman.silverstripepdt.SilverStripeVersion;
import ca.edchipman.silverstripepdt.regions.SilverStripeRegionContext;
import ca.edchipman.silverstripepdt.versioninterfaces.SilverStripeVersionStyleProvider;

@SuppressWarnings("restriction")
public class LineStyleProviderForSS extends LineStyleProviderForHTML {
    private IPreferenceStore preferenceStore;
    
    protected SilverStripeVersionStyleProvider versionStyleProvider;
    
    public LineStyleProviderForSS() {
        super();
    }
    
    /**
     * a method to centralize all the "format rules" for regions 
     * specifically associated for how to "open" the region.
     */
    // NOTE: this method was just copied down form LineStyleProviderForXML
    public TextAttribute getAttributeFor(ITextRegion region) {
        // not sure why this is coming through null, but just to catch it
        if (region == null) {
            return (TextAttribute)getTextAttributes().get(IStyleConstantsXML.XML_CONTENT);
        }
        
        String type = region.getType();
        
        if(this.versionStyleProvider!=null) {
            TextAttribute versionResult=this.versionStyleProvider.getAttributeFor(type);
            if(versionResult!=null) {
                return versionResult;
            }
        }
        
        if (type == SilverStripeRegionContext.SS_OPEN) {
            return (TextAttribute)getTextAttributes().get(IStyleConstantsSS.SS_DELIM);
        } else if (type == SilverStripeRegionContext.SS_CLOSE) {
            return (TextAttribute)getTextAttributes().get(IStyleConstantsSS.SS_DELIM);
        } else if (type == SilverStripeRegionContext.SS_CONDITIONAL_OPEN) {
            return (TextAttribute)getTextAttributes().get(IStyleConstantsSS.SS_CONDITIONAL_OPEN);
        } else if (type == SilverStripeRegionContext.SS_CONDITIONAL_TEXT) {
            return (TextAttribute)getTextAttributes().get(IStyleConstantsSS.SS_CONDITIONAL_TEXT);
        } else if (type == SilverStripeRegionContext.SS_ELSE) {
            return (TextAttribute)getTextAttributes().get(IStyleConstantsSS.SS_ELSE);
        } else if (type == SilverStripeRegionContext.SS_END_IF) {
            return (TextAttribute)getTextAttributes().get(IStyleConstantsSS.SS_END_IF);
        } else if (type == SilverStripeRegionContext.SS_BASE_TAG) {
            return (TextAttribute)getTextAttributes().get(IStyleConstantsSS.SS_BASE_TAG);
        } else if (type == SilverStripeRegionContext.SS_COMMENT_OPEN || type == SilverStripeRegionContext.SS_COMMENT_CLOSE) {
            return (TextAttribute)getTextAttributes().get(IStyleConstantsSS.SS_COMMENT_DELIM);
        } else if (type == SilverStripeRegionContext.SS_COMMENT_TEXT) {
            return (TextAttribute)getTextAttributes().get(IStyleConstantsSS.SS_COMMENT_TEXT);
        } else if (type == SilverStripeRegionContext.SS_REQUIREMENT_OPEN) {
            return (TextAttribute)getTextAttributes().get(IStyleConstantsSS.SS_REQUIREMENT_OPEN);
        } else if (type == SilverStripeRegionContext.SS_REQUIREMENT_CONTENT) {
            return (TextAttribute)getTextAttributes().get(IStyleConstantsSS.SS_REQUIREMENT_CONTENT);
        } else if (type == SilverStripeRegionContext.SS_INCLUDE_OPEN) {
            return (TextAttribute)getTextAttributes().get(IStyleConstantsSS.SS_INCLUDE_OPEN);
        } else if (type == SilverStripeRegionContext.SS_INCLUDE_CONTENT) {
            return (TextAttribute)getTextAttributes().get(IStyleConstantsSS.SS_INCLUDE_CONTENT);
        } else if (type == SilverStripeRegionContext.SS_VARIABLE) {
            return (TextAttribute)getTextAttributes().get(IStyleConstantsSS.SS_VARIABLE);
        } else if (type == SilverStripeRegionContext.SS_CACHEBLOCK_OPEN) {
            return (TextAttribute)getTextAttributes().get(IStyleConstantsSS.SS_CACHEBLOCK_OPEN);
        } else if (type == SilverStripeRegionContext.SS_CACHEBLOCK_CONTENT) {
            return (TextAttribute)getTextAttributes().get(IStyleConstantsSS.SS_CACHEBLOCK_CONTENT);
        } else if (type == SilverStripeRegionContext.SS_END_CACHEBLOCK) {
            return (TextAttribute)getTextAttributes().get(IStyleConstantsSS.SS_END_CACHEBLOCK);
        } else if (type == SilverStripeRegionContext.SS_UNCACHED_OPEN) {
            return (TextAttribute)getTextAttributes().get(IStyleConstantsSS.SS_UNCACHED_OPEN);
        } else if (type == SilverStripeRegionContext.SS_UNCACHED_CONTENT) {
            return (TextAttribute)getTextAttributes().get(IStyleConstantsSS.SS_UNCACHED_CONTENT);
        } else if (type == SilverStripeRegionContext.SS_END_UNCACHED) {
            return (TextAttribute)getTextAttributes().get(IStyleConstantsSS.SS_END_UNCACHED);
        } else if (type == SilverStripeRegionContext.SS_TEMPLATE_FUNCTION_OPEN) {
            return (TextAttribute)getTextAttributes().get(IStyleConstantsSS.SS_TEMPLATE_FUNCTION_OPEN);
        } else if (type == SilverStripeRegionContext.SS_TEMPLATE_FUNCTION_CONTENT) {
            return (TextAttribute)getTextAttributes().get(IStyleConstantsSS.SS_TEMPLATE_FUNCTION_CONTENT);
        } else if (type == SilverStripeRegionContext.SS_LOOP_OPEN) {
            return (TextAttribute)getTextAttributes().get(IStyleConstantsSS.SS_LOOP_OPEN);
        } else if (type == SilverStripeRegionContext.SS_LOOP_CONTENT) {
            return (TextAttribute)getTextAttributes().get(IStyleConstantsSS.SS_LOOP_CONTENT);
        } else if (type == SilverStripeRegionContext.SS_END_LOOP) {
            return (TextAttribute)getTextAttributes().get(IStyleConstantsSS.SS_END_LOOP);
        } else if (type == SilverStripeRegionContext.SS_WITH_OPEN) {
            return (TextAttribute)getTextAttributes().get(IStyleConstantsSS.SS_WITH_OPEN);
        } else if (type == SilverStripeRegionContext.SS_WITH_CONTENT) {
            return (TextAttribute)getTextAttributes().get(IStyleConstantsSS.SS_WITH_CONTENT);
        } else if (type == SilverStripeRegionContext.SS_END_WITH) {
            return (TextAttribute)getTextAttributes().get(IStyleConstantsSS.SS_END_WITH);
        } else if (type == SilverStripeRegionContext.SS_I18N_OPEN) {
            return (TextAttribute)getTextAttributes().get(IStyleConstantsSS.SS_I18N_OPEN);
        } else if (type == SilverStripeRegionContext.SS_I18N_CONTENT) {
            return (TextAttribute)getTextAttributes().get(IStyleConstantsSS.SS_I18N_CONTENT);
        } else if (type == SilverStripeRegionContext.SS_CONTROL_OPEN || type == SilverStripeRegionContext.SS_CONTROL_CONTENT || type == SilverStripeRegionContext.SS_END_CONTROL) {
            return (TextAttribute)getTextAttributes().get(IStyleConstantsSS.SS_DEPRECATED);
        }
        
        
        // first try "standard" tag attributes from super class
        return super.getAttributeFor(region);
    }
    
    public void setSSVersion(String ssVersion) {
        IConfigurationElement languageProvider=SilverStripeVersion.getLanguageDefinition(ssVersion);
        SilverStripeVersionStyleProvider provider=null;
        Object o;
        try {
            if(languageProvider.getAttribute("style_provider")!=null) {
                o = languageProvider.createExecutableExtension("style_provider");
                if(o instanceof SilverStripeVersionStyleProvider) {
                    provider=((SilverStripeVersionStyleProvider) o);
                    provider.setLineStyleProvider(this);
                }
            }
        } catch (CoreException e) {
            e.printStackTrace();
        }
        
        this.versionStyleProvider=provider;
        
        
        //Reset
        this.loadColors();
        
        //Refresh
        if(getHighlighter()!=null) {
            getHighlighter().refreshDisplay();
        } else if(fRecHighlighter != null) {
            fRecHighlighter.refreshDisplay();
        }
    }
    
    protected void loadColors() {
        if(getTextAttributes()!=null) {
            getTextAttributes().clear();
        }
        
        super.loadColors();

        addTextAttribute(IStyleConstantsSS.SS_DELIM);
        addTextAttribute(IStyleConstantsSS.SS_CONDITIONAL_OPEN);
        addTextAttribute(IStyleConstantsSS.SS_CONDITIONAL_TEXT);
        addTextAttribute(IStyleConstantsSS.SS_END_IF);
        addTextAttribute(IStyleConstantsSS.SS_ELSE);
        addTextAttribute(IStyleConstantsSS.SS_BASE_TAG);
        addTextAttribute(IStyleConstantsSS.SS_COMMENT_DELIM);
        addTextAttribute(IStyleConstantsSS.SS_COMMENT_TEXT);
        addTextAttribute(IStyleConstantsSS.SS_REQUIREMENT_OPEN);
        addTextAttribute(IStyleConstantsSS.SS_REQUIREMENT_CONTENT);
        addTextAttribute(IStyleConstantsSS.SS_INCLUDE_OPEN);
        addTextAttribute(IStyleConstantsSS.SS_INCLUDE_CONTENT);
        addTextAttribute(IStyleConstantsSS.SS_VARIABLE);
        addTextAttribute(IStyleConstantsSS.SS_TEMPLATE_FUNCTION_OPEN);
        addTextAttribute(IStyleConstantsSS.SS_TEMPLATE_FUNCTION_CONTENT);
        addTextAttribute(IStyleConstantsSS.SS_DEPRECATED);
        
        //Add loop, with and i18n tags
        addTextAttribute(IStyleConstantsSS.SS_LOOP_OPEN);
        addTextAttribute(IStyleConstantsSS.SS_LOOP_CONTENT);
        addTextAttribute(IStyleConstantsSS.SS_END_LOOP);
        addTextAttribute(IStyleConstantsSS.SS_WITH_OPEN);
        addTextAttribute(IStyleConstantsSS.SS_WITH_CONTENT);
        addTextAttribute(IStyleConstantsSS.SS_END_WITH);
        addTextAttribute(IStyleConstantsSS.SS_I18N_OPEN);
        addTextAttribute(IStyleConstantsSS.SS_I18N_CONTENT);
        
        
        //Add Cache blocks
        addTextAttribute(IStyleConstantsSS.SS_CACHEBLOCK_OPEN);
        addTextAttribute(IStyleConstantsSS.SS_CACHEBLOCK_CONTENT);
        addTextAttribute(IStyleConstantsSS.SS_END_CACHEBLOCK);
        addTextAttribute(IStyleConstantsSS.SS_UNCACHED_OPEN);
        addTextAttribute(IStyleConstantsSS.SS_UNCACHED_CONTENT);
        addTextAttribute(IStyleConstantsSS.SS_END_UNCACHED);
        
        
        if(this.versionStyleProvider!=null) {
            this.versionStyleProvider.loadVersionColors();
        }
    }
    
    
    protected void handlePropertyChange(PropertyChangeEvent event) {
        if (event != null) {
            String prefKey = event.getProperty();
            String styleKey = null;
            
            // check if preference changed is a style preference
            if (IStyleConstantsSS.SS_DELIM.equals(prefKey)) {
                styleKey=IStyleConstantsSS.SS_DELIM;
            } else if (IStyleConstantsSS.SS_CONDITIONAL_OPEN.equals(prefKey)) {
                styleKey=IStyleConstantsSS.SS_CONDITIONAL_OPEN;
            } else if (IStyleConstantsSS.SS_CONDITIONAL_TEXT.equals(prefKey)) {
                styleKey=IStyleConstantsSS.SS_CONDITIONAL_TEXT;
            } else if (IStyleConstantsSS.SS_END_IF.equals(prefKey)) {
                styleKey=IStyleConstantsSS.SS_END_IF;
            } else if (IStyleConstantsSS.SS_ELSE.equals(prefKey)) {
                styleKey=IStyleConstantsSS.SS_ELSE;
            } else if (IStyleConstantsSS.SS_BASE_TAG.equals(prefKey)) {
                styleKey=IStyleConstantsSS.SS_BASE_TAG;
            } else if (IStyleConstantsSS.SS_COMMENT_DELIM.equals(prefKey)) {
                styleKey=IStyleConstantsSS.SS_COMMENT_DELIM;
            } else if (IStyleConstantsSS.SS_COMMENT_TEXT.equals(prefKey)) {
                styleKey=IStyleConstantsSS.SS_COMMENT_TEXT;
            } else if (IStyleConstantsSS.SS_VARIABLE.equals(prefKey)) {
                styleKey=IStyleConstantsSS.SS_VARIABLE;
            } else if (IStyleConstantsSS.SS_REQUIREMENT_OPEN.equals(prefKey)) {
                styleKey=IStyleConstantsSS.SS_REQUIREMENT_OPEN;
            } else if (IStyleConstantsSS.SS_REQUIREMENT_CONTENT.equals(prefKey)) {
                styleKey=IStyleConstantsSS.SS_REQUIREMENT_CONTENT;
            } else if (IStyleConstantsSS.SS_CONTROL_OPEN.equals(prefKey)) {
                styleKey=IStyleConstantsSS.SS_CONTROL_OPEN;
            } else if (IStyleConstantsSS.SS_CONTROL_CONTENT.equals(prefKey)) {
                styleKey=IStyleConstantsSS.SS_CONTROL_CONTENT;
            } else if (IStyleConstantsSS.SS_END_CONTROL.equals(prefKey)) {
                styleKey=IStyleConstantsSS.SS_END_CONTROL;
            } else if (IStyleConstantsSS.SS_INCLUDE_OPEN.equals(prefKey)) {
                styleKey=IStyleConstantsSS.SS_INCLUDE_OPEN;
            } else if (IStyleConstantsSS.SS_INCLUDE_CONTENT.equals(prefKey)) {
                styleKey=IStyleConstantsSS.SS_INCLUDE_CONTENT;
            } else if (IStyleConstantsSS.SS_CACHEBLOCK_OPEN.equals(prefKey)) {
                styleKey=IStyleConstantsSS.SS_CACHEBLOCK_OPEN;
            } else if (IStyleConstantsSS.SS_CACHEBLOCK_CONTENT.equals(prefKey)) {
                styleKey=IStyleConstantsSS.SS_CACHEBLOCK_CONTENT;
            } else if (IStyleConstantsSS.SS_END_CACHEBLOCK.equals(prefKey)) {
                styleKey=IStyleConstantsSS.SS_END_CACHEBLOCK;
            } else if (IStyleConstantsSS.SS_UNCACHED_OPEN.equals(prefKey)) {
                styleKey=IStyleConstantsSS.SS_UNCACHED_OPEN;
            } else if (IStyleConstantsSS.SS_UNCACHED_CONTENT.equals(prefKey)) {
                styleKey=IStyleConstantsSS.SS_UNCACHED_CONTENT;
            } else if (IStyleConstantsSS.SS_END_UNCACHED.equals(prefKey)) {
                styleKey=IStyleConstantsSS.SS_END_UNCACHED;
            } else if (IStyleConstantsSS.SS_TEMPLATE_FUNCTION_OPEN.equals(prefKey)) {
                styleKey=IStyleConstantsSS.SS_TEMPLATE_FUNCTION_OPEN;
            } else if (IStyleConstantsSS.SS_TEMPLATE_FUNCTION_CONTENT.equals(prefKey)) {
                styleKey=IStyleConstantsSS.SS_TEMPLATE_FUNCTION_CONTENT;
            } else if (IStyleConstantsSS.SS_WITH_OPEN.equals(prefKey)) {
                styleKey=IStyleConstantsSS.SS_WITH_OPEN;
            } else if (IStyleConstantsSS.SS_WITH_CONTENT.equals(prefKey)) {
                styleKey=IStyleConstantsSS.SS_WITH_CONTENT;
            } else if (IStyleConstantsSS.SS_END_WITH.equals(prefKey)) {
                styleKey=IStyleConstantsSS.SS_END_WITH;
            } else if (IStyleConstantsSS.SS_LOOP_OPEN.equals(prefKey)) {
                styleKey=IStyleConstantsSS.SS_LOOP_OPEN;
            } else if (IStyleConstantsSS.SS_LOOP_CONTENT.equals(prefKey)) {
                styleKey=IStyleConstantsSS.SS_LOOP_CONTENT;
            } else if (IStyleConstantsSS.SS_END_LOOP.equals(prefKey)) {
                styleKey=IStyleConstantsSS.SS_END_LOOP;
            } else if (IStyleConstantsSS.SS_I18N_OPEN.equals(prefKey)) {
                styleKey=IStyleConstantsSS.SS_I18N_OPEN;
            } else if (IStyleConstantsSS.SS_I18N_CONTENT.equals(prefKey)) {
                styleKey=IStyleConstantsSS.SS_I18N_CONTENT;
            } else if (IStyleConstantsSS.SS_I18N_CONTENT.equals(prefKey)) {
                styleKey=IStyleConstantsSS.SS_I18N_CONTENT;
            }
            
            if(styleKey!= null) {
                addTextAttribute(styleKey);
                
                if(getHighlighter() != null) {
                    getHighlighter().refreshDisplay();
                } else if(fRecHighlighter != null) {
                    fRecHighlighter.refreshDisplay();
                }
            } else {
                super.handlePropertyChange(event);
            }
        } else {
            super.handlePropertyChange(event);
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.wst.sse.ui.style.AbstractLineStyleProvider#getColorPreferences()
     */
    protected IPreferenceStore getColorPreferences() {
        // Create the preference store lazily.
        if (preferenceStore == null) {
            IPreferenceStore ssPrefStore = new ScopedPreferenceStore(new InstanceScope(),SilverStripePDTPlugin.getDefault().getBundle().getSymbolicName());
            IPreferenceStore htmlPrefStore = HTMLUIPlugin.getDefault().getPreferenceStore();
            preferenceStore = new ChainedPreferenceStore(new IPreferenceStore[] { ssPrefStore, htmlPrefStore });
        }
        
        return preferenceStore;
    }
    
    /**
     * Removes a text attribute from the available attributes based on the key
     * @param colorKey Color key to remove
     */
    public void removeTextAttribute(String colorKey) {
        if(getTextAttributes().containsKey(colorKey)) {
            getTextAttributes().remove(colorKey);
        }
    }
    
    /**
     * Adds a text attribute
     * @param colorKey Color key to add support for
     */
    public void addTextAttribute(String colorKey) {
        super.addTextAttribute(colorKey);
    }

    @SuppressWarnings("rawtypes")
    public HashMap getTextAttributes() {
        return super.getTextAttributes();
    }
}