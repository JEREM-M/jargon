package org.irods.jargon.core.pub;

import java.util.List;

import org.irods.jargon.core.connection.JargonProperties;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.SpecificQueryDefinition;
import org.irods.jargon.core.query.IRODSQueryResultSet;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.SpecificQuery;
import org.irods.jargon.core.query.SpecificQueryResultSet;

/**
 * Interface for an object to interact with specific query in IRODS.
 *
 * @author Lisa Stillwell RENCI (www.renci.org)
 *
 **/

public interface SpecificQueryAO extends IRODSAccessObject {

	/**
	 * Add a specific query to iRODS
	 *
	 * @param specificQuery
	 *            {@link SpecificQuery} to be added
	 * @throws JargonException
	 *             for iRODS error
	 * @throws DuplicateDataException
	 *             for duplicate query
	 */
	void addSpecificQuery(SpecificQueryDefinition specificQuery) throws JargonException, DuplicateDataException;

	/**
	 * Remove a specific query from iRODS
	 *
	 * @param specificQuery
	 *            {@link org.irods.jargon.core.pub.domain.SpecificQueryDefinition}
	 *            to be added to iRODS.
	 * @throws JargonException
	 *             for iRODS error
	 */
	void removeSpecificQuery(SpecificQueryDefinition specificQuery) throws JargonException;

	/**
	 * Remove a specific query from iRODS using alias name as identifier
	 *
	 * @param alias
	 *            {@code String} with a unique alias name for this SQL query
	 * @throws JargonException
	 *             for iRODS error
	 */
	void removeSpecificQueryByAlias(String alias) throws JargonException;

	/**
	 * Remove a specific query from iRODS using SQL query as identifier
	 * <p>
	 * Please note that this method will remove all existing Specific Queries that
	 * match the provided SQL query String
	 *
	 * @param sqlQuery
	 *            {@code String} with the a valid SQL query
	 * @throws JargonException
	 *             for iRODS error
	 *
	 */
	void removeAllSpecificQueryBySQL(String sqlQuery) throws JargonException;

	/**
	 * Execute a specific query by providing the alias that the sql had been
	 * registered under. These queries contain an sql statement that can include
	 * bind parameters. This method allows the optional specification of those
	 * parameters. Note that this variant of the query will not close the query out,
	 * instead, it supports paging by the specification of the {@code continueIndex}
	 * that may have been returned in a previous query paging call.
	 * <p>
	 * Note that a {@code DataNotFoundException} will occur if the query alias is
	 * not found.
	 *
	 * @param specificQuery
	 *            {@link SpecificQuery} that defines the query alias or sql, and any
	 *            associated parameters to use
	 * @param maxRows
	 *            {@code int} with the maximum number of rows to return. Note that
	 *            setting this to 0 causes the query to close automatically.
	 * @return {@link IRODSQueryResultSet} implementation with the result rows and
	 *         other information from the invocation of the query
	 * @throws DataNotFoundException
	 *             if the alias cannot be located
	 * @throws JargonException
	 *             general exception
	 * @throws JargonQueryException
	 *             exception in the forumulation of the query
	 */
	SpecificQueryResultSet executeSpecificQueryUsingAlias(SpecificQuery specificQuery, int maxRows)
			throws DataNotFoundException, JargonException, JargonQueryException;

	/**
	 * Execute a specific query by providing the alias that the sql had been
	 * registered under. These queries contain an sql statement that can include
	 * bind parameters. This method allows the optional specification of those
	 * parameters. Note that this variant of the query will not close the query out,
	 * instead, it supports paging by the specification of the {@code continueIndex}
	 * that may have been returned in a previous query paging call.
	 * <p>
	 * Note that a {@code DataNotFoundException} will occur if the query alias is
	 * not found.
	 *
	 * <p>
	 *
	 * This variant supports a common user practice with specific query, where the
	 * provided SQL has parameterized options for offset and result set size, for
	 * example, this query:
	 *
	 * <code>
	 * SELECT c.parent_coll_name, c.coll_name, c.create_ts, c.modify_ts, c.coll_id, c.coll_owner_name, c.coll_owner_zone, c.coll_type,
	 *  u.user_name, u.zone_name, a.access_type_id, u.user_id FROM r_coll_main c JOIN r_objt_access a ON c.coll_id = a.object_id
	 *  JOIN r_user_main u ON a.user_id = u.user_id WHERE c.parent_coll_name = ? LIMIT ? OFFSET ?
	 * </code>
	 *
	 * The 'userDefinedOffset' is a clue that sends this OFFSET value to the code
	 * that prepares the returned result set, so that the 'count' fields in the
	 * result accurately reflect the
	 *
	 *
	 * @param specificQuery
	 *            {@link SpecificQuery} that defines the query alias or sql, and any
	 *            associated parameters to use
	 * @param maxRows
	 *            {@code int} with the maximum number of rows to return. Note that
	 *            setting this to 0 causes the query to close automatically.
	 * @param userDefinedOffset
	 *            {@code int} that represents an offset to use in the returned
	 *            record counts that is enforced within the sql itself. This is used
	 *            because users often use LIMIT and OFFSET statements inside the
	 *            actual SQL to accomplish custom paging. This allows the result set
	 *            to reflect any user supplied offsets
	 * @return {@link IRODSQueryResultSet} implementation with the result rows and
	 *         other information from the invocation of the query
	 * @throws DataNotFoundException
	 *             if the alias cannot be located
	 * @throws JargonException
	 *             general exception
	 * @throws JargonQueryException
	 *             exception in the forumulation of the query
	 */
	SpecificQueryResultSet executeSpecificQueryUsingAlias(SpecificQuery specificQuery, int maxRows,
			int userDefinedOffset) throws DataNotFoundException, JargonException, JargonQueryException;

	/**
	 * Execute a specific query by providing the exact sql that was registered in
	 * iRODS. These queries contain an sql statement that can include bind
	 * parameters. This method allows the optional specification of those
	 * parameters. Note that this variant of the query will not close the query out,
	 * instead, it supports paging by the specification of the {@code continueIndex}
	 * that may have been returned in a previous query paging call.
	 *
	 * @param specificQuery
	 *            {@link SpecificQuery} that defines the query alias or sql, and any
	 *            associated parameters to use
	 * @param maxRows
	 *            {@code int} with the maximum number of rows to return. Note that
	 *            setting this to 0 causes the query to close automatically.
	 * @return {@link IRODSQueryResultSet} implementation with the result rows and
	 *         other information from the invocation of the query
	 * @throws DataNotFoundException
	 *             if the alias cannot be located
	 * @throws JargonException
	 *             general exception
	 * @throws JargonQueryException
	 *             exception in the forumulation of the query
	 */
	SpecificQueryResultSet executeSpecificQueryUsingSql(SpecificQuery specificQuery, int maxRows)
			throws DataNotFoundException, JargonException, JargonQueryException;

	/**
	 * Execute a specific query by providing the exact sql that was registered in
	 * iRODS. These queries contain an sql statement that can include bind
	 * parameters. This method allows the optional specification of those
	 * parameters. Note that this variant of the query will not close the query out,
	 * instead, it supports paging by the specification of the {@code continueIndex}
	 * that may have been returned in a previous query paging call.
	 *
	 * @param specificQuery
	 *            {@link SpecificQuery} that defines the query alias or sql, and any
	 *            associated parameters to use
	 * @param maxRows
	 *            {@code int} with the maximum number of rows to return. Note that
	 *            setting this to 0 causes the query to close automatically.
	 * @param userDefinedOffset
	 *            {@code int} that represents an offset to use in the returned
	 *            record counts that is enforced within the sql itself. This is used
	 *            because users often use LIMIT and OFFSET statements inside the
	 *            actual SQL to accomplish custom paging. This allows the result set
	 *            to reflect any user supplied offsets
	 * @return {@link IRODSQueryResultSet} implementation with the result rows and
	 *         other information from the invocation of the query
	 * @throws DataNotFoundException
	 *             if the alias cannot be located
	 * @throws JargonException
	 *             general exception
	 * @throws JargonQueryException
	 *             exception in the forumulation of the query
	 */
	SpecificQueryResultSet executeSpecificQueryUsingSql(SpecificQuery specificQuery, int maxRows, int userDefinedOffset)
			throws DataNotFoundException, JargonException, JargonQueryException;

	/**
	 * Given a portion of a query alias, find matching specific queries as stored in
	 * iRODS. Note that wildcards in the 'like' statement are not imposed by this
	 * method and must be provided by the caller in the provided
	 * {@code specificQueryAlias}.
	 *
	 * @param specificQueryAlias
	 *            {@code String} with a part of a query alias to search for.
	 * @return {@code List} of {@link SpecificQueryDefinition}
	 * @throws DataNotFoundException
	 *             if no queries found with a matching alias
	 * @throws JargonException
	 *             for iRODS error
	 */
	List<SpecificQueryDefinition> listSpecificQueryByAliasLike(String specificQueryAlias)
			throws DataNotFoundException, JargonException;

	/**
	 * Given a portion of a query alias, find matching specific queries as stored in
	 * iRODS. This variant allows provision of a zohe hint that indicates which
	 * federated zone to query. Note that wildcards in the 'like' statement are not
	 * imposed by this method and must be provided by the caller in the provided
	 * {@code specificQueryAlias}.
	 *
	 * @param specificQueryAlias
	 *            {@code String} with a part of a query alias to search for.
	 * @param zoneHint
	 *            {@code String} with a zone hint used to decide which federated
	 *            zone to query. Note that this should be set to blank if not used
	 *
	 * @return {@code List} of {@link SpecificQueryDefinition}
	 * @throws DataNotFoundException
	 *             if no queries found with a matching alias
	 * @throws JargonException
	 *             for iRODS error
	 */
	List<SpecificQueryDefinition> listSpecificQueryByAliasLike(String specificQueryAlias, String zoneHint)
			throws DataNotFoundException, JargonException;

	/**
	 * Given a specific query alias name, return the associated specific query
	 * definition information.
	 *
	 * @param specificQueryAlias
	 *            {@code String} with the given alias for the query
	 * @return {@link SpecificQueryDefinition} with details about the given query
	 * @throws DataNotFoundException
	 *             if the query with the given alias cannot be found
	 * @throws JargonException
	 *             for iRODS error
	 */
	SpecificQueryDefinition findSpecificQueryByAlias(String specificQueryAlias)
			throws DataNotFoundException, JargonException;

	/**
	 * Given a specific query alias name, return the associated specific query
	 * definition information.
	 *
	 * @param specificQueryAlias
	 *            {@code String} with the given alias for the query
	 * @param zoneHint
	 *            {@code String} with a zone hint used to decide which federated
	 *            zone to query. Note that this should be set to blank if not used
	 * @return {@link SpecificQueryDefinition} with details about the given query
	 * @throws DataNotFoundException
	 *             if the query with the given alias cannot be found
	 * @throws JargonException
	 *             for iRODS error
	 */
	SpecificQueryDefinition findSpecificQueryByAlias(String specificQueryAlias, String zoneHint)
			throws DataNotFoundException, JargonException;

	/**
	 * Check and see if, as a result of previous requests, or based on the server
	 * version, I know that the jargon specific queries required to support specific
	 * query via this API are not available. This method will return {@code true}
	 * only if I know that the support is not there. If I have not checked
	 * previously, or I am not using the dynamic properties cache, which is
	 * configured via {@link JargonProperties}, then a {@code false} will be
	 * returned.
	 *
	 * @return {@code boolean} that will only be {@code true} if I know that the
	 *         jargon specific query support is not configured. This can be used to
	 *         determine whether it is worth bothering to issue such requests.
	 *         <p>
	 *         Currently, this still needs to be wired into the specific query
	 *         support, so consider this experimental
	 * @throws JargonException
	 *             for iRODS error
	 */
	boolean isSpecificQueryToBeBypassed() throws JargonException;

	/**
	 * This is something of an overhead for the 3.0 release of eIRODS, check to see
	 * if I have specific query support. This needs to be done by actually trying a
	 * specific query. This should only be done once if the jargon properties are
	 * set to cache discovered server properties.
	 *
	 * @return {@code boolean} if I support specific query
	 * @throws JargonException
	 *             for iRODS error
	 */
	boolean isSupportsSpecificQuery() throws JargonException;

}
