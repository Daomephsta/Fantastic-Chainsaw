package leviathan143.fantasticchainsaw.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.gradle.api.Nullable;

public class VersionTree
{
	private Map<Integer, Node> roots = new HashMap<>();

	private static class Node
	{
		private final int versionPart;
		@Nullable
		private final Node parent;
		private final Map<Integer, Node> children = new HashMap<>();
		private final String versionString;

		private Node(Node parent, int versionPortion)
		{
			this.parent = parent;
			this.versionPart = versionPortion;

			StringBuilder sb = new StringBuilder();
			for (Node n = this; n != null; n = n.parent)
			{
				sb.insert(0, n.versionPart + ".");
			}
			sb.delete(sb.length() - 1, sb.length());
			this.versionString = sb.toString();
		}

		private void put(int[] version, int offset)
		{
			Node node = children.computeIfAbsent(version[offset], versionPart -> new Node(this, versionPart));
			offset++;
			if (offset >= version.length) return;
			node.put(version, offset);
		}

		private Node getChild(int versionPart)
		{
			return children.get(versionPart);
		}

		@Override
		public String toString()
		{
			return children.toString();
		}
	}

	public void put(String versionString)
	{
		int version[] = toIntArray(versionString);
		if (version.length == 0) throw new IllegalArgumentException("Versions must have at least one part");
		int root = version[0];
		Node rootNode = roots.computeIfAbsent(root, versionPart -> new Node(null, versionPart));
		if (version.length < 1) return;
		rootNode.put(version, 1);
	}

	public Collection<String> getSiblings(String versionString)
	{
		int[] versionParts = toIntArray(versionString);
		Collection<String> children = getChildren(Arrays.copyOfRange(versionParts, 0, versionParts.length - 1));
		children.remove(versionString);
		return children;
	}

	public Collection<String> getChildren(String versionString)
	{
		return getChildren(toIntArray(versionString));
	}

	private Collection<String> getChildren(int[] versionParts)
	{
		Node n = roots.get(versionParts[0]);
		for (int i = 1; i < versionParts.length; i++)
		{
			n = n.getChild(versionParts[i]);
		}
		return n.children.values().stream().map(node -> node.versionString).collect(Collectors.toList());
	}

	private int[] toIntArray(String versionString)
	{
		String[] splitVersion = versionString.split("\\.");
		int[] versionInts = new int[splitVersion.length];
		for (int i = 0; i < splitVersion.length; i++)
		{
			versionInts[i] = Integer.parseInt(splitVersion[i]);
		}
		return versionInts;
	}

	public void clear()
	{
		roots.clear();
	}

	@Override
	public String toString()
	{
		return roots.toString();
	}
}
