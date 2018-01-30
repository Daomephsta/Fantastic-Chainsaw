package leviathan143.fantasticchainsaw.generic.wizards;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.lang.model.SourceVersion;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import leviathan143.fantasticchainsaw.Versioning;
import leviathan143.fantasticchainsaw.Versioning.Version;
import leviathan143.fantasticchainsaw.i18n.ForgeModWizardMessages;
import leviathan143.fantasticchainsaw.interfaces.forgemaven.ForgeMavenInterface;
import leviathan143.fantasticchainsaw.interfaces.mcpbot.MCPBotInterface;
import leviathan143.fantasticchainsaw.util.ISelectionListener;

public class ForgeSetupPage extends WizardPage implements Listener
{
	private final ForgeSetupData forgeSetupData;
	private final ModInfoData modInfo;

	private Combo comboMinecraftVersion;
	private Combo comboForgeVersion;
	private Combo comboMappings;
	private Text textBaseName;
	private Text textModID;
	private Text textModName;
	private Text textModVersion;
	private Text textCredits;
	private Text textAuthors;
	private Text textLogoPath;
	private Text textGroupID;
	private Text textDescription;

	protected ForgeSetupPage()
	{
		super("Forge Setup");
		this.forgeSetupData = new ForgeSetupData();
		this.modInfo = new ModInfoData();
		setPageComplete(false);
	}

	@Override
	public void createControl(Composite parent)
	{
		Composite forgeComposite = new Composite(parent, SWT.NONE);
		GridLayout compositeLayout = new GridLayout(1, false);
		compositeLayout.marginWidth = 10;
		compositeLayout.marginHeight = 10;
		forgeComposite.setLayout(compositeLayout);

		setControl(forgeComposite);

		Group forgeGroup = new Group(forgeComposite, SWT.NONE);
		GridData gd_forgeGroup = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_forgeGroup.widthHint = 534;
		forgeGroup.setLayoutData(gd_forgeGroup);
		GridLayout forgeGroupLayout = new GridLayout(2, false);
		forgeGroupLayout.marginWidth = 10;
		forgeGroupLayout.marginHeight = 10;
		forgeGroup.setLayout(forgeGroupLayout);
		compositeLayout.marginWidth = 10;
		compositeLayout.marginHeight = 10;
		forgeGroup.setText("Forge");

		Label lblGroupID = new Label(forgeGroup, SWT.NONE);
		lblGroupID.setText(ForgeModWizardMessages.groupIdLabelText);

		textGroupID = new Text(forgeGroup, SWT.BORDER);
		textGroupID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textGroupID.addListener(SWT.Modify, this);

		Label lblBaseName = new Label(forgeGroup, SWT.NONE);
		lblBaseName.setText(ForgeModWizardMessages.baseNameLabelText);

		textBaseName = new Text(forgeGroup, SWT.BORDER);
		textBaseName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textBaseName.addListener(SWT.Modify, this);

		Label lblMinecraftVersion = new Label(forgeGroup, SWT.NONE);
		lblMinecraftVersion.setText(ForgeModWizardMessages.mcVersionLabelText);

		comboMinecraftVersion = new Combo(forgeGroup, SWT.READ_ONLY);
		comboMinecraftVersion.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		comboMinecraftVersion.addListener(SWT.Selection, this);
		for (Version mcVer : ForgeMavenInterface.getAvailableMCVersions())
			comboMinecraftVersion.add(mcVer.toString());

		Label lblForgeVersion = new Label(forgeGroup, SWT.NONE);
		lblForgeVersion.setText(ForgeModWizardMessages.forgeVersionLabelText);

		comboForgeVersion = new Combo(forgeGroup, SWT.READ_ONLY);
		comboForgeVersion.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		comboForgeVersion.addListener(SWT.Selection, this);

		Label lblMappingsVersion = new Label(forgeGroup, SWT.NONE);
		lblMappingsVersion.setText(ForgeModWizardMessages.mappingsLabelText);

		comboMappings = new Combo(forgeGroup, SWT.READ_ONLY);
		GridData gd_comboMappings = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		gd_comboMappings.heightHint = 54;
		comboMappings.setLayoutData(gd_comboMappings);
		comboMappings.addListener(SWT.Selection, this);

		Group modInfoGroup = new Group(forgeComposite, SWT.NONE);
		GridLayout gl_modInfoGroup = new GridLayout(3, false);
		gl_modInfoGroup.marginWidth = 10;
		gl_modInfoGroup.marginHeight = 10;
		modInfoGroup.setLayout(gl_modInfoGroup);
		GridData gd_modInfoGroup = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_modInfoGroup.heightHint = 357;
		modInfoGroup.setLayoutData(gd_modInfoGroup);
		modInfoGroup.setText(ForgeModWizardMessages.modInfoGroupTitle);

		Label lblModID = new Label(modInfoGroup, SWT.NONE);
		lblModID.setText(ForgeModWizardMessages.modIDLabelText);

		textModID = new Text(modInfoGroup, SWT.BORDER);
		textModID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		textModID.addListener(SWT.Modify, this);

		Label lblModName = new Label(modInfoGroup, SWT.NONE);
		lblModName.setText(ForgeModWizardMessages.modNameLabelText);

		textModName = new Text(modInfoGroup, SWT.BORDER);
		textModName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		textModName.addListener(SWT.Modify, this);

		Label lblModVersion = new Label(modInfoGroup, SWT.NONE);
		lblModVersion.setText(ForgeModWizardMessages.modVersionlabelText);

		textModVersion = new Text(modInfoGroup, SWT.BORDER);
		textModVersion.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		textModVersion.addListener(SWT.Modify, this);

		Label lblDescription = new Label(modInfoGroup, SWT.NONE);
		lblDescription.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		lblDescription.setText(ForgeModWizardMessages.descriptionLabelText);

		textDescription = new Text(modInfoGroup, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
		GridData gd_textDescription = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		gd_textDescription.heightHint = 82;
		textDescription.setLayoutData(gd_textDescription);
		textDescription.addListener(SWT.Modify, this);

		Label lblAuthors = new Label(modInfoGroup, SWT.NONE);
		lblAuthors.setText(ForgeModWizardMessages.authorsLabelText);

		textAuthors = new Text(modInfoGroup, SWT.BORDER);
		textAuthors.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		textAuthors.addListener(SWT.Modify, this);

		Label lblCredits = new Label(modInfoGroup, SWT.NONE);
		lblCredits.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		lblCredits.setText(ForgeModWizardMessages.creditsLabelText);

		textCredits = new Text(modInfoGroup, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
		GridData gd_textCredits = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		gd_textCredits.heightHint = 84;
		textCredits.setLayoutData(gd_textCredits);
		textCredits.addListener(SWT.Modify, this);

		Label lblLogo = new Label(modInfoGroup, SWT.NONE);
		lblLogo.setText(ForgeModWizardMessages.logoPathLabelText);

		textLogoPath = new Text(modInfoGroup, SWT.BORDER);
		textLogoPath.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textLogoPath.addListener(SWT.Modify, this);

		Button btnBrowseLogo = new Button(modInfoGroup, SWT.NONE);
		btnBrowseLogo.setText(ForgeModWizardMessages.btnBrowseText);

		comboMinecraftVersion.addSelectionListener((ISelectionListener) e ->
		{
			String mcVersion = comboMinecraftVersion.getText();
			comboForgeVersion.setItems(ForgeMavenInterface.getAvailableForgeVersions(mcVersion).toArray(new String[0]));
			for (String forgeVer : ForgeMavenInterface.getAvailableForgeVersions(mcVersion))
				comboForgeVersion.add(forgeVer);
			comboMappings.setItems(MCPBotInterface.getAvailableMappings(mcVersion));
		});

		validatePage();
	}

	@Override
	public void handleEvent(Event e)
	{
		boolean isPageValid = validatePage();
		if (isPageValid) setMessage("");
		setPageComplete(isPageValid);
	}

	private boolean validatePage()
	{
		if (textGroupID.getText().isEmpty())
		{
			setFormattedMessage(IMessageProvider.NONE, ForgeModWizardMessages.infoFieldRequired,
					ForgeModWizardMessages.groupIdLabelText);
			return false;
		}
		else if (!SourceVersion.isName(textGroupID.getText()))
		{
			setFormattedMessage(IMessageProvider.ERROR, ForgeModWizardMessages.errorInvalidGroupID,
					textGroupID.getText());
			return false;
		}
		if (textBaseName.getText().isEmpty())
		{
			setFormattedMessage(IMessageProvider.NONE, ForgeModWizardMessages.infoFieldRequired,
					ForgeModWizardMessages.baseNameLabelText);
			return false;
		}
		if (comboMinecraftVersion.getText().isEmpty())
		{
			setFormattedMessage(IMessageProvider.NONE, ForgeModWizardMessages.infoFieldRequired,
					ForgeModWizardMessages.mcVersionLabelText);
			return false;
		}
		if (comboForgeVersion.getText().isEmpty())
		{
			setFormattedMessage(IMessageProvider.NONE, ForgeModWizardMessages.infoFieldRequired,
					ForgeModWizardMessages.forgeVersionLabelText);
			return false;
		}
		if (comboMappings.getText().isEmpty())
		{
			setFormattedMessage(IMessageProvider.NONE, ForgeModWizardMessages.infoFieldRequired,
					ForgeModWizardMessages.mappingsLabelText);
			return false;
		}
		if (textModID.getText().isEmpty())
		{
			setFormattedMessage(IMessageProvider.NONE, ForgeModWizardMessages.infoFieldRequired,
					ForgeModWizardMessages.modIDLabelText);
			return false;
		}
		else if (!comboMinecraftVersion.getText().isEmpty())
		{
			boolean is1_11plus = new Versioning.Version(comboMinecraftVersion.getText())
					.compareTo(Versioning.V1_11) >= 0;
			if (is1_11plus && !textModID.getText().equals(textModID.getText().toLowerCase()))
			{
				setFormattedMessage(IMessageProvider.ERROR, ForgeModWizardMessages.errorNonLowerCaseModID);
				return false;
			}
			if (comboMinecraftVersion.getText().length() > 64)
			{
				setFormattedMessage(IMessageProvider.ERROR, ForgeModWizardMessages.errorModIDTooLong);
				return false;
			}
		}
		if (textModName.getText().isEmpty())
		{
			setFormattedMessage(IMessageProvider.NONE, ForgeModWizardMessages.infoFieldRequired,
					ForgeModWizardMessages.modNameLabelText);
			return false;
		}
		if (textModVersion.getText().isEmpty())
		{
			setFormattedMessage(IMessageProvider.NONE, ForgeModWizardMessages.infoFieldRequired,
					ForgeModWizardMessages.modVersionlabelText);
			return false;
		}
		if (!textLogoPath.getText().isEmpty())
		{
			Path logoPath = new File(textLogoPath.getText()).toPath();
			if (!Files.exists(logoPath))
			{
				setFormattedMessage(IMessageProvider.ERROR, ForgeModWizardMessages.errorInvalidLogoPath,
						logoPath.toAbsolutePath());
				return false;
			}
		}
		return true;
	}

	private void setFormattedMessage(int type, String format, Object... args)
	{
		String formatted = String.format(format, args);
		if (!formatted.equals(getMessage())) setMessage(formatted, type);
	}

	public void populateSetupData()
	{
		forgeSetupData.groupID = textGroupID.getText();
		forgeSetupData.baseName = textBaseName.getText();
		forgeSetupData.forgeVersion = comboForgeVersion.getText();
		forgeSetupData.mappings = comboMappings.getText();
		forgeSetupData.mcVersion = comboMinecraftVersion.getText();
		forgeSetupData.modVersion = textModVersion.getText();
	}

	public void populateModInfo()
	{
		modInfo.modID = textModID.getText();
		modInfo.name = textModName.getText();
		modInfo.description = textDescription.getText();
		modInfo.logoPath = textLogoPath.getText();
		modInfo.authors = textAuthors.getText();
		modInfo.credits = textCredits.getText();
		modInfo.mcVersion = comboMinecraftVersion.getText();
		modInfo.modVersion = textModVersion.getText();
	}

	public ForgeSetupData getForgeSetupData()
	{
		populateSetupData();
		return forgeSetupData;
	}

	public ModInfoData getModInfoData()
	{
		populateModInfo();
		return modInfo;
	}

	public static class ForgeSetupData
	{
		private String groupID, baseName, forgeVersion, mappings, modVersion, mcVersion;

		public ForgeSetupData()
		{
			groupID = baseName = mcVersion = forgeVersion = mappings = "";
		}

		public String getGroupID()
		{
			return groupID;
		}

		public String getBaseName()
		{
			return baseName;
		}

		public String getForgeVersion()
		{
			return forgeVersion;
		}

		public String getMappings()
		{
			return mappings;
		}

		public String getModVersion()
		{
			return modVersion;
		}

		public String getMCVersion()
		{
			return mcVersion;
		}
	}

	public static class ModInfoData
	{
		private String modID, name, description, logoPath, authors, credits, modVersion, mcVersion;

		public ModInfoData()
		{
			modID = name = description = logoPath = authors = credits = "";
		}

		public String getModID()
		{
			return modID;
		}

		public String getName()
		{
			return name;
		}

		public String getDescription()
		{
			return description;
		}

		public String getLogoPath()
		{
			return logoPath;
		}

		public String getAuthors()
		{
			return authors;
		}

		public String getCredits()
		{
			return credits;
		}

		public String getModVersion()
		{
			return modVersion;
		}

		public String getMcVersion()
		{
			return mcVersion;
		}
	}
}
