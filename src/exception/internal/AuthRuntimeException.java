package exception.internal;

import org.discogs.exception.DiscogsAuthException;

public class AuthRuntimeException extends RuntimeException
{
    public AuthRuntimeException(DiscogsAuthException authException)
    {
	super(authException);
    }
}
