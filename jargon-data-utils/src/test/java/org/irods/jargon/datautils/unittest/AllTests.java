package org.irods.jargon.datautils.unittest;

import org.irods.jargon.datautils.avuautocomplete.AvuAutocompleteServiceImplTest;
import org.irods.jargon.datautils.connection.ConnectionCreatingPoolableObjectFactoryTest;
import org.irods.jargon.datautils.connection.TempPasswordCachingProtocolManagerTest;
import org.irods.jargon.datautils.connectiontester.ConnectionTesterImplTest;
import org.irods.jargon.datautils.datacache.DataCacheServiceImplTest;
import org.irods.jargon.datautils.filearchive.LocalFileGzipCompressorTest;
import org.irods.jargon.datautils.filearchive.LocalTarFileArchiverTest;
import org.irods.jargon.datautils.filesampler.FileSamplerServiceImplTest;
import org.irods.jargon.datautils.indexer.AbstractIndexerVisitorTest;
import org.irods.jargon.datautils.metadatamanifest.MetadataManifestProcessorImplTest;
import org.irods.jargon.datautils.shoppingcart.FileShoppingCartTest;
import org.irods.jargon.datautils.shoppingcart.ShoppingCartServiceImplTest;
import org.irods.jargon.datautils.synchproperties.SynchPropertiesServiceImplTest;
import org.irods.jargon.datautils.tree.DiffTreePostProcessorTest;
import org.irods.jargon.datautils.tree.FileTreeDiffEntryTest;
import org.irods.jargon.datautils.tree.FileTreeDiffUtilityTest;
import org.irods.jargon.datautils.tree.FileTreeNodeTest;
import org.irods.jargon.datautils.visitor.IrodsVisitedCompositeTest;
import org.irods.jargon.datautils.visitor.IrodsVisitedLeafTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ FileTreeDiffEntryTest.class, FileTreeNodeTest.class, FileTreeDiffUtilityTest.class,
		SynchPropertiesServiceImplTest.class, DataCacheServiceImplTest.class,
		ConnectionCreatingPoolableObjectFactoryTest.class, TempPasswordCachingProtocolManagerTest.class,
		FileShoppingCartTest.class, ShoppingCartServiceImplTest.class, DiffTreePostProcessorTest.class,
		LocalTarFileArchiverTest.class, LocalFileGzipCompressorTest.class, FileSamplerServiceImplTest.class,
		ConnectionTesterImplTest.class, MetadataManifestProcessorImplTest.class, AvuAutocompleteServiceImplTest.class,
		AbstractIndexerVisitorTest.class, IrodsVisitedLeafTest.class, IrodsVisitedCompositeTest.class })

public class AllTests {

}
