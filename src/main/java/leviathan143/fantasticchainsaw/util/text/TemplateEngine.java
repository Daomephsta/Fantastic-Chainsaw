package leviathan143.fantasticchainsaw.util.text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class TemplateEngine
{
    private String templateSource;

    public TemplateEngine(File template) throws IOException
    {
	try (BufferedReader reader = new BufferedReader(new FileReader(template)))
	{
	    StringBuilder sourceBuilder = new StringBuilder();
	    String line;
	    
	    while ((line = reader.readLine()) != null)
	    {
		sourceBuilder.append(line + System.lineSeparator());
	    }
	    templateSource = sourceBuilder.toString();
	}
    }

    public String applyTemplate(Map<String, String> argMap)
    {
	String transformedSource = new String(templateSource);
	for(Map.Entry<String, String> arg : argMap.entrySet())
	{
	    transformedSource = transformedSource.replace("$" + arg.getKey(), arg.getValue());
	}
	return transformedSource;   
    }

    public void makeFile(File target, Map<String, String> argMap) throws IOException
    {
	try(BufferedWriter writer = new BufferedWriter(new FileWriter(target)))
	{
	    for(String line : applyTemplate(argMap).split(System.lineSeparator()))
	    {
		writer.write(line);
		writer.newLine();
	    }
	}
    }
}
