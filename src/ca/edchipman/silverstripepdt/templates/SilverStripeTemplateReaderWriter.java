package ca.edchipman.silverstripepdt.templates;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.persistence.TemplatePersistenceData;
import org.eclipse.jface.text.templates.persistence.TemplateReaderWriter;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SilverStripeTemplateReaderWriter extends TemplateReaderWriter {
    private static final String TEMPLATE_ELEMENT = "template"; //$NON-NLS-1$
    private static final String NAME_ATTRIBUTE= "name"; //$NON-NLS-1$
    private static final String ID_ATTRIBUTE= "id"; //$NON-NLS-1$
    private static final String DESCRIPTION_ATTRIBUTE= "description"; //$NON-NLS-1$
    private static final String CONTEXT_ATTRIBUTE= "context"; //$NON-NLS-1$
    private static final String ENABLED_ATTRIBUTE= "enabled"; //$NON-NLS-1$
    private static final String DELETED_ATTRIBUTE= "deleted"; //$NON-NLS-1$
    private static final String AUTO_INSERTABLE_ATTRIBUTE= "autoinsert"; //$NON-NLS-1$
    private static final String SS_VERSIONS_ATTRIBUTE="ss-versions"; //$NON-NLS-1$

    /**
     * Reads templates from a reader and returns them. The reader must present
     * a serialized form as produced by the <code>save</code> method.
     *
     * @param reader the reader to read templates from
     * @return the read templates, encapsulated in instances of <code>TemplatePersistenceData</code>
     * @throws IOException if reading from the stream fails
     */
    public TemplatePersistenceData[] read(Reader reader) throws IOException {
        return read(reader, null);
    }

    /**
     * Reads the template with identifier <code>id</code> from a reader and
     * returns it. The reader must present a serialized form as produced by the
     * <code>save</code> method.
     *
     * @param reader the reader to read templates from
     * @param id the id of the template to return
     * @return the read template, encapsulated in an instances of
     *         <code>TemplatePersistenceData</code>
     * @throws IOException if reading from the stream fails
     * @since 3.1
     */
    public TemplatePersistenceData readSingle(Reader reader, String id) throws IOException {
        TemplatePersistenceData[] datas= read(new InputSource(reader), null, id);
        if (datas.length > 0)
            return datas[0];
        return null;
    }

    /**
     * Reads templates from a stream and adds them to the templates.
     *
     * @param reader the reader to read templates from
     * @param bundle a resource bundle to use for translating the read templates, or <code>null</code> if no translation should occur
     * @return the read templates, encapsulated in instances of <code>TemplatePersistenceData</code>
     * @throws IOException if reading from the stream fails
     */
    public TemplatePersistenceData[] read(Reader reader, ResourceBundle bundle) throws IOException {
        return read(new InputSource(reader), bundle, null);
    }

    /**
     * Reads templates from a stream and adds them to the templates.
     *
     * @param stream the byte stream to read templates from
     * @param bundle a resource bundle to use for translating the read templates, or <code>null</code> if no translation should occur
     * @return the read templates, encapsulated in instances of <code>TemplatePersistenceData</code>
     * @throws IOException if reading from the stream fails
     */
    public TemplatePersistenceData[] read(InputStream stream, ResourceBundle bundle) throws IOException {
        return read(new InputSource(stream), bundle, null);
    }
    
    /**
     * Reads templates from an <code>InputSource</code> and adds them to the templates.
     *
     * @param source the input source
     * @param bundle a resource bundle to use for translating the read templates, or <code>null</code> if no translation should occur
     * @param singleId the template id to extract, or <code>null</code> to read in all templates
     * @return the read templates, encapsulated in instances of <code>TemplatePersistenceData</code>
     * @throws IOException if reading from the stream fails
     */
    private TemplatePersistenceData[] read(InputSource source, ResourceBundle bundle, String singleId) throws IOException {
        try {
            Collection templates= new ArrayList();
            Set ids= new HashSet();

            DocumentBuilderFactory factory= DocumentBuilderFactory.newInstance();
            DocumentBuilder parser= factory.newDocumentBuilder();
            parser.setErrorHandler(new DefaultHandler());
            Document document= parser.parse(source);

            NodeList elements= document.getElementsByTagName(TEMPLATE_ELEMENT);

            int count= elements.getLength();
            for (int i= 0; i != count; i++) {
                Node node= elements.item(i);
                NamedNodeMap attributes= node.getAttributes();

                if (attributes == null)
                    continue;

                String id= getStringValue(attributes, ID_ATTRIBUTE, null);
                if (id != null && ids.contains(id))
                    throw new IOException("Duplicate template id"); //$NON-NLS-1$

                if (singleId != null && !singleId.equals(id))
                    continue;

                boolean deleted = getBooleanValue(attributes, DELETED_ATTRIBUTE, false);

                String name= getStringValue(attributes, NAME_ATTRIBUTE);
                name= translateString(name, bundle);

                String description= getStringValue(attributes, DESCRIPTION_ATTRIBUTE, ""); //$NON-NLS-1$
                description= translateString(description, bundle);

                String context= getStringValue(attributes, CONTEXT_ATTRIBUTE);

                if (name == null || context == null)
                    throw new IOException("Missing required attribute"); //$NON-NLS-1$

                boolean enabled = getBooleanValue(attributes, ENABLED_ATTRIBUTE, true);
                boolean autoInsertable= getBooleanValue(attributes, AUTO_INSERTABLE_ATTRIBUTE, true);
                String[] ssVersionsArray=getStringArrayValue(attributes, SS_VERSIONS_ATTRIBUTE);

                StringBuffer buffer= new StringBuffer();
                NodeList children= node.getChildNodes();
                for (int j= 0; j != children.getLength(); j++) {
                    String value= children.item(j).getNodeValue();
                    if (value != null)
                        buffer.append(value);
                }
                String pattern= buffer.toString();
                pattern= translateString(pattern, bundle);

                SilverStripeTemplate template= new SilverStripeTemplate(name, description, context, pattern, autoInsertable, ssVersionsArray);
                TemplatePersistenceData data= new TemplatePersistenceData(template, enabled, id);
                data.setDeleted(deleted);

                templates.add(data);

                if (singleId != null && singleId.equals(id))
                    break;
            }

            return (TemplatePersistenceData[]) templates.toArray(new TemplatePersistenceData[templates.size()]);

        } catch (ParserConfigurationException e) {
            Assert.isTrue(false);
        } catch (SAXException e) {
            Throwable t= e.getCause();
            if (t instanceof IOException)
                throw (IOException) t;
            else if (t != null)
                throw new IOException(t.getMessage());
            else
                throw new IOException(e.getMessage());
        }

        return null; // dummy
    }

    private boolean getBooleanValue(NamedNodeMap attributes, String attribute, boolean defaultValue) throws SAXException {
        Node enabledNode= attributes.getNamedItem(attribute);
        if (enabledNode == null)
            return defaultValue;
        else if (enabledNode.getNodeValue().equals(Boolean.toString(true)))
            return true;
        else if (enabledNode.getNodeValue().equals(Boolean.toString(false)))
            return false;
        else
            throw new SAXException("Illegal boolean attribute, must be \"true\" or \"false\"."); //$NON-NLS-1$
    }

    private String getStringValue(NamedNodeMap attributes, String name) throws SAXException {
        String val= getStringValue(attributes, name, null);
        if (val == null)
            throw new SAXException("Missing required attribute"); //$NON-NLS-1$
        return val;
    }

    private String getStringValue(NamedNodeMap attributes, String name, String defaultValue) {
        Node node= attributes.getNamedItem(name);
        return node == null ? defaultValue : node.getNodeValue();
    }

    private String[] getStringArrayValue(NamedNodeMap attributes, String name) {
        Node node= attributes.getNamedItem(name);
        return (node == null ? new String[0] : node.getNodeValue().split(","));
    }

    private String translateString(String str, ResourceBundle bundle) {
        if (bundle == null)
            return str;

        int idx= str.indexOf('%');
        if (idx == -1) {
            return str;
        }
        StringBuffer buf= new StringBuffer();
        int k= 0;
        while (idx != -1) {
            buf.append(str.substring(k, idx));
            for (k= idx + 1; k < str.length() && !Character.isWhitespace(str.charAt(k)); k++) {
                // loop
            }
            String key= str.substring(idx + 1, k);
            buf.append(getBundleString(key, bundle));
            idx= str.indexOf('%', k);
        }
        buf.append(str.substring(k));
        return buf.toString();
    }

    private String getBundleString(String key, ResourceBundle bundle) {
        if (bundle != null) {
            try {
                return bundle.getString(key);
            } catch (MissingResourceException e) {
                return '!' + key + '!';
            }
        }
        
        return key; // default messages
    }
}
