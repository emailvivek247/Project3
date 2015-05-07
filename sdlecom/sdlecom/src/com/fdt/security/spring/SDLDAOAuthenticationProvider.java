package com.fdt.security.spring;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.authentication.dao.SaltSource;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.authentication.encoding.PlaintextPasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.Assert;

import com.fdt.common.util.client.ServiceStub;
import com.fdt.common.util.client.ServiceStubRS;
import com.fdt.security.entity.User;
import com.fdt.security.exception.UserNameNotFoundException;

public class SDLDAOAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    //~ Instance fields ================================================================================================

    private PasswordEncoder passwordEncoder = new PlaintextPasswordEncoder();

    private SaltSource saltSource;

    private ServiceStub serviceStub;

    private String nodeName = null;

    //~ Methods ========================================================================================================

    protected void additionalAuthenticationChecks(UserDetails userDetails,
    	UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        Object salt = null;

        if (this.saltSource != null) {
            salt = this.saltSource.getSalt(userDetails);
        }

        if (authentication.getCredentials() == null) {
            logger.debug("Authentication failed: no credentials provided");

            throw new BadCredentialsException(messages.getMessage(
            	"AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"), userDetails);
        }

        String presentedPassword = authentication.getCredentials().toString();

        if (!passwordEncoder.isPasswordValid(userDetails.getPassword(), presentedPassword, salt)) {
            logger.debug("Authentication failed: password does not match stored value");

            throw new BadCredentialsException(messages.getMessage(
                "AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"), userDetails);
        }
    }

    /**
     * Creates a successful {@link Authentication} object.<p>Protected so subclasses can override.</p>
     *  <p>Subclasses will usually store the original credentials the user supplied (not salted or encoded
     * passwords) in the returned <code>Authentication</code> object.</p>
     *
     * @param principal that should be the principal in the returned object (defined by the {@link
     *        #isForcePrincipalAsString()} method)
     * @param authentication that was presented to the provider for validation
     * @param user that was loaded by the implementation
     *
     * @return the successful authentication token
     */
    public Authentication createSuccessAuthentication(Object principal, Authentication authentication,
            UserDetails user) {
    	try {
    		/** If The User is Already Authenticated Do Not Update his Last Login Time **/
    		if (!authentication.isAuthenticated()) {
    			this.serviceStub.updateLastLoginTime(authentication.getName());
    		}
    	} catch (UserNameNotFoundException userNameNotFoundException) {
    		logger.debug("User Name Not Found" ,  userNameNotFoundException);
    	}
    	return super.createSuccessAuthentication(principal, authentication, user);
    }

    protected void doAfterPropertiesSet() throws Exception {
        Assert.notNull(this.serviceStub, "A Service Stub must be set");
    }

    protected final UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication)
            throws AuthenticationException {
        UserDetails loadedUser;

        try {
            loadedUser = this.serviceStub.loadUserByUsername(username, this.nodeName);
            if (loadedUser == null) {
            	throw new UsernameNotFoundException("User Not Found");
            }
            User ecomUser = (User)loadedUser;
            /** We need to do this to Avoid a Database Call **/
            ecomUser.setLastLoginTime(ecomUser.getCurrentLoginTime());
        } catch (UsernameNotFoundException notFound) {
            throw notFound;
        } catch (Exception repositoryProblem) {
            throw new AuthenticationServiceException(repositoryProblem.getMessage(), repositoryProblem);
        }

        if (loadedUser == null) {
            throw new AuthenticationServiceException(
            	"EComFacadeService returned null, which is an interface contract violation");
        }
        return loadedUser;
    }

    /**
     * Sets the PasswordEncoder instance to be used to encode and validate passwords.
     * If not set, the password will be compared as plain text.
     * <p>
     * For systems which are already using salted password which are encoded with a previous release, the encoder
     * should be of type {@code org.springframework.security.authentication.encoding.PasswordEncoder}. Otherwise,
     * the recommended approach is to use {@code org.springframework.security.crypto.password.PasswordEncoder}.
     *
     * @param passwordEncoder must be an instance of one of the {@code PasswordEncoder} types.
     */
    public void setPasswordEncoder(Object passwordEncoder) {
        Assert.notNull(passwordEncoder, "passwordEncoder cannot be null");

        if (passwordEncoder instanceof PasswordEncoder) {
            this.passwordEncoder = (PasswordEncoder) passwordEncoder;
            return;
        }

        if (passwordEncoder instanceof org.springframework.security.crypto.password.PasswordEncoder) {
            final org.springframework.security.crypto.password.PasswordEncoder delegate =
                    (org.springframework.security.crypto.password.PasswordEncoder)passwordEncoder;
            this.passwordEncoder = new PasswordEncoder() {
                public String encodePassword(String rawPass, Object salt) {
                    checkSalt(salt);
                    return delegate.encode(rawPass);
                }

                public boolean isPasswordValid(String encPass, String rawPass, Object salt) {
                    checkSalt(salt);
                    return delegate.matches(rawPass, encPass);
                }

                private void checkSalt(Object salt) {
                    Assert.isNull(salt, "Salt value must be null when used with crypto module PasswordEncoder");
                }
            };

            return;
        }

        throw new IllegalArgumentException("passwordEncoder must be a PasswordEncoder instance");
    }

    protected PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }

    /**
     * The source of salts to use when decoding passwords. <code>null</code>
     * is a valid value, meaning the <code>DaoAuthenticationProvider</code>
     * will present <code>null</code> to the relevant <code>PasswordEncoder</code>.
     * <p>
     * Instead, it is recommended that you use an encoder which uses a random salt and combines it with
     * the password field. This is the default approach taken in the
     * {@code org.springframework.security.crypto.password} package.
     *
     * @param saltSource to use when attempting to decode passwords via the <code>PasswordEncoder</code>
     */
    public void setSaltSource(SaltSource saltSource) {
        this.saltSource = saltSource;
    }

    protected SaltSource getSaltSource() {
        return saltSource;
    }

    public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public ServiceStub getServiceStub() {
		return serviceStub;
	}

	public void setServiceStubRS(ServiceStubRS serviceStubRS) {
		this.serviceStub = serviceStubRS;
	}

}