package leviathan143.fantasticchainsaw.mc18.wizards.model;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import leviathan143.fantasticchainsaw.MetadataHandler;
import leviathan143.fantasticchainsaw.base.ArchiveFileDialog;
import leviathan143.fantasticchainsaw.base.ObservableValue;
import leviathan143.fantasticchainsaw.i18n.ModelWizardMessages;
import leviathan143.fantasticchainsaw.interfaces.forgemaven.ForgeMavenInterface;
import leviathan143.fantasticchainsaw.interfaces.minecraft.MCInterface;
import leviathan143.fantasticchainsaw.interfaces.minecraft.MCResourceRepositoryAggregate;
import leviathan143.fantasticchainsaw.interfaces.minecraft.model.JSONModel;
import leviathan143.fantasticchainsaw.mc18.wizards.model.ModelParameterModel.TextureVariable;
import leviathan143.fantasticchainsaw.util.AnySelectionListener;
import leviathan143.fantasticchainsaw.util.EclipseHelper;
import leviathan143.fantasticchainsaw.util.NIOHelper;
import leviathan143.fantasticchainsaw.util.SWTHelper;
import leviathan143.fantasticchainsaw.util.text.TemplateEngine;

public class ModelParameterPage extends WizardPage implements Observer 
{
    private File forgeJar;
    private MCResourceRepositoryAggregate mcResources;
    private ModelParameterModel parameterModel;
    private TemplateLocationType templateLocationType;
    private URI templateURI;
    private JSONModel templateModel;

    private Text templatePathField;
    private Group parameterGroup;
    private Button showVarsWithDefaultValues;
    private TableViewer parameterTable;

    public ModelParameterPage() 
    {
	super("parameters");
	this.parameterModel = new ModelParameterModel();
    }

    public void createControl(Composite parent) 
    {
	Composite container = new Composite(parent, SWT.NONE);

	setControl(container);
	container.setLayout(new GridLayout(1, false));

	Group templateGroup = new Group(container, SWT.NONE);
	templateGroup.setText(ModelWizardMessages.selectTemplate);
	templateGroup.setLayout(new GridLayout(2, false));
	GridData gd_templateGroup = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
	gd_templateGroup.heightHint = 80;
	templateGroup.setLayoutData(gd_templateGroup);

	Button templateFromArchive = new Button(templateGroup, SWT.RADIO);
	templateFromArchive.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
	templateFromArchive.addSelectionListener(new AnySelectionListener() 
	{	
	    @Override public void handleSelection(SelectionEvent e) { changeTemplateType(TemplateLocationType.ARCHIVE); }
	});
	templateFromArchive.setText(ModelWizardMessages.useJSONFromForgeJarAsTemplate);

	Button templateFromFileSystem = new Button(templateGroup, SWT.RADIO);
	templateFromFileSystem.addSelectionListener(new AnySelectionListener() 
	{	
	    @Override public void handleSelection(SelectionEvent e) { changeTemplateType(TemplateLocationType.FILESYSTEM); }
	});
	templateFromFileSystem.setText(ModelWizardMessages.useJSONFromFilesystemAsTemplate);
	templateFromFileSystem.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));

	templatePathField = new Text(templateGroup, SWT.BORDER);
	templatePathField.addModifyListener(event ->
	{
	    try
	    {
		String templateURIString = templatePathField.getText();
		templateURI = new URI(templateURIString);
		if(templateURI.getScheme() == null) return;
		Path jarPath = Paths.get(templateURIString.substring(10, templateURIString.indexOf('!')));
		if(!Files.exists(jarPath)) return;

		setPageComplete(true);
		
		try(FileSystem fs = NIOHelper.getOrCreateFileSystem(new URI(templateURIString.substring(0, templateURIString.indexOf('!')))))
		{
		    Path templatePath = Paths.get(templateURI);
		    if(!Files.exists(templatePath)) return;

		    templateModel = MCInterface.getJavaRepresentation(mcResources, templatePath);
		    parameterModel.parse(templateModel);
		    parameterTable.refresh();
		}
	    }
	    catch (URISyntaxException uriSyntax)
	    {
		MessageBox msg = new MessageBox(Display.getCurrent().getActiveShell());
		msg.setMessage(uriSyntax.getMessage());
		msg.open();
	    }
	    catch (IOException io)
	    {
		io.printStackTrace();
	    }
	});
	templatePathField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

	Button browseButton = new Button(templateGroup, SWT.NONE);
	browseButton.addSelectionListener(new SelectionAdapter() 
	{
	    @Override
	    public void widgetSelected(SelectionEvent e) 
	    {
		String templateURIString = "";
		if(templateLocationType == TemplateLocationType.FILESYSTEM)
		{
		    FileDialog dialog = new FileDialog(parent.getShell());
		    dialog.setText(ModelWizardMessages.selectTemplate);
		    dialog.setFilterExtensions(new String[] {"*.json", "*.template"});
		    String result = dialog.open();
		    if(result != null) templateURIString = Paths.get(result).toUri().toString();
		}
		else if(templateLocationType == TemplateLocationType.ARCHIVE)
		{
		    FileDialog dialog = new FileDialog(parent.getShell());
		    dialog.setFilterPath(forgeJar.getParent());
		    dialog.setFileName(forgeJar.getName());
		    dialog.setText(ModelWizardMessages.selectArchive);
		    dialog.setFilterExtensions(new String[] {"*.jar"});
		    String path = dialog.open();

		    if(path != null)
		    {
			ArchiveFileDialog archiveDialog = new ArchiveFileDialog(parent.getShell(), Paths.get(path));
			archiveDialog.setFilterPath("/assets");
			archiveDialog.setText(ModelWizardMessages.selectTemplate);
			archiveDialog.setFilterExtensions(new String[] {".json", ".template"});
			archiveDialog.open();

			templateURIString = "jar:file:/" + path.replace('\\', '/') + "!" + archiveDialog.getResult();
		    }
		}
		templatePathField.setText(templateURIString != null ? templateURIString : "");
	    }
	});
	browseButton.setText(ModelWizardMessages.browse);

	parameterGroup = new Group(container, SWT.NONE);
	parameterGroup.setText(ModelWizardMessages.modelParameters);
	parameterGroup.setLayout(new GridLayout(1, false));
	parameterGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

	showVarsWithDefaultValues = new Button(parameterGroup, SWT.CHECK);
	showVarsWithDefaultValues.addSelectionListener(new SelectionAdapter() 
	{
	    @Override
	    public void widgetSelected(SelectionEvent e) {parameterTable.refresh();}
	});
	showVarsWithDefaultValues.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
	showVarsWithDefaultValues.setText(ModelWizardMessages.showVarsWithDefaultValues);

	createParameterTable(parameterGroup);
    }

    private void createParameterTable(Composite parent)
    {
	parameterTable = new TableViewer(parent, SWT.BORDER | SWT.V_SCROLL);
	Table table = parameterTable.getTable();
	table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
	parameterTable.setContentProvider(ModelParameterModel.CONTENT_PROVIDER);
	parameterTable.setInput(parameterModel);
	parameterTable.setComparator(new ViewerComparator()
	{
	    @Override
	    public int compare(Viewer viewer, Object objA, Object objB)
	    {
		TextureVariable textureVarA = (TextureVariable) objA;
		TextureVariable textureVarB = (TextureVariable) objB;
		return textureVarA.getValue().compareTo(textureVarB.getValue());
	    }
	});
	parameterTable.addFilter(new ViewerFilter()
	{
	    @Override
	    public boolean select(Viewer viewer, Object parentElement, Object element)
	    {
		TextureVariable textureVar = (TextureVariable) element;
		return !textureVar.hasDefaultValue() || showVarsWithDefaultValues.getSelection();
	    }
	});
	SWTHelper.setDoubleClickToEdit(parameterTable);

	createTableColumns(parent);
    }

    private void createTableColumns(Composite parent)
    {
	TableViewerColumn keyColumn = SWTHelper.createColumn(parameterTable, "Key", 200);
	keyColumn.setLabelProvider(new ColumnLabelProvider()
	{
	    @Override
	    public String getText(Object element) 
	    {
		return ((ModelParameterModel.TextureVariable)element).getName();
	    }
	});

	TableViewerColumn valueColumn = SWTHelper.createColumn(parameterTable, "Value", 200);
	valueColumn.setLabelProvider(new ColumnLabelProvider()
	{
	    @Override
	    public String getText(Object element) 
	    {
		return ((ModelParameterModel.TextureVariable)element).getValue();
	    }
	});
	valueColumn.setEditingSupport(new ParameterValueEditingSupport(parameterTable));
    }

    public InputStream getFinalModel(Path modelPath)
    {
	InputStream finalModelStream = new ByteArrayInputStream(new byte[0]);
	try
	{
	    if(templateURI.toString().endsWith(".json"))
	    {
		JSONModel finalModel = JSONModel.createChild(templateModel, JSONModel.getResLocFromModelPath(modelPath));
		parameterModel.applyParametersToModel(finalModel);
		finalModelStream = new ByteArrayInputStream(JSONModel.serialise(finalModel).getBytes(StandardCharsets.UTF_8));
	    }
	    else if(templateURI.toString().endsWith(".template"))
	    {
		Map<String, String> argMap = new HashMap<>();
		for(TextureVariable textureVar : parameterModel.getTextureVariables())
		{
		    //Only set variables that have changed
		    if(!textureVar.getValue().equals(textureVar.getDefaultValue())) 
			argMap.put(textureVar.getName(), textureVar.getValue());
		}
		String finalModel = new TemplateEngine(new File(templateURI)).applyTemplate(argMap);
		finalModelStream = new ByteArrayInputStream(finalModel.getBytes(StandardCharsets.UTF_8));
	    }
	}
	catch (IOException e)
	{
	    e.printStackTrace();
	}
	return finalModelStream;
    }

    private void changeTemplateType(TemplateLocationType newType)
    {
	templateLocationType = newType;
    }

    @Override
    public void update(Observable o, Object arg) 
    {
	@SuppressWarnings("unchecked")
	IPath path = ((ObservableValue<IPath>) o).getValue();
	if(path != null)
	{
	    IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(path.segment(0));
	    IJavaProject jProject = JavaCore.create(project);
	    this.forgeJar = getForgeJar(jProject);
	    Path resourceSourceDir = Paths.get(jProject.getProject().getLocationURI()).resolve("src/main/resources");
	    this.mcResources = MCInterface.getResourceAggregateForProject(jProject, forgeJar.toPath(), resourceSourceDir);
	}
    }

    private static File getForgeJar(IJavaProject project)
    {
	try 
	{
	    String forgeVersionString = MetadataHandler.getMetadata(project).getForgeVersion().toString();
	    int buildNumber = Integer.valueOf(forgeVersionString.substring(forgeVersionString.lastIndexOf('.') + 1, forgeVersionString.length()));
	    IPath jarPath = EclipseHelper.getClasspathFile(project, "forgeSrc-" + ForgeMavenInterface.getVersionIdentifier(buildNumber) + ".jar").getPath();
	    return jarPath.toFile();
	} 
	catch (JavaModelException e) 
	{
	    e.printStackTrace();
	}
	return null;
    }

    private static enum TemplateLocationType 
    {
	ARCHIVE, FILESYSTEM;
    }
}
