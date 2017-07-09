package leviathan143.fantasticchainsaw.test.versioning;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class VersionNormalCalculationTest 
{	
	@Test
	public void testVersionNormalCalculation() 
	{
		assertEquals(Versions.V1_12.getNormal(), 1012000);
		assertEquals(Versions.V1_11_2.getNormal(), 1011002);
		assertEquals(Versions.V1_11.getNormal(), 1011000);
		assertEquals(Versions.V1_10_2.getNormal(), 1010002);
		assertEquals(Versions.V1_10.getNormal(), 1010000);
		assertEquals(Versions.V1_9_4.getNormal(), 1009004);
		assertEquals(Versions.V1_9.getNormal(), 1009000);
	}
}
