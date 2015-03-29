package ca.edchipman.silverstripepdt.contentassist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.php.internal.core.preferences.CorePreferencesSupport;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.xml.ui.internal.contentassist.ReplaceNameTemplateContext;
import org.eclipse.wst.sse.core.internal.provisional.IndexedRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.sse.ui.internal.contentassist.ContentAssistUtils;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
import org.w3c.dom.Node;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;

import ca.edchipman.silverstripepdt.SilverStripePDTPlugin;
import ca.edchipman.silverstripepdt.SilverStripeVersion;
import ca.edchipman.silverstripepdt.regions.SilverStripeRegionContext;
import ca.edchipman.silverstripepdt.templates.SilverStripeTemplate;

@SuppressWarnings("restriction")
public class SSTemplateCompletionProcessor extends TemplateCompletionProcessor {
    public static final String TEMPLATE_CONTEXT_ID = "ss_proposal";
    private Image proposalIcon;
    private ITextViewer fTextViewer;
    
    private static final class ProposalComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            return ((TemplateProposal) o2).getRelevance() - ((TemplateProposal) o1).getRelevance();
        }
    }

    private static final Comparator fgProposalComparator = new ProposalComparator();
    private String fContextTypeId = null;
    
    /*
     * Copied from super class except instead of calling createContext(viewer,
     * region) call createContext(viewer, region, offset) instead
     */
    @SuppressWarnings("rawtypes")
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
        fTextViewer=viewer;
        IDocument document=viewer.getDocument();
        int documentPosition = offset;
        String projectSSVersion=CorePreferencesSupport.getInstance().getProjectSpecificPreferencesValue("silverstripe_version", SilverStripeVersion.DEFAULT_VERSION, this.getProject(document));
        
        IndexedRegion treeNode = ContentAssistUtils.getNodeAt(viewer, documentPosition);

        Node node = (Node) treeNode;
        while ((node != null) && (node.getNodeType() == Node.TEXT_NODE) && (node.getParentNode() != null)) {
            node = node.getParentNode();
        }
        IDOMNode xmlnode = (IDOMNode) node;
        
        IStructuredDocumentRegion sdRegion = getStructuredDocumentRegion(documentPosition);
        ITextRegion completionRegion = getCompletionRegion(documentPosition, node);
        
        ITextSelection selection = (ITextSelection) viewer.getSelectionProvider().getSelection();

        // adjust offset to end of normalized selection
        if (selection.getOffset() == offset)
            offset = selection.getOffset() + selection.getLength();

        String prefix = getMatchString(sdRegion, completionRegion, documentPosition);
        Region region = new Region(offset - prefix.length(), prefix.length());
        TemplateContext context = createContext(viewer, region, offset - prefix.length());
        if (context == null)
            return new ICompletionProposal[0];
        // name of the selection variables {line, word}_selection
        context.setVariable("selection", prefix); //$NON-NLS-1$
        
        // TODO Filter based on the completion region context ie if SS open only show ss_open stuff
        Template[] templates = getTemplates(fContextTypeId);

        List matches = new ArrayList();
        for (int i = 0; i < templates.length; i++) {
            Template template = templates[i];
            try {
                context.getContextType().validate(template.getPattern());
            }
            catch (TemplateException e) {
                continue;
            }
            
            if(template instanceof SilverStripeTemplate) {
                SilverStripeTemplate ssTemplate=(SilverStripeTemplate) template;
                
                if ((template.matches(prefix, fContextTypeId)) && (ssTemplate.ssVersions().length==0 || Arrays.asList(ssTemplate.ssVersions()).contains(projectSSVersion))) {
                    matches.add(createProposal(template, context, (IRegion) region, getRelevance(template, prefix)));
                }
            }else if (template.matches(prefix, fContextTypeId)) {
                matches.add(createProposal(template, context, (IRegion) region, getRelevance(template, prefix)));
            }
        }

        Collections.sort(matches, fgProposalComparator);

        return (ICompletionProposal[]) matches.toArray(new ICompletionProposal[matches.size()]);
    }

    /**
     * Creates a concrete template context for the given region in the
     * document. This involves finding out which context type is valid at the
     * given location, and then creating a context of this type. The default
     * implementation returns a <code>SmartReplaceTemplateContext</code> for
     * the context type at the given location. This takes the offset at which
     * content assist was invoked into consideration.
     * 
     * @param viewer
     *            the viewer for which the context is created
     * @param region
     *            the region into <code>document</code> for which the
     *            context is created
     * @param offset
     *            the original offset where content assist was invoked
     * @return a template context that can handle template insertion at the
     *         given location, or <code>null</code>
     */
    private TemplateContext createContext(ITextViewer viewer, IRegion region, int offset) {
        // pretty much same code as super.createContext except create
        // SmartReplaceTemplateContext
        TemplateContextType contextType = getContextType(viewer, region);
        if (contextType != null) {
            IDocument document = viewer.getDocument();
            return new ReplaceNameTemplateContext(contextType, document, region.getOffset(), region.getLength(), offset);
        }
        return null;
    }

    protected ICompletionProposal createProposal(Template template, TemplateContext context, IRegion region, int relevance) {
        return new CustomTemplateProposal(template, context, region, getImage(template), relevance);
    }

    protected TemplateContextType getContextType(ITextViewer viewer, IRegion region) {
        TemplateContextType type = null;

        ContextTypeRegistry registry = getTemplateContextRegistry();
        if (registry != null)
            type = registry.getContextType(fContextTypeId);

        return type;
    }

    protected Image getImage(Template template) {
        if(proposalIcon==null) {
            ImageDescriptor imgTmp=SilverStripePDTPlugin.getImageDescriptor("icons/full/obj16/silverstripe.gif");
            proposalIcon=new Image(null, imgTmp.getImageData());
        }
        
        return proposalIcon;
    }

    private ContextTypeRegistry getTemplateContextRegistry() {
        return SilverStripePDTPlugin.getDefault().getCATemplateContextRegistry();
    }

    protected Template[] getTemplates(String contextTypeId) {
        Template templates[] = null;

        TemplateStore store = getTemplateStore();
        if (store != null)
            templates = store.getTemplates(contextTypeId);

        return templates;
    }

    private TemplateStore getTemplateStore() {
        return SilverStripePDTPlugin.getDefault().getCATemplateStore();
    }

    public void setContextType(String contextTypeId) {
        fContextTypeId = contextTypeId;
    }
    
    public char[] getCompletionProposalAutoActivationCharacters() {
        return new char[] {'<'};
    }
    
    private ITextRegion getCompletionRegion(int offset, IStructuredDocumentRegion sdRegion) {
        ITextRegion region = sdRegion.getRegionAtCharacterOffset(offset);
        if (region == null) {
            return null;
        }

        if (sdRegion.getStartOffset(region) == offset) {
            // The offset is at the beginning of the region
            if ((sdRegion.getStartOffset(region) == sdRegion.getStartOffset()) && (sdRegion.getPrevious() != null) && (!sdRegion.getPrevious().isEnded())) {
                // Is the region also the start of the node? If so, the
                // previous IStructuredDocumentRegion is
                // where to look for a useful region.
                region = sdRegion.getPrevious().getRegionAtCharacterOffset(offset - 1);
            }
            else {
                // Is there no separating whitespace from the previous region?
                // If not,
                // then that region is the important one
                ITextRegion previousRegion = sdRegion.getRegionAtCharacterOffset(offset - 1);
                if ((previousRegion != null) && (previousRegion != region) && (previousRegion.getTextLength() == previousRegion.getLength())) {
                    region = previousRegion;
                }
            }
        }
        else {
            // The offset is NOT at the beginning of the region
            if (offset > sdRegion.getStartOffset(region) + region.getTextLength()) {
                // Is the offset within the whitespace after the text in this
                // region?
                // If so, use the next region
                ITextRegion nextRegion = sdRegion.getRegionAtCharacterOffset(sdRegion.getStartOffset(region) + region.getLength());
                if (nextRegion != null) {
                    region = nextRegion;
                }
            }
            else {
                // Is the offset within the important text for this region?
                // If so, then we've already got the right one.
            }
        }

        // valid WHITE_SPACE region handler (#179924)
        if ((region != null) && (region.getType() == DOMRegionContext.WHITE_SPACE)) {
            ITextRegion previousRegion = sdRegion.getRegionAtCharacterOffset(sdRegion.getStartOffset(region) - 1);
            if (previousRegion != null) {
                region = previousRegion;
            }
        }

        return region;
    }
    
    /**
     * Return the region whose content's require completion. This is something
     * of a misnomer as sometimes the user wants to be prompted for contents
     * of a non-existant ITextRegion, such as for enumerated attribute values
     * following an '=' sign.
     */
    private ITextRegion getCompletionRegion(int documentPosition, Node domnode) {
        if (domnode == null) {
            return null;
        }

        ITextRegion region = null;
        int offset = documentPosition;
        IStructuredDocumentRegion flatNode = null;
        IDOMNode node = (IDOMNode) domnode;

        if (node.getNodeType() == Node.DOCUMENT_NODE) {
            if (node.getStructuredDocument().getLength() == 0) {
                return null;
            }
            ITextRegion result = node.getStructuredDocument().getRegionAtCharacterOffset(offset).getRegionAtCharacterOffset(offset);
            while (result == null) {
                offset--;
                result = node.getStructuredDocument().getRegionAtCharacterOffset(offset).getRegionAtCharacterOffset(offset);
            }
            return result;
        }

        IStructuredDocumentRegion startTag = node.getStartStructuredDocumentRegion();
        IStructuredDocumentRegion endTag = node.getEndStructuredDocumentRegion();

        // Determine if the offset is within the start
        // IStructuredDocumentRegion, end IStructuredDocumentRegion, or
        // somewhere within the Node's XML content.
        if ((startTag != null) && (startTag.getStartOffset() <= offset) && (offset < startTag.getStartOffset() + startTag.getLength())) {
            flatNode = startTag;
        }
        else if ((endTag != null) && (endTag.getStartOffset() <= offset) && (offset < endTag.getStartOffset() + endTag.getLength())) {
            flatNode = endTag;
        }

        if (flatNode != null) {
            // the offset is definitely within the start or end tag, continue
            // on and find the region
            region = getCompletionRegion(offset, flatNode);
        }
        else {
            // the docPosition is neither within the start nor the end, so it
            // must be content
            flatNode = node.getStructuredDocument().getRegionAtCharacterOffset(offset);
            // (pa) ITextRegion refactor
            // if (flatNode.contains(documentPosition)) {
            if ((flatNode.getStartOffset() <= documentPosition) && (flatNode.getEndOffset() >= documentPosition)) {
                // we're interesting in completing/extending the previous
                // IStructuredDocumentRegion if the current
                // IStructuredDocumentRegion isn't plain content or if it's
                // preceded by an orphan '<'
                if ((offset == flatNode.getStartOffset()) &&
                        (flatNode.getPrevious() != null) &&
                        (((flatNode.getRegionAtCharacterOffset(documentPosition) != null) &&
                                (flatNode.getRegionAtCharacterOffset(documentPosition).getType() != DOMRegionContext.XML_CONTENT)) ||
                                (flatNode.getPrevious().getLastRegion().getType() == DOMRegionContext.XML_TAG_OPEN) ||
                                (flatNode.getPrevious().getLastRegion().getType() == DOMRegionContext.XML_END_TAG_OPEN))) {
                    
                    // Is the region also the start of the node? If so, the
                    // previous IStructuredDocumentRegion is
                    // where to look for a useful region.
                    region = flatNode.getPrevious().getLastRegion();
                }
                else if (flatNode.getEndOffset() == documentPosition) {
                    region = flatNode.getLastRegion();
                }
                else {
                    region = flatNode.getFirstRegion();
                }
            }
            else {
                // catch end of document positions where the docPosition isn't
                // in a IStructuredDocumentRegion
                region = flatNode.getLastRegion();
            }
        }

        return region;
    }
    
    private String getMatchString(IStructuredDocumentRegion parent, ITextRegion aRegion, int offset) {
        if (aRegion == null || isCloseRegion(aRegion)) {
            return ""; //$NON-NLS-1$
        }
        String matchString = null;
        String regionType = aRegion.getType();
        if ((regionType == DOMRegionContext.XML_TAG_ATTRIBUTE_EQUALS) || (regionType == DOMRegionContext.XML_TAG_OPEN) || (offset > parent.getStartOffset(aRegion) + aRegion.getTextLength())) {
            matchString = ""; //$NON-NLS-1$
        }
        else if (regionType == DOMRegionContext.XML_CONTENT) {
            matchString = ""; //$NON-NLS-1$
        }
        else {
            if ((parent.getText(aRegion).length() > 0) && (parent.getStartOffset(aRegion) < offset)) {
                matchString = parent.getText(aRegion).substring(0, offset - parent.getStartOffset(aRegion));
            }
            else {
                matchString = ""; //$NON-NLS-1$
            }
        }
        return matchString;
    }
    
    /**
     * StructuredTextViewer must be set before using this.
     */
    private IStructuredDocumentRegion getStructuredDocumentRegion(int pos) {
        return ContentAssistUtils.getStructuredDocumentRegion(fTextViewer, pos);
    }
    
    private boolean isCloseRegion(ITextRegion region) {
        String type = region.getType();
        return ((type == DOMRegionContext.XML_PI_CLOSE) ||
                (type == DOMRegionContext.XML_TAG_CLOSE) ||
                (type == DOMRegionContext.XML_EMPTY_TAG_CLOSE) ||
                (type == DOMRegionContext.XML_CDATA_CLOSE) ||
                (type == DOMRegionContext.XML_COMMENT_CLOSE) ||
                (type == DOMRegionContext.XML_ATTLIST_DECL_CLOSE) ||
                (type == DOMRegionContext.XML_ELEMENT_DECL_CLOSE) ||
                (type == DOMRegionContext.XML_DOCTYPE_DECLARATION_CLOSE) ||
                (type == DOMRegionContext.XML_DECLARATION_CLOSE) ||
                (type == SilverStripeRegionContext.SS_CLOSE) ||
                (type == SilverStripeRegionContext.SS_END_CONTROL) ||
                (type == SilverStripeRegionContext.SS_END_IF) ||
                (type == SilverStripeRegionContext.SS_END_CACHEBLOCK) ||
                (type == SilverStripeRegionContext.SS_END_LOOP) ||
                (type == SilverStripeRegionContext.SS_END_UNCACHED) ||
                (type == SilverStripeRegionContext.SS_END_WITH) ||
                (type == SilverStripeRegionContext.SS_COMMENT_CLOSE));
    }
    
    private static IProject getProject(IDocument document) {
        ITextFileBufferManager fileBufferMgr = FileBuffers.getTextFileBufferManager();
        ITextFileBuffer fileBuffer = fileBufferMgr.getTextFileBuffer(document);
        
        if (fileBuffer != null) {
            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            IResource res = workspace.getRoot().findMember(fileBuffer.getLocation());
            if (res != null) {
                return res.getProject();           
            }
        }
        
        return null;
    }
}
