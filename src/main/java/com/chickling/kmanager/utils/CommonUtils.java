package com.chickling.kmanager.utils;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chickling.kmanager.model.OffsetPoints;

/**
 * @author Hulva Luva.H
 *
 */
public class CommonUtils {
	private static Logger LOG = LoggerFactory.getLogger(CommonUtils.class);

	public static void sortByTimestampThenPartition(List<OffsetPoints> offsetPointsList) {
		Collections.sort(offsetPointsList, new Comparator<OffsetPoints>() {

			@Override
			public int compare(OffsetPoints o1, OffsetPoints o2) {
				int flag = o1.getTimestamp().compareTo(o2.getTimestamp());
				if (flag == 0) {
					return o1.getPartition().compareTo(o2.getPartition());
				} else {
					return flag;
				}
			}
		});
	}

	public static String loadFileContent(String filePath) {
		String contents = null;
		try {
			while (!(new File(filePath)).canRead()) {
				// TODO
				Thread.sleep(10);
			}
			contents = new String(Files.readAllBytes(Paths.get(filePath)));
		} catch (Exception e) {
			LOG.error("load file" + filePath + " failed!", e);
		}
		if (contents == null) {
			throw new RuntimeException("file is empty! Pls check file: " + filePath);
		}
		return contents;
	}

	public static URI getURI(String urlStr) {
		URL url = null;
		URI uri = null;
		try {
			url = new URL(urlStr);
			uri = new URI(url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(), null);
		} catch (Exception e) {
			LOG.error("URL error!");
			// TODO
			return null;
		}
		return uri;
	}
}
