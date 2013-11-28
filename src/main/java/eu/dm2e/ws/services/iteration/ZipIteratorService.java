package eu.dm2e.ws.services.iteration;

import eu.dm2e.utils.FileUtils;
import eu.dm2e.utils.UriUtils;
import eu.dm2e.ws.Config;
import eu.dm2e.ws.ConfigProp;
import eu.dm2e.ws.NS;
import eu.dm2e.ws.api.*;
import eu.dm2e.ws.model.JobStatus;
import eu.dm2e.ws.services.AbstractTransformationService;

import org.apache.commons.codec.digest.DigestUtils;
import org.joda.time.DateTime;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This file was created within the DM2E project.
 * http://dm2e.eu
 * http://github.com/dm2e
 * <p/>
 * Author: Kai Eckert, Konstantin Baierer
 */
@Path("/service/zip-iterator")
public class ZipIteratorService extends AbstractTransformationService {
    public static final String PARAM_ARCHIVE = "archive";
    public static final String PARAM_FORMAT = "format";
    public static final String PARAM_FILE_PATTERN = "filePattern";
    public static final String PARAM_OUTFILE = "outfile";
    public static final String FORMAT_ZIP = "zip";
    public static final String FORMAT_TAR_GZ = "tgz"; // not yet implemented
    public static final String FORMAT_TAR_BZ2 = "tbz"; // not yet implemented

    public ZipIteratorService() {
        final WebservicePojo ws = getWebServicePojo();
        ws.setLabel("Zip-Iterator");

        ParameterPojo infile = ws.addInputParameter(PARAM_ARCHIVE);
        infile.setLabel("Archive");
        infile.setIsRequired(true);

        ParameterPojo format = ws.addInputParameter(PARAM_FORMAT);
        format.setLabel("Format");
        format.setIsRequired(false);
        format.setDefaultValue(FORMAT_ZIP);
        format.setComment("Format of the archive, currently only zip is supported.");

        ParameterPojo pattern = ws.addInputParameter(PARAM_FILE_PATTERN);
        pattern.setLabel("Pattern");
        pattern.setIsRequired(false);
        pattern.setDefaultValue("*.xml");
        pattern.setComment("Pattern of filenames to be extracted, default is *.xml.");


        ParameterPojo out = ws.addOutputParameter(PARAM_OUTFILE);
        out.setLabel("Extracted File");
        out.setHasIterations(true);
    }


    private void findFiles(List<File> result, File dir, String pattern) {
        if (!dir.isDirectory()) throw new RuntimeException("Not a directory: " + dir.getAbsolutePath());
        for (File file:dir.listFiles()) {
            if (file.isDirectory()) {
                findFiles(result, file, pattern);
            } else {
                if (file.getName().matches(pattern)) result.add(file);
            }
        }
    }


    /**
     * GET /file/{uripart}
     * Retrieve contents of an extracted file
     *
     * @return
     */
    @GET
    @Path("/file/{uripart:.*}")
    public Response getFileById(@PathParam("uripart") String uripart) {
        try {
            log.info("Access requested to file URI: " + uripart);
            uripart = UriUtils.uriDecode(uripart);
            File tmpDir = new File(System.getProperty("java.io.tmpdir"));
            String filePath = tmpDir.getCanonicalPath() + "/omnom_archive_" + uripart;
            log.debug("filePath is: " + filePath);
            File file = new File(filePath);
            log.debug("Canonical: " + file.getCanonicalPath());
            if (!file.getCanonicalPath().startsWith(tmpDir.getCanonicalPath() + "/omnom_archive_")) {
                log.warn("BAD REQUEST to file: " + file.getCanonicalPath());
                return Response.status(Response.Status.BAD_REQUEST).entity(
                        "Access to " + uripart + "' not allowed!").build();
            }
            FileInputStream fis = null;
            fis = new FileInputStream(file);
            log.info("Returning content of " + filePath);
            return Response
                    .ok(fis)
                    .header("Content-Type", "application/octet-stream")
                    .header("Content-Disposition", "attachment; filename=" + file.getName())
                    .build();
        } catch (FileNotFoundException e) {
            log.info("File not found: " + e.getMessage());
            return Response.status(404).entity(
                    "File '" + uripart + "' not found on the server. ").build();
        } catch (IOException e) {
            log.warn("BAD REQUEST with uripart: " + uripart);
            return Response.status(Response.Status.BAD_REQUEST).entity(
                    "Access to " + uripart + "' not allowed!").build();
        }

    }

    @Override
    public void run() {
        JobPojo jobPojo = getJobPojo();
        FileUtils fileUtils = new FileUtils(client, jobPojo);
        try {
            jobPojo.debug("Zip Iterator starts to run now.");
            WebserviceConfigPojo wsConf = jobPojo.getWebserviceConfig();
            jobPojo.debug("wsConf: " + wsConf);

            String archiveUrl = wsConf.getParameterValueByName(PARAM_ARCHIVE);
            String format = wsConf.getParameterValueOrDefaultByName(PARAM_FORMAT);
            String pattern = wsConf.getParameterValueOrDefaultByName(PARAM_FILE_PATTERN).replaceAll("\\.", "\\\\.").replaceAll("\\*", "\\.\\*");
            jobPojo.debug("archiveUrl: " + archiveUrl);
            jobPojo.debug("format: " + format);
            jobPojo.debug("pattern: " + pattern);

            jobPojo.setStarted();
            String filedir = fileUtils.downloadAndExtractArchive(archiveUrl, format);
            String fileBase = filedir.substring(filedir.indexOf("/omnom_archive_") + "/omnom_archive_".length());
            List<File> files = new ArrayList<>();
            findFiles(files,new File(filedir),pattern);
            for (File file:files) {
                log.info("Found file: " + file.getCanonicalPath());
                String suffix = file.getCanonicalPath().substring(file.getCanonicalPath().indexOf(fileBase) + fileBase.length());
                String contentUrl = Config.get(ConfigProp.BASE_URI) + "service/zip-iterator/file/" + fileBase +  UriUtils.uriEncodePathElements(suffix);
                FilePojo filePojo = new FilePojo();
                filePojo.setCreated(new DateTime(file.lastModified()));
                filePojo.setExtent(file.length());
                filePojo.setFileRetrievalURI(contentUrl);
                FileInputStream fis = new FileInputStream(file);
                filePojo.setMd5(DigestUtils.md5Hex(fis));
                fis.close();
                filePojo.setFileType(NS.OMNOM_TYPES.XML);
                filePojo.setModified(new DateTime(file.lastModified()));
                filePojo.setOriginalName(file.getName());
                filePojo.setWasGeneratedBy(jobPojo);
                filePojo.setComment("Extracted from: " + archiveUrl);
                filePojo.setLabel(file.getName());
                filePojo.setExtent(file.length());
                String fileUri = client.publishFile(client.createFileFormDataMultiPart(filePojo,null));
                log.info("Content URL: " + contentUrl);
                log.info("URI: " + fileUri);
                jobPojo.addOutputParameterAssignment(PARAM_OUTFILE,fileUri);
                jobPojo.iterate();
            }

            jobPojo.debug("Zip-Iterator is finished now.");
            jobPojo.setStatus(JobStatus.FINISHED);
        } catch (Exception e) {
            jobPojo.setStatus(JobStatus.FAILED);
            jobPojo.fatal(e.toString());
            throw new RuntimeException(e);
        } finally {
            // client.publishPojoToJobService(jobPojo);
        }
    }
}
