package leviathan143.fantasticchainsaw.generic.editors.language;

import java.io.ByteArrayInputStream;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.wb.swt.SWTResourceManager;

import com.google.common.collect.Maps;

import leviathan143.fantasticchainsaw.base.SearchableListDialog;
import leviathan143.fantasticchainsaw.generic.editors.language.LanguageModel.LanguageNode;
import leviathan143.fantasticchainsaw.i18n.LanguageFileEditorMessages;
import leviathan143.fantasticchainsaw.util.ISelectionListener;
import leviathan143.fantasticchainsaw.util.SWTHelper;
import leviathan143.fantasticchainsaw.util.SWTHelper.IDeletionHandler;

public class LocalisationEditor extends EditorPart
{
	private IDocumentProvider docProvider;
	private LanguageModel model;
	// GUI elements
	private TableViewer tableViewer;
	private ToolItem parseEscapes;

	public LocalisationEditor(IDocumentProvider docProvider)
	{
		this.docProvider = docProvider;
	}

	@Override
	public void doSave(IProgressMonitor monitor)
	{}

	@Override
	public void doSaveAs()
	{}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException
	{
		this.setSite(site);
		this.setInput(input);
		model = new LanguageModel(docProvider.getDocument(input));
	}

	@Override
	public boolean isDirty()
	{
		return false;
	}

	@Override
	public boolean isSaveAsAllowed()
	{
		return false;
	}

	@Override
	public void createPartControl(Composite parent)
	{
		parent.setLayout(new GridLayout(1, false));
		ToolBar toolBar = new ToolBar(parent, SWT.FLAT | SWT.RIGHT);
		toolBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		createLanguageDropdown(toolBar);

		parseEscapes = new ToolItem(toolBar, SWT.CHECK);
		parseEscapes.addSelectionListener((ISelectionListener) e -> model.toggleParseEscapes());
		parseEscapes.setImage(SWTResourceManager.getImage(LocalisationEditor.class, "/icons/editors/parseEscapes.png"));
		parseEscapes.setToolTipText(LanguageFileEditorMessages.parseEscapesTooltip);
		parseEscapes.setSelection(model.shouldParseEscapes());

		ToolItem addEntry = new ToolItem(toolBar, SWT.PUSH);
		addEntry.addSelectionListener((ISelectionListener) e ->
		{
			model.addEmptyNode();
			tableViewer.refresh();
		});
		addEntry.setImage(SWTResourceManager.getImage(LocalisationEditor.class, "/icons/editors/addEntry.png"));
		addEntry.setToolTipText(LanguageFileEditorMessages.addEntryTooltip);

		ToolItem removeEntry = new ToolItem(toolBar, SWT.PUSH);
		removeEntry.addSelectionListener((ISelectionListener) e -> SWTHelper.deleteSelectedEntries(tableViewer,
				(IDeletionHandler<LanguageNode>) node -> model.delete(node)));
		removeEntry.setImage(SWTResourceManager.getImage(LocalisationEditor.class, "/icons/editors/removeEntry.png"));
		removeEntry.setToolTipText(LanguageFileEditorMessages.removeEntryTooltip);

		ToolItem synchroniseKeys = new ToolItem(toolBar, SWT.PUSH);
		synchroniseKeys.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				IFile currentFile = getEditorInput().getAdapter(IFile.class);

				FileDialog targetSelector = new FileDialog(parent.getShell());
				targetSelector.setFilterExtensions(new String[]{"*.lang"});
				targetSelector.setFilterPath(currentFile.getLocation().removeLastSegments(1).toString());
				String result = targetSelector.open();
				if (result == null) return;

				IFile target = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(result));
				IDocumentProvider targetDocProvider = new TextFileDocumentProvider();
				try
				{
					targetDocProvider.connect(target);
					LanguageModel targetModel = new LanguageModel(targetDocProvider.getDocument(target));
					if (model.synchroniseKeys(targetModel)) targetDocProvider.changed(target);
					targetDocProvider.disconnect(target);
				}
				catch (CoreException core)
				{
					core.printStackTrace();
				}
				tableViewer.refresh();
			}
		});
		synchroniseKeys
				.setImage(SWTResourceManager.getImage(LocalisationEditor.class, "/icons/editors/synchroniseKeys.png"));
		synchroniseKeys.setToolTipText(LanguageFileEditorMessages.synchroniseKeysTooltip);

		createTableViewer(parent);
	}

	private void createLanguageDropdown(ToolBar toolBar)
	{
		IFile currentFile = getEditorInput().getAdapter(IFile.class);

		ToolItem languageDropdown = new ToolItem(toolBar, SWT.PUSH);
		languageDropdown.setText(Language.getByLangCode(getFileNameNoExtension(currentFile)).getDisplayName());
		languageDropdown.setToolTipText(LanguageFileEditorMessages.selectLanguageTooltip);
		languageDropdown.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				SearchableListDialog<Language> dialog = new SearchableListDialog<Language>(toolBar.getShell(),
						new LabelProvider()
						{
							@Override
							public String getText(Object element)
							{
								return ((Language) element).getDisplayName();
							}
						}, Language.values());
				dialog.setTitle(LanguageFileEditorMessages.selectLanguageTooltip);
				dialog.open();

				Language language = dialog.getResult();
				if (language != null)
				{
					languageDropdown.setText(language.getDisplayName());

					IPath newFilePath = currentFile.getLocation().removeLastSegments(1)
							.append(language.getLangCode() + ".lang");
					IFile newFile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(newFilePath);

					try
					{
						if (!newFile.exists())
							newFile.create(new ByteArrayInputStream(new byte[0]), IResource.NONE, null);

						getEditorSite().getPage().closeEditor(getEditorSite().getPage().getActiveEditor(), true);
						getEditorSite().getPage().openEditor(new FileEditorInput(newFile),
								"leviathan143.fantasticchainsaw.generic.editors.language.LanguageFileEditor");
					}
					catch (CoreException core)
					{
						core.printStackTrace();
					}
				}
			}
		});

	}

	private void createTableViewer(Composite parent)
	{
		tableViewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		tableViewer.setContentProvider(LanguageModel.CONTENT_PROVIDER);
		tableViewer.setInput(model);
		Table table = tableViewer.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		SWTHelper.addDeletionSupport(tableViewer, (IDeletionHandler<LanguageNode>) node -> model.delete(node));
		SWTHelper.setDoubleClickToEdit(tableViewer);
		createTableColumns(parent);
	}

	private void createTableColumns(Composite parent)
	{
		TableViewerColumn langKeyColumn = SWTHelper.createColumn(tableViewer,
				LanguageFileEditorMessages.langKeyClmnTitle, 200);
		langKeyColumn.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				return ((LanguageNode) element).getLangKey();
			}
		});
		langKeyColumn.setEditingSupport(new LanguageKeyEditingSupport(tableViewer));

		TableViewerColumn translationColumn = SWTHelper.createColumn(tableViewer,
				LanguageFileEditorMessages.translationClmnTitle, 400);
		translationColumn.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				return ((LanguageNode) element).getTranslation();
			}
		});
		translationColumn.setEditingSupport(new TranslationEditingSupport(tableViewer));
	}

	public void reparseModel()
	{
		model.destroy();
		model = new LanguageModel(docProvider.getDocument(getEditorInput()));
		tableViewer.setInput(model);
		tableViewer.refresh();
		parseEscapes.setSelection(model.shouldParseEscapes());
	}

	@Override
	public void setFocus()
	{}

	private String getFileNameNoExtension(IFile file)
	{
		return file.getName().replace("." + file.getFileExtension(), "");
	}

	public static enum Language
	{
		Afrikaans("af_za", "Afrikaans"),
		Arabic("ar_sa", "اللغة العربية"),
		Asturian("ast_es", "Asturianu"),
		Azerbaijani("az_az", "Azərbaycanca"),
		Belarusian("be_by", "Беларуская"),
		Bulgarian("bg_bg", "Български"),
		Breton("br_fr", "Brezhoneg"),
		Catalan("ca_es", "Català"),
		Czech("cs_cz", "Čeština"),
		Welsh("cy_gb", "Cymraeg"),
		Danish("da_dk", "Dansk"),
		Austrian_German("de_at", "Österreichisches Deutsch"),
		German("de_de", "Deutsch"),
		Greek("el_gr", "Ελληνικά"),
		Australian_English("en_au", "English (Australia)"),
		Canadian_English("en_ca", "Canadian English"),
		British_English("en_gb", "English (UK)"),
		New_Zealand_English("en_nz", "New Zealand English"),
		British_English_upside_down("en_ud", "ɥsᴉꞁᵷuƎ (ɯopᵷuᴉꞰ pǝʇᴉu∩)"),
		Pirate_English("en_7s", "Pirate Speak"),
		American_English("en_us", "English (US)"),
		Esperanto("eo_uy", "Esperanto"),
		Argentinian_Spanish("es_ar", "Español (Argentina)"),
		Spanish("es_es", "Español (España)"),
		Mexican_Spanish("es_mx", "Español (México)"),
		Uruguayan_Spanish("es_uy", "Español (Uruguay)"),
		Venezuelan_Spanish("es_ve", "Español (Venezuela)"),
		Estonian("et_ee", "Eesti"),
		Basque("eu_es", "Euskara"),
		Persian("fa_ir", "فارسی"),
		Finnish("fi_fi", "Suomi"),
		Filipino("fil_ph", "Filipino"),
		Faroese("fo_fo", "Føroyskt"),
		French("fr_fr", "Français"),
		Canadian_French("fr_ca", "Français québécois"),
		Frisian("fy_nl", "Frysk"),
		Irish("ga_ie", "Gaeilge"),
		Scottish_Gaelic("gd_gb", "Gàidhlig"),
		Galician("gl_es", "Galego"),
		Manx("gv_im", "Gaelg"),
		Hawaiian("haw", "ʻŌlelo Hawaiʻi"),
		Hebrew("he_il", "עברית"),
		Hindi("hi_in", "हिन्दी"),
		Croatian("hr_hr", "Hrvatski"),
		Hungarian("hu_hu", "Magyar"),
		Armenian("hy_am", "Հայերեն"),
		Indonesian("id_id", "Bahasa Indonesia"),
		Icelandic("is_is", "Íslenska"),
		Ido("io", "Ido"),
		Italian("it_it", "Italiano"),
		Japanese("ja_jp", "日本語"),
		Lojban("jbo_en", "la .lojban."),
		Georgian("ka_ge", "ქართული"),
		Korean("ko_kr", "한국어"),
		Kolsch_Ripuarian("ksh_de", "Kölsch/Ripoarisch"),
		Cornish("kw_gb", "Kernewek"),
		Latin("la_va", "Latina"),
		Luxembourgish("lb_lu", "Lëtzebuergesch"),
		Limburgish("li_li", "Limburgs"),
		LOLCAT("lol_us", "LOLCAT"),
		Lithuanian("lt_lt", "Lietuvių"),
		Latvian("lv_lv", "Latviešu"),
		Maori("mi_nz", "Te Reo Māori"),
		Macedonian("mk_mk", "Македонски"),
		Mongolian("mn_mn", "Монгол"),
		Malay("ms_my", "Bahasa Melayu"),
		Maltese("mt_mt", "Malti"),
		Low_German("nds_de", "Platdüütsk"),
		Dutch("nl_nl", "Nederlands"),
		Norwegian_Nynorsk("nn_no", "Norsk Nynorsk"),
		Norwegian("no_no", "Norsk"),
		Occitan("oc_fr", "Occitan"),
		Polish("pl_pl", "Polski"),
		Brazilian_Portuguese("pt_br", "Português (Brasil)"),
		Portuguese("pt_pt", "Português (Portugal)"),
		Quenya_Form_of_Elvish_from_LOTR("qya_aa", "Quenya"),
		Romanian("ro_ro", "Română"),
		Russian("ru_ru", "Русский"),
		Northern_Sami("sme", "Davvisámegiella"),
		Slovak("sk_sk", "Slovenčina"),
		Slovenian("sl_si", "Slovenščina"),
		Somali("so_so", "Af-Soomaali"),
		Albanian("sq_al", "Shqip"),
		Serbian("sr_sp", "Српски"),
		Swedish("sv_se", "Svenska"),
		Swabian_German("swg_de", "Oschdallgaierisch"),
		Thai("th_th", "ภาษาไทย"),
		Tagalog("tl_ph", "Filipino"),
		Klingon("tlh_aa", "tlhIngan Hol"),
		Turkish("tr_tr", "Türkçe"),
		Talossan("tzl_tzl", "Talossan"),
		Ukrainian("uk_ua", "Українська"),
		Valencian("ca-val_es", "Valencià"),
		Vietnamese("vi_vn", "Tiếng Việt"),
		Chinese_Simplified("zh_cn", "简体中文"),
		Chinese_Traditional("zh_tw", "繁體中文");
		
		private static final Map<String, Language> LANGCODE_TO_LANG = Maps.newHashMap();
		static
		{
			for (Language language : Language.values())
			{
				LANGCODE_TO_LANG.put(language.langCode, language);
			}
		}

		private final String displayName;
		private final String langCode;

		private Language(String langCode, String displayName)
		{
			this.langCode = langCode;
			this.displayName = displayName + " - " + this.langCode;
		}

		public static Language getByLangCode(String langCode)
		{
			return LANGCODE_TO_LANG.get(langCode);
		}

		public String getDisplayName()
		{
			return displayName;
		}

		public String getLangCode()
		{
			return langCode;
		}
	}
}
