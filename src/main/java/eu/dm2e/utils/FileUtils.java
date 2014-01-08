package eu.dm2e.utils;

import eu.dm2e.ws.api.JobPojo;
import eu.dm2e.ws.services.Client;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.IOUtils;

import javax.ws.rs.core.Response;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * This file was created within the DM2E project.
 * http://dm2e.eu
 * http://github.com/dm2e
 * <p/>
 * Author: Kai Eckert, Konstantin Baierer
 */
public class FileUtils {

    private JobPojo jobPojo;
    private Client client;

    public FileUtils(Client client, JobPojo job) {
        this.jobPojo = job;
        this.client = client;
    }

    /**
     * @param archiveUrl
     * @param format
     * @return
     * @throws ZipException 
     */
    public String downloadAndExtractArchive(String archiveUrl, String format) throws ZipException {
        if (!"zip".equals(format)) throw new RuntimeException("Unsupported format: " + format);
        Response resp = client.target(archiveUrl).request().get();
        if (resp.getStatus() >= 400) {
            jobPojo.fatal("Could not download archive: " + resp.readEntity(String.class));
            jobPojo.setFailed();
            return null;
        }
        jobPojo.debug("File is available.");

        jobPojo.debug("Create tempfile");
        java.nio.file.Path zipfile;
        FileOutputStream zipfile_fos;
        try {
            zipfile = Files.createTempFile("omnom_archive_", ".zip");
        } catch (IOException e1) {
            jobPojo.fatal("Could not download archive: " + resp.readEntity(String.class));
            jobPojo.setFailed();
            return null;
        }

        jobPojo.debug("Archive will be stored as " + zipfile);

        try {
            zipfile_fos = new FileOutputStream(zipfile.toFile());
        } catch (FileNotFoundException e1) {
            jobPojo.fatal("Could not write out archive: " + resp.readEntity(String.class));
            jobPojo.setFailed();
            return null;
        }
        try {
            IOUtils.copy(resp.readEntity(InputStream.class), zipfile_fos);
        } catch (IOException e) {
            jobPojo.fatal("Could not store archive: " + e);
            jobPojo.setFailed();
            return null;
        } finally {
            IOUtils.closeQuietly(zipfile_fos);
        }
        jobPojo.debug("Archive was stored as " + zipfile);

        jobPojo.debug("Creating temp directory.");
        java.nio.file.Path zipdir;
        try {
            zipdir = Files.createTempDirectory("omnom_archive_");
        } catch (IOException e) {
            jobPojo.fatal(e);
            jobPojo.setFailed();
            return null;
        }
        jobPojo.debug("Temp directory is " + zipdir);

        jobPojo.debug("Unzipping " + zipfile + " to " + zipdir.toString());
        try {
            ZipFile zipFile = new ZipFile(zipfile.toFile());
            zipFile.extractAll(zipdir.toString());
        } catch (ZipException e) {
        	throw e;
        }
        return zipdir.toString();
    }


}
