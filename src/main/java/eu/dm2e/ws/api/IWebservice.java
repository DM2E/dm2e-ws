package eu.dm2e.ws.api;

/**
 * Interface for web services
 */
import java.util.Set;

public interface IWebservice {

	public abstract ParameterPojo addInputParameter(String paramName);

	public abstract ParameterPojo addOutputParameter(String paramName);

	public abstract ParameterPojo getParamByName(String needle);

	public abstract String getLabel();

	public abstract void setLabel(String label);

	public abstract Set<ParameterPojo> getInputParams();

	public abstract void setInputParams(Set<ParameterPojo> inputParams);

	public abstract Set<ParameterPojo> getOutputParams();

	public abstract void setOutputParams(Set<ParameterPojo> outputParams);

    public abstract String getImplementationID();

    public abstract void setImplementationID(String id);

}
