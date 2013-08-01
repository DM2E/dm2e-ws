package eu.dm2e.ws.services.file;

/**
 * This Enum defines the states a file can be in, in place of the 
 * HTTP response when retrieving the file data.
 * <p>
 * The main idea is that we cannot guarantee that file data is
 * available forever but the metadata can be.
 * If a file is deleted, the FileStatus of the file metadata is set to FileStatus.DELETED
 * </p>
 *
 * @author Konstantin Baierer
 */
public enum FileStatus {
	AVAILABLE, // the file can be retrieved
	WAITING, // the file is not yet ready
	DELETED // the file was deleted
}
