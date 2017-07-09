package leviathan143.fantasticchainsaw.test.versioning;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import leviathan143.fantasticchainsaw.Versioning;
import leviathan143.fantasticchainsaw.Versioning.IVersionConstraint;

public class VersionConstraintTest
{
	@Test
	public void testSpecificVersionConstraint()
	{
		IVersionConstraint constraint = Versioning.createVersionConstraint("1.11");
		assertFalse(constraint.acceptsVersion(Versions.V1_12));
		assertFalse(constraint.acceptsVersion(Versions.V1_11_2));
		assertTrue(constraint.acceptsVersion(Versions.V1_11));
		assertFalse(constraint.acceptsVersion(Versions.V1_10_2));
		assertFalse(constraint.acceptsVersion(Versions.V1_10));
		
		IVersionConstraint constraint2 = Versioning.createVersionConstraint("1.11");
		assertFalse(constraint2.acceptsVersion(Versions.V1_12));
		assertFalse(constraint2.acceptsVersion(Versions.V1_11_2));
		assertTrue(constraint2.acceptsVersion(Versions.V1_11));
		assertFalse(constraint2.acceptsVersion(Versions.V1_10_2));
		assertFalse(constraint2.acceptsVersion(Versions.V1_10));
	}
	
	@Test
	public void testVersionRangeConstraint()
	{
		IVersionConstraint constraint = Versioning.createVersionConstraint("1.10.2-1.11.2");
		assertFalse(constraint.acceptsVersion(Versions.V1_12));
		assertTrue(constraint.acceptsVersion(Versions.V1_11_2));
		assertTrue(constraint.acceptsVersion(Versions.V1_11));
		assertTrue(constraint.acceptsVersion(Versions.V1_10_2));
		assertFalse(constraint.acceptsVersion(Versions.V1_10));
	}
	
	@Test
	public void testMinVersionConstraint()
	{
		IVersionConstraint constraint = Versioning.createVersionConstraint("1.11+");
		assertTrue(constraint.acceptsVersion(Versions.V1_12));
		assertTrue(constraint.acceptsVersion(Versions.V1_11_2));
		assertTrue(constraint.acceptsVersion(Versions.V1_11));
		assertFalse(constraint.acceptsVersion(Versions.V1_10_2));
		assertFalse(constraint.acceptsVersion(Versions.V1_10));
	}
	
	@Test
	public void testMaxVersionConstraint()
	{
		IVersionConstraint constraint = Versioning.createVersionConstraint("1.11-");
		assertFalse(constraint.acceptsVersion(Versions.V1_12));
		assertFalse(constraint.acceptsVersion(Versions.V1_11_2));
		assertTrue(constraint.acceptsVersion(Versions.V1_11));
		assertTrue(constraint.acceptsVersion(Versions.V1_10_2));
		assertTrue(constraint.acceptsVersion(Versions.V1_10));
	}
}
