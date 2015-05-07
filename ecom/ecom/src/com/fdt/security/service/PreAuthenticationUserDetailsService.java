package com.fdt.security.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.fdt.security.entity.User;

@Service("preUserDetailsService")
public class PreAuthenticationUserDetailsService implements AuthenticationUserDetailsService<Authentication> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private UserService userService = null;

    @Override
    public UserDetails loadUserDetails(Authentication authentication) throws UsernameNotFoundException {
        String userName = authentication.getPrincipal().toString();
        User user = this.userService.loadUserByUsername(userName, null);
        if (user == null) {
            throw new UsernameNotFoundException("User Not Found");
        }
        logger.debug("The PreAuthentincated User Details Are: " + user);
        return user;
    }
}
