package ca.edchipman.silverstripepdt.contentassist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.php.internal.core.preferences.CorePreferencesSupport;
import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.eclipse.wst.xml.ui.internal.contentassist.DefaultXMLCompletionProposalComputer;

import ca.edchipman.silverstripepdt.SilverStripeVersion;
import ca.edchipman.silverstripepdt.wizards.NewSilverStripeTemplatesWizardPage;

@SuppressWarnings("restriction")
public class SSTemplateCompletionProposalComputer extends DefaultXMLCompletionProposalComputer {
        /** template processor used to create template proposals */
        private SSTemplateCompletionProcessor fTemplateProcessor = null;

        /**
         * <p>Create the computer</p>
         */
        public SSTemplateCompletionProposalComputer() {
            fTemplateProcessor = new SSTemplateCompletionProcessor();
        }

        /**
         * <p>Calls super to add templates based on context and then
         * adds templates not specific to a context</p>
         * 
         * @see org.eclipse.wst.xml.ui.internal.contentassist.AbstractXMLCompletionProposalComputer#computeCompletionProposals(org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext, org.eclipse.core.runtime.IProgressMonitor)
         */
        public List computeCompletionProposals(CompletionProposalInvocationContext context, IProgressMonitor monitor) {
            //get the templates specific to the context
            List proposals = new ArrayList(super.computeCompletionProposals(context, monitor));
            
            //get templates not specific to the context
            proposals.addAll(this.getTemplateProposals(SSTemplateCompletionProcessor.TEMPLATE_CONTEXT_ID, context));
            
            return proposals;
        }
        
        /**
         * @see org.eclipse.wst.xml.ui.internal.contentassist.DefaultXMLCompletionProposalComputer#addEmptyDocumentProposals(org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest, org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext)
         */
        protected void addEmptyDocumentProposals(ContentAssistRequest contentAssistRequest, CompletionProposalInvocationContext context) {
            IProject project=getProject(context.getDocument());
            String ssVersion=SilverStripeVersion.DEFAULT_VERSION;
            
            if(project!=null) {
                ssVersion=CorePreferencesSupport.getInstance().getProjectSpecificPreferencesValue("silverstripe_version", SilverStripeVersion.DEFAULT_VERSION, project);
            }
            
            if(ssVersion.equals(SilverStripeVersion.SS3_0)==false && ssVersion.equals(SilverStripeVersion.SS3_1)==false) {
                addTemplates(contentAssistRequest, NewSilverStripeTemplatesWizardPage.NEW_SS_TEMPLATE_CONTEXTTYPE, context);
            } else {
                addTemplates(contentAssistRequest, NewSilverStripeTemplatesWizardPage.NEW_SS_30_TEMPLATE_CONTEXTTYPE, context);
            }
        }
        
        /**
         * @see org.eclipse.wst.xml.ui.internal.contentassist.DefaultXMLCompletionProposalComputer#addTagInsertionProposals(org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest, int, org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext)
         */
        protected void addTagInsertionProposals(ContentAssistRequest contentAssistRequest, int childPosition, CompletionProposalInvocationContext context) {
            addTemplates(contentAssistRequest, SSTemplateCompletionProcessor.TEMPLATE_CONTEXT_ID, context);
        }
        
        /**
         * <p>Get the template proposals from the template processor</p>
         * 
         * @param templateContext
         * @param context
         * @return
         */
        private List getTemplateProposals(String templateContext, CompletionProposalInvocationContext context) {
            
            List templateProposals = new ArrayList();
            
            if (fTemplateProcessor != null) {
                fTemplateProcessor.setContextType(templateContext);
                ICompletionProposal[] proposals = fTemplateProcessor.computeCompletionProposals(context.getViewer(), context.getInvocationOffset());
            
                templateProposals.addAll(Arrays.asList(proposals));
            }
            
            return templateProposals;
        }
        
        private static IProject getProject(IDocument document) {
            ITextFileBufferManager fileBufferMgr = FileBuffers.getTextFileBufferManager();
            ITextFileBuffer fileBuffer = fileBufferMgr.getTextFileBuffer(document);
            
            if (fileBuffer != null) {
                IWorkspace workspace = ResourcesPlugin.getWorkspace();
                IResource res = workspace.getRoot().findMember(fileBuffer.getLocation());
                if (res != null) return res.getProject();           
            }
            
            return null;
        }
        
        /**
         * Adds templates to the list of proposals
         * 
         * @param contentAssistRequest
         * @param templateContext
         * @param context
         */
        private void addTemplates(ContentAssistRequest contentAssistRequest, String templateContext, CompletionProposalInvocationContext context) {
            if (contentAssistRequest != null) {
                boolean useProposalList = !contentAssistRequest.shouldSeparate();
                List proposals = this.getTemplateProposals(templateContext, context);
        
                for (int i = 0; i < proposals.size(); ++i) {
                    if (useProposalList) {
                        contentAssistRequest.addProposal((ICompletionProposal)proposals.get(i));
                    }
                    else {
                        contentAssistRequest.addMacro((ICompletionProposal)proposals.get(i));
                    }
                }
            }
        }
    }