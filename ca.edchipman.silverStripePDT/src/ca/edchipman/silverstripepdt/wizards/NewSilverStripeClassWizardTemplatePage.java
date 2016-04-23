package ca.edchipman.silverstripepdt.wizards;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.php.internal.core.ast.util.Util;
import org.eclipse.php.internal.core.documentModel.provisional.contenttype.ContentTypeIdForPHP;
import org.eclipse.php.internal.core.preferences.CorePreferencesSupport;
import org.eclipse.php.internal.ui.IPHPHelpContextIds;
import org.eclipse.php.internal.ui.Logger;
import org.eclipse.php.internal.ui.editor.configuration.PHPStructuredTextViewerConfiguration;
import org.eclipse.php.internal.ui.preferences.PHPTemplateStore;
import org.eclipse.php.internal.ui.preferences.PreferenceConstants;
import org.eclipse.php.internal.ui.preferences.PHPTemplateStore.CompiledTemplate;
import org.eclipse.php.internal.ui.viewsupport.ProjectTemplateStore;
import org.eclipse.php.internal.ui.wizards.Messages;
import org.eclipse.php.internal.ui.wizards.NewGenericFileTemplatesWizardPage;
import org.eclipse.php.internal.ui.wizards.PHPFileCreationWizard.FileCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.ui.StructuredTextViewerConfiguration;
import org.eclipse.wst.sse.ui.internal.StructuredTextViewer;
import org.eclipse.wst.sse.ui.internal.provisional.style.LineStyleProvider;

import ca.edchipman.silverstripepdt.SilverStripePDTPlugin;
import ca.edchipman.silverstripepdt.SilverStripePluginImages;
import ca.edchipman.silverstripepdt.SilverStripeVersion;
import ca.edchipman.silverstripepdt.preferences.SilverStripePreferences;
import ca.edchipman.silverstripepdt.templates.SilverStripeTemplate;
import ca.edchipman.silverstripepdt.templates.SilverStripeTemplateStore;

import org.eclipse.swt.widgets.Label;

@SuppressWarnings("restriction")
public class NewSilverStripeClassWizardTemplatePage extends NewGenericFileTemplatesWizardPage {
	public static final String NEW_CLASS_CONTEXTTYPE = "ss_new_class_context"; //$NON-NLS-1$
	
	private ISelection selection;
	private NewSilverStripeClassWizardPage firstPage;
	private TableViewer fTableViewer;
	private GridData data;
	private SourceViewer fPatternViewer;
	private Object fProject;
	private TemplateStore fTemplateStore;
	
	/**
     * Create the wizard.
     */
    public NewSilverStripeClassWizardTemplatePage(final ISelection selection, NewSilverStripeClassWizardPage firstPage) {
        super("newSilverStripeClassWizardTemplatePage", "SilverStripe Class");
        setPageComplete(false);
        setTitle("SilverStripe Class");
        setDescription("Select a template to use as the intial content");
        
        setImageDescriptor(SilverStripePluginImages.DESC_ADD_SS_FILE);
        
        this.selection = selection;
        this.firstPage = firstPage;
    }
    
    public void createControl(Composite ancestor) {
    	Composite parent = new Composite(ancestor, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		parent.setLayout(layout);

		// create composite for Templates table
		Composite innerParent = new Composite(parent, SWT.NONE);
		GridLayout innerLayout = new GridLayout();
		innerLayout.numColumns = 2;
		innerLayout.marginHeight = 0;
		innerLayout.marginWidth = 0;
		innerParent.setLayout(innerLayout);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		innerParent.setLayoutData(gd);

		// Create linked text to just to templates preference page
		Link link = new Link(innerParent, SWT.NONE);
		link.setText(getTemplatesLocationMessage());
		data = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
		link.setLayoutData(data);
		link.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				linkClicked();
			}
		});

		// create table that displays templates
		Table table = new Table(innerParent, SWT.BORDER | SWT.FULL_SELECTION);

		data = new GridData(GridData.FILL_BOTH);
		data.widthHint = convertWidthInCharsToPixels(2);
		data.heightHint = convertHeightInCharsToPixels(10);
		data.horizontalSpan = 2;
		table.setLayoutData(data);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		TableLayout tableLayout = new TableLayout();
		table.setLayout(tableLayout);

		TableColumn column1 = new TableColumn(table, SWT.NONE);
		column1.setText(Messages.NewGenericFileTemplatesWizardPage_0);

		TableColumn column2 = new TableColumn(table, SWT.NONE);
		column2.setText(Messages.NewGenericFileTemplatesWizardPage_1);

		fTableViewer = new TableViewer(table);
		fTableViewer.setLabelProvider(new TemplateLabelProvider());
		fTableViewer.setContentProvider(new TemplateContentProvider());

		fTableViewer.setSorter(new ViewerSorter() {
			@Override
			public int compare(Viewer viewer, Object object1, Object object2) {
				if (object1 instanceof Template && object2 instanceof Template) {
					Template left = (Template) object1;
					Template right = (Template) object2;
					int result = left.getName().compareToIgnoreCase(
							right.getName());
					if (result != 0)
						return result;
					return left.getDescription().compareToIgnoreCase(
							right.getDescription());
				}
				return super.compare(viewer, object1, object2);
			}

			@Override
			public boolean isSorterProperty(Object element, String property) {
				return true;
			}
		});

		fTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent e) {
						updateViewerInput();
					}
				});

		// create viewer that displays currently selected template's contents
		fPatternViewer = doCreateViewer(parent);

		configureTableResizing(innerParent, table, column1, column2);

		String helpId = getNewFileWizardTemplatePageHelpId();
		if (helpId != null) {
			PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, helpId);
		}
		resetTableViewerInput();
		Dialog.applyDialogFont(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IPHPHelpContextIds.NEW);
		setControl(parent);
	}

	public void createFile(IProgressMonitor monitor) throws CoreException,InterruptedException {
		final String containerName = firstPage.getContainerName();
		final String fileName = firstPage.getFileName();
		this.resetTableViewerInput();
		
		IScriptProject project = null;
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(new Path(containerName));
		
		if (!resource.exists() || !(resource instanceof IContainer)) {
			project = DLTKCore.create(resource.getProject());
		}
		
		String lineSeparator = Util.getLineSeparator(null, project);
		final PHPTemplateStore.CompiledTemplate template=this.compileTemplate(containerName, fileName, lineSeparator);
		
		try {
			new FileCreator().createFile((Wizard) this.getWizard(), containerName, fileName, monitor, template.string, template.offset);
			
			saveLastSavedPreferences();
		} finally {
			monitor.done();
		}
	}

	/**
	 * Returns template string to insert.
	 * 
	 * @return String to insert or null if none is to be inserted
	 */
	public CompiledTemplate compileTemplate() {
		Template template = getSelectedTemplate();
		return PHPTemplateStore.compileTemplate(getTemplatesContextTypeRegistry(), template);
	}

	public CompiledTemplate compileTemplate(String containerName, String fileName) {
		Template template=getSelectedTemplate();
		return PHPTemplateStore.compileTemplate(getTemplatesContextTypeRegistry(), template, containerName, fileName);
	}

	public CompiledTemplate compileTemplate(String containerName, String fileName, String lineDelimiter) {
		Template template=getSelectedTemplate();
		return PHPTemplateStore.compileTemplate(getTemplatesContextTypeRegistry(), template, containerName,fileName, lineDelimiter);
	}
	
	protected String getTemplateContextTypeId() {
		return NewSilverStripeClassWizardTemplatePage.NEW_CLASS_CONTEXTTYPE;
	}
	
	protected String getUseTemplateMessage() {
		return "";
	}
	
	protected ContextTypeRegistry getTemplatesContextTypeRegistry() {
		return SilverStripePDTPlugin.getDefault().getNewClassContextRegistry();
	}

	protected String getTemplatesLocationMessage() {
		ContextTypeRegistry templateContextRegistry = getTemplatesContextTypeRegistry();
		TemplateContextType templateContextType = templateContextRegistry.getContextType(getTemplateContextTypeId());
		String name = templateContextType.getName();
		
		return NLS.bind("Templates are \"{0}\" found in the <a>SilverStripe Templates</a> preference page.", name);
	}

	protected String getPreferencePageId() {
		return "ca.edchipman.silverstripepdt.preferences.SilverStripeTemplatesPreferencePage"; //$NON-NLS-1$
	}

	protected IPreferenceStore getPreferenceStore() {
		return SilverStripePDTPlugin.getDefault().getPreferenceStore();
	}

	protected String getNewFileWizardTemplatePageHelpId() {
		return null;
	}
	
	public IProject getProject() {
        return firstPage.getProject();
    }
    
    public IScriptProject getScriptProject() {
        return firstPage.getScriptProject();
    }
    
    public boolean getIsCurrentPage() {
    	return this.isCurrentPage();
    }
	
	private void linkClicked() {
		String pageId = getPreferencePageId();
		PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(getShell(), pageId, new String[] { pageId }, null);
		dialog.open();
		fTableViewer.refresh();
	}

	private SourceViewer doCreateViewer(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(Messages.NewGenericFileTemplatesWizardPage_2);
		GridData data = new GridData();
		data.horizontalSpan = 2;
		label.setLayoutData(data);

		SourceViewer viewer = createViewer(parent);
		viewer.setEditable(false);

		Control control = viewer.getControl();
		data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 2;
		data.heightHint = convertHeightInCharsToPixels(5);
		control.setLayoutData(data);

		return viewer;
	}

	/**
	 * Creates, configures and returns a source viewer to present the template
	 * pattern on the preference page. Clients may override to provide a custom
	 * source viewer featuring e.g. syntax coloring.
	 * 
	 * @param parent
	 *            the parent control
	 * @return a configured source viewer
	 */
	private SourceViewer createViewer(Composite parent) {
		//Hack to wake up PDT's template store (for some reason this is needed)
		getTemplateStore();
		
		SourceViewerConfiguration sourceViewerConfiguration = new StructuredTextViewerConfiguration() {
			StructuredTextViewerConfiguration baseConfiguration = new PHPStructuredTextViewerConfiguration();

			@Override
			public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
				return baseConfiguration
						.getConfiguredContentTypes(sourceViewer);
			}

			@Override
			public LineStyleProvider[] getLineStyleProviders(ISourceViewer sourceViewer, String partitionType) {
				return baseConfiguration.getLineStyleProviders(sourceViewer, partitionType);
			}
		};
		
		SourceViewer viewer = new StructuredTextViewer(parent, null, null, false, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		((StructuredTextViewer) viewer).getTextWidget().setFont(JFaceResources.getFont("org.eclipse.wst.sse.ui.textfont")); //$NON-NLS-1$
		IStructuredModel scratchModel = StructuredModelManager.getModelManager().createUnManagedStructuredModelFor(ContentTypeIdForPHP.ContentTypeID_PHP);
		IDocument document = scratchModel.getStructuredDocument();
		viewer.configure(sourceViewerConfiguration);
		viewer.setDocument(document);
		
		return viewer;
	}

	/**
	 * Updates the pattern viewer.
	 */
	private void updateViewerInput() {
		Template template = getSelectedTemplate();
		if (template != null) {
			fPatternViewer.getDocument().set(template.getPattern());
		}
	}

	/**
	 * Get the currently selected template.
	 * 
	 * @return
	 */
	private Template getSelectedTemplate() {
		Template template = null;
		IStructuredSelection selection = (IStructuredSelection) fTableViewer.getSelection();

		if (selection.size() == 1) {
			template = (Template) selection.getFirstElement();
		}
		return template;
	}

	/**
	 * Correctly resizes the table so no phantom columns appear
	 * 
	 * @param parent
	 *            the parent control
	 * @param buttons
	 *            the buttons
	 * @param table
	 *            the table
	 * @param column1
	 *            the first column
	 * @param column2
	 *            the second column
	 * @param column3
	 *            the third column
	 */
	private void configureTableResizing(final Composite parent,
			final Table table, final TableColumn column1,
			final TableColumn column2) {
		parent.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				Rectangle area = parent.getClientArea();
				Point preferredSize = table.computeSize(SWT.DEFAULT,
						SWT.DEFAULT);
				int width = area.width - 2 * table.getBorderWidth();
				if (preferredSize.y > area.height) {
					// Subtract the scrollbar width from the total column
					// width
					// if a vertical scrollbar will be required
					Point vBarSize = table.getVerticalBar().getSize();
					width -= vBarSize.x;
				}

				Point oldSize = table.getSize();
				if (oldSize.x > width) {
					// table is getting smaller so make the columns
					// smaller first and then resize the table to
					// match the client area width
					column1.setWidth(width / 2);
					column2.setWidth(width / 2);
					table.setSize(width, area.height);
				} else {
					// table is getting bigger so make the table
					// bigger first and then make the columns wider
					// to match the client area width
					table.setSize(width, area.height);
					column1.setWidth(width / 2);
					column2.setWidth(width / 2);
				}
			}
		});
	}

	@Override
	protected ProjectTemplateStore getTemplateStore() {

		IProject project = getProject();

		ProjectTemplateStore templateStore;
		if (ProjectTemplateStore.hasProjectSpecificTempates(project)) {
			templateStore = new ProjectTemplateStore(project);
		} else {
			templateStore = new ProjectTemplateStore(null);
		}

		try {
			templateStore.load();
		} catch (IOException e) {
			// Ignore the error.
		}
		return templateStore;

	}
	
	public TemplateStore getSSTemplateStore() {
		TemplateStore templateStore = new SilverStripeTemplateStore(getTemplatesContextTypeRegistry(), getPreferenceStore(), "ca.edchipman.silverstripepdt.SilverStripe.classtemplates");
        
        try {
            templateStore.load();
        } catch (IOException e) {
            Logger.logException(e);
        }
        
        return templateStore;
    }
	
	public void resetTableViewerInput() {
		IProject newProject = getProject();
		if ((fProject == null && fProject != newProject) || (fProject != null && !fProject.equals(newProject))) {
			fProject = newProject;
			fTemplateStore = getSSTemplateStore();
			fTableViewer.setInput(fTemplateStore);
			loadLastSavedPreferences();
		}
	}
	
	/**
	 * Load the last template name used in New HTML File wizard.
	 */
	protected void loadLastSavedPreferences() {
		String templateName = getPreferenceStore().getString(PreferenceConstants.NEW_PHP_FILE_TEMPLATE);
		if (templateName == null || templateName.length() == 0) {
			fLastSelectedTemplateName = ""; //$NON-NLS-1$
		} else {
			fLastSelectedTemplateName = templateName;
		}
		
		setSelectedTemplate(fLastSelectedTemplateName);
	}

	/**
	 * Select a template in the table viewer given the template name. If
	 * template name cannot be found or templateName is null, just select first
	 * item in table. If no items in table select nothing.
	 * 
	 * @param templateName
	 */
	private void setSelectedTemplate(String templateName) {
		Object template = null;

		if (templateName != null && templateName.length() > 0) {
			// pick the last used template
			template = fTemplateStore.findTemplate(templateName, getTemplateContextTypeId());
		}

		// no record of last used template so just pick first element
		if (template == null) {
			// just pick first element
			template = fTableViewer.getElementAt(0);
		}

		if (template != null) {
			IStructuredSelection selection = new StructuredSelection(template);
			fTableViewer.setSelection(selection, true);
		}
	}
	
	/**
	 * Save template name used for next call to New HTML File wizard.
	 */
	private void saveLastSavedPreferences() {
		String templateName = ""; //$NON-NLS-1$

		Template template = getSelectedTemplate();
		if (template != null) {
			templateName = template.getName();
		}

		getPreferenceStore().setValue(PreferenceConstants.NEW_PHP_FILE_TEMPLATE, templateName);
	}
	
	/**
	 * Label provider for templates.
	 */
	private class TemplateLabelProvider extends LabelProvider implements ITableLabelProvider {

		/*
		 * @see
		 * org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java
		 * .lang.Object, int)
		 */
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		/*
		 * @see
		 * org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.
		 * lang.Object, int)
		 */
		public String getColumnText(Object element, int columnIndex) {
			Template template = (Template) element;

			switch (columnIndex) {
			case 0:
				return template.getName();
			case 1:
				return template.getDescription();
			default:
				return ""; //$NON-NLS-1$
			}
		}
	}
	
	/**
	 * Content provider for templates
	 */
	private class TemplateContentProvider implements IStructuredContentProvider {
		/** The template store. */
		private TemplateStore fStore;

		/*
		 * @see IContentProvider#dispose()
		 */
		public void dispose() {
			fStore = null;
		}

		/*
		 * @see IStructuredContentProvider#getElements(Object)
		 */
		public Object[] getElements(Object input) {
			String ssVersion=CorePreferencesSupport.getInstance().getProjectSpecificPreferencesValue(SilverStripePreferences.SILVERSTRIPE_VERSION, SilverStripeVersion.DEFAULT_VERSION, NewSilverStripeClassWizardTemplatePage.this.getProject());
			Object[] templates=fStore.getTemplates(NewSilverStripeClassWizardTemplatePage.this.getTemplateContextTypeId());
			List<Template> results=new ArrayList();
			
			for(int i=0;i<templates.length;i++) {
				
				if(templates[i] instanceof SilverStripeTemplate) {
					SilverStripeTemplate ssTemplate = (SilverStripeTemplate) templates[i];
					if(ssTemplate.ssVersions().length==0 || Arrays.asList(ssTemplate.ssVersions()).contains(ssVersion)) {
						results.add(ssTemplate);
					}
				}
			}
			
			return (Object[]) results.toArray(new Template[results.size()]);
		}

		/*
		 * @see IContentProvider#inputChanged(Viewer, Object, Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			fStore = (TemplateStore) newInput;
		}
	}
}