package org.irods.jargon.core.pub;

import java.util.List;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.Quota;

/**
 * Manage and administer quota information. These functions require the invoking
 * user to have rodsadmin, due to the underlying use of simple query as well as
 * iadmin functions.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public interface QuotaAO extends IRODSAccessObject {

	/**
	 * List the quota for all users and groups by resource. This differs from the
	 * global quota information returned from the {@code listAllGlobalQuota()}
	 * method and may return multiple entries per user for each resource.
	 *
	 * @return {@code List} of {@link Quota} with information from the catalog. The
	 *         list will be empty if no results found.
	 * @throws JargonException
	 *             for iRODS error
	 */
	List<Quota> listAllQuota() throws JargonException;

	/**
	 * List the total or 'global' quota across resources for all users and groups.
	 * This differs from the per-resource quota from the {@code listAllQuota()}
	 * method
	 *
	 * @return {@code List} of {@link Quota} with information from the catalog. The
	 *         list will be empty if no results found.
	 * @throws JargonException
	 *             for iRODS error
	 */
	List<Quota> listAllGlobalQuota() throws JargonException;

	/**
	 * List the quota data for a given user and current zone, must be invoked by
	 * rodsAdmin
	 *
	 * @param userName
	 *            {@code String} with the user name
	 * @return {@code List} of {@link Quota} data, which may have multiple (per
	 *         resource) quota values.
	 * @throws JargonException
	 *             for iRODS error
	 */
	List<Quota> listQuotaForAUser(String userName) throws JargonException;

	/**
	 * Set the quota (total) for a given user. This is to be invoked by a rodsadmin.
	 *
	 * @param userName
	 *            {@code String} for which the quota will be set
	 * @param quotaValue
	 *            {@code long} with the number of bytes total across resources in
	 *            the zone.
	 * @throws JargonException
	 *             for iRODS error
	 */
	void setUserQuotaTotal(String userName, long quotaValue) throws JargonException;

	/**
	 * Set the quota (total) for a given user group. This is to be invoked by a
	 * rodsadmin
	 *
	 * @param userGroupName
	 *            {@code String} for the user group for which the quota will be set
	 * @param quotaValue
	 *            quotaValue {@code long} with the number of bytes total across
	 *            resources in the zone.
	 * @throws JargonException
	 *             for iRODS error
	 */
	void setUserGroupQuotaTotal(String userGroupName, long quotaValue) throws JargonException;

	/**
	 * Set the quota for the user and resource. This is to be invoked by a rodsadmin
	 *
	 * @param userName
	 *            {@code String} with the user name
	 * @param resourceName
	 *            {@code String} with the resource name to which the quota applies
	 * @param quotaValue
	 *            {@code long} with the number of bytes total for the resource in
	 *            this zone
	 * @throws JargonException
	 *             for iRODS error
	 */
	void setUserQuotaForResource(String userName, String resourceName, long quotaValue) throws JargonException;

	/**
	 * Set the quota for a user group and resource. This is to be invoked by a
	 * rodsadmin.
	 *
	 * @param userGroupName
	 *            {@code String} with the user group name
	 * @param resourceName
	 *            {@code String} with the resource name to which the quota applies
	 * @param quotaValue
	 *            {@code long} with the number of bytes total for the resource in
	 *            this zone
	 * @throws JargonException
	 *             for iRODS error
	 */
	void setUserGroupQuotaForResource(String userGroupName, String resourceName, long quotaValue)
			throws JargonException;

	/**
	 * Get the total 'global' quota set for a user in the current zone. This is to
	 * be invoked by a rodsadmin.
	 *
	 * @param userName
	 *            {@code String} with the user name
	 * @return {@link Quota} with 'total' in the resourceName and a value for the
	 *         total quota, or {@code null} if no such quota exists
	 * @throws JargonException
	 *             for iRODS error
	 */
	Quota getGlobalQuotaForAUser(String userName) throws JargonException;

	/**
	 * Cause the total quota usage to be calculated. This is to be invoked by a
	 * rodsadmin.
	 *
	 * @throws JargonException
	 *             for iRODS error
	 */
	void calculateQuotaUsage() throws JargonException;
}