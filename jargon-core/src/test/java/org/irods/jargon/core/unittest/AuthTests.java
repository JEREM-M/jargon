/**
 * Test suite for authentication methods.  Note that testing.properties will indicate which particular methods can be tested on which platform,
 * those tests will be skipped otherwise
 */
package org.irods.jargon.core.unittest;

import org.irods.jargon.core.connection.auth.AuthenticationFactoryImplTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ AuthenticationFactoryImplTest.class })
public class AuthTests {

}