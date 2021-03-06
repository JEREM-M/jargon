/**
 *
 */
package org.irods.jargon.mdquery.exception;

import org.irods.jargon.core.exception.JargonRuntimeException;

/**
 * General unchecked exception for metadata queries
 *
 * @author Mike Conway - DICE
 *
 */
public class MetadataQueryRuntimeException extends JargonRuntimeException {

	/**
	 *
	 */
	private static final long serialVersionUID = -5850439092572959832L;

	/**
	 *
	 */
	public MetadataQueryRuntimeException() {
	}

	/**
	 * @param message
	 */
	public MetadataQueryRuntimeException(final String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public MetadataQueryRuntimeException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public MetadataQueryRuntimeException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
