package org.irods.jargon.transfer.engine;

import java.util.List;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.TransferOptions.ForceOption;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.transfer.DefaultTransferControlBlock;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.transfer.TransferEngineException;
import org.irods.jargon.transfer.TransferServiceFactory;
import org.irods.jargon.transfer.TransferServiceFactoryImpl;
import org.irods.jargon.transfer.dao.domain.GridAccount;
import org.irods.jargon.transfer.dao.domain.LocalIRODSTransfer;
import org.irods.jargon.transfer.dao.domain.LocalIRODSTransferItem;
import org.irods.jargon.transfer.dao.domain.Synchronization;
import org.irods.jargon.transfer.exception.CannotUpdateTransferInProgressException;
import org.irods.jargon.transfer.exception.PassPhraseInvalidException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central manager of transfer engine. Manages status of entire engine on behalf
 * of client callers. This class is a singleton, and will keep track of the
 * current status of the transfer, receiving call-backs from the various
 * transfers that are underway. Clients can subscribe to this transfer manager
 * for information about the current operations of the transfer engine and
 * receive information about the status and history of the queue.
 * <p/>
 * This manager also is the interface through which transfers may be enqueued by
 * clients,.
 * <p/>
 * This class might evolve into some sort of scheduler, and these capabilities
 * would be exposed here in cooperation with the
 * <code>TransferQueueService</code>
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public final class TransferManagerImpl implements TransferManager {

	private final Logger log = LoggerFactory
			.getLogger(TransferManagerImpl.class);

	private TransferManagerCallbackListener transferManagerCallbackListener = null;

	private final TransferServiceFactory transferServiceFactory = new TransferServiceFactoryImpl();

	/**
	 * Required dependency to be injected
	 */
	private TransferQueueService transferQueueService;

	/**
	 * Required dependency to be injected
	 */
	private GridAccountService gridAccountService;

	private ErrorStatus errorStatus;

	private RunningStatus runningStatus;

	private TransferRunner currentTransferRunner = null;

	private final IRODSFileSystem irodsFileSystem;

	private TransferEngineConfigurationProperties transferEngineConfigurationProperties;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.engine.TransferManager#
	 * getTransferEngineConfigurationProperties()
	 */
	@Override
	public synchronized TransferEngineConfigurationProperties getTransferEngineConfigurationProperties() {
		return transferEngineConfigurationProperties;
	}

	/**
	 * Set the configuration information that controls the behavior of the
	 * transfer engine. If set to null, then the system will exhibit default
	 * behavior
	 * 
	 * @param transferEngineConfigurationProperties
	 *            {@link TransferEngineConfigurationProperties} or
	 *            <code>null</code> if default behaviors are desired
	 */
	public synchronized void setTransferEngineConfigurationProperties(
			final TransferEngineConfigurationProperties transferEngineConfigurationProperties) {
		this.transferEngineConfigurationProperties = transferEngineConfigurationProperties;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.engine.TransferManager#getTransferServiceFactory
	 * ()
	 */
	@Override
	public TransferServiceFactory getTransferServiceFactory() {
		return transferServiceFactory;
	}

	/**
	 * Create an instance of the transfer manager implementation
	 * 
	 * @param irodsFileSystem
	 *            {@link IRODSFileSystem} that provides the iRODS connection and
	 *            service framework from jargon
	 * @param passPhrase
	 *            <code>String</code> with the password to decrypt cached
	 *            account information
	 * @throws PassPhraseInvalidException
	 *             if the pass phrase is invalid
	 * @throws TransferEngineException
	 */
	public TransferManagerImpl(final IRODSFileSystem irodsFileSystem,final String passPhrase) throws PassPhraseInvalidException,
	TransferEngineException {

		if (irodsFileSystem == null) {
			throw new IllegalArgumentException("null irodsFileSystem");
		}
		
		if (passPhrase == null || passPhrase.isEmpty()) {
			throw new IllegalArgumentException("null or empty passPhrase");
		}

		this.irodsFileSystem = irodsFileSystem;

		try {
			init();
		} catch (Exception e) {
			log.error("error initing tranfer manager", e);
			throw new TransferEngineException("Failed to init TransferManager");
		}

		gridAccountService.validatePassPhrase(passPhrase);

	}

	/**
	 * Create an instance of a <code>TransferManager</code>. There should only
	 * be one transfer manager per application.
	 * 
	 * @param transferManagerCallbackListener
	 *            implementation of the
	 *            {@link org.irods.jargon.transfer.engine.TransferManagerCallbackListener}
	 *            class that can receive callbacks from the running transfer
	 *            process.
	 * @param passPhrase
	 *            <code>String</code> with the pass phrase used to encrypt grid
	 *            login data.
	 * @return instance of <code>TransferManager</code>
	 * @throws PassPhraseInvalidException
	 *             , TransferEngineException
	 */
	public TransferManagerImpl(
			final IRODSFileSystem irodsFileSystem,
			final TransferManagerCallbackListener transferManagerCallbackListener,
			final String passPhrase) throws PassPhraseInvalidException,
			TransferEngineException {

		if (irodsFileSystem == null) {
			throw new IllegalArgumentException("null irodsFileSystem");
		}
		
		if (passPhrase == null || passPhrase.isEmpty()) {
			throw new IllegalArgumentException("null or empty passPhrase");
		}

		this.irodsFileSystem = irodsFileSystem;
		this.transferManagerCallbackListener = transferManagerCallbackListener;

		try {
			init();
		} catch (Exception e) {
			throw new TransferEngineException("Failed to init TransferManager");
		}

		gridAccountService.validatePassPhrase(passPhrase);

	}

	/**
	 * Create an instance of a <code>TransferManager</code>. There should only
	 * be one transfer manager per application. This initializer allows
	 * specification of recording of successful transfers. Note that
	 * <code>logSuccessfulTransfers</code> will likely move to a configuration
	 * block in later versions.
	 * 
	 * @param transferManagerCallbackListener
	 *            implementation of the
	 *            {@link org.irods.jargon.transfer.engine.TransferManagerCallbackListener}
	 *            class that can receive callbacks from the running transfer
	 *            process.
	 * @param transferEngineConfigurationProperties
	 *            {@link transferEngineConfigurationProperties} that controls
	 *            behavior of transfers, or <code>null</code> if default
	 *            behaviors are desired
	 *   @param passPhrase
	 *            <code>String</code> with the pass phrase used to encrypt grid
	 *            login data.
	 * 
	 * @return instance of <code>TransferManager</code>
	 * @throws JargonException
	 */

	public TransferManagerImpl(
			final IRODSFileSystem irodsFileSystem,
			final TransferManagerCallbackListener transferManagerCallbackListener,
			final TransferEngineConfigurationProperties transferEngineConfigurationProperties,
			final String passPhrase)
			throws JargonException {

		if (irodsFileSystem == null) {
			throw new IllegalArgumentException("null irodsFileSystem");
		}
		
		if (passPhrase == null || passPhrase.isEmpty()) {
			throw new IllegalArgumentException("null or empty passPhrase");
		}

		this.irodsFileSystem = irodsFileSystem;
		this.transferManagerCallbackListener = transferManagerCallbackListener;
		this.transferEngineConfigurationProperties = transferEngineConfigurationProperties;

		try {
			init();
		} catch (Exception e) {
			log.error("error in init of TransferManagerImpl", e);
			throw new JargonException("Failed to init TransferManager", e);
		}

		gridAccountService.validatePassPhrase(passPhrase);
	}

	private void init() throws JargonException {

		if (transferEngineConfigurationProperties == null) {
			log.info("building transfer configuration properties");
			transferEngineConfigurationProperties = new TransferEngineConfigurationProperties();
		}

		this.errorStatus = ErrorStatus.OK;
		this.runningStatus = RunningStatus.IDLE;
		this.transferQueueService = transferServiceFactory
				.instanceTransferQueueService();
		this.gridAccountService = transferServiceFactory
				.instanceGridAccountService();
		log.info("processing queue at startup");
		transferQueueService.processQueueAtStartup();
	}

	private void publishRunningStatus(final RunningStatus runningStatus)
			throws JargonException {
		if (runningStatus == null) {
			throw new JargonException("null running status");
		}

		if (transferManagerCallbackListener != null) {
			transferManagerCallbackListener
					.transferManagerRunningStatusUpdate(runningStatus);
		}
	}

	private void publishErrorStatus(final ErrorStatus errorStatus)
			throws JargonException {
		if (errorStatus == null) {
			throw new JargonException("null error status");
		}

		if (transferManagerCallbackListener != null) {
			transferManagerCallbackListener
					.transferManagerErrorStatusUpdate(errorStatus);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.engine.TransferManager#getRunningStatus()
	 */
	@Override
	public synchronized RunningStatus getRunningStatus() {
		return runningStatus;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.engine.TransferManager#isPaused()
	 */
	@Override
	public synchronized boolean isPaused() {
		return runningStatus == RunningStatus.PAUSED;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.engine.TransferManager#pause()
	 */
	@Override
	public synchronized void pause() throws JargonException {
		log.info("pause...");
		if (runningStatus == RunningStatus.PAUSED) {
			log.info("already paused");
		}

		if (currentTransferRunner != null) {
			log.info("pause currently running transfer: {}",
					currentTransferRunner.getLocalIRODSTransfer());
			currentTransferRunner.getTransferControlBlock().setPaused(true);
		}

		runningStatus = RunningStatus.PAUSED;
		publishRunningStatus(runningStatus);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.engine.TransferManager#resume()
	 */
	@Override
	public synchronized void resume() throws JargonException {
		log.info("resume...");
		if (runningStatus != RunningStatus.PAUSED) {
			log.info("not paused");
			return;
		}

		log.info("resuming");
		// need to check for pending transfers via database and restart (idle
		// versus processing)
		runningStatus = RunningStatus.IDLE;
		publishRunningStatus(runningStatus);
		processNextInQueueIfIdle();
	}

	protected synchronized void notifyWarningCondition() throws JargonException {
		log.info("notify warning");

		if (runningStatus == RunningStatus.PAUSED) {
			return;
		}

		// warning does not override error status
		if (errorStatus != ErrorStatus.ERROR) {
			errorStatus = ErrorStatus.WARNING;
			publishErrorStatus(errorStatus);
		}
	}

	protected synchronized void notifyErrorCondition() throws JargonException {

		if (runningStatus == RunningStatus.PAUSED) {
			return;
		}

		log.info("notify error");
		errorStatus = ErrorStatus.ERROR;
		publishErrorStatus(errorStatus);
	}

	protected synchronized void notifyOKCondition() throws JargonException {

		if (runningStatus == RunningStatus.PAUSED) {
			return;
		}

		log.info("notify OK");
		errorStatus = ErrorStatus.OK;
		publishErrorStatus(errorStatus);
	}

	protected synchronized void notifyProcessing() throws JargonException {

		if (runningStatus == RunningStatus.PAUSED) {
			return;
		}

		log.info("notify as processing");
		runningStatus = RunningStatus.PROCESSING;
		publishRunningStatus(runningStatus);
	}

	protected synchronized void notifyEnqueued(
			final LocalIRODSTransfer enqueuedTransfer) {
		log.info("a transfer has been enqueued: {}", enqueuedTransfer);
	}

	protected synchronized void notifyComplete() throws JargonException {
		log.info("notifyComplete");

		// if I am paused, then remain paused until resumed
		if (runningStatus != RunningStatus.PAUSED) {
			runningStatus = RunningStatus.IDLE;
			publishRunningStatus(runningStatus);
			processNextInQueueIfIdle();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.engine.TransferManager#getErrorStatus()
	 */
	@Override
	public synchronized ErrorStatus getErrorStatus() {
		return errorStatus;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.engine.TransferManager#purgeAllTransfers()
	 */
	@Override
	public synchronized void purgeAllTransfers() throws JargonException {
		transferQueueService.purgeQueue();
		resetStatus();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.engine.TransferManager#purgeSuccessfulTransfers
	 * ()
	 */
	@Override
	public synchronized void purgeSuccessfulTransfers() throws JargonException {
		transferQueueService.purgeSuccessful();
		resetStatus();
	}


	@Override
	public void enqueueAPut(final String sourceAbsolutePath,
			final String targetAbsolutePath, final String resource,
			final GridAccount gridAccount) throws JargonException {

		if (sourceAbsolutePath == null || sourceAbsolutePath.isEmpty()) {
			throw new JargonException("sourceAbsolutePath is null or empty");
		}

		if (targetAbsolutePath == null || targetAbsolutePath.isEmpty()) {
			throw new JargonException("targetAbsolutePath is null or empty");
		}

		if (resource == null) {
			throw new JargonException(
					"resource is null, set to blank if default is desired");
		}

		if (gridAccount == null) {
			throw new JargonException("gridAccount is null");
		}

		log.info("enquing a put transfer");
		log.info("   sourceAbsolutePath: {}", sourceAbsolutePath);
		log.info("   targetAbsolutePath: {}", targetAbsolutePath);
		log.info("   resource: {}", resource);
		log.info("   gridAccount: {}", gridAccount);

		synchronized (this) {

			transferQueueService.enqueuePutTransfer(sourceAbsolutePath,
					targetAbsolutePath, resource, gridAccount);

			log.info("enqueue of put, current running status: {}",
					runningStatus);
			log.info("current error status:{}", errorStatus);

			processNextInQueueIfIdle();
		}

	}


	@Override
	public void enqueueAGet(final String irodsSourceAbsolutePath,
			final String targetLocalAbsolutePath, final String resource,
			final GridAccount gridAccount) throws JargonException {

		if (irodsSourceAbsolutePath == null
				|| irodsSourceAbsolutePath.isEmpty()) {
			throw new JargonException(
					"irodsSourceAbsolutePath is null or empty");
		}

		if (targetLocalAbsolutePath == null
				|| targetLocalAbsolutePath.isEmpty()) {
			throw new JargonException(
					"targetLocalAbsolutePath is null or empty");
		}

		if (resource == null) {
			throw new JargonException(
					"resource is null, set to blank if default is desired");
		}

		if (gridAccount == null) {
			throw new JargonException("irodsAccount is null");
		}

		log.info("enquing a get transfer");
		log.info("   irodsSourceAbsolutePath: {}", irodsSourceAbsolutePath);
		log.info("   targetLocalAbsolutePath: {}", targetLocalAbsolutePath);
		log.info("   resource: {}", resource);
		log.info("   gridAccount: {}", gridAccount);

		synchronized (this) {


			transferQueueService.enqueueGetTransfer(irodsSourceAbsolutePath,
					targetLocalAbsolutePath, resource, gridAccount);

			log.info("enqueue of get, current running status: {}",
					runningStatus);
			log.info("current error status:{}", errorStatus);

			processNextInQueueIfIdle();
		}

	}


	@Override
	public void enqueueAReplicate(final String irodsAbsolutePath,
			final String targetResource, final GridAccount gridAccount)
			throws JargonException {

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new JargonException("irodsAbsolutePath is null or empty");
		}

		if (targetResource == null) {
			throw new JargonException(
					"targetResource is null, set to blank if default is desired");
		}

		if (gridAccount == null) {
			throw new JargonException("gridAccount is null");
		}

		log.info("enquing a replicate transfer");
		log.info("   irodsAbsolutePath: {}", irodsAbsolutePath);
		log.info("   targetResource: {}", targetResource);
		log.info("   gridAccount: {}", gridAccount);

		synchronized (this) {

			transferQueueService.enqueueReplicateTransfer(irodsAbsolutePath,
					targetResource, gridAccount);

			processNextInQueueIfIdle();
		}

	}


	@Override
	public void enqueueACopy(final String irodsSourceAbsolutePath,
			final String targetResource, final String irodsTargetAbsolutePath,
			final GridAccount gridAccount) throws JargonException {

		if (irodsSourceAbsolutePath == null
				|| irodsSourceAbsolutePath.isEmpty()) {
			throw new JargonException(
					"irodsSourceAbsolutePath is null or empty");
		}

		if (targetResource == null) {
			throw new JargonException(
					"targetResource is null, set to blank if default is desired");
		}

		if (irodsTargetAbsolutePath == null
				|| irodsTargetAbsolutePath.isEmpty()) {
			throw new JargonException(
					"irodsTargetAbsolutePath is null or empty");
		}

		if (gridAccount == null) {
			throw new JargonException("gridAccount is null");
		}

		log.info("enquing a copy transfer");
		log.info("   irodsSourceAbsolutePath: {}", irodsSourceAbsolutePath);
		log.info("   targetResource: {}", targetResource);
		log.info("   irodsTargetAbsolutePath: {}", irodsTargetAbsolutePath);
		log.info("   gridAccount: {}", gridAccount);

		synchronized (this) {

			transferQueueService.enqueueCopyTransfer(irodsSourceAbsolutePath,
					targetResource, irodsTargetAbsolutePath, gridAccount);

			processNextInQueueIfIdle();
		}

	}


	@Override
	public void enqueueASynch(final Synchronization synchronization,
			final GridAccount gridAccount) throws JargonException {

		log.info("enqueueASynch()");

		if (synchronization == null) {
			throw new IllegalArgumentException("null synchronization");
		}

		if (gridAccount == null) {
			throw new IllegalArgumentException("null gridAccount");
		}

		log.info("synchronization:{}", synchronization);
		log.info("gridAccount:{}", gridAccount);

		if (synchronization.getId() <= 0) {
			throw new JargonException(
					"synchronization does not appear to be persisted");
		}

		log.info("enqueuing synch");
		synchronized (this) {
			transferQueueService.enqueueSynchTransfer(synchronization,
					gridAccount);
			processNextInQueueIfIdle();

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.engine.TransferManager#processNextInQueueIfIdle
	 * ()
	 */
	@Override
	public synchronized void processNextInQueueIfIdle() throws JargonException {
		log.info("process next in queue");

		if (runningStatus == RunningStatus.IDLE) {
			log.info("queue is idle, go ahead and process next");
			dequeueNextAndLaunchTransferRunner();
		} else {
			log.info("not idle, do not process");
		}
	}

	private void dequeueNextAndLaunchTransferRunner() throws JargonException {
		log.info("process next in queue");

		if (this.isPaused()) {
			log.info("I am paused, don't process");
			return;
		}

		LocalIRODSTransfer dequeued = transferQueueService.dequeueTransfer();


		if (dequeued == null) {
			log.info("queue is empty");
			runningStatus = RunningStatus.IDLE;
			publishRunningStatus(runningStatus);
			return;
		}

		errorStatus = ErrorStatus.OK;
		publishErrorStatus(errorStatus);
		runningStatus = RunningStatus.PROCESSING;
		publishRunningStatus(runningStatus);

		// last successful path will have a restart value or be blank
		TransferControlBlock transferControlBlock = DefaultTransferControlBlock
				.instance(dequeued.getLastSuccessfulPath());
		transferControlBlock.setTransferOptions(this.getIrodsFileSystem().getIrodsSession().buildTransferOptionsBasedOnJargonProperties());
		// currently defaults to a force...will uprade this in another rev 
		transferControlBlock.getTransferOptions().setForceOption(
				ForceOption.USE_FORCE);
		
		log.info("options for dequeued transfer:{}", transferControlBlock.getTransferOptions());

		// transfer options come from jargon default properties 
		
		log.info(">>>> dequeue {}", dequeued);

		log.info("getting a transferRunner to process");
		currentTransferRunner = new TransferRunner(this, dequeued,
				transferControlBlock, transferQueueService);
		final Thread transferThread = new Thread(currentTransferRunner);
		log.info("launching transfer thread");
		transferThread.start();

		log.info(
				">>>>transfer runner is launched to handle dequeued transfer:{}",
				currentTransferRunner);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.engine.TransferManager#getCurrentQueue()
	 */
	@Override
	public List<LocalIRODSTransfer> getCurrentQueue() throws JargonException {
		return transferQueueService.getCurrentQueue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.engine.TransferManager#getRecentQueue()
	 */
	@Override
	public List<LocalIRODSTransfer> getRecentQueue() throws JargonException {
		return transferQueueService.getRecentQueue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.engine.TransferManager#getErrorQueue()
	 */
	@Override
	public List<LocalIRODSTransfer> getErrorQueue() throws JargonException {
		return transferQueueService.getErrorQueue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.engine.TransferManager#getWarningQueue()
	 */
	@Override
	public List<LocalIRODSTransfer> getWarningQueue() throws JargonException {
		return transferQueueService.getWarningQueue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.engine.TransferManager#
	 * getTransferManagerCallbackListener()
	 */
	@Override
	public TransferManagerCallbackListener getTransferManagerCallbackListener() {
		return transferManagerCallbackListener;
	}

	protected synchronized void setErrorStatus(final ErrorStatus errorStatus) {
		this.errorStatus = errorStatus;
	}

	protected synchronized void setRunningStatus(
			final RunningStatus runningStatus) {
		this.runningStatus = runningStatus;
	}

	@Override
	public synchronized void resetStatus() throws JargonException {
		if (this.getRunningStatus() == RunningStatus.IDLE
				|| this.getRunningStatus() == RunningStatus.PAUSED) {
			this.notifyOKCondition();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.engine.TransferManager#
	 * getAllTransferItemsForTransfer(java.lang.Long)
	 */
	@Override
	public List<LocalIRODSTransferItem> getAllTransferItemsForTransfer(
			final Long localIRODSTransferId) throws JargonException {
		return transferQueueService
				.getAllTransferItemsForTransfer(localIRODSTransferId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.engine.TransferManager#
	 * getErrorTransferItemsForTransfer(java.lang.Long)
	 */
	@Override
	public List<LocalIRODSTransferItem> getErrorTransferItemsForTransfer(
			final Long localIRODSTransferId) throws JargonException {
		return transferQueueService
				.getErrorTransferItemsForTransfer(localIRODSTransferId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.engine.TransferManager#restartTransfer(org.
	 * irods.jargon.transfer.dao.domain.LocalIRODSTransfer)
	 */
	@Override
	public void restartTransfer(final LocalIRODSTransfer localIRODSTransfer)
			throws JargonException {
		log.info("restarting:{}", localIRODSTransfer);
		synchronized (this) {
			transferQueueService.restartTransfer(localIRODSTransfer);
			this.processNextInQueueIfIdle();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.engine.TransferManager#resubmitTransfer(org
	 * .irods.jargon.transfer.dao.domain.LocalIRODSTransfer)
	 */
	@Override
	public void resubmitTransfer(final LocalIRODSTransfer localIRODSTransfer)
			throws JargonException {
		log.info("restarting:{}", localIRODSTransfer);
		synchronized (this) {
			transferQueueService.resubmitTransfer(localIRODSTransfer);
			this.processNextInQueueIfIdle();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.engine.TransferManager#cancelTransfer(org.irods
	 * .jargon.transfer.dao.domain.LocalIRODSTransfer)
	 */
	@Override
	public void cancelTransfer(final LocalIRODSTransfer localIRODSTransfer)
			throws JargonException {
		// see if this is the current transfer, and cancel the running transfer
		// if necessary
		synchronized (this) {

			if (currentTransferRunner == null) {
				log.info("no running transfer to cancel");
				return;
			}

			// if the current transfer equals the given transfer, then send a
			// cancel
			LocalIRODSTransfer runningTransfer = currentTransferRunner
					.getLocalIRODSTransfer();
			log.info("cancel sent for:{}", localIRODSTransfer);
			log.info("currently running transfer: {}", runningTransfer);

			log.info("current txfr id:{}", runningTransfer.getId());
			log.info("txr to cancel id: {}", localIRODSTransfer.getId());
			log.info("are these equal: {}",
					runningTransfer.getId().equals(localIRODSTransfer.getId()));

			if (runningTransfer.getId().equals(localIRODSTransfer.getId())) {
				log.info("cancelling running transfer:{}", runningTransfer);
				currentTransferRunner.getTransferControlBlock().setCancelled(
						true);
			} else {
				log.info("the given transfer not running, will just set the status to cancelled");
				transferQueueService.setTransferAsCancelled(localIRODSTransfer);
			}
		}

	}
	
	@Override
	public synchronized void updatePassword(final GridAccount gridAccount, final String newPassword)  throws CannotUpdateTransferInProgressException, JargonException {
		// transferQueueService.updateUserPasswordInTransferManagerData(gridAccount,
		// newPassword);
		
	}

	protected void notifyStatusUpdate(final TransferStatus transferStatus)
			throws JargonException {
		if (transferStatus == null) {
			throw new JargonException("null transfer status");
		}

		if (transferManagerCallbackListener != null) {
			transferManagerCallbackListener.statusCallback(transferStatus);
		}
	}

	protected void notifyOverallStatusUpdate(final TransferStatus transferStatus)
			throws JargonException {
		if (transferStatus == null) {
			throw new JargonException("null transfer status");
		}

		if (transferManagerCallbackListener != null) {
			transferManagerCallbackListener
					.overallStatusCallback(transferStatus);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.engine.TransferManager#getTransferQueueService
	 * ()
	 */
	@Override
	public synchronized TransferQueueService getTransferQueueService() {
		return transferQueueService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.engine.TransferManager#getIrodsFileSystem()
	 */
	@Override
	public synchronized IRODSFileSystem getIrodsFileSystem() {
		return irodsFileSystem;
	}

	/**
	 * @return the gridAccountService
	 */
	@Override
	public GridAccountService getGridAccountService() {
		return gridAccountService;
	}

	/**
	 * @param gridAccountService the gridAccountService to set
	 */
	public synchronized void setGridAccountService(GridAccountService gridAccountService) {
		this.gridAccountService = gridAccountService;
	}
	
	@Override
	public synchronized void validatePassPhrase(final String passPhrase)
			throws PassPhraseInvalidException, TransferEngineException {

		/*
		 * All parm checks delegated to validatePassPhrase
		 */
		gridAccountService.validatePassPhrase(passPhrase);
	}


}
