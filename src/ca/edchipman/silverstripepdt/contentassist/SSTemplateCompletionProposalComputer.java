package ca.edchipman.silverstripepdt.contentassist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.wst.html.ui.internal.templates.TemplateContextTypeIdsHTML;
import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.eclipse.wst.xml.ui.internal.contentassist.DefaultXMLCompletionProposalComputer;

import ca.edchipman.silverstripepdt.wizards.NewSilverStripeTemplatesWizardPage;

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
            addTemplates(contentAssistRequest, NewSilverStripeTemplatesWizardPage.NEW_SS_TEMPLATE_CONTEXTTYPE, context);
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