/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.cloud.gcs.doclib.service;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;

import java.io.IOException;

import java.util.Dictionary;
import java.util.Map;

import org.apache.felix.service.command.Descriptor;

import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Allen R. Ziegenfus
 */
@Component(
	immediate = true,
	property = {
		"osgi.command.function=listFiles", "osgi.command.function=countFiles",
		"osgi.command.scope=doclib"
	},
	service = GCSDoclibOSGICommands.class
)

public class GCSDoclibOSGICommands {

	@Descriptor("Count GCS Files")
	public void countFiles() throws PortalException {
		if (Validator.isNull(_bucketName)) {
			System.out.println("Bucket name not set");

			return;
		}

		Storage storage = StorageOptions.getDefaultInstance(
		).getService();

		System.out.println("Bucket: " + _bucketName);

		long sizeDuplicateFiles = 0;
		long sizeAdaptiveFiles = 0;
		long sizeDocThumbnailFiles = 0;
		long sizeDocPreviewFiles = 0;
		long sizeRegularFiles = 0;
		long sizeIndexFiles = 0;
		int numDuplicateFiles = 0;
		int numAdaptiveFiles = 0;
		int numDocThumbnailFiles = 0;
		int numDocPreviewFiles = 0;
		int numRegularFiles = 0;
		int numIndexFiles = 0;

		Page<Blob> blobs = storage.list(_bucketName);

		for (Blob blob : blobs.iterateAll()) {
			String blobName = blob.getName();

			if (blobName.contains("//")) {
				sizeDuplicateFiles += blob.getSize();
				numDuplicateFiles++;

				if ((sizeDuplicateFiles % 10000) == 0) {
					printFilesData(
						"duplicated", numDuplicateFiles, sizeDuplicateFiles);
				}
			}
			else if (blobName.contains("adaptive")) {
				sizeAdaptiveFiles += blob.getSize();
				numAdaptiveFiles++;

				if ((numAdaptiveFiles % 10000) == 0) {
					printFilesData(
						"adaptive", numAdaptiveFiles, sizeAdaptiveFiles);
				}
			}
			else if (blobName.contains("document_thumbnail")) {
				sizeDocThumbnailFiles += blob.getSize();
				numDocThumbnailFiles++;

				if ((numDocThumbnailFiles % 10000) == 0) {
					printFilesData(
						"document_thumbnail", numDocThumbnailFiles,
						sizeDocThumbnailFiles);
				}
			}
			else if (blobName.contains("document_preview")) {
				sizeDocPreviewFiles += blob.getSize();
				numDocPreviewFiles++;

				if ((numDocPreviewFiles % 10000) == 0) {
					printFilesData(
						"document_preview", numDocPreviewFiles,
						sizeDocPreviewFiles);
				}
			}
			else if (blobName.endsWith(".index")) {
				sizeIndexFiles += blob.getSize();
				numIndexFiles++;

				if ((numIndexFiles % 10000) == 0) {
					printFilesData(".index", numIndexFiles, sizeIndexFiles);
				}
			}
			else {
				sizeRegularFiles += blob.getSize();
				numRegularFiles++;

				if ((numRegularFiles % 10000) == 0) {
					printFilesData(
						"regular", numRegularFiles, sizeRegularFiles);
				}
			}
		}

		printFilesData(".index", numIndexFiles, sizeIndexFiles);
		printFilesData("duplicated", numDuplicateFiles, sizeDuplicateFiles);
		printFilesData("adaptive", numAdaptiveFiles, sizeAdaptiveFiles);
		printFilesData(
			"document_thumbnail", numDocThumbnailFiles, sizeDocThumbnailFiles);
		printFilesData(
			"document_preview", numDocPreviewFiles, sizeDocPreviewFiles);
		printFilesData("regular", numRegularFiles, sizeRegularFiles);
	}

	@Descriptor("List GCS Files")
	public void listFiles() throws PortalException {
		if (Validator.isNull(_bucketName)) {
			System.out.println("Bucket name not set");

			return;
		}

		Storage storage = StorageOptions.getDefaultInstance(
		).getService();

		System.out.println("Bucket: " + _bucketName);

		Page<Blob> blobs = storage.list(_bucketName);

		for (Blob blob : blobs.iterateAll()) {
			System.out.println(blob.getName());
		}
	}

	public void printFilesData(String fileType, int numFiles, long sizeFiles) {
		System.out.printf(
			"Number of %s files: %d, total size: %d\n", fileType, numFiles,
			sizeFiles);
	}

	@Activate
	protected void activate(
		Map<String, Object> properties, BundleContext bundleContext) {

		try {
			Configuration configuration = _configurationAdmin.getConfiguration(
				"com.liferay.portal.store.gcs.configuration." +
					"GCSStoreConfiguration",
				StringPool.QUESTION);

			Dictionary<String, Object> gcsProperties =
				configuration.getProperties();

			if (gcsProperties == null) {
				return;
			}

			_bucketName = GetterUtil.getString(gcsProperties.get("bucketName"));
		}
		catch (IOException ioException) {
			System.out.println("Could not get GCS configuration");
		}

		_bundleContext = bundleContext;
	}

	private String _bucketName;
	private BundleContext _bundleContext;

	@Reference
	private ConfigurationAdmin _configurationAdmin;

}