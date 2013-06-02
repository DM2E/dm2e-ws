package eu.dm2e.ws.services.file;

// this defines the states a file can be in, in addition to the
// HTTP response when retrieving the file data.
public enum FileStatus {
	AVAILABLE, // the file can be retrieved
	WAITING, // the file is not yet ready
	DELETED // the file was deleted
}