package ca.edchipman.silverstripepdt.style;


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

@SuppressWarnings("restriction")
public class LineStyleProviderForSS extends LineStyleProviderForHTML {
    private IPreferenceStore preferenceStore;
    
    protected String ssVersion=SilverStripeVersion.SS3_1;
    
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
        } else if (type == SilverStripeRegionContext.SS_COMMENT_OPEN) {
            return (TextAttribute)getTextAttributes().get(IStyleConstantsSS.SS_COMMENT_DELIM);
        } else if (type == SilverStripeRegionContext.SS_COMMENT_TEXT) {
            return (TextAttribute)getTextAttributes().get(IStyleConstantsSS.SS_COMMENT_TEXT);
        } else if (type == SilverStripeRegionContext.SS_COMMENT_CLOSE) {
            return (TextAttribute)getTextAttributes().get(IStyleConstantsSS.SS_COMMENT_DELIM);
        } else if (type == SilverStripeRegionContext.SS_REQUIREMENT_OPEN) {
            return (TextAttribute)getTextAttributes().get(IStyleConstantsSS.SS_REQUIREMENT_OPEN);
        } else if (type == SilverStripeRegionContext.SS_REQUIREMENT_CONTENT) {
            return (TextAttribute)getTextAttributes().get(IStyleConstantsSS.SS_REQUIREMENT_CONTENT);
        } else if (type == SilverStripeRegionContext.SS_INCLUDE_OPEN) {
            return (TextAttribute)getTextAttributes().get(IStyleConstantsSS.SS_INCLUDE_OPEN);
        } else if (type == SilverStripeRegionContext.SS_INCLUDE_CONTENT) {
            return (TextAttribute)getTextAttributes().get(IStyleConstantsSS.SS_INCLUDE_CONTENT);
        } else if (type == SilverStripeRegionContext.SS_CONTROL_OPEN) {
        	if(this.ssVersion.equals(SilverStripeVersion.SS3_1) || this.ssVersion.equals(SilverStripeVersion.SS3_0)) {
        		return (TextAttribute)getTextAttributes().get(IStyleConstantsSS.SS_DEPRECATED);
        	}
        	
            return (TextAttribute)getTextAttributes().get(IStyleConstantsSS.SS_CONTROL_OPEN);
        } else if (type == SilverStripeRegionContext.SS_CONTROL_CONTENT) {
        	if(this.ssVersion.equals(SilverStripeVersion.SS3_1) || this.ssVersion.equals(SilverStripeVersion.SS3_0)) {
        		return (TextAttribute)getTextAttributes().get(IStyleConstantsSS.SS_DEPRECATED);
        	}
        	
            return (TextAttribute)getTextAttributes().get(IStyleConstantsSS.SS_CONTROL_CONTENT);
        } else if (type == SilverStripeRegionContext.SS_END_CONTROL) {
        	if(this.ssVersion.equals(SilverStripeVersion.SS3_1) || this.ssVersion.equals(SilverStripeVersion.SS3_0)) {
        		return (TextAttribute)getTextAttributes().get(IStyleConstantsSS.SS_DEPRECATED);
        	}
        	
            return (TextAttribute)getTextAttributes().get(IStyleConstantsSS.SS_END_CONTROL);
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
        }
        
        
        // first try "standard" tag attributes from super class
        return super.getAttributeFor(region);
    }
    
    public void setSSVersion(String version) {
        this.ssVersion=version;
        
        //Reset
        this.loadColors();
        
        //Refresh
        if(getHighlighter()!=null) {
            getHighlighter().refreshDisplay();
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
        addTextAttribute(IStyleConstantsSS.SS_CONTROL_OPEN);
        addTextAttribute(IStyleConstantsSS.SS_CONTROL_CONTENT);
        addTextAttribute(IStyleConstantsSS.SS_END_CONTROL);
        
        if(this.ssVersion==null) {
            this.ssVersion=SilverStripeVersion.SS3_1;
        }
        
        if(this.ssVersion.equals(SilverStripeVersion.SS3_0) || this.ssVersion.equals(SilverStripeVersion.SS3_1)) {
            addTextAttribute(IStyleConstantsSS.SS_LOOP_OPEN);
            addTextAttribute(IStyleConstantsSS.SS_LOOP_CONTENT);
            addTextAttribute(IStyleConstantsSS.SS_END_LOOP);
            addTextAttribute(IStyleConstantsSS.SS_WITH_OPEN);
            addTextAttribute(IStyleConstantsSS.SS_WITH_CONTENT);
            addTextAttribute(IStyleConstantsSS.SS_END_WITH);
            addTextAttribute(IStyleConstantsSS.SS_I18N_OPEN);
            addTextAttribute(IStyleConstantsSS.SS_I18N_CONTENT);
        }
        
        if(this.ssVersion.equals(SilverStripeVersion.SS2_3)==false) {
            addTextAttribute(IStyleConstantsSS.SS_CACHEBLOCK_OPEN);
            addTextAttribute(IStyleConstantsSS.SS_CACHEBLOCK_CONTENT);
            addTextAttribute(IStyleConstantsSS.SS_END_CACHEBLOCK);
            addTextAttribute(IStyleConstantsSS.SS_UNCACHED_OPEN);
            addTextAttribute(IStyleConstantsSS.SS_UNCACHED_CONTENT);
            addTextAttribute(IStyleConstantsSS.SS_END_UNCACHED);
        }
    }
    
    
    protected void handlePropertyChange(PropertyChangeEvent event) {
        if (event != null) {
            String prefKey = event.getProperty();
            // check if preference changed is a style preference
            if (IStyleConstantsSS.SS_DELIM.equals(prefKey)) {
                addTextAttribute(IStyleConstantsSS.SS_DELIM);

                // this is what AbstractLineStyleProvider.propertyChange() does
                getHighlighter().refreshDisplay();
            } else if (IStyleConstantsSS.SS_CONDITIONAL_OPEN.equals(prefKey)) {
                addTextAttribute(IStyleConstantsSS.SS_CONDITIONAL_OPEN);

                // this is what AbstractLineStyleProvider.propertyChange() does
                getHighlighter().refreshDisplay();
            } else if (IStyleConstantsSS.SS_CONDITIONAL_TEXT.equals(prefKey)) {
                addTextAttribute(IStyleConstantsSS.SS_CONDITIONAL_TEXT);

                // this is what AbstractLineStyleProvider.propertyChange() does
                getHighlighter().refreshDisplay();
            } else if (IStyleConstantsSS.SS_END_IF.equals(prefKey)) {
                addTextAttribute(IStyleConstantsSS.SS_END_IF);

                // this is what AbstractLineStyleProvider.propertyChange() does
                getHighlighter().refreshDisplay();
            } else if (IStyleConstantsSS.SS_ELSE.equals(prefKey)) {
                addTextAttribute(IStyleConstantsSS.SS_ELSE);

                // this is what AbstractLineStyleProvider.propertyChange() does
                getHighlighter().refreshDisplay();
            } else if (IStyleConstantsSS.SS_BASE_TAG.equals(prefKey)) {
                addTextAttribute(IStyleConstantsSS.SS_BASE_TAG);

                // this is what AbstractLineStyleProvider.propertyChange() does
                getHighlighter().refreshDisplay();
            } else if (IStyleConstantsSS.SS_COMMENT_DELIM.equals(prefKey)) {
                addTextAttribute(IStyleConstantsSS.SS_COMMENT_DELIM);

                // this is what AbstractLineStyleProvider.propertyChange() does
                getHighlighter().refreshDisplay();
            } else if (IStyleConstantsSS.SS_COMMENT_TEXT.equals(prefKey)) {
                addTextAttribute(IStyleConstantsSS.SS_COMMENT_TEXT);

                // this is what AbstractLineStyleProvider.propertyChange() does
                getHighlighter().refreshDisplay();
            } else if (IStyleConstantsSS.SS_VARIABLE.equals(prefKey)) {
                addTextAttribute(IStyleConstantsSS.SS_VARIABLE);

                // this is what AbstractLineStyleProvider.propertyChange() does
                getHighlighter().refreshDisplay();
            } else if (IStyleConstantsSS.SS_REQUIREMENT_OPEN.equals(prefKey)) {
                addTextAttribute(IStyleConstantsSS.SS_REQUIREMENT_OPEN);

                // this is what AbstractLineStyleProvider.propertyChange() does
                getHighlighter().refreshDisplay();
            } else if (IStyleConstantsSS.SS_REQUIREMENT_CONTENT.equals(prefKey)) {
                addTextAttribute(IStyleConstantsSS.SS_REQUIREMENT_CONTENT);

                // this is what AbstractLineStyleProvider.propertyChange() does
                getHighlighter().refreshDisplay();
            } else if (IStyleConstantsSS.SS_CONTROL_OPEN.equals(prefKey)) {
                addTextAttribute(IStyleConstantsSS.SS_CONTROL_OPEN);

                // this is what AbstractLineStyleProvider.propertyChange() does
                getHighlighter().refreshDisplay();
            } else if (IStyleConstantsSS.SS_CONTROL_CONTENT.equals(prefKey)) {
                addTextAttribute(IStyleConstantsSS.SS_CONTROL_CONTENT);

                // this is what AbstractLineStyleProvider.propertyChange() does
                getHighlighter().refreshDisplay();
            } else if (IStyleConstantsSS.SS_END_CONTROL.equals(prefKey)) {
                addTextAttribute(IStyleConstantsSS.SS_END_CONTROL);

                // this is what AbstractLineStyleProvider.propertyChange() does
                getHighlighter().refreshDisplay();
            } else if (IStyleConstantsSS.SS_INCLUDE_OPEN.equals(prefKey)) {
                addTextAttribute(IStyleConstantsSS.SS_INCLUDE_OPEN);

                // this is what AbstractLineStyleProvider.propertyChange() does
                getHighlighter().refreshDisplay();
            } else if (IStyleConstantsSS.SS_INCLUDE_CONTENT.equals(prefKey)) {
                addTextAttribute(IStyleConstantsSS.SS_INCLUDE_CONTENT);

                // this is what AbstractLineStyleProvider.propertyChange() does
                getHighlighter().refreshDisplay();
            } else if (IStyleConstantsSS.SS_CACHEBLOCK_OPEN.equals(prefKey)) {
                addTextAttribute(IStyleConstantsSS.SS_CACHEBLOCK_OPEN);

                // this is what AbstractLineStyleProvider.propertyChange() does
                getHighlighter().refreshDisplay();
            } else if (IStyleConstantsSS.SS_CACHEBLOCK_CONTENT.equals(prefKey)) {
                addTextAttribute(IStyleConstantsSS.SS_CACHEBLOCK_CONTENT);

                // this is what AbstractLineStyleProvider.propertyChange() does
                getHighlighter().refreshDisplay();
            } else if (IStyleConstantsSS.SS_END_CACHEBLOCK.equals(prefKey)) {
                addTextAttribute(IStyleConstantsSS.SS_END_CACHEBLOCK);

                // this is what AbstractLineStyleProvider.propertyChange() does
                getHighlighter().refreshDisplay();
            } else if (IStyleConstantsSS.SS_UNCACHED_OPEN.equals(prefKey)) {
                addTextAttribute(IStyleConstantsSS.SS_UNCACHED_OPEN);

                // this is what AbstractLineStyleProvider.propertyChange() does
                getHighlighter().refreshDisplay();
            } else if (IStyleConstantsSS.SS_UNCACHED_CONTENT.equals(prefKey)) {
                addTextAttribute(IStyleConstantsSS.SS_UNCACHED_CONTENT);

                // this is what AbstractLineStyleProvider.propertyChange() does
                getHighlighter().refreshDisplay();
            } else if (IStyleConstantsSS.SS_END_UNCACHED.equals(prefKey)) {
                addTextAttribute(IStyleConstantsSS.SS_END_UNCACHED);

                // this is what AbstractLineStyleProvider.propertyChange() does
                getHighlighter().refreshDisplay();
            } else if (IStyleConstantsSS.SS_TEMPLATE_FUNCTION_OPEN.equals(prefKey)) {
                addTextAttribute(IStyleConstantsSS.SS_TEMPLATE_FUNCTION_OPEN);

                // this is what AbstractLineStyleProvider.propertyChange() does
                getHighlighter().refreshDisplay();
            } else if (IStyleConstantsSS.SS_TEMPLATE_FUNCTION_CONTENT.equals(prefKey)) {
                addTextAttribute(IStyleConstantsSS.SS_TEMPLATE_FUNCTION_CONTENT);

                // this is what AbstractLineStyleProvider.propertyChange() does
                getHighlighter().refreshDisplay();
            } else if (IStyleConstantsSS.SS_WITH_OPEN.equals(prefKey)) {
                addTextAttribute(IStyleConstantsSS.SS_WITH_OPEN);

                // this is what AbstractLineStyleProvider.propertyChange() does
                getHighlighter().refreshDisplay();
            } else if (IStyleConstantsSS.SS_WITH_CONTENT.equals(prefKey)) {
                addTextAttribute(IStyleConstantsSS.SS_WITH_CONTENT);

                // this is what AbstractLineStyleProvider.propertyChange() does
                getHighlighter().refreshDisplay();
            } else if (IStyleConstantsSS.SS_END_WITH.equals(prefKey)) {
                addTextAttribute(IStyleConstantsSS.SS_END_WITH);

                // this is what AbstractLineStyleProvider.propertyChange() does
                getHighlighter().refreshDisplay();
            } else if (IStyleConstantsSS.SS_LOOP_OPEN.equals(prefKey)) {
                addTextAttribute(IStyleConstantsSS.SS_LOOP_OPEN);

                // this is what AbstractLineStyleProvider.propertyChange() does
                getHighlighter().refreshDisplay();
            } else if (IStyleConstantsSS.SS_LOOP_CONTENT.equals(prefKey)) {
                addTextAttribute(IStyleConstantsSS.SS_LOOP_CONTENT);

                // this is what AbstractLineStyleProvider.propertyChange() does
                getHighlighter().refreshDisplay();
            } else if (IStyleConstantsSS.SS_END_LOOP.equals(prefKey)) {
                addTextAttribute(IStyleConstantsSS.SS_END_LOOP);

                // this is what AbstractLineStyleProvider.propertyChange() does
                getHighlighter().refreshDisplay();
            } else if (IStyleConstantsSS.SS_I18N_OPEN.equals(prefKey)) {
                addTextAttribute(IStyleConstantsSS.SS_I18N_OPEN);

                // this is what AbstractLineStyleProvider.propertyChange() does
                getHighlighter().refreshDisplay();
            } else if (IStyleConstantsSS.SS_I18N_CONTENT.equals(prefKey)) {
                addTextAttribute(IStyleConstantsSS.SS_I18N_CONTENT);

                // this is what AbstractLineStyleProvider.propertyChange() does
                getHighlighter().refreshDisplay();
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
}
