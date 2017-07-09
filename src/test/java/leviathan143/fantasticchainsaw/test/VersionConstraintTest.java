package leviathan143.fantasticchainsaw.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import leviathan143.fantasticchainsaw.Versioning;
import leviathan143.fantasticchainsaw.Versioning.IVersionConstraint;
import leviathan143.fantasticchainsaw.Versioning.Version;

public class VersionConstraintTest
{
	private static final Version V1_12 = new Version("1.12");
	private static final Version V1_11_2 = new Version("1.11.2");
	private static final Version V1_11 = new Version("1.11");
	private static final Version V1_10_2 = new Version("1.10.2");
	private static final Version V1_10 = new Version("1.10");
	
	@Test
	public void testSpecificVersionConstraint()
	{
		IVersionConstraint constraint = Versioning.createVersionConstraint("1.11");
		assertFalse(constraint.acceptsVersion(V1_12));
		assertFalse(constraint.acceptsVersion(V1_11_2));
		assertTrue(constraint.acceptsVersion(V1_11));
		assertFalse(constraint.acceptsVersion(V1_10_2));
		assertFalse(constraint.acceptsVersion(V1_10));
		
		IVersionConstraint constraint2 = Versioning.createVersionConstraint("1.11");
		assertFalse(constraint2.acceptsVersion(V1_12));
		assertFalse(constraint2.acceptsVersion(V1_11_2));
		assertTrue(constraint2.acceptsVersion(V1_11));
		assertFalse(constraint2.acceptsVersion(V1_10_2));
		assertFalse(constraint2.acceptsVersion(V1_10));
	}
	
	@Test
	public void testVersionRangeConstraint()
	{
		IVersionConstraint constraint = Versioning.createVersionConstraint("1.10.2-1.11.2");
		assertFalse(constraint.acceptsVersion(V1_12));
		assertTrue(constraint.acceptsVersion(V1_11_2));
		assertTrue(constraint.acceptsVersion(V1_11));
		assertTrue(constraint.acceptsVersion(V1_10_2));
		assertFalse(constraint.acceptsVersion(V1_10));
	}
	
	@Test
	public void testMinVersionConstraint()
	{
		IVersionConstraint constraint = Versioning.createVersionConstraint("1.11+");
		assertTrue(constraint.acceptsVersion(V1_12));
		assertTrue(constraint.acceptsVersion(V1_11_2));
		assertTrue(constraint.acceptsVersion(V1_11));
		assertFalse(constraint.acceptsVersion(V1_10_2));
		assertFalse(constraint.acceptsVersion(V1_10));
	}
	
	@Test
	public void testMaxVersionConstraint()
	{
		IVersionConstraint constraint = Versioning.createVersionConstraint("1.11-");
		assertFalse(constraint.acceptsVersion(V1_12));
		assertFalse(constraint.acceptsVersion(V1_11_2));
		assertTrue(constraint.acceptsVersion(V1_11));
		assertTrue(constraint.acceptsVersion(V1_10_2));
		assertTrue(constraint.acceptsVersion(V1_10));
	}
}
