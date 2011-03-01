package org.irods.jargon.core.transfer;

import org.irods.jargon.core.exception.JargonException;

/**
 * An interface for an object that can control a recursive transfer process,
 * providing a common reference object between the transferring process and the
 * recursive transfer method (get, put, rel, etc).
 * <p/>
 * Implementations of this class can act as a filter to select items for
 * transfer, and can also be used to signal a cancel.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface TransferControlBlock {

	public final int MAX_ERROR_DEFAULT = 5;
	public final int MAX_ERROR_IGNORE = -1;

	/**
	 * Indicates whether the given operation should be cancelled. This method
	 * must be synchronized.
	 * 
	 * @return <code>boolean</code> if the operation should be cancelled.
	 */
	boolean isCancelled();

	/**
	 * Indicates that the given operation should be paused. This method must be
	 * synchronized
	 * 
	 * @return<code>boolean</code> if the operation should be paused.
	 */
	boolean isPaused();

	/**
	 * Send a signal to cancel the operation. A transfer operation will check
	 * this and will cancel a recursive transfer if indicated. This method must
	 * be synchronized.
	 * 
	 * @param cancelled
	 *            <code>boolean</code> that will be true if the operation must
	 *            be cancelled.
	 */
	void setCancelled(final boolean cancelled);

	/**
	 * Send a signal to pause the operation. A transfer operation will check
	 * this and will pause a recursive transfer if indicated. This method must
	 * be synchronized.
	 * 
	 * @param paused
	 *            <code>boolean</code> that will be true if the operation must
	 *            be paused.
	 */
	void setPaused(final boolean paused);

	/**
	 * Method to filter the transfer. An absolute path appropriate to the
	 * transfer is given, and a booelan will be returned that indicates whether
	 * the file should be transferred or not. A <code>false</code> indicates to
	 * ignore this file.
	 * 
	 * The absolute path depends on the operation. For a put, it would be the
	 * local source file. For a get, it would be the irods file. For a
	 * replication, it would be the irods file.
	 * 
	 * @param absolutePath
	 *            <code>String</code> with the appropriate file path to filter.
	 * @return <code>boolean</code> with a value of <code>true</code> if the
	 *         file should be acted upon.
	 * @throws JargonException
	 */
	boolean filter(final String absolutePath) throws JargonException;

	/**
	 * Get the maximum number of errors to allow before canceling the transfer
	 * 
	 * @return <code>int</code> with the maximum number of errors before
	 *         canceling.
	 */
	int getMaximumErrorsBeforeCanceling();

	/**
	 * Set the maximum number of errors to allow before canceling the transfer
	 * 
	 * @param maximumErrorsBeforeCancelling
	 *            <code>int</code> with the maximum number of errors before
	 *            canceling. -1 indicates that the number of errors will be
	 *            ignored
	 * @throws JargonException
	 */
	void setMaximumErrorsBeforeCanceling(final int maximumErrorsBeforeCancelling)
			throws JargonException;

	/**
	 * Get the total number of transfer errors that have occurred in this
	 * transfer so far
	 * 
	 * @return <code>int</code> with the number of errors encountered so far.
	 */
	int getErrorCount();

	/**
	 * Check if enough errors have been accumulated to cause the transfer to be
	 * abandoned.
	 * 
	 * @return <code>boolean</code> that indicates whether reporting this error
	 *         requires that the transfer be cancelled.
	 */
	boolean shouldTransferBeAbandonedDueToNumberOfErrors();

	/**
	 * Indicate an error in the transfer. This method will increment the error
	 * counter in the <code>TransferControlBlock</code>.
	 */
	void reportErrorInTransfer();

	/**
	 * Gets the total number of files to be transferred. This is initialized
	 * automatically if a callback listener has been added.
	 * 
	 * @return
	 */
	int getTotalFilesToTransfer();

	/**
	 * Set the total number of files to be transferred. This is initialized
	 * automatically if a callback listener has been added.
	 * 
	 * @param totalFilesToTransfer
	 */
	void setTotalFilesToTransfer(int totalFilesToTransfer);

	/**
	 * Get a running total of the files transferred so far. This is initialized
	 * automatically if a callback listener has been added.
	 * 
	 * @return
	 */
	int getTotalFilesTransferredSoFar();

	/**
	 * Increment the count of files that have been transferred so far // FIXME: change to increment and return value...avoid increment and get
	 */
	void incrementFilesTransferredSoFar();

}
