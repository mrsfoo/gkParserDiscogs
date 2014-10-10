package exception.internal;

import org.discogs.exception.FetchException;

public class FetchRuntimeException extends RuntimeException
{
    public FetchRuntimeException(FetchException fetchException)
    {
	super(fetchException);
    }
}
