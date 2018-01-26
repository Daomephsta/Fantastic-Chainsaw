package leviathan143.fantasticchainsaw.generic.editors.language;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.viewers.IStructuredContentProvider;

import com.google.common.collect.Sets;

import leviathan143.fantasticchainsaw.FantasticPlugin;

public class LanguageModel
{
    public static final LanguageModelContentProvider CONTENT_PROVIDER = new LanguageModelContentProvider();

    private static final String PARSE_ESCAPES = "#PARSE_ESCAPES";

    private final IPositionUpdater posUpdater = new DefaultPositionUpdater(FantasticPlugin.NAME);
    private IDocument doc;

    private boolean parseEscapes;
    private Set<LanguageNode> nodes = Sets.newHashSet();

    public LanguageModel(IDocument doc)
    {
	this.doc = doc;
	doc.addPositionCategory(FantasticPlugin.NAME);
	doc.addPositionUpdater(posUpdater);
	try (BufferedReader reader = new BufferedReader(new StringReader(doc.get())))
	{
	    String line;
	    for (int l = 0; (line = reader.readLine()) != null; l++)
	    {
		int equalsPos = line.indexOf('=');
		if (equalsPos < 0) // Ignore comments and #PARSE_ESCAPES
		{
		    if (l == 0 && line.equals(PARSE_ESCAPES)) parseEscapes = true;
		}
		else
		{
		    IRegion lineInfo = doc.getLineInformation(l);
		    Position langKeyPos = new Position(lineInfo.getOffset(), equalsPos);
		    Position translationPos = new Position(lineInfo.getOffset() + equalsPos + 1,
			    line.length() - equalsPos - 1);
		    nodes.add(new LanguageNode(langKeyPos, translationPos));
		}
	    }
	}
	catch (IOException e)
	{
	    e.printStackTrace();
	}
	catch (BadLocationException e)
	{
	    e.printStackTrace();
	}
    }

    public LanguageModel(LanguageModel model)
    {
	nodes.addAll(model.nodes);
    }

    public boolean synchroniseKeys(LanguageModel target)
    {
	boolean changesMade = false;
	Set<String> keys = this.nodes.stream().map(LanguageNode::getLangKey).collect(Collectors.toSet());

	for (LanguageNode node : target.nodes)
	{
	    if (!keys.contains(node.getLangKey()))
	    {
		this.addEmptyNode().setLangKey(node.getLangKey());
		changesMade = true;
	    }
	}
	return changesMade;
    }

    public LanguageNode addEmptyNode()
    {
	try
	{
	    int offset = 0;
	    if (!nodes.isEmpty())
	    {
		offset = doc.getLineInformation(doc.getNumberOfLines() - 1).getOffset();
		doc.replace(offset, 0, "key=value" + System.lineSeparator());
	    }
	    else doc.set("key=value" + System.lineSeparator());

	    Position langKeyPos = new Position(offset, 3);
	    Position translationPos = new Position(offset + 4, 5);
	    LanguageNode node = new LanguageNode(langKeyPos, translationPos);
	    nodes.add(node);

	    return node;
	}
	catch (BadLocationException e)
	{
	    e.printStackTrace();
	}
	return null;
    }

    public void destroy()
    {
	try
	{
	    doc.removePositionCategory(FantasticPlugin.NAME);
	    doc.removePositionUpdater(posUpdater);
	}
	catch (BadPositionCategoryException e)
	{
	    e.printStackTrace();
	}
    }

    public void delete(LanguageNode node)
    {
	node.delete();
	nodes.remove(node);
    }

    public void toggleParseEscapes()
    {
	this.parseEscapes = !this.parseEscapes;
	try
	{
	    if (parseEscapes)
		doc.replace(0, 0, PARSE_ESCAPES + System.lineSeparator());
	    else doc.replace(0, doc.getLineLength(0), "");
	}
	catch (BadLocationException e)
	{
	    e.printStackTrace();
	}
    }

    public boolean shouldParseEscapes()
    {
	return parseEscapes;
    }

    private static class LanguageModelContentProvider implements IStructuredContentProvider
    {
	@Override
	public Object[] getElements(Object obj)
	{
	    Set<LanguageNode> langNodes = (Set<LanguageNode>) ((LanguageModel) obj).nodes;
	    return langNodes.toArray();
	}
    }

    public class LanguageNode
    {
	private Position langKeyPos;
	private Position translationPos;

	public LanguageNode(Position langKeyPos, Position translationPos)
	{
	    this.langKeyPos = langKeyPos;
	    this.translationPos = translationPos;
	    try
	    {
		doc.addPosition(FantasticPlugin.NAME, langKeyPos);
		doc.addPosition(FantasticPlugin.NAME, translationPos);
	    }
	    catch (BadLocationException | BadPositionCategoryException e)
	    {
		e.printStackTrace();
	    }
	}

	void delete()
	{
	    try
	    {
		int lineLen = doc.getLineLength(doc.getLineOfOffset(langKeyPos.getOffset()));
		doc.replace(langKeyPos.getOffset(), lineLen, "");
		doc.removePosition(FantasticPlugin.NAME, langKeyPos);
		langKeyPos.delete();
		doc.removePosition(FantasticPlugin.NAME, translationPos);
		translationPos.delete();
	    }
	    catch (BadLocationException | BadPositionCategoryException e)
	    {
		e.printStackTrace();
	    }
	}

	public void setTranslation(String translation)
	{
	    replaceString(translationPos, translation);
	}

	public String getTranslation()
	{
	    return getString(translationPos);
	}

	public void setLangKey(String langKey)
	{
	    replaceString(langKeyPos, langKey);
	}

	public String getLangKey()
	{
	    return getString(langKeyPos);
	}

	private String getString(Position pos)
	{
	    if (pos.isDeleted) return null;
	    try
	    {
		return doc.get(pos.getOffset(), pos.getLength());
	    }
	    catch (BadLocationException e)
	    {
		e.printStackTrace();
	    }
	    return null;
	}

	private void replaceString(Position pos, String replacement)
	{
	    if (pos.isDeleted) return;
	    try
	    {
		doc.replace(pos.getOffset(), pos.getLength(), replacement);
	    }
	    catch (BadLocationException e)
	    {
		e.printStackTrace();
	    }
	}
    }
}
