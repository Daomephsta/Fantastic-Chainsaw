package leviathan143.fantasticchainsaw.i18n;

import org.eclipse.osgi.util.NLS;

public class LanguageFileEditorMessages extends NLS
{
	private static final String BUNDLE_NAME = "leviathan143.fantasticchainsaw.i18n.languageFileEditor"; //$NON-NLS-1$

	static
	{
		NLS.initializeMessages(BUNDLE_NAME, LanguageFileEditorMessages.class);
	}

	public static String locEditorTitle;
	public static String textEditorTitle;
	public static String langKeyClmnTitle;
	public static String translationClmnTitle;
	public static String selectLanguageTooltip;
	public static String parseEscapesTooltip;
	public static String addEntryTooltip;
	public static String removeEntryTooltip;
	public static String synchroniseKeysTooltip;
}
