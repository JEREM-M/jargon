package org.irods.jargon.ticket;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;

/**
 * Factory for various services that support iRODS tickets. This is helpful for
 * creating these services in various applications in a way that is easy to mock
 * and test.
 * <p/>
 * Note that tickets are not supported in versions before iRODS 3.1
 * 
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface TicketServiceFactory {

	/**
	 * Create a new instance of the <code>TicketAdminService</code> that can
	 * create and modify tickets
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} that describes the server and connection
	 *            info
	 * @return {@link TicketAdminService} object to interact with iRODS tickets
	 * @throws JargonException
	 */
	TicketAdminService instanceTicketAdminService(IRODSAccount irodsAccount)
			throws JargonException;

	/**
	 * Create a new instance of the <code>TicketClientOperations</code> that can
	 * redeem tickets
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} that describes the server and connection
	 *            info
	 * @return {@link TicketAdminService} object to interact with iRODS tickets
	 * @throws JargonException
	 */
	TicketClientOperations instanceTicketClientOperations(
			IRODSAccount irodsAccount) throws JargonException;

}