package exception.internal;

import org.discogs.exception.DiscogsFetchException;

public class FetchRuntimeException extends RuntimeException
{
    public FetchRuntimeException(DiscogsFetchException fetchException)
    {
	super(fetchException);
    }
}
