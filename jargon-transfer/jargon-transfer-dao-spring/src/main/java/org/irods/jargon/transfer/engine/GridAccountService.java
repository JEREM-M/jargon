package org.irods.jargon.transfer.engine;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.transfer.TransferEngineException;
import org.irods.jargon.transfer.dao.GridAccountDAO;
import org.irods.jargon.transfer.dao.KeyStoreDAO;
import org.irods.jargon.transfer.dao.domain.GridAccount;
import org.irods.jargon.transfer.dao.domain.KeyStore;
import org.irods.jargon.transfer.exception.PassPhraseInvalidException;

/**
 * Service to manage the stored account information in the transfer engine. This
 * information allows storage of iRODS accounts for transfers, and for
 * remembering logins.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface GridAccountService {

	/**
	 * @return the gridAccountDAO
	 */
	GridAccountDAO getGridAccountDAO();

	/**
	 * @param gridAccountDAO
	 *            the gridAccountDAO to set
	 */
	void setGridAccountDAO(GridAccountDAO gridAccountDAO);

	/**
	 * @return the keyStoreDAO
	 */
	KeyStoreDAO getKeyStoreDAO();

	/**
	 * @param keyStoreDAO
	 *            the keyStoreDAO to set
	 */
	void setKeyStoreDAO(KeyStoreDAO keyStoreDAO);

	/**
	 * Compare a given pass phrase's hash value with the previously stored
	 * value. If they match, then the pass phrase is validated and cached. This
	 * allows <code>GridAccount</code> information containing stored passwords
	 * to be decrypted.
	 * 
	 * @param passPhrase
	 *            <code>String</code> with the pass phrase to be validated, in
	 *            clear text
	 * @throws PassPhraseInvalidException
	 *             if the pass phrase is not validated
	 * @throws TransferEngineException
	 */
	void validatePassPhrase(String passPhrase)
			throws PassPhraseInvalidException, TransferEngineException;

	/**
	 * Given a pass phrase (presented as clear next, not a hash), update all
	 * stored grid accounts to reflect the new pass phrase.
	 * <p/>
	 * This method only works if the original pass phrase is first validated,
	 * calling the <code>validatePassPhrase()</code> method of this class
	 * 
	 * @param passPhrase
	 * @return
	 * @throws PassPhraseinvalidException
	 *             if the existing pass phrase was not properly validated before
	 *             setting a new one.
	 * @throws TransferEngineException
	 */
	KeyStore storePassPhrase(String passPhrase)
			throws PassPhraseInvalidException, TransferEngineException;

	/**
	 * Given an <code>IRODSAccount</code>, add a new <code>GridAccount</code>,
	 * or update the underlying <code>GridAccount</code> with the information
	 * from the <code>IRODSAccount</code>. (default storage resource, default
	 * path, password).
	 * <p/>
	 * Note that the grid account is unique by host, zone, and user name.
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} that will be used to create or update the
	 *            <code>GridAccount</code>
	 * @return {@link GridAccount} that is equivalent to the
	 *         <code>IRODSAccount</code>. Note that the password in the returned
	 *         <code>GridAccount</code> is encrypted by the pass phrase set in
	 *         the <code>TransferManager</code>
	 * @throws PassPhraseInvalidException
	 * @throws TransferEngineException
	 */
	GridAccount addOrUpdateGridAccountBasedOnIRODSAccount(
			final IRODSAccount irodsAccount) throws PassPhraseInvalidException,
			TransferEngineException;

	/**
	 * Return the pass phrase used to encrypt/decrypt the password information
	 * cached in the transfer database
	 * 
	 * @return
	 */
	String getCachedPassPhrase();

}